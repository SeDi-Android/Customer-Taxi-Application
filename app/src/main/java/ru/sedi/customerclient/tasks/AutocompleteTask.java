package ru.sedi.customerclient.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.GoogleAutocomplete;
import ru.sedi.customerclient.NewDataSharing.LocationConverter;
import ru.sedi.customerclient.NewDataSharing.SediAutocomplete;
import ru.sedi.customerclient.NewDataSharing.YandexPointWrapper;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;


public class AutocompleteTask extends AsyncTask<Pair<String, LatLong>, ArrayList<_Point>, Void> {
    private final AutoCompleteTextView mCompleteTextView;
    private final Context mContext;
    private QueryList<_Point> mAddresses;
    OkHttpClient okHttpClient = new OkHttpClient();

    public AutocompleteTask(Context context, AutoCompleteTextView completeTextView, QueryList<_Point> sediAddresses) {
        mContext = context;
        mAddresses = sediAddresses;
        if (mAddresses == null) {
            mAddresses = new QueryList<>();
        }
        mAddresses.clear();
        mCompleteTextView = completeTextView;
    }

    @Override
    protected Void doInBackground(Pair<String, LatLong>... params) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        if (isCancelled()) return null;
        Pair<String, LatLong> s = params[0];

        requestByGoogle(s);
        requestByYandex(s);
        if (s.first.length() > 2)
            requestBySedi(s);

        return null;
    }

    private void requestByYandex(Pair<String, LatLong> s) {
        String url = "https://geocode-maps.yandex.ru/1.x/?format=json";
        url += "&geocode=" + s.first;
        Call call = okHttpClient.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                LogUtil.log(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;
                String jsonString = response.body().string();

                List<YandexPointWrapper.FeatureMember> geoObjects;
                try {
                    YandexPointWrapper.YandexPoint yandexPoints = new Gson().fromJson(jsonString, YandexPointWrapper.YandexPoint.class);
                    geoObjects = yandexPoints.getResponse().getGeoObjectCollection().getFeatureMember();
                } catch (JsonSyntaxException e) {
                    LogUtil.log(e);
                    return;
                }
                if (geoObjects != null) {
                    ArrayList<_Point> arrayList = new ArrayList<>();
                    for (YandexPointWrapper.FeatureMember example1 : geoObjects) {
                        YandexPointWrapper.GeoObject object = example1.getGeoObject();
                        _Point convert = LocationConverter.convert(object);
                        convert.setDataSource(_Point.Type.YANDEX);
                        arrayList.add(new _Point(convert));
                    }
                    publishProgress(arrayList);
                }
            }
        });
    }

    private void requestBySedi(Pair<String, LatLong> s) {
        String url = "http://api.sedi.ru/handlers/autocomplete.ashx?q=addr&types=street,object,city";
        url += "&search=" + s.first;
        if (s.second != null && s.second.isValid()) {
            url += String.format(Locale.ENGLISH, "&lat=%f&lon=%f"
                    , s.second.Latitude, s.second.Longitude);
            url += "&radius=5000";
        }
        Call call = okHttpClient.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                LogUtil.log(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String string = response.body().string();
                SediAutocomplete.Address[] example;
                try {
                    example = new Gson().fromJson(string, SediAutocomplete.Address[].class);
                } catch (JsonSyntaxException e) {
                    LogUtil.log(e);
                    return;
                }
                if (example != null) {
                    ArrayList<_Point> arrayList = new ArrayList<>();
                    for (SediAutocomplete.Address example1 : example) {
                        _Point convert = LocationConverter.convert(example1);
                        convert.setDataSource(_Point.Type.SEDI);
                        arrayList.add(new _Point(convert));
                    }
                    publishProgress(arrayList);
                }
            }
        });
    }


    private void requestByGoogle(Pair<String, LatLong> s) {
        String url = String.format("https://maps.googleapis.com/maps/api/place/queryautocomplete/json?key=%s",
                mContext.getString(R.string.google_api_key));
        url += "&input=" + s.first;
        if (s.second != null && s.second.isValid()) {
            url += String.format(Locale.ENGLISH, "&location=%f,%f&radius=5000"
                    , s.second.Latitude, s.second.Longitude);
        }
        Call call = okHttpClient.newCall(new Request.Builder().url(url).build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.log(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;
                String string = response.body().string();
                GoogleAutocomplete.Example example = null;
                try {
                    example = new Gson().fromJson(string, GoogleAutocomplete.Example.class);
                } catch (JsonSyntaxException e) {
                    LogUtil.log(e);
                }
                if (example != null && example.getPredictions() != null) {
                    if (!example.getStatus().equals("OK"))
                        LogUtil.log(LogUtil.ERROR, example.getStatus());

                    ArrayList<_Point> arrayList = new ArrayList<>();
                    for (GoogleAutocomplete.Prediction prediction : example.getPredictions()) {
                        _Point convert = LocationConverter.convert(prediction);
                        convert.setDataSource(_Point.Type.GOOGLE);
                        arrayList.add(new _Point(convert));
                    }
                    publishProgress(arrayList);

                }
            }
        });
    }

    @Override
    protected void onProgressUpdate(ArrayList<_Point>... values) {
        super.onProgressUpdate(values);
        for (_Point point : values[0]) {
            if (TextUtils.isEmpty(point.getDesc().trim()) || containAddress(point)) continue;
            mAddresses.add(point);
        }
        Collections.sort(mAddresses, (o1, o2) -> o1.getDataSource().getWeight() < o2.getDataSource().getWeight() ? -1 : 1);

        ArrayAdapter<_Point> adapter = new ArrayAdapter<>(mContext, R.layout.item_autocomplete, R.id.tvText ,mAddresses);
        mCompleteTextView.setAdapter(adapter);
        mCompleteTextView.showDropDown();
    }

    private boolean containAddress(_Point point) {
        for (_Point addres : mAddresses) {
            if (addres.getDesc().equalsIgnoreCase(point.getDesc())) {
                if (point.getDataSource().equals(_Point.Type.SEDI)
                        || addres.getDataSource().equals(_Point.Type.GOOGLE)) {
                    mAddresses.remove(addres);
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}