package ru.sedi.customerclient.classes;

import android.text.TextUtils;

import java.io.IOException;

import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.GeoLocation.Nominatium.NominatimGeocoder;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class AddressParser {
    private NominatimGeocoder geocoder;
    private String stringAddress;


    public AddressParser(NominatimGeocoder geocoder, String stringAddress) {
        this.geocoder = geocoder;
        this.stringAddress = stringAddress;
    }

    public AddressParser(String stringAddress) {
        this.stringAddress = stringAddress;
    }

    public String parse() {
        if (stringAddress == null || stringAddress.length() < 1)
            return null;

        removeDoubleWhitespace();

        String address = null;
        if (isMakeDivide(","))
            address = findWithDivider(",");

        if (address == null) {
            stringAddress = stringAddress.replace(",", " ");
            removeDoubleWhitespace();
            address = findWithDivider(" ");
        }
        return address;
    }

    private boolean isMakeDivide(String divider) {
        return stringAddress.contains(divider);
    }

    private String findWithDivider(String divider) {
        String[] addressElements = getElementDivide(divider);
        if (addressElements.length < 1)
            return null;
        return tryGetCorrectedAddress(addressElements);
    }

    public String tryGetCorrectedAddress(String[] addressElements) {
        int limitConcatenation = 2;
        String tempCity = "";
        for (int i = 0; i < addressElements.length; i++) {
            if (i >= limitConcatenation - 1)
                break;

            tempCity += " " + addressElements[i];
            tempCity = tempCity.trim();
            boolean isCity = findCityInGeo(tempCity);
            if (isCity) {
                return getCorrectAddressString(tempCity);
            }
        }
        return null;
    }

    private String getCorrectAddressString(String tempCity) {
        stringAddress = stringAddress.replace(", ", " ");
        stringAddress = stringAddress.replace(tempCity, tempCity + ", ");
        removeDoubleWhitespace();
        return stringAddress;
    }

    private boolean findCityInGeo(String element) {
        try {
            QueryList<_Point> points = geocoder.syncSearch(element);
            _Point point = points.FirstOrDefault(item -> item.getCityName().toLowerCase().contains(element));
            return point != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeDoubleWhitespace() {
        stringAddress = stringAddress.replaceAll("( +)", " ").trim();
    }

    public String[] getElementDivide(String divider) {
        return stringAddress.split(divider);
    }

    public String getStringAddress() {
        return stringAddress;
    }

}
