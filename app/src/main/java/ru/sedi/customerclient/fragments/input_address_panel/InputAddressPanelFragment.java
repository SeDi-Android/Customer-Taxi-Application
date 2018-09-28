package ru.sedi.customerclient.fragments.input_address_panel;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.interfaces.IAction;

public class InputAddressPanelFragment extends Fragment implements AHBottomNavigation.OnTabSelectedListener {

    @BindView(R.id.bnv_page_type)
    AHBottomNavigation bnv_page_type;

    @BindView(R.id.fl_address_main_box)
    FrameLayout fl_address_main_box;
    private IAction mSaveRouteListener;
    private _Point mAddressPoint;
    private Class<? extends Fragment> mLastFragmentClass;
    private int mIndex = -1;
    private Fragment mVoiceInputAddressFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_address_panel,
                container, false);
        ButterKnife.bind(this, view);
        prepareNavigationView();
        return view;
    }

    private void prepareNavigationView() {
        bnv_page_type.setDefaultBackgroundColor(ContextCompat.getColor(getContext(), R.color.backgroundColor));
        bnv_page_type.setUseElevation(false);
        bnv_page_type.setAccentColor(ContextCompat.getColor(getContext(), R.color.primaryColor));
        bnv_page_type.setInactiveColor(Color.BLACK);
        bnv_page_type.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
        bnv_page_type.addItem(new AHBottomNavigationItem("", R.drawable.ic_magnify_dark));
        bnv_page_type.addItem(new AHBottomNavigationItem("", R.drawable.ic_home_map_marker_dark));
        bnv_page_type.addItem(new AHBottomNavigationItem("", R.drawable.ic_routes_dark));
        bnv_page_type.addItem(new AHBottomNavigationItem("", R.drawable.ic_microphone_dark));
        bnv_page_type.setOnTabSelectedListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        pushFragment(getInputAddressFragment());
    }

    @NonNull
    private InputAddressFragment getInputAddressFragment() {
        InputAddressFragment fragment = new InputAddressFragment();
        fragment.setSaveRouteListener(mSaveRouteListener);
        fragment.setAddressPointWithIndex(mAddressPoint, mIndex);
        return fragment;
    }

    @NonNull
    public Fragment getVoiceInputAddressFragment() {
        InputAddressFragment fragment = new InputAddressFragment();
        fragment.setSaveRouteListener(mSaveRouteListener);
        fragment.setAddressPointWithIndex(null, mIndex);
        fragment.setIsVoiceInput();
        return fragment;
    }

    @NonNull
    private AddressHistoryFragment getAddressHistoryFragment() {
        AddressHistoryFragment fragment = new AddressHistoryFragment();
        fragment.setSaveRouteListener(mSaveRouteListener);
        return fragment;
    }

    @NonNull
    private RouteHistoryFragment getRouteHistoryFragment() {
        RouteHistoryFragment fragment = new RouteHistoryFragment();
        fragment.setSaveRouteListener(mSaveRouteListener);
        return fragment;
    }

    private void pushFragment(Fragment fragment) {
        if (mLastFragmentClass != null && mLastFragmentClass.equals(fragment.getClass())) return;

        mLastFragmentClass = fragment.getClass();

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_address_main_box, fragment)
                .commit();
    }

    public void setSaveRouteListener(IAction saveRouteListener) {
        mSaveRouteListener = saveRouteListener;
    }

    public void setAddressPoint(_Point addressPoint) {
        mAddressPoint = addressPoint;
    }

    public void setAddressPointWithIndex(_Point addressPoint, int index) {
        mAddressPoint = addressPoint;
        mIndex = index;
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        switch (position) {
            case 0: {
                pushFragment(getInputAddressFragment());
                return true;
            }
            case 1: {
                pushFragment(getAddressHistoryFragment());
                return true;
            }
            case 2: {
                pushFragment(getRouteHistoryFragment());
                return true;
            }
            case 3: {
                mLastFragmentClass = null;
                pushFragment(getVoiceInputAddressFragment());
                bnv_page_type.setCurrentItem(0, false);
                return false;
            }
        }
        return false;
    }
}
