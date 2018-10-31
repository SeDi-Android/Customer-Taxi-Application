package ru.sedi.customerclient.NewDataSharing.Collections;

import android.content.Context;
import android.os.AsyncTask;

import kg.ram.asyncjob.AsyncJob;
import kg.ram.asyncjob.IOnSuccessListener;
import ru.sedi.customerclient.NewDataSharing.PaymentSystems;
import ru.sedi.customerclient.NewDataSharing.RouteHistroryCollection;
import ru.sedi.customerclient.NewDataSharing._Owner;
import ru.sedi.customerclient.NewDataSharing._TariffServiceData;
import ru.sedi.customerclient.Otto.HeaderUpdateEvent;
import ru.sedi.customerclient.Otto.SediBus;
import ru.sedi.customerclient.ServerManager.ServerManager;
import ru.sedi.customerclient.classes.App;
import ru.sedi.customerclient.classes.Customer._LoginInfo;
import ru.sedi.customerclient.classes.Helpers.SaveDataHelper;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class Collections {

    private static Collections sController;
    private _ServiceCollection mServiceManager = new _ServiceCollection();
    private _TariffsCollection mTariffs = new _TariffsCollection();
    private _AddressHistoryCollection mAddressHistory = new _AddressHistoryCollection();
    private PaymentSystemCollection mSystemCollection = new PaymentSystemCollection();
    private ActiveOrdersCollection mActiveOrdersCollection = new ActiveOrdersCollection();
    private PaymentCardCollection mCardCollection = new PaymentCardCollection();
    private RouteHistroryCollection mHistroryCollection = new RouteHistroryCollection();
    private _LoginInfo mUser = new _LoginInfo();

    public Collections() {
        SaveDataHelper.load(this);
    }

    public static Collections me() {
        if (sController == null)
            sController = new Collections();
        return sController;
    }

    public _ServiceCollection getServices() {
        return mServiceManager;
    }

    public _TariffsCollection getTariffs() {
        return mTariffs;
    }

    public PaymentSystemCollection getPaySystems() {
        return mSystemCollection;
    }

    public _AddressHistoryCollection getAddressHistory() {
        return mAddressHistory;
    }

    public RouteHistroryCollection getRoutesHistory() {
        return mHistroryCollection;
    }

    public PaymentCardCollection getPaymentCards() {
        return mCardCollection;
    }

    public void updateTariffsServices(IOnSuccessListener<_TariffServiceData> successListener) {
        AsyncJob<_TariffServiceData> tariffUpdateJob = new AsyncJob.Builder<_TariffServiceData>()
                .doWork(() ->
                        ServerManager.GetInstance().getTariffsAndServices())
                .onSuccess(tariffs -> {
                    if (tariffs == null)
                        return;

                    if (tariffs.getSpecs() != null)
                        mServiceManager.set(tariffs.getSpecs());

                    Collections.me().save();
                    SediBus.getInstance().post(new HeaderUpdateEvent());
                    Prefs.setValue(PrefsName.ENABLE_PROMO, tariffs.isKeywordDiscount());

                    if (successListener != null)
                        successListener.onSuccess(tariffs);
                }).onFailure(throwable -> LogUtil.log(LogUtil.ERROR, throwable.getMessage()))
                .build();
        tariffUpdateJob.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void save() {
        SaveDataHelper.save();
    }

    public void updatePaymentSystems() {
        try {
            PaymentSystems systems = ServerManager.GetInstance().getPaymentSystems();
            if (systems != null) {
                mSystemCollection.set(systems.getPaymentSystems(), systems.getRecurrentPaymentSystems());
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    public void updatePaymentCard(final Context context, final boolean needProgress) {
        mCardCollection.update(context, needProgress);
    }

    public ActiveOrdersCollection getActiveOrders() {
        return mActiveOrdersCollection;
    }


    public void updateAuth() {
        try {
            _LoginInfo loginData = ServerManager.GetInstance().login();
            if (mUser.getID() <= 0 || loginData.getID() > 0) {
                LogUtil.log(LogUtil.INFO, "Login info success updated");
                setUser(loginData);
            }
        } catch (Exception e) {
            LogUtil.log(LogUtil.ERROR, e.getMessage());
            if (e.getMessage() != null) {
                if (e.getMessage().equalsIgnoreCase("loginincorrect")) {
                    App.isAuth = false;
                    setUser(new _LoginInfo());
                    Collections.me().save();
                    Prefs.setValue(PrefsName.USER_KEY, "");
                }
            }
        }
    }

    public _LoginInfo getUser() {
        return mUser;
    }

    public void setUser(_LoginInfo user) {
        if (user == null)
            return;
        user.setUserKey(mUser.getUserKey());
        mUser = user;
        SediBus.getInstance().post(new HeaderUpdateEvent());
    }

    public _Owner getOwner() {
        return mUser.getOwner();
    }

    public boolean enableEasyCostCalculate() {
        return App.isTaxiLive;
        /*return getTariffs().getAll().size() == 1
                && getOwner().getPhones().isEmpty();*/
    }

    public void updateAllInfo() {
        new Thread(() -> {
            updateAuth();
            updatePaymentSystems();
        }).start();
    }
}
