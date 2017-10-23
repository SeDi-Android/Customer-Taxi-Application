package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

import java.util.List;


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

        String desc = object.getStructuredFormatting().getMainText();
        String[] split = object.getStructuredFormatting().getSecondaryText().split(",");
        desc += ", " + split[0];

        point.setDesc(revertGoogleAddress(desc));
        point.setPlaceId(object.getPlaceId());
        return point;
    }

    public static _Point convert(YandexPointWrapper.GeoObject object) {
        _Point point = new _Point();

        List<YandexPointWrapper.Component> components = object.getMetaDataProperty()
                .getGeocoderMetaData().getAddress().getComponents();

        point.setCountryName(getYandexAddressComponent(components, "country"));
        point.setCityName(getYandexAddressComponent(components, "locality"));
        point.setStreetName(getYandexAddressComponent(components, "street"));
        point.setHouseNumber(getYandexAddressComponent(components, "house"));
        point.setObjectName(getYandexAddressComponent(components, "metro"));

        String[] split = object.getPoint().getPos().split(" ");
        point.setGeoPoint(new _GeoPoint(Double.parseDouble(split[1]), Double.parseDouble(split[0])));

        if(point.isMinimalAddress())
            point.setChecked(true);

        return point;
    }

    private static String getYandexAddressComponent(List<YandexPointWrapper.Component> components, String name) {
        for (YandexPointWrapper.Component component : components) {
            if (component.getKind().equalsIgnoreCase(name))
                return component.getName();
        }
        return null;
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
