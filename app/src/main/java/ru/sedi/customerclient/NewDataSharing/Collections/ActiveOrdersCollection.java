package ru.sedi.customerclient.NewDataSharing.Collections;

import android.content.Context;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.ServerManager.ParserManager;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.classes.SendOrders.SendOrderAdapter;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.IWhere;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;

public class ActiveOrdersCollection {

    private QueryList<_Order> mOrders = new QueryList<>();
    private SendOrderAdapter mOrderAdapter;

    public _Order get(final int id) {
        return mOrders.FirstOrDefault(new IWhere<_Order>() {
            @Override
            public boolean Condition(_Order item) {
                return item.getID() == id;
            }
        });
    }

    public void remove(final _Order order) {
        QueryList<_Order> list = mOrders.Where(new IWhere<_Order>() {
            @Override
            public boolean Condition(_Order item) {
                return item.getID() == order.getID();
            }
        });
        mOrders.removeAll(list);
        notifyAdapter();
    }

    public _Order[] getAsArray() {
        return mOrders.toArray(new _Order[mOrders.size()]);
    }

    public QueryList<_Order> getAll() {
        return mOrders;
    }

    public void set(_Order[] orders) {
        mOrders = new QueryList<>(orders);
    }

    public void update(final Context context, final boolean needProgress) {
        SweetAlertDialog pd = null;
        if (needProgress) {
            pd = ProgressDialogHelper.show(context, context.getString(R.string.update_active_order_message));
        }

        final SweetAlertDialog finalPd = pd;
        AsyncAction.run(() -> ServerManager.GetInstance().getOrders(null, null), new IActionFeedback<Server>() {
                            @Override
                            public void onResponse(Server server) {
                                if (finalPd != null)
                                    finalPd.dismiss();
                                if (server.isSuccess()) {
                                    QueryList<_Order> sendOrderList = new QueryList<>();
                                    try {
                                        sendOrderList = ParserManager.parseOrders(server);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    mOrders.clear();
                                    for (_Order order : sendOrderList) {
                                        add(order);
                                    }
                                    notifyAdapter();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (finalPd != null)
                                    finalPd.dismiss();
                                if (needProgress)
                                    MessageBox.show(context, e.getMessage());
                            }
                        }
        );
    }

    public SendOrderAdapter getAdapter(Context context) {
        mOrderAdapter = new SendOrderAdapter(context, mOrders);
        return mOrderAdapter;
    }

    public void notifyAdapter() {
        if (mOrderAdapter == null)
            return;

        AsyncAction.runInMainThread(() -> mOrderAdapter.notifyDataSetChanged());
    }

    public boolean isEmpty() {
        return mOrders.isEmpty();
    }

    public void add(_Order order) {
        if (order == null)
            return;

        QueryList<_Order> orders = mOrders.Where(item -> item.getID() == order.getID());
        mOrders.removeAll(orders);
        mOrders.add(order);
        notifyAdapter();
    }
}
