package ru.sedi.customerclient.NewDataSharing;

import android.support.annotation.UiThread;

import java.util.Arrays;

import ru.sedi.customerclient.adapters.RouteHistoryAdapter;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.IWhere;
import ru.sedi.customerclient.common.LINQ.QueryList;


public class RouteHistroryCollection {

    private static RouteHistroryCollection mThis;
    private QueryList<RouteHistory> mRouteHistories = new QueryList<>();
    private RouteHistoryAdapter mHistoryAdapter;

    public static RouteHistroryCollection me() {
        if (mThis == null)
            mThis = new RouteHistroryCollection();
        return mThis;
    }

    public boolean add(RouteHistory history) throws Exception {
        for (RouteHistory routeHistory : mRouteHistories) {
            if (routeHistory.equals(history)) {
                throw new Exception("Маршрут с таким названием уже существует");
            }
        }
        mRouteHistories.add(history);
        updateAdapter();
        return true;
    }

    public void remove(final RouteHistory history) {
        QueryList<RouteHistory> where = mRouteHistories.Where(new IWhere<RouteHistory>() {
            @Override
            public boolean Condition(RouteHistory item) {
                return item.equals(history);
            }
        });
        mRouteHistories.removeAll(where);
        updateAdapter();
    }

    public QueryList<RouteHistory> getAll() {
        return mRouteHistories;
    }

    public RouteHistory[] getAsArray() {
        return mRouteHistories.toArray(new RouteHistory[mRouteHistories.size()]);
    }

    public void set(RouteHistory[] history) {
        mRouteHistories.clear();
        mRouteHistories.addAll(Arrays.asList(history));
    }

    public RouteHistoryAdapter getAdapter(IAction<RouteHistory> action) {
        mHistoryAdapter = new RouteHistoryAdapter(mRouteHistories, action);
        return mHistoryAdapter;
    }

    public boolean contains(QueryList<_Point> points){
        for (RouteHistory routeHistory : mRouteHistories) {
            if(routeHistory.getRoute().equals(points))
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
