package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

import org.apache.commons.lang3.text.StrBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.sedi.customerclient.common.LogUtil;


public class LocationConverter {
    public static _Point convert(SediAutocomplete.Address object) {
        _Point point = new _Point();

        point.setID(object.getN());
        point.setGeoPoint(new _GeoPoint(object.getG().getLat(),
                object.getG().getLon()));

        if (!TextUtils.isEmpty(object.getC())) {
            point.setCityName(object.getC());
        }

        String type = object.getT();
        if (!TextUtils.isEmpty(object.getV())) {
            switch (type) {
                case "s":
                    point.setStreetName(object.getV());
                    break;
                case "o":
                    point.setObjectName(object.getV());
            }
        }

        point.setChecked(true);
        return point;
    }

    public static _Point convert(GoogleAutocomplete.Prediction object) {
        _Point point = new _Point();

        try {
            String desc = object.getStructuredFormatting().getMainText();
            String secondaryText = object.getStructuredFormatting().getSecondaryText();
            point.setGoogleStruckFormatting(object.getStructuredFormatting());
            point.setPlaceId(object.getPlaceId());
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return point;
    }

    public static _Point convert(YandexPointWrapper.GeoObject object) {
        _Point point = new _Point();

        List<YandexPointWrapper.Component> components = object.getMetaDataProperty()
                .getGeocoderMetaData().getAddress().getComponents();

        point.setCountryName(getYandexAddressComponent(components, "country"));

        String locality = getYandexAddressComponent(components, "locality");
        if (TextUtils.isEmpty(locality))
            locality = getYandexAddressComponent(components, "province", true);
        point.setCityName(locality);

        String street = getYandexAddressComponent(components, "street");
        street = convertToSediFormat(street);
        point.setStreetName(street);
        point.setHouseNumber(getYandexAddressComponent(components, "house"));
        point.setObjectName(getYandexAddressComponent(components, "metro"));

        String[] split = object.getPoint().getPos().split(" ");
        point.setGeoPoint(new _GeoPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0])));

        if (point.isMinimalAddress())
            point.setChecked(true);

        return point;
    }

    private static String convertToSediFormat(String street) {
        if (TextUtils.isEmpty(street)) return "";
        String[] keyword = {"проспект", "улица", "проезд", "шоссе", "переулок"};
        String[] streetComponent = street.split(" ");

        boolean needMove = false;
        for (String s : keyword) {
            if (s.equalsIgnoreCase(streetComponent[0])) {
                needMove = true;
                break;
            }
        }
        if (needMove) {
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < streetComponent.length; i++) {
                builder.append(streetComponent[i]);
                builder.append(" ");
            }
            builder.append(streetComponent[0]);
            String newString = builder.toString();
            LogUtil.log(LogUtil.INFO, "Convert from %s to %s", street, newString);
            return newString;
        } else {
            return street;
        }
    }

    private static String getYandexAddressComponent(List<YandexPointWrapper.Component> components, String name) {
        return getYandexAddressComponent(components, name, false);
    }

    private static String getYandexAddressComponent(List<YandexPointWrapper.Component> components, String name, boolean getLast) {
        ArrayList<String> names = new ArrayList<>();
        for (YandexPointWrapper.Component component : components) {
            if (component.getKind().equalsIgnoreCase(name)) {
                if (!getLast)
                    return component.getName();
                else
                    names.add(component.getName());
            }
        }
        String lastName = !names.isEmpty() ? names.get(names.size() - 1) : "";
        return lastName;
    }

    private static String revertGoogleAddress(String replace) {
        String[] split = replace.split(", ");
        String s = "";
        for (int i = split.length - 1; i >= 0; i--) {
            if (!s.isEmpty())
                s += ", ";
            s += split[i];
        }
        return s;
    }
}
