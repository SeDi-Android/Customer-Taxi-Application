package ru.sedi.customerclient.activitys.user_profile;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;


public class ProfileTabManager {

    private Context mContext;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FragmentManager mFragmentManager;

    public ProfileTabManager(Context context, FragmentManager fm, TabLayout tabLayout, ViewPager viewPager) {
        mTabLayout = tabLayout;
        mViewPager = viewPager;
        mFragmentManager = fm;
        mContext = context;
        update();
    }

    private void update() {
        mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.profile)));

        //Для TaxiLive кредикти убиравем
        if (!App.isTaxiLive)
            mTabLayout.addTab(mTabLayout.newTab().setText(mContext.getString(R.string.my_card)));
        else {
            mTabLayout.setVisibility(View.GONE);
        }

        final ProfilePagerAdapter adapter = new ProfilePagerAdapter
                (mFragmentManager, mTabLayout.getTabCount());
        mViewPager.setOffscreenPageLimit(mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
