package ru.sedi.customerclient.activitys.user_registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.main_2.MainActivity2;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Validator;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.CountryCodes;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class UserPhoneRegisterActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.etRegionCode)
    EditText etRegionCode;
    @BindView(R.id.etMail)
    EditText etMail;
    @BindView(R.id.etPhone)
    EditText etPhone;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etPartnerCode)
    EditText etPartnerCode;
    @BindView(R.id.btnSend)
    Button btnSend;
    @BindView(R.id.cbGetKeyOnMail)
    CheckBox cbGetKeyOnMail;
    @BindView(R.id.cbHasPartnerCode)
    CheckBox cbHasPartnerCode;

    public static UserPhoneRegisterActivity Instance;

    public static Intent getIntent(Context context) {
        return new Intent(context, UserPhoneRegisterActivity.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        setContentView(R.layout.actvt_registration_phone);
        ButterKnife.bind(this);

        updateTitle(R.string.phone, R.drawable.ic_account_plus);
        InitUiElements();
    }

    private void InitUiElements() {
        final String code = CountryCodes.getCode(UserPhoneRegisterActivity.this,
                getPackageName());
        etRegionCode.setText(code);

        etPhone.setText(getDevicePhoneNumber(code));
        etPhone.setFilters(new InputFilter[]{getPhoneFilter()});
        etPhone.requestFocus();

        cbHasPartnerCode.setOnCheckedChangeListener(this);
        cbGetKeyOnMail.setOnCheckedChangeListener(this);

        btnSend.setOnClickListener(view -> {
            boolean isMailRegistration = cbGetKeyOnMail.isChecked();
            String data;

            if (isMailRegistration) {
                data = etMail.getText().toString();
                if (!Validator.valid(Validator.EMAIL_PATTERN, data)) {
                    MessageBox.show(UserPhoneRegisterActivity.this, getString(R.string.msg_IncorrectEmail), null);
                    etMail.requestFocus();
                    return;
                }
            } else {
                String phoneNumber = etPhone.getText().toString();
                String regionaleCode = "+" + etRegionCode.getText().toString();
                phoneNumber = phoneNumber.replace(regionaleCode, "");
                data = regionaleCode + phoneNumber;
                if (!Validator.valid(Validator.PHONE_PATTERN, data)) {
                    MessageBox.show(UserPhoneRegisterActivity.this, getString(R.string.bad_phone_number), null);
                    etPhone.requestFocus();
                    return;
                }
            }

            String name = etName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                MessageBox.show(UserPhoneRegisterActivity.this, getString(R.string.msg_IncorrectName));
                etName.requestFocus();
                return;
            }

            Prefs.setValue(PrefsName.PARTNER_KEY, etPartnerCode.getText().toString());
            showAuthActivity(data, name, isMailRegistration);
        });
    }

    /**
     * Получение номера телефона на данном устройстве.
     *
     * @return - номер телефона;
     */
    private String getDevicePhoneNumber(String code) {
        try {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (tMgr != null && !TextUtils.isEmpty(tMgr.getLine1Number())) {
                String number = tMgr.getLine1Number();
                number = number.replace(("+" + code), "");
                return number;
            } else return Const.EmptyStr;
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
            return Const.EmptyStr;
        }
    }

    private void showAuthActivity(final String data, final String name, final boolean byMail) {
        final ProgressDialog pd = ProgressDialogHelper.show(this, getString(R.string.send_key));
        AsyncAction.run(() -> {
            if (byMail)
                Prefs.setValue(PrefsName.REGISTER_USER_EMAIL, data);
            else
                Prefs.setValue(PrefsName.REGISTER_USER_PHONE, data);
            String userType = "Customer";
            return ServerManager.GetInstance().getSmsKey(data, byMail, userType);
        }, new IActionFeedback<Server>() {
            @Override
            public void onResponse(Server server) {
                if (pd != null)
                    pd.dismiss();

                if (server.isSuccess()) {
                    Intent intent = new Intent(UserPhoneRegisterActivity.this, UserKeyInputActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(UserKeyInputActivity.DATA, data);
                    intent.putExtra(UserKeyInputActivity.BY_MAIL, byMail);
                    intent.putExtra(UserKeyInputActivity.NAME, name);
                    startActivity(intent);
                } else
                    MessageBox.show(UserPhoneRegisterActivity.this, server.getResponceMessage(), null);
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();

                MessageBox.show(UserPhoneRegisterActivity.this, e.getMessage(), null);
            }
        });
    }

    private InputFilter getPhoneFilter() {
        final Character[] enableElements = new Character[]{'+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        InputFilter customFilter = (arg0, arg1, arg2, arg3, arg4, arg5) -> {
            for (int i = arg1; i < arg2; i++) {
                if (!Arrays.asList(enableElements).contains(arg0.charAt(i))) {
                    return "";
                }
            }
            return null;
        };
        return customFilter;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cbHasPartnerCode) {
            etPartnerCode.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }

        if (buttonView.getId() == R.id.cbGetKeyOnMail) {
            refreshView(isChecked);
        }
    }

    private void refreshView(boolean isMailRegister) {
        ((TextView) findViewById(R.id.tvRegistrationMessage)).setText(
                isMailRegister ? R.string.input_email_for_send_message : R.string.input_phone_for_sms_message);
        findViewById(R.id.llPhoneLayout).setVisibility(isMailRegister ? View.GONE : View.VISIBLE);
        btnSend.setText(isMailRegister ? R.string.GetEmail : R.string.GetSms);
        etMail.setVisibility(isMailRegister ? View.VISIBLE : View.GONE);
        if (isMailRegister)
            etMail.requestFocus();
        else
            etPhone.requestFocus();
    }

}