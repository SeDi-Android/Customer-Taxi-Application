package ru.sedi.customerclient.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.driver_rating.DriverRatingActivity;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.enums.OrderStatuses;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Rating;
import ru.sedi.customerclient.common.LINQ.QueryList;


public class OrderHistoryAdapter extends ArrayAdapter<_Order> {

    private Activity mActivity;
    private QueryList<_Order> orders;

    public OrderHistoryAdapter(Activity activity, QueryList<_Order> orders) {
        super(activity, R.layout.list_order_history, orders);
        this.mActivity = activity;
        this.orders = orders;
    }

    static class ViewHolder {
        public TextView tvOrderId, tvTime, tvRoute, tvStatus;
        public Button btnSetRating;
        public RatingBar rbRating;
        public LinearLayout llRatingLayout;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final ViewHolder holder;
            View rowView = convertView;
            final _Order order = orders.get(position);

            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_order_history, parent, false);
            holder = new ViewHolder();
            holder.tvOrderId = (TextView) rowView.findViewById(R.id.loh_tvOrderId);
            holder.tvTime = (TextView) rowView.findViewById(R.id.loh_tvOrderTime);
            holder.tvRoute = (TextView) rowView.findViewById(R.id.loh_tvOrderRoute);
            holder.tvStatus = (TextView) rowView.findViewById(R.id.loh_tvOrderStatus);
            holder.llRatingLayout = (LinearLayout) rowView.findViewById(R.id.llRatingLayout);
            holder.rbRating = (RatingBar) rowView.findViewById(R.id.rbRating);
            holder.btnSetRating = (Button) rowView.findViewById(R.id.loh_btnSetRating);

            holder.tvOrderId.setText(String.valueOf(order.getID()));
            holder.tvTime.setText(order.getDate());
            String strAddress = "";
            for (_Point sediAddress : order.getRoute().getPoints())
                strAddress += strAddress.length() <= 0 ? sediAddress.asString() : "→" + sediAddress.asString();
            holder.tvRoute.setText(strAddress);
            holder.tvStatus.setText(order.getStatus().getName());
            holder.btnSetRating.setOnClickListener(view -> {
                mActivity.startActivityForResult(DriverRatingActivity.getIntentForResult(getContext(), String.valueOf(order.getID())),
                        DriverRatingActivity.SUCCESS_RESPONSE);
            });

            boolean isRate = order.getRating() != null;
            if(isRate){
                _Rating rating = order.getRating();
                holder.rbRating.setNumStars(rating.getRate());
                holder.rbRating.setRating(rating.getRate());
                holder.llRatingLayout.setVisibility(View.VISIBLE);
            } else {
                holder.llRatingLayout.setVisibility(View.GONE);
            }
            boolean orderIsDoneOk = order.getStatus().getID().equalsIgnoreCase(OrderStatuses.doneOk.name());
            holder.btnSetRating.setVisibility(orderIsDoneOk && !isRate ? View.VISIBLE : View.GONE);
            return rowView;
        } catch (Exception e) {
            BaseActivity.Instance.showDebugMessage(65, e);
            return new View(BaseActivity.Instance);
        }
    }
}
