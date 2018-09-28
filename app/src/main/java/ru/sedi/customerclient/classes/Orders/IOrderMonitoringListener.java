package ru.sedi.customerclient.classes.Orders;

import java.util.List;

import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.common.LINQ.QueryList;

/**
 * Created by sedi_user on 20.03.2018.
 */

public interface IOrderMonitoringListener {
    void onOrdersUpdateChanges(QueryList<_Order> orders);
}
