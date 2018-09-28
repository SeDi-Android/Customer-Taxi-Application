package ru.sedi.customerclient.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.db.DBHistoryRoute;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.common.MessageBox.MessageBox;

public class SaveNewRouteDialog extends AppCompatDialog {

    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.tvRoute) TextView tvRoute;

    private Context mContext;
    private _Route mRoute;

    public SaveNewRouteDialog(Context context, _Route route) {
        super(context, R.style.AppCompatAlertDialogStyle);
        mContext = context;
        mRoute = route;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_save_route);
        updateDialogWindow();

        ButterKnife.bind(this);
        init();
    }

    private void updateDialogWindow() {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(attributes);
    }

    private void init() {
        String route = "";
        for (_Point point : mRoute.getPoints()) {
            if (!route.isEmpty())
                route += " -> ";
            route += point.asString();
        }

        tvRoute.setText(route);
    }

    @OnClick(R.id.btnSave)
    @SuppressWarnings("unused")
    public void onSaveClick() {
        String name = etName.getText().toString();

        if (name.trim().isEmpty()) {
            MessageBox.show(mContext, mContext.getString(R.string.empty_route_name_error_message));
            return;
        }

        DBHistoryRoute history = new DBHistoryRoute(name.trim(), mRoute.getPoints());
        try {
            boolean success = Collections.me().getRoutesHistory().add(history);
            if(success)
                dismiss();
        } catch (Exception e) {
            MessageBox.show(mContext, e.getMessage());
        }
    }
}
