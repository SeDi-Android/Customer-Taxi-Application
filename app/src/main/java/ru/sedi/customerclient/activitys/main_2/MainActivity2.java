package ru.sedi.customerclient.activitys.main_2;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kg.ram.asyncjob.AsyncJob;
import kg.ram.asyncjob.IOnSuccessListener;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.Collections._ServiceCollection;
import ru.sedi.customerclient.NewDataSharing.CostCalculationResult;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.Otto.LocaleChangeEvent;
import ru.sedi.customerclient.Otto.OrderRegisterEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.about_application.AboutAppActivity;
import ru.sedi.customerclient.activitys.active_orders_activity.ActiveOrdersActivity;
import ru.sedi.customerclient.activitys.partner_program.PartnerProgramActivity;
import ru.sedi.customerclient.activitys.user_registration.UserPhoneRegisterActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.GeoLocation.SediOsmrManager;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders.ActiveOrdersMonitoring;
import ru.sedi.customerclient.classes.Orders.IOrderMonitoringListener;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.OsmMapController;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.GeoTools.GeoTools;
import ru.sedi.customerclient.common.GeoTools.Units;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.dialogs.AdditionalInfoDialog;
import ru.sedi.customerclient.dialogs.DateTimeDialog;
import ru.sedi.customerclient.dialogs.DriverInfoDialog;
import ru.sedi.customerclient.dialogs.SpecsDialog;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.fragments.input_address_panel.InputAddressPanelFragment;
import ru.sedi.customerclient.interfaces.IAction;
import ru.sedi.customerclient.interfaces.ILocationChangeListener;
import ru.sedi.customerclient.tasks.LocationGeocodeTask;

public class MainActivity2 extends BaseActivity implements ILocationChangeListener {

    private static final int LAYOUT_RES_ID = R.layout.activity_main2;
    private static final int MAP_ICON_COUNT = 10;
    private static String RUN_WITH_ADDRESSES = "RUN_WITH_ADDRESSES";

