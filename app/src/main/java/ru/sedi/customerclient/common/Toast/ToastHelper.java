package ru.sedi.customerclient.common.Toast;

import android.widget.Toast;
import ru.sedi.customerclient.base.BaseActivity;

public class ToastHelper {

    public static void ShowLongToast(final String toastMessage) {
        BaseActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.Instance, toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showShortToast(final String toastMessage) {
        BaseActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.Instance, toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void ShowCustomToast(final String toastMessage, final int vibDuration, final int toastShowDuration) {
        BaseActivity.Instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaseActivity.Instance.getVibrator().vibrate(vibDuration);
                Toast.makeText(BaseActivity.Instance, toastMessage, toastShowDuration).show();
            }
        });
    }
}
