package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

public class NameId {
    private String Name;
    private String ID;

    public NameId(String name, String ID) {
        Name = name;
        this.ID = ID;
    }

    public NameId() {
    }

    public NameId(NameId obj) {
        Name = obj.getName();
        ID = obj.getID();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getID() {
        if (TextUtils.isEmpty(ID)) return "";
        return ID.trim();
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
