package ru.sedi.customerclient.classes;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class App extends android.app.Application {
    public static int RADIUS_LIMIT = 200;
    private static App mInstance;
    public static boolean isAuth = false;
    public static boolean isTaxiLive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isTaxiLive = getApplicationInfo().packageName.contains("taxilive");
        updateRadiusLimit();
        new Prefs(this);
        LocationService.with(this);
        new LogUtil(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        isAuth = !Prefs.getString(PrefsName.USER_KEY).isEmpty();
    }

    /**
     * Настройка (в метрах) указывающая с какой погрешностью адрес будет считать геоточкой
     * для геокодеров.
     */
    private void updateRadiusLimit() {
        int resource = getResource(this, "integer", "radius_limit");
        if (resource != -1)
            RADIUS_LIMIT = getResources().getInteger(resource);
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
}
