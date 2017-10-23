package ru.sedi.customerclient.activitys.route;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kg.ram.asyncjob.AsyncJob;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.RouteHistory;
import ru.sedi.customerclient.NewDataSharing._Order;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.activitys.exemplary_location.ExemplaryLocationActivity;
import ru.sedi.customerclient.adapters.AddressHistoryAdapter;
import ru.sedi.customerclient.base.BaseActivity;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Const;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.AsyncAction.IAction;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.MessageBox.UserChoiseListener;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.tasks.AddressDetailTask;
import ru.sedi.customerclient.tasks.AutocompleteTask;

public class RouteActivity extends BaseActivity {

    @BindView(R.id.llFirst) LinearLayout llFirst;
    @BindView(R.id.llFirstAddressLayout) LinearLayout llFirstAddressLayout;
    @BindView(R.id.llRouteList) LinearLayout llRouteList;
    @BindView(R.id.acFirstPoint) AutoCompleteTextView acFirstPoint;
    @BindView(R.id.etFirstEntrance) EditText etFirstEntrance;
    @BindView(R.id.acInputAddress) AutoCompleteTextView acInputAddress;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.rvList) RecyclerView rvList;
    @BindView(R.id.scrollView2) ScrollView scrollView;
    @BindView(R.id.tilEntrance) TextInputLayout tilEntrance;

    private RecyclerView.Adapter recycleAdapter = null;
    private _Point mFirstAddress = null;
    private _Order mOrder;
    private _Point mTempSediAddress = null;
    private AutocompleteTask mAutoCompliteTask;
    private AddressDetailTask mAddressDetailTask;
    private QueryList<_Point> mSediAddresses = new QueryList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvt_route);
        updateTitle(R.string.input_address, R.drawable.ic_pencil);
        ButterKnife.bind(this);

        mOrder = _OrderRegistrator.me().getOrder();
        if (mOrder.getRoute().isEmpty())
            mOrder.getRoute().addByIndex(0, new _Point());
        init();
        updateTabs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationService.me().startListener();
        updateAutocompliteField();
        updateRouteList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationService.me().stopListener();
        _OrderRegistrator.me().calculate(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!mOrder.getRoute().isValidRoute())
                mOrder.getRoute().setPoints(new QueryList<_Point>());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        tilEntrance.setVisibility(App.isTaxiLive ? View.GONE : View.VISIBLE);

        acFirstPoint.addTextChangedListener(getTextWatcher(acFirstPoint));
        acInputAddress.addTextChangedListener(getTextWatcher(acInputAddress));

        acFirstPoint.setOnItemClickListener((parent, view, position, id) -> autocompliteListener(true, position));
        acInputAddress.setOnItemClickListener((parent, view, position, id) -> autocompliteListener(false, position));

        acFirstPoint.setOnFocusChangeListener((v, hasFocus) -> {
            boolean subInFocus = etFirstEntrance.hasFocus();
            boolean visibleFirstLayout = llFirstAddressLayout.getVisibility() == View.VISIBLE;
            if (!hasFocus && !subInFocus && visibleFirstLayout) {
                String first = acFirstPoint.getText().toString();
                String defCity = Prefs.getString(PrefsName.LAST_CITY) + ", ";
                if (!first.isEmpty() && defCity.equalsIgnoreCase(first))
                    return;
                onAddFirstClick();
            }
        });

        etFirstEntrance.setOnFocusChangeListener((v, hasFocus) -> {
            boolean subInFocus = acFirstPoint.hasFocus();
            boolean visibleFirstLayout = llFirstAddressLayout.getVisibility() == View.VISIBLE;
            if (!hasFocus && !subInFocus && visibleFirstLayout) {
                String first = acFirstPoint.getText().toString();
                String defCity = Prefs.getString(PrefsName.LAST_CITY) + ", ";
                if (!first.isEmpty() && defCity.equalsIgnoreCase(first))
                    return;
                onAddFirstClick();
            }
        });


        updateAutocompliteField();
    }

    private void updateTabs() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.history));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_routes));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    recycleAdapter = getHistoryAdapter();
                } else if (position == 1) {
                    recycleAdapter = getRouteHistoryAdapter();
                }
                rvList.setAdapter(recycleAdapter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        rvList.setAdapter(getHistoryAdapter());
    }

    private AddressHistoryAdapter getHistoryAdapter() {
        return Collections.me().getAddressHistory().getAdapter(new IAction<_Point>() {
            @Override
            public void Action(_Point param) {
                if (getCurrentFocus() != null && getCurrentFocus() instanceof AutoCompleteTextView) {
                    String address = param.asString(true);
                    ((AutoCompleteTextView) getCurrentFocus()).setText(address, false);
                    ((AutoCompleteTextView) getCurrentFocus()).setSelection(address.length());

                    if (getCurrentFocus().equals(acFirstPoint))
                        mFirstAddress = param;
                    else
                        mTempSediAddress = param;
                }
            }
        });
    }

    public RecyclerView.Adapter getRouteHistoryAdapter() {
        return Collections.me().getRoutesHistory().getAdapter(new IAction<RouteHistory>() {
            @Override
            public void Action(RouteHistory param) {
                mOrder.getRoute().clearPoints();
                mOrder.getRoute().setPoints(param.getRoute());
                Prefs.setValue(PrefsName.LAST_CITY, param.getRoute().get(param.getRoute().size() - 1).getCityName());
                updateAutocompliteField();
                updateRouteList();

                scrollView.fullScroll(View.FOCUS_UP);
            }
        });
    }

    private void autocompliteListener(boolean isFirst, int index) {
        if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
            mAddressDetailTask.cancel(true);

        _Point sediAddress = mSediAddresses.tryGet(index);

        if (sediAddress == null) return;

        mAddressDetailTask = new AddressDetailTask(point -> {
            if (isFirst) {
                mFirstAddress = point.copy();
                if (mFirstAddress != null) {
                    acFirstPoint.setText(mFirstAddress.asString() + " ", false);
                    acFirstPoint.setSelection(acFirstPoint.getText().length());
                }
            } else {
                mTempSediAddress = point.copy();
                if (mTempSediAddress != null) {
                    acInputAddress.setText(mTempSediAddress.asString() + " ", false);
                    acInputAddress.setSelection(acInputAddress.getText().length());
                }
            }
        });
        mAddressDetailTask.execute(sediAddress);
    }

    private void updateAutocompliteField() {
        acFirstPoint.setText("", false);
        acInputAddress.setText("", false);


        if (!TextUtils.isEmpty(Prefs.getString(PrefsName.LAST_CITY))) {
            String lastCity = Prefs.getString(PrefsName.LAST_CITY) + ", ";

            acFirstPoint.setText(lastCity, false);
            acFirstPoint.setSelection(acFirstPoint.length());

            QueryList<_Point> points = mOrder.getRoute().getPoints();
            _Point p = points.tryGet(0);
            if (!points.isEmpty() && (p != null && p.getChecked())) {
                acInputAddress.setText(lastCity, false);
                acInputAddress.setSelection(acFirstPoint.length());
            }

            if (llFirstAddressLayout.getVisibility() == View.VISIBLE)
                acFirstPoint.requestFocus();
            else
                acInputAddress.requestFocus();
        }
    }

    private void updateRouteList() {
        llFirst.removeAllViews();
        llRouteList.removeAllViews();

        QueryList<_Point> points = mOrder.getRoute().getPoints();
        if (points.size() > 0 && points.get(0).getChecked()) {
            llFirstAddressLayout.setVisibility(View.GONE);
            View firstPointView = getRouteItemView(0);
            llFirst.addView(firstPointView);
        } else {
            llFirstAddressLayout.setVisibility(View.VISIBLE);
            acFirstPoint.requestFocus();
        }
        if (points.size() > 1) {
            for (int i = 1; i < mOrder.getRoute().size(); i++)
                llRouteList.addView(getRouteItemView(i));
        }
    }

    private View getRouteItemView(final int index) {
        final _Point a = mOrder.getRoute().getByIndex(index);
        if (a == null || !a.getChecked())
            return new View(getBaseContext());
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_single_imagebutton, null);

        TextView tvAddress = (TextView) v.findViewById(R.id.tvListViewElement);
        tvAddress.setTextColor(getResources().getColor(R.color.primaryColor));
        tvAddress.setText(a.asString(true));

        ImageButton btnRemove = (ImageButton) v.findViewById(R.id.ibtnListView);
        btnRemove.setBackgroundResource(R.drawable.btn_orange);
        btnRemove.setImageDrawable(ContextCompat.getDrawable(RouteActivity.this, R.drawable.ic_delete));
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBox.show(RouteActivity.this, R.string.msg_RemoveAddressFromRouteQuestion, -1, new UserChoiseListener() {
                    @Override
                    public void OnOkClick() {
                        super.OnOkClick();
                        mOrder.getRoute().remove(a);
                        updateRouteList();
                        updateAutocompliteField();
                    }
                }, true, new int[]{R.string.yes, R.string.no});
            }
        });
        return v;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK && null != data) {
                    try {
                        final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        View view = getCurrentFocus();
                        if (view instanceof AppCompatAutoCompleteTextView)
                            ((AutoCompleteTextView) view).setText(text.get(0));
                        if (view instanceof AppCompatEditText)
                            ((EditText) view).setText(text.get(0));
                    } catch (Exception e) {
                        BaseActivity.Instance.showDebugMessage(44, e);
                    }
                }
                break;
            }
        }
    }

    private TextWatcher getTextWatcher(final AutoCompleteTextView field) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (field.isPerformingCompletion() || before > count) {
                    if (!field.isPerformingCompletion()) {
                        mTempSediAddress = null;
                        mFirstAddress = null;
                    }
                    return;
                }

                if (before > count && !TextUtils.isEmpty(s))
                    Prefs.setValue(PrefsName.LAST_CITY, "");

                if (mFirstAddress != null)
                    mFirstAddress.setChecked(false);

                if (mTempSediAddress != null)
                    mTempSediAddress.setChecked(false);

                if ((mTempSediAddress != null && mTempSediAddress.isMinimalAddress())
                        || (mFirstAddress != null && mFirstAddress.isMinimalAddress()))
                    return;

                if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
                    mAddressDetailTask.cancel(true);

                if (mAutoCompliteTask != null && !mAutoCompliteTask.isCancelled())
                    mAutoCompliteTask.cancel(true);

                mAutoCompliteTask = new AutocompleteTask(RouteActivity.this, field, mSediAddresses);
                LatLong location = LocationService.with(RouteActivity.this).getLocation();
                mAutoCompliteTask.execute(new Pair<>(s.toString(), location));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    private void setAutocompliteResponce(AutoCompleteTextView field, List<_Point> points) {
        if (points == null || points.size() < 1)
            return;

        mSediAddresses = new QueryList<>(points);

        QueryList<String> s = new QueryList<>();
        for (_Point object : mSediAddresses)
            s.add(object.getDesc() + " ");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(),
                R.layout.list_yandex_address, R.id.tvYandexAddress, s);

        if (field != null) {
            field.setAdapter(adapter);
            field.showDropDown();
        }
    }

    private void saveAddress() {
        if (mTempSediAddress == null)
            return;

        if (mOrder.getRoute().checkSequence(mTempSediAddress)) {
            MessageBox.show(RouteActivity.this, getString(R.string.address_sequence_message));
            return;
        }

        mOrder.getRoute().addPoint(mTempSediAddress);
        Prefs.setValue(PrefsName.LAST_CITY, mTempSediAddress.getCityName());
        mTempSediAddress = null;

        updateAutocompliteField();

        updateRouteList();
    }

    private void saveFirstAddress() {
        if (mFirstAddress == null)
            return;

        //Если адрес полный - дописывам подъезд.
        if (!TextUtils.isEmpty(etFirstEntrance.getText().toString())) {
            String e = etFirstEntrance.getText().toString();
            if (TextUtils.isEmpty(e))
                return;
            mFirstAddress.setEntranceNumber(Integer.parseInt(e));
        }

        mOrder.getRoute().setByIndex(0, mFirstAddress.copy());
        Prefs.setValue(PrefsName.LAST_CITY, mFirstAddress.getCityName());
        mFirstAddress = null;

        updateAutocompliteField();
        etFirstEntrance.setText(Const.EmptyStr);

        updateRouteList();
    }

    private void checkAddressInGeoService(final String address, final boolean isFirstAddress) {
        if (TextUtils.isEmpty(address))
            return;

        _Point point = mFirstAddress;
        if (!isFirstAddress)
            point = mTempSediAddress;

        if (point != null && point.isMinimalAddress()) {
            String house = address.replace(point.asString(), "");
            if (!house.isEmpty())
                point.setHouseNumber(house);
        }

        if (point == null) {
            new AsyncJob.Builder<_Point>()
                    .withProgress(this, R.string.check_address_action)
                    .onSuccess(p -> {
                        if (p == null) {
                            MessageBox.show(RouteActivity.this, getString(R.string.msg_EmptyAnswerGeoCoder), null);
                            return;
                        }
                        if (isFirstAddress) {
                            mFirstAddress = p;
                            saveFirstAddress();
                        } else {
                            mTempSediAddress = p;
                            saveAddress();
                        }
                    }).onFailure(throwable -> MessageBox.show(this, throwable.getMessage()))
                    .buildAndExecute();
        } else {
            _Point finalPoint = point;
            new AsyncJob.Builder<_Point>()
                    .withProgress(this, R.string.check_address_action)
                    .doWork(() -> ServerManager.GetInstance().findAddress(finalPoint))
                    .onSuccess(p -> {
                        if (p == null) {
                            MessageBox.show(RouteActivity.this, getString(R.string.msg_EmptyAnswerGeoCoder), null);
                            return;
                        }
                        if (isFirstAddress) {
                            mFirstAddress = p;
                            saveFirstAddress();
                        } else {
                            mTempSediAddress = p;
                            saveAddress();
                        }
                    }).onFailure(throwable -> MessageBox.show(this, throwable.getMessage()))
                    .buildAndExecute();
        }
    }

    @OnClick(R.id.btnSaveAddress)
    @SuppressWarnings("unused")
    public void saveAndFinish() {
        String lastCity = Prefs.getString(PrefsName.LAST_CITY) + ",";

        String firstAddress = acFirstPoint.getText().toString().trim();
        if ((llFirstAddressLayout.getVisibility() == View.VISIBLE)
                && !TextUtils.isEmpty(firstAddress)
                && !firstAddress.equalsIgnoreCase(lastCity)
                && (mFirstAddress == null || !mFirstAddress.getChecked())) {
            checkAddressInGeoService(firstAddress, true);
            return;
        }

        if (mOrder.getRoute().size() > 1 && !mOrder.getRoute().isValidRoute()) {
            MessageBox.show(RouteActivity.this, getString(R.string.need_first_address_message));
            updateAutocompliteField();
            return;
        }

        String address = acInputAddress.getText().toString().trim();
        if (!TextUtils.isEmpty(address)
                && !address.equalsIgnoreCase(lastCity)
                && (mTempSediAddress == null || !mTempSediAddress.getChecked())) {
            checkAddressInGeoService(address, false);
            return;
        }

        if (!mOrder.getRoute().isValidRoute())
            mOrder.getRoute().setPoints(new QueryList<_Point>());

        saveFirstAddress();
        saveAddress();
        closeActivity(RouteActivity.this);

    }

    public void onAddFirstClick() {
        if (mFirstAddress != null && mFirstAddress.getChecked()) {
            saveFirstAddress();
            return;
        }
        if (acFirstPoint.getText().length() < 1) {
            showSoftKeyboard(RouteActivity.this);
            return;

        }
        checkAddressInGeoService(acFirstPoint.getText().toString(), true);
    }

    @OnClick(R.id.ibtnAdd)
    @SuppressWarnings("unused")
    public void onAddClick() {
        if (mTempSediAddress != null && mTempSediAddress.getChecked()) {
            saveAddress();
            return;
        }
        if (acInputAddress.getText().length() < 1) {
            showSoftKeyboard(RouteActivity.this);
            return;

        }
        checkAddressInGeoService(acInputAddress.getText().toString(), false);
    }

    @OnClick(R.id.ibtnSearchMeByGps)
    @SuppressWarnings("unused")
    public void getAddressByLocation() {
        if (!LocationService.me().onceProviderEnabled()) {
            MessageBox.show(RouteActivity.this, R.string.msg_LocationSettingsOff, -1, new UserChoiseListener() {
                @Override
                public void OnOkClick() {
                    super.OnOkClick();
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }, true, new int[]{R.string.yes, R.string.no});
            return;
        }

        startActivity(new Intent(this, ExemplaryLocationActivity.class));
    }

    @OnClick(R.id.ibtnShowMap)
    @SuppressWarnings("unused")
    public void onShowMapClick() {
        startNewActivity(MapInputAddressActivity.class, null);
    }

    @OnClick(R.id.ibtnVoiceInput)
    @SuppressWarnings("unused")
    public void onVoiceRecognitionClick() {
        SpeechRecognitionHelper.run(this);
    }
}
