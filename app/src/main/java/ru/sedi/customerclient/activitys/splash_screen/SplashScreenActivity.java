package ru.sedi.customerclient.activitys.splash_screen;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.widget.TextView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.activitys.new_main.NewMainActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Device;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;


public class SplashScreenActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionBar();
        setContentView(R.layout.actvt_splash_screen);

        findViewById(R.id.rvBackground).setBackgroundColor(ContextCompat.getColor(
                this, R.color.splashBackgroundColor));
        ((TextView) this.findViewById(R.id.ss_tvVersionApp))
                .setText(getString(R.string.version_caption_symbol) + getAppVersion());

        updateLocale();
        Prefs.contains(PrefsName.ENABLE_PROMO, false);

        boolean deniedPermission = AppPermissionHelper.requestAllDeniedPermission(this);
        if (deniedPermission)
            return;

        loadApplicationData();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadApplicationData();
    }

    /**
     * Загружаем.
     */
    private void loadApplicationData() {
        if (!Device.hasNet(SplashScreenActivity.this)) {
            showNetworkErrorMessage();
            return;
        }

        Collections.me().updateAllInfo();
        startNecessaryActivity();
    }

    /**
     * Если нет интернета просим включить или закрыть приложение.
     */
    private void showNetworkErrorMessage() {
        MessageBox.show(SplashScreenActivity.this, R.string.noInetOrSimCard, -1, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
                loadApplicationData();
            }

            @Override
            public void onCancelClick() {
                super.onCancelClick();
                applicationTerminate();
            }
        }, true, new int[]{R.string.repeat, R.string.exit});
    }


    /**
     * Запуск основного окна.
     */
    private void startNecessaryActivity() {
        try {
            _OrderRegistrator.me().setOrderCreateListener(null);
            _OrderRegistrator.me().resetLastOrder();
            //Collections.me().updateTariffsServices();

           /* Class c = MainActivity.class;
            if (Prefs.getBool(PrefsName.ENABLED_SET_DEFAULT_ADDRESS)
                    && AppPermissionHelper.isLocationPermissionGranted(SplashScreenActivity.this))
                c = ExemplaryLocationActivity.class;*/

            startActivity(NewMainActivity.getIntent(this));

            /*Intent intent = new Intent(SplashScreenActivity.this, c);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            return false;
        return super.onKeyDown(keyCode, event);
    }
}