package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.Geocoder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;

import kg.ram.asyncjob.AsyncJob;
import kg.ram.asyncjob.IOnSuccessListener;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.MessageBox.MessageBox;

/**
 * Created by RAM on 29.05.2017.
 */

public class GoogleGeocoder {

    private String mApiKey;
    private String mLocale = "ru";
    private Context mContext;
    private final String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s&language=%s";

    public GoogleGeocoder(Context context, String locale) {
        mContext = context;
        mLocale = locale;
        mApiKey = mContext.getString(R.string.googleApiKey);
    }

    public GoogleGeocoder(Context context) {
        mContext = context;
    }

    public void geocode(LatLong latLong, boolean withProgress, IOnSuccessListener<_Point> listener) {
        geocode(latLong.Latitude, latLong.Longitude, withProgress, listener);
    }

    public void geocode(double latitude, double longitude, boolean withProgress, IOnSuccessListener<_Point> listener) {
        AsyncJob.Builder<_Point> builder = new AsyncJob.Builder<_Point>()
                .doWork(() -> {
                    GoogleGeocoderResponse response = requestGeocode(latitude, longitude);
                    if (!response.isSuccess()) {
                        String s = response.getStatus() + "\nFor location: " + latitude + ", " + longitude;
                        throw new IllegalAccessException(s);
                    }

                    if (response.getResults().length <= 0) {
                        throw new IndexOutOfBoundsException("Empty google geocoder result.");
                    }

                    GoogleGeocoderResponse.GoogleGeocoderResults results = response.getResults()[0];
                    _Point p = results.convertToPoint();
                    return p;
                })
                .onSuccess(listener);
        if (withProgress) {
            builder.withProgress(mContext, R.string.msg_PleaseWait);
            builder.onFailure(throwable -> MessageBox.show(mContext, throwable.getMessage()));
        } else {
            builder.onFailure(throwable -> Log.e("GoogleGeocoder", throwable.getMessage()));
        }
        AsyncJob<_Point> build = builder.build();
        build.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private GoogleGeocoderResponse requestGeocode(double latitude, double longitude) throws IOException {
        String url = String.format(Locale.ENGLISH, URL, latitude, longitude, mApiKey, mLocale);
        OkHttpClient okHttpClient = new OkHttpClient();
        Call call = okHttpClient.newCall(new Request.Builder().url(url).build());
        Response execute = call.execute();
        if (!execute.isSuccessful()) {
            throw new IOException(execute.message());
        }

        return new Gson().fromJson(execute.body().string(), GoogleGeocoderResponse.class);
    }
}
