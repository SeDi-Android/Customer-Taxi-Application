package ru.sedi.customerclient.activitys.new_main;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.Otto.HeaderUpdateEvent;
import ru.sedi.customerclient.Otto.LocaleChangeEvent;
import ru.sedi.customerclient.Otto.OrderRegisterEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ParserManager;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.about_application.AboutAppActivity;
import ru.sedi.customerclient.activitys.active_orders_activity.ActiveOrdersActivity;
import ru.sedi.customerclient.activitys.choose_tariff.ChooseTariffActivity;
import ru.sedi.customerclient.activitys.main.NavigationViewHelper;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.base.SingleFragmentDialogActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.UpdateChecker;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.dialogs.OrderConfirmationDialog;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.fragments.InputAddressFragment;
import ru.sedi.customerclient.fragments.InputAddressMapFragment;
import ru.sedi.customerclient.interfaces.OnOrderChangeListener;

public class NewMainActivity extends BaseActivity implements OnOrderChangeListener {

    public static final int LAYOUT = R.layout.activity_new_main;
    private static final int TIMER_TIMEOUT = 5000;

    @BindView(R.id.tvTime) TextView tvTime;
    @BindView(R.id.view_shadow) FrameLayout view_shadow;

    @BindView(R.id.ivTime) ImageView ivTime;

    @BindView(R.id.tvService) TextView tvService;
    @BindView(R.id.rlServiceIconLayout) RelativeLayout rlService;
    @BindView(R.id.tvServiceCheckedCount) TextView tvServiceCheckedCount;

    @BindView(R.id.tvInfo) TextView tvInfo;
    @BindView(R.id.ivInfo) ImageView ivInfo;

    @BindView(R.id.llOptions) LinearLayout llOptions;
    @BindView(R.id.llShowItemsLayout) LinearLayout llShowItemsLayout;
    @BindView(R.id.llAddressItems) LinearLayout llAddresses;
    @BindView(R.id.btnGetTaxi) Button btnGetTaxi;


    @BindView(R.id.llOrderRegisterLayout) LinearLayout llOrderRegisterLayout;

    @BindView(R.id.mapView) MapView mapView;

    @BindView(R.id.rlSearchProgressPanel) RelativeLayout rlSearchProgressPanel;
    @BindView(R.id.btnCancel) Button btnCancel;
    @BindView(R.id.ivRadar) ImageView ivRadar;

    private _Order mOrder;
    private NavigationViewHelper mNavigationViewHelper;
    private Unbinder mUnbinder;

    private InputAddressFragment mInputAddressFragment;
    private InputAddressMapFragment mInputAddressMapFragment;
    private double lastExitTouch;
    private Timer mOrderMonitoringTimer;
    private boolean isCancelledNow;
    private boolean isShowLocationSettings;
    private boolean isCalculateNow;

