package com.act.snapjob_worker.Global;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.act.snapjob_worker.R;
import com.act.snapjob_worker.Services.MyFirebaseMessagingService;

public class Common {

    public static final String WORKER_LOCATION_REFERENCE = "WorkerLocation";

    public static final String TOKEN_REFERENCE = "Token";
    public static final String NOTIF_TITLE = "title";
    public static final String NOTIF_CONTENT = "body";

    public static void showNotification(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "snapjobWorker";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"SnapJob - Worker",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Job is ready");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.logo2)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.logo2));

        if (pendingIntent != null){
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id,notification);
    }
}
