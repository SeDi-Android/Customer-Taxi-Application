package ru.sedi.customerclient.NewDataSharing;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.loopj.android.http.RequestParams;

import java.util.Locale;
import java.util.Optional;

import ru.sedi.customer.R;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.common.GeoTools.GeoPoint;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.db.DbHistoryPoint;
import ru.sedi.customerclient.enums.PrefsName;

public class _Point {

    public enum Type {

        GOOGLE(3), YANDEX(2), SEDI(1);

        private int weight;

        Type(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    private String AddressString = "";
    private String CountryName = "";
    private String PostalCode = "";
    private String CityName = "";
    private String StreetName = "";
    private String HouseNumber = "";
    private String ObjectName = "";
    private String PlaceId = "";
    private String Description = "";
    private boolean Checked;
    private int EntranceNumber = -1;
    private _GeoPoint GeoPoint = new _GeoPoint();
    private boolean Coordinatesonly;
    private int WaitTime;
    private int ID = -1;
    private Type mType;
    private GoogleAutocomplete.StructuredFormatting mGoogleStruckFormatting;

    public _Point() {
    }

    public _Point(_Point point) {
        AddressString = point.AddressString;
        CountryName = point.CountryName;
        PostalCode = point.PostalCode;
        CityName = point.CityName;
        StreetName = point.StreetName;
        HouseNumber = point.HouseNumber;
        ObjectName = point.ObjectName;
        PlaceId = point.PlaceId;
        Description = point.Description;
        Checked = point.Checked;
        EntranceNumber = point.EntranceNumber;
        GeoPoint = point.GeoPoint;
        WaitTime = point.WaitTime;
        this.ID = point.ID;
        Coordinatesonly = point.Coordinatesonly;
        mType = point.mType;
        mGoogleStruckFormatting = point.mGoogleStruckFormatting;
    }

    public _Point(String addressString, LatLong latLong) {
        AddressString = addressString;
        GeoPoint = new _GeoPoint(latLong.Latitude, latLong.Longitude);
        Checked = GeoPoint.isValid();
    }

    public _Point(String cityName, LatLong latLong, boolean isCoordinatesonly, boolean isChecked) {
        CityName = cityName;
        GeoPoint = new _GeoPoint(latLong.Latitude, latLong.Longitude);
        Coordinatesonly = isCoordinatesonly;
        Checked = isChecked;
    }

    public _Point(String cityName, LatLong latLong, boolean isCoordinatesonly) {
        this(cityName, latLong, isCoordinatesonly, true);
    }

    public _Point(DbHistoryPoint point) {
        CountryName = point.getCountryName();
        PostalCode = point.getPostalCode();
        CityName = point.getCityName();
        StreetName = point.getStreetName();
        HouseNumber = point.getHouseNumber();
        ObjectName = point.getObjectName();
        PlaceId = point.getPlaceId();
        Checked = true;
        EntranceNumber = point.getEntranceNumber();
        GeoPoint = new _GeoPoint(point.getLatitude(), point.getLongetude());
        this.ID = point.getId();
        if (!TextUtils.isEmpty(point.getType()))
            mType = Type.valueOf(point.getType());
    }

    public boolean isSingleStringPoint() {
        return !TextUtils.isEmpty(AddressString);
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public String getPostalCode() {
        return PostalCode;
    }

    public void setPostalCode(String postalCode) {
        PostalCode = postalCode;
    }

    public String getCityName() {
        return CityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    public String getStreetName() {
        return StreetName;
    }

    public void setStreetName(String streetName) {
        StreetName = streetName;
    }

    public String getHouseNumber() {
        return HouseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        HouseNumber = houseNumber;
    }

    public String getObjectName() {
        return ObjectName;
    }

    public void setObjectName(String objectName) {
        ObjectName = objectName;
    }

    public int getEntranceNumber() {
        return EntranceNumber;
    }

    public void setEntranceNumber(int entranceNumber) {
        EntranceNumber = entranceNumber;
    }

    public _GeoPoint getGeoPoint() {
        return GeoPoint;
    }

    public LatLong getLatLong() {
        return new LatLong(GeoPoint.getLat(), GeoPoint.getLon());
    }

    public void setGeoPoint(_GeoPoint geoPoint) {
        GeoPoint = geoPoint;
    }

    public void setGeoPoint(LatLong point) {
        GeoPoint = new _GeoPoint(point.Latitude, point.Longitude);
    }

    public int getWaitTime() {
        return WaitTime;
    }

    public void setWaitTime(int waitTime) {
        WaitTime = waitTime;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getPlaceId() {
        return PlaceId;
    }

    public void setPlaceId(String placeId) {
        PlaceId = placeId;
    }

    public boolean getChecked() {
        return Checked;
    }

    public void setChecked(boolean checked) {
        Checked = checked;
    }

    public _Point copy() {
        return new _Point(this);
    }

    public String asShortAddress() {
        if (!TextUtils.isEmpty(AddressString))
            return AddressString;
        else if (Coordinatesonly)
            return getLocationString();
        else if (isMinimalAddress())
            return getAddressOrObject();
        else
            return getCityName();
    }

    private String getLocationString() {
        return String.format(Locale.ENGLISH, "%.5f, %.5f", GeoPoint.getLat(), GeoPoint.getLon());
    }

    public String getAddressOrObject() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(getStreetName())) {
            sb.append(getStreetName());

            if (!TextUtils.isEmpty(getHouseNumber())) {
                sb.append(", ").append(getHouseNumber());
            }
        } else if (!TextUtils.isEmpty(getObjectName())) {
            sb.append(getObjectName());
        }
        return sb.toString();
    }

    public String asString() {
        return addressInRussianFormat();
    }

    public String asString(boolean withFormat) {
        boolean isRuLocale = Prefs.getString(PrefsName.LOCALE_CODE).equalsIgnoreCase("ru");
        if (!isRuLocale && withFormat)
            return addressInEuropeanFormat();
        return addressInRussianFormat();
    }

    @NonNull
    private String addressInRussianFormat() {
        //Если адрес указан строкой, то нужно вернуть его.
        if (!TextUtils.isEmpty(AddressString))
            return AddressString;

        StringBuilder builder = new StringBuilder();

        try {
            if (Coordinatesonly) {
                builder.append(getLocationString());
            } else {
                if (!TextUtils.isEmpty(CityName)) {
                    builder.append(CityName);
                    builder.append(", ");
                }

                if (!TextUtils.isEmpty(StreetName)) {
                    builder.append(StreetName);
                }

                if (!TextUtils.isEmpty(HouseNumber)) {
                    if (builder.length() > 0)
                        builder.append(" ");
                    builder.append(HouseNumber);
                }

                if (EntranceNumber > 0) {
                    if (builder.length() > 0)
                        builder.append(String.format(", %s ", App.getInstance().getString(R.string.entrance_short)));
                    builder.append(EntranceNumber);
                }

                if (!TextUtils.isEmpty(ObjectName)) {
                    String s = builder.toString();
                    if (!s.endsWith(", ")) {
                        builder.append(", ");
                    }
                    builder.append(String.format("%s", ObjectName));
                }
            }

            return builder.toString();
        } catch (Exception e) {
            LogUtil.log(e);
            return "";
        }
    }

    //Reitmenstrasse 7, Объект, 8952 Schlieren
    @NonNull
    private String addressInEuropeanFormat() {
        //Если адрес указан строкой, то нужно вернуть его.
        if (!TextUtils.isEmpty(AddressString))
            return AddressString;

        StringBuilder builder = new StringBuilder();
        try {
            if (!TextUtils.isEmpty(StreetName))
                builder.append(StreetName);

            if (!TextUtils.isEmpty(HouseNumber)) {
                if (builder.length() > 0)
                    builder.append(" ");
                builder.append(HouseNumber);
            }

            if (builder.length() > 0)
                builder.append(", ");

            if (!TextUtils.isEmpty(ObjectName))
                builder.append(ObjectName).append(", ");

            if (!TextUtils.isEmpty(PostalCode))
                builder.append(PostalCode);

            if (!TextUtils.isEmpty(CityName))
                if (builder.length() > 0)
                    builder.append(" ");
            builder.append(CityName);

            if (Coordinatesonly)
                builder.append(" (Geopunkt)");

            return builder.toString();
        } catch (Exception e) {
            LogUtil.log(e);
            return "";
        }
    }

    public String getDesc() {
        if (TextUtils.isEmpty(Description)) {
            return asString();
        }
        return Description;
    }

    public void setDesc(String desc) {
        Description = desc;
    }

    public boolean isCoordinatesonly() {
        return Coordinatesonly;
    }

    public boolean isMinimalAddress() {
        return !TextUtils.isEmpty(CityName)
                && (!TextUtils.isEmpty(StreetName)
                || !TextUtils.isEmpty(ObjectName));
    }

    public boolean equalsAddress(_Point point) {
        return point.getGeoPoint().toLatLong().equals(getGeoPoint().toLatLong());
    }

    public RequestParams asRequestParam(int i, RequestParams params) {
        if (!TextUtils.isEmpty(AddressString)) {
            params.put("addressString" + i, AddressString);
        } else {
            if (ID > 0)
                params.put("addrid" + i, ID);

            if (!TextUtils.isEmpty(CityName))
                params.put("city" + i, CityName);

            if (!TextUtils.isEmpty(StreetName))
                params.put("street" + i, StreetName);

            if (!TextUtils.isEmpty(ObjectName))
                params.put("object" + i, ObjectName);

            if (!TextUtils.isEmpty(HouseNumber))
                params.put("house" + i, HouseNumber);

            if (EntranceNumber > 0)
                params.put("entrace" + i, EntranceNumber);

            if (Coordinatesonly)
                params.put("coordinatesonly" + i, Coordinatesonly);
        }

        if (GeoPoint.toLatLong().isValid()) {
            params.put("lat" + i, GeoPoint.getLat());
            params.put("lon" + i, getGeoPoint().getLon());
        }

        return params;
    }

    public void setCoordinatesonly(boolean coordinatesonly) {
        Coordinatesonly = coordinatesonly;
    }

    public Type getDataSource() {
        return mType;
    }

    public void setDataSource(Type source) {
        mType = source;
    }

    @Override
    public String toString() {
        return getDesc();
    }

    public Type getType() {
        return mType;
    }

    public void setGoogleStruckFormatting(GoogleAutocomplete.StructuredFormatting googleStruckFormatting) {
        mGoogleStruckFormatting = googleStruckFormatting;
    }

    public GoogleAutocomplete.StructuredFormatting getGoogleStruckFormatting() {
        return mGoogleStruckFormatting;
    }
}
