package ru.sedi.customerclient.activitys.choose_tariff;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.activitys.active_orders_activity.ActiveOrdersActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.TinyBinaryFormatter.BinaryArray;
import ru.sedi.customerclient.common.TinyBinaryFormatter.BinaryObject;
import ru.sedi.customerclient.common.TinyBinaryFormatter.TinyFormatter;
import ru.sedi.customerclient.dialogs.OrderConfirmationDialog;
import ru.sedi.customerclient.interfaces.IAction;

public class ChooseTariffActivity extends BaseActivity {

    public static final String TARIFFS = "TARIFFS";

    private TextView tvDurationAndDistance;
    private ListView lvTariffs;
    private _Order mOrder;
    private QueryList<_Tariff> mTariffs = new QueryList<>();
    private IAction postRegisterAction;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_calc_cost);
        mOrder = _OrderRegistrator.me().getOrder();

        if (getIntent().hasExtra(TARIFFS))
            setTariffs(getIntent().getByteArrayExtra(TARIFFS));

        updateTitle(R.string.cost_calculation, R.drawable.ic_cash_multiple);
        if (getSupportActionBar() != null)
            getSupportActionBar().setElevation(0);

        if (mTariffs.isEmpty()) {
            MessageBox.show(this, getString(R.string.msg_NoTariffs));
            return;
        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTariffs.size() == 1 && App.isAuth)
            new OrderConfirmationDialog(ChooseTariffActivity.this, mOrder, postRegisterAction).show();
    }

    private void init() {
        postRegisterAction = () -> {
            startActivity(ActiveOrdersActivity.getIntent(this));
            finish();
        };

        if (App.isAuth) {
            Collections.me().getUser().updateBalance(ChooseTariffActivity.this, false);
        }

        tvDurationAndDistance = (TextView) this.findViewById(R.id.cca_tvDurationAndDistance);
        lvTariffs = (ListView) this.findViewById(R.id.cca_lvTariffs);
        initData();
    }

    /**
     * Вставляем данные в UI.
     */
    private void initData() {
        try {
            ListAdapter adapter = Collections.me().getTariffs().getAdapter(this, mTariffs);
            lvTariffs.setAdapter(adapter);
            lvTariffs.setOnItemClickListener((adapterView, view, i, l) -> {
                if (!App.isAuth) {
                    showRegistrationDialog(ChooseTariffActivity.this);
                    return;
                }

                _Tariff tariff = mTariffs.get(i).copy();
                mOrder.setTariff(tariff);
                new OrderConfirmationDialog(ChooseTariffActivity.this, mOrder, postRegisterAction).show();
            });

            _Tariff tariff = mTariffs.get(0);
            mOrder.setTariff(tariff);
            lvTariffs.setItemChecked(0, true);
            tvDurationAndDistance.setText(getOrderDistanceAndDuration());
        } catch (Exception e) {
            showDebugMessage(49, e);
        }
    }

    /**
     * Данные о длительности расстоянии.
     */
    private String getOrderDistanceAndDuration() {
        try {
            return String.format(getString(R.string.CalcCostRouteTime), mOrder.getDistance(), mOrder.getDuration());
        } catch (Exception e) {
            showDebugMessage(50, e);
            return "";
        }
    }

    /**
     * Обрабатываем тарифы.
     */
    public void setTariffs(byte[] tariffs) {
        try {
            BinaryArray binaryArray = new BinaryArray(tariffs);
            Object[] orders = binaryArray.Array();
            if (orders == null)
                return;
            mTariffs.clear();
            for (Object o : Arrays.asList(orders))
                mTariffs.add((_Tariff) TinyFormatter.Deserialize((BinaryObject) o, _Tariff.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, ChooseTariffActivity.class);
    }
}