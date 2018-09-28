package ru.sedi.customerclient.fragments.input_address_panel;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import io.realm.RealmResults;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.db.DBHistoryRoute;
import ru.sedi.customerclient.db.DbHistoryPoint;
import ru.sedi.customerclient.interfaces.IAction;


public class RouteHistoryFragment extends ListFragment {

    private RealmResults<DBHistoryRoute> mHistoryPoints;
    private IAction mSaveRouteListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistoryPoints = Collections.me().getRoutesHistory().getAll();
        ListAdapter adapter = new RouteHistoryListAdapter(getContext(),
                mHistoryPoints);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        DBHistoryRoute point = mHistoryPoints.get(position);
        QueryList<DbHistoryPoint> route = point.getRoute();
        _Route r = _OrderRegistrator.me().getOrder().getRoute();
        r.clearPoints();
        for (DbHistoryPoint historyPoint : route) {
            r.addPoint(historyPoint.toPoint());
        }
        if (mSaveRouteListener != null) mSaveRouteListener.action();
        getParentFragment().getFragmentManager().popBackStack();
    }

    public void setSaveRouteListener(IAction saveRouteListener) {
        mSaveRouteListener = saveRouteListener;
    }
}
