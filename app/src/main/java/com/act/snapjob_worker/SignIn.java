package com.act.snapjob_worker;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.act.snapjob_worker.Global.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import static android.content.ContentValues.TAG;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity implements View.OnClickListener{

    private String userID;
    private EditText workerName, workerAge, workerEmail, workerPass, workerJob, workerNum;
    private ProgressBar progressBar;
    private Button signUpbutton;
    private FirebaseAuth mAuth;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        mAuth = FirebaseAuth.getInstance();

        signUpbutton = (Button) findViewById(R.id.signUp);
        signUpbutton.setOnClickListener(this);

        workerName = (EditText) findViewById(R.id.workerNameDB);
        workerAge = (EditText) findViewById(R.id.workerAgeDB);
        workerEmail = (EditText) findViewById(R.id.workerEmailDB);
        workerPass = (EditText) findViewById(R.id.workerPassDB);
        workerNum = (EditText) findViewById(R.id.workerNumDB);
        workerJob = (EditText) findViewById(R.id.workerJobDB);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signUp:
                signUpbutton();
                break;
        }
    }

    private void signUpbutton(){
        String email = workerEmail.getText().toString().trim();
        String password = workerPass.getText().toString().trim();
        String name = workerName.getText().toString().trim();
        String age = workerAge.getText().toString().trim();
        String job = workerJob.getText().toString().trim();
        String number = workerNum.getText().toString().trim();
        String avail = "Not Available";

        Map<String, Object> note = new HashMap<>();


        if (name.isEmpty()){
            workerName.setError("Full name is required!");
            workerName.requestFocus();
            return;
        }
        if (age.isEmpty()){
            workerAge.setError("Age is Required");
            workerAge.requestFocus();
            return;
        }
        if (job.isEmpty()){
            workerJob.setError("Job is Required");
            workerJob.requestFocus();
            return;
        }
        if (email.isEmpty()){
            workerEmail.setError("Email is required!");
            workerEmail.requestFocus();
            return;
        }
        if (number.isEmpty()){
            workerNum.setError("Phone number is required!");
            workerNum.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            workerEmail.setError("Please provide valid Email Address!");
            workerEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            workerPass.setError("Password was not set");
            workerPass.requestFocus();
            return;
        }
        if (password.length() < 6 ){
            workerPass.setError("Password length must be more than 6 characters");
            workerPass.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            User user = new User(name,job,number,email);

                            FirebaseDatabase.getInstance().getReference("Workers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){
                                        Toast.makeText(SignIn.this,"You are now part of the Team! Welcome " + name, Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                                    userID = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = fStore.collection("Workers").document(userID);
                                    Map<String, Object> workers = new HashMap<>();
                                    workers.put("fullName", name);
                                    workers.put("age", age);
                                    workers.put("job", job);
                                    workers.put("available", avail);
                                    workers.put("number", number);
                                    workers.put("email", email);

                                    documentReference.set(workers).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: User profile is created for " + userID);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: " + e.toString());
                                        }
                                    });
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            /*
                            Toast.makeText(SignIn.this,"You are now part of the Team! Welcome " + name, Toast.LENGTH_LONG).show();
                            userID = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Workers").document(userID);
                            Map<String, Object> workers = new HashMap<>();
                            workers.put("fullName", name);
                            workers.put("age", age);
                            workers.put("job", job);
                            workers.put("available", avail);
                            workers.put("number", number);
                            workers.put("email", email);
                            documentReference.set(workers).addOnSuccessListener(new OnSuccessListener<Void>() {

                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User profile is created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                                startActivity(new Intent(getApplicationContext(), MainActivity.class)); */
                           }else {
                            Toast.makeText(SignIn.this,"Sign Up Failed! Try Again", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
}
