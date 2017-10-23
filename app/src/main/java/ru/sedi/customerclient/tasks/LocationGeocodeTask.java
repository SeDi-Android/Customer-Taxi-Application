package ru.sedi.customerclient.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.common.GeoTools.GeoTools;
import ru.sedi.customerclient.common.GeoTools.Units;
import ru.sedi.customerclient.common.LatLong;

public class LocationGeocodeTask extends AsyncTask<LatLong, Void, _Point> {

    private final Context mContext;
    private final ru.sedi.customerclient.common.AsyncAction.IAction<_Point> mAction;

    public LocationGeocodeTask(Context context, ru.sedi.customerclient.common.AsyncAction.IAction<_Point> action) {
        mContext = context;
        mAction = action;
    }

    @Override
    protected _Point doInBackground(LatLong... params) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isCancelled()) return null;

        LatLong param = params[0];
        _Point point = null;
        try {
            point = LocationService.me().getAddressByLocationPoint(mContext, param.toString());
        } catch (IOException e) {
            return point;
        }

        _Point coordinatOnlyPoint = new _Point();
        if (GeoTools.calculateDistance(param, point.getLatLong(), Units.Meters) > App.RADIUS_LIMIT) {
            coordinatOnlyPoint.setCityName(point.getCityName());
            coordinatOnlyPoint.setGeoPoint(param);
            coordinatOnlyPoint.setCoordinatesonly(true);
            coordinatOnlyPoint.setChecked(true);
            return coordinatOnlyPoint;
        }
        return point;
    }

    @Override
    protected void onPostExecute(_Point point) {
        super.onPostExecute(point);
        if (point == null || mAction == null) return;
        mAction.Action(point);
    }
}
