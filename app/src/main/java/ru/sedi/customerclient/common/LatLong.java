package ru.sedi.customerclient.common;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.Locale;

public class LatLong implements Serializable {


    public double Latitude;
    public double Longitude;

    public LatLong() {

    }

    public LatLong(double latitude, double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public boolean equals(LatLong obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        return Math.abs(obj.Latitude - Latitude) < 1e-5 &&
                Math.abs(obj.Longitude - Longitude) < 1e-5;
    }

    public boolean isValid() {
        return (Latitude!=0.0 && Longitude !=0.0) && (Latitude!=200 && Longitude !=200);
    }

    public GeoPoint toGeopoint() {
        return new GeoPoint(Latitude, Longitude);
    }

    public String toString(){
        return String.format(Locale.ENGLISH, "%f, %f", Latitude, Longitude);
    }

}
