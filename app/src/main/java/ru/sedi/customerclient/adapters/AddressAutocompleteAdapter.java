package ru.sedi.customerclient.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.common.LogUtil;

/**
 * Created by sedi_user on 20.02.2018.
 */

public class AddressAutocompleteAdapter extends ArrayAdapter<_Point> {

    private final List<_Point> mPoints;

    public AddressAutocompleteAdapter(@NonNull Context context, List<_Point> points) {
        super(context, R.layout.item_autocomplete);
        mPoints = points;
    }

    @Override
    public int getCount() {
        return mPoints.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = getInflateView(parent);
        if (mPoints.size() < position) {
            return getEmptyView(parent);
        }
        _Point point;
        try {
            point = mPoints.get(position);
        } catch (IndexOutOfBoundsException e) {
            return getEmptyView(parent);
        }

        TextView tvText = convertView.findViewById(R.id.tvText);
        TextView tvDesc = convertView.findViewById(R.id.tvDescription);

        String firstText = getFirstText(point);
        String secondText = getSecondText(point);

        if (TextUtils.isEmpty(firstText) && TextUtils.isEmpty(secondText)) {
            LogUtil.log(LogUtil.WARNING, "IGNORED: " + point.asString());
            return new View(getContext());
        }

        if (!TextUtils.isEmpty(firstText)) {
            tvText.setText(firstText);
            tvDesc.setText(secondText);
        } else {
            tvText.setText(secondText);
            tvDesc.setVisibility(View.GONE);
        }

        if (point.getType() != null) {
            tvText.setText(tvText.getText().toString());
            //String autocompleteChar = point.getType().name().substring(0,1);
            //tvText.setText(tvText.getText().toString() + " (" + autocompleteChar + ")");
        }

        return convertView;
    }

    @NonNull
    private View getEmptyView(@NonNull ViewGroup parent) {
        View view = new View(parent.getContext());
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(layoutParams!=null) {
            layoutParams.height = 0;
            view.setLayoutParams(layoutParams);
        }
        return view;
    }

    private String getSecondText(_Point point) {
        if (point.getType() != null
                && point.getType().equals(_Point.Type.GOOGLE)) {
            return getSecondByGoogle(point);
        }
        String secondText = "";
        String val = point.getCityName();
        if (!TextUtils.isEmpty(val))
            secondText = secondText.concat(val);

        val = point.getCountryName();
        if (!TextUtils.isEmpty(val)) {
            if (!secondText.isEmpty())
                secondText = secondText.concat(", ");
            secondText = secondText.concat(val);
        }
        return secondText;
    }

    @Nullable
    private String getSecondByGoogle(_Point point) {
        if (point.getGoogleStruckFormatting() == null) return null;
        String secondaryText = point.getGoogleStruckFormatting().getSecondaryText();
        if(secondaryText!=null) {
            String[] split = secondaryText.split(",");
            secondaryText = "";
            int finalIndex = split.length - 1;
            for (int i = 0; i < finalIndex; i++) {
                secondaryText += split[i];
                if (i != finalIndex - 1)
                    secondaryText += ",";
            }
        }
        return secondaryText;
    }

    private String getFirstText(_Point point) {
        if (point.getType() != null && point.getType().equals(_Point.Type.GOOGLE)) {
            return getFirstByGoogle(point);
        }
        String firstText = "";
        String val = point.getStreetName();
        if (TextUtils.isEmpty(val)) {
            val = point.getObjectName();
        }
        if (!TextUtils.isEmpty(val))
            firstText = firstText.concat(val);
        else return "";

        val = point.getHouseNumber();
        if (!TextUtils.isEmpty(val)) {
            if (!firstText.isEmpty())
                firstText = firstText.concat(" ");
            firstText = firstText.concat(val);
        }
        return firstText;
    }

    @Nullable
    private String getFirstByGoogle(_Point point) {
        if (point.getGoogleStruckFormatting() != null)
            return point.getGoogleStruckFormatting().getMainText();
        return null;
    }

    private View getInflateView(@NonNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_autocomplete, parent, false);
    }
}
