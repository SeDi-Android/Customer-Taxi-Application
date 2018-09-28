package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LatLong;

public class _GeoPoint {
    private double Lat;
    private double Lon;

    public _GeoPoint(double lat, double lon) {
        Lat = lat;
        Lon = lon;
    }

    public _GeoPoint() {
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLon() {
        return Lon;
    }

    public void setLon(double lon) {
        Lon = lon;
    }

    public LatLong toLatLong() {
        return new LatLong(getLat(), getLon());
    }

    @Override
    public String toString() {
        return "GeoPoint{" +
                "Lat=" + Lat +
                ", Lon=" + Lon +
                '}';
    }

    public boolean isValid() {
        return getLat() != 0 && getLon() != 0;
    }
}
