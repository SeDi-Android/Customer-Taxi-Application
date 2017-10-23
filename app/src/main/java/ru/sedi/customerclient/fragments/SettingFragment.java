package ru.sedi.customerclient.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import ru.sedi.customer.R;
import ru.sedi.customerclient.Otto.HeaderUpdateEvent;
import ru.sedi.customerclient.Otto.LocaleChangeEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.activitys.settings.SettingsActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.actvt_settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PrefsName.LOCALE_CODE)) {
            ((SettingsActivity) getActivity()).updateLocale();
            ((SettingsActivity) getActivity()).refreshView();
            SediBus.getInstance().post(new LocaleChangeEvent(true));
        }

        if (key.equals(PrefsName.VIBRATION_NOTIFY)) {
            SediBus.getInstance().post(new HeaderUpdateEvent());
        }

        //TODO Убрать эту фигню перед коммитом
        if (key.equals("IS_OSM")) {
            String s = "Как геокодер используется ";
            if (Prefs.getBool(key))
                s += "OSM";
            else
                s += "GOOGLE";
            getPreferenceScreen().findPreference("IS_OSM").setSummary(s);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().findPreference("LOCALE_CODE").setVisible(!App.isTaxiLive);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        String s = "Как геокодер используется ";
        if (Prefs.getBool("IS_OSM")) s += "OSM";
        else s += "GOOGLE";
        getPreferenceScreen().findPreference("IS_OSM").setSummary(s);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


}
