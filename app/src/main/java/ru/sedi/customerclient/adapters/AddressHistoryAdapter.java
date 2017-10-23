package ru.sedi.customerclient.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class AddressHistoryAdapter extends RecyclerView.Adapter<AddressHistoryAdapter.AddressHistoryHolder> {

    private QueryList<_Point> mPoints = null;
    private IAction<_Point> mItemClickListener;


    public AddressHistoryAdapter(QueryList<_Point> points, IAction<_Point> listener) {
        mPoints = points;
        mItemClickListener = listener;
    }

    @Override
    public AddressHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_imagebutton, parent, false);
        return new AddressHistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(AddressHistoryHolder holder, int position) {
        holder.update(mPoints.tryGet(position));
    }

    @Override
    public int getItemCount() {
        return mPoints.size();
    }

    public class AddressHistoryHolder extends RecyclerView.ViewHolder {
        private View mView;
        private _Point mPoint;

        public AddressHistoryHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void update(final _Point point) {
            if (point == null)
                return;

            mPoint = point;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null)
                        mItemClickListener.Action(mPoint);
                }
            });
            ((TextView) mView.findViewById(R.id.tvListViewElement)).setText(mPoint.asString(true));
            ((ImageButton) mView.findViewById(R.id.ibtnListView)).setImageDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.ic_delete));
            mView.findViewById(R.id.ibtnListView).setBackgroundResource(R.drawable.btn_gray);
            mView.findViewById(R.id.ibtnListView).setOnClickListener(v -> new AlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.remove_question)
                    .setPositiveButton(R.string.yes,
                            (dialog, which) -> Collections.me().getAddressHistory().remove(point))
                    .setNegativeButton(R.string.no, null)
                    .create().show());
        }
    }
}