    /**
     * Возвращает экземпляр Intent NewMainActivity.
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, NewMainActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);
        updateToolbar();

        mUnbinder = ButterKnife.bind(this);
        SediBus.getInstance().register(this);
        new UpdateChecker(this).start();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mInputAddressMapFragment = (InputAddressMapFragment)
                fragmentManager.findFragmentById(R.id.frmntMapInputAddress);
        mInputAddressFragment = (InputAddressFragment)
                fragmentManager.findFragmentById(R.id.frmntInputLayout);

        mNavigationViewHelper = new NavigationViewHelper(this);
        mOrder = _OrderRegistrator.me().getOrder();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.search_radar);
        animation.setInterpolator(new LinearInterpolator());
        ivRadar.setAnimation(animation);

        Collections.me().updateTariffsServices(tariffServiceData -> {
            if (tariffServiceData.getTariffs2().size() == 1) {
                mOrder.addChangeListener(getOrderChangeListener());
                _OrderRegistrator.me().setOrderCreateListener(() -> {
                    clearOrderCalcView();
                    refreshAllViews();
                });
            }
        });

        view_shadow.setOnClickListener(v -> {
            if (llOptions.getVisibility() == View.VISIBLE)
                setMenuState(true);
        });

        //Для авторизованного пользователя, ищем все его заказы что бы показать их.
        //Если пользователь не авторизован, используем его местоположение, для певого адреса.
        if (App.isAuth) {
            tryGetActiveOrder();
        } else
            mInputAddressMapFragment.findUserAddress();

        llShowItemsLayout.setOnClickListener(v -> setMenuState(llOptions.getVisibility() == View.VISIBLE));
        onLoginInfoUpdated(null);
        refreshAllViews();
    }

    private void tryGetActiveOrder() {
        new AsyncJob.Builder<QueryList<_Order>>()
                .doWork(() -> {
                    Server orders = ServerManager.GetInstance().getOrders(null);
                    return ParserManager.parseOrders(orders);
                })
                .onSuccess(orders -> {
                    if (orders == null) return;

                    //Ишем все заказы в поиске
                    QueryList<_Order> orderInSearch = orders.Where(item ->
                            OrderStatuses
                                    .getShortStatus(item.getStatus().getID())
                                    .equals(OrderStatuses.search));

                    //Если заказов в поиске нет
                    if (orderInSearch.isEmpty()) {
                        setSearchPanelState(View.GONE, Const.NoId);

                        //Ищем заказы для мониторинга
                        QueryList<_Order> monitoringOrder = orders.Where(item -> {
                            OrderStatuses shortStatus = OrderStatuses
                                    .getShortStatus(item.getStatus().getID());
                            return shortStatus.equals(OrderStatuses.waittaxi)
                                    || shortStatus.equals(OrderStatuses.inway)
                                    || shortStatus.equals(OrderStatuses.driverwaitcustomer);
                        });

                        //Если мониторинг заказов нет, открываем "чистый" экран.
                        //Если есть 1 заказ отрисовываем точки на карте.
                        //Если заказов более 1, открываем список.
                        if (monitoringOrder.isEmpty()) {
                            stopOrderMonitor();
                            setInputAddressLayoutState(View.VISIBLE);
                            mInputAddressMapFragment.updateMonitoredOrderPoint(null);
                            mInputAddressMapFragment.findUserAddress();
                        } else if (monitoringOrder.size() == 1) {
                            runOrderMonitoring();
                            llOrderRegisterLayout.setVisibility(View.GONE);
                            mInputAddressMapFragment.updateMonitoredOrderPoint(monitoringOrder.tryGet(0));
                        } else {
                            setInputAddressLayoutState(View.GONE);
                            stopOrderMonitor();
                            startActivity(ActiveOrdersActivity.getIntent(this));
                        }
                    } else {
                        //Выбираем срочные заказы
                        orderInSearch = orderInSearch.Where(_Order::isRush);

                        //Если срочный заказ только 1 и он срочный, запускаем радар поиска
                        //В остальных случаях (несколько срочных, или предварительный) то открывает
                        //activity с активными заказами.
                        if (orderInSearch.size() == 1) {
                            _Order order = orderInSearch.tryGet(0);
                            runOrderMonitoring();
                            setSearchPanelState(View.VISIBLE, order != null ? order.getID() : Const.NoId);
                        } else {
                            startActivity(ActiveOrdersActivity.getIntent(this));
                        }
                    }
                })
                .build().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void setInputAddressLayoutState(int state) {
        if (!Helpers.isVisibilityState(state))
            return;
        llOrderRegisterLayout.setVisibility(state);
    }

    @Subscribe
    @SuppressWarnings(Const.UNUSED)
    public void onNewOrderRegisterSuccess(OrderRegisterEvent event) {
        refreshAllViews();
        tryGetActiveOrder();
    }

    private void stopOrderMonitor() {
        if (mOrderMonitoringTimer != null) {
            mOrderMonitoringTimer.cancel();
            mOrderMonitoringTimer = null;
        }
        setInputAddressLayoutState(View.VISIBLE);
    }

    private void runOrderMonitoring() {
        if (mOrderMonitoringTimer == null) {
            mOrderMonitoringTimer = new Timer();
            mOrderMonitoringTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isCancelledNow) return;
                    tryGetActiveOrder();
                }
            }, TIMER_TIMEOUT, TIMER_TIMEOUT);
        }
    }

    private void setSearchPanelState(int visibleState, int searchOrderId) {
        int currentState = rlSearchProgressPanel.getVisibility();
        if (currentState == visibleState)
            return;
        rlSearchProgressPanel.setVisibility(visibleState);
        btnCancel.setOnClickListener(v -> cancelSearchableOrder(searchOrderId));
        Animation animation = ivRadar.getAnimation();
        if (animation != null) {
            if (visibleState == View.VISIBLE)
                animation.start();
            else
                animation.cancel();
        }
    }

    private void cancelSearchableOrder(int searchOrderId) {
        isCancelledNow = true;
        new AsyncJob.Builder<Boolean>()
                .withProgress(this, R.string.msg_PleaseWait)
                .doWork(() -> {
                    Server server = ServerManager.GetInstance().cancelOrder(searchOrderId);
                    return server.isSuccess();
                })
                .onSuccess(isSuccess -> {
                    String msg = getString(R.string.cancellation_not_possible);
                    if (isSuccess) {
                        msg = getString(R.string.CancelOrderSuccess);
                        stopOrderMonitor();
                        setSearchPanelState(View.GONE, Const.NoId);
                    }
                    MessageBox.show(this, msg);
                    isCancelledNow = false;
                })
                .onFailure(throwable -> {
                    MessageBox.show(this, throwable.getMessage());
                    isCancelledNow = false;
                })
                .buildAndExecute();
    }

    private void setMenuState(boolean hideOptionLayout) {
        llOptions.setVisibility(hideOptionLayout ? View.GONE : View.VISIBLE);
        view_shadow.setVisibility(hideOptionLayout ? View.GONE : View.VISIBLE);
        llAddresses.setVisibility(!hideOptionLayout ? View.GONE : View.VISIBLE);
    }

    private void clearOrderCalcView() {
        findViewById(R.id.rlTariffInfo).setVisibility(View.GONE);
        findViewById(R.id.tvSingleTariffInfo).setVisibility(View.GONE);
        findViewById(R.id.pbTariffLoading).setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrder = _OrderRegistrator.me().getOrder();
        updateUi();
        addMapScrollListener(null);
        if (mInputAddressFragment != null)
            mInputAddressFragment.updateUi();

        if (isShowLocationSettings) {
            isShowLocationSettings = false;
            mInputAddressMapFragment.findUserAddress();
        }
    }


    /**
     * Обновляем UI.
     */
    private void updateUi() {
        //Время заказа
        tvTime.setText(getOrderTime());
        ivTime.setVisibility(mOrder.isRush() ? View.GONE : View.VISIBLE);

        //Выбранные спецуслуги.
        String checkedServiceNames = Collections.me().getServices().getCheckedNames();
        tvService.setText(checkedServiceNames);
        rlService.setVisibility(checkedServiceNames.isEmpty() ? View.GONE : View.VISIBLE);
        int size = Collections.me().getServices().getChecked().size();
        tvServiceCheckedCount.setText(String.valueOf(size));

        //Доп. инфо.
        String orderDesc = mOrder.getDescription();
        tvInfo.setText(orderDesc);
        ivInfo.setVisibility(orderDesc.isEmpty() ? View.GONE : View.VISIBLE);

        btnGetTaxi.setText(mOrder.getRoute().size() <= 1 ? R.string.get_taxi : R.string.cost_calculation);



    }

