package ru.sedi.customerclient.ServerManager;

import android.content.Context;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;

import java.net.URLEncoder;

import ru.sedi.customerclient.NewDataSharing.BankCard;
import ru.sedi.customerclient.NewDataSharing.CostCalculationResult;
import ru.sedi.customerclient.NewDataSharing.PaymentSystems;
import ru.sedi.customerclient.NewDataSharing._Bill;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._GeoPoint;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._OrderRegisterAnswer;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.NewDataSharing._TariffServiceData;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Customer._Balance;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.InvitationTypes;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.enums.ServerCommands;
import ru.sedi.customerclient.enums.UserTypes;

public class ServerManager {
    public static final String LOGININCORRECT = "loginincorrect";
    public static final String USERNOTFOUND = "usernotfound";
    private static ServerManager ourInstance = new ServerManager();

    public static ServerManager GetInstance() {
        return ourInstance;
    }

    private ServerManager() {
    }

    private Server serverQuery(String cmd, RequestParams params) {
        Server server = new Server(App.getInstance().getBaseContext(),
                Prefs.getString(PrefsName.USER_KEY),
                Prefs.getString(PrefsName.LOCALE_CODE));
        server.getData(cmd, params);
        return server;
    }

    /**
     * Authorization in server;
     */
    public _LoginInfo login() throws Exception {
        LogUtil.log(LogUtil.INFO, "Выполнение авторизации на сервере");
        Server server = serverQuery(ServerCommands.LOGIN, new RequestParams());
        if (!server.isSuccess() && server.getError() != null) {
            if (server.getError().getID().equals(LOGININCORRECT) ||
                    server.getError().getID().equals(USERNOTFOUND))
                throw new Exception(LOGININCORRECT);
        }
        _LoginInfo info = ParserManager.parseLogin(server);
        return info;
    }

    /**
     * The passage of SMS authorization
     *
     * @param smsCode  - code by sms;
     * @param userName - user name;
     */
    public _LoginInfo getUser(String smsCode, String userName) throws Exception {
        LogUtil.log(LogUtil.INFO, "Получить авторизационный ключ");
        RequestParams params = new RequestParams();
        params.add("actkey", smsCode);
        params.add("name", userName);
        params.add("usertype", "customer");

        Server server = serverQuery(ServerCommands.GET_ACTIVATION_KEY, params);
        _LoginInfo info = ParserManager.parseAuth(server);
        return info;
    }

    /**
     * Request sms code.
     */
    public Server getSmsKey(String phoneOrMail, boolean byMail, String userType) {
        LogUtil.log(LogUtil.INFO, "Получить смс ключ");
        RequestParams params = new RequestParams();
        if (byMail) {
            params.add("way", "byemail");
            params.add("email", phoneOrMail);
        } else
            params.add("phone", phoneOrMail);
        params.add("usertype", userType);
        return serverQuery(ServerCommands.GET_SMS_KEY, params);
    }

    /**
     * Calculation cost for order.
     */
    public QueryList<_Tariff> calculateCost(RequestParams params) throws Exception {
        LogUtil.log(LogUtil.INFO, "Расчитать стоимость заказа");
        if (params == null)
            return null;
        Server server = serverQuery(ServerCommands.CALCCOST, params);
        return ParserManager.parseCalculateCost(server);
    }

    /**
     * Registration new order.
     */
    public _OrderRegisterAnswer registrationOrderOnServer(RequestParams params) throws Exception {
        LogUtil.log(LogUtil.INFO, "Зарегистрировать заказ на сервере");
        if (params == null)
            return null;
        Server server = serverQuery(ServerCommands.ADD_ORDER, params);
        if (!server.isSuccess() && server.getError() != null) {
            if (server.getError().getID().equals(LOGININCORRECT)) {
                throw new Exception(LOGININCORRECT);
            }
        }
        return ParserManager.parseOrderRegisterAnswer(server);
    }

    /**
     * Get tariffs and special services.
     */
    public _TariffServiceData getTariffsAndServices() throws Exception {
        LogUtil.log(LogUtil.INFO, "Получить тарифы и сервисы");
        Server server = serverQuery(ServerCommands.GET_TARIFFS, new RequestParams());
        _TariffServiceData services = ParserManager.parseTariffServiceData(server);
        return services;
    }

