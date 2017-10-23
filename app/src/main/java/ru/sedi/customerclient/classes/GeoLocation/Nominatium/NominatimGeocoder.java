package ru.sedi.customerclient.classes.GeoLocation.Nominatium;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import kg.ram.asyncjob.AsyncJob;
import kg.ram.asyncjob.IOnSuccessListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.Toast.ToastHelper;

public class NominatimGeocoder {

    private final String URL_FORMAT = "http://nominatim.openstreetmap.org/search?format=json&accept-language=%s&q=%s&poligon=1&addressdetails=1";
    private final OkHttpClient mOkHttpClient;
    private Context mContext;
    private String mLang = "ru";


    public NominatimGeocoder(Context context, String lang) {
        mContext = context;
        if (!TextUtils.isEmpty(lang))
            mLang = lang;
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public void asyncSearch(String query, boolean showProgress, IOnSuccessListener<QueryList<_Point>> successListener) {
        AsyncJob.Builder<QueryList<_Point>> builder = new AsyncJob.Builder<QueryList<_Point>>()
                .doWork(() -> syncSearch(query))
                .onSuccess(successListener);
        if (showProgress) {
            builder.withProgress(mContext, R.string.msg_PleaseWait);
            builder.onFailure(throwable -> ToastHelper.showShortToast(throwable.getMessage()));
        }
        builder.buildAndExecute();
    }

    public QueryList<_Point> syncSearch(String query) throws IOException {
        Request request = generateRequest(query.replace(" ", ""));

        try {
            Response execute = mOkHttpClient.newCall(request).execute();
            if (!execute.isSuccessful() || execute.body() == null) return new QueryList<>();
            String string = new String(execute.body().string());
            Address[] addresses = new Gson().fromJson(string, Address[].class);
            return convertToPoint(addresses);
        } catch (IOException e) {
            String exMessage = e.getMessage();
            if (exMessage.contains("not found"))
                exMessage = "timeout";
            throw new IOException(exMessage);
        } catch (JsonSyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Request generateRequest(String query) {
        String url = String.format(URL_FORMAT, mLang, query);
        return new Request.Builder().url(url).build();
    }

    private QueryList<_Point> convertToPoint(Address[] addresses) {
        QueryList<_Point> points = new QueryList<>();
        for (Address address : addresses) {
            if (address.getAddressElements() == null) continue;

            _Point point = new _Point();
            point.setGeoPoint(new LatLong(address.getLatitude(), address.getLongitude()));
            point.setCityName(address.getAddressElements().getCity());
            point.setStreetName(address.getAddressElements().getStreet());
            point.setHouseNumber(address.getAddressElements().getHousenumber());
            point.setPostalCode(address.getAddressElements().getPostcode());
            if (!point.isMinimalAddress() && !TextUtils.isEmpty(point.getCityName()))
                point.setCoordinatesonly(true);
            point.setChecked(true);
            points.add(point);
        }
        return points;
    }
}
