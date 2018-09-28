package ru.sedi.customerclient.NewDataSharing.Collections;

import ru.sedi.customerclient.NewDataSharing.PaymentSystem;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class PaymentSystemCollection {

    private QueryList<PaymentSystem> mPaymentSystems = new QueryList<>();
    private QueryList<PaymentSystem> mRecurrentSystems = new QueryList<>();

    public PaymentSystemCollection() {
    }

    public void set(QueryList<PaymentSystem> paymentSystem, QueryList<PaymentSystem> recurrentSystem) {
        if (paymentSystem != null)
            mPaymentSystems = new QueryList<>(paymentSystem);
        if (recurrentSystem != null)
            mRecurrentSystems = new QueryList<>(recurrentSystem);
    }

    public void set(PaymentSystem[] paymentSystem, PaymentSystem[] recurrentSystem) {
        if (paymentSystem != null)
            mPaymentSystems = new QueryList<>(paymentSystem);
        if (recurrentSystem != null)
            mRecurrentSystems = new QueryList<>(recurrentSystem);
    }

    public QueryList<PaymentSystem> getAll() {
        return mPaymentSystems;
    }

    public PaymentSystem[] getPaymentAsArray() {
        return mPaymentSystems.toArray(new PaymentSystem[mPaymentSystems.size()]);
    }

    public QueryList<PaymentSystem> getEnabled() {
        return mPaymentSystems.Where(PaymentSystem::isEnabled);
    }

    public QueryList<PaymentSystem> getRecurrent() {
        return mRecurrentSystems.Where(PaymentSystem::isEnabled);
    }

    public PaymentSystem[] getRecurrentAsArray() {
        return mRecurrentSystems.toArray(new PaymentSystem[mRecurrentSystems.size()]);
    }
}
