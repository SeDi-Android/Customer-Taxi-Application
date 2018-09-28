package ru.sedi.customerclient.classes;

public class PartnerMessageHolder {
    private String mCutomerIndividualMessage;
    private String mDriverIndividualMessage;
    private String mCustomerSimpleMessage;
    private String mDriverSimpleMessage;

    public String getCutomerIndividualMessage() {
        return mCutomerIndividualMessage;
    }

    public void setCutomerIndividualMessage(String cutomerIndividualMessage) {
        mCutomerIndividualMessage = cutomerIndividualMessage;
    }

    public String getDriverIndividualMessage() {
        return mDriverIndividualMessage;
    }

    public void setDriverIndividualMessage(String driverIndividualMessage) {
        mDriverIndividualMessage = driverIndividualMessage;
    }

    public String getCustomerSimpleMessage() {
        return mCustomerSimpleMessage;
    }

    public void setCustomerSimpleMessage(String customerSimpleMessage) {
        mCustomerSimpleMessage = customerSimpleMessage;
    }

    public String getDriverSimpleMessage() {
        return mDriverSimpleMessage;
    }

    public void setDriverSimpleMessage(String driverSimpleMessage) {
        mDriverSimpleMessage = driverSimpleMessage;
    }
}
