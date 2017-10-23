package ru.sedi.customerclient.NewDataSharing;

public class _Service{
    private String Name;
    private String ID;
    private _Cost Cost = new _Cost(0, "");
    private boolean mChecked;

    public _Service() {
    }

    public _Cost getCost() {
        return Cost;
    }

    public void setCost(_Cost cost) {
        Cost = cost;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
