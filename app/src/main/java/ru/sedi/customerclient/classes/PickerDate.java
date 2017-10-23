package ru.sedi.customerclient.classes;

import ru.sedi.customerclient.common.DateTime;

public class PickerDate {

    private DateTime mDateTime;
    private String mValue;

    public PickerDate(DateTime dateTime, String value) {
        mDateTime = dateTime;
        mValue = value;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public String getValue() {
        return mValue;
    }
}
