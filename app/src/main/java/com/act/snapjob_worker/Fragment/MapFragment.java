package com.act.snapjob_worker.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.act.snapjob_worker.Global.Common;
import com.act.snapjob_worker.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String userID;

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    SupportMapFragment mapFragment;

    DatabaseReference onlineRef, currentUserRef, driversLocationRef;
    GeoFire geoFire;

    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null)
                currentUserRef.onDisconnect().removeValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map_fragment, container, false);

        init();

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }

    private void init() {

        userID = mAuth.getCurrentUser().getUid();
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }

        registerOnlineSystem();

        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);



                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                //Update Location
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()),
                        (key, error) -> {
                            if (error != null)
                                Snackbar.make(mapFragment.getView(),error.getMessage(),Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(mapFragment.getView(),"GPS is online",Snackbar.LENGTH_LONG).show();
                        });

                //get address name
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude(),1);
                    String cityName = addressList.get(0).getLocality();

                    driversLocationRef = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCE)
                            .child(cityName);
                    currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    geoFire = new GeoFire(driversLocationRef);

                    //Update Location
                    geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()),
                            (key, error) -> {
                                if (error != null)
                                    Snackbar.make(mapFragment.getView(),error.getMessage(),Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(mapFragment.getView(),"GPS is online",Snackbar.LENGTH_LONG).show();
                            });
                }catch (IOException e){
                    Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
                }
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(),getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show()).addOnSuccessListener(location -> {
            //get address name
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addressList;
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                String cityName = addressList.get(0).getLocality();

                driversLocationRef = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCE)
                        .child(cityName);
                currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                geoFire = new GeoFire(driversLocationRef);


            } catch (IOException e) {
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        //Check Permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(getView(),getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation().addOnFailureListener(e -> Toast.makeText(getContext(),"" + e.getMessage(), Toast.LENGTH_LONG).show()).addOnSuccessListener(location -> {
                                LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));
                            });
                            return true;
                        });

                        //Set Layout buttton
                        View locationbutton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1"))
                        .getParent()).findViewById(Integer.parseInt("2"));

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationbutton.getLayoutParams();
                        //Right bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
                        params.setMargins(0,50,0,0);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"Permission " + permissionDeniedResponse.getPermissionName() + "" + "was denied!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.uber_maps_style));
            if (!success)
                Log.e("ERROR","Style Parse Error");
        }catch (Resources.NotFoundException e){
            Log.e("ERROR",e.getMessage());
        }
    }
}
