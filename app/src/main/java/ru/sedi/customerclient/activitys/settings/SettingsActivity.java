package ru.sedi.customerclient.activitys.settings;

import android.os.Bundle;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.fragments.SettingFragment;


public class SettingsActivity extends BaseActivity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshView();
    }

    public void refreshView() {
        setContentView(R.layout.actvt_setting);
        updateTitle(R.string.settings, R.drawable.ic_settings);
        SettingFragment fragment = new SettingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
    }
}