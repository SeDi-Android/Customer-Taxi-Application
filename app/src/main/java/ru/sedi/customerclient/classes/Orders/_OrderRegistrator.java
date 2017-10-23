package ru.sedi.customerclient.classes.Orders;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._OrderRegisterAnswer;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.Otto.OrderRegisterEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.choose_tariff.ChooseTariffActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.TinyBinaryFormatter.BinaryArray;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.IAction;

public class _OrderRegistrator {
    private static _OrderRegistrator ourInstance = new _OrderRegistrator();
    private _Order mOrder = new _Order();
    private _Order mLastCalcOrder = null;
    private OnNewOrderCreateListener mOrderCreateListener;

    public static _OrderRegistrator me() {
        return ourInstance;
    }

    private _OrderRegistrator() {
    }

    public _Order getOrder() {
        return mOrder;
    }

    public void calculateAndShow(final Context context) {
        final SweetAlertDialog pd = ProgressDialogHelper.show(context, context.getString(R.string.cost_calculation));
        AsyncAction.run(() -> ServerManager.GetInstance().calculateCost(orderUrlBuilder(context, false, null)), new IActionFeedback<QueryList<_Tariff>>() {
            @Override
            public void onResponse(QueryList<_Tariff> result) {
                if (pd != null)
                    pd.dismiss();
                if (result.isEmpty()) {
                    MessageBox.show(context, context.getString(R.string.msg_CalcCostFail), null, new UserChoiseListener() {
                        @Override
                        public void OnOkClick() {
                            super.OnOkClick();
                            calculateAndShow(context);
                        }

                        @Override
                        public void onCancelClick() {
                            super.onCancelClick();
                        }
                    }, true, new int[]{R.string.yes, R.string.no});
                    return;
                }

                _Tariff[] array = result.toArray(new _Tariff[result.size()]);
                BinaryArray binaryArray = new BinaryArray(array);

                Intent i = new Intent(context, ChooseTariffActivity.class);
                i.putExtra(ChooseTariffActivity.TARIFFS, binaryArray.GetBinary());
                context.startActivity(i);
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();
                MessageBox.show(context, e.getMessage(), null);
            }
        });
    }

    public void calculate(Context context) {
        if (getOrder().getRoute().size() < 2) {
            _Tariff tariff = Collections.me().getTariffs().getAll().tryGet(0);
            if (!Collections.me().enableEasyCostCalculate())
                tariff = null;
            _OrderRegistrator.me().getOrder().setTariff(tariff);
            mLastCalcOrder = null;
            return;
        }

        if (mLastCalcOrder != null && mLastCalcOrder.equals(mOrder)) {
            _OrderRegistrator.me().getOrder().updateListeners();
            return;
        }

        _Order.OnChangeListener listener = mOrder.getChangeListener();
        if (listener != null)
            listener.onStartCalculate();

        new AsyncJob.Builder<QueryList<_Tariff>>()
                .doWork(() -> ServerManager.GetInstance().calculateCost(orderUrlBuilder(context, false, null)))
                .onSuccess(tariffs -> {
                    if (tariffs.isEmpty() || tariffs.size() > 1) return;
                    _OrderRegistrator.me().getOrder().setTariff(tariffs.get(0));
                    mLastCalcOrder = mOrder.fullCopy();
                    mLastCalcOrder.setSpecs(Collections.me().getServices().getCheckedNameId());
                })
                .onFailure(throwable -> {
                    LogUtil.log(LogUtil.ERROR, throwable.getMessage());
                    if(listener!=null)
                        listener.OnFailureCalculate(throwable.getMessage());
                })
                .buildAndExecute();
    }

