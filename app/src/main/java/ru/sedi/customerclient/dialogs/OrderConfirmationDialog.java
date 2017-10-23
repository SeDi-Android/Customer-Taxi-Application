package ru.sedi.customerclient.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.Validator;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.RouteHistory;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.activitys.customer_balance.CustomerBalanceActivity;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.IAction;


public class OrderConfirmationDialog extends AlertDialog.Builder {

    @BindView(R.id.tvMessage) TextView tvMessage;
    @BindView(R.id.etPhone) EditText etPhone;
    @BindView(R.id.etRouteTitle) EditText etRouteTitle;
    @BindView(R.id.cbSaveRoute) CheckBox cbSaveRoute;
    @BindView(R.id.cbCashless) CheckBox cbCashless;

    private final Context mContext;
    private final _Order mOrder;
    private final IAction mPostRegisterAction;

    public OrderConfirmationDialog(Context context, _Order order, IAction postRegistrationAction) {
        super(context);
        mContext = context;
        mOrder = order;
        mPostRegisterAction = postRegistrationAction;
        setView(getView(context));
        setPositiveButton(R.string.take_order, (dialog, which) -> onPositiveButtonClick(dialog));
        setNegativeButton(R.string.cancel, null);
    }

    private void onPositiveButtonClick(DialogInterface dialog) {
        String contactNumber = etPhone.getText().toString();
        if (!checkValidPhone(contactNumber)) {
            new OrderConfirmationDialog(mContext, mOrder, mPostRegisterAction).show();
            return;
        }

        if (!saveRoute(cbSaveRoute, etRouteTitle))
            return;

        if (cbCashless.isChecked()) {
            final double customerBalance = Collections.me().getUser().getBalance().getBalance();
            if (mOrder.getCost() <= customerBalance) {
                mOrder.setCashless(true);
            } else {
                showNeedPayDialog((int) customerBalance);
                return;
            }
        }

        _OrderRegistrator.me().register(mContext, contactNumber, mPostRegisterAction);
        dialog.dismiss();
    }

    private View getView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_confirm_dialog, null);

        ButterKnife.bind(this, view);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvMessage.setText(Html.fromHtml(getFullOrderData()));
        tvMessage.setMovementMethod(new ScrollingMovementMethod());

        cbCashless.setVisibility(App.isTaxiLive ? View.GONE : View.VISIBLE);
        cbSaveRoute.setOnCheckedChangeListener((buttonView, isChecked) ->
                etRouteTitle.setVisibility(cbSaveRoute.isChecked() ? View.VISIBLE : View.GONE));

        boolean contains = Collections.me().getRoutesHistory().contains(mOrder.getRoute().getPoints());
        cbSaveRoute.setVisibility(contains ? View.GONE : View.VISIBLE);

        String userPhone = Prefs.getString(PrefsName.REGISTER_USER_PHONE);
        etPhone.setText(userPhone);
        return view;
    }

    private boolean saveRoute(CheckBox cbSaveRoute, EditText etRouteTitle) {
        if (cbSaveRoute.isChecked()) {
            String title = etRouteTitle.getText().toString();
            if (title.isEmpty()) {
                MessageBox.show(mContext, mContext.getString(R.string.set_route_name_message));
                return false;
            } else {
                RouteHistory history = new RouteHistory(title, new QueryList<>(mOrder.getRoute().getPoints()));
                try {
                    Collections.me().getRoutesHistory().add(history);
                    return true;
                } catch (Exception e) {
                    MessageBox.show(mContext, e.getMessage());
                    return false;
                }
            }

        } else
            return true;
    }

    private boolean checkValidPhone(String s) {
        if (!Validator.Valid(Validator.PHONE_PATTERN, s)) {
            ToastHelper.showShortToast(mContext.getString(R.string.msg_incorrect_contact_phone));
            return false;
        }

        if (TextUtils.isEmpty(Prefs.getString(PrefsName.REGISTER_USER_PHONE)))
            Prefs.setValue(PrefsName.REGISTER_USER_PHONE, s);
        return true;
    }

    private String getFullOrderData() {
        try {
            String color = mContext.getString(R.color.primaryColor).replace("#ff", "#");
            String currency = Collections.me().getUser().getBalance().getCurrency();
            String checkedNames = Collections.me().getServices().getCheckedNames();
            String checkedServices = !TextUtils.isEmpty(checkedNames)
                    ? String.format(mContext.getString(R.string.msg_CalcCost_3), color, checkedNames)
                    : "";

            String cost = mOrder.getCost() <= 0
                    ? mContext.getString(R.string.FactFor)
                    : String.format("≈%.2f%s", mOrder.getCost(), currency);

            StringBuilder sb = new StringBuilder();
            if (mOrder.getRoute().size() > 1) {
                for (int i = 1; i < mOrder.getRoute().size(); i++) {
                    if (!sb.toString().isEmpty())
                        sb.append("→");
                    sb.append(mOrder.getRoute().getByIndex(i).asString(true));
                }
            }

            if (TextUtils.isEmpty(sb.toString()))
                sb.append(mContext.getString(R.string.DirectionsFor));

            String info = String.format(mContext.getString(R.string.msg_CalcCost_1),
                    color,
                    mOrder.isRush() ? mContext.getString(R.string.now_time) : mOrder.getDateTime().toString(DateTime.DATE_TIME),
                    color,
                    mOrder.getRoute().getByIndex(0).asString(true),
                    color,
                    sb.toString(),
                    checkedServices,
                    color,
                    cost);

            if (mOrder.getDiscount() > 0)
                info += String.format(mContext.getString(R.string.msg_CalcCost_2), color, mOrder.getDiscount(), currency);

            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void showNeedPayDialog(final int customerBalance) {
        MessageBox.show(mContext, mContext.getString(R.string.pay_necessary_sum_message), null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
                double missingAmount = mOrder.getCost() - customerBalance;
                Intent intent = new Intent(mContext, CustomerBalanceActivity.class);
                intent.putExtra(CustomerBalanceActivity.MISSING_SUM, missingAmount);
                mContext.startActivity(intent);
            }

            @Override
            public void onCancelClick() {
                super.onCancelClick();
            }
        }, true, new int[]{R.string.yes, R.string.no});
    }
}
