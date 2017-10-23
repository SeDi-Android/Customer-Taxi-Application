package ru.sedi.customerclient.activitys.active_order_map_activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.views.MapView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.PhoneWrapper;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Phone;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.OsmMapController;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.DateTime;
import ru.sedi.customerclient.common.GeoTools.GeoTools;
import ru.sedi.customerclient.common.GeoTools.Units;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Device;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.interfaces.IAction;

import static ru.sedi.customerclient.enums.OrderStatuses.execute;

public class ActiveOrderMapActivity extends BaseActivity implements View.OnClickListener {

    public static final int MAXIMUM_ZOOMLEVEL = 18;
    private OsmMapController mController;
    private Timer mUpdateTimer = new Timer();
    private _Order mOrder;
    private _Driver mDriver;
    final int ICON_START_POINT = R.drawable.ic_map_1,
            ICON_DRIVER_POINT = R.drawable.ic_map_taxi_car;

    @BindView(R.id.mva_tvDriverName) TextView tvDriverName;
    @BindView(R.id.mva_tvDriverCar) TextView tvDriverCar;
    @BindView(R.id.mva_tvDuration) TextView tvDuration;
    @BindView(R.id.mva_llDuration) LinearLayout llDuration;

    @BindView(R.id.mva_ibtnCall) ImageButton ibtnCall;
    @BindView(R.id.mva_ibtnShowStartPoint) ImageButton ibtnShowStartPoint;
    @BindView(R.id.mva_ibtnShowDriverCar) ImageButton ibtnShowDriverCar;
    @BindView(R.id.mva_ibtnZoomIn) ImageButton ibtnZoomIn;
    @BindView(R.id.mva_ibtnZoomOut) ImageButton ibtnZoomOut;
    private int mOrderId;
    private AsyncJob<_Order> mOrderUpdateTask;


    public static Intent getIntent(Context context, int orderId) {
        Intent intent = new Intent(context, ActiveOrderMapActivity.class);
        intent.putExtra("orderId", orderId);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_map_view);
        ButterKnife.bind(this);
        updateTitle(R.string.map, R.drawable.ic_google_maps);

        if (!getIntent().hasExtra("orderId")) {
            showMessageOrderLoadError();
            return;
        }
        mOrderId = getIntent().getIntExtra("orderId", -1);
        mOrder = Collections.me().getActiveOrders().get(mOrderId);
        if (mOrder == null) {
            showMessageOrderLoadError();
            return;
        }
        mDriver = mOrder.getDriver();

        init();

