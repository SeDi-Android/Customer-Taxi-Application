package ru.sedi.customerclient.db;

import io.realm.RealmList;
import io.realm.RealmObject;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;

public class DBHistoryRoute extends RealmObject {
    private String mName;
    private RealmList<DbHistoryPoint> mRoute = new RealmList<>();

    public DBHistoryRoute(String name, QueryList<_Point> route) {
        mName = name;

        for (_Point point : route) {
            point.setChecked(true);
            mRoute.add(new DbHistoryPoint(point));
        }
    }

    public DBHistoryRoute() {
    }

    public String getName() {
        return mName;
    }


    public QueryList<DbHistoryPoint> getRoute() {
        return new QueryList<>(mRoute);
    }

    @Override
    public boolean equals(Object o) {
        try {
            return mName.equalsIgnoreCase(((DBHistoryRoute) o).getName());
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public String getRouteString() {
        String route = "";
        for (DbHistoryPoint point : mRoute) {
            if (!route.isEmpty())
                route += "→\n";
            route += point.toString();
        }
        return route;
    }

    @Override
    public String toString() {
        return getName();
    }
}
