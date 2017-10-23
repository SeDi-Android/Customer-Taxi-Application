package ru.sedi.customerclient.NewDataSharing;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import ru.sedi.customerclient.common.LINQ.QueryList;


public class _Driver {

    private _GeoPoint Geo;
    private _Car Car;
    private QueryList<_Phone> Phones;
    private int ID;
    private String Name;
    private float Rating;


    public _Driver() {
        Geo = new _GeoPoint();
        Car = new _Car();
    }

    public _GeoPoint getGeo() {
        return Geo;
    }

    public _Car getCar() {
        return Car;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public QueryList<_Phone> getPhones() {
        return Phones;
    }

    public _Phone getPhone(@NonNull String phoneType) {
        if (TextUtils.isEmpty(phoneType) || Phones == null || Phones.isEmpty())
            return null;

        return Phones.FirstOrDefault(item -> item.getType().equals(phoneType)
                && !item.getNumber().isEmpty());
    }

    public float getRating() {
        return Rating;
    }

    public boolean isValid() {
        return ID > 0 && getGeo().toLatLong().isValid();
    }
}
