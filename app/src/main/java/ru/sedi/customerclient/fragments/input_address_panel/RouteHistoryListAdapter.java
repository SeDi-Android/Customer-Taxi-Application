package ru.sedi.customerclient.fragments.input_address_panel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.sedi.customer.R;
import ru.sedi.customerclient.db.DBHistoryRoute;


public class RouteHistoryListAdapter extends ArrayAdapter<DBHistoryRoute> {

    private final RealmResults<DBHistoryRoute> mRoutes;

    public RouteHistoryListAdapter(@NonNull Context context, RealmResults<DBHistoryRoute> routes) {
        super(context, R.layout.item_route, routes);
        mRoutes = routes;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        DBHistoryRoute route = mRoutes.get(position);
        String name = route.getName();
        ((TextView)convertView.findViewById(R.id.tvName)).setText(name);
        ((TextView)convertView.findViewById(R.id.tvRoute)).setText(route.getRouteString());
        convertView.findViewById(R.id.ivRemove).setOnClickListener(view -> {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<DBHistoryRoute> all = realm1.where(DBHistoryRoute.class)
                        .equalTo("mName", name).findAll();
                all.deleteAllFromRealm();
            }, this::notifyDataSetChanged);
        });
        return convertView;
    }
}
