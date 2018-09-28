package ru.sedi.customerclient.NewDataSharing;

import android.content.Context;
import android.text.TextUtils;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Helpers.Helpers;

public class _Tariff {
    private String Type;
    private int ID;
    private String Name;
    private float Cost;
    private float CostFull;
    private int Cars;
    private boolean IsMinimumCost;
    private String[] Details;
    private String Currency;
    private int CostCalculationId;

    public _Tariff(String type, int ID, String name, float cost, float costFull, int cars,
                   String[] details, String currency, int costCalculationId) {
        Type = type;
        this.ID = ID;
        Name = name;
        Cost = cost;
        CostFull = costFull;
        Cars = cars;
        Details = details;
        Currency = currency;
        CostCalculationId = costCalculationId;
    }

    public _Tariff() {
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public float getCost() {
        return Cost;
    }

    public void setCost(int cost) {
        Cost = cost;
    }

    public float getCostFull() {
        return CostFull;
    }

    public void setCostFull(int costFull) {
        CostFull = costFull;
    }

    public int getCars() {
        return Cars;
    }

    public void setCars(int cars) {
        Cars = cars;
    }

    public String[] getDetails() {
        return Details;
    }

    public String getStringDetails() {
        if (Details == null) return "";

        String details = "";
        for (String detail : Details) {
            if (!TextUtils.isEmpty(details))
                details = details.concat("\n");
            details = details.concat(detail);
        }
        return details;
    }

    public void setDetails(String[] details) {
        Details = details;
    }

    public String getCurrency() {
        if (Currency == null)
            Currency = Const.EmptyStr;
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public boolean isMinimumCost() {
        return IsMinimumCost;
    }

    public String toString(Context context) {
        String cost = getCost() > 0 ? Helpers.decimalFormat(getCost()) + getCurrency() : context.getString(R.string.fact_cost);
        return String.format("%s (%s)", getName(), cost);
    }

    public float getDiscount() {
        return Math.abs(getCostFull() - getCost());
    }

    public void setIsMinimumCost(boolean isMinimumCost) {
        IsMinimumCost = isMinimumCost;
    }

    public int getCostCalculationId() {
        return CostCalculationId;
    }

    public _Tariff copy() {
        return new _Tariff(Type, ID, Name, Cost, CostFull, Cars, Details, Currency, CostCalculationId);
    }

    public NameId getNameId() {
        return new NameId(Name, String.valueOf(ID));
    }
}
