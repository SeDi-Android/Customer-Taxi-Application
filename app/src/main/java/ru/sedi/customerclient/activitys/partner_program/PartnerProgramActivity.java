package ru.sedi.customerclient.activitys.partner_program;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.PartnerMessageHolder;
import ru.sedi.customerclient.classes.Validator;
import ru.sedi.customerclient.common.CountryCodes;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.enums.InvitationTypes;
import ru.sedi.customerclient.enums.UserTypes;


public class PartnerProgramActivity extends BaseActivity {

    public static final int LAYOUT = R.layout.activity_partner_program;
    private static final int REQUEST_CODE_CONTACT_PICK = 0;

    private PartnerMessageHolder mMessageHolder = new PartnerMessageHolder();

    @BindView(R.id.etPhone)
    EditText etPhone;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etMessage)
    EditText etMessage;
    @BindView(R.id.rgAppType)
    RadioGroup rgAppType;
    @BindView(R.id.tvInfo)
    TextView tvInfo;
    private int mAccountId;


    public static Intent getIntent(Context context) {
        return new Intent(context, PartnerProgramActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        updateTitle(R.string.PartnerProgram, R.drawable.ic_share_variant);
        ButterKnife.bind(this);

        mAccountId = Collections.me().getUser().getAccountID();
        updateInvitationMessage();
        showSupportInfo();

        rgAppType.setOnCheckedChangeListener((radioGroup, i) -> {
            updateInvitationMessage();
            showChangeMessageToast();
        });
        etPhone.setOnFocusChangeListener((view, b) -> {
            if (!b) updateInvitationMessage();
        });
    }

    private void showChangeMessageToast() {
        Toast.makeText(this, R.string.change_text_message, Toast.LENGTH_SHORT).show();
    }

    private void updateInvitationMessage() {
        UserTypes userType = getSelectedUserType();
        InvitationTypes invitationType = InvitationTypes.ByDistributorAccountId;
        String phone = etPhone.getText().toString().replaceAll("[^0-9+]", "");

        if (!TextUtils.isEmpty(phone))
            invitationType = InvitationTypes.ByPhoneNumber;


        requestInvitationMessage(userType, invitationType);
    }


    //region Requests
    private void requestInvitationMessage(UserTypes userType, InvitationTypes invitationType) {
        String restoredMessage = getSavedInvitationMessage(userType, invitationType);

        if (!TextUtils.isEmpty(restoredMessage)) {
            setMessageText(restoredMessage);
            return;
        }

        new AsyncJob.Builder<String>()
                .withProgress(this, null)
                .doWork(() ->
                        ServerManager.GetInstance().getInvitationText(mAccountId, userType,
                                invitationType)
                )
                .onSuccess(message -> {
                    saveMessageText(message, userType, invitationType);
                    setMessageText(message);
                })
                .onFailure(exception -> MessageBox.show(this, exception.getMessage()))
                .buildAndExecute();
    }

    //endregion

    //region Work with MessageHolder

    private String getSavedInvitationMessage(UserTypes userTypes, InvitationTypes invitationType) {
        String message = "";
        switch (userTypes) {
            case customer: {
                switch (invitationType) {
                    case ByPhoneNumber: {
                        message = mMessageHolder.getCutomerIndividualMessage();
                        break;
                    }
                    case ByDistributorAccountId: {
                        message = mMessageHolder.getCustomerSimpleMessage();
                        break;
                    }
                }
                break;
            }

            case employee:
                switch (invitationType) {
                    case ByPhoneNumber: {
                        message = mMessageHolder.getDriverIndividualMessage();
                        break;
                    }
                    case ByDistributorAccountId: {
                        message = mMessageHolder.getDriverSimpleMessage();
                        break;
                    }
                }
                break;
        }
        return message;
    }

    private void saveMessageText(String message, UserTypes userType, InvitationTypes invitationType) {
        switch (invitationType) {
            case ByDistributorAccountId:
                saveSimpleMessageText(message, userType);
                break;
            case ByPhoneNumber:
                saveIndividualMessageText(message, userType);
                break;
        }
    }

    private void saveIndividualMessageText(String messageText, UserTypes userType) {
        switch (userType) {
            case customer:
                mMessageHolder.setCutomerIndividualMessage(messageText);
                break;
            case employee:
                mMessageHolder.setDriverIndividualMessage(messageText);
                break;
        }
    }

    private void saveSimpleMessageText(String message, UserTypes userType) {
        switch (userType) {
            case customer:
                mMessageHolder.setCustomerSimpleMessage(message);
                break;
            case employee:
                mMessageHolder.setDriverSimpleMessage(message);
                break;
        }
    }
    //endregion

    private void setMessageText(String messageText) {
        etMessage.setText(messageText);
    }

    private void showSupportInfo() {
        String textInfo = "";
        //Special promo for TAXIMASTER
        if (getPackageName().equalsIgnoreCase(Const.MASTER_PACKAGE_NAME)) {
            textInfo = "Отправьте сообщение с ссылкой на мобильное приложение такси Мастер Вашим родным, " +
                    "друзьям, просто знакомым и получайте 5% с каждой их поездки в нашем такси!" +
                    " Срок действия партнерской программы не ограничен!";
        }
        tvInfo.setText(textInfo);
    }

    private String getPartnerUrl(String phone, String name, UserTypes type) {
        String partnerUrl = "";
        partnerUrl += "cmd=invite";
        partnerUrl += "&username=" + name;
        partnerUrl += "&promocode=" + mAccountId;
        partnerUrl += "&userphone=" + encode(phone);
        partnerUrl += "&usertype=" + type.name();
        partnerUrl += "&apikey=" + getString(R.string.sediApiKey);
        return partnerUrl;
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    //region OnClicks
    @OnClick(R.id.ibtnContactList)
    @SuppressWarnings("unused")
    public void onContactListClick() {
        if (!AppPermissionHelper.checkPermission(this, Manifest.permission.READ_CONTACTS)) {
            Toast.makeText(this, "Missing permission", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_CONTACT_PICK);
    }

    @OnClick(R.id.btnSend)
    @SuppressWarnings("unused")
    public void onSendClick() {
        createCustomerInvitation();
    }
    //endregion

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONTACT_PICK && resultCode == RESULT_OK) {
            updateContactData(data);
            updateInvitationMessage();
        }
    }

    private void createCustomerInvitation() {
        String phone = etPhone.getText().toString().replaceAll("[^0-9+]", "");

        if (TextUtils.isEmpty(phone)) {
            Helpers.showShareChooserDialog(this, etMessage.getText().toString());
            return;
        }

        if (!Validator.valid(Validator.PHONE_PATTERN, phone)) {
            MessageBox.show(this, R.string.invalid_phone_number);
            return;
        }

        String name = etName.getText().toString();

        new AsyncJob.Builder<Boolean>()
                .withProgress(this, R.string.generate_invite)
                .doWork(() -> {
                    String hash = ServerManager.GetInstance().getHash(getPartnerUrl(phone, name,
                            getSelectedUserType()));
                    if (TextUtils.isEmpty(hash))
                        throw new NoSuchFieldException(getString(R.string.invitation_isnt_kept_message));
                    return true;
                })
                .onFailure(throwable -> MessageBox.show(this, throwable.getMessage()))
                .onSuccess(suscess -> {
                    Helpers.showShareChooserDialog(this, phone, etMessage.getText().toString());
                })
                .buildAndExecute();
    }

    private void updateContactData(Intent data) {
        //Select contact ID and Name
        String[] fields = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(data.getData(), fields, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }
        cursor.moveToFirst();

        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        etName.setText(name);

        cursor.close();

        //Select phone number by contact ID
        String selection = String.format(Locale.getDefault(), "contact_id = %1$d AND data2 = %2$d", id, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, selection, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return;
        }
        cursor.moveToFirst();
        String phoneNumber = cursor.getString(0);
        if (!TextUtils.isEmpty(phoneNumber))
            phoneNumber = correctPhoneNumber(phoneNumber);

        etPhone.setText(phoneNumber);
    }

    private String correctPhoneNumber(String phone) {
        if (!phone.contains("+")) {
            if (phone.startsWith("0") || phone.startsWith("8")) {
                String newCountyCode = "+" + CountryCodes.getCode(this, getPackageName());
                phone = phone.replaceFirst(String.valueOf(phone.charAt(0)), newCountyCode);
            }
        }
        return phone.replace("-", "").replace(" ", "");
    }

    public UserTypes getSelectedUserType() {
        int checkedId = rgAppType.getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.rbDriverApp:
                return UserTypes.employee;
            default:
                return UserTypes.customer;
        }
    }
}
