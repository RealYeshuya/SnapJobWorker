package com.act.snapjob_worker.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.act.snapjob_worker.Global.GrabValues;
import com.act.snapjob_worker.Global.Transactions;
import com.act.snapjob_worker.Global.User;
import com.act.snapjob_worker.R;
import com.act.snapjob_worker.RequestAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryFragment extends Fragment {

    private FirebaseUser user;
    private RecyclerView listTransHistory;
    private DatabaseReference transReference;
    private RequestAdapter requestAdapter;
    ArrayList<Transactions> listHistory;
    private String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_history,container,false);


        listTransHistory = (RecyclerView) view.findViewById(R.id.listTransHistory);
        transReference = FirebaseDatabase.getInstance().getReference("Transactions");
        listTransHistory.setHasFixedSize(true);
        listTransHistory.setLayoutManager(new LinearLayoutManager(getActivity()));

        TextView noTrans = (TextView) view.findViewById(R.id.no_trans);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();

        listHistory = new ArrayList<>();
        requestAdapter = new RequestAdapter(getActivity(), listHistory);
        listTransHistory.setAdapter(requestAdapter);

        transReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Transactions transactions = dataSnapshot.getValue(Transactions.class);
                    String wid = transactions.getWorkerId();
                    String transactionStat = transactions.getTransactionStatus();
                    if(wid.equals(userID)){
                        if(transactionStat.equals("Complete")||transactionStat.equals("Declined")){
                            listHistory.add(0,transactions);
                            noTrans.setVisibility(View.GONE);
                        }
                    }
                    listTransHistory.post(new Runnable() {
                        @Override
                        public void run() {
                            listTransHistory.smoothScrollToPosition(0);
                        }
                    });
                }
                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //DO NOTHING
            }
        });

        return view;
    }
}
