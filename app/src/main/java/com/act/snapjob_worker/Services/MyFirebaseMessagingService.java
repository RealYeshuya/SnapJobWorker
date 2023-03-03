package com.act.snapjob_worker.Services;

import androidx.annotation.NonNull;

import com.act.snapjob_worker.Fragment.HomeFragment;
import com.act.snapjob_worker.Global.Common;
import com.act.snapjob_worker.Global.GrabValues;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            GrabValues.updateToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null){
            Common.showNotification(this, new Random().nextInt(),
                    dataRecv.get(Common.NOTIF_TITLE),
                    dataRecv.get(Common.NOTIF_CONTENT),
                    null);
        }
    }
}
