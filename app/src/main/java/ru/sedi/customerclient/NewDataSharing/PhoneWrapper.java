package ru.sedi.customerclient.NewDataSharing;

/**
 * Created by RAM on 11.05.2017.
 */

public class PhoneWrapper {
    private _Phone mPhone;
    private String mDesc;

    public PhoneWrapper(_Phone phone, String desc) {
        mPhone = phone;
        mDesc = desc;
    }

    public _Phone getPhone() {
        return mPhone;
    }

    public String getDesc() {
        return mDesc;
    }

    @Override
    public String toString() {
        return getDesc();
    }
}
