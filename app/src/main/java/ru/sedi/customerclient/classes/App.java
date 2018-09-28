package ru.sedi.customerclient.classes;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class App extends android.app.Application {
    public static int RADIUS_LIMIT = 200;
    private static App mInstance;
    public static boolean isAuth = false;
    public static boolean isExcludedApp = false;
    public static boolean isMetricaInitialized;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initializeAppMetrica();
        initializeRealmDb();

        new Prefs(this);
        LocationService.with(this);
        new LogUtil(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        isAuth = !Prefs.getString(PrefsName.USER_KEY).isEmpty();
    }

    /**
     * Инициализации Appmetrica от Yandex.
     * Если в параметрах сборки приложения имеется строковый ресурс с id {@code appmetrica_key}
     * и в этом ресурсе имеется api-ключ Appmetrica, то активируем Appmetrica.
     */
    private void initializeAppMetrica() {
        int appmetrica_res = getResource(this, "string", "appmetrica_key");
        if (appmetrica_res <= 0) return;

        String appmetrica_key = getString(appmetrica_res);
        if (appmetrica_key.isEmpty()) return;

        YandexMetricaConfig.Builder configBuilder = YandexMetricaConfig.newConfigBuilder(appmetrica_key);
        YandexMetrica.activate(getApplicationContext(), configBuilder.build());
        YandexMetrica.enableActivityAutoTracking(this);
        isMetricaInitialized = true;
    }

    private void initializeRealmDb() {
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public App() {
        mInstance = this;
    }

    public static App getInstance() {
        return mInstance;
    }

    public static int getResource(Context context, String defType, String name) {
        int identifier = context.getResources().getIdentifier(name, defType, context.getPackageName());
        return identifier != 0 ? identifier : -1;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
