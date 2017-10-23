package ru.sedi.customerclient.classes;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;

import java.util.List;

import ru.sedi.customerclient.common.LINQ.QueryList;
import ru.sedi.customerclient.common.LatLong;
import ru.sedi.customerclient.interfaces.IAction;


public class OsmMapController {

    private GeoPoint mDefaultPoint; // = new GeoPoint(55.749046, 37.617999);
    private final int DEFAULT_ZOOM = 13;

    private Context mContext;
    private MapView mMap;
    private IMapController mController;
    private OverlayManager mOverlayManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public OsmMapController(Context context, MapView map) {
        mContext = context;
        mMap = map;
        mDefaultPoint = App.isTaxiLive
                ? new GeoPoint(47.378390, 8.541411) // Цюрих
                : new GeoPoint(55.749046, 37.617999); //Москва

        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);
        mController = mMap.getController();
        mOverlayManager = mMap.getOverlayManager();
        mController.setCenter(mDefaultPoint);
        mController.setZoom(DEFAULT_ZOOM);
    }

    public ItemizedIconOverlay addPoint(final LatLong loc, int icon, final IAction singleTapAction) {
        OverlayItem myLocationOverlayItem = new OverlayItem("", "", loc.toGeopoint());
        myLocationOverlayItem.setMarker(ContextCompat.getDrawable(mContext, icon));
        List<OverlayItem> items = new QueryList<>();
        items.add(myLocationOverlayItem);
        ItemizedIconOverlay currentLocationOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                if (singleTapAction != null)
                    singleTapAction.action();
                return true;
            }

            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return true;
            }
        }, mContext);
        addOverlay(currentLocationOverlay);
        return currentLocationOverlay;
    }

    public void addOverlay(Overlay overlay) {
        mOverlayManager.add(overlay);
        invalidate();
    }

    public void addOverlay(int pIndex, Overlay overlay) {
        mOverlayManager.add(pIndex, overlay);
        invalidate();
    }

    public void setOverlay(Overlay overlay) {
        clearAllOverlays();
        addOverlay(overlay);
    }

    public void clearAllOverlays() {
        mOverlayManager.clear();
        invalidate();
    }

    public void moveTo(LatLong location) {
        mController.animateTo(location.toGeopoint());
    }

    public void zoomTo(LatLong location, int zoom) {
        mController.setZoom(zoom);
        mController.animateTo(location.isValid() ? location.toGeopoint() : mDefaultPoint);
    }

    public void setTileSource(ITileSource tileSource) {
        mMap.setTileSource(tileSource);
        invalidate();
    }

    private void invalidate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMap.invalidate();
            }
        });
    }

    public void zoomIn() {
        mController.zoomIn();
        invalidate();
    }

    public void zoomOut() {
        mController.zoomOut();
        invalidate();
    }

    public MapView getMap() {
        return mMap;
    }

    public void remove(Object o) {
        if (o == null) return;

        mOverlayManager.remove(o);
        invalidate();
    }

    public void zoomToBoundingBox(BoundingBox boundingBox) {
        mMap.zoomToBoundingBox(boundingBox, true);
    }
}