    /**
     * Запускает диалог с номерами диспетчерских.
     */
    @OnClick(R.id.ibtnCallToDispatcher)
    @SuppressWarnings(Const.UNUSED)
    public void onCallToDispatcherClick() {
        showPhonesDialog();
    }

    /**
     * Отображает меню.
     */
    @OnClick(R.id.ibtnMenu)
    @SuppressWarnings(Const.UNUSED)
    public void onMenuButtonClick() {
        mNavigationViewHelper.openMenu();
    }

    /**
     * Запускает окно с выбором даты / времени.
     */
    @OnClick(R.id.llTime)
    @SuppressWarnings(Const.UNUSED)
    public void onTimeLayoutClick() {
        startActivity(SingleFragmentDialogActivity.getIntent(this,
                SingleFragmentDialogActivity.TIME_DIALOG));
    }

    /**
     * Запускает окно с выбором доп. услуг.
     */
    @OnClick(R.id.llService)
    @SuppressWarnings(Const.UNUSED)
    public void onServiceLayoutClick() {
        if (Collections.me().getServices().isEmpty()) {
            MessageBox.show(this, getString(R.string.missing_services_message));
            return;
        }
        startActivity(SingleFragmentDialogActivity.getIntent(this,
                SingleFragmentDialogActivity.SERVICES_DIALOG));
    }

    /**
     * Запускает окно для ввода доп. инфо.
     */
    @OnClick(R.id.llInfo)
    @SuppressWarnings(Const.UNUSED)
    public void onInfoLayoutClick() {
        startActivity(SingleFragmentDialogActivity.getIntent(this,
                SingleFragmentDialogActivity.INFO_DIALOG));
    }


    @OnClick(R.id.btnGetTaxi)
    @SuppressWarnings(Const.UNUSED)
    public void onGetTaxiClick() {
        if (isCalculateNow) {
            ToastHelper.ShowLongToast(getString(R.string.wait_calculation));
            return;
        }

        if (mOrderMonitoringTimer != null) {
            MessageBox.show(this, R.string.only_one_order_message);
            return;
        }

        if (Collections.me().getTariffs().isEmpty()) {
            MessageBox.show(this, getString(R.string.msg_NoTariffs), null);
            return;
        }

        _Order order = _OrderRegistrator.me().getOrder();
        _Route route = order.getRoute();
        if (route.size() == 0 || !route.isValidRoute()) {
            MessageBox.show(this, getString(R.string.IncorrectRoute), null);
            return;
        }

        if (Collections.me().getTariffs().size() == 1) {
            if (!App.isAuth) {
                showRegistrationDialog(this, false);
                return;
            }
            new OrderConfirmationDialog(this, order, () -> refreshAllViews()).show();
        } else {
            if (route.size() == 1 || !Collections.me().getUser().isAllowCalc()) {
                BaseActivity.Instance.startNewActivity(ChooseTariffActivity.class);
                return;
            }
            _OrderRegistrator.me().calculateAndShow(this);
        }
    }


