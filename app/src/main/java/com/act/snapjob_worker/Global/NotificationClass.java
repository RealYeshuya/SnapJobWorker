package com.act.snapjob_worker.Global;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.act.snapjob_worker.Fragment.HomeFragment;
import com.act.snapjob_worker.Home;
import com.act.snapjob_worker.R;

public class NotificationClass extends AppCompatActivity {

    public static String CHANNEL_2_ID = "channel2";

    public void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel channel2 = new NotificationChannel(CHANNEL_2_ID, "channel 2", NotificationManager.IMPORTANCE_HIGH);

            channel2.setDescription("This A Notification");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel2);
        }
    }

    public void jobRequestAlert(){
        Intent notificationintent = new Intent (getApplicationContext(), HomeFragment.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_2_ID)
                .setSmallIcon(R.drawable.logo2)
                .setContentTitle("Job Request!")
                .setContentText("You have a job")
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,notificationintent,0))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();
        ((NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,notification);
    }
}