    @BindView(R.id.ll_options_panel)
    LinearLayout llOptionsPanel;
    @BindView(R.id.ll_options)
    LinearLayout ll_options;
    @BindView(R.id.ll_road_info)
    LinearLayout ll_road_info;
    @BindView(R.id.fl_shadow)
    FrameLayout fl_shadow;
    @BindView(R.id.iv_time)
    ImageView ivTime;
    @BindView(R.id.iv_info)
    ImageView ivInfo;
    @BindView(R.id.tv_spec_count)
    TextView tvSpecsCount;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_spec)
    TextView tv_spec;
    @BindView(R.id.tv_info)
    TextView tv_info;
    @BindView(R.id.tv_road_distance)
    TextView tv_road_distance;
    @BindView(R.id.tv_road_time)
    TextView tv_road_time;
    @BindView(R.id.ll_spec_img)
    LinearLayout llSpecImg;
    @BindView(R.id.ll_address)
    LinearLayout ll_addresses;
    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.btn_get_taxi)
    Button btnGetTaxi;
    @BindView(R.id.fab_find_me)
    FloatingActionButton fab_find_me;
    @BindView(R.id.map_pin)
    ImageView map_pin;
    @BindView(R.id.ibtn_call_dispatcher)
    ImageButton ibtn_call_dispatcher;

    private Polyline mMapRouteOverlay;
    private SediOsmrManager mRoadManager;
    private NavigationViewHelper mNavigationViewHelper;
    private OsmMapController mMapController;
    private boolean isShowLocationSettings;
    private LatLong mUserLocation;
    private boolean mIsFirstRun = true;
    private final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private LocationGeocodeTask mLocationGeocodeTask;
    private _Point mTempPoint;

    //Используется при движении карты - после определения точки, сюда подставляется адрес.
    private EditText mInputView;
    //Используется для блокировки кнопки пока определяется адрес для поля {@code mInputView}
    private ImageButton mActionButton;

    private Intent mRestartIntent;
    private IOrderMonitoringListener mOrderMonitoringListener;

    private boolean mIsWaitingUserLocation;


    //<editor-fold desc="Static block">
    public static Intent getIntent(Context context, boolean withAddresses) {
        Intent intent = new Intent(context, MainActivity2.class);
        intent.putExtra(RUN_WITH_ADDRESSES, withAddresses);
        return intent;
    }
    //</editor-fold>

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT_RES_ID);
        hideActionBar();
        ButterKnife.bind(this);
        SediBus.getInstance().register(this);
        LocationService.me().startListener(this);

        mMapController = new OsmMapController(this, mapView);
        mNavigationViewHelper = new NavigationViewHelper(this);
        mRoadManager = new SediOsmrManager(this, false);

        updateAddresses();
        updateActionPanel();
        updateScrollListener();
        updateGetTaxiBtnText();

        prepareCallButton();

        Collections.me().updateTariffsServices(null);

        _Order order = _OrderRegistrator.me().getOrder();
        int costCalculationId = order.getCostCalculationId();
        if (costCalculationId > 0)
            restoreInfoByCostCalculationId(costCalculationId);

        if (!getIntent().getBooleanExtra(RUN_WITH_ADDRESSES, false)) {
            requestUserCity();

            if (App.isAuth)
                tryGetActiveOrder();
            else if (order.getRoute().isEmpty()) {
                findUserAddress();
            }
        }
    }

    private void prepareCallButton() {
        Drawable drawable = ContextCompat.getDrawable(this,
                App.isExcludedApp ? R.drawable.small_logo : R.drawable.ic_phone_in_talk);
        ibtn_call_dispatcher.setImageDrawable(drawable);
    }

    private void restoreInfoByCostCalculationId(int costCalculationId) {
        _Order order = _OrderRegistrator.me().getOrder();

        new AsyncJob.Builder<CostCalculationResult>()
                .doWork(() -> ServerManager.GetInstance().getCostCalculationResult(costCalculationId))
                .onSuccess(order::setCostCalculationResult)
                .onFailure((ex) -> order.resetCostCalculationInfo())
                .buildAndExecute();
    }

    private void requestUserCity() {
        LatLong lastLocation = LocationService.me().getLastLocation();
        if (lastLocation == null) {
            Prefs.setValue(PrefsName.CURRENT_CITY, "");
            return;
        }
        new AsyncJob.Builder<_Point>()
                .doWork(() -> LocationService.me()
                        .getAddressByLocationPoint(this, lastLocation.toString()))
                .onSuccess(point -> {
                    if (point != null && !TextUtils.isEmpty(point.getCityName())) {
                        Prefs.setValue(PrefsName.CURRENT_CITY, point.getCityName());
                        Log.i("Current city: ", point.getCityName());
                    }
                })
                .build()
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Subscribe
    @SuppressWarnings(Const.UNUSED)
    public void onNewOrderRegisterSuccess(OrderRegisterEvent event) {
        _OrderRegistrator.me().resetLastOrder();
        updateAddresses();
        updateActionPanel();
        tryGetActiveOrder();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onLocaleChange(LocaleChangeEvent event) {
        mRestartIntent = getIntent();
    }

    private void findUserAddress() {
        if (!Prefs.getBool(PrefsName.ENABLED_SET_DEFAULT_ADDRESS)) {
            moveToUserLocation();
            return;
        }

        if (mUserLocation == null || !mUserLocation.isValid()) {
            if (!LocationService.me().onceProviderEnabled())
                showLocationErrorDialog();
            else
                mIsWaitingUserLocation = true;
        } else {
            mMapController.zoomTo(mUserLocation, 18);
            showExampleAddressDialog();
        }
    }

    private void moveToUserLocation() {
        if (mUserLocation == null)
            mUserLocation = LocationService.me().getLastLocation();

        if (mUserLocation != null)
            mMapController.zoomTo(mUserLocation, MapView.MAXIMUM_ZOOMLEVEL);
    }

    private void tryGetActiveOrder() {
        mOrderMonitoringListener = orders -> {
            orders = orders.Where(o -> {
                OrderStatuses status = OrderStatuses.getShortStatus(o.getStatus().getID());
                return status != OrderStatuses.cancelled;
            });

            //Заказов нет
            if (orders == null || orders.isEmpty()) {
                ActiveOrdersMonitoring.getInstance().removeListener(mOrderMonitoringListener);
                updateMonitoredOrderPoint(null);
                setInputAddressLayoutVisibilityState(true);
                findUserAddress();
                return;
            }

            ActiveOrdersMonitoring.getInstance().removeListener(mOrderMonitoringListener);
            updateMonitoredOrderPoint(null);
            setInputAddressLayoutVisibilityState(true);
            startActivity(ActiveOrdersActivity.getIntent(this));
            moveToUserLocation();
        };
        ActiveOrdersMonitoring.getInstance().addListener(mOrderMonitoringListener);
    }

    public void updateMonitoredOrderPoint(_Order order) {
        mMapController.clearAllOverlays();

        if (order == null) return;

        _Point point = order.getRoute().getPoints().tryGet(0);
        if (point != null)
            mMapController.addPoint(point.getLatLong(), R.drawable.ic_map_1, null);

        _Driver driver = order.getDriver();
        if (driver != null && driver.isValid()) {
            IAction driverTap = getDriverTap(order);
            mMapController.addPoint(driver.getGeo().toLatLong(),
                    R.drawable.ic_map_taxi_car, driverTap);
            mMapController.moveTo(driver.getGeo().toLatLong());
        }
    }

    public IAction getDriverTap(_Order order) {
        return () -> new DriverInfoDialog(this, order).show();
    }

    private void setInputAddressLayoutVisibilityState(boolean visible) {
        int state = visible ? View.VISIBLE : View.GONE;
        if (!Helpers.isVisibilityState(state)) return;

        if (visible) updateScrollListener();
        else mMapController.getMap().setOnTouchListener(null);

        ll_addresses.setVisibility(state);
        ll_options.setVisibility(state);
        map_pin.setVisibility(state);
        fab_find_me.setVisibility(state);
    }


    private void showExampleAddressDialog() {
        if (!mIsFirstRun || !mUserLocation.isValid()) return;

        mIsFirstRun = false;

        IOnSuccessListener<_Point> successListener = point -> {
            if (point == null || !point.getChecked())
                return;

            if (GeoTools.calculateDistance(mUserLocation, point.getLatLong(), Units.Meters)
                    > App.RADIUS_LIMIT) {
                point = new _Point(point.getCityName(), mUserLocation, true, true);
            }
            if (!point.isCoordinatesonly()) {
                if (mInputView != null) {
                    mInputView.setText(point.asString());
                }
                mTempPoint = point;
                Prefs.setValue(PrefsName.LAST_CITY, point.getCityName());
            }
        };
        LocationService.me().getAsyncPointByLocation(this, false, mUserLocation, successListener);
    }

    public void updateScrollListener() {
        View.OnTouchListener touchListener = (v, event) -> {
            mIsWaitingUserLocation = false;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                IGeoPoint mapCenter = mMapController.getMap().getMapCenter();
                setAddressOnView(mapCenter);
                return true;
            }
            return false;
        };
        mMapController.getMap().setOnTouchListener(touchListener);
        //mPinView.setVisibility(action == null ? View.GONE: View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRestartIntent != null) {
            finish();
            startActivity(mRestartIntent);
            return;
        }

        if (isShowLocationSettings) {
            isShowLocationSettings = false;
            findUserAddress();
        }
    }

    @Override
    protected void onDestroy() {
        ActiveOrdersMonitoring.getInstance().removeListener(mOrderMonitoringListener);
        super.onDestroy();
    }

    private void updateAddresses() {
        mTempPoint = null;
        ll_addresses.removeAllViews();
        _Order order = _OrderRegistrator.me().getOrder();
        QueryList<_Point> points = order.getRoute().getPoints();

        for (int i = 0; i < points.size(); i++) {
            ll_addresses.addView(getAddressView(ll_addresses, points.get(i), i));
        }

        if (points.size() < 2)
            ll_addresses.addView(getAddressView(ll_addresses, null, points.size()));

        updateGetTaxiBtnText();
        updateOrderRoute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (shadowIsVisible()) {
                ll_options.performClick();
                return true;
            }
            if (mNavigationViewHelper.menuIsOpen()) {
                mNavigationViewHelper.closeMenu();
                return true;
            }

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
                return true;
            }

            showExitDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.exit, (dialogInterface, i) -> {
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    private boolean shadowIsVisible() {
        return fl_shadow != null
                && fl_shadow.getVisibility() == View.VISIBLE;
    }

    public void hideActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    private void updateGetTaxiBtnText() {
        _Order order = _OrderRegistrator.me().getOrder();
        int routePoints = order.getRoute().getPoints().size();
        @StringRes int textRes = routePoints <= 1 ? R.string.get_taxi : R.string.cost_calculation;
        btnGetTaxi.setText(textRes);
    }

    private void updateActionPanel() {
        updateTimeViews();
        updateSpecsViews();
        updateInfoViews();
    }

    private void updateTimeViews() {
        _Order order = _OrderRegistrator.me().getOrder();
        tv_time.setText(getOrderTime());
        ivTime.setVisibility(order.isRush() ? View.GONE : View.VISIBLE);
    }

    private void updateSpecsViews() {
        _ServiceCollection services = Collections.me().getServices();
        String checkedNames = services.getCheckedNames();
        tv_spec.setText(checkedNames);

        int checkedCount = services.getChecked().size();
        tvSpecsCount.setText(String.valueOf(checkedCount));
        llSpecImg.setVisibility(checkedCount <= 0 ? View.GONE : View.VISIBLE);
    }

    private void updateInfoViews() {
        _Order order = _OrderRegistrator.me().getOrder();
        String description = order.getDescription();
        tv_info.setText(description);
        ivInfo.setVisibility(description.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public String getOrderTime() {
        _Order order = _OrderRegistrator.me().getOrder();
        String dateTime = order.isRush()
                ? getString(R.string.now_time)
                : order.getDateTime().toString(DateTime.DATE_TIME);

        if (!order.isRush() && App.isExcludedApp)
            dateTime += " Uhr";
        return dateTime;
    }

    private void showDispatcherPhone() {
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

    private void showLocationErrorDialog() {
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
    public void onLocationChange(Location location) {
        mUserLocation = new LatLong(location.getLatitude(), location.getLongitude());
        saveUserLastGeopoint(mUserLocation);
        if (mIsWaitingUserLocation) {
            mIsWaitingUserLocation = false;
            showExampleAddressDialog();
            mMapController.zoomTo(mUserLocation, MapView.MAXIMUM_ZOOMLEVEL);
        }
    }

    /**
     * Сохраняет последнее известное местоположение пользователя.
     *
     * @param location точка с местоположением пользователя.
     */
    private void saveUserLastGeopoint(LatLong location) {
        String json = new Gson().toJson(new GeoPoint(location.Latitude, location.Longitude));
        Prefs.setValue(PrefsName.USER_LAST_GEOPOINT, json);
    }

    public View getAddressView(ViewGroup parentView, _Point address, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_input_address_new,
                parentView, false);

        TextView tv_position = (TextView) view.findViewById(R.id.tv_position);
        TextView tvAddress = (TextView) view.findViewById(R.id.tv_address);
        EditText etAddress = (EditText) view.findViewById(R.id.et_address);
        ImageButton ibtnAction = (ImageButton) view.findViewById(R.id.ibtn_action);

        boolean empty = (address == null || TextUtils.isEmpty(address.asShortAddress()));

        tv_position.setText(String.valueOf(ALPHABET.charAt(index)));

        tvAddress.setVisibility(empty ? View.GONE : View.VISIBLE);
        etAddress.setVisibility(empty ? View.VISIBLE : View.GONE);

        etAddress.setHint(R.string.move_map_or_click);

        if (empty) {
            mInputView = etAddress;
            mActionButton = ibtnAction;
            map_pin.setVisibility(View.VISIBLE);
            etAddress.setOnClickListener(view1 -> showInputAddressFragment());
            ibtnAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check));
            ibtnAction.setOnClickListener(view1 -> addOnRoute());
        } else {
            mInputView = null;
            mActionButton = null;
            map_pin.setVisibility(View.GONE);
            tvAddress.setText(address.asShortAddress());
            tvAddress.setOnClickListener(view1 -> MessageBox.show(this, address.asString(true)));
            ibtnAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pencil));
            ibtnAction.setOnClickListener(view1 -> {
                showEditOrderDialog(view1, index);
            });
        }
        return view;
    }

    private void showEditOrderDialog(View targetView, int index) {
        PopupMenu popupMenu = new PopupMenu(this, targetView);
        popupMenu.inflate(R.menu.menu_popup_address);
        if (index == 0) {
            popupMenu.getMenu().removeItem(R.id.menu_move_up_point);
        }
        int routePointCount = _OrderRegistrator.me().getOrder().getRoute().size();
        if (routePointCount == 1 || index != routePointCount - 1) {
            popupMenu.getMenu().removeItem(R.id.menu_add_route_point);
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_move_up_point: {
                    moveUpPoint(index);
                    return true;
                }
                case R.id.menu_remove_route_point: {
                    removeFromRoute(index);
                    return true;
                }
                case R.id.menu_add_route_point: {
                    ll_addresses.addView(getAddressView(ll_addresses, null, (index + 1)));
                    return true;
                }
                case R.id.menu_edit_route_point: {
                    _Point point = _OrderRegistrator.me().getOrder().getRoute().getByIndex(index);
                    showInputAddressFragment(point, index);
                    return true;
                }
            }
            return true;
        });
        popupMenu.show();
    }

    private void moveUpPoint(int index) {
        if (index == 0) return;

        _Order order = _OrderRegistrator.me().getOrder();

        _Point point = order.getRoute().getByIndex(index);
        order.getRoute().getPoints().remove(index);
        order.getRoute().addByIndex((index - 1), point);

        updateAddresses();
    }

    private void removeFromRoute(int index) {
        _Order order = _OrderRegistrator.me().getOrder();
        order.getRoute().getPoints().remove(index);
        updateAddresses();
    }

    private void showInputAddressFragment(_Point point, int index) {
        InputAddressPanelFragment fragment = new InputAddressPanelFragment();
        fragment.setSaveRouteListener(this::updateAddresses);
        if (index >= 0)
            fragment.setAddressPointWithIndex(point, index);
        else
            fragment.setAddressPoint(mTempPoint);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_fullscreen_container, fragment)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

    }

    private void showInputAddressFragment() {
        mIsWaitingUserLocation = false;
        showInputAddressFragment(mTempPoint, -1);
    }

    private void addOnRoute() {
        if (mTempPoint == null) return;
        _Order order = _OrderRegistrator.me().getOrder();
        order.getRoute().addPoint(mTempPoint.copy());
        Prefs.setValue(PrefsName.LAST_CITY, mTempPoint.getCityName());
        updateAddresses();
    }


    public void setAddressOnView(IGeoPoint geoPoint) {
        if (mLocationGeocodeTask != null && !mLocationGeocodeTask.isCancelled()) {
            mLocationGeocodeTask.cancel(true);
        }
        if (mInputView == null) return;
        mLocationGeocodeTask = new LocationGeocodeTask(this, param1 -> {
            mTempPoint = param1;
            if (mInputView != null) {
                mInputView.setText(mTempPoint.asShortAddress());
                mInputView.setSelection(mInputView.length());
            }

            if (mActionButton != null) {
                mActionButton.setEnabled(true);
            }
        });
        mLocationGeocodeTask.execute(new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude()));
        mActionButton.setEnabled(false);
    }

    private void updateOrderRoute() {
        mMapController.clearAllOverlays();
        ll_road_info.setVisibility(View.GONE);
        QueryList<_Point> points = _OrderRegistrator.me().getOrder().getRoute().getPoints();
        if (points.isEmpty())
            return;

        int size = 1;
        for (_Point sediAddress : points) {
            try {
                if (!sediAddress.getLatLong().isValid()) continue;

                String resDrawable = String.format("%s%s", "ic_map_", size <= MAP_ICON_COUNT ? String.valueOf(size) : "other");
                addPointOnMap(sediAddress.getGeoPoint().toLatLong(),
                        sediAddress.asString(),
                        getResources().getIdentifier(resDrawable, "drawable", getPackageName()));
                size++;
            } catch (Exception e) {
                LogUtil.log(e);
            }
        }
        buildRouteRoad();
    }

    private ItemizedIconOverlay addPointOnMap(LatLong loc, String msg, int icon) {
        return mMapController.addPoint(loc, icon, () -> {
            if (loc.equals(mUserLocation)) {
                //showMyLocationChooisePointDialog();
            } else
                MessageBox.show(this, String.format(getString(R.string.msg_YouAddressIs_RemoveQuestion), msg), null, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        _Order order = _OrderRegistrator.me().getOrder();
                        _Point sediAddress = order.getRoute().getPoints().FirstOrDefault(item -> item.getLatLong().equals(loc));
                        if (sediAddress == null) return;
                        order.getRoute().remove(sediAddress);
                        updateAddresses();
                    }
                }, true, new int[]{R.string.yes, R.string.no});
        });
    }

    private void buildRouteRoad() {
        try {
            QueryList<_Point> points = _OrderRegistrator.me().getOrder().getRoute().getPoints();
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            AsyncAction.run(() -> {
                for (_Point sediAddress : points) {
                    if (!sediAddress.getLatLong().isValid())
                        continue;
                    waypoints.add(sediAddress.getGeoPoint().toLatLong().toGeopoint());
                }

                if (waypoints.size() < 2)
                    return null;

                Road road = mRoadManager.getRoad(waypoints);
                if (road.mStatus != Road.STATUS_OK || road.mLength == 0) {
                    LogUtil.log(LogUtil.ERROR, "Road Manager: Постороение маршрута, статус BAD!");
                    return null;
                }

                return road;
            }, new IActionFeedback<Road>() {
                @Override
                public void onResponse(Road road) {
                    if (mMapRouteOverlay != null)
                        mMapController.remove(mMapRouteOverlay);

                    mMapRouteOverlay = RoadManager.buildRoadOverlay(road, MainActivity2.this);
                    mMapRouteOverlay.setWidth(10);
                    mMapRouteOverlay.setColor(ContextCompat.getColor(MainActivity2.this,
                            R.color.primaryColor));

                    mMapController.addOverlay(0, mMapRouteOverlay);
                    centredPolyline(mMapRouteOverlay.getPoints());

                    displayRoadInfo(road);
                }

                @Override
                public void onFailure(Exception e) {
                    centredPolyline(waypoints);
                    displayRoadInfo(null);
                    LogUtil.log(e);
                }
            });
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
    }

    public void displayRoadInfo(Road routeInfo) {
        if (routeInfo != null) {
            tv_road_distance.setText(getString(R.string.road_distance_format, routeInfo.mLength));
            tv_road_time.setText(getString(R.string.road_duration_format, (routeInfo.mDuration / 60)));
        }
        ll_road_info.setVisibility(routeInfo != null ? View.VISIBLE : View.GONE);
    }

    private void centredPolyline(List<GeoPoint> points) {
        if (points.isEmpty()) return;
        if (points.size() == 1) {
            GeoPoint geoPoint = points.get(0);
            mMapController.zoomTo(new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude()), 18);
            return;
        }

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = Integer.MIN_VALUE;

        for (GeoPoint point : points) {
            if (point.getLatitude() < minLat)
                minLat = point.getLatitude();
            if (point.getLatitude() > maxLat)
                maxLat = point.getLatitude();
            if (point.getLongitude() < minLong)
                minLong = point.getLongitude();
            if (point.getLongitude() > maxLong)
                maxLong = point.getLongitude();
        }

        BoundingBox boundingBox = new BoundingBox(maxLat, maxLong, minLat, minLong);
        mMapController.zoomToBoundingBox(boundingBox.increaseByScale(1.3f));
    }

    private void setOptionsPanelVisible(boolean visible) {
        llOptionsPanel.setVisibility(!visible ? View.GONE : View.VISIBLE);
        fl_shadow.setVisibility(!visible ? View.GONE : View.VISIBLE);
    }


    //<editor-fold desc="OnClicks">
    @OnClick({R.id.fab_find_me})
    @SuppressWarnings(Const.UNUSED)
    public void onFindMeBtnClick() {
        LatLong location = LocationService.me().getLocation();
        if (!location.isValid()) {
            showLocationErrorDialog();
            return;
        }
        if (mInputView != null) {
            setAddressOnView(new GeoPoint(location.Latitude, location.Longitude));
        }
        mMapController.moveTo(location);
    }

    @OnClick({R.id.ibtn_call_dispatcher})
    @SuppressWarnings(Const.UNUSED)
    public void onCallDispatcherBtnClick() {
        if (App.isExcludedApp) {
            startActivity(AboutAppActivity.getIntent(this));
        } else {
            showDispatcherPhone();
        }
    }

    @OnClick({R.id.ll_options, R.id.fl_shadow})
    @SuppressWarnings(Const.UNUSED)
    public void onOptionsBtnClick() {
        boolean isVisible = llOptionsPanel.getVisibility() == View.VISIBLE;
        setOptionsPanelVisible(!isVisible);
    }

    @OnClick(R.id.ll_time)
    @SuppressWarnings(Const.UNUSED)
    public void onTimeBtnClick() {
        DateTimeDialog dialog = new DateTimeDialog();
        dialog.setDismissListener(this::updateTimeViews);
        dialog.show(getSupportFragmentManager(), null);
    }

    @OnClick(R.id.ll_spec)
    @SuppressWarnings(Const.UNUSED)
    public void onSpecsBtnClick() {
        SpecsDialog dialog = new SpecsDialog();
        dialog.setDismissListener(this::updateSpecsViews);
        dialog.show(getSupportFragmentManager(), null);
    }

    @OnClick(R.id.ll_info)
    @SuppressWarnings(Const.UNUSED)
    public void onInfoBtnClick() {
        AdditionalInfoDialog dialog = new AdditionalInfoDialog();
        dialog.setDismissListener(this::updateInfoViews);
        dialog.show(getSupportFragmentManager(), null);
    }

    @OnClick(R.id.ibtn_menu)
    @SuppressWarnings(Const.UNUSED)
    public void onMenuBtnClick() {
        if (mNavigationViewHelper.menuIsOpen()) {
            mNavigationViewHelper.closeMenu();
        } else {
            mNavigationViewHelper.openMenu();
        }
    }

    @OnClick(R.id.fab_share)
    @SuppressWarnings(Const.UNUSED)
    public void onShareBtnClick() {
        if (!App.isAuth) {
            showAuthorizationDialog();
        } else {
            startActivity(PartnerProgramActivity.getIntent(this));
        }
    }

    private void showAuthorizationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.RegistrationMessage)
                .setPositiveButton(R.string.autorithation, (dialogInterface, i) -> {
                    startActivity(UserPhoneRegisterActivity.getIntent(this));
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    @OnClick(R.id.btn_get_taxi)
    @SuppressWarnings(Const.UNUSED)
    public void onGetTaxiBtnClick() {
        setOptionsPanelVisible(false);
        _Order order = _OrderRegistrator.me().getOrder();
        QueryList<_Point> points = order.getRoute().getPoints();
        if (points.isEmpty()) {
            MessageBox.show(this, R.string.IncorrectRoute);
            return;
        }
        _OrderRegistrator.me().calculateAndShow(this);
    }

//</editor-fold>
}