    public PaymentSystems getPaymentSystems() throws Exception {
        LogUtil.log(LogUtil.INFO, "Получить платежные системы");
        Server server = serverQuery(ServerCommands.GET_PAYMENTSYSTEMS, new RequestParams());
        PaymentSystems systems = ParserManager.parsePaymentSystems(server);
        return systems;
    }

    /**
     * Get orders.
     *
     * @param startDate - date asyncSearch
     * @param statuses  - orders statuses
     */
    public Server getOrders(DateTime startDate, OrderStatuses... statuses) {
        LogUtil.log(LogUtil.INFO, "Получить мои заказы с сервера");
        String command = "";
        RequestParams params = new RequestParams();
        if (startDate != null) {
            params.put("from", startDate.toString("yyyy-MM-dd'T'00:00:00"));
            params.put("to", DateTime.Now().toString("yyyy-MM-dd'T'HH:mm:00"));
        }
        if (statuses != null) {
            String strStatuses = "";
            for (OrderStatuses s : statuses) {
                strStatuses += (strStatuses.length() > 0 ? "," : "") + s.toString();
            }
            params.add("statuses", strStatuses);
        }
        return serverQuery(ServerCommands.GET_ORDERS, params);
    }

    /**
     * Get order by id;
     *
     * @param orderId - order id;
     */
    public _Order getOrder(int orderId) throws Exception {
        LogUtil.log(LogUtil.INFO, "Получить заказы ID: %d decimalFormat сервера", orderId);
        RequestParams params = new RequestParams();
        params.put("orderids", orderId);
        Server server = serverQuery(ServerCommands.GET_ORDERS, params);
        QueryList<_Order> orders = ParserManager.parseOrders(server);
        return orders.tryGet(0);
    }

    /**
     * Cancel order.
     *
     * @param orderId - order id;
     */
    public Server cancelOrder(int orderId) {
        LogUtil.log(LogUtil.INFO, String.format("ServerManager->cancelOrder(): Отменить заказ с ID %d", orderId));
        return serverQuery(ServerCommands.CANCEL_ORDER, new RequestParams("orderid", orderId));
    }

    /**
     * Set rating for order.
     *
     * @param orderId - order id;
     * @param rating  - rating;
     * @param comment - comment;
     */
    public Server setRating(String orderId, int rating, String comment) {
        LogUtil.log(LogUtil.INFO, String.format("ServerManager->setRating(): Выставить рейтинг для заказа с ID %s", orderId));
        RequestParams params = new RequestParams();
        params.add("orderId", orderId);
        params.put("rating", rating);
        params.add("comment", comment);

        return serverQuery(ServerCommands.SET_RATING, params);
    }

    /**
     * The invoice in payment systems
     *
     * @param phone         - phone number;
     * @param sum           - payment sum;
     * @param paymentSystem - payment system;
     */
    public _Bill bill(String phone, double sum, String paymentSystem) throws Exception {
        LogUtil.log(LogUtil.INFO, "Выставить счет на номер %s на сумму %.2f в системе %s", phone, sum, paymentSystem);
        RequestParams params = new RequestParams();
        params.put("sum", sum);
        params.add("service", paymentSystem);
        params.add("phone", URLEncoder.encode(phone));
        Server server = serverQuery(ServerCommands.BILL, params);

        _Bill bill = ParserManager.parseBill(server);
        return bill;
    }

    /**
     * Get customer balance;
     */
    public _Balance getCustomerBalance() throws Exception {
        LogUtil.log(LogUtil.INFO, "Обновить баланс пользователя");
        Server server = serverQuery(ServerCommands.BALANCE, new RequestParams());
        _Balance balance = ParserManager.parseBalance(server);
        return balance;
    }

    public Server updatePartnerCode(String partnerCode) {
        LogUtil.log(LogUtil.INFO, "Обновление кода партнерской программы");
        RequestParams params = new RequestParams();
        params.put("promocode", partnerCode);
        return serverQuery(ServerCommands.PROMOCODE, params);

    }

    public _LoginInfo getProfile() throws Exception {
        LogUtil.log(LogUtil.INFO, "Получение профиля пользователя");
        Server server = serverQuery(ServerCommands.GET_PROFILE, new RequestParams());
        _LoginInfo info = ParserManager.parseLogin(server);
        return info;
    }

    public Boolean updateProfile(RequestParams params) throws Exception {
        Server server = serverQuery(ServerCommands.SET_PROFILE, params);
        return ParserManager.parseStandart(server);
    }

