package ru.sedi.customerclient.activitys.route;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.IntDef;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.GeoLocation.SediOsmrManager;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.OsmMapController;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.ILocationChangeListener;
import ru.sedi.customerclient.tasks.AddressDetailTask;
import ru.sedi.customerclient.tasks.AutocompleteTask;


public class MapInputAddressActivity extends BaseActivity implements ILocationChangeListener {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.llSearchPanel) LinearLayout llSearchPanel;
    @BindView(R.id.tvTime) TextView tvTime;
    @BindView(R.id.tvDistance) TextView tvDistance;
    @BindView(R.id.acAddress) AutoCompleteTextView acAddress;
    @BindView(R.id.zoomControls) ZoomControls zoomControls;
    @BindView(R.id.fabRemoveRoute) FloatingActionButton fabRemoveRoute;
    @BindView(R.id.fabShowSearchPanel) FloatingActionButton fabShowSearchPanel;
    @BindView(R.id.fabFindMe) FloatingActionButton fabFindMe;
    @BindView(R.id.fabSuccess) FloatingActionButton fabSuccess;
    @BindView(R.id.fabVoiceInput) FloatingActionButton fabVoiceInput;

    private OsmMapController mMapController;
    private SediOsmrManager mRoadManager;
    private LatLong mUserLocation = LocationService.DEFAULT_LOCATION;
    private Overlay mTapOverlay;
    private final int MAP_ICON_COUNT = 10;
    private QueryList<_Point> mSediAddresses = new QueryList<>();
    private _Point mTempSediAddress;
    private ItemizedIconOverlay<OverlayItem> searchAddressOverlay;
    private AutocompleteTask mAutoCompliteTask;
    private AddressDetailTask mAddressDetailTask;
    private _Order mOrder;
    private ClickListener mClickListiner = new ClickListener();
    private boolean mAddressChangeFlag;
    private org.osmdroid.views.overlay.Polyline mMapRouteOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_map_input_address);

        ButterKnife.bind(this);

        LocationService.me().startListener();
        mUserLocation = LocationService.me().getLocation();
        if (!mUserLocation.isValid())
            LocationService.me().subscribe(this);

        mOrder = _OrderRegistrator.me().getOrder();
        mMapController = new OsmMapController(this, mapView);

        initUiElements();
        reconstructMap();
    }

    @NonNull
    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (before > count || acAddress.isPerformingCompletion()) {
                    acAddress.dismissDropDown();
                    return;
                }

                mAddressChangeFlag = true;
                mTempSediAddress = null;

                if (mAutoCompliteTask != null && !mAutoCompliteTask.isCancelled())
                    mAutoCompliteTask.cancel(true);

                mAutoCompliteTask = new AutocompleteTask(MapInputAddressActivity.this, acAddress, mSediAddresses);
                mAutoCompliteTask.execute(new Pair<>(s.toString(), mUserLocation));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private void onAutocompliteSuccess(List<_Point> points) {
        mSediAddresses = new QueryList<>(points);

        QueryList<String> s = new QueryList<>();
        for (_Point object : mSediAddresses)
            s.add(object.getDesc() + " ");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                R.layout.list_yandex_address, R.id.tvYandexAddress, s);
        acAddress.setAdapter(adapter);
        acAddress.showDropDown();
    }

    public void updateTitle() {
        int size = mOrder.getRoute().size();
        int titleRes = R.string.AsGiving;
        if (size >= 1)
            titleRes = R.string.route;
        super.updateTitle(titleRes, R.drawable.ic_google_maps);
    }

    /**
     * Инициализация Ui элементов
     */
    private void initUiElements() {
        fabFindMe.setOnClickListener(mClickListiner);

        fabVoiceInput.setOnClickListener(mClickListiner);
        fabSuccess.setOnClickListener(mClickListiner);
        fabRemoveRoute.setOnClickListener(mClickListiner);
        fabShowSearchPanel.setOnClickListener(mClickListiner);

        zoomControls.setIsZoomInEnabled(true);
        zoomControls.setIsZoomOutEnabled(true);
        zoomControls.setOnZoomInClickListener(v -> mMapController.zoomIn());
        zoomControls.setOnZoomOutClickListener(v -> mMapController.zoomOut());

        acAddress.addTextChangedListener(getTextWatcher());
        acAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                checkAndSaveAddress();
            }
            return false;
        });
        acAddress.setOnItemClickListener((parent, view, position, id) -> {
            if (mAddressDetailTask!=null && !mAddressDetailTask.isCancelled())
                mAddressDetailTask.cancel(true);
            mAddressDetailTask = new AddressDetailTask(point -> setAddressDetailResponse(acAddress, point));
            mAddressDetailTask.execute(mSediAddresses.tryGet(position));
        });


        //Инициализируем карту и ставим настройки
        mMapController.getMap().setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                Prefs.setValue(PrefsName.MAP_ZOOM_LEVEL, zoomEvent.getZoomLevel());
                return false;
            }
        });

        //Создаем менеджер прокладки маршрута
        mRoadManager = new SediOsmrManager(this, false);
        mMapController.clearAllOverlays();

        mTapOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                addNewAddressOnMap(geoPoint);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                addNewAddressOnMap(geoPoint);
                return false;
            }
        });
    }

    private void setAddressDetailResponse(AutoCompleteTextView field, _Point point) {
        mTempSediAddress = point;
        Prefs.setValue(PrefsName.LAST_CITY, mTempSediAddress.getCityName());
        field.setText(mTempSediAddress.asString() + " ", false);
        field.setSelection(acAddress.getText().length());
        addSearchingPointOnMap(mTempSediAddress.getGeoPoint().toLatLong(), mTempSediAddress.asString());
        mMapController.zoomTo(mTempSediAddress.getGeoPoint().toLatLong(), Prefs.getInt(PrefsName.MAP_ZOOM_LEVEL));
    }

    private void checkAndSaveAddress() {
        String address = acAddress.getText().toString().trim();
        if (mTempSediAddress != null && address.equalsIgnoreCase(mTempSediAddress.asString())) {
            showAddressSaveDialog(mTempSediAddress.asString());
            return;
        } else {
            if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
                mAddressDetailTask.cancel(true);
            mAddressDetailTask = new AddressDetailTask(point -> setAddressDetailResponse(acAddress, point));
            mAddressDetailTask.execute(mTempSediAddress);
        }
    }

    /**
     * Видимость панели для поиска
     */
    private void showSearchPanel(boolean visible) {
        if (visible) {
            acAddress.requestFocus();
            acAddress.setText(Prefs.getString(PrefsName.LAST_CITY) + ", ", false);
            acAddress.setSelection(acAddress.getText().length());
        }
        if (!visible) {
            mAddressChangeFlag = false;
            acAddress.setText(Const.EmptyStr);
            Helpers.hideKeyboard(this.getCurrentFocus());
        }
        llSearchPanel.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Добавляет новый адрес на карту, в место нажатия на карте
     */
    private void addNewAddressOnMap(GeoPoint geoPoint) {
        LatLong latLong = new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude());
        checkAddressInGeoService(latLong, Const.NoId);
        LogUtil.log(LogUtil.INFO, "На карте нажали на координату: LAT: %s; LON: %s;", latLong.Latitude, latLong.Longitude);
    }

    /**
     * Полное перестроение карты и маршрута
     */
    private void reconstructMap() {
        clearRouteInfo();
        mMapController.clearAllOverlays();
        mMapController.addOverlay(mTapOverlay);
        addCustomerOnMap();
        updateOrderRoute();
        updateTitle();
    }

    /**
     * Диалог полного удаления маршрута
     */
    private void showRemoveRouteDialog() {
        MessageBox.show(MapInputAddressActivity.this, getString(R.string.msg_RemoveRouteQuestion), null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
                mOrder.getRoute().clearPoints();
                reconstructMap();
            }
        }, true, new int[]{R.string.yes, R.string.no});
    }

    /**
     * Обновляет маршрут по текущему заказу
     */
    private void updateOrderRoute() {
        QueryList<_Point> points = mOrder.getRoute().getPoints();
        if (points.isEmpty()){
            if(mUserLocation.isValid())
                mMapController.zoomTo(mUserLocation, 18);
            return;
        }

        int size = 1;
        for (_Point sediAddress : points) {
            try {
                if (!sediAddress.getChecked())
                    continue;

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

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        int visibility = llSearchPanel.getVisibility();
        if (keyCode == KeyEvent.KEYCODE_BACK && visibility == View.VISIBLE) {
            llSearchPanel.setVisibility(View.GONE);
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Добавляет пользователя на карту
     */
    private void addCustomerOnMap() {
        if (mUserLocation == null || !mUserLocation.isValid()) return;
        addPointOnMap(mUserLocation, getString(R.string.IamHere), R.drawable.ic_my_loc);
        //mMapController.zoomTo(mUserLocation, 18);
        //mMapController.addPoint(mUserLocation, R.drawable.ic_my_loc, () -> showMyLocationChooisePointDialog());
    }

    /**
     * Добавляет новый элемент на карту
     *
     * @param loc  - гео-позиция
     * @param msg  - сообщение при вызове MessageBox
     * @param icon - иконка
     */
    private void addPointOnMap(final LatLong loc, final String msg, int icon) {
        mMapController.addPoint(loc, icon, () -> {
            if (loc.equals(mUserLocation)) {
                showMyLocationChooisePointDialog();
            } else
                MessageBox.show(MapInputAddressActivity.this, String.format(getString(R.string.msg_YouAddressIs_RemoveQuestion), msg), null, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        _Point sediAddress = mOrder.getRoute().getPoints().FirstOrDefault(item -> item.getLatLong().equals(loc));
                        if (sediAddress == null) return;
                        mOrder.getRoute().remove(sediAddress);
                        reconstructMap();
                    }
                }, true, new int[]{R.string.yes, R.string.no});
        });
    }

    /**
     * Добавляет на карту точку после поиска в гео-кодере
     *
     * @param loc - гео-позиция
     * @param msg - сообщение при вызове MessageBox
     */
    private void addSearchingPointOnMap(final LatLong loc, final String msg) {

        mMapController.remove(searchAddressOverlay);
        OverlayItem overlayItem = new OverlayItem("", "", loc.toGeopoint());
        overlayItem.setMarker(ContextCompat.getDrawable(this, R.drawable.ic_map_find));
        List<OverlayItem> items = new QueryList<>();
        items.add(overlayItem);
        searchAddressOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                showAddressSaveDialog(msg);
                return true;
            }

            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return true;
            }
        }, getBaseContext());
        mMapController.addOverlay(searchAddressOverlay);
    }

    private void showAddressSaveDialog(String msg) {
        new AlertDialog.Builder(MapInputAddressActivity.this)
                .setMessage(String.format(getString(R.string.msg_AddPointAs_Question), msg))
                .setPositiveButton(R.string.AsStart, (dialog, which) -> {
                    mOrder.getRoute().addByIndex(0, mTempSediAddress.copy());
                    showSearchPanel(false);
                    reconstructMap();
                })
                .setNegativeButton(R.string.AsEnd, (dialog, which) -> {
                    mOrder.getRoute().addPoint(mTempSediAddress.copy());
                    showSearchPanel(false);
                    reconstructMap();
                })
                .setNeutralButton(R.string.close, (dialog, which) -> dialog.dismiss()).create().show();
    }

    /**
     * Диалог события на клик на человечке
     */
    private void showMyLocationChooisePointDialog() {
        new AlertDialog.Builder(this).
                setTitle(null).setMessage(R.string.msg_IamHere).setPositiveButton(R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        }).setNegativeButton(R.string.AsGiving, (dialog, which) -> {
            checkAddressInGeoService(new LatLong(mUserLocation.Latitude, mUserLocation.Longitude), 0);
        }).setNeutralButton(R.string.AsRoute, (dialog, which) -> {
            int index = mOrder.getRoute().size() - 1;
            checkAddressInGeoService(new LatLong(mUserLocation.Latitude, mUserLocation.Longitude), index);
        }).create().show();
    }

    /**
     * Построения линии маршрута
     */
    private void buildRouteRoad() {
        try {
            clearRouteInfo();
            QueryList<_Point> points = mOrder.getRoute().getPoints();
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            AsyncAction.run(() -> {
                for (_Point sediAddress : points) {
                    if (!sediAddress.getChecked())
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

                if (mMapRouteOverlay != null)
                    mMapController.remove(mMapRouteOverlay);

                mMapRouteOverlay = RoadManager.buildRoadOverlay(road, MapInputAddressActivity.this);
                mMapRouteOverlay.setWidth(10);
                mMapRouteOverlay.setColor(ContextCompat.getColor(MapInputAddressActivity.this, R.color.primaryColor));
                return mMapRouteOverlay;
            }, new IActionFeedback<Overlay>() {
                @Override
                public void onResponse(Overlay result) {
                    mMapController.addOverlay(result);
                    centredPolyline(((Polyline) result).getPoints());
                }

                @Override
                public void onFailure(Exception e) {
                    centredPolyline(waypoints);
                    LogUtil.log(e);
                }
            });
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
    }

    private void centredPolyline(List<GeoPoint> points) {
        if (points.isEmpty()) return;
        if (points.size() < 3) {
            GeoPoint geoPoint = points.get(points.size() - 1);
            mMapController.zoomTo(new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude()), 18);
            return;
        }

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLong = Double.MAX_VALUE;
        double maxLong = Double.MIN_VALUE;

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

    /**
     * Очищает ифнормацию о маршруте
     */
    private void clearRouteInfo() {
        tvDistance.setText("");
        tvTime.setText("");
    }

    /**
     * Проверка адреса введеного пользователем
     */
    private void checkAddressInGeoService(final LatLong latLong, final int index) {
        final SweetAlertDialog pd = ProgressDialogHelper.show(this, getString(R.string.CheckedAddress));

        AsyncAction.run(() -> {
            QueryList<_Point> addressListByLocationPoint = LocationService.me().getAddressListByLocationPoint(MapInputAddressActivity.this, latLong.toString());
            return addressListByLocationPoint;
        }, new IActionFeedback<QueryList<_Point>>() {
            @Override
            public void onResponse(QueryList<_Point> result) {
                if (pd != null)
                    pd.dismiss();

                if (result.isEmpty()) {
                    MessageBox.show(MapInputAddressActivity.this, getString(R.string.msg_GeoServiceReturnNull), null);
                    return;
                }
                _Point point = result.tryGet(0);
                result.add(new _Point(point.getCityName(), latLong, true));
                showChooseAddressDialog(result, index);
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();
                MessageBox.show(MapInputAddressActivity.this, e.getMessage(), null);
            }
        });
    }

    /**
     * Отображение диалога для выбора адреса которые вернул Яндекс
     */
    private void showChooseAddressDialog(final QueryList<_Point> sediAddresses, final int index) {
        QueryList<String> stringAddress = new QueryList<>();
        for (_Point a : sediAddresses) {
            if (a.asString().trim().length() < 1)
                continue;
            stringAddress.add(a.asString(true) + " ");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(MapInputAddressActivity.this, R.layout.list_yandex_address, R.id.tvYandexAddress, stringAddress);

        AlertDialog.Builder addressDialog = new AlertDialog.Builder(this);
        addressDialog.setTitle(getString(R.string.select_address))
                .setAdapter(adapter, (dialogInterface, i) -> {
                    final _Point a = sediAddresses.get(i);
                    LogUtil.log(LogUtil.INFO, "Пользователь выбрал адрес: " + a.asString());
                    mOrder.getRoute().setByIndex(index, a);
                    //Collections.me().getAddressHistory().add(a.copy());
                    int size = mOrder.getRoute().size();
                    String resDrawable = String.format("%s%s", "ic_map_", size <= MAP_ICON_COUNT ? String.valueOf(size) : "other");
                    addPointOnMap(a.getGeoPoint().toLatLong(), a.asString(), getResources().getIdentifier(resDrawable, "drawable", getPackageName()));
                    Prefs.setValue(PrefsName.LAST_CITY, a.getCityName());
                    buildRouteRoad();
                }).create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {
                    try {
                        final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        acAddress.setText((text.get(0)));
                    } catch (Exception e) {
                        BaseActivity.Instance.showDebugMessage(44, e);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onLocationChange(Location location) {
        if (mMapController == null) return;
        if (location != null && (mUserLocation == null || !mUserLocation.isValid())) {
            mUserLocation = new LatLong(location.getLatitude(), location.getLongitude());
            reconstructMap();
            mMapController.zoomTo(mUserLocation, mMapController.getMap().getMaxZoomLevel());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationService.me().stopListener(this);
        _OrderRegistrator.me().calculate(this);
    }

    public class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fabFindMe: {
                    if (LocationService.me().onceProviderEnabled()) {
                        mUserLocation = LocationService.me().getLocation();
                        mMapController.zoomTo(mUserLocation, mMapController.getMap().getMaxZoomLevel());
                        reconstructMap();
                    } else {
                        MessageBox.show(MapInputAddressActivity.this,
                                getString(R.string.msg_LocationSettingsOff), null,
                                new UserChoiseListener() {
                                    @Override
                                    public void OnOkClick() {
                                        super.OnOkClick();
                                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    }
                                }, true);
                    }
                    break;
                }
                case R.id.fabVoiceInput: {
                    SpeechRecognitionHelper.run(MapInputAddressActivity.this);
                    break;
                }
                case R.id.fabSuccess: {
                    finish();
                    break;
                }
                case R.id.fabRemoveRoute: {
                    showRemoveRouteDialog();
                    break;
                }
                case R.id.fabShowSearchPanel: {
                    boolean visibleNow = llSearchPanel.getVisibility() == View.VISIBLE;
                    if (mAddressChangeFlag) {
                        new AlertDialog.Builder(MapInputAddressActivity.this)
                                .setMessage(R.string.unsave_point_message)
                                .setPositiveButton(R.string.yes, (dialog, which) -> checkAndSaveAddress())
                                .setNegativeButton(R.string.no, (dialog, which) -> showSearchPanel(!visibleNow))
                                .create().show();
                    } else
                        showSearchPanel(!visibleNow);
                    break;
                }
                default:
                    break;
            }
        }
    }
}