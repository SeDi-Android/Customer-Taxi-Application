package ru.sedi.customerclient.NewDataSharing;

import android.text.TextUtils;

import com.google.gson.Gson;

import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Customer._Customer;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class _Order {
    private NameId Status;
    private String Date;
    private _Route Route;
    private double Cost;
    private float Discount;
    private String Currency;
    private NameId Tariff;
    private NameId Type = new NameId("", "rush");
    private QueryList<NameId> Specs = new QueryList<>();
    private int RentHours;
    private boolean Cashless;
    private _Customer Customer;
    private String Description;
    private double Distance; // Расстояние м-ду водителем
    private double Duration; // Расстояние м-ду водителем
    private _Driver Driver; // может отсутствовать
    private _Rating Rating;
    private int ID;
    private String Details;
    private boolean Isminimumcost;

    private OnChangeListener mChangeListener;

    public _Order(_Order order) {
        Status = new NameId(order.Status);
        Date = order.Date;
        Route = new _Route(order.Route.getPoints(), order.getRoute().getLength());
        Cost = order.Cost;
        Discount = order.Discount;
        Currency = order.Currency;
        Tariff = new NameId(order.Tariff);
        Specs = new QueryList<>(order.Specs);
        RentHours = order.RentHours;
        Cashless = order.Cashless;
        Customer = new _Customer(order.Customer);
        Description = order.Description;
        Distance = order.Distance;
        Duration = order.Duration;
        Driver = order.Driver;
        Rating = order.Rating;
        this.ID = order.ID;
        Type = order.Type;
    }

    public _Order() {
        DateTime date = DateTime.Now();
        int currentMinute = date.getMinute();
        date.setMinute(currentMinute - (currentMinute % 5));
        date.addMinute(20);

        Date = date.toString(DateTime.WEB_DATE);
        Route = new _Route();
        Status = new NameId(App.isTaxiLive ? "Wird registriert..." : "Регистрация", OrderStatuses.search.name());
        Tariff = new NameId();
        Customer = new _Customer();
    }

    //<editor-fold desc="Getter / Setter">
    public NameId getStatus() {
        return Status;
    }

    public String getDate() {
        if (TextUtils.isEmpty(Date))
            Date = DateTime.Now().toString(DateTime.WEB_DATE);
        String s = new String(Date);
        String date = "";

        String[] split = s.split("T");
        String[] dateSplit = split[0].split("-");

        for (int i = dateSplit.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(date))
                date += ".";
            date += dateSplit[i];
        }

        String time = "";
        String[] timeSplit = split[1].split(":");

        for (int i = 0; i < timeSplit.length - 1; i++) {
            if (!TextUtils.isEmpty(time))
                time += ":";
            time += timeSplit[i];
        }
        return String.format("%s %s", date, time);
    }

    public void setDate(String date) {
        Date = date;
    }

    public _Route getRoute() {
        return Route;
    }

    public void setRoute(_Route route) {
        Route = route;
    }

    public double getCost() {
        return Cost;
    }

    public NameId getTariff() {
        return Tariff;
    }

    public void setTariff(_Tariff tariff) {
        if (tariff == null) {
            Currency = "";
            Tariff = new NameId();
            Cost = 0;
            Details = "";
            Isminimumcost = false;
            Discount = 0;
        } else {
            Currency = tariff.getCurrency();
            Tariff = tariff.getNameId();
            Cost = tariff.getCost();
            Details = tariff.getStringDetails();
            Isminimumcost = tariff.isMinimumCost();
            Discount = tariff.getCostFull() - tariff.getCost();
        }
        updateListeners();
    }

    public String getCurrency() {
        return Currency != null ? Currency : "P";
    }

    public QueryList<NameId> getSpecs() {
        return Specs;
    }

    public void setSpecs(QueryList<NameId> specs) {
        Specs = specs;
    }

    public boolean isCashless() {
        return Cashless;
    }

    public void setCashless(boolean cashless) {
        Cashless = cashless;
    }

    public _Customer getCustomer() {
        return Customer;
    }

    public void setCustomer(_Customer customer) {
        Customer = customer;
    }

    public String getDescription() {
        if (Description == null)
            Description = Const.EmptyStr;
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public int getDuration() {
        return (int) Duration;
    }

    public void setDuration(double duration) {
        Duration = duration;
    }

    public _Driver getDriver() {
        return Driver;
    }

    public _Rating getRating() {
        return Rating;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isRush() {
        return Type != null && Type.getID().equals("rush");
    }

    public void setRush(boolean rush) {
        Type = new NameId("", rush ? "rush" : "preliminary");
    }

    public DateTime getDateTime() {
        return DateTime.fromString(Date, DateTime.WEB_DATE);
    }

    public void setDateTime(DateTime dateTime) {
        Date = dateTime.toString(DateTime.WEB_DATE);
    }

    public float getDiscount() {
        return Discount;
    }

    public String getDetails() {
        return Details;
    }

    public boolean isMinimumcost() {
        return Isminimumcost;
    }

    public String getDriverCarInfo() {
        if (getDriver() == null)
            return Const.EmptyStr;

        StringBuilder builder = new StringBuilder();
        if (!TextUtils.isEmpty(getDriver().getName()))
            builder.append(getDriver().getName());

        _Car car = getDriver().getCar();
        if (car != null) {
            if (!builder.toString().isEmpty())
                builder.append(" ");
            builder.append(car.getCarInfo());
        }
        return builder.toString();
    }

    public boolean isValidPreOrderTime() {
        return (getDateTime().getTime() - DateTime.Now().getTime() > DateTime.MINUTE * 15);
    }

    public void addChangeListener(OnChangeListener listener) {
        mChangeListener = listener;
    }

    public void updateListeners() {
        if (mChangeListener != null) {
            try {
                mChangeListener.onSuccessCalculate(this);
            } catch (Exception e) {
                if (e instanceof NullPointerException)
                    mChangeListener = null;
            }
        }
    }

    public boolean equals(_Order newOrder) {
        //Check route size
        if (newOrder.getRoute().size() != getRoute().size())
            return false;

        //Check start point
        _Point p1 = newOrder.getRoute().getPoints().tryGet(0);
        _Point p2 = this.getRoute().getPoints().tryGet(0);
        if (p1 == null || p2 == null || !p1.asString().contentEquals(p2.asString()))
            return false;

        //Check end point
        p1 = newOrder.getRoute().getPoints().tryGet(newOrder.getRoute().getPoints().size() - 1);
        p2 = this.getRoute().getPoints().tryGet(this.getRoute().getPoints().size() - 1);
        if (p1 == null || p2 == null || !p1.asString().contentEquals(p2.asString()))
            return false;

        //Check specs
        QueryList<NameId> checked = Collections.me().getServices().getCheckedNameId();
        if (checked.size() != this.getSpecs().size())
            return false;

        for (int i = 0; i < checked.size(); i++) {
            NameId id = newOrder.getSpecs().tryGet(i);
            NameId id2 = getSpecs().tryGet(0);
            if (id == null || id2 == null || !id.getID().equalsIgnoreCase(id2.getID()))
                return false;
        }

        //Check time / type
        if (isRush() != newOrder.isRush())
            return false;

        return true;
    }

    public OnChangeListener getChangeListener() {
        return mChangeListener;
    }

    public _Order fullCopy() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return new _Order(gson.fromJson(json, _Order.class));
    }

    public void setRating(_Rating rating) {
        Rating = rating;
    }

    //</editor-fold>

    public interface OnChangeListener {
        void onSuccessCalculate(_Order order);
        void onStartCalculate();
        void OnFailureCalculate(String message);
    }


}
