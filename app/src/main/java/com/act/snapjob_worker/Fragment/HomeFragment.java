package com.act.snapjob_worker.Fragment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.act.snapjob_worker.Global.GrabValues;
import com.act.snapjob_worker.HomeLogin;
import com.act.snapjob_worker.MainActivity;
import com.act.snapjob_worker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SnapshotMetadata;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    private FirebaseUser user;
    private String userID;
    private DocumentReference documentReference;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private boolean value;

       public Switch toggleJobButton;
       public TextView stateOnOff;
       public static String CHANNEL_1_ID = "channel1";

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home,container,false);
        toggleJobButton = (Switch) view.findViewById(R.id.toggleJob);
        stateOnOff = (TextView) view.findViewById(R.id.toggleJob);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = mAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("Workers").document(userID);

        //save switch state
        SharedPreferences sharedPreferences = this.getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = this.getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        toggleJobButton.setChecked(sharedPreferences.getBoolean("value",false));

        //Update Token
        Task<String> insId = FirebaseInstallations.getInstance().getId();
        FirebaseInstallations.getInstance().getToken(true)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<InstallationTokenResult>() {
                    @Override
                    public void onSuccess(InstallationTokenResult installationTokenResult) {
                        Log.d("TOKEN", installationTokenResult.getToken());
                        GrabValues.updateToken(getContext(),installationTokenResult.getToken());
                    }
                });

/*
       documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Toast.makeText(getActivity(),"Error Reading Data", Toast.LENGTH_LONG).show();
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()){
                    String availability = value.getString("available");

                    if (availability == "Not Available"){

                    }
                }
            }
        }); */

        //switch button
        toggleJobButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (toggleJobButton.isChecked()){

                    documentReference.update("available", "Available").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                editor.putBoolean("value",true);
                                editor.apply();
                                toggleJobButton.setChecked(true);
                                createNotificationChannels();
                                jobnotifOn();
                            }
                            else {
                                Toast.makeText(getContext(),"Unable to go Online", Toast.LENGTH_LONG).show();
                            }
                        }

                    });
                    toggleJobButton.setChecked(true);
                }
                else{
                    documentReference.update("available", "Not Available").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                editor.putBoolean("value",false);
                                editor.apply();
                                toggleJobButton.setChecked(false);
                                createNotificationChannels();
                                jobnotifOff();
                            }
                            else {
                                Toast.makeText(getContext(),"Unable to go Offline", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    toggleJobButton.setChecked(false);
                }
            }
        });

        //view current JOb
        final TextView jobView = view.findViewById(R.id.currentJob);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Toast.makeText(getActivity(),"Error Loading Data", Toast.LENGTH_LONG).show();
                    Log.d(TAG, error.toString());
                    return;
                }

                if (value.exists()){
                    String job = value.getString("job");
                    jobView.setText(job);
                }
            }
        });

        return view;
    }

    public void jobnotifOn() {
        Intent notificationintent = new Intent (getActivity(),HomeFragment.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),CHANNEL_1_ID)
                .setSmallIcon(R.drawable.logo2)
                .setContentTitle("Job Status:")
                .setContentText("You are now On Duty")
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(getActivity(),0,notificationintent,0))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();
        ((NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,notification);
    }
    public void jobnotifOff() {
        Intent notificationintent = new Intent (getActivity(),HomeFragment.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),CHANNEL_1_ID)
                .setSmallIcon(R.drawable.logo2)
                .setContentTitle("Job Status:")
                .setContentText("You are now Off Duty")
                .setOngoing(false)
                .setContentIntent(PendingIntent.getActivity(getActivity(),0,notificationintent,0))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();
        ((NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,notification);
    }
    private void createNotificationChannels(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_1_ID, "channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("This A Notification");
            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
