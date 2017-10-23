package ru.sedi.customerclient.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;

import ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.GoogleDetailAPI;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.ServerManager;

public class AddressDetailTask extends AsyncTask<_Point, Void, _Point> {

    private final OnAddressDetailSuccess mAddressDetailSuccess;

    public AddressDetailTask(OnAddressDetailSuccess addressDetailSuccess) {
        mAddressDetailSuccess = addressDetailSuccess;
    }

    @Override
    protected _Point doInBackground(_Point... params) {
        _Point point = params[0];
        if (point == null) return null;
        if (point.getChecked())
            return point;

        String placeId = point.getPlaceId();
        if (TextUtils.isEmpty(placeId))
            return null;

        _Point p;
        try {
            p = new GoogleDetailAPI().detail(placeId);
            _Point address = ServerManager.GetInstance().findAddress(p);
            if (address != null)
                p = address;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }


    @Override
    protected void onPostExecute(_Point point) {
        super.onPostExecute(point);
        if (point == null || mAddressDetailSuccess == null) return;
        mAddressDetailSuccess.onSuccessResponse(point);

    }
}
