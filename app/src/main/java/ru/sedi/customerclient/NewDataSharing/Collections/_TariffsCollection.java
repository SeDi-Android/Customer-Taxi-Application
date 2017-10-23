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

    public _Tariff[] getAsArray() {
        return getAll().toArray(new _Tariff[getAll().size()]);
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
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice,
                getNameList(tariffs)) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(18);
                ((TextView) v).setTextColor(ContextCompat.getColor(context, R.color.firstColor));
                return v;
            }
        };*/
        TariffAdapter adapter = new TariffAdapter(context, tariffs);
        return adapter;
    }

    public ListAdapter getAdapter(Context context) {
        return getAdapter(context, getAll());
    }

    public int size() {
        return mTariffs.size();
    }

    /*public QueryList<String> getNameList(QueryList<_Tariff> tariffs) {
        QueryList<String> list = new QueryList<>();
        for (_Tariff tariff : tariffs) {
            list.add(tariff.toString());
        }
        return list;
    }*/
}
