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
import ru.sedi.customerclient.db.DBHistoryRoute;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class RouteHistoryAdapter extends RecyclerView.Adapter<RouteHistoryAdapter.RouteHistoryHolder> {

    private QueryList<DBHistoryRoute> mRouteHistories;
    private IAction<DBHistoryRoute> mAction;

    public RouteHistoryAdapter(QueryList<DBHistoryRoute> histories, IAction<DBHistoryRoute> action) {
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

        private DBHistoryRoute mDBHistoryRoute;
        private View mView;

        public RouteHistoryHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (mAction != null && mDBHistoryRoute != null)
                    mAction.Action(mDBHistoryRoute);
            });
        }

        public void update(DBHistoryRoute DBHistoryRoute) {
            if (DBHistoryRoute == null)
                return;

            mDBHistoryRoute = DBHistoryRoute;

            tvName.setText(DBHistoryRoute.getName());
            tvRoute.setText(DBHistoryRoute.getRouteString());
        }

        //@OnClick(R.id.ibtnRemove)
        @SuppressWarnings("unused")
        public void onRemoveClick() {
            if (mDBHistoryRoute == null) return;

            new AlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.remove_question)
                    .setPositiveButton(R.string.yes, (dialog, which) -> mRouteHistories.remove(mDBHistoryRoute))
                    .setNegativeButton(R.string.no, null)
                    .create().show();
        }
    }
}
