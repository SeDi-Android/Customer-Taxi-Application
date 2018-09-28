package ru.sedi.customerclient.NewDataSharing;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;

public class _Route {
    private QueryList<_Point> Points = new QueryList<>();
    private double Length;
    private OnRouteChangeListener listener;

    public _Route(OnRouteChangeListener listener) {
        this.listener = listener;
    }

    public _Route(QueryList<_Point> points, double length) {
        Points = points;
        Length = length;
        notifyListener();
    }

    public QueryList<_Point> getPoints() {
        return Points;
    }

    public void setPoints(QueryList<_Point> points) {
        Points = points;
        filter();
        notifyListener();
    }

    public void addPoint(_Point point) {
        LogUtil.log(LogUtil.INFO, point.asString());
        Points.add(point);
        filter();
        notifyListener();
    }

    public double getLength() {
        return Length;
    }

    public void setLength(double length) {
        Length = length;
    }

    public String asString() {
        if (!isValidRoute()) {
            return App.getInstance().getString(R.string.NotSelected);
        }

        StringBuilder sb = new StringBuilder();
        for (_Point p : Points) {
            if (!sb.toString().isEmpty())
                sb.append("→");
            sb.append(p.asString(true));
        }
        return sb.toString();
    }

    public int size() {
        return Points.Where(_Point::getChecked).size();
    }

    public boolean isEmpty() {
        return Points.isEmpty();
    }

    public void addByIndex(int i, _Point point) {
        if (i < 0) {
            addPoint(point);
        } else {
            LogUtil.log(LogUtil.INFO, point.asString());
            Points.add(i, point);
            filter();
            notifyListener();
        }

    }

    public _Point getByIndex(int i) {
        if (Points.isEmpty() || Points.size() < i)
            return new _Point();
        return Points.get(i);
    }

    public void setByIndex(int i, _Point point) {
        if (i < 0 || (Points.size() - 1 < i))
            addPoint(point);
        else {
            Points.set(i, point);
            filter();
            notifyListener();
        }
        Prefs.setValue(PrefsName.LAST_CITY, point.getCityName());
    }

    public void remove(_Point a) {
        Points.remove(a);
    }

    public _Point getLast() {
        return Points.get(Points.size() - 1);
    }

    public boolean isValidRoute() {
        QueryList<_Point> points = Points;
        _Point p = points.tryGet(0);
        if (!points.isEmpty() && (p != null && p.getChecked()))
            return true;
        else
            return false;
    }

    public boolean checkSequence(_Point nextSediAddress) {
        if (Points.size() < 1)
            return false;

        _Point lastSediAddress = Points.tryGet(Points.size() - 1);
        if (lastSediAddress == null)
            return false;
        return lastSediAddress.equalsAddress(nextSediAddress);
    }

    public void clearPoints() {
        Points.clear();
    }

    public void filter() {
        QueryList<_Point> where = Points.Where(item -> !item.getChecked());
        if(where.isEmpty()) return;
        Points.removeAll(where);
        notifyListener();
    }

    public void notifyListener(){
        if(listener!=null) listener.onRouteChange();
    }

    public void setListener(OnRouteChangeListener listener) {
        this.listener = listener;
    }


    //region Interfaces
    public interface OnRouteChangeListener{
        void onRouteChange();
    }
    //endregion
}
