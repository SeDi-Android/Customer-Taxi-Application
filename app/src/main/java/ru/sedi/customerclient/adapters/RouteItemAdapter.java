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
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class RouteItemAdapter extends RecyclerView.Adapter<RouteItemAdapter.AddressHistoryHolder> {

    private QueryList<_Point> mPoints;


    public RouteItemAdapter(QueryList<_Point> points) {
        mPoints = points;
    }

    @Override
    public AddressHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_single_imagebutton, parent, false);
        return new AddressHistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(AddressHistoryHolder holder, int position) {
        holder.update(position);
    }

    @Override
    public int getItemCount() {
        return mPoints.size();
    }

    class AddressHistoryHolder extends RecyclerView.ViewHolder {
        private View mView;

        AddressHistoryHolder(final View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void update(final int position) {
            _Point p = mPoints.tryGet(position);
            if (p == null || !p.getChecked()) {
                mView.setVisibility(View.GONE);
                return;
            }

            ((TextView) mView.findViewById(R.id.tvListViewElement)).setText(p.asString(true));
            ((ImageButton) mView.findViewById(R.id.ibtnListView)).setImageDrawable(ContextCompat.getDrawable(mView.getContext(), R.drawable.ic_delete));
            mView.findViewById(R.id.ibtnListView).setBackgroundResource(R.drawable.btn_gray);
            mView.findViewById(R.id.ibtnListView).setOnClickListener(v -> new AlertDialog.Builder(mView.getContext())
                    .setMessage(R.string.remove_question)
                    .setPositiveButton(R.string.yes,
                            (dialog, which) -> {
                                int i = mPoints.indexOf(p);
                                mPoints.remove(p);
                                if(i < 0)
                                    notifyDataSetChanged();
                                else
                                    notifyItemRemoved(i);
                            })
                    .setNegativeButton(R.string.no, null)
                    .create().show());
        }
    }
}
