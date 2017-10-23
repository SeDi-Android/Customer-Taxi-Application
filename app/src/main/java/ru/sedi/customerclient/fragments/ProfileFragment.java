package ru.sedi.customerclient.fragments;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.loopj.android.http.RequestParams;

import org.apache.http.util.TextUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProfileFragment extends Fragment {

    public static final int LAYOUT = R.layout.fragment_profile;

    @BindView(R.id.etSecondname) EditText etSecondname;
    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.etBirthday) EditText etBirthday;
    @BindView(R.id.etPhone) EditText etPhone;
    @BindView(R.id.etGender) EditText etGender;

    @BindView(R.id.llBirthday) LinearLayout llBirthday;
    private Context mContext;
    private _LoginInfo mInfo;

    public ProfileFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, view);
        llBirthday.setVisibility(App.isTaxiLive ? GONE : VISIBLE);
        mInfo = Collections.me().getUser();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(false);
        if (!App.isAuth)
            return;
        getProfile();
    }

    private void init(_LoginInfo profile) {
        etSecondname.setText(profile.getSecondName());
        etName.setText(profile.getName());
        if (!profile.getPhones().isEmpty())
            etPhone.setText(profile.getPhones().get(0).getNumber());
        etPhone.setEnabled(!App.isTaxiLive);

        etGender.setText(getString(profile.getGender() ? R.string.male : R.string.female));
        etGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGenderPicker();
            }
        });
        etBirthday.setText(profile.getBirthday().toString(DateTime.DATE));
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(etBirthday.getText().toString());
            }
        });
    }

    private void showGenderPicker() {
        final String s[] = {getString(R.string.male), getString(R.string.female)};
        boolean isMale = etGender.getText().toString().equalsIgnoreCase(getString(R.string.male));
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setSingleChoiceItems(s, isMale ? 0 : 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etGender.setText(s[which]);
                    }
                })
                .create().show();
    }

    @OnClick(R.id.btnSave)
    public void save() {
        final RequestParams params = new RequestParams();
        if (!validFieldValue(etName))
            return;
        params.put("firstname", etName.getText().toString());

        if (!validFieldValue(etSecondname))
            return;
        params.put("secondname", etSecondname.getText().toString());

        if (!validFieldValue(etGender))
            return;
        params.put("gender", etGender.getText().toString().equalsIgnoreCase(getString(R.string.male)));

        if (!validFieldValue(etPhone))
            return;
        params.put("phone0", etPhone.getText().toString());
        params.put("phonetype0", "MobilePersonal");

        if (!validFieldValue(etBirthday))
            return;
        DateTime birthday = DateTime.fromString(etBirthday.getText().toString(), DateTime.DATE);
        params.put("birthday", birthday.toString(DateTime.WEB_DATE));

        setProfile(params);
    }


    private boolean validFieldValue(EditText editText) {
        if (TextUtils.isEmpty(editText.getText().toString())) {
            MessageBox.show(mContext, String.format(getString(R.string.msg_empty_field_format), editText.getHint()));
            editText.requestFocus();
            return false;
        }
        return true;
    }

    private void showDatePicker(String date) {
        DateTime dateTime = DateTime.Now();
        if (!TextUtils.isEmpty(date))
            dateTime = DateTime.fromString(date, DateTime.DATE);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (view, year, monthOfYear, dayOfMonth) -> {
            DateTime dateTime1 = new DateTime(year, monthOfYear, dayOfMonth);
            etBirthday.setText(dateTime1.toString(DateTime.DATE));
        }, dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
        dialog.getDatePicker().setMinDate(new DateTime(1920, 1, 1).getTime());
        dialog.getDatePicker().setMaxDate(DateTime.Now().getTime());
        dialog.show();
    }

    public void getProfile() {
        if (mInfo != null) {
            init(mInfo);
        }

        AsyncJob.Builder<_LoginInfo> builder = new AsyncJob.Builder<_LoginInfo>()
                .doWork(() -> ServerManager.GetInstance().getProfile())
                .onSuccess(result -> {
                    Collections.me().setUser(result);
                    mInfo = result;
                    Collections.me().setUser(mInfo);
                    Collections.me().save();
                    init(mInfo);
                });

        if (mInfo == null) {
            builder.withProgress(getContext(), R.string.get_profile_process)
                    .onFailure(throwable -> showErrorDialog(throwable.getMessage()));
        }

        builder.buildAndExecute();
    }

    private void showErrorDialog(String message) {
        MessageBox.show(mContext, message, null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
                getActivity().finish();
            }
        }, false, new int[]{R.string.ok});
    }

    public void setProfile(final RequestParams params) {
        new AsyncJob.Builder<Boolean>()
                .withProgress(mContext, R.string.msg_update_profife_progress)
                .doWork(() -> ServerManager.GetInstance().updateProfile(params))
                .onSuccess(result -> {
                    String msg = getString(R.string.msg_update_profile_success);
                    if (!result)
                        msg = getString(R.string.msg_update_profile_error);
                    MessageBox.show(mContext, msg);
                    getProfile();
                })
                .onFailure(t -> MessageBox.show(mContext, t.getMessage()))
                .buildAndExecute();
    }
}
