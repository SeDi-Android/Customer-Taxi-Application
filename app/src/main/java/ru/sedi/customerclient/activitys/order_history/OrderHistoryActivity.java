package ru.sedi.customerclient.activitys.order_history;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Rating;
import ru.sedi.customerclient.ServerManager.ParserManager;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.driver_rating.DriverRatingActivity;
import ru.sedi.customerclient.adapters.OrderHistoryAdapter;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.dialogs.SaveNewRouteDialog;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.enums.PrefsName;

public class OrderHistoryActivity extends BaseActivity {

    @BindView(R.id.spnrPeriod) Spinner spnrPeriond;
    @BindView(R.id.spnrType) Spinner spnrType;

    private OrderHistoryAdapter mAdapter;

    private SpinnerItemSelected mItemSelected = new SpinnerItemSelected();
    private OrderStatuses[] mSelectedType;
    private DateTime mSelectedPeriod = null;

    private QueryList<_Order> mOrders = new QueryList<>();
    private String[] mPeriods, mTypes;
    private Unbinder mUnbinder;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_order_history);
        mUnbinder = ButterKnife.bind(this);

        mPeriods = getResources().getStringArray(R.array.TimePeriods);
        mTypes = getResources().getStringArray(R.array.Type);
        mAdapter = new OrderHistoryAdapter(this, mOrders);

        updateTitle(R.string.orders_history, R.drawable.ic_history);
        trySetElevation(0);
        init();

        Toast.makeText(this, R.string.select_period_and_update_message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!App.isAuth) {
            showRegistrationDialog(OrderHistoryActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
    }

    private void init() {
        spnrPeriond.setAdapter(createAdapter(mPeriods));
        int periodPos = Prefs.getInt(PrefsName.TYPE_SEARCH_INDEX);
        spnrPeriond.setSelection(periodPos);
        spnrPeriond.setOnItemSelectedListener(mItemSelected);

        spnrType.setAdapter(createAdapter(mTypes));
        int typePos = Prefs.getInt(PrefsName.TYPE_SEARCH_INDEX);
        spnrType.setSelection(typePos);
        spnrType.setOnItemSelectedListener(mItemSelected);

        ListView lvOrders = (ListView) this.findViewById(R.id.oha_lvOrders);
        lvOrders.setAdapter(mAdapter);
        registerForContextMenu(lvOrders);
    }

    @OnClick(R.id.ibtnSearch)
    @SuppressWarnings(Const.UNUSED)
    public void onSearchClick() {
        if (mSelectedPeriod == null) {
            resetPeriod();
        }
        loadOrdersFromServer();
    }

    private void resetPeriod() {
        mSelectedPeriod = DateTime.Now();
        mSelectedPeriod.setHour(23);
        mSelectedPeriod.setMinute(59);
    }

    private void loadOrdersFromServer() {
        new AsyncJob.Builder<QueryList<_Order>>()
                .withProgress(this, R.string.msg_PleaseWait)
                .doWork(() -> {
                    Server server = ServerManager.GetInstance().getOrders(mSelectedPeriod, mSelectedType);
                    return ParserManager.parseOrders(server);
                })
                .onSuccess(orders -> {
                    if (orders == null || orders.size() < 1) {
                        MessageBox.show(OrderHistoryActivity.this, getString(R.string.no_have_order_messsage));
                        return;
                    }
                    mOrders.clear();
                    mOrders.addAll(orders);
                    java.util.Collections.reverse(mOrders);
                    mAdapter.notifyDataSetChanged();
                })
                .onFailure(throwable -> MessageBox.show(OrderHistoryActivity.this, throwable.getMessage()))
                .buildAndExecute();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.oha_lvOrders) {
            menu.setHeaderTitle(getString(R.string.OrderAction));
            menu.add(R.string.CopyOrder);
            menu.add(R.string.save_as_my_route);
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

        if (item.getTitle().equals(getString(R.string.save_as_my_route))) {
            _Order order = mAdapter.getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position);
            if (order == null) {
                MessageBox.show(this, "Не возможно скопировать заказ, заказ NULL");
                return true;
            }
            new SaveNewRouteDialog(OrderHistoryActivity.this, order.getRoute()).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DriverRatingActivity.SUCCESS_RESPONSE && resultCode == RESULT_OK) {
            loadOrdersFromServer();
        }
    }

    public SpinnerAdapter createAdapter(String[] array) {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, array);
    }

    class SpinnerItemSelected implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            int viewId = ((View) view.getParent()).getId();
            if (viewId == R.id.spnrType) {
                if (position == 0)
                    mSelectedType = null;
                if (position == 1)
                    mSelectedType = new OrderStatuses[]{OrderStatuses.doneOk};
                if (position == 2)
                    mSelectedType = new OrderStatuses[]{OrderStatuses.cancelled, OrderStatuses.trycancel};
                if (position == 3)
                    mSelectedType = new OrderStatuses[]{OrderStatuses.doneOk};

                Prefs.setValue(PrefsName.TYPE_SEARCH_INDEX, position);
            }

            if (viewId == R.id.spnrPeriod) {
                resetPeriod();
                if (position == 0)
                    mSelectedPeriod.addMonth(-1);
                if (position == 1)
                    mSelectedPeriod.addDay(-7);
                if (position == 2)
                    mSelectedPeriod.addDay(-1);

                Prefs.setValue(PrefsName.PERIOD_SEARCH_INDEX, position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
