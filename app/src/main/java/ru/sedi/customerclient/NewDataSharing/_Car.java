package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

public class _Car {
    private String Number = "";
    private String[] Properties = new String[]{};
    private int ID = -1;
    private String Name = "";

    public _Car(String number, String[] properties, int ID, String name) {
        Number = number;
        Properties = properties;
        this.ID = ID;
        Name = name;
    }

    public _Car() {
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String[] getProperties() {
        return Properties;
    }

    public void setProperties(String[] properties) {
        Properties = properties;
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

    public String getCarInfo() {
        String carInfo = "";
        if (!TextUtils.isEmpty(getName()))
            carInfo += getName();

        if (!TextUtils.isEmpty(getNumber()))
            carInfo += " " + getNumber();
        return carInfo;
    }
}
