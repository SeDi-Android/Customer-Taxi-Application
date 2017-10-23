package ru.sedi.customerclient.classes.GeoLocation.YandexGeoTools;

import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;

/**
 * User: Stalker
 * Date: 06.03.14
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class YandexGeoToolsHelper {
    private static YandexGeoToolsHelper ourInstance = new YandexGeoToolsHelper();

    public static YandexGeoToolsHelper getInstance() {
        return ourInstance;
    }

    private YandexGeoToolsHelper() {
    }

    private String checkAddressInYandexGeoCoder(String address) {
        StringBuilder builder = new StringBuilder();
        /*HttpClient client = new DefaultHttpClient();
        StringBuilder sb = new StringBuilder();
        sb.append("http://geocode-maps.yandex.ru/1.x/?format=json&sco=latlong&geocode=");
        sb.append(URLEncoder.encode(address));

        HttpGet httpGet = new HttpGet(sb.toString());
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (ClientProtocolException e) {
            BaseActivity.Instance.showDebugMessage(17, e);
        } catch (IOException e) {
            BaseActivity.Instance.showDebugMessage(18, e);
        }*/
        return builder.toString();
    }

    public QueryList<_Point> findAddress(String address) {
        long startDate = System.currentTimeMillis();
        QueryList<_Point> findedAddreses = new QueryList<_Point>();
        return findedAddreses;
        /*final String jsonResponce = checkAddressInYandexGeoCoder(address);
        try {
            JSONObject jsonObject = new JSONObject(jsonResponce);
            jsonObject = (JSONObject) jsonObject.get("response");
            jsonObject = (JSONObject) jsonObject.get("GeoObjectCollection");
            JSONArray jsonArray = (JSONArray) jsonObject.get("featureMember");

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String localityName = "", thoroughfareName = "",
                            premiseNumber = "", kind = "", addressDesc = "";

                    jsonObject = (JSONObject) jsonArray.getJSONObject(i);
                    jsonObject = (JSONObject) jsonObject.get("GeoObject");
                    JSONObject Point = (JSONObject) jsonObject.get("Point");
                    String[] names = jsonObject.getString("name").split(",");
                    if (names.length >= 1)
                        thoroughfareName = names[0];
                    if (names.length >= 2)
                        premiseNumber = names[1];

                    jsonObject = (JSONObject) jsonObject.get("metaDataProperty");
                    jsonObject = (JSONObject) jsonObject.get("GeocoderMetaData");
                    kind = jsonObject.getString("kind");
                    addressDesc = jsonObject.getString("text");

                    jsonObject = (JSONObject) jsonObject.get("AddressDetails");
                    jsonObject = (JSONObject) jsonObject.get("Country");
                    jsonObject = (JSONObject) jsonObject.get("AdministrativeArea");
                    jsonObject = (JSONObject) jsonObject.get("SubAdministrativeArea");
                    if (jsonObject.has("SubAdministrativeAreaName"))
                        localityName = jsonObject.getString("SubAdministrativeAreaName");
                    jsonObject = (JSONObject) jsonObject.get("Locality");
                    if (jsonObject.has("LocalityName"))
                        localityName = jsonObject.getString("LocalityName");

                    final SediAddress newAddress = new SediAddress(-1, localityName, thoroughfareName, premiseNumber,
                            "", kind, true, LatLong.fromStringWithRevert(Point.getString("pos"), " "), addressDesc);

                    if (findedAddreses.Contains(new IWhere<SediAddress>() {
                        @Override
                        public boolean Condition(SediAddress item) {
                            return item.asString().equalsIgnoreCase(newAddress.asString());
                        }
                    }))
                        continue;
                    findedAddreses.add(newAddress);

                } catch (Exception e) {
                    LogUtil.log(LogUtil.ERROR, e.getMessage());
                }
            }
            LogUtil.log(LogUtil.INFO, "Yandex geocoder execute on: %d ms", System.currentTimeMillis() - startDate);
        } catch (JSONException e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
        return findedAddreses;*/
    }
}
