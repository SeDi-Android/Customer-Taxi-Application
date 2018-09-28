package ru.sedi.customerclient.classes.GeoLocation;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadLeg;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import ru.sedi.customerclient.classes.App;

public class SediOsmrManager extends OSRMRoadManager {

    private boolean mUseShortInfo;
    private final String BASE_URL = "http://osrm.sedi.ru:5005/route/v1/driving/";
    private final String TAXILIVE_URL = "http://taxilive.ch:5000/route/v1/driving/";


    public SediOsmrManager(Context context, boolean useShortInfo) {
        super(context);
        super.setService(App.isExcludedApp ? TAXILIVE_URL : BASE_URL);
        mUseShortInfo = useShortInfo;
    }

    @Override
    protected String getUrl(ArrayList<GeoPoint> waypoints, boolean getAlternate) {
        StringBuilder urlString = new StringBuilder(mServiceUrl);
        for (int i = 0; i < waypoints.size(); i++) {
            GeoPoint p = waypoints.get(i);
            if (i > 0)
                urlString.append(';');
            urlString.append(Double.toString(p.getLongitude()) + "," + Double.toString(p.getLatitude()));
        }
        urlString.append("?alternatives=" + (getAlternate ? "true" : "false"));
        if (mUseShortInfo)
            urlString.append("&overview=false");
        else
            urlString.append("&overview=full&steps=true");
        urlString.append(mOptions);
        return urlString.toString();
    }

    @Override
    protected Road[] getRoads(ArrayList<GeoPoint> waypoints, boolean getAlternate) {
        String url = getUrl(waypoints, getAlternate);
        Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoads:" + url);
        String jString = BonusPackHelper.requestStringFromUrl(url, mUserAgent);
        if (jString == null) {
            Log.e(BonusPackHelper.LOG_TAG, "OSRMRoadManager::getRoad: request failed.");
            return defaultRoad(waypoints);
        }

        try {
            JSONObject jObject = new JSONObject(jString);
            String jCode = jObject.getString("code");
            if (!"Ok".equals(jCode)) {
                Log.e(BonusPackHelper.LOG_TAG, "OSRMRoadManager::getRoad: OnFailureCalculate code=" + jCode);
                Road[] roads = defaultRoad(waypoints);
                if ("NoRoute".equals(jCode)) {
                    roads[0].mStatus = Road.STATUS_INVALID;
                }
                return roads;
            } else {
                JSONArray jRoutes = jObject.getJSONArray("routes");
                Road[] roads = new Road[jRoutes.length()];
                for (int i = 0; i < jRoutes.length(); i++) {
                    Road road = new Road();
                    roads[i] = road;
                    road.mStatus = Road.STATUS_OK;
                    JSONObject jRoute = jRoutes.getJSONObject(i);
                    String route_geometry = "";
                    if (jRoute.has("geometry"))
                        route_geometry = jRoute.getString("geometry");
                    if (!route_geometry.isEmpty()) {
                        road.mRouteHigh = PolylineEncoder.decode(route_geometry, 10, false);
                        road.mBoundingBox = BoundingBoxE6.fromGeoPoints(road.mRouteHigh);
                    }
                    road.mLength = jRoute.getDouble("distance") / 1000.0;
                    road.mDuration = jRoute.getDouble("duration");
                    //legs:
                    JSONArray jLegs = jRoute.getJSONArray("legs");
                    for (int l = 0; l < jLegs.length(); l++) {
                        //leg:
                        JSONObject jLeg = jLegs.getJSONObject(l);
                        RoadLeg leg = new RoadLeg();
                        road.mLegs.add(leg);
                        leg.mLength = jLeg.getDouble("distance");
                        leg.mDuration = jLeg.getDouble("duration");
                        //steps:
                        JSONArray jSteps = jLeg.getJSONArray("steps");
                        RoadNode lastNode = null;
                        String lastRoadName = "";
                        for (int s = 0; s < jSteps.length(); s++) {
                            JSONObject jStep = jSteps.getJSONObject(s);
                            RoadNode node = new RoadNode();
                            node.mLength = jStep.getDouble("distance") / 1000.0;
                            node.mDuration = jStep.getDouble("duration");
                            JSONObject jStepManeuver = jStep.getJSONObject("maneuver");
                            JSONArray jLocation = jStepManeuver.getJSONArray("location");
                            node.mLocation = new GeoPoint(jLocation.getDouble(1), jLocation.getDouble(0));
                            String direction = jStepManeuver.getString("type");
                            if (direction.equals("turn") || direction.equals("ramp") || direction.equals("merge")) {
                                String modifier = jStepManeuver.getString("modifier");
                                direction = direction + '-' + modifier;
                            } else if (direction.equals("roundabout")) {
                                int exit = jStepManeuver.getInt("exit");
                                direction = direction + '-' + exit;
                            } else if (direction.equals("rotary")) {
                                int exit = jStepManeuver.getInt("exit");
                                direction = "roundabout" + '-' + exit; //convert rotary in roundabout...
                            }
                            node.mManeuverType = getManeuverCode(direction);
                            String roadName = jStep.optString("name", "");
                            node.mInstructions = buildInstructions(node.mManeuverType, roadName);
                            if (lastNode != null && node.mManeuverType == 2 && lastRoadName.equals(roadName)) {
                                //workaround for https://github.com/Project-OSRM/osrm-backend/issues/2273
                                //"new name", but identical to previous name:
                                //skip, but update values of last node:
                                lastNode.mDuration += node.mDuration;
                                lastNode.mLength += node.mLength;
                            } else {
                                road.mNodes.add(node);
                                lastNode = node;
                                lastRoadName = roadName;
                            }
                        } //steps
                    } //legs
                } //routes
                Log.d(BonusPackHelper.LOG_TAG, "OSRMRoadManager.getRoads - finished");
                return roads;
            } //if code is Ok
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultRoad(waypoints);
        }
    }

    @Override
    public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
        return getRoads(waypoints, true);
    }

    @Override
    public Road getRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = getRoads(waypoints, false);
        return roads[0];
    }

}
