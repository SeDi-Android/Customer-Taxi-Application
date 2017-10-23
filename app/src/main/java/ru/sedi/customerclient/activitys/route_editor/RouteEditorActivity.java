package ru.sedi.customerclient.activitys.route_editor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.adapters.RouteItemAdapter;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class RouteEditorActivity extends BaseActivity {

    public static Intent getIntent(Context context){
        return new Intent(context, RouteEditorActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_editor);
        updateTitle(R.string.edit, R.drawable.ic_pencil);

        RecyclerView rvList = (RecyclerView) findViewById(R.id.rvList);

        rvList.setLayoutManager(new LinearLayoutManager(this));

        QueryList<_Point> points = _OrderRegistrator.me().getOrder().getRoute().getPoints();
        RecyclerView.Adapter adapter = new RouteItemAdapter(points);
        rvList.setAdapter(adapter);
    }
}
