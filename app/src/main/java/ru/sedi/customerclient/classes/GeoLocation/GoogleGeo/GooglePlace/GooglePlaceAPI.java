package ru.sedi.customerclient.classes.GeoLocation.GoogleGeo.GooglePlace;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;

/**
 * Class: GooglePlaceAPI;
 * Name: RAM;
 * Description:
 */
public class GooglePlaceAPI {

    private static final String TAG = GooglePlaceAPI.class.getSimpleName();

    private static final String DEFAULT_GOOGLE_KEY = "AIzaSyAMG5aI7dwaLb63FiPSPMa3DGQhxNKIr8c";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/queryautocomplete";
    private static final String OUT_JSON = "/json";
    private final Context mContext;

    //Use default quota 100000 queries

    private String mGoogleApiKey = "";
    private static final int RADIUS = 5000 * 10;

    public GooglePlaceAPI(Context context) {
        mContext = context;
    }

    public ArrayList<_Point> autocomplete(String input, String language) {
        ArrayList<_Point> resultList = new ArrayList<>();
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(getAutocompliteJson(input, language));
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            Gson gson = new Gson();
            Prediction[] predictions = gson.fromJson(predsJsonArray.toString(), Prediction[].class);
            for (Prediction prediction : predictions) {
                if(!prediction.isEnabledType()) continue;

                AddressStructure structure = prediction.getAddressStructure();
                String desc = String.format("%s, %s", structure.getCity(), structure.getMainText());

                _Point p = new _Point();
                p.setDesc(desc);
                p.setPlaceId(prediction.getPlaceId());
                resultList.add(p);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }



    private String getAutocompliteJson(String input, String language) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        if(TextUtils.isEmpty(mGoogleApiKey)) {
            mGoogleApiKey = getApiKey();
        }

        long startTime = System.currentTimeMillis();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + mGoogleApiKey);
            sb.append("&language=" + language);

            if (LocationService.me().getLocation().isValid()) {
                LatLong location = LocationService.me().getLocation();
                sb.append(String.format(Locale.ENGLISH, "&location=%f,%f", location.Latitude, location.Longitude));
                sb.append(String.format("&radius=%d", RADIUS));
                sb.append("&rankby=distance");
            }
            //sb.append("&types=address");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
            LogUtil.log(LogUtil.INFO, "Google return place info on: %d ms", System.currentTimeMillis() - startTime);
            return jsonResults.toString();
        } catch (MalformedURLException e) {
            LogUtil.log(LogUtil.ERROR, "Error processing Places API URL" + e.toString());
            return "";
        } catch (IOException e) {
            LogUtil.log(LogUtil.ERROR, "Error connecting to Places API" + e.toString());
            return "";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getApiKey() {
        if(mContext == null) return DEFAULT_GOOGLE_KEY;

        String key = mContext.getString(R.string.googleApiKey);
        if (!TextUtils.isEmpty(key))
            return key;
        else
            return DEFAULT_GOOGLE_KEY;
    }
}
