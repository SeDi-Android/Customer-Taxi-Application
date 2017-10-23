package ru.sedi.customerclient.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import ru.sedi.customer.R;

public abstract class BaseFragmentActivity extends AppCompatActivity {

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);

        mFragmentManager = getSupportFragmentManager();
        Fragment fragment = mFragmentManager.findFragmentById(R.id.single_fragment);
        if (fragment == null) {
            mFragmentManager
                    .beginTransaction()
                    .add(R.id.single_fragment, getFragment())
                    .commit();
        }
    }

    public abstract Fragment getFragment();

    public void replaceFragment(Fragment fragment, boolean withBack) {
        FragmentTransaction transaction = mFragmentManager
                .beginTransaction()
                .replace(R.id.single_fragment, fragment);

        if (withBack)
            transaction.addToBackStack(fragment.getClass().getName());

        transaction.commit();
    }
}
