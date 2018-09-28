package ru.sedi.customerclient.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.Helpers.SaveDataHelper;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.activitys.user_registration.UserPhoneRegisterActivity;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class BaseActivity extends AppCompatActivity {


    public static BaseActivity Instance;
    private Vibrator mVibrator;
    boolean m_showPush = true;

    public void onCreate(Bundle savedInstanceState, boolean showPush) {
        super.onCreate(savedInstanceState);
        Instance = this;
        m_showPush = showPush;
        initBaseComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        m_showPush = true;
        initBaseComponent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        removePush();
        initBaseComponent();
    }

    public void updateTitle(int title, int icon) {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null)
            return;

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.whiteColor), PorterDuff.Mode.SRC_ATOP);
        supportActionBar.setElevation(0);
        supportActionBar.setHomeAsUpIndicator(upArrow);

        supportActionBar.setTitle(title);
        if (icon > 0) {
            supportActionBar.setIcon(icon);
            supportActionBar.setLogo(icon);
        }
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
    }

    public void trySetElevation(int elevation) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(elevation);
    }

    public void hideActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar == null)
            return;
        supportActionBar.hide();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Инициализация базовых компонентов и раширений
     */
    private void initBaseComponent() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        updateLocale();
    }

    /**
     * Инициализация доступа к вибрации
     */
    public Vibrator getVibrator() {
        try {
            if (mVibrator == null)
                mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            return mVibrator;
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * Сообщение об ошибке, используется в try/catch
     *
     * @param e Ошибка (тип Exception)
     */
    public void showDebugMessage(int exId, Exception e) {
        final String exceptionMessage = String.format(
                "ExID:%d.\nMSG:%s.",
                exId,
                e.getMessage());

        LogUtil.log(e);

        if (Prefs.getBool(PrefsName.IS_DEVELOPER_MODE)
                && Prefs.getBool(PrefsName.SHOW_DEBUG_MESSAGE))
            Toast.makeText(getBaseContext(), exceptionMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Update current app locale;
     */
    public void updateLocale() {
        try {
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            android.content.res.Configuration conf = res.getConfiguration();
            String localeCode = Prefs.getString(PrefsName.LOCALE_CODE);
            conf.locale = new Locale(localeCode);
            res.updateConfiguration(conf, dm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Закрытие активити
     */
    public void closeActivity(Context context) {
        try {
            ((Activity) context).finish();
        } catch (Exception e) {
            showDebugMessage(2, e);
        }
    }

    /**
     * Сворачивание приложения и открытие рабочего стола
     */
    public void minimaizeApp() {
        SaveDataHelper.save();
        _OrderRegistrator.me().setOrderCreateListener(null);
        removePush();
        LocationService.me().stopListener();
        finish();
    }

    /**
     * Удалить значек из трея
     */
    public void removePush() {
    }

    public void applicationTerminate() {
        removePush();
        System.exit(0);
    }

    public void startNewActivity(Class aClass) {
        startNewActivity(aClass, null);
    }

    public void startNewActivity(Class aClass, Map<String, String> extra) {
        Intent intent = new Intent(this, aClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isNaN(extra))
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
                LogUtil.log(
                        LogUtil.INFO,
                        String.format("Передан ключ: %s и значение: %s;", entry.getKey(), entry.getValue())
                );
            }
        startActivity(intent);
    }

    public void showRegistrationDialog(final Context context, boolean needClose) {
        MessageBox.show(context, R.string.RegistrationMessage, -1, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                startNewActivity(UserPhoneRegisterActivity.class, null);
            }

            @Override
            public void onCancelClick() {
                /*if (context instanceof ChooseTariffActivity)
                    return;*/
                if (needClose)
                    closeActivity(context);
            }
        }, true, new int[]{R.string.yes, R.string.no});
    }

    public void showRegistrationDialog(final Context context) {
        showRegistrationDialog(context, true);
    }

    public boolean isNaN(Object o) {
        try {
            return o == null ? false : true;
        } catch (Exception e) {
            showDebugMessage(3, e);
            return false;
        }
    }

    public String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            showDebugMessage(4, e);
            return "X.X";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initBaseComponent();
    }

    public static void showSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
    }

}
