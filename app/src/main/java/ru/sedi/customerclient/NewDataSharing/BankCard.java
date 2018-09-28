package ru.sedi.customerclient.NewDataSharing;

public class BankCard {
    private boolean IsEnabled;
    private NameId Service;
    private String Name;
    private String ID;

    public BankCard(boolean isEnabled, NameId service, String name, String ID) {
        IsEnabled = isEnabled;
        Service = service;
        Name = name;
        this.ID = ID;
    }

    public BankCard() {
    }

    public boolean isEnabled() {
        return IsEnabled;
    }

    public NameId getService() {
        return Service;
    }

    public String getName() {
        return Name;
    }

    public String getID() {
        return ID;
    }

    public void setIsEnabled(boolean isEnabled) {
        IsEnabled = isEnabled;
    }
}
