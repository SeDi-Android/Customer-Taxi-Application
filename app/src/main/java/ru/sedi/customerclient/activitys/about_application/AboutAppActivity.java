package ru.sedi.customerclient.activitys.about_application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Device;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;


public class AboutAppActivity extends BaseActivity implements View.OnClickListener {

    private long mLastClickTime;
    private final int CLICK_TIMEOUT = 500;
    private final int CLICK_LIMIT = 9;
    private int mClickCount;
    View mView;

    public static Intent getIntent(Context context) {
        return new Intent(context, AboutAppActivity.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_about_application);
        updateTitle(R.string.about_application, R.drawable.ic_information_outline);
        init();
    }

    private void init() {
        try {
            ImageView ivLogotype = (ImageView) this.findViewById(R.id.ivLogo);
            ivLogotype.setOnClickListener(this);

            TextView tvShortText = (TextView) this.findViewById(R.id.tvShortText);
            String appDesc = String.format(getString(R.string.app_description_format), getString(R.string.appName));

            int resource = App.getResource(getBaseContext(), "string", "personal_app_desc");
            if (resource > 0)
                appDesc = getString(resource);

            tvShortText.setText(appDesc);

            TextView tvBuildInfo = (TextView) this.findViewById(R.id.tvBuildInfo);
            tvBuildInfo.setText(getBuildText());

            Button btnWhatNew = (Button) this.findViewById(R.id.btnWhatNew);
            btnWhatNew.setText(String.format(getString(R.string.what_new_format), getAppVersion()));
            btnWhatNew.setOnClickListener(this);
            btnWhatNew.setVisibility(App.isTaxiLive ? View.GONE : View.VISIBLE);


        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * Возвращает инфу о приложении для текущего пользователя
     */
    private String getBuildText() {
        String userKey = Prefs.getString(PrefsName.USER_KEY);
        int id = Collections.me().getUser().getID();

        StringBuilder msg = new StringBuilder();
        msg.append("V:").append(getAppVersion()).append(" | "); //Версия приложения
        if (!TextUtils.isEmpty(userKey))
            msg.append("UK:").append(userKey.substring(0, 7)).append(" | ");//Ключ пользователя
        msg.append("GK:").append(getString(R.string.sediApiKey).substring(0, 7)).append(" | "); //Ключ группы
        msg.append("UID:").append(id); //ID пользователя

        return msg.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivLogo: {
                break;
            }
            case R.id.btnWhatNew: {
                showNewFuncDialog();
                break;
            }
        }
    }

    /**
     * Отображает диалог с данными о новых фишках.
     */
    private void showNewFuncDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.element_web_activity, null);

        WebView webView = (WebView) mView.findViewById(R.id.ewa_webView);
        if (webView != null) {
            webView.clearCache(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new UserWebViewClient());
            webView.loadUrl("http://sedi.ru:8087/mobile/sedi_customer_version.htm");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(mView);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    public class UserWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!Device.hasNet(AboutAppActivity.this) && AboutAppActivity.this.mView != null) {
                mView.findViewById(R.id.ewa_pbPageLoadProgress).setVisibility(View.VISIBLE);
                mView.findViewById(R.id.ewa_webView).setVisibility(View.GONE);
                return;
            }

            if (mView != null)
                mView.findViewById(R.id.ewa_pbPageLoadProgress).setVisibility(View.GONE);

        }
    }
}