package ru.sedi.customerclient.common.AsyncAction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import ru.sedi.customer.R;
import ru.sedi.customerclient.common.LogUtil;

public class ProgressDialogHelper {

    public static ProgressDialog show(Context context) {
        return show(context, context.getString(R.string.msg_WaitingServerResponce));
    }

    public static ProgressDialog show(Context context, int msgId) {
        return show(context, context.getString(msgId));
    }

    public static ProgressDialog show(Context context, final String msg) {
        return show(context, msg, true, null);
    }

    public static ProgressDialog show(Context context, final String msg, boolean cancelable, DialogInterface.OnKeyListener listener) {
        if (context == null)
            return null;
        try {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(msg);
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
