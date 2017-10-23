package ru.sedi.customerclient.adapters;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.RouteHistory;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class RouteHistoryAdapter extends RecyclerView.Adapter<RouteHistoryAdapter.RouteHistoryHolder> {

    private QueryList<RouteHistory> mRouteHistories;
    private IAction<RouteHistory> mAction;

    public RouteHistoryAdapter(QueryList<RouteHistory> histories, IAction<RouteHistory> action) {
        mRouteHistories = histories;
        mAction = action;
    }

    @Override
    public RouteHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteHistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(RouteHistoryHolder holder, int position) {
        holder.update(mRouteHistories.tryGet(position));
    }

    @Override
    public int getItemCount() {
        return mRouteHistories.size();
    }

    public class RouteHistoryHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvRoute) TextView tvRoute;

        private RouteHistory mRouteHistory;
        private View mView;

        public RouteHistoryHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (mAction != null && mRouteHistory != null)
                    mAction.Action(mRouteHistory);
            });
        }

        public void update(RouteHistory routeHistory) {
            if (routeHistory == null)
                return;

            mRouteHistory = routeHistory;

            tvName.setText(routeHistory.getName());
            tvRoute.setText(routeHistory.getRouteString());
        }

        @OnClick(R.id.ibtnRemove)
        @SuppressWarnings("unused")
        public void onRemoveClick() {
            if (mRouteHistory == null) return;

            new AlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.remove_question)
                    .setPositiveButton(R.string.yes, (dialog, which) -> mRouteHistories.remove(mRouteHistory))
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        }
    }
}
