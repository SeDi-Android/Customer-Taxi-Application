package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

public class _Bill {
    private String ID;
    private NameId Service;
    private NameId Status;
    private String Date;
    private String DateLife;
    private double Sum;
    private String Comment;
    private String PayUrl;
    private String PayeePurse;
    private Payer Payer;

    public _Bill() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public NameId getService() {
        return Service;
    }

    public void setService(NameId service) {
        Service = service;
    }

    public NameId getStatus() {
        return Status;
    }

    public void setStatus(NameId status) {
        Status = status;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDateLife() {
        return DateLife;
    }

    public void setDateLife(String dateLife) {
        DateLife = dateLife;
    }

    public double getSum() {
        return Sum;
    }

    public void setSum(double sum) {
        Sum = sum;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getPayUrl() {
        return PayUrl;
    }

    public void setPayUrl(String payUrl) {
        PayUrl = payUrl;
    }

    public String getPayeePurse() {
        return PayeePurse;
    }

    public String getPayerPhone() {
        if (Payer == null || TextUtils.isEmpty(Payer.getPhone()))
            return "";
        return Payer.getPhone();
    }

    public boolean isQiwiWallet() {
        NameId service = getService();
        return service !=null
                && !TextUtils.isEmpty(service.getID())
                && service.getID().equals("qiwiwallet");
    }
}
