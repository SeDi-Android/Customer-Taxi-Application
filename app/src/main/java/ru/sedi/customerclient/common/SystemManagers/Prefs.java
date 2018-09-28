package ru.sedi.customerclient.common.SystemManagers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.enums.PrefsName;

public class Prefs {

    private static SharedPreferences mSharedPreferences;
    private String DEFAULT_LANG;

    public Prefs(Context context) {
        if(mSharedPreferences == null) {
            DEFAULT_LANG = App.isExcludedApp ? "de" : "ru";
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            initDefaultPreference();
        }
    }

    private void initDefaultPreference() {
        if (!mSharedPreferences.contains(PrefsName.ENABLED_SET_DEFAULT_ADDRESS))
            setValue(PrefsName.ENABLED_SET_DEFAULT_ADDRESS, true);

        if (!mSharedPreferences.contains(PrefsName.LOCALE_CODE))
            setValue(PrefsName.LOCALE_CODE, DEFAULT_LANG);

        if (!mSharedPreferences.contains(PrefsName.MAP_ZOOM_LEVEL))
            setValue(PrefsName.MAP_ZOOM_LEVEL, 22);
    }

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public static int getInt(String key) {
        try {
            return mSharedPreferences.getInt(key, 0);
        } catch (Exception e) {
            LogUtil.log(e);
            return 0;
        }
    }

    public static long getLong(String key) {
        try {
            return mSharedPreferences.getLong(key, 0);
        } catch (Exception e) {
            LogUtil.log(e);
            return 0;
        }
    }

    public static boolean getBool(String key) {
        try {
            return mSharedPreferences.getBoolean(key, false);
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public static String getString(String key) {
        try {
            return mSharedPreferences.getString(key, "");
        } catch (Exception e) {
            LogUtil.log(e);
            return "";
        }
    }

    public static float getDouble(String key) {
        try {
            return mSharedPreferences.getFloat(key, 0);
        } catch (Exception e) {
            LogUtil.log(e);
            return 0;
        }
    }

    public static void setValue(String key, Object value) {
        try {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            if (value instanceof Integer)
                editor.putInt(key, Integer.parseInt(value.toString()));
            if (value instanceof Long)
                editor.putLong(key, Long.parseLong(value.toString()));
            if (value instanceof String)
                editor.putString(key, value.toString());
            if (value instanceof Boolean)
                editor.putBoolean(key, Boolean.parseBoolean(value.toString()));
            editor.apply();
        } catch (NumberFormatException e) {
            LogUtil.log(e);
        }
    }

    public static boolean contains(String key, @Nullable Object initValue) {
        boolean contain = mSharedPreferences.contains(key);
        if (initValue != null)
            setValue(key, initValue);
        return contain;
    }
}
