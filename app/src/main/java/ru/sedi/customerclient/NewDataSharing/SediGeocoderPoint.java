package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LatLong;

public class SediGeocoderPoint {



    class GeocoderGeopoint {
        private double Latitude;
        private double Longitude;

        public _GeoPoint toDefaultGeopoint() {
            return new _GeoPoint(Latitude, Longitude);
        }
    }

    private String CountryName;
    private String PostalCode;
    private String CityName;
    private String StreetName;
    private String HouseNumber;
    private String ObjectName;
    private int EntranceNumber;
    private GeocoderGeopoint GeoPoint;
    private boolean Coordinatesonly;
    private int Id;

    public LatLong getLatLong() {
        return new LatLong(GeoPoint.Latitude, GeoPoint.Longitude);
    }

    public _Point toDefaultPoint() {
        _Point point = new _Point();
        point.setChecked(true);
        point.setCountryName(this.CountryName);
        point.setCityName(this.CityName);
        point.setPostalCode(this.PostalCode);
        point.setStreetName(this.StreetName);
        point.setHouseNumber(this.HouseNumber);
        point.setObjectName(this.ObjectName);
        point.setEntranceNumber(this.EntranceNumber);
        point.setGeoPoint(this.GeoPoint.toDefaultGeopoint());
        point.setCoordinatesonly(this.Coordinatesonly);
        point.setID(this.Id);
        return point;
    }
}
