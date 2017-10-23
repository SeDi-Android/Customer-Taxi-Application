package ru.sedi.customerclient.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;

import ru.sedi.customerclient.fragments.AddressHistoryFragment;
import ru.sedi.customerclient.fragments.GivingTimeFragment;
import ru.sedi.customerclient.fragments.OtherInfoFragment;
import ru.sedi.customerclient.fragments.RoutesHistoryFragment;
import ru.sedi.customerclient.fragments.ServicesFragment;

public class SingleFragmentDialogActivity extends BaseFragmentActivity {

    public static final int TIME_DIALOG = 1;
    public static final int SERVICES_DIALOG = 2;
    public static final int INFO_DIALOG = 3;
    public static final int ROUTE_DIALOG = 4;
    public static final int POINTS_DIALOG = 5;

    private static final String TYPE = "TYPE";

    /**
     * Create new intent instance.
     *
     * @param dialogType once of TIME_DIALOG, SERVICES_DIALOG, INFO_DIALOG;
     * @param context    caller context.
     * @return intent instance.
     */
    public static Intent getIntent(Context context, int dialogType) {
        Intent intent = new Intent(context, SingleFragmentDialogActivity.class);
        intent.putExtra(TYPE, dialogType);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public Fragment getFragment() {
        int type = getIntent().getIntExtra(TYPE, -1);
        switch (type) {
            case TIME_DIALOG:
                return GivingTimeFragment.getInstance(true);
            case SERVICES_DIALOG:
                return ServicesFragment.getInstance(true);
            case INFO_DIALOG:
                return OtherInfoFragment.getInstance(true);
            case ROUTE_DIALOG:
                return RoutesHistoryFragment.getInstance();
            case POINTS_DIALOG:
                return AddressHistoryFragment.getInstance();
            default:
                return new Fragment();
        }
    }
}
