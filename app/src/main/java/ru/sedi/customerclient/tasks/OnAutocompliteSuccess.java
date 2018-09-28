package ru.sedi.customerclient.tasks;

import java.util.List;

import ru.sedi.customerclient.NewDataSharing._Point;

/**
 * Created by Marchenko Roman on 21.02.2017.
 */

public interface OnAutocompliteSuccess {
    void onSuccessResponse(List<_Point> points);
}
