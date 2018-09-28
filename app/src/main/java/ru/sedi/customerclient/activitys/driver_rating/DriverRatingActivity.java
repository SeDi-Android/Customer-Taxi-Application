package ru.sedi.customerclient.activitys.driver_rating;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RatingBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class DriverRatingActivity extends AppCompatActivity {

    public static final int SUCCESS_RESPONSE = 321;
    private static final String ORDER_ID = "orderId";

    private OkHttpClient mOkHttpClient = new OkHttpClient.Builder().build();
    private String mOrderId = "-1";

    @BindView(R.id.ddr_rbRating)
    RatingBar rbRating;
    @BindView(R.id.ddr_etMessage)
    EditText etMessage;

    public static Intent getIntent(Context context, String orderId) {
        Intent intent = new Intent(context, DriverRatingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(ORDER_ID, orderId);
        return intent;
    }

    public static Intent getIntentForResult(Context context, String orderId) {
        Intent intent = new Intent(context, DriverRatingActivity.class);
        intent.putExtra(ORDER_ID, orderId);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_driver_rating);
        ButterKnife.bind(this);
        readIntentExtras();
        updateWindowParameters();

        rbRating.setRating(0f);
    }

    private void readIntentExtras() {
        if (getIntent().getExtras() != null) {
            mOrderId = getIntent().getExtras().getString(ORDER_ID);
            LogUtil.log(LogUtil.INFO, "ID заказа: %s", mOrderId);
        }
        if (mOrderId.length() > 0)
            setTitle(String.format(getString(R.string.msg_OrderNWith_), mOrderId));
        else {
            finish();
        }
    }

    @OnClick(R.id.ddr_btnSave)
    @SuppressWarnings(Const.UNUSED)
    public void onSaveBtnClick() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.msg_SetRatingQuestion)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> setRating())
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    @OnClick(R.id.ddr_ibtnVoice)
    @SuppressWarnings(Const.UNUSED)
    public void onVoiceInputBtnClick() {
        SpeechRecognitionHelper.run(DriverRatingActivity.this);
    }

    private void setRating() {
        final String comment = etMessage.getText().toString();
        int rating = (int) rbRating.getRating();

        Call call = mOkHttpClient.newCall(getRequest(rating, comment));

        final ProgressDialog pd = ProgressDialogHelper.show(this);
        pd.setOnCancelListener(dialogInterface -> call.cancel());

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                pd.dismiss();
                if (!call.isCanceled()) showAlertMessage(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                pd.dismiss();
                handleResponse(response);
            }
        });
    }

    private Request getRequest(int rating, String comment) {
        String urlChanel = getString(R.string.groupChanel);
        String httpProtocol = Server.isHttp(urlChanel) ? "http" : "https";
        String url = String.format("%s://%s/webapi?q=set_rating", httpProtocol, urlChanel);

        new Prefs(this);

        RequestBody requestBody = new FormBody.Builder()
                .add("orderid", mOrderId)
                .add("rating", String.valueOf(rating))
                .add("apikey", getString(R.string.sediApiKey))
                .add("userkey", Prefs.getString(PrefsName.USER_KEY))
                .addEncoded("comment", comment)
                .build();

        return new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
    }

    private void handleResponse(Response response) {
        if (!response.isSuccessful()) {
            MessageBox.show(DriverRatingActivity.this, R.string.Error);
            return;
        }

        try {
            String string = response.body().string();
            JSONObject o = new JSONObject(string);
            if (o.has("Success") && o.getBoolean("Success")) {
                runOnUiThread(() -> new AlertDialog.Builder(this)
                        .setMessage(R.string.msg_RatingSetSuccess)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            setResult(RESULT_OK);
                            finish();
                        })
                        .create().show());
            } else {
                String msg = getString(R.string.Error);
                if (o.has("Message")) {
                    msg = o.getString("Message");
                }
                showAlertMessage(msg);
            }
        } catch (IOException e) {
            showAlertMessage(e.getMessage());
        } catch (JSONException e) {
            showAlertMessage(e.getMessage());
        }
    }

    private void showAlertMessage(String msg) {
        MessageBox.show(DriverRatingActivity.this, msg);
    }

    private void updateWindowParameters() {
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