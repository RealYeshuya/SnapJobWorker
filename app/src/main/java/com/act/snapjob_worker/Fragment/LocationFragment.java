package com.act.snapjob_worker.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.act.snapjob_worker.Callback.IFirebaseUserInfoListener;
import com.act.snapjob_worker.Callback.IfirebaseFailedListener;
import com.act.snapjob_worker.Global.Common;
import com.act.snapjob_worker.Global.GeoQueryModel;
import com.act.snapjob_worker.Global.UserGeoModel;
import com.act.snapjob_worker.Global.UserInfoModel;
import com.act.snapjob_worker.R;
import com.act.snapjob_worker.WorkProgress;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LocationFragment extends Fragment implements OnMapReadyCallback, IfirebaseFailedListener, IFirebaseUserInfoListener {

    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference workerReference;
    private LatLng newPosition, clientPosition;
    Polyline polylineDistance = null;
    String userID;
    String userName, userAddress, userIdd, userTransactionStatus, transactionID, workerName, transactionDescription, userPhoneNum;

    private GoogleMap mMap;

    //date
    Calendar c = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String strDate = sdf.format(c.getTime());

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Load User
    private double distance = 1.0; //default in km
    private static final double LIMIT_RANGE = 10.0; // km
    private Location previousLocation, currentLocation; // Use to calculate distance

    //Listener
    IFirebaseUserInfoListener iFirebaseUserInfoListener;
    IfirebaseFailedListener iFirebaseFailedListener;

    SupportMapFragment mapFragment;

    private boolean isFirstTime = true;

    DatabaseReference onlineRef, currentUserRef, usersLocationRef;
    GeoFire geoFire;

    TextView nameCard,addressCard,phoneCard;
    Button arriveButton, callButton;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.root_layout)
    FrameLayout root_layout;

    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentUserRef != null) {
                currentUserRef.onDisconnect().removeValue();

            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
        }
    };
    private String cityName;

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
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueEventListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map_fragment, container, false);

        Bundle data = getArguments();

        nameCard = view.findViewById(R.id.userNameCard);
        addressCard = view.findViewById(R.id.userAddressCard);
        //phoneCard = view.findViewById(R.id.userNumCard);

        arriveButton = view.findViewById(R.id.arriveButton);
        callButton = view.findViewById(R.id.callButton);
        //moreInfo = view.findViewById(R.id.moreInfo);


        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

        if (data != null){
            userName = data.getString("userFullName");
            userAddress = data.getString("uAddress");
            userTransactionStatus = data.getString("uTransactionStatus");
            userIdd = data.getString("userID");
            transactionID = data.getString("transId");
            workerName = data.getString("workerName");
            transactionDescription = data.getString("transDesc");
            userPhoneNum = data.getString("userPhoneNum");


            //Toast.makeText(getContext(), userName + userIdd, Toast.LENGTH_SHORT).show();
        }

        // Name, phone, Address card
        nameCard.setText(userName);
        addressCard.setText(userAddress);
        //phoneCard.setText(userPhoneNum);
        callButton.setText(userPhoneNum);

