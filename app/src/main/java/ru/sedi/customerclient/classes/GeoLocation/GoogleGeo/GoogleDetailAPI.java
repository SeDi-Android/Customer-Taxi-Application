package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.GooglePlace.GooglePlaceAPI;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class GoogleDetailAPI {
    private static final String TAG = GooglePlaceAPI.class.getSimpleName();

    public static final String JSON_PARSE_ERROR = "Failed to parse JSON data!";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/details";
    private static final String OUT_JSON = "/json";

    //Use default quota 100000 queries
    private String mGoogleApiKey = "AIzaSyAMG5aI7dwaLb63FiPSPMa3DGQhxNKIr8c";

    /**
     * Get address detail by reference ID.
     * @param reference - reference ID.
     */
    public _Point detail(String reference) {

        _Point sediAddress = new _Point();
        String language = Prefs.getString(PrefsName.LOCALE_CODE);
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        String key = App.getInstance().getString(R.string.googleApiKey);
        if (!TextUtils.isEmpty(key))
            mGoogleApiKey = key;

        long startTime = System.currentTimeMillis();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + mGoogleApiKey);
            sb.append("&placeid=" + URLEncoder.encode(reference, "utf8"));
            sb.append("&language=" + URLEncoder.encode(language, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

            LogUtil.log(LogUtil.INFO, "Google return detail info on: %d ms", System.currentTimeMillis() - startTime);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Detail API URL", e);
            return sediAddress;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Detail API", e);
            return sediAddress;
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            jsonObj = jsonObj.getJSONObject("result");

            //Get sediAddress details
            addressComponentsFromJSON(sediAddress, jsonObj);

            //Get location details
            addressLocationFromJSON(sediAddress, jsonObj);
            sediAddress.setChecked(true);
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sediAddress;
    }

    //<editor-fold desc="Detail support methods">
    private void addressLocationFromJSON(_Point result, JSONObject address) throws IOException {
        //Declare
        JSONObject addressLocation;

        try {
            addressLocation = address.getJSONObject("geometry").getJSONObject("location");
            result.setGeoPoint(new LatLong(addressLocation.getDouble("lat"), addressLocation.getDouble("lng")));
        } catch (JSONException e) {
            // Throw exception
            throw new IOException(JSON_PARSE_ERROR);
        }
    }

    private void addressComponentsFromJSON(_Point result, JSONObject address) throws IOException {
        // Declare
        JSONObject addressComponent;

        // Parse address_components array
        JSONArray addressComponents;
        try {
            // Create address components
            addressComponents = address.getJSONArray("address_components");

            // Street Number
            addressComponent = getAddressComponent(addressComponents, "street_number");
            String streetNumber = getJSONStringField(addressComponent, "long_name");
            if (streetNumber != null)
                result.setHouseNumber(streetNumber);
            // Route
            addressComponent = getAddressComponent(addressComponents, "route");
            String route = getJSONStringField(addressComponent, "long_name");
            if (route != null)
                result.setStreetName(route);
            else
                result.setObjectName(address.getString("name"));
                //result.setStreetName(address.getString("name"));

            if (streetNumber == null && (route != null && !route.equalsIgnoreCase(address.getString("name"))))
                result.setStreetName(result.getStreetName() + " (" + address.getString("name") + ")");
                //result.setStreetName(result.getStreetName() + " (" + address.getString("name") + ")");

            // Locality
            addressComponent = getAddressComponent(addressComponents, "locality");
            String locality = getJSONStringField(addressComponent, "long_name");
            if (locality != null)
                result.setCityName(locality);

            addressComponent = getAddressComponent(addressComponents, "country");
            String country = getJSONStringField(addressComponent, "long_name");
            if (country != null)
                result.setCountryName(country);

        } catch (JSONException e) {
            // Throw exception
            throw new IOException(JSON_PARSE_ERROR);
        }
    }

    private JSONObject getAddressComponent(JSONArray componentArray, String componentType) throws IOException {
        // Go through array looking for specified type
        for (int i = 0; i < componentArray.length(); i++) {
            // Declare
            JSONObject component;

            try {
                // Get address component
                component = componentArray.getJSONObject(i);

                // Get types array
                JSONArray componentTypes = component.getJSONArray("types");

                // Check if we have the correct type
                if (isTypeInTypeArray(componentType, componentTypes)) {
                    return component;
                }
            } catch (JSONException e) {
                // Throw exception
                throw new IOException(JSON_PARSE_ERROR);
            }
        }

        // Return
        return null;
    }

    private boolean isTypeInTypeArray(String type, JSONArray typeArray) throws IOException {
        // Parameter check
        if (type == null || typeArray == null) return false;

        // Check if we have the desired type in the types JSON array
        for (int i = 0; i < typeArray.length(); i++) {
            try {
                if (typeArray.getString(i).equals(type)) {
                    return true;
                }
            } catch (JSONException e) {
                // Throw exception
                throw new IOException(JSON_PARSE_ERROR);
            }
        }

        // Return
        return false;
    }

    private String getJSONStringField(JSONObject json, String field) {
        // Check parameters
        if (json == null || field == null) return null;

        // Locate field
        try {
            return json.getString(field);
        } catch (JSONException e) {
            return null;
        }
    }
    //</editor-fold>
}
