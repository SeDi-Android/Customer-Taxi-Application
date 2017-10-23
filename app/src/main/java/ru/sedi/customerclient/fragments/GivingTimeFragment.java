package ru.sedi.customerclient.fragments;


import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.Toast.ToastHelper;


public class GivingTimeFragment extends Fragment {

    private static final int LAYOUT = R.layout.actvt_giving_time;
    private static final int DIALOG_LAYOUT = R.layout.actvt_giving_time_dialog;
    private static final String IS_DIALOG = "IS_DIALOG";

    @BindView(R.id.sw_rush_order) Switch swRushOrder;
    private _Order mOrder;
    private Unbinder mUnbinder;

    /**
     * Create new GivingTimeFragment instance.
     *
     * @param isDialog use this fragment in dialog.
     * @return GivingTimeFragment fragment.
     */
    public static GivingTimeFragment getInstance(boolean isDialog) {
        GivingTimeFragment fragment = new GivingTimeFragment();
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
        mOrder = _OrderRegistrator.me().getOrder();
        init();

        return view;
    }

    /**
     * Initialize view component.
     */
    private void init() {
        setRushCheckedState(mOrder.isRush());
        swRushOrder.setOnCheckedChangeListener((buttonView, isChecked) ->
                updateSwitchTextColor(isChecked));

        PreorderTimeFragment preorderTimeFragment = (PreorderTimeFragment) getChildFragmentManager()
                .findFragmentById(R.id.frg_preorder);
        if (preorderTimeFragment == null) {
            preorderTimeFragment = PreorderTimeFragment.getInstance();
            preorderTimeFragment.subscribeOnChange(() -> {setRushCheckedState(false);});
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frg_preorder, preorderTimeFragment)
                    .commit();
        }
    }

    /**
     * Set switch state by order time type.
     *
     * @param rush order type flag (true == rush, false == preliminary)
     */
    private void setRushCheckedState(boolean rush) {
        swRushOrder.setChecked(rush);
        updateSwitchTextColor(rush);
    }

    /**
     * Update switch text color by order tyme type.
     * @param rush order type flag (true == rush, false == preliminary)
     */
    private void updateSwitchTextColor(boolean rush) {
        @ColorRes int color = rush ? R.color.primaryColor : R.color.liteGrayColor2;
        swRushOrder.setTextColor(ContextCompat.getColor(getContext(), color));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        _OrderRegistrator.me().calculate(getContext());
    }

    @SuppressWarnings(Const.UNUSED)
    @OnClick(R.id.btn_save)
    public void onSaveButtonClick() {
        boolean isRush = swRushOrder.isChecked();
        mOrder.setRush(isRush);

        if (!isRush && !mOrder.isValidPreOrderTime()) {
            ToastHelper.showShortToast(getString(R.string.incorrect_order_time_message));
            return;
        }
        FragmentActivity activity = getActivity();
        if (activity != null)
            activity.finish();
    }
}
