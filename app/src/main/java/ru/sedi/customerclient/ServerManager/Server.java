package ru.sedi.customerclient.ServerManager;


import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Error;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Device;
import ru.sedi.customerclient.enums.SediJsonObject;
import ru.sedi.customerclient.enums.ServerCommands;

/**
 * Class: Server
 * Author: RAM / 27.10.2014
 * Description: Обеспечивает работу запросов к SeDi серверу через HTTP
 */
public class Server {

    private final String ERR_EMPTY_JSON = "От сервера получена пустая строка";

    private SyncHttpClient mSyncHttpClient = new SyncHttpClient();
    private _Error mError;
    private String mExecCommand = "";
    private Context mContext;
    private String mUserKey = "";
    private String mApiKey = "";
    private String mJsonString = "";
    private String mUrlChanel = "";
    private String mDefLang = "ru";
    private String mUrlWithParams = "";
    private long mExecutedStartTime;

    public Server(Context context, String userKey, String lang) {
        mContext = context;
        mApiKey = mContext.getString(R.string.sediApiKey);
        mUrlChanel = mContext.getString(R.string.groupChanel);
        mUserKey = userKey;

        if (!TextUtils.isEmpty(lang))
            mDefLang = lang;
    }

    public void getData(final String command, RequestParams params) {
        String urlChanel;
        if (isHttp(mUrlChanel))
            urlChanel = "http://%s/handlers/sedi/api.ashx";
        else
            urlChanel = "https://%s/handlers/sedi/api.ashx";
        String url = String.format(urlChanel, mUrlChanel);

        mExecCommand = command;
        params.add("q", command);
        params.add("dateformat", "iso");
        params.add("apikey", mApiKey);
        params.add("useragent", Device.getInfo(mContext));
        params.add("lang", mDefLang);

        if (mUserKey.length() > 0)
            params.add("userkey", mUserKey);

        mUrlWithParams = url + "?" + params.toString();

        mSyncHttpClient.setConnectTimeout(30000);
        mSyncHttpClient.setTimeout(30000);
        mSyncHttpClient.post(mContext, url, params, getResponseHandler(command));
    }

    public static boolean isHttp(String urlChanel) {
        return urlChanel.contains("test2")
                || urlChanel.contains("sedikg")
                || urlChanel.contains("krasnodar")
                || urlChanel.contains("snt")
                || urlChanel.contains("taxiwedo")
                || urlChanel.contains("busvan");
    }

    /**
     * Запрос и получение ответа
     *
     * @param command команда серверу;
     */
    private AsyncHttpResponseHandler getResponseHandler(final String command) {
        AsyncHttpResponseHandler asyncHttpResponseHandler = new AsyncHttpResponseHandler(Looper.getMainLooper()) {
            @Override
            public boolean getUseSynchronousMode() {
                return true;
            }

            @Override
            public void onStart() {
                super.onStart();
                mExecutedStartTime = System.currentTimeMillis();
                mError = null;
                LogUtil.log(LogUtil.INFO, String.format("Query: %s; URL: %s",
                        mExecCommand,
                        mUrlWithParams));
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                try {
                    mJsonString = new String(responseBody, "UTF-8");

                    if (command.equals(ServerCommands.GET_ACTIVATION_KEY))
                        getActivationKey(headers);

                    LogUtil.log(LogUtil.INFO, "Query: %s; Executed in %d ms.",
                            mExecCommand, System.currentTimeMillis() - mExecutedStartTime);
                } catch (UnsupportedEncodingException e) {
                    LogUtil.log(LogUtil.ERROR, e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                mError = new _Error(error.getMessage(), error.getMessage());
            }

        };
        return asyncHttpResponseHandler;
    }

    /**
     * Получение ключа пользователя из кукисов
     * @param headers
     */
    private void getActivationKey(cz.msebera.android.httpclient.Header[] headers) {
        if (!isSuccess()) return;
        try {
            for (Header header : headers) {
                if (header.getValue().contains("auth=")) {
                    int endIndx = header.getValue().indexOf(";");
                    mUserKey = header.getValue().substring(5, endIndx);
                }
            }
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
        }
    }

    /**
     * Получен ли ответ от сервера
     */
    public boolean isSuccess() {
        try {
            if (mError != null || TextUtils.isEmpty(getJson()))
                return false;

            JSONObject object = new JSONObject(mJsonString);
            if (object.has(SediJsonObject.Success)) {
                boolean success = object.getBoolean(SediJsonObject.Success);
                if (!success) {
                    if (object.has(SediJsonObject.Error)) {
                        mError = new Gson().fromJson(object.getString("Message"), _Error.class);
                    }
                }
                return success;
            }

            return false;
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
            return false;
        }
    }

    /**
     * Получение сообщения в ответе сервера
     */
    public String getResponceMessage() {
        try {
            if (mError != null)
                return mError.getName();

            if (TextUtils.isEmpty(mJsonString))
                return ERR_EMPTY_JSON;

            JSONObject jsonDoc = new JSONObject(mJsonString);
            String serverResponce = jsonDoc.getString("Message");
            return "Server send: " + serverResponce;
        } catch (JSONException e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
            return e.getMessage();
        }
    }


    /**
     * Получение строки с ответом сервера
     */
    public String getJson() {
        return mJsonString;
    }

    /**
     * Получение авторизационного ключа пользователя
     */
    public String getUserKey() {
        return mUserKey;
    }

//ru.sedi.customer.vederko
    public _Error getError() {
        return mError;
    }

    /**
     * Возвращает url для использования в запросах автокомплита в SeDi.
     * @param context context вызывающего.
     * @return строку с url для автокомплита SeDi.
     */
    public static String getAutocompleteUrl(Context context) {
        StringBuilder sb = new StringBuilder();
        String chanelName = context.getString(R.string.groupChanel);
        sb.append(isHttp(chanelName) ? "http://" : "https://");
        sb.append(chanelName);
        sb.append("/handlers/autocomplete.ashx?q=addr&types=street,object,city");
        return sb.toString();
    }
}
