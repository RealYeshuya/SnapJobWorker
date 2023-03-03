package com.act.snapjob_worker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordReset extends AppCompatActivity {

    private EditText emailText;
    private Button resetPass;
    //private ProgressBar progressBar;
    private LoadingDialog loadingDialog;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        emailText = (EditText) findViewById(R.id.workerEmail);
        resetPass = (Button) findViewById(R.id.resetPass);
        //progressBar = (ProgressBar) findViewById(R.id.progressBarReset);

        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(PasswordReset.this);

        resetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = emailText.getText().toString().trim();

        if (email.isEmpty()){
            emailText.setError("Email is required!");
            emailText.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailText.setError("Please provide valid email");
            emailText.requestFocus();
            return;
        }
        //progressBar.setVisibility(View.VISIBLE);
        loadingDialog.startLoadingDialog();
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(PasswordReset.this,"Please Check your Email to reset your password!", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
                else {
                    Toast.makeText(PasswordReset.this,"There was an Error! Try Again", Toast.LENGTH_LONG).show();
                }
                loadingDialog.dismissDialog();
            }
        });
    }
}