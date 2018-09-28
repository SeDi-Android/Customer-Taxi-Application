package ru.sedi.customerclient.classes.Orders;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.ServerManager.ParserManager;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LogUtil;


public class ActiveOrdersMonitoring {
    private static ActiveOrdersMonitoring mInstance;
    private final int TIMEOUT = 10000;
    private ArrayList<IOrderMonitoringListener> mListeners = new ArrayList<>();
    private boolean mIsMonitored;
    private Handler mHandler;

    public synchronized static ActiveOrdersMonitoring getInstance() {
        if (mInstance == null)
            mInstance = new ActiveOrdersMonitoring();
        return mInstance;
    }

    private ActiveOrdersMonitoring() {
    }

    private void startMonitoring() {
        if (mIsMonitored) {
            updateActiveOrders();
            return;
        }

        mIsMonitored = true;
        mHandler = new Handler();
        mHandler.post(getRunnable());
    }

    public void stopMonitoring() {
        if (!mIsMonitored) return;
        mIsMonitored = false;
    }

    public synchronized void addListener(IOrderMonitoringListener listener) {
        if (listener == null) return;
        if (mListeners.contains(listener)) {
            return;
        }
        mListeners.add(listener);
        startMonitoring();
    }

    public synchronized void removeListener(IOrderMonitoringListener listener) {
        if (listener == null) return;
        if (!mListeners.contains(listener)) {
            return;
        }
        mListeners.remove(listener);
    }

    private synchronized void notifyListeners(QueryList<_Order> orders) {
        for (IOrderMonitoringListener listener : mListeners) {
            listener.onOrdersUpdateChanges(orders);
        }
    }

    private Runnable getRunnable() {
        return () -> {
            if (mIsMonitored && !mListeners.isEmpty()) {
                updateActiveOrders(false);
            } else {
                stopMonitoring();
            }
        };
    }

    public void updateActiveOrders() {
        updateActiveOrders(true);
    }

    private void updateActiveOrders(boolean isExternalCall) {
        new AsyncJob.Builder<QueryList<_Order>>()
                .doWork(() -> {
                    Server server = ServerManager.GetInstance().getOrders(null);
                    return ParserManager.parseOrders(server);
                })
                .onSuccess(orders -> {
                    notifyListeners(orders);
                    if (!isExternalCall)
                        mHandler.postDelayed(getRunnable(), TIMEOUT);
                })
                .onFailure(throwable -> {
                    LogUtil.log(LogUtil.ERROR, throwable.getMessage());
                    if (!isExternalCall)
                        mHandler.postDelayed(getRunnable(), TIMEOUT);
                })
                .build().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
