package ru.sedi.customerclient.activitys.exemplary_location;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.activitys.main.MainActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.GeoLocation.Nominatium.NominatimGeocoder;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.OsmMapController;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.ILocationChangeListener;


public class ExemplaryLocationActivity extends BaseActivity implements ILocationChangeListener {

    private LatLong mPoint = null;
    private _Point mSediAddress = null;
    private MapView mMapView;
    private EditText etAddress;
    private OsmMapController mMapController;
    private EventHandler mEventHandler = new EventHandler();
    private SweetAlertDialog pd;
    private double mLastAccuracy = -1;
    private NominatimGeocoder mGeocoder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_exempl_location);
        updateTitle(R.string.giving_address, R.drawable.ic_map_marker_radius);

        LocationService.me().startListener(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocationService.me().onceProviderEnabled()) {
            showLocationSettingError();
            return;
        }

        updateAllView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationService.me().stopListener(this);
    }

    private void updateAllView() {
        if (mPoint == null && LocationService.me().onceProviderEnabled()) {
            pd = ProgressDialogHelper.show(this, getString(R.string.msg_SearchLocation), true, (dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    finish();
                    return true;
                }
                return false;
            });
        } else
            updateLocationView(false, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapController = new OsmMapController(this, mMapView);
        mMapController.addOverlay(getTapOverlay());

        etAddress = (EditText) findViewById(R.id.etAddress);
        etAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                save();
                return true;
            }
            return false;
        });
        etAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (mPoint!=null)
                LocationService.me().unsubscribe(this);
        });

        ((CheckBox) findViewById(R.id.cbDontUse)).setOnCheckedChangeListener(mEventHandler);

        findViewById(R.id.btnSuccess).setOnClickListener(mEventHandler);
        findViewById(R.id.btnClose).setOnClickListener(mEventHandler);
        findViewById(R.id.fabFindMe).setOnClickListener(mEventHandler);
    }

    private Overlay getTapOverlay() {
        return new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                mPoint = new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude());
                updateLocationView(true, true);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                return false;
            }
        });
    }

    private void save() {
        if (mSediAddress == null)
            return;

        if (mSediAddress.equalsString(etAddress.getText().toString())) {
            _OrderRegistrator.me().getOrder().getRoute().addPoint(mSediAddress);
            Prefs.setValue(PrefsName.LAST_CITY, mSediAddress.getCityName());
            finish();
        } else {
            checkAddressInGeoService();
        }
    }

    /**
     * Уведомление о недоступном GPS.
     */
    private void showLocationSettingError() {
        MessageBox.show(this,
                getString(R.string.msg_LocationSettingError),
                null,
                new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(viewIntent);
                    }

                    @Override
                    public void onCancelClick() {
                        finish();
                    }
                },
                true, new int[]{R.string.yes, R.string.no});
    }

    /**
     * Добавляем заказчика на карту.
     */
    private void addCustomerOnMap() {
        if (mPoint == null || !mPoint.isValid()) {
            return;
        }
        mMapController.clearAllOverlays();
        mMapController.addOverlay(getTapOverlay());
        mMapController.addPoint(mPoint, R.drawable.ic_my_loc, null);
        mMapController.zoomTo(mPoint, 18);
    }

    /**
     * Проверка адреса введеного пользователем
     */
    private void checkAddressInGeoService() {
        final String address = etAddress.getText().toString();
        if (TextUtils.isEmpty(address)) {
            MessageBox.show(this, R.string.need_first_address_error_message, -1);
            return;
        }
        final SweetAlertDialog pd = ProgressDialogHelper.show(this, getString(R.string.check_address_action));
        AsyncAction.run(() -> LocationService.me().getAddressListByLocationPoint(ExemplaryLocationActivity.this,
                address), new IActionFeedback<QueryList<_Point>>() {
            @Override
            public void onResponse(QueryList<_Point> result) {
                if (pd != null)
                    pd.dismiss();

                if (result.isEmpty()) {
                    MessageBox.show(ExemplaryLocationActivity.this, getString(R.string.bad_geo_response_message), null);
                    return;
                }
                showChooseAddressDialog(result);
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();
                MessageBox.show(ExemplaryLocationActivity.this, e.getMessage(), null);
            }
        });
    }

    /**
     * Отображение диалога для выбора адреса которые вернул Яндекс
     */
    private void showChooseAddressDialog(final QueryList<_Point> sediAddresses) {
        QueryList<String> stringAddress = new QueryList<>();
        for (_Point a : sediAddresses)
            stringAddress.add(a.asString(true) + " ");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(ExemplaryLocationActivity.this, R.layout.list_yandex_address, R.id.tvYandexAddress, stringAddress);

        AlertDialog.Builder addressDialog = new AlertDialog.Builder(this);
        addressDialog.setTitle(getString(R.string.select_address))
                .setAdapter(adapter, (dialogInterface, i) -> {
                    _Point point = sediAddresses.tryGet(i);
                    if (point == null)
                        return;

                    mSediAddress = new _Point(point);
                    mPoint = mSediAddress.getGeoPoint().toLatLong();
                    etAddress.setText(mSediAddress.asString(true));
                    etAddress.setSelection(etAddress.getText().length());
                    addCustomerOnMap();
                }).create().show();
    }

    private void updateLocationView(boolean withProgress, boolean isTap) {
        if (!LocationService.me().onceProviderEnabled() || mGeocoder != null)
            return;

        mGeocoder = new NominatimGeocoder(ExemplaryLocationActivity.this, Prefs.getString(PrefsName.LOCALE_CODE));
        mGeocoder.asyncSearch(mPoint.toString(), withProgress, param -> AsyncAction.runInMainThread(() -> {
            mGeocoder = null;
            if (param == null || param.isEmpty())
                return;
            _Point point = param.tryGet(0);
            if (point == null)
                return;
            if (isTap) {
                param.add(new _Point(point.getCityName(), mPoint, true));
                showChooseAddressDialog(param);
            } else {
                mSediAddress = new _Point(point);
                mPoint = mSediAddress.getGeoPoint().toLatLong();

                etAddress.setText(mSediAddress.asString(true));
                etAddress.setSelection(etAddress.getText().length());
                addCustomerOnMap();
            }
        }));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void finish() {
        if (isTaskRoot())
            startNewActivity(MainActivity.class, null);
        super.finish();
    }

    @Override
    public void onLocationChange(Location location) {
        if (location == null || location.getLatitude() <= 0.0)
            return;

        if (pd != null)
            pd.dismiss();

        if (mLastAccuracy < 0)
            mLastAccuracy = location.getAccuracy();

        if (location.getAccuracy() > mLastAccuracy) return;

        mLastAccuracy = location.getAccuracy();
        mPoint = new LatLong(location.getLatitude(), location.getLongitude());
        updateLocationView(mSediAddress == null, false);
    }

    private class EventHandler implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.cbDontUse)
                Prefs.setValue(PrefsName.ENABLED_SET_DEFAULT_ADDRESS, !isChecked);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibtnSearch:
                    checkAddressInGeoService();
                    break;
                case R.id.btnSuccess:
                    save();
                    break;
                case R.id.btnClose:
                    finish();
                    break;
                case R.id.fabFindMe:
                    mPoint = LocationService.me().getLocation();
                    updateAllView();
                    break;
                default:
                    break;
            }
        }
    }
}