/*
        moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View vDialog = getLayoutInflater().inflate(R.layout.locationdialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                View clientName = vDialog.findViewById(R.id.clientNameMore);
                View clientJob = vDialog.findViewById(R.id.clientJobMore);

                builder.setView(clientName);
                builder.setView(clientJob);



            }
        });
        */


        //when clicking arrive button
        arriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                workerReference = FirebaseDatabase.getInstance().getReference("Transactions").child(transactionID);

                HashMap hashMap = new HashMap();
                hashMap.put("workerArrived", "Yes");

                //bring data to another class
                workerReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(getContext(), WorkProgress.class);
                            intent.putExtra("userName", userName);
                            intent.putExtra("userAdd", userAddress);
                            intent.putExtra("transID", transactionID);
                            intent.putExtra("workerName", workerName);
                            intent.putExtra("transDesc", transactionDescription);
                            //intent.putExtra("transactionStatus",userTransactionStatus);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getContext(),"Error Arriving!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
        //call button
        callButton.setOnClickListener(new View.OnClickListener() {
        //dialog to confirm call button
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Call " + userName + "?")
                        .setMessage(userPhoneNum)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String callUserNum = "tel:" + userPhoneNum;
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse(callUserNum));

                                if (Build.VERSION.SDK_INT > 23) {
                                    startActivity(intent);
                                } else {

                                    if (ActivityCompat.checkSelfPermission(getContext(),
                                            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                        Toast.makeText(getActivity(), "Permission Not Granted ", Toast.LENGTH_SHORT).show();
                                    } else {
                                        final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                                        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, 9);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });
        return view;
    }

    private void init() {

        iFirebaseFailedListener = this;
        iFirebaseUserInfoListener = this;

        userID = mAuth.getCurrentUser().getUid();
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mapFragment.getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }

        registerOnlineSystem();

        buildLocationRequest();

        buildLocationCallback();
        
        updateLocation();

        loadAvailableUsers();

    }

    private void updateLocation() {
        if (fusedLocationProviderClient == null){
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            fusedLocationProviderClient.getLastLocation()
                    .addOnFailureListener(e -> Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show()).addOnSuccessListener(location -> {
                //get address name

            });
        }
    }

    private void buildLocationCallback() {
        if (locationCallback == null){
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    //if user has change location calculate and load again
                    if (isFirstTime) {
                        previousLocation = currentLocation = locationResult.getLastLocation();
                        isFirstTime = false;
                    } else {
                        previousLocation = currentLocation;
                        currentLocation = locationResult.getLastLocation();
                    }

                    if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) // Not over range
                        loadAvailableUsers();
                    else {
                        //Do Nothing
                    }

                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude(), 1);
                        String cityName = addressList.get(0).getLocality();

                        usersLocationRef = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCE)
                                .child(cityName);
                        currentUserRef = usersLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        geoFire = new GeoFire(usersLocationRef);


                    } catch (IOException e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }


                    //get address name
                    //Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    //List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocation(locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude(), 1);
                        String cityName = addressList.get(0).getLocality();


                        usersLocationRef = FirebaseDatabase.getInstance().getReference(Common.WORKER_LOCATION_REFERENCE)
                                .child(cityName);
                        currentUserRef = usersLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        geoFire = new GeoFire(usersLocationRef);

                        //Update Location
                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()),
                                (key, error) -> {
                                    if (error != null)
                                        Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();

                                });

                        registerOnlineSystem(); //only register

                    } catch (IOException e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }
            };
        }
    }

    private void buildLocationRequest() {
        if (locationRequest == null){
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(50f);
            locationRequest.setInterval(15000);
            locationRequest.setFastestInterval(10000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void loadAvailableUsers() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mapFragment.getView(),getString(R.string.permission_require),Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(mapFragment.getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(location -> {
            //Load user/s
            Geocoder geocoder = new Geocoder(getContext(),Locale.getDefault());
            List<Address> addressList;
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                cityName = addressList.get(0).getLocality();

                //Query
                DatabaseReference user_location_ref = FirebaseDatabase.getInstance().getReference(Common.USER_LOCATION_REFERENCE).child(cityName);
                GeoFire geoFire = new GeoFire(user_location_ref);
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),distance);

                geoQuery.removeAllListeners();

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        if (key.equals(userIdd)){
                            Common.userFound.add(new UserGeoModel(key, location));
                        }
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (distance <= LIMIT_RANGE){
                            distance++;
                            loadAvailableUsers(); //Continue search in new distance
                        }
                        else {
                            distance = 1.0; //Reset it
                            addUserMarker();
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Snackbar.make(getView(),error.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });

                //Listen to new user in city and range
                /*
                user_location_ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        //Have new user
                        GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                        GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                geoQueryModel.getL().get(1));
                        UserGeoModel userGeoModel = new UserGeoModel(snapshot.getKey(),
                                geoLocation);
                        Location newUserLocation = new Location("");
                        newUserLocation.setLatitude(geoLocation.latitude);
                        newUserLocation.setLongitude(geoLocation.longitude);
                        float newDistance = location.distanceTo(newUserLocation) / 1000;
                        if (newDistance <= LIMIT_RANGE)
                            findUserByKey(userGeoModel); //if user is range, add to map
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                */
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void addUserMarker() {
        if (Common.userFound.size() > 0){
            Observable.fromIterable(Common.userFound).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userGeoModel -> {
                        //on next
                        findUserByKey(userGeoModel);
                    }, throwable -> {
                        Snackbar.make(getView(),throwable.getMessage(),Snackbar.LENGTH_SHORT).show();
                    },() -> {

                    });
        }
        else {
            Snackbar.make(getView(), getString(R.string.users_not_found),Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findUserByKey(UserGeoModel userGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFO_REFERENCE)
                .child(userGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()){
                            userGeoModel.setUserInfoModel(snapshot.getValue(UserInfoModel.class));
                            iFirebaseUserInfoListener.onUserInfoLoadSuccess(userGeoModel);
                        }
                        else {
                            iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key) + userGeoModel.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListener.onFirebaseLoadFailed(error.getMessage());
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

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

                        //Move Location
                        buildLocationRequest();

                        buildLocationCallback();

                        updateLocation();
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
        Snackbar.make(mapFragment.getView(),"You are now visible",Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUserInfoLoadSuccess(UserGeoModel userGeoModel) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rsz_worker_help);
        // If already have marker with this key doesn't set again
        if (!Common.markerList.containsKey(userGeoModel.getKey())) {
            Common.markerList.put(userGeoModel.getKey(), mMap.addMarker(new MarkerOptions().position(new LatLng(userGeoModel.getGeoLocation()
                    .latitude, userGeoModel.getGeoLocation().longitude))
                    .flat(true)
                    .title(Common.buildName(userGeoModel.getUserInfoModel().getFullName(), userGeoModel.getUserInfoModel().getEmail()))
                    .snippet(userGeoModel.getUserInfoModel().getPhoneNumber())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))));

            clientPosition = new LatLng(userGeoModel.getGeoLocation().latitude,userGeoModel.getGeoLocation().longitude);
            createPolyline(newPosition,clientPosition);

        }


        if (!TextUtils.isEmpty(cityName)){
            DatabaseReference userLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.USER_LOCATION_REFERENCE)
                    .child(cityName)
                    .child(userGeoModel.getKey());
            userLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChildren()){
                        if (Common.markerList.get(userGeoModel.getKey()) != null)
                            Common.markerList.get(userGeoModel.getKey()).remove(); //remove marker
                        Common.markerList.remove(userGeoModel.getKey()); // Remove marker info from hash map
                        userLocation.removeEventListener(this); // Remove Event listener
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //Snackbar.make(mapFragment.getView(),error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createPolyline(LatLng newPosition, LatLng clientPosition) {
        PolylineOptions polylineOptions = new PolylineOptions().add(newPosition,clientPosition);
        polylineDistance = mMap.addPolyline(polylineOptions);
        polylineDistance.setColor(getResources().getColor(R.color.colorPrimary));
    }

}
