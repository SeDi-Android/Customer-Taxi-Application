package ru.sedi.customerclient.classes.GeoLocation.Nominatium;
import com.google.gson.annotations.SerializedName;

/**
 * Структура описывающая объекты для геокодера Nominatium.
 */
public final class Address {
    /** Долгота адреса. */
    @SerializedName("lon")
    private double longitude;

    /** Широта адреса. */
    @SerializedName("lat")
    private double latitude;

    /** Текстовое написание адреса. */
    @SerializedName("display_name")
    private String displayName;

    /** Объект описывающий компоненты адреса (город, улица, дом и др.) */
    @SerializedName("address")
    private AddressElement addressElements;

    public Address() {
    }

    /** Возвращает долготу для текущего адреса */
    public double getLongitude() {
        return longitude;
    }

    /** Возвращает широту для текущего адреса */
    public double getLatitude() {
        return latitude;
    }

    /** Возвращает объект описывающий компоненты адреса */
    public AddressElement getAddressElements() {
        return addressElements;
    }
}
