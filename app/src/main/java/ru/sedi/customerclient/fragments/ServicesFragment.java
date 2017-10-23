package ru.sedi.customerclient.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.adapters.ServicesAdapter;


public class ServicesFragment extends Fragment {

    private static final int LAYOUT = R.layout.actvt_services;
    private static final int DIALOG_LAYOUT = R.layout.actvt_services_dialog;
    private static final String IS_DIALOG = "IS_DIALOG";

    @BindView(R.id.sa_lvServices) ListView lvServices;
    private Unbinder mUnbinder;

    public static ServicesFragment getInstance(boolean isDialog) {
        ServicesFragment fragment = new ServicesFragment();
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
        mUnbinder = ButterKnife.bind(this, view);
        init();
        return view;
    }

    /**
     * Initialization.
     */
    private void init() {
        ServicesAdapter adapter = new ServicesAdapter(getActivity(),
                Collections.me().getServices().getAll());
        lvServices.setAdapter(adapter);
    }

    /**
     * action on save button click.
     */
    @OnClick(R.id.btn_save)
    @SuppressWarnings(Const.UNUSED)
    public void onSaveBtnClick() {
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

}
