package ru.sedi.customerclient.NewDataSharing;

public class _Phone {
    public static final String MOBILE_WORK = "mobilework";
    public static final String DISPATCHER = "dispatcherphone";


    private String Number;
    private String Type;

    public _Phone(String number, String type) {
        Number = number;
        Type = type;
    }

    public _Phone() {
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
