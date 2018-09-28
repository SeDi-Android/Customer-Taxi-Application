package ru.sedi.customerclient.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.interfaces.IAction;

import static android.app.Activity.RESULT_OK;

/**
 * Created by RAM on 01.02.2018.
 */

public class AdditionalInfoDialog extends BottomSheetDialogFragment {

    @BindView(R.id.etOtherInfo)
    EditText etOtherInfo;
    @BindView(R.id.view)
    ListView mListViewCompat;
    @BindView(R.id.ibtnAction)
    ImageButton ibtnAction;

    private _Order mOrder;
    private IAction mDismissListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.actvt_other_info_dialog, container, false);
        ButterKnife.bind(this, view);
        mOrder = _OrderRegistrator.me().getOrder();
        init();
        return view;
    }

    private void init() {
        //Добавляем сообщения по умолчанию.
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add(getString(R.string.need_air_conditioning));
        msgs.add(getString(R.string.need_more_message));
        msgs.add(getString(R.string.call_when_filing));
        msgs.add(getString(R.string.free_trunk_car));

        mListViewCompat.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, msgs));
        mListViewCompat.setOnItemClickListener((parent, view, position, id) ->
                etOtherInfo.setText((String) parent.getAdapter().getItem(position)));

        etOtherInfo.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                saveAdditionalInfo();
                return true;
            }
            return false;
        });
        etOtherInfo.setText(mOrder.getDescription());
        ibtnAction.setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(getContext(), ibtnAction);
            menu.getMenuInflater().inflate(R.menu.menu_additional_info, menu.getMenu());
            menu.setOnMenuItemClickListener(menuItem -> processPopupClick(menuItem.getItemId()));
            menu.show();
        });
    }

    /**
     * Обрабатывает нажатие на элементе Popup menu.
     *
     * @param id идентификатор пункта меню.
     * @return флаг об обработанном событии.
     */
    private boolean processPopupClick(int id) {
        switch (id) {
            case R.id.menu_clear_item: {
                etOtherInfo.setText(Const.EmptyStr);
                return true;
            }
            case R.id.menu_voice_input_item: {
                SpeechRecognitionHelper.run(this);
                return true;
            }

            case R.id.menu_save_item: {
                saveAdditionalInfo();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    /**
     * Сохраняет в заказ введенное в поле {@code etOtherInfo} сообщение.
     */
    @OnClick(R.id.btn_save)
    @SuppressWarnings(Const.UNUSED)
    public void saveAdditionalInfo() {
        Helpers.hideKeyboard(etOtherInfo);

        int length = etOtherInfo.getText().length();
        if (length > 0 && length < 4) {
            MessageBox.show(getActivity(), getString(R.string.bad_text_lenght_message), null);
            return;
        }
        mOrder.setDescription(etOtherInfo.getText().toString());
        if (mDismissListener != null) {
            mDismissListener.action();
        }
        dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        final ArrayList<String> text = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        etOtherInfo.setText((text.get(0)));
                    } catch (Exception e) {
                        BaseActivity.Instance.showDebugMessage(44, e);
                    }
                }
                break;
            }
        }
    }

    public void setDismissListener(IAction dismissListener) {
        mDismissListener = dismissListener;
    }
}
