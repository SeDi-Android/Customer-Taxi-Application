package ru.sedi.customerclient.activitys.active_orders_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SendOrders.SendOrderAdapter;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.Toast.ToastHelper;

public class ActiveOrdersActivity extends BaseActivity {
    public static ActiveOrdersActivity Instance;
    private SendOrderAdapter mAdapter;
    private Timer mTimer = null;

    @BindView(R.id.refreshLayout) SwipeRefreshLayout refLayout;

    public static Intent getIntent(Context context){
        return new Intent(context, ActiveOrdersActivity.class);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_my_order);
        ButterKnife.bind(this);
        Instance = this;
        updateTitle(R.string.ActiveOrder, R.drawable.ic_star);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.isAuth) {
            showRegistrationDialog(this);
            return;
        }
        init();
    }

    private void init() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Collections.me().updateActiveOrders(ActiveOrdersActivity.this, false);
                }
            }, 100, 20000);

            mAdapter = Collections.me().getActiveOrders().getAdapter(ActiveOrdersActivity.this);

            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(mAdapter);
            registerForContextMenu(listView);

            refLayout.setColorSchemeResources(R.color.primaryColor);
            refLayout.setOnRefreshListener(() -> {
                Collections.me().updateActiveOrders(ActiveOrdersActivity.this, true);
                refLayout.setRefreshing(false);
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_order, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_update_item) {
            if (!App.isAuth)
                return true;
            Collections.me().updateActiveOrders(ActiveOrdersActivity.this, true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            menu.setHeaderTitle(getString(R.string.OrderAction));
            menu.add(R.string.CopyOrder);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.CopyOrder))) {
            _Order order = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
            if (order == null) {
                MessageBox.show(this, "Не возможно скопировать заказ, заказ NULL");
                return true;
            }
            _OrderRegistrator.me().copyFromHistory(order);
            ToastHelper.showShortToast(getString(R.string.success_order_copy));
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null)
            mTimer.cancel();
    }
}