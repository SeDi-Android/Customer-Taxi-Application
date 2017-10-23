package ru.sedi.customerclient.activitys.partner_program;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.common.CountryCodes;
import ru.sedi.customerclient.common.MessageBox.MessageBox;


public class PartnerProgramActivity extends BaseActivity {

    public static final int LAYOUT = R.layout.activity_partner_program;
    private static final int REQUEST_CODE_CONTACT_PICK = 0;

    private final String CUSTOMER = "customer";
    private final String EMPLOYEE = "employee";
    private final String URL = "http://%s/m/apps/invite/index.htm?apikey=%s&usertype=%s&userphone=%s&username=%s&promocode=%d";

    @BindView(R.id.etPhone) EditText etPhone;
    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.etMessage) EditText etMessage;
    @BindView(R.id.rgAppType) RadioGroup rgAppType;
    @BindView(R.id.tvInfo) TextView tvInfo;
    private Unbinder mUnbinder;

    public static Intent getIntent(Context context){
        return new Intent(context, PartnerProgramActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        updateTitle(R.string.PartnerProgram, R.drawable.ic_share_variant);
        mUnbinder = ButterKnife.bind(this);

        showSupportInfo();
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

    @OnClick(R.id.btnSend)
    @SuppressWarnings("unused")
    public void onSendClick() {
        String phone = etPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            MessageBox.show(PartnerProgramActivity.this, String.format(
                    getString(R.string.msg_empty_field_format), getString(R.string.phone_number)));
            etPhone.requestFocus();
            return;
        }
        String name = etName.getText().toString();
        String userMessage = etMessage.getText().toString();

        new AsyncJob.Builder<String>()
                .withProgress(this, R.string.generate_invite)
                .doWork(() ->
                        ServerManager.GetInstance().getShortUrl(getPartnerUrl(phone, name, CUSTOMER)))
                .onSuccess(s -> {
                    String msg = generateMessage(s, userMessage);
                    Helpers.showShareChooserDialog(this, phone, msg);
                })
                .buildAndExecute();
    }

    private String generateMessage(String url, String userMessage) {
        return String.format("%s %s", userMessage, url).trim();
    }

    private String getPartnerUrl(String phone, String name, String type){
        int accountID = Collections.me().getUser().getAccountID();
        String partnerUrl = String.format(Locale.getDefault(), URL, getString(R.string.groupChanel),
                getString(R.string.sediApiKey), encode(type), encode(phone), encode(name), accountID);
        return partnerUrl;
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONTACT_PICK && resultCode == RESULT_OK) {
            updateContactData(data);
        }
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
        if (phone.contains("+")) return phone;

        if (phone.startsWith("0") || phone.startsWith("8")) {
            String newCountyCode = "+" + CountryCodes.getCode(this);
            phone = phone.replaceFirst(String.valueOf(phone.charAt(0)), newCountyCode);
        }
        return phone.replace("-", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    public String getSelectedAppType() {
        int id = rgAppType.getCheckedRadioButtonId();
        switch (id) {
            case R.id.rbCustomerApp:
                return CUSTOMER;
            case R.id.rbDriverApp:
                return EMPLOYEE;
            default:
                return CUSTOMER;
        }
    }
}
