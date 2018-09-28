package ru.sedi.customerclient.classes;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customerclient.NewDataSharing.GoogleAutocomplete;
import ru.sedi.customerclient.NewDataSharing.LocationConverter;
import ru.sedi.customerclient.NewDataSharing.YandexPointWrapper;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;

public class ExternalAutocomplete {

    private final String locale;
    private String googleKey;
    private boolean isNextQueriesAsAsync;
    private OkHttpClient client = new OkHttpClient.Builder().build();
    private ArrayList<Call> activeCalls = new ArrayList<>();

    public ExternalAutocomplete(String googleKey, String locale) {
        this.googleKey = googleKey;
        this.locale = locale;
    }

    public void find(AutocompleteQuery query, ExternalAutocompleteResponse callback) {
        if (isNextQueriesAsAsync) {
            searchInYandex(query, callback);
            searchInGoogle(query, callback);
        } else {
            if (query.isEuropeanQuery()) {
                searchForEurope(query, callback);
            } else {
                searchForSng(query, callback);
            }
        }
    }

    public void cancelCalls(){
        for (Call call : activeCalls) {
            call.cancel();
        }
    }

    private void searchForSng(AutocompleteQuery query, ExternalAutocompleteResponse callback) {
        searchInYandex(query, new ExternalAutocompleteResponse() {
            @Override
            public void onSuccess(ArrayList<_Point> points) {
                callback.onSuccess(points);
            }

            @Override
            public void onFailure() {
                searchInGoogle(query, callback);
            }
        });
    }

    private void searchForEurope(AutocompleteQuery query, ExternalAutocompleteResponse callback) {
        searchInGoogle(query, new ExternalAutocompleteResponse() {
            @Override
            public void onSuccess(ArrayList<_Point> points) {
                callback.onSuccess(points);
            }

            @Override
            public void onFailure() {
                searchInYandex(query, callback);
            }
        });
    }

