package ru.sedi.customerclient.classes.GeoLocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;

/**
 * Created by Marchenko Roman on 28.02.2017.
 */

public class DefaultApiLocation implements LocationServiceListener, LocationListener {

    private final LocationManager mLocationManager;
    private final Context mContext;
    private final LocationService mLocationService;
    private Location mLocation;

    public DefaultApiLocation(Context context, LocationManager locationManager, LocationService locationService) {
        mContext = context;
        mLocationManager = locationManager;
        mLocationService = locationService;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = new Location(location);
        if (mLocationService != null)
            mLocationService.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void start() {
        if (mLocationManager == null
                || !AppPermissionHelper.isLocationPermissionGranted(mContext)) {
            return;
        }

        if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        }

        if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, this);
        }
    }

    @Override
    public void stop() {
        if (mLocationManager == null
                || !AppPermissionHelper.isLocationPermissionGranted(mContext)) {
            return;
        }
        mLocationManager.removeUpdates(this);
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }
}
