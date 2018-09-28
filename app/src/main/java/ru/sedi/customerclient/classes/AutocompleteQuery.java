package ru.sedi.customerclient.classes;

import android.text.TextUtils;

import ru.sedi.customerclient.common.LatLong;

public class AutocompleteQuery {
    private String city;
    private String address;
    private LatLong userLocation;
    private boolean isEuropeanQuery;

    public AutocompleteQuery(String city, String address, LatLong userLocation, boolean isEuropeanQuery) {
        this.city = city;
        this.address = address;
        this.userLocation = userLocation;
        this.isEuropeanQuery = isEuropeanQuery;
    }

    public String getAddress() {
        if (!TextUtils.isEmpty(city))
            return city + ", " + address;

        return address;
    }

    public LatLong getUserLocation() {
        return userLocation;
    }

    public boolean isEuropeanQuery() {
        return isEuropeanQuery;
    }

    public boolean containCorrectCity() {
        return !TextUtils.isEmpty(city) && !city.contains("(");
    }

    public String getCity() {
        return city;
    }
}
