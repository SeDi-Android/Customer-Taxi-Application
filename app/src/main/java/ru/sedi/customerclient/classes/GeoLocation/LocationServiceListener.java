package ru.sedi.customerclient.classes.GeoLocation;

import android.location.Location;

/**
 * Created by Marchenko Roman on 28.02.2017.
 */

public interface LocationServiceListener {
    void start();
    void stop();
    Location getLocation();
}
