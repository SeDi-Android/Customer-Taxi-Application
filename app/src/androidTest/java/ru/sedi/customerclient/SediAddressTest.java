package ru.sedi.customerclient;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customerclient.NewDataSharing._Error;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.enums.SediJsonObject;

@RunWith(AndroidJUnit4.class)
public class SediAddressTest {

    @Test
    public void checkAddress() {

        OkHttpClient httpClient = new OkHttpClient();

        String urlFormat = "https://api.msk.sedi.ru/handlers/sedi/api.ashx?addr=%s&apikey=fc85d178-dd76-4aee-8664-a36682dd3f77" +
                "&dateformat=iso&lang=ru&q=check_address";

        String[] ads = {"Москва, нагорная 21к1", "Москва, мира 44", "Москва, ивантеевская 5", "Москва, краснопролетарская 16", "Москва, 1-й самотечный переулок 16/2с9", "Москва, первый самотечный 16/2с9",
                "Москва, палиха 10с9", "Москва, Лесная 20"};


        for (String ad : ads) {
            Request request = new Request.Builder()
                    .url(String.format(urlFormat, ad))
                    .build();
            try {
                Response execute = httpClient.newCall(request).execute();
                if (!execute.isSuccessful()) {
                    Log.e("TAPP - Not success ", "for address" + ad);
                    continue;
                }

                String s = execute.body().string();
                if (TextUtils.isEmpty(s)) {
                    Log.e("TAPP - Empty responce", "for address" + ad);
                    continue;
                }
                _Point point;
                try {
                    point = new Gson().fromJson(getOriginalJson(s, SediJsonObject.Address), _Point.class);
                } catch (Exception e) {
                    Log.e("TAPP - Bad responce", e.getMessage());
                    continue;
                }
                //System.out.println(point.asString());
                Log.i("TAPP - Is success", point.asString());
            } catch (IOException e) {
                Log.i("TAPP - Error", e.getMessage());
            }
        }

    }

    private static String getOriginalJson(String json, String objectName) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("Error")) {
                Gson s = new Gson();
                _Error error = s.fromJson(jsonObject.get("Error").toString(), _Error.class);
                throw new Exception(error.getName());
            }

            if (!jsonObject.has(objectName))
                return "";
            String s = jsonObject.get(objectName).toString();
            return s;
        } catch (JSONException e) {
            LogUtil.log(e);
            return "";
        }

    }
}
