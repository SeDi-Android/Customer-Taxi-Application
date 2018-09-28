package ru.sedi.customerclient.fragments.input_address_panel;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import kg.ram.asyncjob.AsyncJob;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.sedi.customer.R;
import ru.sedi.customerclient.NewDataSharing.LocationConverter;
import ru.sedi.customerclient.NewDataSharing.SediAutocomplete;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.ServerManager.Server;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.adapters.AddressAutocompleteAdapter;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.AutocompleteQuery;
import ru.sedi.customerclient.classes.ExternalAutocomplete;
import ru.sedi.customerclient.classes.GeoLocation.LocationService;
import ru.sedi.customerclient.classes.Helpers.Helpers;
import ru.sedi.customerclient.classes.Orders._OrderRegistrator;
import ru.sedi.customerclient.classes.SpeechRecognitionHelper;
import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.MessageBox.MessageBox;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.db.DbPointsCache;
import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.interfaces.IAction;
import ru.sedi.customerclient.tasks.AddressDetailTask;
import ru.sedi.customerclient.widget.EditTextWithActionIcon;


/**
 * A simple {@link Fragment} subclass.
 */
public class InputAddressFragment extends Fragment {

    @BindView(R.id.et_address)
    EditTextWithActionIcon et_address;
    @BindView(R.id.lv_addresses)
    ListView lv_addresses;
    @BindView(R.id.fab_save)
    FloatingActionButton fab_save;
    @BindView(R.id.pb_loading)
    ProgressBar pb_loading;
    private AddressAutocompleteTask mAutoCompleteTask;
    private AddressDetailTask mAddressDetailTask;
    private _Point mTempPoint;
    private final QueryList<_Point> mAddresses = new QueryList<>();
    private AddressAutocompleteAdapter adapter;

    private IAction mDissmissAction;
    private ExternalAutocomplete externalAutocomplete;


    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int length = charSequence.length();
            if (length > 0 && charSequence.charAt(length - 1) == ' ')
                return;

            if (i1 > i2) {
                Prefs.getString("");
                mTempPoint = null;
                isRequestWithoutCity = false;
            }

            if (mTempPoint != null)
                mTempPoint.setChecked(false);

            if (mAutoCompleteTask != null)
                mAutoCompleteTask.cancel(true);

            if (length <= 0) {
                setProgressVisibility(false);
                return;
            }

