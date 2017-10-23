package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.Geocoder;

import android.text.TextUtils;

import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LatLong;

/**
 * Created by RAM on 29.05.2017.
 */

public class GoogleGeocoderResponse {
    private static final String OK_STATUS = "OK";
    private GoogleGeocoderResults[] results;
    private String status;

    public GoogleGeocoderResults[] getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return !TextUtils.isEmpty(getStatus())
                && getStatus().equalsIgnoreCase(OK_STATUS);
    }

    class GoogleGeocoderResults {
        private GoogleGeocoderAddressComponent[] address_components;
        private String formatted_address;
        private String[] types;
        private String place_id;
        private GoogleGeocoderGeometry geometry;

        public String getAddress() {
            return formatted_address;
        }

        public String[] getTypes() {
            return types;
        }

        public GoogleGeocoderGeometry getGeometry() {
            return geometry;
        }

        public _Point convertToPoint() {
            _Point point = new _Point();
            GoogleGeocoderAddressComponent component;
            if ((component = getByType("country")) != null)
                point.setCountryName(component.getName());
            if ((component = getByType("locality")) != null)
                point.setCityName(component.getName());
            if ((component = getByType("route")) != null)
                point.setStreetName(component.getName());
            if ((component = getByType("street_number")) != null)
                point.setHouseNumber(component.getName());
            if (getGeometry() != null && getGeometry().getLocation() != null) {
                GoogleGeocoderGeometry.GoogleGeocoderLocation location = getGeometry().getLocation();
                point.setGeoPoint(new LatLong(location.getLat(), location.getLng()));
            }
            if (point.isMinimalAddress() && point.getLatLong().isValid())
                point.setChecked(true);
            return point;
        }

        private GoogleGeocoderAddressComponent getByType(String type) {
            for (GoogleGeocoderAddressComponent component : address_components) {
                if (component.containType(type))
                    return component;
            }
            return null;
        }

        private class GoogleGeocoderGeometry {
            private GoogleGeocoderLocation location;

            public GoogleGeocoderLocation getLocation() {
                return location;
            }

            private class GoogleGeocoderLocation {
                private double lat;
                private double lng;

                public double getLat() {
                    return lat;
                }

                public double getLng() {
                    return lng;
                }
            }
        }

        private class GoogleGeocoderAddressComponent {
            private String long_name;
            private String short_name;
            private String[] types;

            public String getName() {
                return long_name;
            }

            public String[] getTypes() {
                return types;
            }

            boolean containType(String type) {
                for (String s : types) {
                    if (s.equals(type))
                        return true;
                }
                return false;
            }
        }
    }
}