        if (enabledDriverOnMap())
            goToDriver();
        else
            goToFirstPoint();
    }

    private void init() {
        MapView map = (MapView) this.findViewById(R.id.mva_mapView);
        mController = new OsmMapController(this, map);
        mController.clearAllOverlays();

        ibtnCall.setOnClickListener(this);
        ibtnShowStartPoint.setOnClickListener(this);
        ibtnShowDriverCar.setOnClickListener(this);
        ibtnZoomIn.setOnClickListener(this);
        ibtnZoomOut.setOnClickListener(this);

        tvDuration.setMovementMethod(new ScrollingMovementMethod());
        tvDuration.requestFocus();

        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                AsyncAction.runInMainThread(() -> timerTask());
            }
        }, 0, 10000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mva_ibtnShowStartPoint: {
                goToFirstPoint();
                break;
            }
            case R.id.mva_ibtnShowDriverCar: {
                goToDriver();
                break;
            }
            case R.id.mva_ibtnZoomIn: {
                mController.zoomIn();
                break;
            }
            case R.id.mva_ibtnZoomOut: {
                mController.zoomOut();
                break;
            }
            case R.id.mva_ibtnCall: {
                callToDriver();
                break;
            }
            default:
                break;
        }
    }

    /**
     * Двигаем карту на водителя.
     */
    private void goToDriver() {
        _Driver driver = mOrder.getDriver();
        if (driver != null && driver.isValid()) {
            mController.zoomTo(
                    driver.getGeo().toLatLong(),
                    MAXIMUM_ZOOMLEVEL);
        }
    }

    /**
     * Двигаем карту на 1 адреснут точку.
     */
    private void goToFirstPoint() {
        if (mOrder != null) {
            mController.zoomTo(
                    mOrder.getRoute().getByIndex(0).getLatLong(),
                    MAXIMUM_ZOOMLEVEL);
        }
    }

    /**
     * Задача для таймера.
     */
    private void timerTask() {
        try {
            mOrder = Collections.me().getActiveOrders().get(mOrderId);
            if (mOrder == null) {
                showMessageOrderLoadError();
                mUpdateTimer.cancel();
                LogUtil.log(LogUtil.ERROR, "Не удалось получить заказ по id или он NULL");
                return;
            }

            if (mOrderUpdateTask == null || mOrderUpdateTask.getStatus() == AsyncTask.Status.FINISHED) {
                mOrderUpdateTask = new AsyncJob.Builder<_Order>()
                        .doWork(() -> ServerManager.GetInstance().getOrder(mOrder.getID()))
                        .onSuccess(order -> {
                            mOrder = order;
                            mDriver = mOrder.getDriver();
                            updateMapMarker();
                        })
                        .onFailure(throwable -> LogUtil.log(LogUtil.ERROR, throwable.getMessage()))
                        .build();
            }

            if (mOrderUpdateTask.getStatus() == AsyncTask.Status.RUNNING)
                return;

            mOrderUpdateTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ошибка при загрузке заказов.
     */
    private void showMessageOrderLoadError() {
        MessageBox.show(ActiveOrderMapActivity.this, getString(R.string.msg_OrderLoadError), null, new UserChoiseListener() {
            @Override
            public void OnOkClick() {
                super.OnOkClick();
                closeActivity(ActiveOrderMapActivity.this);
            }
        }, false, new int[]{R.string.ok});
    }

    /**
     * Обновление маркеров карты.
     */
    private void updateMapMarker() {
        runOnUiThread(() -> {
            try {
                //Чистим
                mController.clearAllOverlays();

                //Первая точка маршрута
                final _Point firstPoint = mOrder.getRoute().getByIndex(0);
                if (firstPoint != null) {
                    mController.addPoint(firstPoint.getLatLong(), ICON_START_POINT, new IAction() {
                        @Override
                        public void action() {
                            mController.zoomTo(firstPoint.getLatLong(), MAXIMUM_ZOOMLEVEL);
                        }
                    });
                }

                //Водитель
                if (mDriver != null) {
                    updateDriverInfoLayout();
                    if (enabledDriverOnMap()) {
                        mController.addPoint(
                                mDriver.getGeo().toLatLong(),
                                ICON_DRIVER_POINT,
                                () -> mController.zoomTo(mDriver.getGeo().toLatLong(), MAXIMUM_ZOOMLEVEL));
                    }
                }
                //Обновляем кнопки
                updateVisibilityButtons();
            } catch (Exception e) {
                showDebugMessage(72, e);
            }
        });
    }

    /**
     * Обновляем информацию по водителю и времени прибытия на заказ.
     */
    private void updateDriverInfoLayout() {
        if (mDriver == null) return;

        runOnUiThread(() -> {
            //Имя
            if (!TextUtils.isEmpty(mDriver.getName())) {
                tvDriverName.setText(mDriver.getName());
                tvDriverName.setVisibility(View.VISIBLE);
            } else {
                tvDriverName.setVisibility(View.INVISIBLE);
            }

            //Авто
            if (!TextUtils.isEmpty(mDriver.getCar().getCarInfo())) {
                tvDriverCar.setText(mDriver.getCar().getCarInfo());
                tvDriverCar.setVisibility(View.VISIBLE);
            } else {
                tvDriverCar.setVisibility(View.INVISIBLE);
            }

            OrderStatuses status = OrderStatuses.getShortStatus(mOrder.getStatus().getID());
            if (!status.equals(OrderStatuses.execute) && !status.equals(OrderStatuses.cancelled)) {
                int minsToCustomer = getTimeToCustomer();
                String msg = getString(R.string.msg_UnknowTime);
                if (minsToCustomer > 0) {
                    msg = getString(R.string.msg_DriverInYouPlaseOn)
                            + DateTime.dateStringFromMins(ActiveOrderMapActivity.this, minsToCustomer);
                }
                tvDuration.setText(msg);
            } else {
                llDuration.setVisibility(View.GONE);
            }

            //Позвонить
            ibtnCall.setOnClickListener(v -> callToDriver());
        });
    }

    /**
     * Звонок водителю
     */
    private void callToDriver() {
        if (mDriver == null) return;

        QueryList<_Phone> phones = mDriver.getPhones();
        if (phones == null || phones.isEmpty()){
            MessageBox.show(this, R.string.msg_driver_phone_not_found);
            return;
        }

        QueryList<PhoneWrapper> phoneItems = new QueryList<>();
        _Phone p = mDriver.getPhone(_Phone.MOBILE_WORK);
        if (p != null)
            phoneItems.add(new PhoneWrapper(p, getString(R.string.call_to_driver)));

        p = mDriver.getPhone(_Phone.DISPATCHER);
        if (p != null)
            phoneItems.add(new PhoneWrapper(p, getString(R.string.call_to_dispatcher)));

        if (phoneItems.isEmpty()){
            MessageBox.show(this, R.string.msg_driver_phone_not_found);
            return;
        }

        QueryList<String> numberDesc = phoneItems.Select(PhoneWrapper::getDesc);
        new AlertDialog.Builder(this)
                .setItems(numberDesc.toArray(new String[numberDesc.size()]), (dialog, which) -> {
                    String number = phoneItems.get(which).getPhone().getNumber();
                    if (Device.hasSim(ActiveOrderMapActivity.this)) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"
                                + number));
                        ActiveOrderMapActivity.this.startActivity(callIntent);
                    } else
                        MessageBox.show(ActiveOrderMapActivity.this,
                                getString(R.string.msg_DriverPhoneNumberIs_, number));
                })
                .create().show();
    }

    /**
     * Время прибытия к заказчику.
     */
    private int getTimeToCustomer() {
        try {
            if (mDriver == null || !mDriver.getGeo().toLatLong().isValid())
                return -1;

            LatLong startPoint = mOrder.getRoute().getByIndex(0).getLatLong();
            double v = GeoTools.calculateDistance(startPoint.Latitude, startPoint.Longitude, mDriver.getGeo().getLat(), mDriver.getGeo().getLon(), Units.Kilometers);
            return (int) (Math.round((v / 40) * 60));
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
            return -1;
        }
    }

    /**
     * Оновляем кнопки
     */
    private void updateVisibilityButtons() {
        if (mOrder == null) {
            ibtnShowStartPoint.setVisibility(View.INVISIBLE);
            ibtnShowDriverCar.setVisibility(View.INVISIBLE);
            findViewById(R.id.mva_rlDriverInfo).setVisibility(View.INVISIBLE);
        } else {
            ibtnShowStartPoint.setVisibility(View.VISIBLE);
            ibtnShowDriverCar.setVisibility(enabledDriverOnMap() ? View.VISIBLE : View.INVISIBLE);
            findViewById(R.id.mva_rlDriverInfo).setVisibility((mDriver != null && mDriver.getID() > 0) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateTimer.cancel();
    }

    /**
     * Условия отображения водителя на карте.
     */
    private boolean enabledDriverOnMap() {
        if (mDriver == null || !mDriver.isValid())
            return false;

        String statusId = mOrder.getStatus().getID();
        return ((OrderStatuses.getShortStatus(statusId) == OrderStatuses.inway ||
                OrderStatuses.getShortStatus(statusId) == OrderStatuses.driverwaitcustomer ||
                OrderStatuses.getShortStatus(statusId) == execute));
    }
}