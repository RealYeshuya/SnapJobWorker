package com.act.snapjob_worker;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.act.snapjob_worker.Fragment.HomeFragment;
import com.act.snapjob_worker.Fragment.MapFragment;
import com.act.snapjob_worker.Fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navlistener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new HomeFragment()).commit();

        //FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.add(R.id.fragment_container,new HomeFragment());
        //fragmentTransaction.commit();
    }
    private BottomNavigationView.OnNavigationItemSelectedListener navlistener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()){
                case R.id.ic_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.ic_work:
                    selectedFragment = new MapFragment();
                    break;
                case R.id.ic_profile:
                    selectedFragment = new ProfileFragment();
                    break;
                default:
                    selectedFragment = new HomeFragment();
                break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
            return true;
        }
    };
}
