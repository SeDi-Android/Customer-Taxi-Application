package ru.sedi.customerclient.activitys.user_profile;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;

public class ProfileActivity extends BaseActivity {

    public static final int LAYOUT = R.layout.actvt_profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        updateTitle(R.string.profile, R.drawable.ic_account_card_details);
        trySetElevation(0);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        new ProfileTabManager(getBaseContext(), getSupportFragmentManager(), tabLayout, viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.isAuth) {
            showRegistrationDialog(this);
        }
    }
}
