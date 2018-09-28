package ru.sedi.customerclient.common.SystemManagers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import ru.sedi.customerclient.common.LogUtil;

public class Device {
    public static boolean hasNet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static boolean hasSim(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telephonyManager.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
            else {
                return false;
            }
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public static String getInfo(Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("Android:" + Build.VERSION.RELEASE);
            builder.append("|Device: " + Build.BRAND + " " + Build.MODEL);
            builder.append("|AppVersion: " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
        return builder.toString();
    }
}