    public BankCard[] getPaymentCard() throws Exception {
        Server server = serverQuery(ServerCommands.GET_CARDS, new RequestParams());
        BankCard[] balance = ParserManager.parseBankCards(server);
        return balance;
    }

    public Server setCard(BankCard card, boolean enable, boolean delete) throws Exception {
        RequestParams params = new RequestParams();
        params.put("service", card.getService().getID());
        params.put("id", card.getID());
        params.put("enable", enable);
        params.put("delete", delete);

        return serverQuery(ServerCommands.SET_CARD, params);
    }

    public _Bill addRecurrentCard(String mail, String id) throws Exception {
        RequestParams params = new RequestParams();
        params.put("email", mail);
        params.put("service", id);

        Server server = serverQuery(ServerCommands.ADD_CARD, params);
        _Bill bill = ParserManager.parseBill(server);
        return bill;
    }

    public void sendTokenOnServer(String key) {
        RequestParams params = new RequestParams();
        params.put("androidtoken", key);
        serverQuery(ServerCommands.ADD_ANDROID_TOKEN, params);
    }

    public _Point checkAddress(Context context, String addressString) throws Exception {
        /*QueryList<_Point> point = LocationService.me()
                .getAddressListByLocationPoint(context, addressString);
        return point.tryGet(0);*/

        RequestParams params = new RequestParams();
        params.put("addr", addressString);
        Server server = serverQuery(ServerCommands.CHECK_ADDRESS, params);
        _Point point = ParserManager.parseAddress(server);
        return point;
    }

    public _Driver[] driverNearMe(LatLong location) throws Exception {
        RequestParams params = new RequestParams();
        params.put("lat", location.Latitude);
        params.put("lon", location.Longitude);
        params.put("radius", 1000);

        Server server = serverQuery(ServerCommands.GET_DRIVERS, params);
        _Driver[] drivers = ParserManager.parseDrivers(server);
        return drivers;
    }

    public _Point findAddress(_Point p) throws Exception {
        if (p == null) return null;

        RequestParams params = new RequestParams();

        if (!TextUtils.isEmpty(p.getCountryName()))
            params.put("country", p.getCountryName());
        if (!TextUtils.isEmpty(p.getCityName()))
            params.put("city", p.getCityName());
        if (!TextUtils.isEmpty(p.getStreetName()))
            params.put("street", p.getStreetName());
        if (!TextUtils.isEmpty(p.getHouseNumber()))
            params.put("house", p.getHouseNumber());
        if (!TextUtils.isEmpty(p.getObjectName()))
            params.put("object", p.getObjectName());
        if (p.getGeoPoint() != null && p.getChecked()) {
            _GeoPoint gp = p.getGeoPoint();
            params.put("lat", gp.getLat());
            params.put("lon", gp.getLon());
        }
        params.put("precision", 0);

        Server server = serverQuery(ServerCommands.FIND_ADDRESS, params);
        _Point point = ParserManager.parseCheckAddress(server);
        return point;
    }

    public String getHash(String partnerUrl) throws Exception {
        RequestParams params = new RequestParams();
        params.add("sourcestring", partnerUrl);

        Server server = serverQuery(ServerCommands.GET_HASHBYSTRING, params);

        return ParserManager.parseHash(server);
    }

    public String getInvitationText(String hash) throws Exception {
        RequestParams params = new RequestParams();
        params.add("hash", hash);

        Server server = serverQuery(ServerCommands.GET_INVITATION_TEXT, params);
        return ParserManager.parseInvitation(server);
    }

    public String getInvitationText(int distributorAccountID, UserTypes userTypes, InvitationTypes invitationTypes) throws Exception {
        RequestParams params = new RequestParams();
        params.add("distributorAccountID", String.valueOf(distributorAccountID));
        params.add("userType", userTypes.name());
        params.add("invitationType", invitationTypes.name());

        Server server = serverQuery(ServerCommands.GET_INVITATION_TEXT, params);
        return ParserManager.parseInvitation(server);
    }

    public CostCalculationResult getCostCalculationResult(int costCalculationId) throws Exception {
        RequestParams params = new RequestParams();
        params.add("costcalculationid", String.valueOf(costCalculationId));

        Server server = serverQuery(ServerCommands.GET_COSTCALCULATION_RESULT, params);
        return ParserManager.parseCostCalculationResult(server);
    }
}
