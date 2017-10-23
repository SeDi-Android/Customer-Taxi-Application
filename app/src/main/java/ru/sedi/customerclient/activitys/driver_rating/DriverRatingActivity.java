package ru.sedi.customerclient.activitys.driver_rating;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;

public class DriverRatingActivity extends AppCompatActivity {

    public static final int SUCCESS_RESPONSE = 321;
    public static final String COMMENT = "comment";
    public static final String RATING = "rating";
    public static final String ID = "id";

    //<editor-fold desc="Vars">
    private String date = "";
    private String orderId = "";
    private RatingBar rbRating;
    private EditText etMessage;
    private Button btnSave;
    private ImageButton ibtnVoiceInput;
    //</editor-fold>

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_driver_rating);
        InitOrderIdAndDate();
        SetWindowParameters();
        InitUiElements();
    }

    private void InitOrderIdAndDate() {
        if (getIntent().getExtras() != null) {
            date = getIntent().getExtras().getString("date");
            orderId = getIntent().getExtras().getString("orderId");
            LogUtil.log(LogUtil.INFO, "ID заказа: %s", orderId);
        }
        if (date.length() > 0 && orderId.length() > 0)
            setTitle(String.format(getString(R.string.msg_OrderNWith_), orderId, date));
        else {
            LogUtil.log(LogUtil.ERROR, "Нет id или даты");
            finish();
        }
    }

    private void InitUiElements() {
        etMessage = (EditText) this.findViewById(R.id.ddr_etMessage);
        ibtnVoiceInput = (ImageButton) this.findViewById(R.id.ddr_ibtnVoice);
        ibtnVoiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpeechRecognitionHelper.run(DriverRatingActivity.this);
            }
        });
        btnSave = (Button) this.findViewById(R.id.ddr_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageBox.show(DriverRatingActivity.this, getString(R.string.msg_SetRatingQuestion), null, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        SetRating();
                    }

                    @Override
                    public void onCancelClick() {
                        super.onCancelClick();
                    }
                }, true, new int[]{R.string.yes, R.string.no});
            }
        });
        rbRating = (RatingBar) this.findViewById(R.id.ddr_rbRating);
    }

    private void SetRating() {
        final String comment = etMessage.getText().toString();
        int i = (int) rbRating.getRating();
        final int rating = (i <= 0) ? 0 : i;

        final SweetAlertDialog pd = ProgressDialogHelper.show(this);
        AsyncAction.run(() -> ServerManager.GetInstance().setRating(orderId, rating, comment),
                new IActionFeedback<Server>() {
                    @Override
                    public void onResponse(Server server) {
                        if (pd != null)
                            pd.dismiss();

                        if (!server.isSuccess()) {
                            MessageBox.show(DriverRatingActivity.this, String.format(getString(R.string.service_unavailable_message), server.getResponceMessage()), null);
                            return;
                        }
                        MessageBox.show(DriverRatingActivity.this, getString(R.string.msg_RatingSetSuccess), null, new UserChoiseListener() {
                            @Override
                            public void OnOkClick() {
                                Intent resultIntent = new Intent();
                                resultIntent
                                        .putExtra(ID, Integer.parseInt(orderId))
                                        .putExtra(COMMENT, comment)
                                        .putExtra(RATING, rating);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        }, false);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (pd != null)
                            pd.dismiss();
                        MessageBox.show(DriverRatingActivity.this, String.format(getString(R.string.service_unavailable_message), e.getMessage()), null);
                    }
                });
    }

    private void SetWindowParameters() {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(attributes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {
                    try {
                        final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        etMessage.setText((text.get(0)));
                    } catch (Exception e) {
                        BaseActivity.Instance.showDebugMessage(66, e);
                    }
                }
                break;
            }
        }
    }
}