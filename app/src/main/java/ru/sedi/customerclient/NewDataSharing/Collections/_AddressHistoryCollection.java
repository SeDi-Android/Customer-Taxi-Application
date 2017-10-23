package ru.sedi.customerclient.NewDataSharing.Collections;

import android.support.annotation.UiThread;

import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.adapters.AddressHistoryAdapter;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class _AddressHistoryCollection {

    private QueryList<_Point> mPoints = new QueryList<>();
    private AddressHistoryAdapter mAdapter = null;

    public _AddressHistoryCollection() {
    }

    public void add(final _Point p) {
        if (p.isCoordinatesonly())
            return;

        boolean contains = mPoints.Contains(item -> item.getCityName().equalsIgnoreCase(p.getCityName())
                && item.getStreetName().equalsIgnoreCase(p.getStreetName())
                && item.getHouseNumber().equalsIgnoreCase(p.getHouseNumber()));

        if (contains)
            return;

        mPoints.add(p.copy());
        updateAdapter();
    }

    public void remove(final _Point p) {
        QueryList<_Point> list = mPoints.Where(item -> item.equals(p));
        mPoints.removeAll(list);
        updateAdapter();
    }

    public _Point[] getAsArray() {
        return mPoints.toArray(new _Point[mPoints.size()]);
    }

    public QueryList<_Point> getAll() {
        mPoints = mPoints.OrderBy(item -> item.getCityName());
        return mPoints;
    }

    public void set(_Point[] p) {
        mPoints = new QueryList<>(p);
    }

    public AddressHistoryAdapter getAdapter(IAction<_Point> action) {
        mAdapter = new AddressHistoryAdapter(mPoints, action);
        return mAdapter;
    }

    @UiThread
    private void updateAdapter() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void recreateWith(QueryList<_Point> addresses) {
        if(addresses == null || addresses.isEmpty())
            return;

        mPoints.clear();
        for (_Point address : addresses) {
            mPoints.add(address.copy());
        }
        updateAdapter();
    }

    public boolean isEmpty() {
        return mPoints.isEmpty();
    }
}
