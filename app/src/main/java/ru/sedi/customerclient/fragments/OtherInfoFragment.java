package ru.sedi.customerclient.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;

import static android.app.Activity.RESULT_OK;

public class OtherInfoFragment extends Fragment {

    private static final int LAYOUT = R.layout.actvt_other_info;
    private static final int DIALOG_LAYOUT = R.layout.actvt_other_info_dialog;
    private static final String IS_DIALOG = "IS_DIALOG";

    @BindView(R.id.etOtherInfo) EditText etOtherInfo;
    @BindView(R.id.view) ListView mListViewCompat;
    @BindView(R.id.ibtnSpeechRec) ImageButton ibtnSpeechRec;

    private _Order mOrder;

    public static OtherInfoFragment getInstance(boolean isDialog) {
        OtherInfoFragment fragment = new OtherInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_DIALOG, isDialog);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        int layoutRes = LAYOUT;
        if (args != null && args.getBoolean(IS_DIALOG, false)) {
            layoutRes = DIALOG_LAYOUT;
        }

        View view = inflater.inflate(layoutRes, container, false);
        ButterKnife.bind(this, view);
        mOrder = _OrderRegistrator.me().getOrder();
        init();
        return view;
    }

    /*
     * Initialization.
     */
    private void init() {
        //Create easy message
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add(getString(R.string.need_air_conditioning));
        msgs.add(getString(R.string.need_more_message));
        msgs.add(getString(R.string.call_when_filing));
        msgs.add(getString(R.string.free_trunk_car));

        mListViewCompat.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, msgs));
        mListViewCompat.setOnItemClickListener((parent, view, position, id) ->
                etOtherInfo.setText((String) parent.getAdapter().getItem(position)));

        etOtherInfo.setText(mOrder.getDescription());
        ibtnSpeechRec.setOnClickListener(view -> SpeechRecognitionHelper.run(OtherInfoFragment.this));
    }

    /**
     * action on save button click.
     */
    @OnClick(R.id.btn_save)
    @SuppressWarnings(Const.UNUSED)
    public void SaveOrderOtherInfo() {
        int length = etOtherInfo.getText().length();
        if (length > 0 && length < 4) {
            MessageBox.show(getActivity(), getString(R.string.bad_text_lenght_message), null);
            LogUtil.log(LogUtil.ERROR, "Длина сообщения меньше 3 символов");
            return;
        }
        mOrder.setDescription(etOtherInfo.getText().toString());
        getActivity().finish();
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

}
