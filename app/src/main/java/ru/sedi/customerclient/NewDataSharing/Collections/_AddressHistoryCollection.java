package ru.sedi.customerclient.NewDataSharing.Collections;

import android.support.annotation.UiThread;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.adapters.AddressHistoryAdapter;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.db.DbHistoryPoint;

public class _AddressHistoryCollection {

    private final Realm mRealm;
    private RealmResults<DbHistoryPoint> historyPoints;
    private AddressHistoryAdapter mAdapter;

    public _AddressHistoryCollection() {
        mRealm = Realm.getDefaultInstance();
        historyPoints = mRealm.where(DbHistoryPoint.class).distinct("Description").findAll();
    }

    public void add(final QueryList<_Point> points) {
        mRealm.executeTransactionAsync(realm -> {
            for (_Point p : points) {
                if (p.isCoordinatesonly()) continue;

                boolean empty = realm.where(DbHistoryPoint.class)
                        .equalTo("CityName", p.getCityName())
                        .and().equalTo("StreetName", p.getStreetName())
                        .and().equalTo("HouseNumber", p.getHouseNumber())
                        .findAll().isEmpty();
                if (!empty) continue;
                realm.copyToRealm(new DbHistoryPoint(p));
            }
        }, () -> updateAdapter());
    }

    public void remove(final _Point p) {
        mRealm.executeTransactionAsync(realm -> {
            RealmResults<DbHistoryPoint> all = realm.where(DbHistoryPoint.class)
                    .equalTo("CityName", p.getCityName())
                    .and().equalTo("StreetName", p.getStreetName())
                    .and().equalTo("HouseNumber", p.getHouseNumber())
                    .findAll();
            all.deleteAllFromRealm();
        }, () -> updateAdapter());
    }

    /*public _Point[] getAsArray() {
        return mPoints.toArray(new _Point[mPoints.size()]);
    }*/

    public RealmResults<DbHistoryPoint> getAll() {
        return historyPoints;
    }

    /*public void set(_Point[] p) {
        mPoints = new QueryList<>(p);
    }*/

    public AddressHistoryAdapter getAdapter(IAction<_Point> action) {
        mAdapter = new AddressHistoryAdapter(null, action);
        return mAdapter;
    }

    @UiThread
    private void updateAdapter() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return historyPoints.isEmpty();
    }
}
