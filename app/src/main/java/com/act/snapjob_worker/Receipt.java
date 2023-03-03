package com.act.snapjob_worker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;

public class Receipt extends AppCompatActivity {

    String rclientName, rtransId, rclientAdd, rdate, rworkerName, rtransDesc, cWorkerID, rTransStatus;
    private Button payment,backReturn;
    private TextView clientNameTxt, clientAddTxt, transIdTxt, dateTxt, workerName, transDescTxt, transStat, transFee;

    private DatabaseReference databaseReference, workerReference;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        databaseReference = FirebaseDatabase.getInstance().getReference("Transactions");
        workerReference = FirebaseDatabase.getInstance().getReference("Workers");

        cWorkerID = firebaseUser.getUid();

        payment = (Button) findViewById(R.id.paymentReceive);
        backReturn = (Button) findViewById(R.id.returnButton);
        clientNameTxt = (TextView) findViewById(R.id.clientNameR);
        clientAddTxt = (TextView) findViewById(R.id.clientAddR);
        transIdTxt = (TextView) findViewById(R.id.transID);
        dateTxt = (TextView) findViewById(R.id.dateR);
        workerName = (TextView) findViewById(R.id.workerNameR);
        transDescTxt = (TextView) findViewById(R.id.transDescriptR);
        transStat = (TextView) findViewById(R.id.transStatus);
        transFee = (TextView) findViewById(R.id.transFeeReceipt);

        rclientName = getIntent().getStringExtra("clientName");
        rclientAdd = getIntent().getStringExtra("clientAdd");
        rtransId = getIntent().getStringExtra("transId");
        rdate = getIntent().getStringExtra("date");
        rworkerName = getIntent().getStringExtra("workerName");
        rtransDesc = getIntent().getStringExtra("transDesc");
        rTransStatus = getIntent().getStringExtra("transactionStatus");


        dateTxt.setText("Date: " + rdate);
        clientNameTxt.setText("Client Name:" + rclientName);
        clientAddTxt.setText("Address: " + rclientAdd);
        transIdTxt.setText("TID: " + rtransId);
        workerName.setText("Worker Name: " + rworkerName);
        transDescTxt.setText("Description: " + rtransDesc);
        transStat.setText("Status: " + rTransStatus);

        if (rTransStatus.equals("Complete")){
            payment.setVisibility(View.GONE);
            transStat.setVisibility(View.GONE);
            backReturn.setVisibility(View.VISIBLE);
        }
        else if (rTransStatus.equals("Declined")){
            payment.setVisibility(View.GONE);
            transFee.setVisibility(View.GONE);
            backReturn.setVisibility(View.VISIBLE);
        }
        else {
            transStat.setVisibility(View.GONE);
            payment.setVisibility(View.VISIBLE);
            backReturn.setVisibility(View.GONE);
        }

        //back button
        backReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //payment button
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirm();
            }
        });

    }
    //dialog confirm
    private void dialogConfirm(){
        //final View view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you confirm Payment Release?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                HashMap hashMap = new HashMap();
                hashMap.put("transactionStatus","Complete");
                databaseReference.child(rtransId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            updateWorkerStatus();
                            Toast.makeText(Receipt.this,"Job Done!", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(Receipt.this,"Error Completing Payment!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        builder.show();
    }
    private void updateWorkerStatus(){
        HashMap hashMap1 = new HashMap();
        hashMap1.put("status","Available");
        workerReference.child(cWorkerID).updateChildren(hashMap1).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(Receipt.this,Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(Receipt.this,"There was an Error Updating Worker Status!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}