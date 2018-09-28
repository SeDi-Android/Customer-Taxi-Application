package ru.sedi.customerclient.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.driver_rating.DriverRatingActivity;
import ru.sedi.customerclient.activitys.splash_screen.SplashScreenActivity;
import ru.sedi.customerclient.common.SystemManagers.Prefs;
import ru.sedi.customerclient.enums.PrefsName;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * A notification channel with vibration, sound, and indication.
     */
    private final String DEFAULT_CH = "DEFAULT_CH";

    /**
     * A notification channel without sound.
     */
    private final String SILENT_CH = "SILENT_CH";

    /**
     * Notification manager.
     */
    private NotificationManager mNtfManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (Prefs.getSharedPreferences() == null)
            new Prefs(getApplication());

        if (mNtfManager == null) {
            mNtfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        String msg;
        if (remoteMessage.getNotification() == null) {
            Map<String, String> data = remoteMessage.getData();
            if (data == null || data.isEmpty()) return;

            //We check for rating action.
            if (messageTypeEquals(data, "setRaiting")) {
                startRatingActivity(data);
                return;
            }

            msg = getNotificationMessage(data);

        } else {
            msg = remoteMessage.getNotification().getBody();
        }

        if (TextUtils.isEmpty(msg)) return;

        Double ntfId = Math.random() * 100;
        mNtfManager.notify(ntfId.intValue(), getNotification(msg));
    }

    /**
     * Returns the message text from the notification.
     *
     * @param data collection of data from the notification.
     * @return the text of the message.
     */
    private String getNotificationMessage(Map<String, String> data) {
        String msg = null;
        if (data.containsKey("title"))
            msg = data.get("title");
        if (data.containsKey("message"))
            msg = data.get("message");

        return msg;
    }

    /**
     * Starts an activity for rating the driver for the trip.
     *
     * @param data collection of data from the notification.
     */
    private void startRatingActivity(Map<String, String> data) {
        String orderId = null;
        if (data.containsKey("orderId"))
            orderId = data.get("orderId");

        if (TextUtils.isEmpty(orderId)) return;
        startActivity(DriverRatingActivity.getIntent(this, orderId));
    }

    /**
     * Creates a new notification object.
     *
     * @param message notification text.
     * @return notification object.
     */
    private Notification getNotification(String message) {
        Intent intent = new Intent(getBaseContext(), SplashScreenActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean isSilentMode = Prefs.getBool(PrefsName.VIBRATION_NOTIFY);

        String chanelId = getChanelId(isSilentMode ? SILENT_CH : DEFAULT_CH);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chanelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(message)
                .setContentTitle(getString(R.string.appName))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 100, 200});

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            int defaults = Notification.DEFAULT_ALL;
            if (isSilentMode)
                defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
            builder.setDefaults(defaults);
        }

        return builder.build();
    }

    /**
     * Checks whether this notice is an action.
     *
     * @param data        collection of data from a notification.
     * @param messageType type for verification.
     * @return true if there is a key in the notification {@code messageType}
     */
    private boolean messageTypeEquals(Map<String, String> data, String messageType) {
        if (data == null || data.isEmpty()) return false;

        final String type = "messageType";
        return data.containsKey(type) && data.get(type).equalsIgnoreCase(messageType);

    }

    /**
     * Checks if there is a notification channel for the specified {@code channelId}.
     * If there is no channel, it will be created, otherwise it will just return {@code channelId}.
     *
     * @param channelId notification channel id.
     * @return id notification channel id.
     */
    public String getChanelId(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = mNtfManager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId,
                        getString(R.string.notification_channel_desc),
                        NotificationManager.IMPORTANCE_DEFAULT);

                channel.setVibrationPattern(new long[]{100, 200, 100, 200});
                channel.enableLights(true);

                if (channelId.equals(SILENT_CH)) {
                    AudioAttributes attributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                    channel.setSound(null, attributes);
                }

                mNtfManager.createNotificationChannel(channel);
            }

        }
        return channelId;
    }
}
