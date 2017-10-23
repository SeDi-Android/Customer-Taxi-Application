package ru.sedi.customerclient.activitys.user_profile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.Validator;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.PaymentSystem;
import ru.sedi.customerclient.NewDataSharing._Bill;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.IFunc;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;

public class NewCardDialog extends AppCompatDialog {

    private final Context mContext;
    private QueryList<PaymentSystem> recurrent = new QueryList<>();

    @BindView(R.id.spnrPaymentSystem) Spinner spnrPaymentSystem;
    @BindView(R.id.etEmail) EditText etEmail;

    public NewCardDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);
        mContext = context;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_card);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateLayoutParams();
    }

    @Override
    public void show() {
        super.show();
        recurrent = Collections.me().getPaySystems().getRecurrent();
        if (recurrent == null || recurrent.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.empty_card_system)
                    .setPositiveButton(R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewCardDialog.this.dismiss();
                        }
                    }).create().show();
            Collections.me().updatePaymentSystems();
            return;
        } else {
            init();
        }
    }

    private void init() {
        ArrayAdapter<PaymentSystem> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, recurrent);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnrPaymentSystem.setAdapter(adapter);
    }

    private void updateLayoutParams() {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);
    }

    @OnClick(R.id.btnAddCard)
    public void onAddCardClick() {
        String mail = etEmail.getText().toString();
        if (TextUtils.isEmpty(mail) || !Validator.Valid(Validator.EMAIL_PATTERN, mail)) {
            MessageBox.show(mContext, R.string.msg_IncorrectEmail, -1);
            return;
        }

        PaymentSystem system = (PaymentSystem) spnrPaymentSystem.getSelectedItem();
        if (system == null) {
            MessageBox.show(mContext, "Выбранная система NULL");
            return;
        }

        addNewCard(mail, system.getPaymentSystem().getID());
    }

    private void addNewCard(final String mail, final String id) {
        final SweetAlertDialog show = ProgressDialogHelper.show(mContext);
        AsyncAction.run(new IFunc<_Bill>() {
            @Override
            public _Bill Func() throws Exception {
                return ServerManager.GetInstance().addRecurrentCard(mail, id);
            }
        }, new IActionFeedback<_Bill>() {
            @Override
            public void onResponse(_Bill result) {
                if (show != null)
                    show.dismiss();

                if (result.getStatus().getID().equalsIgnoreCase("invoiced")) {
                    MessageBox.show(mContext, mContext.getString(R.string.success_pay_message), null, new UserChoiseListener() {
                        @Override
                        public void OnOkClick() {
                            super.OnOkClick();
                            String url = result.getPayUrl();
                            if (TextUtils.isEmpty(url))
                                url = "http://sedi.ru/";
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            mContext.startActivity(i);
                        }
                    }, true, new int[]{R.string.Continue, R.string.close});
                } else {
                    MessageBox.show(mContext, String.format(mContext.getString(R.string.error_pay_message), result.getStatus().getName()));
                }
                dismiss();
            }

            @Override
            public void onFailure(Exception e) {
                if (show != null)
                    show.dismiss();

                MessageBox.show(mContext, e.getMessage());
            }
        });
    }
}
