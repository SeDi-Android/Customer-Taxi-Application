package ru.sedi.customerclient.classes;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import org.jsoup.Jsoup;
import ru.sedi.customer.R;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class UpdateChecker {

    private Context mContext;
    private PackageInfo mPackageInfo;
    private ApplicationInfo mApplicationInfo;
    private final String MARKET_URI = "market://details?id=";
    private final String MARKET_URL = "https://play.google.com/store/apps/details?id=";


    public UpdateChecker(Context context) {
        try {
            mContext = context;
            mApplicationInfo = mContext.getApplicationContext().getApplicationInfo();
            mPackageInfo = mContext.getPackageManager().getPackageInfo(mApplicationInfo.packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.log(e);
        }

    }

    public void start() {
        long time = Prefs.getLong(PrefsName.NEXT_UPDATE_TIME);
        if (time <= System.currentTimeMillis())
            new UpdateAppTask().execute();
    }

    private class UpdateAppTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return sendUpdateRequest();
        }

        @Override
        protected void onPostExecute(Boolean needUpdate) {
            super.onPostExecute(needUpdate);
            if (!needUpdate) {
                LogUtil.log(LogUtil.INFO, "Установлена последняя версия приложения");
                return;
            }

            String msgFormat = mContext.getString(R.string.msg_NewVersionNotification);
            String appName = (String) mContext.getPackageManager().getApplicationLabel(mApplicationInfo);
            MessageBox.show(mContext,
                    String.format(msgFormat, appName),
                    mContext.getString(R.string.HasUpdate),
                    new UserChoiseListener() {
                        @Override
                        public void OnOkClick() {
                            super.OnOkClick();
                            try {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI + mPackageInfo.packageName)));
                            } catch (Exception e) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + mPackageInfo.packageName)));
                            }
                        }

                        @Override
                        public void onCancelClick() {
                            super.onCancelClick();
                        }
                    }, true, new int[]{R.string.Update, R.string.no});
            Prefs.setValue(PrefsName.NEXT_UPDATE_TIME, System.currentTimeMillis() + DateTime.DAY);
        }

        private boolean sendUpdateRequest() {
            try {
                String curVersion = mPackageInfo.versionName;
                String newVersion = Jsoup.connect(MARKET_URL + mPackageInfo.packageName + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return value(curVersion) < value(newVersion);
            } catch (Exception e) {
                LogUtil.log(e);
                return false;
            }
        }

        private float value(String val) {
            try {
                return Float.valueOf(val);
            } catch (NumberFormatException e) {
                LogUtil.log(e);
                return 0.0f;
            }
        }
    }
}
