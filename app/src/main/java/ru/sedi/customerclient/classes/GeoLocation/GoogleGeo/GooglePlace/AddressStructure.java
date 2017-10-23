package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.GooglePlace;

/**
 * Created by RAM on 04.04.2017.
 */

public class AddressStructure {
    private String main_text;
    private String secondary_text;

    public AddressStructure() {
    }

    public String getMainText() {
        return main_text;
    }

    public String[] getSecondaryText() {
        return secondary_text.split(", ");
    }

    public String getCity(){
        return getSecondaryText()[0];
    }
}
