package ru.sedi.customerclient.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Service;
import ru.sedi.customerclient.adapters.ServicesAdapter;
import ru.sedi.customerclient.base.NoTitleFragmentDialog;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.interfaces.IAction;
import ru.sedi.customerclient.widget.BottomSheetListView;

/**
 * Created by RAM on 01.02.2018.
 */

public class SpecsDialog extends BottomSheetDialogFragment {

    @BindView(R.id.sa_lvServices)
    ListView lvServices;
    private IAction mDismissListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.actvt_services_dialog, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        disableBottomSheetDrag();
    }

    private void disableBottomSheetDrag() {
        FrameLayout bottomSheet = (FrameLayout) getDialog().findViewById(android.support.design.R.id.design_bottom_sheet);
        if(bottomSheet == null) return;

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        if(behavior == null) return;

        //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_DRAGGING)
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    /**
     * Initialization.
     */
    private void init() {
        QueryList<_Service> all = Collections.me().getServices().getAll();
        ServicesAdapter adapter = new ServicesAdapter(getActivity(),
                all);
        lvServices.setAdapter(adapter);
    }

    /**
     * action on save button click.
     */
    @OnClick(R.id.btn_save)
    @SuppressWarnings(Const.UNUSED)
    public void onSaveBtnClick() {
        dismiss();
    }

    @Override
    public void dismiss() {
        if (mDismissListener != null) {
            mDismissListener.action();
        }
        super.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setDismissListener(IAction dismissListener) {
        mDismissListener = dismissListener;
    }
}
