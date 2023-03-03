package com.act.snapjob_worker.Global;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.act.snapjob_worker.Fragment.LocationFragment;
import com.act.snapjob_worker.R;
import com.google.android.gms.maps.MapFragment;

public class UserTransaction  extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usertransaction);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Bundle data = new Bundle();

        String uName = getIntent().getStringExtra("userName");
        String uAdd = getIntent().getStringExtra("address");
        String uTransactionStatus = getIntent().getStringExtra("transactionStatus");
        String uId = getIntent().getStringExtra("userId");
        String transId = getIntent().getStringExtra("uTransId");
        String workerName = getIntent().getStringExtra("workerName");
        String transactionDescription = getIntent().getStringExtra("transDesc");
        String userPhoneNum = getIntent().getStringExtra("userPhoneNum");

        data.putString("userFullName",uName);
        data.putString("uAddress",uAdd);
        data.putString("uTransactionStatus",uTransactionStatus);
        data.putString("userID",uId);
        data.putString("transId",transId);
        data.putString("workerName",workerName);
        data.putString("transDesc",transactionDescription);
        data.putString("userPhoneNum", userPhoneNum);

        LocationFragment locationFragment = new LocationFragment();
        locationFragment.setArguments(data);
        fragmentTransaction.replace(R.id.userTransactionFrame, locationFragment);
        fragmentTransaction.commit();

        //Toast.makeText(this, uName + uId, Toast.LENGTH_SHORT).show();
    }
}
