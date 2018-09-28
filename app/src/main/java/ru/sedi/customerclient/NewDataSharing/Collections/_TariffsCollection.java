package ru.sedi.customerclient.NewDataSharing.Collections;

import android.content.Context;
import android.widget.ListAdapter;

import ru.sedi.customerclient.adapters.TariffAdapter;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class _TariffsCollection {
    QueryList<_Tariff> mTariffs = new QueryList<>();

    public _TariffsCollection() {
    }

    public QueryList<_Tariff> getAll() {
        if (mTariffs == null)
            mTariffs = new QueryList<>();
        return mTariffs;
    }

    public void set(_Tariff[] tariffs) {
        mTariffs = new QueryList<>(tariffs);
    }

    public void clear() {
        mTariffs.clear();
    }

    public void add(QueryList<_Tariff> tariffs) {
        mTariffs.addAll(tariffs);
    }

    public boolean isEmpty() {
        return mTariffs.isEmpty();
    }

    public ListAdapter getAdapter(final Context context, QueryList<_Tariff> tariffs) {
        TariffAdapter adapter = new TariffAdapter(context, tariffs);
        return adapter;
    }

    public int size() {
        return mTariffs.size();
    }

}
