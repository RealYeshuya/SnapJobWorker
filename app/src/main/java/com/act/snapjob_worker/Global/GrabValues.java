package com.act.snapjob_worker.Global;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.act.snapjob_worker.Services.MyFirebaseMessagingService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import static android.content.ContentValues.TAG;

public class GrabValues extends AppCompatActivity {

    private DocumentReference documentReference;
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String userID, fullName, age, job, phoneNum;
    private FirebaseUser user;

    public static void updateToken(Context context, String token) {
        TokenModel tokenModel = new TokenModel(token);

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show()).addOnSuccessListener(aVoid -> {

        });
    }

    public void getSpecifiedUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = mAuth.getCurrentUser().getUid();
        documentReference = fStore.collection("Workers").document(userID);
    }

    public String retrieveName() {
        getSpecifiedUser();
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()) {
                    GrabValues.this.fullName = value.getString("fullName");
                }
            }
        });
        return this.fullName;
    }

    public String retrieveAge() {
        getSpecifiedUser();
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()) {
                    GrabValues.this.age = value.getString("age");
                }
            }
        });
        return this.age;
    }

    public String retrieveJob() {
        getSpecifiedUser();
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()) {
                    GrabValues.this.job = value.getString("job");
                }
            }
        });
        return this.job;
    }

    public String retrievePhoneNum() {
        getSpecifiedUser();
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, error.toString());
                    return;
                }
                if (value.exists()) {
                    GrabValues.this.phoneNum = value.getString("job");
                }
            }
        });
        return this.phoneNum;
    }
}
