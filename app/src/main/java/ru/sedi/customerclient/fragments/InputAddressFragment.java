package ru.sedi.customerclient.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Route;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.route_editor.RouteEditorActivity;
import ru.sedi.customerclient.base.SingleFragmentDialogActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.common.Toast.ToastHelper;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.OnOrderChangeListener;
import ru.sedi.customerclient.tasks.AddressDetailTask;
import ru.sedi.customerclient.tasks.AutocompleteTask;
import ru.sedi.customerclient.tasks.LocationGeocodeTask;

import static android.app.Activity.RESULT_OK;


public class InputAddressFragment extends Fragment {

    @BindView(R.id.llAddresses) LinearLayout llAddresses;

    private AutocompleteTask mAutoCompleteTask;
    private AddressDetailTask mAddressDetailTask;
    private QueryList<_Point> mSediAddresses = new QueryList<>();
    private _Route mRoute;
    private _Point mTempPoint;
    private final String APLHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private AutoCompleteTextView mTargetActvField;
    private String mRecognizedText;
    private OnOrderChangeListener mListener;
    private LocationGeocodeTask mLocationGeocodeTask;

    public InputAddressFragment getInstance() {
        return new InputAddressFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_address, container, false);
        ButterKnife.bind(this, view);
        updateUi();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        if (activity instanceof OnOrderChangeListener)
            mListener = (OnOrderChangeListener) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mRecognizedText) && mTargetActvField != null) {
        /*    updateAllView();
        else {*/
            mTargetActvField.setText(mRecognizedText);
            mTargetActvField.requestFocus();
            mTargetActvField.setSelection(mTargetActvField.getText().length());
            mRecognizedText = null;
        }
    }

    private void updateAllView() {
        Helpers.hideKeyboard(getActivity().getCurrentFocus());
        if (mListener != null)
            mListener.refreshAllViews();
    }

    /**
     * Update addresses views.
     */
    public void updateUi() {
        if (!TextUtils.isEmpty(mRecognizedText) && mTargetActvField != null) return;

        mRoute = _OrderRegistrator.me().getOrder().getRoute();
        mTempPoint = new _Point();

        Map<Integer, Object> map = new LinkedHashMap<>();

        QueryList<_Point> mAllPoints = mRoute.getPoints();
        if (mAllPoints.isEmpty()) {
            map.put(0, new _Point());
        } else {
            int size = mAllPoints.size();
            if (size == 1) {
                _Point point = mAllPoints.tryGet(0);
                if (point != null && point.getChecked()) {
                    map.put(0, point);
                    map.put(1, new _Point());
                } else {
                    map.put(0, point);
                }
            }
            if (size > 1) {
                //Hack for show button with dots
                if (size > 3) {
                    map.put(0, mAllPoints.tryGet(0)); // take first
                    map.put(1, new Object()); // dots
                    int position = size - 1;
                    map.put(position, mAllPoints.tryGet(position)); //take last
                } else {
                    for (int i = 0; i < mAllPoints.size(); i++) {
                        map.put(i, mAllPoints.tryGet(i));
                    }
                }
            }
        }

        llAddresses.removeAllViews();
        int i = 0;
        for (Map.Entry<Integer, Object> entry : map.entrySet()) {
            Object o = entry.getValue();
            if (o instanceof _Point) {
                boolean isLastElement = i == (map.size() - 1);
                llAddresses.addView(getAddressView(entry.getKey(), (_Point) o, isLastElement));
            } else {
                llAddresses.addView(getDotView());
            }
            i++;
        }

        if (App.isTaxiLive)
            _OrderRegistrator.me().calculate(getContext());
    }

    /**
     * Generate view with dots for hide addresses if his >2.
     *
     * @return view with dots.
     */
    private View getDotView() {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.item_collapse_adress, null, false);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.btnMore);
        imageButton.setOnClickListener(v -> startActivity(RouteEditorActivity.getIntent(getContext())));
        return view;
    }

    /**
     * Generate view for address.
     *
     * @param i             index.
     * @param point         address point.
     * @param isLastElement last element flag.
     * @return view for address.
     */
    private View getAddressView(int i, _Point point, boolean isLastElement) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_input_adress, null, false);

        TextInputLayout tilHint = (TextInputLayout) view.findViewById(R.id.tilAddressHint);
        AutoCompleteTextView acAddress = (AutoCompleteTextView) view.findViewById(R.id.acAddress);
        FloatingActionButton fabAction = (FloatingActionButton) view.findViewById(R.id.fabAction);
        TextView tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        TextView tvPositionName = (TextView) view.findViewById(R.id.tvPositionName);

        tvPositionName.setText(String.valueOf(APLHABET.charAt(i)));//APLHABET.charAt(i));

        boolean checked = point.getChecked();

        if (!checked) {
            //Set hint
            String hint = (i == 0)
                    ? getString(R.string.FirstInputAddressHint)
                    : getString(R.string.NextInputAddressHint);
            tilHint.setHint(hint);

            //Update autocomplete field
            acAddress.setAdapter(null);
            String address = point.asString();
            String defCity = Prefs.getString(PrefsName.LAST_CITY);
            if (TextUtils.isEmpty(address) && !TextUtils.isEmpty(defCity)) {
                address += defCity + ", ";
            }
            acAddress.setText(address, false);
            acAddress.setSelection(address.length());
            acAddress.setOnItemClickListener((parent, v, position, id) -> autocompliteListener(acAddress, position));
            acAddress.setOnFocusChangeListener((v, hasFocus) -> updateFabIcon(fabAction, acAddress));
            acAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(acAddress.isPerformingCompletion()) return;

                    if (before > count) {
                        setCityName("");
                        mTempPoint = null;
                    }

                    if (mTempPoint != null)
                        mTempPoint.setChecked(false);

                    if (mAutoCompleteTask != null && !mAutoCompleteTask.isCancelled())
                        mAutoCompleteTask.cancel(true);

                    if (mTempPoint != null && mTempPoint.isMinimalAddress())
                        return;

                    mAutoCompleteTask = new AutocompleteTask(getContext(), acAddress, mSediAddresses);
                    LatLong location = LocationService.with(getContext()).getLocation();
                    mAutoCompleteTask.execute(new Pair<>(s.toString(), location));
                }

                @Override
                public void afterTextChanged(Editable s) {
                    updateFabIcon(fabAction, acAddress);
                }
            });
        } else {
            //Hide hint
            tilHint.setVisibility(View.GONE);
            //Disabled autocomplete field
            acAddress.setEnabled(false);

            //Set textview and action
            tvAddress.setEllipsize(App.isTaxiLive
                    ? TextUtils.TruncateAt.END
                    : TextUtils.TruncateAt.START);
            tvAddress.setOnClickListener(v -> showChangeOrderDialog(point));
            tvAddress.setVisibility(View.VISIBLE);
            tvAddress.setText(point.asString(true));
        }


        if (isLastElement && mListener != null && LocationService.me().onceProviderEnabled()) {
            ru.sedi.customerclient.common.AsyncAction.IAction<LatLong> action = param -> {
                if (mLocationGeocodeTask != null && !mLocationGeocodeTask.isCancelled())
                    mLocationGeocodeTask.cancel(true);
                mLocationGeocodeTask = new LocationGeocodeTask(getContext(), param1 -> {
                    mTempPoint = param1;
                    acAddress.setText(mTempPoint.asString(), false);
                    acAddress.requestFocus();
                    acAddress.setSelection(acAddress.length());
                });
                mLocationGeocodeTask.execute(param);
            };
            mListener.addMapScrollListener(checked ? null : action);
        }

        fabAction.setVisibility((checked && !isLastElement) ? View.GONE : View.VISIBLE);
        fabAction.setOnClickListener(v -> fabClick(acAddress, i, (FloatingActionButton) v));
        updateFabIcon(fabAction, acAddress);
        return view;
    }


    /**
     * Generate and show alert dialog for modification address point.
     * Modification maybe onSuccessCalculate or remove.
     *
     * @param point address point.
     */
    private void showChangeOrderDialog(_Point point) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.change_address_query_message, point.asString(true)))
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    mRoute.remove(point);
                    updateAllView();
                })
                .setNegativeButton(R.string.edit, (dialog, which) -> {
                    point.setChecked(false);
                    updateAllView();
                })
                .setNeutralButton(R.string.cancel, null)
                .create().show();
    }

    /**
     * Update floating button icon.
     *
     * @param fabAction        button.
     * @param completeTextView autocomplete text view.
     */
    private void updateFabIcon(FloatingActionButton fabAction, AutoCompleteTextView completeTextView) {
        @DrawableRes int mDrawable = R.drawable.ic_pencil;
        if (!completeTextView.isEnabled()) {
            mDrawable = R.drawable.ic_plus;
        } else if (completeTextView.hasFocus() &&
                completeTextView.isEnabled() &&
                completeTextView.getText().toString().trim().length() > 0) {
            mDrawable = R.drawable.ic_check;
        }
        fabAction.setImageDrawable(ContextCompat.getDrawable(getContext(), mDrawable));
    }

    /**
     * action for floating action button click.
     *
     * @param acAddress autocomplete field.
     * @param fab       button.
     */
    private void fabClick(AutoCompleteTextView acAddress, int index, FloatingActionButton fab) {
        String address = acAddress.getText().toString().trim();
        if ((mTempPoint == null || !mTempPoint.getChecked()) && !address.isEmpty()
                && acAddress.isEnabled() && acAddress.hasFocus()) {
            checkAddressInGeoService(index, address);
        } else {
            if (!acAddress.isEnabled()) {
                mRoute.getPoints().add(new _Point());
            } else if (address.isEmpty() || !acAddress.hasFocus()) {
                showPopupMenu(acAddress, fab);
                return;
            } else {
                mRoute.setByIndex(index, mTempPoint.copy());
                setCityName(mTempPoint.getCityName());
            }
            updateAllView();
        }
    }

    private void setCityName(String cityName) {
        Prefs.setValue(PrefsName.LAST_CITY, cityName);
    }

    /**
     * Create and show popup menu for fab.
     *
     * @param acAddress  - autocomplete field.
     * @param anchorView - anchor view.
     */
    private void showPopupMenu(AutoCompleteTextView acAddress, View anchorView) {
        Helpers.hideKeyboard(getActivity().getCurrentFocus());
        mTargetActvField = acAddress;

        PopupMenu menu = new PopupMenu(getContext(), anchorView);
        menu.inflate(R.menu.menu_input_address);
        menu.setOnMenuItemClickListener(this::actionOnPopupClick);

        MenuPopupHelper helper = new MenuPopupHelper(getContext(), (MenuBuilder) menu.getMenu(), anchorView);
        helper.setForceShowIcon(true);
        helper.show();
    }

    private boolean actionOnPopupClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location_input_item: {
                if (!LocationService.me().onceProviderEnabled()) {
                    if (mListener != null)
                        mListener.showLocationErrorDialog();
                    return false;
                }
                LatLong location = LocationService.me().getLocation();
                if (!location.isValid()) return false;
                LocationService.me().getAsyncPointByLocation(getContext(), location, point -> {
                    mTempPoint = point;
                    mTargetActvField.setText(mTempPoint.asString(), false);
                    mTargetActvField.requestFocus();
                    mTargetActvField.setSelection(mTargetActvField.getText().length());
                });
                return true;
            }
            case R.id.menu_voice_input_item: {
                SpeechRecognitionHelper.run(InputAddressFragment.this);
                return true;
            }

            case R.id.menu_from_routes_item: {
                if (Collections.me().getRoutesHistory().isEmpty()) {
                    ToastHelper.showShortToast(getString(R.string.empty_route_history_message));
                    return false;
                }
                startActivity(SingleFragmentDialogActivity.getIntent(getContext(),
                        SingleFragmentDialogActivity.ROUTE_DIALOG));
                return true;
            }

            case R.id.menu_from_history_item: {
                if (Collections.me().getAddressHistory().isEmpty()) {
                    ToastHelper.showShortToast(getString(R.string.empty_address_history_message));
                    return false;
                }
                startActivity(SingleFragmentDialogActivity.getIntent(getContext(),
                        SingleFragmentDialogActivity.POINTS_DIALOG));
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        final ArrayList<String> text = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        mRecognizedText = text.get(0);
                    } catch (Exception e) {
                        mRecognizedText = Const.EmptyStr;
                        ToastHelper.showShortToast(e.getMessage());
                    }
                }
                break;
            }
        }
    }

    private void checkAddressInGeoService(int index, final String address) {
        if (TextUtils.isEmpty(address))
            return;

        if (mTempPoint != null && mTempPoint.isMinimalAddress()) {
            String house = address.replace(mTempPoint.asString(), "");
            if (!house.isEmpty())
                mTempPoint.setHouseNumber(house.trim());
        }

        if (mTempPoint == null || !mTempPoint.isMinimalAddress()) {
            new AsyncJob.Builder<_Point>()
                    .withProgress(getContext(), R.string.check_address_action)
                    .doWork(() -> ServerManager.GetInstance().checkAddress(getContext(), address))
                    .onSuccess(p -> {
                        if (p == null) {
                            MessageBox.show(getContext(), getString(R.string.msg_EmptyAnswerGeoCoder), null);
                            return;
                        }
                        if (mRoute == null) return;
                        mRoute.setByIndex(index, p.copy());
                        setCityName(p.getCityName());
                        updateAllView();
                    }).onFailure(throwable -> MessageBox.show(getContext(), throwable.getMessage()))
                    .buildAndExecute();
        } else {
            _Point finalPoint = mTempPoint;
            new AsyncJob.Builder<_Point>()
                    .withProgress(getContext(), R.string.check_address_action)
                    .doWork(() -> {
                        _Point point = ServerManager.GetInstance().findAddress(finalPoint);
                        return point;
                    })
                    .onSuccess(p -> {
                        if (p == null) {
                            MessageBox.show(getContext(), getString(R.string.msg_EmptyAnswerGeoCoder), null);
                            return;
                        }
                        if (mRoute == null) return;
                        mRoute.setByIndex(index, p.copy());
                        setCityName(p.getCityName());
                        updateAllView();
                    }).onFailure(throwable -> MessageBox.show(getContext(), throwable.getMessage()))
                    .buildAndExecute();
        }
    }

    private void autocompliteListener(AutoCompleteTextView acAddress, int index) {
        if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
            mAddressDetailTask.cancel(true);

        _Point sediAddress = mSediAddresses.tryGet(index);

        if (sediAddress == null)
            return;

        if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
            mAddressDetailTask.cancel(true);

        mAddressDetailTask = new AddressDetailTask(point -> {
            if (point == null) return;

            mTempPoint = point;

            acAddress.setText(mTempPoint.asString() + " ", false);
            acAddress.setSelection(acAddress.getText().length());
        });
        mAddressDetailTask.execute(sediAddress);
    }

}
