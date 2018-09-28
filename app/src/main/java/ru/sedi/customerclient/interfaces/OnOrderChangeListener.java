package ru.sedi.customerclient.interfaces;


import ru.sedi.customerclient.common.LatLong;

/**
 * Created by RAM on 21.03.2017.
 */

public interface OnOrderChangeListener {
    void refreshAllViews();

    void showLocationErrorDialog();

    void addMapScrollListener(ru.sedi.customerclient.common.AsyncAction.IAction<LatLong> action);
}
