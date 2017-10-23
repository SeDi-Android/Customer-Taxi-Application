package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;

public class RouteHistory {
    private String mName;
    private long mCreateTime = 0L;
    private _Point[] mRoute = new _Point[]{};

    public RouteHistory(String name, QueryList<_Point> route) {
        mName = name;
        mCreateTime = System.currentTimeMillis();

        for (_Point point : route) {
            point.setChecked(true);
        }
        mRoute = route.toArray(new _Point[route.size()]);
    }

    public RouteHistory() {
    }

    public String getName() {
        return mName;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public QueryList<_Point> getRoute() {
        return new QueryList<>(mRoute);
    }

    @Override
    public boolean equals(Object o) {
        try {
            return mName.equalsIgnoreCase(((RouteHistory)o).getName());
        } catch (Exception e) {
            LogUtil.log(e);
            return false;
        }
    }

    public String getRouteString() {
        String route = "";
        for (_Point point : mRoute) {
            if (!route.isEmpty())
                route += "→\n";
            route += point.asString(true);
        }
        return route;
    }
}
