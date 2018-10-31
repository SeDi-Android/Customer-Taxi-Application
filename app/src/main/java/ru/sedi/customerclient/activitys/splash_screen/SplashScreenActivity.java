package ru.sedi.customerclient.activitys.splash_screen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.TextView;

import com.yandex.metrica.YandexMetrica;

import java.util.Set;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.activitys.main_2.MainActivity2;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
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

    private boolean handleExternalAddresses(Uri data) {
        if (data == null || data.getQueryParameterNames().isEmpty()) return false;

        _Order order = _OrderRegistrator.me().getOrder();
        _Route route = order.getRoute();

        Set<String> parameterNames = data.getQueryParameterNames();
        if (parameterNames.contains("from") && parameterNames.contains("from_str")) {
            _Point p = new _Point(data.getQueryParameter("from_str"),
                    getLocationFromData(data.getQueryParameter("from")));
            route.addPoint(p);
        }

        if (parameterNames.contains("to") && parameterNames.contains("to_str")) {
            _Point p = new _Point(data.getQueryParameter("to_str"),
                    getLocationFromData(data.getQueryParameter("to")));
            route.addPoint(p);
        }

        if (parameterNames.contains("id_calculation")) {
            String id_calculation = data.getQueryParameter("id_calculation");
            try {
                Integer id = Integer.valueOf(id_calculation);
                order.setCostCalculationId(id);
            } catch (Exception ignored) {
            }
        }

        if (parameterNames.contains("oid")) {
            String order_id = data.getQueryParameter("oid");
            order.setExternalOrderId(order_id);
        }

        if (App.isMetricaInitialized) {
            YandexMetrica.reportAppOpen(this);
        }
        return true;
    }

    private LatLong getLocationFromData(String parameter) {
        if (TextUtils.isEmpty(parameter)) return new LatLong(0, 0);
        String[] split = parameter.split(",");
        try {
            return new LatLong(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        } catch (NumberFormatException e) {
            LogUtil.log(e);
            return new LatLong(0, 0);
        }

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
        new Handler().postDelayed(this::startNecessaryActivity, 2000);
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

            boolean withAddresses = handleExternalAddresses(getIntent().getData());
            startActivity(MainActivity2.getIntent(this, withAddresses).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

            //MoveVersionActivity заглушка когда приложения нужно объеденить
            //startActivity(MoveVersionActivity.getIntent(this));

            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode != KeyEvent.KEYCODE_BACK && super.onKeyDown(keyCode, event);
    }
}