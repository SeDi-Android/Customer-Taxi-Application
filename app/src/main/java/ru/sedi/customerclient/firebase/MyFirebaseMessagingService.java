package ru.sedi.customerclient.firebase;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import ru.sedi.customer.R;
import ru.sedi.customerclient.activitys.splash_screen.SplashScreenActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "";
        if (remoteMessage.getNotification() == null) {
            Map<String, String> data = remoteMessage.getData();
            if (data == null || data.isEmpty())
                return;

            if(data.containsKey("title"))
                title = data.get("title");
            if(data.containsKey("message"))
                title = data.get("message");

        } else {
            title = remoteMessage.getNotification().getBody();
        }

        Intent intent = new Intent(getBaseContext(), SplashScreenActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification build = new NotificationCompat.Builder(this)
                .setWhen(System.currentTimeMillis())
                .setContentText(title)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.appName))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        Double ntfId = Math.random() * 100;
        NotificationManagerCompat.from(this).notify(ntfId.intValue(), build);
    }
}
