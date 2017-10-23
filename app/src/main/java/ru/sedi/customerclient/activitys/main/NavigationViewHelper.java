package ru.sedi.customerclient.activitys.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.Otto.HeaderUpdateEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.activitys.about_application.AboutAppActivity;
import ru.sedi.customerclient.activitys.active_orders_activity.ActiveOrdersActivity;
import ru.sedi.customerclient.activitys.customer_balance.CustomerBalanceActivity;
import ru.sedi.customerclient.activitys.order_history.OrderHistoryActivity;
import ru.sedi.customerclient.activitys.partner_program.PartnerProgramActivity;
import ru.sedi.customerclient.activitys.settings.SettingsActivity;
import ru.sedi.customerclient.activitys.user_profile.ProfileActivity;
import ru.sedi.customerclient.activitys.user_registration.UserPhoneRegisterActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Customer._Balance;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.common.SystemManagers.Device;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;


public class NavigationViewHelper implements NavigationView.OnNavigationItemSelectedListener, DrawerLayout.DrawerListener {

    private Context mContext;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private View mHeaderView;

    public NavigationViewHelper(Activity activity) {
        mContext = activity;
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) activity.findViewById(R.id.nvView);
        mNavigationView.setNavigationItemSelectedListener(this);
        mHeaderView = mNavigationView.getHeaderView(0);
        mDrawerLayout.setDrawerListener(this);

        updateMenuItems();

        updateHeader(new HeaderUpdateEvent());
        SediBus.getInstance().register(this);
    }

    private void updateMenuItems() {
        if (App.isTaxiLive) {
            MenuItem item = mNavigationView.getMenu().findItem(R.id.menu_customer_balance);
            if (item != null)
                item.setVisible(false);

            item = mNavigationView.getMenu().findItem(R.id.menu_about);
            if (item != null)
                item.setVisible(false);
        }
    }

    @Subscribe
    public void updateHeader(HeaderUpdateEvent event) {
        if (mHeaderView != null) {
            mHeaderView.setVisibility(App.isAuth ? View.VISIBLE : View.GONE);
            updateBalance(Collections.me().getUser().getBalance());
            final Switch swSilence = (Switch) mHeaderView.findViewById(R.id.swSilenceMode);
            swSilence.setChecked(Prefs.getBool(PrefsName.VIBRATION_NOTIFY));
            swSilence.setOnClickListener(v -> Prefs.setValue(PrefsName.VIBRATION_NOTIFY, swSilence.isChecked()));

            ImageButton ibtnExit = (ImageButton) mHeaderView.findViewById(R.id.ibtnExit);
            ibtnExit.setOnClickListener(v -> {
                closeMenu();
                new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle)
                        .setMessage(R.string.msg_exit_account)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            Prefs.setValue(PrefsName.USER_KEY, "");
                            App.isAuth = false;
                            Collections.me().setUser(new _LoginInfo());
                            mContext.startActivity(new Intent(mContext, UserPhoneRegisterActivity.class));
                            updateHeader(new HeaderUpdateEvent());
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            });
        }
    }

    @Subscribe
    public void updateBalance(_Balance balance) {
        if (mHeaderView != null) {
            ((TextView) mHeaderView.findViewById(R.id.tvUserName)).setText(Collections.me().getUser().getName());
            TextView tvBalance = (TextView) mHeaderView.findViewById(R.id.tvBalance);
            tvBalance.setText(
                    String.format("%s: %.1f%s",
                            mContext.getString(R.string.Balance),
                            balance.getBalance(),
                            balance.getCurrency()));
            tvBalance.setVisibility(App.isTaxiLive ? View.GONE : View.VISIBLE);

        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.menu_my_order: {
                mContext.startActivity(new Intent(mContext, ActiveOrdersActivity.class));
                break;
            }
            case R.id.menu_order_history: {
                mContext.startActivity(new Intent(mContext, OrderHistoryActivity.class));
                break;
            }
            case R.id.menu_customer_balance: {
                mContext.startActivity(new Intent(mContext, CustomerBalanceActivity.class));
                break;
            }
            case R.id.menu_profile: {
                mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                break;
            }
            case R.id.menu_about: {
                mContext.startActivity(new Intent(mContext, AboutAppActivity.class));
                break;
            }
            case R.id.menu_settings: {
                mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                break;
            }
            case R.id.menu_shared: {
                if (!App.isAuth)
                    shareApp();
                else
                    mContext.startActivity(PartnerProgramActivity.getIntent(mContext));
                break;
            }
            case R.id.menu_exit: {
                ((BaseActivity) mContext).minimaizeApp();
                break;
            }
            case R.id.menu_writeUs: {
                String email = Collections.me().getOwner().getEmail();

                String info = "***This system information please do not delete it***";
                info += "\n" + Device.getInfo(mContext);
                info += "\nISS" + Collections.me().getUser().getID() + "SD";
                info += "\n" + mContext.getPackageName().replace("ru.sedi", "app");
                info += "\n***\n\n";

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_TEXT, info);
                try {
                    BaseActivity.Instance.startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mContext, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
        return false;
    }

    private void shareApp() {
        if (Collections.me().getUser().isEnabledPartnerProgram()) {
            new AlertDialog.Builder(mContext)
                    .setMessage(R.string.share_welcome_message)
                    .setPositiveButton(R.string.autorithation, (dialog, which) ->
                            mContext.startActivity(new Intent(mContext, UserPhoneRegisterActivity.class)))
                    .setNegativeButton(R.string.shared_app, (dialog, which) -> showShareAppChooserDialog())
                    .create().show();
        } else {
            showShareAppChooserDialog();
        }

    }

    private void showShareAppChooserDialog() {
        String msg = mContext.getString(R.string.share_message);
        msg = String.format(msg, mContext.getString(R.string.appName),
                "https://play.google.com/store/apps/details?id=" + mContext.getApplicationInfo().packageName);
        Helpers.showShareChooserDialog(mContext, msg);
    }

    public void openMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    public boolean menuIsOpen() {
        return mDrawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    public void closeMenu() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        String email = Collections.me().getOwner().getEmail();
        mNavigationView.getMenu().findItem(R.id.menu_writeUs).setVisible(!TextUtils.isEmpty(email));
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {
    }
}
