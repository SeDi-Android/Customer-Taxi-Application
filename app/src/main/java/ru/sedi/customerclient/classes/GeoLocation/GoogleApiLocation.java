package ru.sedi.customerclient.classes.GeoLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import ru.sedi.customerclient.classes.Helpers.AppPermissionHelper;


public class GoogleApiLocation implements GoogleApiClient.ConnectionCallbacks, LocationServiceListener {

    private final GoogleApiClient mGoogleApiClient;
    private final LocationListener mLocationListener;
    private final Context mContext;
    private final LocationRequest mLocationRequest;
    private Location mLocation;
    private LocationCallback mLocationCallback;

    public static boolean isEnabled(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS;
    }

    public GoogleApiLocation(Context context, @NonNull LocationListener listener,
                             @NonNull GoogleApiClient.OnConnectionFailedListener failedListener) {
        mContext = context;
        mLocationListener = listener;
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        mLocation = new Location(location);
                        mLocationListener.onLocationChanged(mLocation);
                    }
                }
            }
        };
        mGoogleApiClient = new GoogleApiClient.Builder(context, this, failedListener)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void start() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            if (AppPermissionHelper.isLocationPermissionGranted(mContext)) {
                LocationServices
                        .getFusedLocationProviderClient(mContext)
                        .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        }
    }

    @Override
    public void stop() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        if (AppPermissionHelper.isLocationPermissionGranted(mContext)) {
            mLocationCallback = new LocationCallback();
            LocationServices
                    .getFusedLocationProviderClient(mContext)
                    .removeLocationUpdates(mLocationCallback);
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
}
