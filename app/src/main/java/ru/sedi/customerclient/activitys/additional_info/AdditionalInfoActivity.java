package ru.sedi.customerclient.activitys.additional_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.fragments.OtherInfoFragment;

public class AdditionalInfoActivity extends BaseActivity {

    private static final int LAYOUT = R.layout.single_fragment_activity;

    /**
     * Новый экземпляр для Intent AdditionalInfoActivity.
     *
     * @param context caller context.
     * @return intent instance.
     */
    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, AdditionalInfoActivity.class);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        updateTitle(R.string.additional_info, R.drawable.ic_information_outline);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.single_fragment);
        if (fragment == null) {
            fragment = new OtherInfoFragment();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.single_fragment, fragment)
                    .commit();
        }
    }


}