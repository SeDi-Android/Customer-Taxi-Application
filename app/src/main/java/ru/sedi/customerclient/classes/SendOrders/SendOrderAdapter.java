package ru.sedi.customerclient.classes.SendOrders;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.active_order_map_activity.ActiveOrderMapActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.common.AsyncAction.AsyncAction;
import ru.sedi.customerclient.common.AsyncAction.IActionFeedback;
import ru.sedi.customerclient.common.AsyncAction.IFunc;
import ru.sedi.customerclient.common.AsyncAction.ProgressDialogHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;


public class SendOrderAdapter extends ArrayAdapter<_Order> {
    Context mContext;
    QueryList<_Order> mSendOrders;

    public SendOrderAdapter(Context context, QueryList<_Order> sendOrders) {
        super(context, R.layout.list_send_order, sendOrders);
        mContext = context;
        mSendOrders = sendOrders;
    }

    static class ViewHolder {
        public TextView tvAddresAndRoute;
        public TextView tv_OrderTime;
        public TextView tv_OrderCost;
        public TextView tv_OrderStatus;
        public TextView tvCarInfo;
        public LinearLayout carInfoLayout;
        private ImageButton ibtnRemove, ibtnMap;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            String currency = Collections.me().getUser().getCurrency();
            final ViewHolder holder;
            View rowView = convertView;
            final _Order order = mSendOrders.get(position);
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_send_order, parent, false);
                holder = new ViewHolder();
                holder.tvAddresAndRoute = (TextView) rowView.findViewById(R.id.lso_tvAddresAndRoute);
                holder.tv_OrderTime = (TextView) rowView.findViewById(R.id.lso_tvTime);
                holder.tv_OrderCost = (TextView) rowView.findViewById(R.id.lso_tvCost);
                holder.tv_OrderStatus = (TextView) rowView.findViewById(R.id.lso_tvStatus);
                holder.tvCarInfo = (TextView) rowView.findViewById(R.id.tvCarInfo);
                holder.ibtnRemove = (ImageButton) rowView.findViewById(R.id.lso_ibtnRemove);
                holder.carInfoLayout = (LinearLayout) rowView.findViewById(R.id.lso_l5);
                holder.ibtnMap = (ImageButton) rowView.findViewById(R.id.lso_ibtnMap);
                rowView.setTag(new Object[]{holder, order});
            } else holder = (ViewHolder) ((Object[]) rowView.getTag())[0];

            String strAddress = "";
            for (_Point sediAddress : order.getRoute().getPoints())
                strAddress += strAddress.length() <= 0 ? sediAddress.asString() : "→" + sediAddress.asString();
            holder.tvAddresAndRoute.setText(strAddress);
            holder.tv_OrderTime.setText(order.getDate());
            holder.tv_OrderStatus.setText(order.getStatus().getName());

            double cost = order.getCost();
            holder.tv_OrderCost.setText(
                    String.format(mContext.getString(R.string.OrderCostIs_), cost, currency));
            holder.tv_OrderCost.setVisibility(cost > 0 ? View.VISIBLE : View.GONE);

            if (order.getDriver() != null) {
                String info = order.getDriverCarInfo();
                if (!TextUtils.isEmpty(info)) {
                    holder.tvCarInfo.setText(info);
                    holder.carInfoLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.carInfoLayout.setVisibility(View.GONE);
                }
            }
            holder.ibtnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessageBox.show(mContext, mContext.getString(R.string.msg_CancelThisOrderQuestion), null, new UserChoiseListener() {
                        @Override
                        public void OnOkClick() {
                            super.OnOkClick();
                            cancelOrder(order);
                        }

                        @Override
                        public void onCancelClick() {
                            super.onCancelClick();
                        }
                    }, true, new int[]{R.string.yes, R.string.no});
                }
            });

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, ActiveOrderMapActivity.class);
                    i.putExtra("orderId", order.getID());
                    mContext.startActivity(i);
                }
            };
            holder.ibtnMap.setOnClickListener(clickListener);
            rowView.setOnClickListener(clickListener);
            return rowView;
        } catch (Exception e) {
            BaseActivity.Instance.showDebugMessage(33, e);
            return new View(BaseActivity.Instance);
        }
    }

    private void cancelOrder(final _Order send_Order) {
        final SweetAlertDialog pd = ProgressDialogHelper.show(mContext, mContext.getString(R.string.CancelledOrder));
        AsyncAction.run(new IFunc<Server>() {
            @Override
            public Server Func() throws Exception {
                return ServerManager.GetInstance().cancelOrder(send_Order.getID());
            }
        }, new IActionFeedback<Server>() {
            @Override
            public void onResponse(Server server) {
                if (pd != null)
                    pd.dismiss();
                String msg = mContext.getString(R.string.cancellation_not_possible);
                if (server.isSuccess()) {
                    msg = mContext.getString(R.string.CancelOrderSuccess);
                    Collections.me().getActiveOrders().remove(send_Order);
                }
                MessageBox.show(mContext, msg);
            }

            @Override
            public void onFailure(Exception e) {
                if (pd != null)
                    pd.dismiss();

                MessageBox.show(mContext, e.getMessage());
            }
        });

    }
}
