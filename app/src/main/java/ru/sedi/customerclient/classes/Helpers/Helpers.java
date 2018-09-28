package ru.sedi.customerclient.classes.Helpers;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.text.DecimalFormat;

import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.partner_program.PartnerProgramActivity;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class Helpers {

    private static DecimalFormat mDecimalFormat;

    public static void showShareChooserDialog(Context context, String msg) {
        showShareChooserDialog(context, null, msg);
    }

    public static void showShareChooserDialog(Context context, String phone, String msg) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.setType("text/plain");
        if (!TextUtils.isEmpty(phone))
            intent.putExtra("address", phone);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.select)));
    }

    public static void hideKeyboard(View view) {
        if (view == null) return;
        InputMethodManager systemService = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        systemService.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view) {
        if (view == null) return;
        InputMethodManager systemService = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        systemService.showSoftInput(view, 0);
    }

    public static String decimalFormat(double v) {
        if (mDecimalFormat == null) {
            mDecimalFormat = new DecimalFormat("0.##");
        }
        return mDecimalFormat.format(v);
    }

    public static void copyToClipboard(Context context, String payeePurse) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(payeePurse, payeePurse);
        manager.setPrimaryClip(data);
    }

    public static String maskCardNumber(String cardNumber, String mask) {
        if (TextUtils.isEmpty(cardNumber))
            return "XXXX XXXX XXXX XXXX";
        int index = 0;
        StringBuilder maskedNumber = new StringBuilder();
        for (int i = 0; i < mask.length(); i++) {
            char c = mask.charAt(i);
            if (c == '#') {
                char cardSymbol = 'X';
                try {
                    cardSymbol = cardNumber.charAt(index);
                } catch (IndexOutOfBoundsException e) {
                    maskedNumber.append(cardSymbol);
                    index++;
                    continue;
                }
                maskedNumber.append(cardSymbol);
                index++;
            } else if (c == 'x' || c == 'X') {
                maskedNumber.append(c);
                index++;
            } else {
                maskedNumber.append(c);
            }
        }
        return maskedNumber.toString();
    }

    public static boolean isVisibilityState(int state) {
        return state == View.VISIBLE
                || state == View.INVISIBLE
                || state == View.GONE;
    }

    /**
     * Показывает сообщение о приемуществах партнерской программы, для групп
     * {@see Const.MASTER_PACKAGE_NAME}, {@see Const.T24_PACKAGE_NAME}
     * @param context контекст.
     * @param packName имя пакета для уведомления.
     * @param args 2 процента (стартовый и от заказа).
     */
    @SuppressLint("StringFormatMatches")
    public static void showPartnerInviteMessage(Context context, String packName, Object... args) {
        if (context.getPackageName().equalsIgnoreCase(packName)
                && !Prefs.getBool(packName)) {
            String msg = "";
            try {
                msg = context.getString(R.string.promo_text_format, args);
            } catch (Exception e) {
                LogUtil.log(e);
            }
            if (msg.isEmpty()) return;

            new AlertDialog.Builder(context)
                    .setMessage(msg)
                    .setPositiveButton(R.string.PartnerProgram,
                            (dialog, which) ->
                                    context.startActivity(PartnerProgramActivity.getIntent(context)))
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
            Prefs.setValue(packName, true);
        }
    }


}
