package ru.sedi.customerclient.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.PhoneWrapper;
import ru.sedi.customerclient.NewDataSharing._Driver;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Phone;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Device;

public class DriverInfoDialog extends AppCompatDialog {

    private static final int LAYOUT_RES_ID = R.layout.dialog_driver_info;
    private final Picasso mPicasso;

    private final String DRIVER_PHOTO_URL_FORMAT = "%1$s/handlers/sedi/image.ashx?type=employee&id=%2$d";

    private final Unbinder mUnbinder;

    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_car_info) TextView tvCarInfo;
    @BindView(R.id.tv_route_info) TextView tvRouteInfo;
    @BindView(R.id.rb_rating) RatingBar rbRating;
    @BindView(R.id.btn_call) Button btn_call;
    @BindView(R.id.iv_photo) ImageView ivPhoto;
    @BindView(R.id.pb_photo_loader) ProgressBar pbPhotoLoader;

    public DriverInfoDialog(Context context, _Order order) {
        super(context);
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(LAYOUT_RES_ID);
        updateWindowAttrs();
        mUnbinder = ButterKnife.bind(this);
        mPicasso = Picasso.with(getContext());

        if (order == null) {
            showIncorrectOrderInfoDialog(getContext().getString(R.string.order_is_null));
            return;
        }

        setDriverInfo(order.getDriver());
        setRouteInfo(order.getRoute());
    }

    private void showIncorrectOrderInfoDialog(String errorMessage) {
        MessageBox.show(getContext(), errorMessage);
    }

    private void updateWindowAttrs() {
        if (getWindow() == null) return;
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);
    }

    private void setDriverInfo(_Driver driver) {
        if (driver == null) {
            showIncorrectOrderInfoDialog(getContext().getString(R.string.driver_is_null));
            return;
        }

        setTitle(driver.getName());
        //tvName.setText(driver.getName());
        tvCarInfo.setText(driver.getCar().getCarInfo());

        float rating = driver.getRating();
        rating = (rating < 0) ? 0 : Math.round(rating / 2);
        rbRating.setMax(5);
        rbRating.setRating(rating);

        QueryList<_Phone> phones = driver.getPhones();
        if (phones == null || phones.isEmpty()) btn_call.setVisibility(View.GONE);
        else {
            btn_call.setOnClickListener(v -> {
                callToDriver(phones);
            });
        }

        String imageUrl = getImageUrl(driver.getID());
        mPicasso.load(Uri.parse(imageUrl)).into(ivPhoto, new Callback() {
            @Override
            public void onSuccess() {
                pbPhotoLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                pbPhotoLoader.setVisibility(View.INVISIBLE);
                ivPhoto.setImageDrawable(ContextCompat.getDrawable(getContext(),
                        R.drawable.default_user_icon));
            }
        });
    }

    private void callToDriver(QueryList<_Phone> phones) {
        QueryList<PhoneWrapper> phoneItems = new QueryList<>();
        _Phone p = phones.FirstOrDefault(item -> item.getType().equalsIgnoreCase(_Phone.MOBILE_WORK));
        if (p != null)
            phoneItems.add(new PhoneWrapper(p, getContext().getString(R.string.call_to_driver)));

        p = phones.FirstOrDefault(item -> item.getType().equalsIgnoreCase(_Phone.DISPATCHER));
        if (p != null)
            phoneItems.add(new PhoneWrapper(p, getContext().getString(R.string.call_to_dispatcher)));

        if (phoneItems.isEmpty()) {
            MessageBox.show(getContext(), R.string.msg_driver_phone_not_found);
            return;
        }

        QueryList<String> numberDesc = phoneItems.Select(PhoneWrapper::getDesc);
        new AlertDialog.Builder(getContext())
                .setItems(numberDesc.toArray(new String[numberDesc.size()]), (dialog, which) -> {
                    String number = phoneItems.get(which).getPhone().getNumber();
                    if (Device.hasSim(getContext())) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + number));
                        getContext().startActivity(callIntent);
                    } else
                        MessageBox.show(getContext(),
                                getContext().getString(R.string.msg_DriverPhoneNumberIs_, number));
                })
                .create().show();
    }

    private void setRouteInfo(_Route routeInfo) {
        if (routeInfo == null || routeInfo.isEmpty()) {
            showIncorrectOrderInfoDialog(getContext().getString(R.string.route_is_null));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (_Point p : routeInfo.getPoints()) {
            if (!sb.toString().isEmpty())
                sb.append("→");
            sb.append(p.asString(true));
        }
        tvRouteInfo.setText(sb.toString());
    }

    private String getImageUrl(int driverId) {
        String chanel = getContext().getString(R.string.groupChanel);
        boolean isHttp = Server.isHttp(chanel);
        chanel = isHttp ? "http://" + chanel : "https://" + chanel;
        return String.format(DRIVER_PHOTO_URL_FORMAT, chanel, driverId);
    }

    @Override
    public void dismiss() {
        mPicasso.cancelRequest(ivPhoto);
        mUnbinder.unbind();
        super.dismiss();
    }


}
