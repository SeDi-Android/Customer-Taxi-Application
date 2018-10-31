package ru.sedi.customerclient.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.SediGeocoderPoint;
import ru.sedi.customerclient.NewDataSharing.SediGeocoderResponse;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.Geocoder.GoogleGeocoder;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.common.GeoTools.GeoTools;
import ru.sedi.customerclient.common.GeoTools.Units;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class LocationGeocodeTask extends AsyncTask<LatLong, Void, _Point> {

    private static final String SEDI_GEOCODE_FORMAT = "/webapi?q=get_address&lat=%f&lon=%f&radius=%d";
    private static final int SEDI_SEARCH_RADIUS = 60;


    private final Context mContext;
    private final ru.sedi.customerclient.common.AsyncAction.IAction<_Point> mAction;

    public LocationGeocodeTask(Context context, ru.sedi.customerclient.common.AsyncAction.IAction<_Point> action) {
        mContext = context;
        mAction = action;
    }

    @Override
    protected _Point doInBackground(LatLong... params) {
        if (isCancelled()) return null;

        LatLong param = params[0];
        _Point point;
        try {
            if (App.isTaxiLive) {
                point = new GoogleGeocoder(mContext, Prefs.getString(PrefsName.LOCALE_CODE))
                        .syncGeocode(param.Latitude, param.Longitude);
            } else {
                //Тут пока скрыл запрос потому что от нашего
                /*point = getResultGeocodeFromSedi(param);
                if (point == null) {
                    point = LocationService.me().getAddressByLocationPoint(mContext, param.toString());
                }*/
                point = LocationService.me().getAddressByLocationPoint(mContext, param.toString());
            }
        } catch (IOException e) {
            return null;
        }

        _Point coordinatOnlyPoint = new _Point();
        if (point!=null && GeoTools.calculateDistance(param, point.getLatLong(), Units.Meters) > App.RADIUS_LIMIT) {
            coordinatOnlyPoint.setCityName(point.getCityName());
            coordinatOnlyPoint.setGeoPoint(param);
            coordinatOnlyPoint.setCoordinatesonly(true);
            coordinatOnlyPoint.setChecked(true);
            return coordinatOnlyPoint;
        }
        return point;
    }

    private _Point getResultGeocodeFromSedi(LatLong param) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        String groupChanel = mContext.getString(R.string.groupChanel);
        StringBuilder sb = new StringBuilder();
        sb.append(Server.isHttp(groupChanel) ? "http://" : "https://");
        sb.append(groupChanel);
        sb.append(String.format(SEDI_GEOCODE_FORMAT, param.Latitude, param.Longitude, SEDI_SEARCH_RADIUS));

        Request request = new Request.Builder().url(sb.toString()).build();
        Response execute = client.newCall(request).execute();
        if (!execute.isSuccessful() || execute.body() == null) return null;
        String jsonBody = execute.body().string();

        SediGeocoderResponse geododerResponse = new Gson().fromJson(jsonBody,
                SediGeocoderResponse.class);

        if (!geododerResponse.isSuccess() || geododerResponse.getAddresses().length < 1)
            return null;

        _Point point = null;

        int bestDistance = 999;
        for (SediGeocoderPoint p : geododerResponse.getAddresses()) {
            double distance = GeoTools.calculateDistance(param, p.getLatLong(), Units.Meters);
            if (distance < bestDistance) {
                bestDistance = (int) distance;
                point = p.toDefaultPoint();
            }
        }
        if (point != null)
            point.setChecked(true);
        return point;
    }

    @Override
    protected void onPostExecute(_Point point) {
        super.onPostExecute(point);
        if (point == null || mAction == null) return;
        mAction.Action(point);
    }
}
