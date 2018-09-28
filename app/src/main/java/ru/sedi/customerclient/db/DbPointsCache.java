package ru.sedi.customerclient.db;

import io.realm.RealmObject;
import ru.sedi.customerclient.NewDataSharing._GeoPoint;
import ru.sedi.customerclient.NewDataSharing._Point;

public class DbPointsCache extends RealmObject {
    private String CountryName;
    private String PostalCode;
    private String CityName;
    private String StreetName;
    private String HouseNumber;
    private String ObjectName;
    private String PlaceId;
    private String Description;
    private boolean Checked;
    private int EntranceNumber;
    private double Latitude;
    private double Longetude;
    private int ID;
    private String mType;


    public DbPointsCache() {
    }

    public DbPointsCache(_Point point) {
        CountryName = point.getCountryName();
        PostalCode = point.getPostalCode();
        CityName = point.getCityName();
        StreetName = point.getStreetName();
        HouseNumber = point.getHouseNumber();
        PlaceId = point.getPlaceId();
        Description = point.getDesc();
        Checked = true;
        EntranceNumber = point.getEntranceNumber();
        _GeoPoint geoPoint = point.getGeoPoint();
        Latitude = geoPoint.getLat();
        Longetude = geoPoint.getLon();
        ID = point.getID();
        if (point.getType() != null)
            mType = point.getType().name();
    }

    @Override
    public String toString() {
        return Description;
    }


    /*public _Point toPoint() {
        return new _Point(this);
    }*/


    public String getCountryName() {
        return CountryName;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public String getCityName() {
        return CityName;
    }

    public String getStreetName() {
        return StreetName;
    }

    public String getHouseNumber() {
        return HouseNumber;
    }

    public String getObjectName() {
        return ObjectName;
    }

    public String getPlaceId() {
        return PlaceId;
    }

    public int getEntranceNumber() {
        return EntranceNumber;
    }

    public double getLongetude() {
        return Longetude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public int getId() {
        return ID;
    }

    public String getType() {
        return mType;
    }
}
