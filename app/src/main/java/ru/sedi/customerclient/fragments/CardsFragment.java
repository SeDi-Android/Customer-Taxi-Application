package ru.sedi.customerclient.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.activitys.user_profile.NewCardDialog;
import ru.sedi.customerclient.adapters.CardAdapter;
import ru.sedi.customerclient.classes.App;

public class CardsFragment extends Fragment {

    @BindView(R.id.rvList) RecyclerView mList;

    public CardsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cards, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (!App.isAuth)
            return;

        CardAdapter adapter = Collections.me().getPaymentCards().getAdapter(getActivity());
        Collections.me().getPaymentCards().update(getContext(), false);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.card_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add_card:
                new NewCardDialog(getActivity()).show();
                break;

            case R.id.menu_update:
                Collections.me().updatePaymentCard(getActivity(), true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
