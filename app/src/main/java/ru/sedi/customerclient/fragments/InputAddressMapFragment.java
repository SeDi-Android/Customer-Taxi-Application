package ru.sedi.customerclient.fragments;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kg.ram.asyncjob.IOnSuccessListener;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.GeoLocation.SediOsmrManager;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.OsmMapController;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.GeoTools.GeoTools;
import ru.sedi.customerclient.common.GeoTools.Units;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.dialogs.DriverInfoDialog;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.IAction;
import ru.sedi.customerclient.interfaces.ILocationChangeListener;
import ru.sedi.customerclient.interfaces.OnOrderChangeListener;


public class InputAddressMapFragment extends Fragment implements ILocationChangeListener {

    private static final int MAP_ICON_COUNT = 10;
    public static final int LAYOUT = R.layout.fragment_input_address_map;

    private OsmMapController mMapController;
    private Overlay mTapOverlay;
    private Polyline mMapRouteOverlay;
    private SediOsmrManager mRoadManager;
    private _Order mOrder;
    private LatLong mUserLocation = LocationService.DEFAULT_LOCATION;
    private SweetAlertDialog mDialog;
    private OnOrderChangeListener mListener;
    private ItemizedIconOverlay mCustomerOverlay;
    private boolean mIsFirstRun = true;
    private _Order mMonitoringOrder;
    private ImageView mPinView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT, container, false);
        LocationService.with(getContext()).startListener(this);
        init(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        if (activity instanceof OnOrderChangeListener)
            mListener = (OnOrderChangeListener) activity;
    }

    private void init(View view) {
        mPinView = (ImageView) view.findViewById(R.id.iv_map_pin);
        MapView mapView = (MapView) view.findViewById(R.id.mapView);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabFindMe);
        fab.setOnClickListener(v -> {
            if (mUserLocation.isValid()) {
                addCustomerOnMap();
                mMapController.zoomTo(mUserLocation, 18);
            } else {
                if (mListener != null)
                    mListener.showLocationErrorDialog();
            }
        });

        mOrder = _OrderRegistrator.me().getOrder();
        mMapController = new OsmMapController(getContext(), mapView);
        mRoadManager = new SediOsmrManager(getContext(), false);
        mTapOverlay = getTapOverlay();
        mUserLocation = LocationService.me().getLocation();
        reconstructMap();
    }


    public void findUserAddress() {
        if (!mUserLocation.isValid()) {
            if (LocationService.me().onceProviderEnabled())
                mDialog = ProgressDialogHelper.show(getContext(), R.string.msg_SearchLocation);
            else {
                if (mListener != null)
                    mListener.showLocationErrorDialog();
            }
        } else {
            mMapController.zoomTo(mUserLocation, 18);
            showExampleAddressDialog();
        }
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

            _Point finalPoint = point;
            if (!point.isCoordinatesonly()) {
                new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.approximate_location_format, point.asString(true)))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            mOrder.getRoute().getPoints().add(finalPoint.copy());
                            Prefs.setValue(PrefsName.LAST_CITY, finalPoint.getCityName());
                            updateAllView();
                        })
                        .setNegativeButton(R.string.no, null)
                        .create().show();
            } else {
                mOrder.getRoute().getPoints().add(finalPoint.copy());
                Prefs.setValue(PrefsName.LAST_CITY, finalPoint.getCityName());
                updateAllView();
            }
        };

        LocationService.me().getAsyncPointByLocation(getContext(), mUserLocation, successListener);
    }

    /**
     * Полное перестроение карты и маршрута
     */
    public void reconstructMap() {
        mOrder = _OrderRegistrator.me().getOrder();

        mMapController.clearAllOverlays();
        updateOrderRoute();
    }

    private void updateOrderRoute() {
        QueryList<_Point> points = mOrder.getRoute().getPoints();
        if (points.isEmpty())
            return;

        int size = 1;
        for (_Point sediAddress : points) {
            try {
                if (!sediAddress.getLatLong().isValid())
                    continue;

                String resDrawable = String.format("%s%s", "ic_map_", size <= MAP_ICON_COUNT ? String.valueOf(size) : "other");
                addPointOnMap(sediAddress.getGeoPoint().toLatLong(),
                        sediAddress.asString(),
                        getResources().getIdentifier(resDrawable, "drawable",
                                getContext().getPackageName()));
                size++;
            } catch (Exception e) {
                LogUtil.log(e);
            }
        }
        buildRouteRoad();
    }

    private void buildRouteRoad() {
        try {
            QueryList<_Point> points = mOrder.getRoute().getPoints();
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

                if (mMapRouteOverlay != null)
                    mMapController.remove(mMapRouteOverlay);

                mMapRouteOverlay = RoadManager.buildRoadOverlay(road, getContext());
                mMapRouteOverlay.setWidth(10);
                mMapRouteOverlay.setColor(ContextCompat.getColor(getContext(), R.color.primaryColor));
                return mMapRouteOverlay;
            }, new IActionFeedback<Overlay>() {
                @Override
                public void onResponse(Overlay result) {
                    mMapController.addOverlay(0, result);
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

    private void addCustomerOnMap() {
        if (mUserLocation == null || !mUserLocation.isValid()) return;
        if (mCustomerOverlay != null)
            mMapController.remove(mCustomerOverlay);
        mCustomerOverlay = addPointOnMap(mUserLocation, getString(R.string.IamHere), R.drawable.ic_my_loc);
    }

    public Overlay getTapOverlay() {
        return new MapEventsOverlay(new MapEventsReceiver() {
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

    private void addNewAddressOnMap(GeoPoint geoPoint) {
        LatLong latLong = new LatLong(geoPoint.getLatitude(), geoPoint.getLongitude());
        checkAddressInGeoService(latLong, Const.NoId);
        LogUtil.log(LogUtil.INFO, "На карте нажали на координату: LAT: %s; LON: %s;", latLong.Latitude, latLong.Longitude);
    }

    private void checkAddressInGeoService(LatLong latLong, int index) {
        LocationService.me().getAsyncPointByLocation(getContext(), latLong, point -> {
            QueryList<_Point> points = new QueryList<>();
            points.add(point);
            points.add(new _Point(point.getCityName(), latLong, true));
            showChooseAddressDialog(points, index);
        });
    }

    private void showChooseAddressDialog(QueryList<_Point> sediAddresses, int index) {
        QueryList<String> stringAddress = new QueryList<>();
        for (_Point a : sediAddresses) {
            if (a.asString().trim().length() < 1)
                continue;
            stringAddress.add(a.asString(true) + " ");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.list_yandex_address, R.id.tvYandexAddress, stringAddress);

        AlertDialog.Builder addressDialog = new AlertDialog.Builder(getContext());
        addressDialog.setTitle(getString(R.string.select_address))
                .setAdapter(adapter, (dialogInterface, i) -> {
                    final _Point a = sediAddresses.get(i);
                    LogUtil.log(LogUtil.INFO, "Пользователь выбрал адрес: " + a.asString());
                    mOrder.getRoute().setByIndex(index, a);
                    int size = mOrder.getRoute().size();
                    String resDrawable = String.format("%s%s", "ic_map_", size <= MAP_ICON_COUNT ? String.valueOf(size) : "other");
                    addPointOnMap(a.getGeoPoint().toLatLong(), a.asString(), getResources().getIdentifier(resDrawable, "drawable",
                            getContext().getPackageName()));
                    updateAllView();
                    buildRouteRoad();
                }).create().show();
    }

    private ItemizedIconOverlay addPointOnMap(LatLong loc, String msg, int icon) {
        return mMapController.addPoint(loc, icon, () -> {
            if (loc.equals(mUserLocation)) {
                showMyLocationChooisePointDialog();
            } else
                MessageBox.show(getContext(), String.format(getString(R.string.msg_YouAddressIs_RemoveQuestion), msg), null, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        _Point sediAddress = mOrder.getRoute().getPoints().FirstOrDefault(item -> item.getLatLong().equals(loc));
                        if (sediAddress == null) return;
                        mOrder.getRoute().remove(sediAddress);
                        updateAllView();
                    }
                }, true, new int[]{R.string.yes, R.string.no});
        });
    }

    private void updateAllView() {
        if (mListener != null)
            mListener.refreshAllViews();
    }

    private void showMyLocationChooisePointDialog() {
        new AlertDialog.Builder(getContext()).
                setTitle(null).setMessage(R.string.msg_IamHere).setPositiveButton(R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        }).setNegativeButton(R.string.AsGiving, (dialog, which) -> {
            checkAddressInGeoService(new LatLong(mUserLocation.Latitude, mUserLocation.Longitude), 0);
        }).setNeutralButton(R.string.AsRoute, (dialog, which) -> {
            int index = mOrder.getRoute().size() - 1;
            checkAddressInGeoService(new LatLong(mUserLocation.Latitude, mUserLocation.Longitude), index);
        }).create().show();
    }

    @Override
    public void onLocationChange(Location location) {
        mUserLocation = new LatLong(location.getLatitude(), location.getLongitude());
        if (mDialog != null) {
            showExampleAddressDialog();
            mDialog.dismissWithAnimation();
            mDialog = null;
            reconstructMap();
            mMapController.zoomTo(mUserLocation, 18);
        }
    }

    public void updateMonitoredOrderPoint(_Order order) {
        mMapController.clearAllOverlays();

        if (order == null) {
            mMonitoringOrder = null;
            reconstructMap();
            return;
        }

        addMapScrollListener(null);
        mMonitoringOrder = new _Order(order);

        _Point point = mMonitoringOrder.getRoute().getPoints().tryGet(0);
        if (point != null)
            mMapController.addPoint(point.getLatLong(), R.drawable.ic_map_1, null);

        _Driver driver = mMonitoringOrder.getDriver();
        if (driver != null && driver.isValid()) {
            IAction driverTap = getDriverTap(order);
            mMapController.addPoint(driver.getGeo().toLatLong(),
                    R.drawable.ic_map_taxi_car, driverTap);
        }
    }

    public void addMapScrollListener(ru.sedi.customerclient.common.AsyncAction.IAction<LatLong> action) {
        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                IGeoPoint mapCenter = mMapController.getMap().getMapCenter();
                action.Action(new LatLong(mapCenter.getLatitude(), mapCenter.getLongitude()));
                return true;
            }
            return false;
        };
        mMapController.getMap().setOnTouchListener(action == null ? null : touchListener);
        mPinView.setVisibility(action == null ? View.GONE: View.VISIBLE);
    }

    public IAction getDriverTap(_Order order) {
        return () -> {
            new DriverInfoDialog(getContext(), order).show();
        };
    }
}
