package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LINQ.QueryList;

public class _TariffServiceData {
    QueryList<_Tariff> Tariffs = new QueryList<>();
    QueryList<_Tariff> Tariffs2 = new QueryList<>();
    QueryList<_Service> Specs = new QueryList<>();
    private boolean AllowRent;
    private boolean AllowCustomerCost;
    private boolean AllowTaxometer;
    private boolean Cashless;
    private int Discount;
    private boolean DiscountToRegister;
    private boolean KeywordDiscount;

    public _TariffServiceData() {
    }

    public QueryList<_Service> getSpecs() {
        return Specs;
    }

    public boolean isKeywordDiscount() {
        return KeywordDiscount;
    }

    public QueryList<_Tariff> getTariffs() {
        return Tariffs;
    }

    public QueryList<_Tariff> getTariffs2() {
        return Tariffs2;
    }

    public boolean hasOnceTariff() {
        return (Tariffs != null || Tariffs2 != null);
    }
}
