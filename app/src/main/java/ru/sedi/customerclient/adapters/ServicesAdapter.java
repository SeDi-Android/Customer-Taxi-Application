package ru.sedi.customerclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Locale;

import ru.sedi.customer.R;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.NewDataSharing._Service;
import ru.sedi.customerclient.common.LINQ.QueryList;

public class ServicesAdapter extends ArrayAdapter<_Service> {
    private QueryList<_Service> mServices;
    private Context mContext;

    public ServicesAdapter(Context context, QueryList<_Service> servicesList) {
        super(context, R.layout.list_services, servicesList);
        mContext = context;
        mServices = servicesList;
    }

    private static class ViewHolder {
        TextView tvServiceName;
        CheckBox cbChecked;
    }

    public View getView(int position, View view, ViewGroup parent) {
        try {
            final ViewHolder holder;
            final _Service service = mServices.get(position);
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_services, parent, false);
                holder = new ViewHolder();
                holder.tvServiceName = (TextView) view.findViewById(R.id.ls_tvServiceName);
                holder.cbChecked = (CheckBox) view.findViewById(R.id.ls_cbChecked);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            view.setTag(holder);

            holder.tvServiceName.setText(getServiceInfo(service));

            holder.cbChecked.setChecked(service.isChecked());
            holder.cbChecked.setOnClickListener(v -> service.setChecked(holder.cbChecked.isChecked()));

            view.setOnClickListener(v -> holder.cbChecked.performClick());

            return view;
        } catch (Exception e) {
            BaseActivity.Instance.showDebugMessage(39, e);
            return new View(BaseActivity.Instance);
        }
    }

    /**
     * Return formatted string with service name, cost and unit.
     * @param service - service.
     * @return formatted string.
     */
    private String getServiceInfo(_Service service) {
        //Use format as => ServiceName(+10$)
        return String.format(Locale.getDefault(), "%s (+%.0f%s)",
                service.getName(),
                service.getCost().getValue(),
                service.getCost().getUnit());
    }


}