    public void register(final Context context, final String contactNumber, final IAction postAction) {
        final SweetAlertDialog pd = ProgressDialogHelper.show(context);
        AsyncAction.run(() -> ServerManager.GetInstance().registrationOrderOnServer(orderUrlBuilder(context, true, contactNumber)), new IActionFeedback<_OrderRegisterAnswer>() {
            @Override
            public void onResponse(_OrderRegisterAnswer result) {
                if (pd != null)
                    pd.dismiss();

                if (!result.isSuccess()) {
                    MessageBox.show(context, result.getMessage(), null, new UserChoiseListener() {
                        @Override
                        public void OnOkClick() {
                            super.OnOkClick();
                            calculateAndShow(context);
                        }

                        @Override
                        public void onCancelClick() {
                            super.onCancelClick();
                        }
                    }, true, new int[]{R.string.yes, R.string.no});
                    return;
                }

                MessageBox.show(context, result.getMessage(), null, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        mOrder.setID(result.getObjectId());
                        for (_Point point : mOrder.getRoute().getPoints())
                            Collections.me().getAddressHistory().add(point);

                        Collections.me().getActiveOrders().add(mOrder.fullCopy());
                        resetLastOrder();
                        if (postAction != null)
                            postAction.action();
                        SediBus.getInstance().post(new OrderRegisterEvent());
                    }
                }, false, new int[]{R.string.ok});
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();
                if (e.getMessage().equals("loginincorrect")) {
                    App.isAuth = false;
                    ((BaseActivity) context).showRegistrationDialog(context);
                    return;
                }
                MessageBox.show(context, e.getMessage(), null);
            }
        });
    }

    private RequestParams orderUrlBuilder(Context context, boolean isRegistration, String contactNumber) throws Exception {
        RequestParams params = new RequestParams();
        //Время подачи
        if (mOrder.isRush())
            mOrder.setDateTime(DateTime.Now());

        //Если разрешена скидка по ключевому слову, и оно указано
        if (Prefs.getBool(PrefsName.ENABLE_PROMO)
                && Prefs.getString(PrefsName.PROMO_KEY).length() > 1) {
            params.add("keyword", Prefs.getString(PrefsName.PROMO_KEY));
        }

        params.put("ordertype", mOrder.isRush() ? "rush" : "preliminary");
        params.put("date", mOrder.getDateTime().toString(DateTime.WEB_DATE));

        //Если отмечены дополнительные сервисы
        String serviceIds = Collections.me().getServices().getCheckedIds();
        if (!TextUtils.isEmpty(serviceIds)) {
            params.add("specs", serviceIds);
        }

        //Если адресов нет то ничего не делать
        if (!mOrder.getRoute().getByIndex(0).getChecked())
            return null;


        for (int i = 0; i < mOrder.getRoute().size(); i++)
            params = mOrder.getRoute().getByIndex(i).asRequestParam(i, params);

        //Если регистрация добавить доп. параметры
        if (isRegistration) {
            String tariffId = mOrder.getTariff().getID();
            if (tariffId.isEmpty())
                throw new Exception(context.getString(R.string.select_tariff));
            params.put("tariff", tariffId);
            params.put("details", mOrder.getDetails());
            params.put("isminimumcost", mOrder.isMinimumcost());
            params.put("cost", mOrder.getCost());
            params.put("name", Collections.me().getUser().getName());
            params.put("phone", contactNumber);
            params.put("comment", mOrder.getDescription());
            params.put("cashless", mOrder.isCashless());
        }
        return params;
    }

    public void resetLastOrder() {
        mOrder = new _Order();
        mLastCalcOrder = null;
        Collections.me().getServices().reset();
        if (mOrderCreateListener != null)
            mOrderCreateListener.onCreateOrder();
    }

    public void setOrderCreateListener(OnNewOrderCreateListener orderCreateListener) {
        mOrderCreateListener = orderCreateListener;
    }

    public void copyFromHistory(_Order order) {
        resetLastOrder();
        _Route route = order.getRoute();
        for (_Point point : route.getPoints())
            point.setChecked(true);
        mOrder.setRoute(route);
        mOrder.setSpecs(order.getSpecs());
        Collections.me().getServices().setChecked(mOrder.getSpecs());
        mOrder.setDescription(order.getDescription());
    }
}
