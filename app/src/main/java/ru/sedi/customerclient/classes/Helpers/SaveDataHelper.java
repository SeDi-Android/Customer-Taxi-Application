package ru.sedi.customerclient.classes.Helpers;


import ru.sedi.customerclient.NewDataSharing.Collections.Collections;
import ru.sedi.customerclient.NewDataSharing.PaymentSystem;
import ru.sedi.customerclient.NewDataSharing.RouteHistory;
import ru.sedi.customerclient.NewDataSharing._Point;
import ru.sedi.customerclient.NewDataSharing._Service;
import ru.sedi.customerclient.NewDataSharing._Tariff;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.common.LogUtil;

/**
 * Class: SaveDataHelper
 * Author: RAM / 20.11.2014
 * Description:
 */
public class SaveDataHelper {

    public _Service[] mServices;
    public _Point[] mSediAddresses;
    public _Tariff[] mTariffs;
    public RouteHistory[] mRoutes;
    public _LoginInfo mUser;
    public PaymentSystem[] mPaymentSystems;
    public PaymentSystem[] mRecurrentSystems;

    public static final String F_NAME = "data.j";

    /**
     * Constructor
     */
    public SaveDataHelper() {
    }

    /**
     * Save the app data: addresses, services, tarrifs.
     */
    public static synchronized void save() {
        LogUtil.log(LogUtil.INFO, "Save");
        SaveDataHelper cHelper = new SaveDataHelper();

        cHelper.mServices = Collections.me().getServices().getAsArray();
        cHelper.mSediAddresses = Collections.me().getAddressHistory().getAsArray();
        cHelper.mTariffs = Collections.me().getTariffs().getAsArray();
        cHelper.mUser = Collections.me().getUser();
        cHelper.mPaymentSystems = Collections.me().getPaySystems().getPaymentAsArray();
        cHelper.mRecurrentSystems = Collections.me().getPaySystems().getRecurrentAsArray();
        cHelper.mRoutes = Collections.me().getRoutesHistory().getAsArray();

        FileSaveHelper.Save(cHelper, F_NAME);
    }

    /**
     * Load the app cache.
     *
     * @param collections
     */
    public static void load(Collections collections) {
        try {
            Object o = FileSaveHelper.Load(F_NAME, SaveDataHelper.class);
            SaveDataHelper saveHelper;
            if (o != null)
                saveHelper = (SaveDataHelper) o;
            else
                saveHelper = new SaveDataHelper();

            if (saveHelper.mServices != null)
                collections.getServices().set(saveHelper.mServices);
            if (saveHelper.mSediAddresses != null)
                collections.getAddressHistory().set(saveHelper.mSediAddresses);
            if (saveHelper.mTariffs != null)
                collections.getTariffs().set(saveHelper.mTariffs);
            if (saveHelper.mUser != null)
                collections.setUser(saveHelper.mUser);
            PaymentSystem[] ps = null, rs = null;
            if (saveHelper.mPaymentSystems != null)
                ps = saveHelper.mPaymentSystems;
            if (saveHelper.mRecurrentSystems != null)
                rs = saveHelper.mRecurrentSystems;
            collections.getPaySystems().set(ps, rs);
            if (saveHelper.mRoutes != null)
                collections.getRoutesHistory().set(saveHelper.mRoutes);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Clearing the app cache.
     */
    public static synchronized void resetData() {
        SaveDataHelper cHelper = new SaveDataHelper();

        cHelper.mServices = new _Service[]{};
        cHelper.mSediAddresses = new _Point[]{};
        cHelper.mTariffs = new _Tariff[]{};
        cHelper.mUser = new _LoginInfo();
        cHelper.mRoutes = new RouteHistory[]{};

        FileSaveHelper.Save(cHelper, F_NAME);
    }


}
