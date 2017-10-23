package ru.sedi.customerclient.ServerManager;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import ru.sedi.customerclient.classes.Customer._Balance;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.enums.SediJsonObject;
import ru.sedi.customerclient.NewDataSharing.BankCard;
import ru.sedi.customerclient.NewDataSharing.PaymentSystems;
import ru.sedi.customerclient.NewDataSharing._Bill;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._Error;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._OrderRegisterAnswer;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.NewDataSharing._TariffServiceData;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;

public class ParserManager {

    private static String getOriginalJson(String json, String objectName) throws Exception {
        try {
            LogUtil.log(LogUtil.INFO, "Объект:%s; Cтрока:%s", objectName, json);
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

    public static _LoginInfo parseLogin(Server server) throws Exception {
        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.LoginInfo);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _LoginInfo info = gson.fromJson(s, _LoginInfo.class);
        return info;
    }

    public static _LoginInfo parseAuth(Server server) throws Exception {
        _LoginInfo info = parseLogin(server);
        if (info != null)
            info.setUserKey(server.getUserKey());
        return info;
    }

    public static _Balance parseBalance(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Balance);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _Balance balance = gson.fromJson(s, _Balance.class);
        return balance;
    }

    public static _Bill parseBill(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Bills);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _Bill[] bills = gson.fromJson(s, _Bill[].class);
        if (bills.length <= 0)
            throw new Exception("Получена пустая структура на запрос :" + SediJsonObject.Bills);
        return bills[0];
    }

    public static _TariffServiceData parseTariffServiceData(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        Gson gson = new Gson();
        _TariffServiceData data = gson.fromJson(s, _TariffServiceData.class);
        return data;
    }

    public static QueryList<_Tariff> parseCalculateCost(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Tariffs);
        if (TextUtils.isEmpty(s))
            return null;

        //Parse tariffs
        Gson gson = new Gson();
        _Tariff[] data = gson.fromJson(s, _Tariff[].class);

        //Update Duration
        s = getOriginalJson(server.getJson(), SediJsonObject.Duration);
        _OrderRegistrator.me().getOrder().setDuration(gson.fromJson(s, Integer.class));

        //Update Distance
        s = getOriginalJson(server.getJson(), SediJsonObject.Distance);
        _OrderRegistrator.me().getOrder().setDistance(gson.fromJson(s, Integer.class));

        return new QueryList<>(data);
    }

    public static QueryList<_Order> parseOrders(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Orders);
        if (TextUtils.isEmpty(s))
            return new QueryList<>();

        Gson gson = new Gson();
        _Order[] data = gson.fromJson(s, _Order[].class);
        return new QueryList<>(data);
    }

    public static _OrderRegisterAnswer parseOrderRegisterAnswer(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _OrderRegisterAnswer data = gson.fromJson(s, _OrderRegisterAnswer.class);
        return data;
    }

    public static PaymentSystems parsePaymentSystems(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        PaymentSystems data = gson.fromJson(s, PaymentSystems.class);
        return data;
    }

    public static boolean parseStandart(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        return server.isSuccess();
    }

    public static BankCard[] parseBankCards(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Cards);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        BankCard[] data = gson.fromJson(s, BankCard[].class);
        return data;
    }

    public static _Point parseAddress(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        LogUtil.log(LogUtil.INFO, "return: " + s);
        s = getOriginalJson(s, SediJsonObject.Address);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _Point data = gson.fromJson(s, _Point.class);
        data.setChecked(true);
        return data;
    }

    public static _Driver[] parseDrivers(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        s = getOriginalJson(s, SediJsonObject.Drivers);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _Driver[] data = gson.fromJson(s, _Driver[].class);
        return data;
    }

    public static _Point parseCheckAddress(Server server) throws Exception {
        if (!server.isSuccess())
            throw new Exception(server.getResponceMessage());

        String s = server.getJson();
        LogUtil.log(LogUtil.INFO, "return: " + s);
        s = getOriginalJson(s, SediJsonObject.Addresses);
        if (TextUtils.isEmpty(s))
            return null;

        Gson gson = new Gson();
        _Point[] data = gson.fromJson(s, _Point[].class);
        if(data == null || data.length <= 0)
            return null;
        _Point point = data[0];
        point.setChecked(true);
        return point;
    }

    public static String parseShortUrl(Server server) throws Exception {
        String s = server.getJson();
        LogUtil.log(LogUtil.INFO, "return: " + s);
        s = getOriginalJson(s, SediJsonObject.ShortUrl);
        return s;
    }
}
