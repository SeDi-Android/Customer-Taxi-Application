package ru.sedi.customerclient.classes.Orders;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;

import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.CostCalculationResult;
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
        new AsyncJob.Builder<QueryList<_Tariff>>()
                .withProgress(context, R.string.cost_calculation)
                .doWork(() -> {
                    if (mOrder.getCostCalculationResult() != null) {
                        CostCalculationResult result = mOrder.getCostCalculationResult();
                        mOrder.setDistance(result.getRouteDistance());
                        mOrder.setDuration(result.getRouteDuration());
                        QueryList<_Tariff> tariffs = new QueryList<>();
                        tariffs.add(result.getTariff());
                        return tariffs;
                    } else {
                        return ServerManager.GetInstance().calculateCost(orderUrlBuilder(context, false, null));
                    }
                })
                .onSuccess(tariffs -> {
                    if (tariffs.isEmpty()) {
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

                    _Tariff[] array = tariffs.toArray(new _Tariff[tariffs.size()]);
                    BinaryArray binaryArray = new BinaryArray(array);

                    Intent i = new Intent(context, ChooseTariffActivity.class);
                    i.putExtra(ChooseTariffActivity.TARIFFS, binaryArray.GetBinary());
                    context.startActivity(i);
                })
                .onFailure(throwable -> MessageBox.show(context, throwable.getMessage(), null)).buildAndExecute();
    }

    public void register(final Context context, final String contactNumber, final IAction postAction) {
        final ProgressDialog pd = ProgressDialogHelper.show(context);
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
                        Collections.me().getAddressHistory().add(mOrder.getRoute().getPoints());
                        Collections.me().getActiveOrders().add(mOrder);
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
            params.put("costcalculationid", mOrder.getCostCalculationId());
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
