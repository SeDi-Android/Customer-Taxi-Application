package ru.sedi.customerclient.activitys.main;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.Otto.HeaderUpdateEvent;
import ru.sedi.customerclient.Otto.LocaleChangeEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.activitys.about_application.AboutAppActivity;
import ru.sedi.customerclient.activitys.active_orders_activity.ActiveOrdersActivity;
import ru.sedi.customerclient.activitys.additional_info.AdditionalInfoActivity;
import ru.sedi.customerclient.activitys.choose_tariff.ChooseTariffActivity;
import ru.sedi.customerclient.activitys.giving_time.GivingTimeActivity;
import ru.sedi.customerclient.activitys.route.MapInputAddressActivity;
import ru.sedi.customerclient.activitys.route.RouteActivity;
import ru.sedi.customerclient.activitys.special_services.ServicesActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.UpdateChecker;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.dialogs.DiscountDialog;
import ru.sedi.customerclient.dialogs.OrderConfirmationDialog;
import ru.sedi.customerclient.enums.PrefsName;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    Map<String, String> mExtras = new HashMap<>();
    public static MainActivity Instance;
    private double lastExitTouch = 0;
    private NavigationViewHelper mNavigationViewHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        setContentView(R.layout.actvt_main);
        SediBus.getInstance().register(this);
        hideActionBar();

        //Проверяем версию в маркете.
        new UpdateChecker(this).start();
        init();

        if (!Collections.me().getActiveOrders().isEmpty()) {
            startActivity(new Intent(this, ActiveOrdersActivity.class));
        }

        individualFunction();
    }

    /**
     * Если у данной группы есть специальные сообщения - показываем уже авторизованному пользователю.
     */
    private void individualFunction() {
        if (App.isAuth) {
            Helpers.showPartnerInviteMessage(this, Const.MASTER_PACKAGE_NAME, 10, 5);
            Helpers.showPartnerInviteMessage(this, Const.T24_PACKAGE_NAME, 15, 5);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
        LocationService.me().stopListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SediBus.getInstance().unregister(this);
    }

    /**
     * Инициализация UI
     */
    private void init() {

        mNavigationViewHelper = new NavigationViewHelper(this);

        findViewById(R.id.ma_rlRoute).setOnClickListener(this);
        findViewById(R.id.ma_rlMapRoute).setOnClickListener(this);
        findViewById(R.id.ma_rlGivingTime).setOnClickListener(this);
        findViewById(R.id.ma_rlServices).setOnClickListener(this);
        findViewById(R.id.ma_rlOtherInformation).setOnClickListener(this);

        findViewById(R.id.ma_rlDiscount).setOnClickListener(this);

        findViewById(R.id.ma_ibtnCallToDispatcher).setOnClickListener(this);
        findViewById(R.id.ma_ibtnMenu).setOnClickListener(this);

        this.findViewById(R.id.ma_btnGetTaxi).setOnClickListener(this);

        onLoginInfoUpdated(null);
    }

    /**
     * Инициализация данных в UI
     */
    private void initDataInUi() {
        _Order order = _OrderRegistrator.me().getOrder();
        if (Collections.me().enableEasyCostCalculate() && order.getChangeListener() == null) {
            order.addChangeListener(getOrderChangeListener());
        }
        _OrderRegistrator.me().setOrderCreateListener(() -> clearOrderCalcView());

        String services = Collections.me().getServices().getCheckedNames();

        ((TextView) this.findViewById(R.id.ma_tvRoute)).setText(order.getRoute().asString());
        ((TextView) this.findViewById(R.id.ma_tvGivingTime)).setText(order.isRush() ? getString(R.string.now_time) : order.getDateTime().toString(DateTime.DATE_TIME));
        ((TextView) this.findViewById(R.id.ma_tvServices)).setText(TextUtils.isEmpty(services) ? getString(R.string.NotSelected) : services);
        ((TextView) this.findViewById(R.id.ma_tvOtherInformation)).setText(order.getDescription());
        ((TextView) this.findViewById(R.id.ma_tvDiscountCode)).setText(Prefs.getString(PrefsName.PROMO_KEY));

        ((Button) this.findViewById(R.id.ma_btnGetTaxi)).setText(R.string.get_taxi);

        boolean enabledPromo = Prefs.getBool(PrefsName.ENABLE_PROMO);
        findViewById(R.id.ma_rlDiscount).setVisibility(enabledPromo ? View.VISIBLE : View.GONE);
    }

    /**
     * Скрывает панель с расчетом.
     */
    private void clearOrderCalcView() {
        findViewById(R.id.rlTariffInfo).setVisibility(View.GONE);
        findViewById(R.id.tvSingleTariffInfo).setVisibility(View.GONE);
        findViewById(R.id.pbTariffLoading).setVisibility(View.GONE);
    }

    @NonNull
    private _Order.OnChangeListener getOrderChangeListener() {
        return new _Order.OnChangeListener() {
            @Override
            public void onSuccessCalculate(_Order order) {
                TextView tvTariffInfo = (TextView) findViewById(R.id.tvSingleTariffInfo);

                if (order.getRoute().size() <= 1) {
                    findViewById(R.id.rlTariffInfo).setVisibility(View.GONE);
                    return;
                }

                tvTariffInfo.setOnClickListener(v -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(order.getDetails())
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                });
                tvTariffInfo.setText(String.format(getString(R.string.easy_tariff_format),
                        order.getCost(), order.getCurrency(), order.getDuration(), order.getDistance()));

                animateState(findViewById(R.id.rlTariffInfo), true);
                tvTariffInfo.setVisibility(View.VISIBLE);
                findViewById(R.id.pbTariffLoading).setVisibility(View.GONE);
            }

            @Override
            public void onStartCalculate() {
                animateState(findViewById(R.id.rlTariffInfo), true);
                findViewById(R.id.tvSingleTariffInfo).setVisibility(View.GONE);
                findViewById(R.id.pbTariffLoading).setVisibility(View.VISIBLE);
            }

            @Override
            public void OnFailureCalculate(String message) {
                MessageBox.show(MainActivity.this, message);
            }
        };
    }

    /**
     * Анимирует показ - скрытие view с расчетом.
     * @param view целевое view.
     * @param isShow флаг отображения.
     */
    private void animateState(View view, boolean isShow) {
        if (isShow)
            view.setVisibility(View.VISIBLE);
        view
                .animate()
                .alpha(isShow ? 1 : 0)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!isShow)
                            view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (mNavigationViewHelper.menuIsOpen()) {
                    mNavigationViewHelper.closeMenu();
                    return true;
                }
                if (System.currentTimeMillis() - lastExitTouch > 1000) {
                    ToastHelper.showShortToast(getString(R.string.exit_message));
                    lastExitTouch = System.currentTimeMillis();
                    return true;
                } else {
                    minimaizeApp();
                    return true;
                }
            }
            case KeyEvent.KEYCODE_MENU: {

            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ma_rlRoute: {
                startNewActivity(RouteActivity.class, null);
                break;
            }
            case R.id.ma_rlMapRoute: {
                startNewActivity(MapInputAddressActivity.class, null);
                break;
            }
            case R.id.ma_rlGivingTime: {
                startNewActivity(GivingTimeActivity.class, null);
                break;
            }
            case R.id.ma_rlServices: {
                if (Collections.me().getServices().isEmpty()) {
                    MessageBox.show(MainActivity.this, getString(R.string.missing_services_message), null);
                    LogUtil.log(LogUtil.ERROR, "Список сервисов пуст!");
                    break;
                }
                startNewActivity(ServicesActivity.class, null);
                break;
            }
            case R.id.ma_ibtnCallToDispatcher: {
                showPhonesDialog();
                break;
            }
            case R.id.ma_ibtnMenu: {
                mNavigationViewHelper.openMenu();
                break;
            }
            case R.id.ma_btnGetTaxi: {
                if (Collections.me().getTariffs().isEmpty()) {
                    MessageBox.show(MainActivity.this, getString(R.string.msg_NoTariffs), null);
                    return;
                }

                _Order order = _OrderRegistrator.me().getOrder();
                _Route route = order.getRoute();
                if (route.size() <= 0 || !route.isValidRoute()) {
                    MessageBox.show(MainActivity.this, getString(R.string.IncorrectRoute), null);
                    break;
                }

                if (Collections.me().enableEasyCostCalculate()) {
                    if (!App.isAuth) {
                        showRegistrationDialog(this, false);
                        break;
                    }
                    new OrderConfirmationDialog(MainActivity.this, order, null).show();
                } else {
                    if (route.size() <= 1 || !Collections.me().getUser().isAllowCalc()) {
                        BaseActivity.Instance.startNewActivity(ChooseTariffActivity.class, mExtras);
                        break;
                    }
                    _OrderRegistrator.me().calculateAndShow(MainActivity.this);
                }
                break;
            }
            case R.id.ma_rlOtherInformation: {
                startNewActivity(AdditionalInfoActivity.class, null);
                break;
            }
            case R.id.ma_rlDiscount: {
                new DiscountDialog(MainActivity.this).show();
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * Отображает диалог с номерами диспетчерских.
     */
    private void showPhonesDialog() {
        final List<String> phones = Collections.me().getOwner().getPhones();
        if (phones.size() < 1) {
            ToastHelper.ShowLongToast(getString(R.string.empty_dispatche_phone_message));
            return;
        }

        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.call_to_dispatcher)
                .setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, phones), (dialog, which) -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phones.get(which)));
                    startActivity(callIntent);
                })
                .setPositiveButton(getString(R.string.cancel), null)
                .create().show();
    }


    /**
     * Обновляет все view.
     */
    public void updateView() {
        try {
            runOnUiThread(this::initDataInUi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onLocaleChange(LocaleChangeEvent event) {
        setContentView(R.layout.actvt_main);
        init();
        updateView();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onLoginInfoUpdated(HeaderUpdateEvent event) {
        boolean useShort = Collections.me().enableEasyCostCalculate();

        findViewById(R.id.ivLogo).setVisibility(useShort ? View.GONE : View.VISIBLE);

        if (useShort) {
            ((ImageButton) findViewById(R.id.ma_ibtnCallToDispatcher)).setImageDrawable(
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.small_logo));
            findViewById(R.id.ma_ibtnCallToDispatcher).setOnClickListener(v -> startActivity(
                    AboutAppActivity.getIntent(MainActivity.this)));
        }
    }
}