package ru.sedi.customerclient.activitys.user_profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import ru.sedi.customerclient.fragments.CardsFragment;
import ru.sedi.customerclient.fragments.ProfileFragment;

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;
    private ProfileFragment mProfileFragment;
    private CardsFragment mCards;


    public ProfilePagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (mProfileFragment == null)
                    mProfileFragment = new ProfileFragment();
                return mProfileFragment;
            case 1:
                if (mCards == null)
                    mCards = new CardsFragment();
                return mCards;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}