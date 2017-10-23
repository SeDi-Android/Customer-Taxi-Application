package ru.sedi.customerclient.activitys.giving_time;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ru.sedi.customer.R;
import ru.sedi.customerclient.fragments.GivingTimeFragment;
import ru.sedi.customerclient.base.BaseActivity;

public class GivingTimeActivity extends BaseActivity {

    private static final int LAYOUT = R.layout.single_fragment_activity;


    /**
     * Новый экземпляр для Intent GivingTimeActivity
     *
     * @param context caller context.
     * @return intent instance.
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, GivingTimeActivity.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        updateTitle(R.string.giving_time, R.drawable.ic_clock);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.single_fragment);
        if (fragment == null) {
            fragment = GivingTimeFragment.getInstance(false);
            fragmentManager
                    .beginTransaction()
                    .add(R.id.single_fragment, fragment)
                    .commit();
        }
    }
}
