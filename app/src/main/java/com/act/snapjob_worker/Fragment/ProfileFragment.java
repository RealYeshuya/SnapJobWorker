package com.act.snapjob_worker.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.act.snapjob_worker.Global.GrabValues;
import com.act.snapjob_worker.MainActivity;
import com.act.snapjob_worker.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment {

    private FirebaseUser user;
    private DatabaseReference reference;
    private String userID;
    private String status = "";
    private Button logout, editProfile;
    private EditText nameField, ageField, jobField;

    private DocumentReference documentReference;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    GrabValues grabValues = new GrabValues();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = mAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("Workers").document(userID);

        logout = view.findViewById(R.id.logout);
        editProfile = view.findViewById(R.id.editProfile);

        final TextView fullnameView = view.findViewById(R.id.workerName);
        final TextView ageView = view.findViewById(R.id.workerAge);
        final TextView jobView = view.findViewById(R.id.workerWork);
        TextView statusView = view.findViewById(R.id.statusJ);
        ImageView wPhoto = view.findViewById(R.id.workerPic);

/*
        wPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Change Photo?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storageReference = FirebaseStorage.getInstance().getReference();

                                Glide.with(getActivity()).load(documentReference).
                            }
                        });
                return false;
            }
        });

 */
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null){
                    Toast.makeText(getActivity(),"Error Loading Data", Toast.LENGTH_LONG).show();
                    Log.d(TAG, error.toString());
                    return;
                }

                if (value.exists()){
                    String fullName = value.getString("fullName");
                    String age = value.getString("age");
                    String job = value.getString("job");
                    status = value.getString("available");

                    fullnameView.setText(fullName);
                    ageView.setText(age);
                    jobView.setText(job);
                    statusView.setText(status);

                    editProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View vDialog = getLayoutInflater().inflate(R.layout.edit_profile_dialog,null);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                            nameField = vDialog.findViewById(R.id.fullnameNew);
                            ageField = vDialog.findViewById(R.id.ageNew);
                            jobField = vDialog.findViewById(R.id.jobNew);

                            nameField.setHint(fullName);
                            ageField.setHint(age);
                            jobField.setHint(job);

                            builder.setTitle("Edit Profile")
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            String nameString = nameField.getText().toString().trim();
                                            String ageString = ageField.getText().toString().trim();
                                            String jobString = jobField.getText().toString().trim();

                                            int h = 0;

                                            if (nameString.isEmpty() || ageString.isEmpty() || jobString.isEmpty()) {
                                                h = 1;
                                                Toast.makeText(getContext(),"All credentials must be filled!", Toast.LENGTH_LONG).show();
                                            }
                                            else {
                                                h = 2;
                                                //Toast.makeText(getContext(),"Updating...", Toast.LENGTH_SHORT).show();
                                            }
                                            if (h == 2){
                                                documentReference.update("fullName", nameString).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Unable to update Name", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                documentReference.update("age", ageString).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Unable to update Age", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                documentReference.update("job", jobString).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Unable to update Job", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                //Toast.makeText(getContext(),"Profile Updated", Toast.LENGTH_LONG).show();
                                                Snackbar.make(getContext(),getView(),"Profile Successfully Updated", Snackbar.LENGTH_LONG).show();
                                            }
                                            else {
                                                Snackbar.make(getContext(),getView(),"An error occurred", Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Do Nothing
                                }
                            });
                            builder.setView(vDialog);
                            builder.show();;
                        }
                    });
                }
            }
        });
        checkAvailablility(status);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String curStat = checkAvailablility(status);

                if (curStat.equals("Available")){
                    statusalertDialog();
                    statusalertDialog().show();
                }
                if (curStat.equals("Not Available")){
                    //update status when logging out
                    documentReference.update("available","Not Available").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                //logout
                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Toast.makeText(getContext(),"Logged Out", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getContext(),"There is an error occurred when logging out", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        return view;
    }

    private String checkAvailablility(String status) {
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Toast.makeText(getActivity(),"Error Loading Data", Toast.LENGTH_LONG).show();
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()){
                    ProfileFragment.this.status = value.getString("available");
                }
            }
        }); return this.status;
    }

    private Dialog statusalertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Oops..")
                .setMessage("Job Status must be set to 'Off Duty' first before Logging out!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, new HomeFragment());
                        /*BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
                            @Override
                            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                final boolean equals = item.equals(R.id.ic_home);
                                return true;
                            }
                        };*/
                        fragmentTransaction.commit();
                    }
                });
        return builder.create();
    }
}
