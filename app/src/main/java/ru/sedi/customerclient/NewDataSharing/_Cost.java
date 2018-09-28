package ru.sedi.customerclient.NewDataSharing;

/**
 * Created by RAM on 23.10.2015.
 */
public class _Cost {
    private double Value;
    private String Unit;

    public _Cost(double value, String unit) {
        Value = value;
        Unit = unit;
    }

    public _Cost() {
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }
}
