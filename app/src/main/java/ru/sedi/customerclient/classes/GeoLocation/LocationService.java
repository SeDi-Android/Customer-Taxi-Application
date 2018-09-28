package ru.sedi.customerclient.classes.GeoLocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kg.ram.asyncjob.AsyncJob;
import kg.ram.asyncjob.IOnSuccessListener;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.GeoLocation.Nominatium.NominatimGeocoder;
import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.ILocationChangeListener;

public class LocationService implements LocationListener {

    public static final LatLong DEFAULT_LOCATION = new LatLong(200, 200);

    private static LocationService instance;
    private final LocationManager mLocationManager;
    private final Context mContext;
    private List<ILocationChangeListener> mListeners = new ArrayList<>();
    private LocationServiceListener mLocationServiceListener;
    private boolean mGoogleConnectionFailed;

    public static LocationService me() {
        return instance;
    }

    private LocationService(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        createApiLocation();
    }

    private void createApiLocation() {
        /*if (GoogleApiLocation.isEnabled(mContext) && !mGoogleConnectionFailed)
            mLocationServiceListener = new GoogleApiLocation(mContext, this, getFailedListener());
        else*/
            mLocationServiceListener = new DefaultApiLocation(mContext, mLocationManager, this);
    }

    public static LocationService with(Context context) {
        if (instance == null)
            instance = new LocationService(context);
        return instance;
    }

    public _Point getAddressByLocationPoint(Context context, String query) throws IOException {
        return getAddressListByLocationPoint(context, query).tryGet(0);
    }

    public QueryList<_Point> getAddressListByLocationPoint(Context context, String query) throws IOException {
        return new NominatimGeocoder(context, Prefs.getString(PrefsName.LOCALE_CODE)).syncSearch(query);
    }


    public void getAsyncPointByLocation(Context context, LatLong latLong, IOnSuccessListener<_Point> successListener) {
        getAsyncPointByLocation(context, true, latLong, successListener);
    }


    public void getAsyncPointByLocation(Context context, boolean withProgress, LatLong latLong, IOnSuccessListener<_Point> successListener) {
        AsyncJob.Builder<_Point> build = new AsyncJob.Builder<_Point>();

        if (withProgress)
            build.withProgress(context, R.string.msg_PleaseWait);

        build.doWork(() -> getAddressByLocationPoint(context, latLong.toString()))
                .onSuccess(successListener)
                .onFailure(throwable -> ToastHelper.ShowLongToast(throwable.getMessage()));
        build.build().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean onceProviderEnabled() {
        if (mLocationManager == null) return false;
        return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public LatLong getLocation() {
        if (mLocationServiceListener == null) return DEFAULT_LOCATION;
        Location location = mLocationServiceListener.getLocation();
        if (location == null) return DEFAULT_LOCATION;

        return new LatLong(location.getLatitude(), location.getLongitude());
    }

    public void startListener(ILocationChangeListener listener) {
        subscribe(listener);
        startListener();
    }

    public void stopListener(ILocationChangeListener listener) {
        unsubscribe(listener);
        stopListener();
    }

    public void startListener() {
        mLocationServiceListener.start();
    }

    public void stopListener() {
        mLocationServiceListener.stop();
    }

    public void subscribe(ILocationChangeListener listener) {
        mListeners.add(listener);
    }

    public void unsubscribe(ILocationChangeListener listener) {
        mListeners.remove(listener);
    }

    private void notifySubscriber(Location location) {
        for (ILocationChangeListener listener : mListeners) {
            listener.onLocationChange(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        notifySubscriber(location);
    }

    private GoogleApiClient.OnConnectionFailedListener getFailedListener() {
        return connectionResult -> {
            mGoogleConnectionFailed = true;
            createApiLocation();
        };
    }

    public LatLong getLastLocation() {
        if (mLocationManager == null
                || !AppPermissionHelper.isLocationPermissionGranted(mContext)) {
            return null;
        }

        Location gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long gpsTime = 0, networkTime = 0;

        if (gpsLocation != null) gpsTime = gpsLocation.getTime();
        if (networkLocation != null) networkTime = networkLocation.getTime();

        Location location = gpsTime > networkTime ? gpsLocation : networkLocation;

        if (location == null) return null;

        return new LatLong(location.getLatitude(), location.getLongitude());
    }
}
