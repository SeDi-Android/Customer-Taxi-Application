package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LINQ.QueryList;

public class PaymentSystems {
    QueryList<PaymentSystem> PaymentSystems = new QueryList<>();
    QueryList<PaymentSystem> RecurrentPaymentSystems = new QueryList<>();

    public QueryList<PaymentSystem> getPaymentSystems() {
        return PaymentSystems;
    }

    public QueryList<PaymentSystem> getRecurrentPaymentSystems() {
        return RecurrentPaymentSystems;
    }
}
