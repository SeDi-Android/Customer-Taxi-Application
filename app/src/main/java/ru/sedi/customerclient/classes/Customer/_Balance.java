package ru.sedi.customerclient.classes.Customer;

public class _Balance {
    private String Currency = "₱";// валюта счета
    private double Credit; // допустимый кредит
    private double Value; // баланс лицевого счета
    private double Locked;
    private double mLocked;

    public _Balance(String currency, double credit, double value, double locked) {
        Currency = currency;
        Credit = credit;
        Value = value;
        Locked = locked;
    }

    public _Balance() {
    }

    public String getCurrency() {
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public double getCredit() {
        return Credit;
    }

    public void setCredit(double credit) {
        Credit = credit;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public double getBalance(){
        return (Value + Credit) - Locked;
    }

    public double getLocked() {
        return mLocked;
    }
}
