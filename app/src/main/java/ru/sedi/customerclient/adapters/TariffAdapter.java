package ru.sedi.customerclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;


public class TariffAdapter extends ArrayAdapter<_Tariff> {

    Context ctx;
    QueryList<_Tariff> mTariffs;

    public TariffAdapter(Context context, QueryList<_Tariff> mTariffs) {
        super(context, R.layout.item_tariff, mTariffs);
        this.ctx = context;
        this.mTariffs = mTariffs;
    }

    static class ViewHolder {
        public TextView tvTariff;
        public ImageButton ibtnDetail;
    }

    public View getView(final int position, final View convertView, ViewGroup parent) {
        try {
            final ViewHolder holder;
            View rowView = convertView;
            final _Tariff tariff = mTariffs.get(position);

            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_tariff, parent, false);
            holder = new ViewHolder();
            holder.tvTariff = (TextView) rowView.findViewById(R.id.tvTariff);
            holder.ibtnDetail = (ImageButton) rowView.findViewById(R.id.ibtnDetail);

            holder.tvTariff.setText(tariff.toString(getContext()));

            if (tariff.getDetails() == null || tariff.getDetails().length <= 0)
                holder.ibtnDetail.setVisibility(View.GONE);

            holder.ibtnDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDetail(tariff);
                }
            });

            return rowView;
        } catch (Exception e) {
            BaseActivity.Instance.showDebugMessage(65, e);
            return new View(BaseActivity.Instance);
        }
    }

    private void showDetail(_Tariff tariff) {
        MessageBox.show(ctx, tariff.getStringDetails(), tariff.toString(getContext()));
    }
}