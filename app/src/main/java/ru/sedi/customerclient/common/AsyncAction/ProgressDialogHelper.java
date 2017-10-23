package ru.sedi.customerclient.common.AsyncAction;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.common.LogUtil;

public class ProgressDialogHelper {

    public static SweetAlertDialog show(Context context) {
        return show(context, context.getString(R.string.msg_WaitingServerResponce));
    }

    public static SweetAlertDialog show(Context context, int msgId) {
        return show(context, context.getString(msgId));
    }

    public static SweetAlertDialog show(Context context, final String msg) {
        return show(context, msg, true, null);
    }

    public static SweetAlertDialog show(Context context, final String msg, boolean cancelable, DialogInterface.OnKeyListener listener) {
        if (context == null)
            return null;
        try {
            SweetAlertDialog progressDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(context, R.color.primaryColor));
            progressDialog.setTitleText(msg);
            if (listener != null)
                progressDialog.setOnKeyListener(listener);
            progressDialog.setCancelable(cancelable);
            progressDialog.show();
            return progressDialog;
        } catch (Exception ex) {
            LogUtil.log(ex);
            return null;
        }
    }
}
