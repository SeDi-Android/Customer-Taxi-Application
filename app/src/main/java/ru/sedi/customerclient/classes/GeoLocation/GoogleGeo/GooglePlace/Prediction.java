package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.GooglePlace;

import ru.sedi.customerclient.common.LINQ.QueryList;

public class Prediction {
    private String description;
    private String id;
    private String place_id;
    private String reference;
    private QueryList<Terms> terms = new QueryList<>();
    private QueryList<String> types = new QueryList<>();
    private AddressStructure structured_formatting;

    public Prediction() {
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getPlaceId() {
        return place_id;
    }

    public String getReference() {
        return reference;
    }

    public QueryList<Terms> getTerms() {
        return terms;
    }

    public boolean isEnabledType(){
        return types.Contains(item -> item.equals("route") || item.equals("street_address"));
    }

    public AddressStructure getAddressStructure() {
        return structured_formatting;
    }
}
