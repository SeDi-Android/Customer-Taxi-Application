package ru.sedi.customerclient.NewDataSharing;

import android.support.annotation.UiThread;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.sedi.customerclient.adapters.RouteHistoryAdapter;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.db.DBHistoryRoute;


public class RouteHistroryCollection {

    private static RouteHistroryCollection mThis;
    private final Realm mRealm;
    private final RealmResults<DBHistoryRoute> mRouteHistories;
    private RouteHistoryAdapter mHistoryAdapter;

    public RouteHistroryCollection() {
        mRealm = Realm.getDefaultInstance();
        mRouteHistories = mRealm.where(DBHistoryRoute.class).findAll();
    }

    public static RouteHistroryCollection me() {
        if (mThis == null)
            mThis = new RouteHistroryCollection();
        return mThis;
    }

    public boolean add(DBHistoryRoute history) throws Exception {
        for (DBHistoryRoute DBHistoryRoute : mRouteHistories) {
            if (DBHistoryRoute.equals(history)) {
                throw new Exception("Маршрут с таким названием уже существует");
            }
        }
        mRealm.executeTransactionAsync(realm -> {
            realm.copyToRealm(history);
        }, this::updateAdapter);
        return true;
    }

    public void remove(final DBHistoryRoute history) {
        mRealm.executeTransactionAsync(realm -> {
            RealmResults<DBHistoryRoute> all = realm.where(DBHistoryRoute.class)
                    .equalTo("mName", history.getName())
                    .findAll();
            all.deleteAllFromRealm();
        }, this::updateAdapter);
    }

    public RealmResults<DBHistoryRoute> getAll() {
        return mRouteHistories;
    }

    public DBHistoryRoute[] getAsArray() {
        return mRouteHistories.toArray(new DBHistoryRoute[mRouteHistories.size()]);
    }

    public RouteHistoryAdapter getAdapter(IAction<DBHistoryRoute> action) {
        mHistoryAdapter = new RouteHistoryAdapter(null, action);
        return mHistoryAdapter;
    }

    public boolean contains(QueryList<_Point> points) {
        for (DBHistoryRoute DBHistoryRoute : mRouteHistories) {
            if (DBHistoryRoute.getRoute().equals(points))
                return true;
        }
        return false;
    }

    @UiThread
    public void updateAdapter() {
        if (mHistoryAdapter != null)
            mHistoryAdapter.notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return mRouteHistories.isEmpty();
    }
}
