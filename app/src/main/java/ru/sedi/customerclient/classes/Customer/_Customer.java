package ru.sedi.customerclient.classes.Customer;

public class _Customer {
    private String Phone;
    private int ID;
    private String Name;

    public _Customer(String phone, int ID, String name) {
        Phone = phone;
        this.ID = ID;
        Name = name;
    }

    public _Customer() {
    }

    public _Customer(_Customer customer) {
        Phone = customer.getPhone();
        ID = customer.getID();
        Name = customer.getName();
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
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
}
