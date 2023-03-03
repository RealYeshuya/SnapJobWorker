package com.act.snapjob_worker;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.act.snapjob_worker.Global.GrabValues;
import com.act.snapjob_worker.Global.TokenModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

public class HomeLogin extends Application {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null){


            //Go to homepage
            Intent intent = new Intent(HomeLogin.this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