            mAutoCompleteTask = new AddressAutocompleteTask(false);
            LatLong location = LocationService.with(getContext()).getLocation();
            mAutoCompleteTask.execute(new Pair<>(charSequence.toString(), location));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private int mIndex;
    private boolean mIsVoiceInput;
    private boolean isRequestWithoutCity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_input_address_view,
                container, false);
        ButterKnife.bind(this, view);

        String localeCode = Prefs.getString(PrefsName.LOCALE_CODE);
        String googleKey = getContext().getString(R.string.googleApiKey);
        externalAutocomplete = new ExternalAutocomplete(googleKey, localeCode);

        adapter = new AddressAutocompleteAdapter(getContext(), mAddresses);
        lv_addresses.setAdapter(adapter);


        String lastCity = Prefs.getString(PrefsName.LAST_CITY);
        et_address.setText(lastCity + ", ");
        et_address.setSelection(et_address.length());
        et_address.addTextChangedListener(mTextWatcher);
        et_address.setListener(() -> et_address.setText(""));


        Realm realm = Realm.getDefaultInstance();
        RealmResults<DbPointsCache> all = realm.where(DbPointsCache.class).findAll();
        for (DbPointsCache p : all) {
            mAddresses.add(new _Point(p.getCityName(), new LatLong(p.getLatitude(), p.getLongetude()),
                    false));
            adapter.notifyDataSetChanged();
        }
        lv_addresses.setOnItemClickListener((adapterView, view1, i, l) -> autocompliteListener(i));

        fab_save.setOnClickListener(view1 -> {

            boolean isValidAddress = mTempPoint != null
                    && mTempPoint.getChecked()
                    && mTempPoint.isMinimalAddress();

            if (isValidAddress) {
                savePoint();
            } else {
                if (et_address.getText().length() <= 0) {
                    MessageBox.show(getContext(), R.string.incorrect_address_message);
                    return;
                }
                checkAddressInGeoService(et_address.getText().toString());
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    try {
                        final ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        et_address.setText(text.get(0));
                        et_address.setSelection(et_address.length());
                    } catch (Exception e) {
                        LogUtil.log(e);
                    }
                }
                break;
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mTempPoint != null)
            et_address.setText(mTempPoint.asString());
        if (mIsVoiceInput) {
            SpeechRecognitionHelper.run(this);
            mTempPoint = null;
            mIsVoiceInput = false;
        }
        et_address.requestFocus();
        Helpers.showKeyboard(et_address);
    }

    private void savePoint() {
        Realm defaultInstance = Realm.getDefaultInstance();
        defaultInstance.executeTransactionAsync(realm -> {
            long count = realm.where(DbPointsCache.class).contains("CityName", mTempPoint.getCityName()).count();
            if (count > 0) return;
            DbPointsCache obj = new DbPointsCache(mTempPoint);
            realm.copyToRealm(obj);
        });

        if (mIndex >= 0) {
            _OrderRegistrator.me().getOrder().getRoute().setByIndex(mIndex, mTempPoint.copy());
        } else {
            _OrderRegistrator.me().getOrder().getRoute().addPoint(mTempPoint.copy());
        }
        Prefs.setValue(PrefsName.LAST_CITY, mTempPoint.getCityName());

        if (mDissmissAction != null) mDissmissAction.action();

        getParentFragment().getFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        if (mAutoCompleteTask != null)
            mAutoCompleteTask.cancel(true);
        if (mAddressDetailTask != null)
            mAddressDetailTask.cancel(true);
        Helpers.hideKeyboard(getView());
        super.onDestroyView();
    }

    /**
     * Поиск адреса по строке.
     *
     * @param address адрес указанные в поле.
     */
    private void checkAddressInGeoService(String address) {
        if (TextUtils.isEmpty(address))
            return;

        if (mTempPoint != null && mTempPoint.isMinimalAddress()) {
            String house = address.replace(mTempPoint.asString(), "");
            if (!house.isEmpty())
                mTempPoint.setHouseNumber(house.trim());
        }

        if (mTempPoint == null || !mTempPoint.getChecked()) {
            new AsyncJob.Builder<_Point>()
                    .withProgress(getContext(), R.string.check_address_action)
                    .doWork(() -> {
                        //Ищем как есть...
                        _Point point = getPointByStringFromSedi(address);
                        if (point != null)
                            return point;
                        point = getPointByStringFromOsm(address);
                        if (point != null)
                            return point;

                        //Добавляем город
                        String correctedAddress = getCorrectedAddress(address);
                        if (TextUtils.isEmpty(correctedAddress))
                            return null;

                        //Если город добавился, ищем уже с городом
                        point = getPointByStringFromSedi(correctedAddress);
                        if (point != null)
                            return point;
                        point = getPointByStringFromOsm(correctedAddress);
                        if (point != null)
                            return point;
                        return null;
                    })
                    .onSuccess(p -> {
                        if (p == null) {
                            MessageBox.show(getContext(), getString(R.string.msg_EmptyAnswerGeoCoder), null);
                            return;
                        }
                        mTempPoint = p.copy();
                        updateTextField();
                        savePoint();
                    }).onFailure(throwable -> MessageBox.show(getContext(), throwable.getMessage()))
                    .buildAndExecute();
        }
    }

    private String getCorrectedAddress(String originalAddress) {
        if (!originalAddress.contains(",")) {
            String city = Prefs.getString(PrefsName.CURRENT_CITY);
            return String.format("%s, %s", city, originalAddress);
        } else {
            return null;
        }
    }

    private _Point getPointByStringFromOsm(String stringAddress) {
        try {
            stringAddress = URLEncoder.encode(stringAddress, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.log(e);
        }
        try {
            return LocationService.me().getAddressByLocationPoint(getContext(), stringAddress);
        } catch (IOException e) {
            return null;
        }

    }

    private _Point getPointByStringFromSedi(String stringAddress) {
        try {
            return ServerManager.GetInstance().checkAddress(getContext(), stringAddress);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Проверяет адрес из автокомплите на необходимость уточнения (google)
     *
     * @param index порядковый индекс.
     */
    private void autocompliteListener(int index) {
        if (mAddressDetailTask != null && !mAddressDetailTask.isCancelled())
            mAddressDetailTask.cancel(true);

        _Point sediAddress = mAddresses.tryGet(index);

        if (sediAddress == null) return;
        _Point.Type type = sediAddress.getType();
        if (type != null && (type.equals(_Point.Type.SEDI)
                || (type.equals(_Point.Type.YANDEX) && sediAddress.getChecked()))) {
            mTempPoint = sediAddress.copy();
            updateTextField();
        } else {
            setProgressVisibility(true);
            mAddressDetailTask = new AddressDetailTask(point -> {
                mTempPoint = point;
                updateTextField();
                setProgressVisibility(false);
            });
            mAddressDetailTask.execute(sediAddress);
//            et_address.removeTextChangedListener(mTextWatcher);
//            et_address.setText(sediAddress.getDesc().trim() + " ");
//            et_address.setSelection(et_address.getText().length());
//            et_address.addTextChangedListener(mTextWatcher);
        }

    }

    /**
     * Обновляет текст в поле для ввода адреса.
     */
    private void updateTextField() {
        et_address.removeTextChangedListener(mTextWatcher);
        String address = mTempPoint.asString();
        address = getAddressWithoutSubregion(address);
        et_address.setText(address + " ");
        et_address.setSelection(et_address.getText().length());
        et_address.addTextChangedListener(mTextWatcher);
    }

    private String getAddressWithoutSubregion(String address) {
        if (!address.contains("("))
            return address;

        int start = address.indexOf("(");
        int end = address.indexOf(")");
        String newAddress = address.replace(address.substring(start, end), "");
        LogUtil.log(LogUtil.INFO, "Original: %s | Cleared: %s", address, newAddress);
        return newAddress;
    }

    private void setProgressVisibility(boolean visible) {
        pb_loading.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Устанавливает слушателя для onDestroyView().
     *
     * @param action
     */
    public void setSaveRouteListener(IAction action) {
        mDissmissAction = action;
    }

    public void setAddressPointWithIndex(_Point addressPoint, int index) {
        mTempPoint = addressPoint;
        mIndex = index;
    }

    public void setIsVoiceInput() {
        mIsVoiceInput = true;
    }

    public class AddressAutocompleteTask extends AsyncTask<Pair<String, LatLong>, ArrayList<_Point>, Void> {
        private final boolean mIsAutoRequest;

        private String mCity = "";
        private Pair<String, LatLong>[] mParams;
        private OkHttpClient okHttp = new OkHttpClient();
        private Call sediCall;
        private int requestCounter;


        public AddressAutocompleteTask(boolean isAutoRequest) {
            mIsAutoRequest = isAutoRequest;
        }

        @Override
        protected void onCancelled(Void aVoid) {
            cancelAllCalls();
            super.onCancelled(aVoid);
        }

        private void cancelAllCalls() {
            if (sediCall != null) sediCall.cancel();
            if (externalAutocomplete != null) externalAutocomplete.cancelCalls();
            mAddresses.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            cancelAllCalls();
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressVisibility(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Pair<String, LatLong>... params) {

            requestCounter = 0;
            mParams = params;

            if (!mIsAutoRequest) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                mAddresses.clear();
            }
            if (isCancelled()) return null;

            Pair<String, LatLong> param = params[0];
            String stringAddress = param.first;
            if (TextUtils.isEmpty(stringAddress))
                return null;

            if (addressContainRegion(stringAddress))
                param = removeRegionFromAddress(param);

            stringAddress = param.first;
            mCity = getCityName(stringAddress);

            if (!isRequestWithoutCity) {
                requestCounter++;
                requestBySedi(mCity, param);
            }

            AutocompleteQuery query = new AutocompleteQuery(mCity, stringAddress, param.second, App.isExcludedApp);
            if (externalAutocomplete != null) {
                requestCounter++;
                externalAutocomplete.find(query, new ExternalAutocomplete.ExternalAutocompleteResponse() {
                    @Override
                    public void onSuccess(ArrayList<_Point> points) {
                        publishProgress(points);
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            }

            return null;
        }

        private Pair<String, LatLong> removeRegionFromAddress(Pair<String, LatLong> param) {
            String address = param.first;
            String[] split = address.split(",");
            if (split.length > 1) {
                String city = split[0];
                if (city.contains("(")) {
                    address = address.replace(city, "");
                    city = city.substring(0, city.indexOf("(")).trim();
                    address = city + address;
                    return new Pair<>(address, param.second);
                } else {
                    return param;
                }
            }
            return param;
        }

        private String getCityName(String stringAddress) {
            if (isRequestWithoutCity) return "";
            String[] split = stringAddress.split(",");
            if (split.length > 1) {
                return split[0];
            } else {
                return Prefs.getString(PrefsName.CURRENT_CITY);
            }
        }

        private boolean isValidCity(_Point convert) {
            if (TextUtils.isEmpty(mCity)) return true;
            return !TextUtils.isEmpty(convert.getCityName())
                    && convert.getCityName().equalsIgnoreCase(mCity);
        }

        private void requestBySedi(String city, Pair<String, LatLong> s) {
            String url = Server.getAutocompleteUrl(getContext());
            url += "&search=" + s.first;

            if (!TextUtils.isEmpty(city))
                url += "&city=" + city;

            if (s.second != null && s.second.isValid()) {
                url += String.format(Locale.ENGLISH, "&lat=%f&lon=%f", s.second.Latitude,
                        s.second.Longitude);
                url += "&radius=100";
            }

            sediCall = okHttp.newCall(new Request.Builder().url(url).build());
            sediCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) {
                    QueryList<_Point> sediAddresses = new QueryList<>();
                    try {
                        if (!response.isSuccessful()) {
                            publishProgress(sediAddresses);
                            return;
                        }
                        String string = response.body().string();
                        SediAutocomplete.Address[] example;
                        try {
                            example = new Gson().fromJson(string, SediAutocomplete.Address[].class);
                        } catch (JsonSyntaxException e) {
                            LogUtil.log(e);
                            publishProgress(sediAddresses);
                            return;
                        }
                        if (example != null) {
                            for (SediAutocomplete.Address example1 : example) {
                                _Point convert = LocationConverter.convert(example1);
                                convert.setDataSource(_Point.Type.SEDI);
                                sediAddresses.add(new _Point(convert));
                            }
                            LogUtil.log(LogUtil.INFO, "Sedi autocomplete size: " + sediAddresses.size());
                            publishProgress(sediAddresses);
                            return;
                        }
                        publishProgress(sediAddresses);
                    } catch (IOException e) {
                        publishProgress(sediAddresses);
                    }
                }
            });
        }

        @Override
        protected void onProgressUpdate(ArrayList<_Point>... values) {
            super.onProgressUpdate(values);
            synchronized (mAddresses) {

                requestCounter--;

                ArrayList<_Point> points = values[0];
                if (points != null) {
                    _Point[] value = points.toArray(new _Point[points.size()]);
                    for (_Point point : value) {
                        if (containAddress(point)) continue;
                        if (!point.getType().equals(_Point.Type.GOOGLE)
                                && !isValidCity(point)) continue;
                        mAddresses.add(point);
                    }
                    Collections.sort(mAddresses, (o1, o2) -> o1.getDataSource().getWeight() < o2.getDataSource().getWeight() ? -1 : 1);
                    adapter.notifyDataSetChanged();
                }

                if (requestCounter == 0) {
                    setProgressVisibility(false);
                    if (mAddresses.size() < 3 && !mIsAutoRequest) {
                        isRequestWithoutCity = true;
                        mAutoCompleteTask = new AddressAutocompleteTask(true);
                        mAutoCompleteTask.execute(mParams);
                    }
                }
            }
        }

        private boolean containAddress(_Point point) {
            if (point.getType().equals(_Point.Type.GOOGLE)) return false;

            for (_Point address : mAddresses.toArray(new _Point[mAddresses.size()])) {
                if (address.getDesc().equalsIgnoreCase(point.getDesc())) {
                    if (point.getDataSource().equals(_Point.Type.SEDI)
                            || address.getType().equals(point.getType())) {
                        mAddresses.remove(address);
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }

    }

    private boolean addressContainRegion(String stringAddress) {
        return stringAddress.contains("(");
    }

}
