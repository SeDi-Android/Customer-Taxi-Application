package ru.sedi.customerclient.NewDataSharing;

public class PaymentSystem {
    private NameId PaymentSystem;
    private boolean Enabled;
    private double Customer;
    private double Employee;
    private boolean Recurrent;
    private String Url;

    public PaymentSystem() {
    }

    public PaymentSystem(NameId paymentSystem, boolean enabled, double customer, double employee, boolean recurrent, String url) {
        PaymentSystem = paymentSystem;
        Enabled = enabled;
        Customer = customer;
        Employee = employee;
        Recurrent = recurrent;
        Url = url;
    }

    public NameId getPaymentSystem() {
        return PaymentSystem;
    }

    public boolean isEnabled() {
        return Enabled;
    }

    public double getCustomer() {
        return Customer;
    }

    public double getEmployee() {
        return Employee;
    }

    public boolean isRecurrent() {
        return Recurrent;
    }

    public String getUrl() {
        return Url;
    }

    @Override
    public String toString() {
        return PaymentSystem.getName();
    }
}
