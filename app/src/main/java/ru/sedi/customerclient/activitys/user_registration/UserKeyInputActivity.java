package ru.sedi.customerclient.activitys.user_registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;


public class UserKeyInputActivity extends BaseActivity {

    public static final String DATA = "data";
    public static final String BY_MAIL = "byMail";
    public static final String NAME = "name";

    @BindView(R.id.etSmsKey)
    EditText etSmsKey;
    @BindView(R.id.tvMessage)
    TextView tvMessage;
    @BindView(R.id.btnSend)
    Button btnSend;

    private String mData, mUserName = "User";
    private boolean byMail;
    private Context mContext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.actvt_registration_key);
        ButterKnife.bind(this);

        updateTitle(R.string.key, R.drawable.ic_account_plus);

        if (getIntent() != null) {
            if (getIntent().hasExtra(DATA))
                mData = getIntent().getStringExtra(DATA);
            if (getIntent().hasExtra(BY_MAIL))
                byMail = getIntent().getBooleanExtra(BY_MAIL, false);
            if (getIntent().hasExtra(NAME))
                mUserName = getIntent().getStringExtra(NAME);

        } else {
            finish();
        }
        initUiElements();
    }

    private void initUiElements() {
        etSmsKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    btnSend.performClick();
                    return true;
                }
                return false;
            }
        });
        etSmsKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    Helpers.hideKeyboard(etSmsKey);
                    btnSend.performClick();
                }
            }
        });
        tvMessage.setText(String.format("%s: %s", tvMessage.getText(), mData));
        btnSend.setOnClickListener(view -> {
            String smsKey = etSmsKey.getText().toString();
            if (smsKey.length() != 4) {
                MessageBox.show(UserKeyInputActivity.this, R.string.bad_key_lenght_message, -1);
                return;
            }
            registrationUserBySmsKey(smsKey);
        });
    }

    private void registrationUserBySmsKey(final String smsKey) {
        new AsyncJob.Builder<_LoginInfo>()
                .withProgress(mContext, R.string.save_user_message)
                .doWork(() -> {
                    _LoginInfo user = ServerManager.GetInstance().getUser(smsKey, mUserName);
                    if (user == null)
                        throw new Exception("Данные о пользователе недоступны. Попробуйте позднее.");

                    Prefs.setValue(PrefsName.USER_KEY, user.getUserKey());
                    //Партнерский ключ
                    String partnerCode = Prefs.getString(PrefsName.PARTNER_KEY);
                    if (!TextUtils.isEmpty(partnerCode))
                        ServerManager.GetInstance().updatePartnerCode(partnerCode);

                    //Токен FIREBASE
                    String key = Prefs.getString(PrefsName.FIREBASE_TOKEN);
                    if (!TextUtils.isEmpty(key))
                        ServerManager.GetInstance().sendTokenOnServer(key);
                    return user;
                })
                .onSuccess(loginInfo -> {
                    App.isAuth = true;
                    Collections.me().setUser(loginInfo);

                    closeActivity(UserPhoneRegisterActivity.Instance);
                    closeActivity(UserKeyInputActivity.this);
                })
                .onFailure(throwable -> MessageBox.show(mContext, throwable.getMessage()))
                .buildAndExecute();
    }


    private void resendSmsKey() {
        final ProgressDialog pd = ProgressDialogHelper.show(this, getString(R.string.resend_key_message));
        String userType = "Customer";
        AsyncAction.run(() -> ServerManager.GetInstance().getSmsKey(mData, byMail, userType),
                new IActionFeedback<Server>() {
                    @Override
                    public void onResponse(Server server) {
                        if (pd != null)
                            pd.dismiss();

                        MessageBox.show(UserKeyInputActivity.this, server.getResponceMessage(), null);
                        etSmsKey.setText(Const.EmptyStr);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (pd != null)
                            pd.dismiss();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_registration_key, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_resend_item) {
            if (mData.length() > 0) {
                MessageBox.show(UserKeyInputActivity.this, R.string.resend_key_question_message, -1, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        resendSmsKey();
                    }

                    @Override
                    public void onCancelClick() {
                        super.onCancelClick();
                    }
                }, true, new int[]{R.string.repeat, R.string.no});
            }
            LogUtil.log(LogUtil.INFO, "Пользователь отправил ключ на : " + mData);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}