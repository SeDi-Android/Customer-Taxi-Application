package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LINQ.QueryList;

public class _Owner {
    private int ID;
    private String Name;
    private String Url;
    private double DefaultCommission;
    private String Email;
    private QueryList<String> Phones = new QueryList<>();

    public _Owner(int ID, String name, String url, double defaultCommission, QueryList<String> phones) {
        this.ID = ID;
        Name = name;
        Url = url;
        DefaultCommission = defaultCommission;
        Phones = phones;
    }

    public _Owner() {
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

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public QueryList<String> getPhones() {
        return Phones;
    }

    public void setPhones(QueryList<String> phones) {
        Phones = phones;
    }

    public double getDefaultCommission() {
        return DefaultCommission;
    }

    public void setDefaultCommission(double defaultCommission) {
        DefaultCommission = defaultCommission;
    }

    public String getEmail() {
        return Email;
    }
}