    private void searchInYandex(AutocompleteQuery query, ExternalAutocompleteResponse callback) {
        Request request = getYandexSearchRequest(query);

        Call yandexCall = client.newCall(request);
        activeCalls.add(yandexCall);
        yandexCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(!call.isCanceled()) {
                    setNextQueryAsAsync(true);
                    callback.onFailure();
                }
                activeCalls.remove(yandexCall);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (isCorrectResponse(response)) {
                    try {
                        List<YandexPointWrapper.FeatureMember> members = handleYandexResponse(response);
                        QueryList<_Point> points = convertMembersToPoint(query, members);
                        callback.onSuccess(points);
                    } catch (Exception e) {
                        callback.onFailure();
                    }
                    activeCalls.remove(yandexCall);
                } else {
                    setNextQueryAsAsync(true);
                    callback.onFailure();
                    activeCalls.remove(yandexCall);
                }
            }
        });
    }

    private QueryList<_Point> convertMembersToPoint(AutocompleteQuery query, List<YandexPointWrapper.FeatureMember> members) {
        QueryList<_Point> points = new QueryList<>();
        for (YandexPointWrapper.FeatureMember member : members) {
            _Point convert = LocationConverter.convert(member.getGeoObject());
            if (query.containCorrectCity()
                    && convert.getCityName() != null
                    && !convert.getCityName().equalsIgnoreCase(query.getCity())) continue;
            //if (!convert.isMinimalAddress()) continue;

            convert.setDataSource(_Point.Type.YANDEX);
            points.add(new _Point(convert));
        }
        return points;
    }

    private List<YandexPointWrapper.FeatureMember> handleYandexResponse(Response response) throws Exception {
        if (response.body() != null) {
            String json = response.body().string();
            YandexPointWrapper.YandexPoint yandexPoints = new Gson().fromJson(json,
                    YandexPointWrapper.YandexPoint.class);
            return yandexPoints.getResponse().getGeoObjectCollection().getFeatureMember();
        } else {
            throw new Exception("Response body is null");
        }

    }

    private Request getYandexSearchRequest(AutocompleteQuery query) {
        String url = buildYandexSearchUrl(query);
        return new Request.Builder().url(url).build();
    }

    private String buildYandexSearchUrl(AutocompleteQuery query) {
        String url = "https://geocode-maps.yandex.ru/1.x/?format=json";
        if(isUserLocationValid(query.getUserLocation())){
            LatLong userLoction = query.getUserLocation();
            url += String.format(Locale.ENGLISH, "&ll=%f,%f&spn=0.5,0.5",
                    userLoction.Longitude, userLoction.Latitude);
        }
        url += "&geocode=" + query.getAddress();
        url += "&lang=" + locale;
        return url;
    }

    private void searchInGoogle(AutocompleteQuery query, ExternalAutocompleteResponse callback) {
        Request request = getGoogleSearchRequest(query);
        Call googleCall = client.newCall(request);
        activeCalls.add(googleCall);
        googleCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(!call.isCanceled()) {
                    setNextQueryAsAsync(true);
                    callback.onFailure();
                }
                activeCalls.remove(googleCall);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (isCorrectResponse(response)) {
                    try {
                        GoogleAutocomplete.Example autocomplete = handleGoogleResponse(response);
                        //Google autocomplete response status not OK
                        if (!autocomplete.isOk()) {
                            setNextQueryAsAsync(true);
                            callback.onFailure();
                        } else {
                            setNextQueryAsAsync(false);
                            List<GoogleAutocomplete.Prediction> predictions = autocomplete.getPredictions();
                            QueryList<_Point> points = convertPredictionsToPoint(query, predictions);
                            callback.onSuccess(points);
                        }
                    } catch (Exception ignored) {

                    }
                    activeCalls.remove(googleCall);
                } else {
                    setNextQueryAsAsync(true);
                    callback.onFailure();
                    activeCalls.remove(googleCall);
                }
            }
        });
    }

    private void setNextQueryAsAsync(boolean b) {
        isNextQueriesAsAsync = b;
    }

    private QueryList<_Point> convertPredictionsToPoint(AutocompleteQuery query, List<GoogleAutocomplete.Prediction> predictions) {
        QueryList<_Point> points = new QueryList<>();
        for (GoogleAutocomplete.Prediction prediction : predictions) {
            _Point convert = LocationConverter.convert(prediction);
            if (query.containCorrectCity()
                    && convert.getGoogleStruckFormatting() != null
                    && !convert.getGoogleStruckFormatting().containCity(query.getCity())) {
                continue;
            }
            convert.setDataSource(_Point.Type.GOOGLE);
            points.add(new _Point(convert));
        }
        return points;
    }

    private GoogleAutocomplete.Example handleGoogleResponse(Response response) throws Exception {
        if (response.body() != null) {
            String string = response.body().string();
            return new Gson().fromJson(string, GoogleAutocomplete.Example.class);
        } else {
            throw new Exception("Response body is null");
        }
    }

    private boolean isCorrectResponse(Response response) {
        return response != null && response.isSuccessful();
    }

    private Request getGoogleSearchRequest(AutocompleteQuery query) {
        String url = buildGoogleSearchUrl(query);
        return new Request.Builder().url(url).build();
    }

    private String buildGoogleSearchUrl(AutocompleteQuery query) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/place/queryautocomplete/json?key=%s",
                googleKey);
        url += "&language=" + locale;
        url += "&input=" + query.getAddress();
        if (isUserLocationValid(query.getUserLocation())) {
            LatLong loction = query.getUserLocation();
            url += String.format(Locale.ENGLISH, "&location=%f,%f&radius=5000"
                    , loction.Latitude, loction.Longitude);
        }
        return url;
    }

    private boolean isUserLocationValid(LatLong location) {
        return location != null && location.isValid();
    }

    public interface ExternalAutocompleteResponse {
        void onSuccess(ArrayList<_Point> points);

        void onFailure();
    }
}
