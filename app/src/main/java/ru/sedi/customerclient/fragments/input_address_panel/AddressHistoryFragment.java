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
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.db.DbHistoryPoint;
import ru.sedi.customerclient.interfaces.IAction;


public class AddressHistoryFragment extends ListFragment {

    private RealmResults<DbHistoryPoint> mHistoryPoints;
    private IAction mSaveRouteListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistoryPoints = Collections.me().getAddressHistory().getAll();
        mHistoryPoints = mHistoryPoints.sort("CityName");
        ListAdapter adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
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
        DbHistoryPoint point = mHistoryPoints.get(position);
        _OrderRegistrator.me().getOrder().getRoute().addPoint(point.toPoint());

        if(mSaveRouteListener!=null) mSaveRouteListener.action();
        getParentFragment().getFragmentManager().popBackStack();
    }

    public void setSaveRouteListener(IAction saveRouteListener) {
        mSaveRouteListener = saveRouteListener;
    }
}
