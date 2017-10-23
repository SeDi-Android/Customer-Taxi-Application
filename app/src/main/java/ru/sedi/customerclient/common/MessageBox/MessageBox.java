package ru.sedi.customerclient.common.MessageBox;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;

public class MessageBox {
    public static void show(Context context, String message) {
        show(context, message, null, null, false);
    }

    public static void show(Context context, String message, String caption) {
        show(context, message, caption, null, false);
    }

    public static void show(Context context, int messageId) {
        show(context, messageId, -1);
    }
    public static void show(Context context, int messageId, int captionId) {
            show(context, context.getString(messageId), captionId > 0 ? context.getString(captionId) : null, null, false);
    }

    public static void show(Context context, CharSequence message, String caption, final UserChoiseListener listener, boolean showCancel) {
        show(context, message, caption, listener, showCancel, new int[0]);
    }

    public static void show(Context context, int messageId, int captionId, final UserChoiseListener listener, boolean showCancel, int[] buttonsCaptions) {
        show(context, context.getString(messageId), captionId > 0 ? context.getString(captionId) : null, listener, showCancel, buttonsCaptions);
    }

    public static void show(Context context, CharSequence message, String caption, final UserChoiseListener listener, boolean showCancel, int[] buttonsCaptions) {
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(caption);
        builder.setMessage(message);

        if (showCancel) {
            int cancelCaption = buttonsCaptions != null && buttonsCaptions.length > 1 ? buttonsCaptions[1] : R.string.cancel;
            builder.setNegativeButton(cancelCaption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onCancelClick();
                    }
                }
            });
        }

        int okCaption = buttonsCaptions != null && buttonsCaptions.length > 0 ? buttonsCaptions[0] : R.string.ok;
        builder.setPositiveButton(okCaption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.OnOkClick();
                }
            }
        });

        AsyncAction.runInMainThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });

    }

}
