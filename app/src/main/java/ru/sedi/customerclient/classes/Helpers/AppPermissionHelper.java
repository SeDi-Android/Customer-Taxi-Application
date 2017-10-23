package ru.sedi.customerclient.classes.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AppPermissionHelper {

    public static boolean checkPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                || ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean requestAllDeniedPermission(Activity activity) {
        List<String> permissions = new ArrayList<>();

        if (!checkPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                || !checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!checkPermission(activity, Manifest.permission.GET_ACCOUNTS)) {
            permissions.add(Manifest.permission.GET_ACCOUNTS);
        }

        if (!checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!checkPermission(activity, Manifest.permission.READ_PHONE_STATE)) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!checkPermission(activity, Manifest.permission.READ_CONTACTS)) {
            permissions.add(Manifest.permission.READ_CONTACTS);
        }

        if (permissions.size() <= 0)
            return false;

        String[] permissions_array = permissions.toArray(new String[permissions.size()]);
        ActivityCompat.requestPermissions(activity, permissions_array, 0);
        return true;
    }

}
