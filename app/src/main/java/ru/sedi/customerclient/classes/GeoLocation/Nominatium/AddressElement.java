package ru.sedi.customerclient.classes.GeoLocation.Nominatium;

import android.text.TextUtils;

public class AddressElement {
    /** Массив слов для определения районов и др. в адресах */
    private final String[] excludeDictionary = {"район", "область", "округ"};

    /** Номер дома */
    private String house_number;

    /** Улица (3 поля могу содержать улицу, зависит от всяких свойст OSM) */
    private String road;
    private String pedestrian;
    private String living_street;

    /** Город (4 поля могут содержать названия города, зависит от населенности) */
    private String city;
    private String town;
    private String village;
    private String hamlet;

    /** Район, район города, округ  */
    private String suburb;
    private String state_district;
    private String state;

    /** Почтовый индекс */
    private String postcode;


    /** Возвращает номер дома */
    public String getHousenumber() {
        return house_number;
    }

    /** Возврщает улицу */
    public String getStreet() {
        String s = null;
        //Ищем улицу в одном из полей...
        if (!TextUtils.isEmpty(road)) s = road;
        if (!TextUtils.isEmpty(pedestrian)) s = pedestrian;
        if (!TextUtils.isEmpty(living_street)) s = living_street;

        if (TextUtils.isEmpty(s)) return null;

        //Предполагаем что в suburb стоит район города (так чаще всего и есть)
        //доклеиваем его к городу в скобочках.
        if (!TextUtils.isEmpty(suburb))
            return String.format("%s (%s)", s, suburb);
        return s;
    }

    /** Возвращает почтовый индекс */
    public String getPostcode() {
        return postcode;
    }

    /** Возвращает город */
    public String getCity() {
        //Ищем город, сначала мелкие городишки...
        if (!TextUtils.isEmpty(town)) return town;
        if (!TextUtils.isEmpty(village)) return village;
        if (!TextUtils.isEmpty(hamlet)) return hamlet;

        //Если мелких нет, берем city
        if (!TextUtils.isEmpty(city)) {
            boolean isRegion = false;
            //Проверяем, ни район ли города это (часто такое было в Москве)
            for (String s : excludeDictionary) {
                if (city.contains(s)) {
                    isRegion = true;
                    break;
                }
            }
            //Если это не район, считаем его городом.
            if (!isRegion)
                return city;
        }

        //Из-за того что в москве, в state стоит город, предположим,
        //что если в city район городо то тут стоит город, и вернем его.
        if (!TextUtils.isEmpty(state))
            return state;

        //Если все плохо и твориться что-то странное, вернем просто city.
        return city;
    }


}
