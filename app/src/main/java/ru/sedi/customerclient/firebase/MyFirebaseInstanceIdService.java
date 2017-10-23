package ru.sedi.customerclient.firebase;

import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ru.sedi.customerclient.enums.PrefsName;
import ru.sedi.customerclient.common.LogUtil;
import ru.sedi.customerclient.common.SystemManagers.Prefs;


public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (TextUtils.isEmpty(token)) return;

        Prefs.setValue(PrefsName.FIREBASE_TOKEN, token);
        LogUtil.log(LogUtil.INFO, "Receive token:\n" + token);
    }
}
