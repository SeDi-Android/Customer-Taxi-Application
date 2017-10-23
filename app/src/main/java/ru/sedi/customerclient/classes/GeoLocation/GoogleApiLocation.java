package ru.sedi.customerclient.classes.GeoLocation;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;


public class GoogleApiLocation implements GoogleApiClient.ConnectionCallbacks, LocationListener, LocationServiceListener {

    private final GoogleApiClient mGoogleApiClient;
    private final LocationListener mLocationListener;
    private final Context mContext;
    private final LocationRequest mLocationRequest;
    private Location mLocation;

    public static boolean isEnabled(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS;
    }

    public GoogleApiLocation(Context context, @NonNull LocationListener listener,
                             @NonNull GoogleApiClient.OnConnectionFailedListener failedListener) {
        mContext = context;
        mLocationListener = listener;
        mGoogleApiClient = new GoogleApiClient.Builder(context, this, failedListener)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void start() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            if (AppPermissionHelper.isLocationPermissionGranted(mContext)) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void stop() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        if (AppPermissionHelper.isLocationPermissionGranted(mContext)) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public Location getLocation() {
        return mLocation;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location == null) return;
        mLocation = new Location(location);
        if (mLocationListener != null)
            mLocationListener.onLocationChanged(mLocation);
    }
}
