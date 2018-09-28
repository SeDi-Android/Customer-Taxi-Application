package ru.sedi.customerclient.dialogs;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.base.NoTitleFragmentDialog;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.fragments.PreorderTimeFragment;
import ru.sedi.customerclient.interfaces.IAction;

public class DateTimeDialog extends BottomSheetDialogFragment {

    private _Order mOrder;

    @BindView(R.id.sw_rush_order) Switch swRushOrder;
    private IAction mDismissListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.actvt_giving_time_dialog, container, false);
        ButterKnife.bind(this, view);
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
            preorderTimeFragment.subscribeOnChange(() -> {
                setRushCheckedState(false);
            });
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
     *
     * @param rush order type flag (true == rush, false == preliminary)
     */
    private void updateSwitchTextColor(boolean rush) {
        @ColorRes int color = rush ? R.color.primaryColor : R.color.liteGrayColor2;
        swRushOrder.setTextColor(ContextCompat.getColor(getContext(), color));
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
        if(mDismissListener!=null) {
            mDismissListener.action();
        }
        dismiss();
    }

    public void setDismissListener(IAction dismissListener) {
        mDismissListener = dismissListener;
    }
}
