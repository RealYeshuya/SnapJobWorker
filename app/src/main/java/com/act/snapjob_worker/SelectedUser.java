package com.act.snapjob_worker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.act.snapjob_worker.Global.Client;
import com.act.snapjob_worker.Global.Common;
import com.act.snapjob_worker.Global.UserTransaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SelectedUser extends AppCompatActivity implements View.OnClickListener{

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference, userLocationRef;
    TextView uNameTxt, uAddTxt, uKeyTxt, utransactionDescriptionTxt;
    String uName;
    String uAdd;
    String uKey;
    String uId;
    String uTransactionStatus;
    String uTransId;
    String workerID;
    String workerName;
    String transactionDescription;
    String userPhoneNum;
    String uPic;
    EditText declineReason;
    Button acceptBtn, declineBtn;
    ImageView cPhoto;

    private FirebaseUser worker;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public SelectedUser(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecteduserinfo);

        worker = FirebaseAuth.getInstance().getCurrentUser();
        workerID = mAuth.getCurrentUser().getUid();

        declineReason = (EditText) findViewById(R.id.declineReason);

        uNameTxt = findViewById(R.id.userName);
        uAddTxt = findViewById(R.id.userAdd);
        utransactionDescriptionTxt = findViewById(R.id.jobDisc);
        acceptBtn = (Button) findViewById(R.id.btnaccept);
        declineBtn = (Button) findViewById(R.id.btndecline);
        cPhoto = (ImageView) findViewById(R.id.imageView3);

        acceptBtn.setOnClickListener(this);
        declineBtn.setOnClickListener(this);

        getData();
        setData();
    }

    private void getData() {
        if (getIntent().hasExtra("name")) {
            uName = getIntent().getStringExtra("name");
            uId = getIntent().getStringExtra("userId");
            uTransactionStatus = getIntent().getStringExtra("transactionStatus");
            uAdd = getIntent().getStringExtra("address");
            uTransId = getIntent().getStringExtra("transId");
            workerName = getIntent().getStringExtra("workerName");
            transactionDescription = getIntent().getStringExtra("transDesc");
            userPhoneNum = getIntent().getStringExtra("userPhoneNum");

        } else {
            Toast.makeText(this, "No Data...", Toast.LENGTH_SHORT).show();
        }
        databaseReference.child("Users").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Client client = snapshot.getValue(Client.class);
                    if (client != null){
                        uPic = client.image;
                        Picasso.get()
                                .load(uPic)
                                .fit()
                                .centerCrop()
                                .into(cPhoto);
                    }
                    else {
                        uPic = "Wla Unod";
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setData() {
        uNameTxt.setText("Name: " + uName);
        uAddTxt.setText("Address: " + uAdd);
        utransactionDescriptionTxt.setText("Description: " + transactionDescription);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnaccept:

                HashMap hashMap = new HashMap();
                hashMap.put("transactionStatus","Ongoing");

                HashMap hashMap1 = new HashMap();
                hashMap1.put("status", "On Duty");

                databaseReference.child("Transactions").child(uTransId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){

                                databaseReference.child("Workers").child(workerID).updateChildren(hashMap1).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(SelectedUser.this,"Request Accepted!", Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(SelectedUser.this, UserTransaction.class);
                                            intent.putExtra("userName",uName);
                                            intent.putExtra("address",uAdd);
                                            intent.putExtra("transactionStatus",uTransactionStatus);
                                            intent.putExtra("userId",uId);
                                            intent.putExtra("uTransId",uTransId);
                                            intent.putExtra("workerName",workerName);
                                            intent.putExtra("transDesc", transactionDescription);
                                            intent.putExtra("userPhoneNum", userPhoneNum);

                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                        else {
                                            Toast.makeText(SelectedUser.this,"Error changing Status", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                        }
                        else {
                            Toast.makeText(SelectedUser.this,"Failed to Accept Request", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            break;
            case R.id.btndecline:
                //finish();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.decline_dialog, null);
                //builder.setTitle("Decline");
                builder.setMessage("Decline this request from "+uName+"?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap hashMap2 = new HashMap();
                        hashMap2.put("transactionStatus","Declined");
                        hashMap2.put("declineReason",declineReason);

                        databaseReference = FirebaseDatabase.getInstance().getReference("Transactions");
                        userLocationRef = FirebaseDatabase.getInstance().getReference(Common.USER_LOCATION_REFERENCE);
                        databaseReference.child(uTransId).updateChildren(hashMap2).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    userLocationRef.child("Bacolod").child(uId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(SelectedUser.this,"Request has been Declined",Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(SelectedUser.this,"There is an error while declining",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                builder.show();
                break;
        }
    }
}

