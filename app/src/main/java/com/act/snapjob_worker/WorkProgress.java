package com.act.snapjob_worker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.act.snapjob_worker.Global.Client;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import pl.droidsonroids.gif.GifImageView;

public class WorkProgress extends AppCompatActivity {

    String clientName, transId, clientAdd, workerName, transStatus, transDesc, clientNum, uKey, uPic;
    private Button fininshJob;
    //private ProgressBar progressBar;
    private TextView clientNameTxt, clientAddTxt, transIdTxt, dateTxt, transDescTxt, clientNumTxt;
    private ImageView userPic;
    private GifImageView loadingJob;

    private DatabaseReference databaseReference, imageReference;


    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String strDate = sdf.format(c.getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_progress);

        databaseReference = FirebaseDatabase.getInstance().getReference("Transactions");
        imageReference = FirebaseDatabase.getInstance().getReference();

        fininshJob = (Button) findViewById(R.id.finishButton);
        //progressBar = (ProgressBar) findViewById(R.id.progressbar);
        loadingJob = (GifImageView) findViewById(R.id.loadingJob);
        clientNameTxt = (TextView) findViewById(R.id.clientName);
        clientAddTxt = (TextView) findViewById(R.id.clientAdd);
        transIdTxt = (TextView) findViewById(R.id.transactionId);
        dateTxt = (TextView) findViewById(R.id.dateR);
        transDescTxt = (TextView) findViewById(R.id.transDescript);
        //clientNumTxt = (TextView) findViewById(R.id.clientNum);

        userPic = (ImageView) findViewById(R.id.uPic);

        clientName = getIntent().getStringExtra("userName");
        clientAdd = getIntent().getStringExtra("userAdd");
        transId = getIntent().getStringExtra("transID");
        workerName = getIntent().getStringExtra("workerName");
        transDesc = getIntent().getStringExtra("transDesc");
        //transStatus = getIntent().getStringExtra("transactionStatus");


        dateTxt.setText("Date: "+ strDate);
        transDescTxt.setText("Description: "+transDesc);
        clientNameTxt.setText("Name "+clientName);
        clientAddTxt.setText("Address "+clientAdd);
        transIdTxt.setText("TID: "+transId);

        Glide.with(this).load(R.drawable.repair).into(loadingJob);
        imageReference.child("Users").orderByChild("fullName").equalTo(clientName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keySnapshot: snapshot.getChildren()){
                    String userKey = keySnapshot.getKey();
                    uKey = userKey;

                    imageReference.child("Users").child(uKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Client client = snapshot.getValue(Client.class);
                            if (client != null){
                                uPic = client.image;
                                Picasso.get()
                                        .load(uPic)
                                        .fit()
                                        .centerCrop()
                                        .into(userPic);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //Do nothing
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Do nothing
            }
        });

        fininshJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                transStatus = "Pending";

                databaseReference.child(transId).child("transactionStatus").setValue(transStatus).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(WorkProgress.this, Receipt.class);
                            intent.putExtra("clientName",clientName);
                            intent.putExtra("clientAdd",clientAdd);
                            intent.putExtra("transId",transId);
                            intent.putExtra("date",strDate);
                            intent.putExtra("workerName",workerName);
                            intent.putExtra("transDesc",transDesc);
                            intent.putExtra("transactionStatus",transStatus);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(WorkProgress.this,"Error Updating Status!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}