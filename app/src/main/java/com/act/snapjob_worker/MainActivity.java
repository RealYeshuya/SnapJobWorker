package com.act.snapjob_worker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText emailWorker, passwordWorker;
    private Button login;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        login = (Button) findViewById(R.id.signIn);
        login.setOnClickListener(this);

        TextView signin = (TextView) findViewById(R.id.signUp);
        signin.setOnClickListener(this);

        emailWorker = (EditText) findViewById(R.id.workerEmail);
        passwordWorker = (EditText) findViewById(R.id.workerPass);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        user = mAuth.getCurrentUser();

        if (user != null){
            Intent intent = new Intent(MainActivity.this, Home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signUp:
                signInPage();
                break;
            case R.id.signIn:
                userLogin();
                 break;
        }

    }
    private void userLogin(){
        String email = emailWorker.getText().toString().trim();
        String password = passwordWorker.getText().toString().trim();

        if (email.isEmpty()){
            emailWorker.setError("Email is Required!");
            emailWorker.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailWorker.setError("Please Enter Email!");
            emailWorker.requestFocus();
            return;
        }
        if (password.isEmpty()){
            passwordWorker.setError("Please Enter Password!");
            passwordWorker.requestFocus();
            return;
        }

        if (password.length() < 6 ){
            passwordWorker.setError("Password length is less than 6 characters");
            passwordWorker.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    openHome();
                    Toast.makeText(MainActivity.this,"Login Success", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MainActivity.this,"Login Failed! Check Credentials", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

    }
    public void signInPage(){
        Intent signInPage = new Intent(this, SignIn.class);
        startActivity(signInPage);
    }
    public void openHome(){
        Intent next = new Intent(this, Home.class);
        next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(next);
    }
}