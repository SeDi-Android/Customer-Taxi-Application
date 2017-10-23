package ru.sedi.customerclient.activitys.special_services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.fragments.ServicesFragment;


public class ServicesActivity extends BaseActivity {

    private static final int LAYOUT = R.layout.single_fragment_activity;

    /**
     * Возвращает экземпляр Intent ServicesActivity.
     * @param context - Caller context.
     * @return - intent for ServicesActivity.
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, ServicesActivity.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        updateTitle(R.string.service_or_option, R.drawable.ic_checkbox_marked_circle_outline);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.single_fragment);
        if (fragment == null) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.single_fragment, ServicesFragment.getInstance(false))
                    .commit();
        }
    }


}