    /**
     * Прячет toolbar.
     */
    private void updateToolbar() {
        ActionBar sab = getSupportActionBar();
        if (sab != null) sab.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOrderMonitoringTimer != null) {
            mOrderMonitoringTimer.cancel();
            mOrderMonitoringTimer = null;
        }
        mUnbinder.unbind();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (llOptions.getVisibility() == View.VISIBLE) {
                setMenuState(true);
                return true;
            }
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
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Отображает диалог с номерами диспетчеров.
     */
    private void showPhonesDialog() {
        final List<String> phones = Collections.me().getOwner().getPhones();
        if (phones.size() < 1) {
            ToastHelper.ShowLongToast(getString(R.string.empty_dispatche_phone_message));
            return;
        }

        new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.call_to_dispatcher)
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, phones), (dialog, which) -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phones.get(which)));
                    startActivity(callIntent);
                })
                .setPositiveButton(getString(R.string.cancel), null)
                .create().show();
    }

    /**
     * Возвращает дату/время.
     *
     * @return RUSH or order time.
     */
    public String getOrderTime() {
        String dateTime = mOrder.isRush()
                ? getString(R.string.now_time)
                : mOrder.getDateTime().toString(DateTime.DATE_TIME);

        if (!mOrder.isRush() && App.isTaxiLive)
            dateTime += " Uhr";
        return dateTime;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onLocaleChange(LocaleChangeEvent event) {
        setContentView(R.layout.actvt_main);
        updateUi();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onLoginInfoUpdated(HeaderUpdateEvent event) {
        boolean useShort = Collections.me().enableEasyCostCalculate();

        if (useShort) {
            ((ImageButton) findViewById(R.id.ibtnCallToDispatcher)).setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.small_logo));
            findViewById(R.id.ibtnCallToDispatcher).setOnClickListener(v -> startActivity(
                    AboutAppActivity.getIntent(this)));
        }
    }

    public _Order.OnChangeListener getOrderChangeListener() {
        return new _Order.OnChangeListener() {
            @Override
            public void onSuccessCalculate(_Order order) {
                isCalculateNow = false;
                setTariffInfoInPanel(order);
            }

            @Override
            public void onStartCalculate() {
                isCalculateNow = true;
                hideTariffInfoPanel();
            }

            @Override
            public void OnFailureCalculate(String message) {
                MessageBox.show(NewMainActivity.this, message);
                animateState(findViewById(R.id.rlTariffInfo), false);
            }
        };
    }

    private void hideTariffInfoPanel() {
        animateState(findViewById(R.id.rlTariffInfo), true);
        findViewById(R.id.tvSingleTariffInfo).setVisibility(View.GONE);
        findViewById(R.id.pbTariffLoading).setVisibility(View.VISIBLE);
    }

    private void setTariffInfoInPanel(_Order order) {
        TextView tvTariffInfo = (TextView) findViewById(R.id.tvSingleTariffInfo);

        if (order.getRoute().size() <= 1) {
            findViewById(R.id.rlTariffInfo).setVisibility(View.GONE);
            return;
        }

        tvTariffInfo.setOnClickListener(v ->
                new AlertDialog.Builder(NewMainActivity.this)
                        .setMessage(order.getDetails())
                        .setPositiveButton(R.string.ok, null)
                        .create().show());
        tvTariffInfo.setText(String.format(getString(R.string.easy_tariff_format),
                order.getCost(), order.getCurrency(), order.getDuration(), order.getDistance()));

        animateState(findViewById(R.id.rlTariffInfo), true);
        tvTariffInfo.setVisibility(View.VISIBLE);
        findViewById(R.id.pbTariffLoading).setVisibility(View.GONE);
    }

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
    public void refreshAllViews() {
        updateUi();
        addMapScrollListener(null);
        if (mInputAddressMapFragment != null)
            mInputAddressMapFragment.reconstructMap();
        if (mInputAddressFragment != null)
            mInputAddressFragment.updateUi();
    }

    /**
     * Alert message if GPS is setting disabled;
     */
    @Override
    public void showLocationErrorDialog() {
        MessageBox.show(this,
                getString(R.string.msg_LocationSettingError),
                null,
                new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        isShowLocationSettings = true;
                        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(viewIntent);
                    }

                    @Override
                    public void onCancelClick() {
                    }
                },
                true, new int[]{R.string.yes, R.string.no});
    }

    @Override
    public void addMapScrollListener(ru.sedi.customerclient.common.AsyncAction.IAction<LatLong> action) {
        if(mInputAddressMapFragment!=null)
            mInputAddressMapFragment.addMapScrollListener(action);
    }
}
