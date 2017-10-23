package ru.sedi.customerclient.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.Collections._AddressHistoryCollection;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.adapters.AddressHistoryAdapter;


public class AddressHistoryFragment extends Fragment {

    public static AddressHistoryFragment getInstance() {
        return new AddressHistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes_history, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.rvList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        _Order order = _OrderRegistrator.me().getOrder();

        _AddressHistoryCollection addressHistory = Collections.me().getAddressHistory();

        AddressHistoryAdapter adapter = addressHistory.getAdapter(param -> {
            order.getRoute().addPoint(param.copy());
            getActivity().finish();
        });

        rv.setAdapter(adapter);
    }
}
