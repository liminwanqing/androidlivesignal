package com.signal.example.keep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public abstract class FgService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    protected void createNotification(int id, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            NotificationChannel serviceChannel = manager.getNotificationChannel(channelId);
            if (serviceChannel == null) {
                serviceChannel = new NotificationChannel(
                        channelId,
                        "FgKeepAlive" + this.getClass().getSimpleName(),
                        NotificationManager.IMPORTANCE_LOW
                );
            }
            serviceChannel.setVibrationPattern(new long[]{-1});
            serviceChannel.enableVibration(false);
            manager.createNotificationChannel(serviceChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.star_off)
                .setContentTitle("keeper")
                .setContentText("push")
                .setVibrate(new long[]{-1})
                .setPriority(NotificationCompat.PRIORITY_MIN);
        Notification notification = mBuilder.build();
        notification.sound = null;
        notification.vibrate = null;
        notification.defaults &= ~Notification.DEFAULT_SOUND;
        notification.defaults &= ~Notification.DEFAULT_VIBRATE;
        startForeground(id, notification);
    }
}
