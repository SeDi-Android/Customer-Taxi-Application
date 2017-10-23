package ru.sedi.customerclient.activitys.customer_balance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.PaymentSystem;
import ru.sedi.customerclient.NewDataSharing._Bill;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Customer._Balance;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Validator;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.enums.PrefsName;


public class CustomerBalanceActivity extends BaseActivity {

    public static final String MISSING_SUM = "MISSING_SUM";
    private Context mContext;
    private PaymentSystem mSelectedPaymentSystem = null;
    private double mMissingAmount;

    @BindView(R.id.etPhone) EditText etPhone;
    @BindView(R.id.etSum) EditText etSum;
    @BindView(R.id.spnrPaymentSystem) Spinner spnrPaymentSystem;

    @BindView(R.id.tvBalance) TextView tvBalance;
    @BindView(R.id.tvCredit) TextView tvCredit;
    @BindView(R.id.tvLocked) TextView tvLocked;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.actvt_customer_balance);
        ButterKnife.bind(this);
        SediBus.getInstance().register(this);

        updateTitle(R.string.user_balance, R.drawable.ic_cash_multiple);
        trySetElevation(0);

        if (getIntent().hasExtra(MISSING_SUM))
            mMissingAmount = getIntent().getDoubleExtra(MISSING_SUM, 0);

        initDataForUiElements();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.isAuth) {
            showRegistrationDialog(this);
            return;
        }
        Collections.me().getUser().updateBalance(mContext, false);
    }

    @OnItemSelected(R.id.spnrPaymentSystem)
    @SuppressWarnings("unused")
    void onItemSelected(AdapterView<?> parent) {
        QueryList<PaymentSystem> paySystems = Collections.me().getPaySystems().getEnabled();
        mSelectedPaymentSystem = paySystems.get(parent.getSelectedItemPosition());
    }

    private void initDataForUiElements() {
        _Balance balance = Collections.me().getUser().getBalance();
        updateBalanceViewLayout(balance);

        String paymentPhone = Collections.me().getUser().getQiwiPaymentPhone();
        etPhone.setText(paymentPhone);

        if (mMissingAmount > 0) {
            etSum.setText(String.format("%.2f", mMissingAmount));
            ToastHelper.ShowLongToast(getString(R.string.set_need_sum_message));
        }

        boolean no_payment_systems = Collections.me().getPaySystems().getEnabled().isEmpty();
        findViewById(R.id.llPayLayout).setVisibility(no_payment_systems ? View.GONE : View.VISIBLE);

        spnrPaymentSystem.setAdapter(getAdapter());
        etSum.requestFocus();
    }

    @Subscribe
    public void updateBalanceViewLayout(_Balance balance) {
        if (balance == null) {
            return;
        }

        String currency = balance.getCurrency();
        tvBalance.setText(
                String.format(
                        Locale.ENGLISH, "%.1f %s",
                        balance.getBalance(),
                        currency));

        tvCredit.setText(
                String.format(
                        Locale.ENGLISH,
                        (getString(R.string.Credit) + "\n%.1f %s"),
                        balance.getCredit(),
                        currency));

        tvLocked.setText(
                String.format(
                        Locale.ENGLISH,
                        (getString(R.string.Locked) + "\n%.1f %s"),
                        balance.getLocked(),
                        currency));
    }

    private double getSumWithPercent(String sum) {
        if (mSelectedPaymentSystem == null) {
            MessageBox.show(mContext, getString(R.string.select_system));
            return 0d;
        }
        if (TextUtils.isEmpty(sum))
            return 0d;
        double parseDouble = 0d;
        try {
            parseDouble = Double.parseDouble(sum);
        } catch (NumberFormatException e) {
            sum = sum.replace(",", ".");
            getSumWithPercent(sum);
        }
        return parseDouble + (parseDouble * mSelectedPaymentSystem.getCustomer());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_order, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_update_item) {
            Collections.me().getUser().updateBalance(mContext, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SediBus.getInstance().unregister(this);
    }

    public SpinnerAdapter getAdapter() {
        QueryList<PaymentSystem> paySystems = Collections.me().getPaySystems().getEnabled();
        QueryList<String> names = new QueryList<>();

        for (PaymentSystem paySystem : paySystems) {
            int id = mContext.getResources().getIdentifier(paySystem.getPaymentSystem().getID(), "string", mContext.getPackageName());
            if (id > 0)
                names.add(mContext.getString(id));
            else
                names.add(paySystem.getPaymentSystem().getID());
        }
        return new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, names);
    }

    @OnClick(R.id.btnRechargeBalance)
    public void bill() {
        if (mSelectedPaymentSystem == null) {
            MessageBox.show(mContext, getString(R.string.select_system));
            return;
        }

        final String phone = etPhone.getText().toString();
        final double sum = getSumWithPercent(etSum.getText().toString());

        //Проверяем номер телефона
        if (!Validator.Valid(Validator.PHONE_PATTERN, phone)) {
            LogUtil.log(LogUtil.ERROR, "Номер телефона имеет неверный формат!");
            MessageBox.show(mContext, R.string.bad_phone_number, -1);
            return;
        }
        Prefs.setValue(PrefsName.QIWI_PAYMENT_PHONE, phone);

        //Проверяем сумму
        if (sum < 1) {
            LogUtil.log(LogUtil.ERROR, "Сумма платежа имеет неверный формат!");
            MessageBox.show(mContext, R.string.bad_payment_sum_message, -1);
            return;
        }

        String currency = Collections.me().getUser().getBalance().getCurrency();
        double percent = mSelectedPaymentSystem.getCustomer() * 100;
        final String systemName = mSelectedPaymentSystem.getPaymentSystem().getID();

        MessageBox.show(mContext,
                String.format(
                        getString(R.string.msg_QiwiPaymentSumQuestion),
                        sum,
                        currency,
                        percent,
                        systemName), null,
                new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        perfomBill(phone, sum, systemName);
                    }
                }, true, new int[]{R.string.yes, R.string.no});
    }

    private void perfomBill(final String phone, final double sum, final String system) {
        final SweetAlertDialog pd = ProgressDialogHelper.show(this);
        AsyncAction.run(() -> ServerManager.GetInstance().bill(phone, sum, system),
                new IActionFeedback<_Bill>() {
                    @Override
                    public void onResponse(_Bill bill) {
                        if (pd != null)
                            pd.dismiss();

                        //Если выбран Qiwi-кошелек, вывести другое сообщение,
                        //чем при простом платеже.
                        if (bill.isQiwiWallet()) {
                            showQiwiWalletPayMessage(bill);
                        } else {
                            showUrlPayMessage(bill);
                        }
                        etSum.setText("");
                        spnrPaymentSystem.setSelection(0);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (pd != null)
                            pd.dismiss();
                        MessageBox.show(mContext, e.getMessage());
                    }
                });
    }

    private void showQiwiWalletPayMessage(_Bill bill) {
        StringBuilder builder = new StringBuilder();
        builder
                .append("Счет успешно выставлен.")
                .append("\n")
                .append("Для его оплаты сделайте перевод на QIWI-кошелек ")
                .append(bill.getPayeePurse())
                .append(" c QIWI-кошелька ")
                .append(bill.getPayerPhone())
                .append(". \n\n")
                .append("Платеж будет проведен в течении 20 минут.")
                .append("\n\n")
                .append("При платеже из других источников обязательно укажите в комментарии к платежу номер ")
                .append(bill.getPayerPhone());

        MessageBox.show(this, builder.toString(), null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                Helpers.copyToClipboard(mContext, bill.getPayeePurse());
                ToastHelper.ShowLongToast("Номер для оплаты успешно скопирован");
            }
        }, true, new int[]{R.string.copy_number, R.string.close});
    }

    private void showUrlPayMessage(final _Bill bill) {
        MessageBox.show(mContext, "Счет " + bill.getStatus().getName() + ". Войдите в свой аккаунт и оплатите счет.", null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
            }

            @Override
            public void onCancelClick() {
                super.onCancelClick();
                try {
                    String url = bill.getPayUrl();
                    if (TextUtils.isEmpty(url))
                        url = "https://qiwi.ru/";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                } catch (Exception e) {
                    LogUtil.log(LogUtil.ERROR, e.getMessage());
                }
            }
        }, true, new int[]{R.string.close, R.string.OpenQiwiSuit});
    }
}