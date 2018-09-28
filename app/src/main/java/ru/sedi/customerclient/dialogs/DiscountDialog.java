package ru.sedi.customerclient.dialogs;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import ru.sedi.customer.R;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

/**
 * Class: DiscountDialog
 * Author: RAM
 * Description: Диалоговое окно для указания промокода.
 */
public class DiscountDialog extends AppCompatDialog {

    Context mContext;

    public DiscountDialog(Context context) {
        super(context, R.style.AppCompatAlertDialogStyle);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_discount);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
        attributes.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(attributes);

        mContext = context;

        initUiElements();
    }

    /**
     * Инициализация UI элементов
     */
    private void initUiElements() {
        ((EditText) findViewById(R.id.dd_etDiscountCode)).setText(Prefs.getString(PrefsName.PROMO_KEY));
        findViewById(R.id.dd_btnSaveDiscountCode).setOnClickListener(v -> {
            saveDiscountCode();
        });
    }

    /**
     * Сохранение промокода в памяти.
     */
    private void saveDiscountCode() {
        String promo = ((EditText) findViewById(R.id.dd_etDiscountCode)).getText().toString();
        Prefs.setValue(PrefsName.PROMO_KEY, promo);
        //MainActivity.Instance.updateView();
        dismiss();
    }
}
