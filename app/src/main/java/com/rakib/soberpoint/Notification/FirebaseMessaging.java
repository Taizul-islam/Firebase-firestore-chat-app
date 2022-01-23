package com.rakib.soberpoint.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.HomeActivity;

public class FirebaseMessaging extends FirebaseMessagingService {
    String CHANNEL_ID = "SUSTian";
    Intent intent;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        remoteMessage.getData();
        String user = remoteMessage.getData().get("sent");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type=remoteMessage.getData().get("type");
        MediaPlayer mp= MediaPlayer.create(getApplicationContext(), R.raw.tuhin);
        mp.start();
        showMessageNotification(user,title,body,type);

    }
    private void showMessageNotification(String from, String title, String message,String type) {
        int NOTIFICATION_ID = 234;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        switch (type) {
            case "request":
                intent = new Intent(this, HomeActivity.class);
                intent.putExtra("type",type);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            case "comment":
                intent = new Intent(this, HomeActivity.class);
                intent.putExtra("type","home");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;

        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent);
        MediaPlayer mp= MediaPlayer.create(getApplicationContext(), R.raw.notification);
        mp.start();
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}