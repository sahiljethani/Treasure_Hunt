package com.example.treasurehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.treasurehunt.Models.UserLocation;
import com.example.treasurehunt.Models.Users;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

import static com.example.treasurehunt.util.Constants.MAPVIEW_BUNDLE_KEY;

public class Map extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseFirestore mDb;

    private FusedLocationProviderClient mFusedLocationClient;
    private UserLocation mUserLocation;

    private static final String TAG = "MAP";

    private FirebaseAuth mAuth;
   // private ListenerRegistration  mUserListEventListener;

    private GoogleMap mGooglemap;
    private LatLngBounds mMapBoundary;
    private UserLocation mCurrUserPos;





    private MapView mMapView;

    private GeoPoint geoPoint;

    private ArrayList <UserLocation> ArrayUserLocation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
        getUserDetails();




    }

    public void getUserFromFireStore() {
        Log.d(TAG, "getUserLocations: IT IS CALLED");
       mDb.collection("User Location")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if(task.getResult()!=null) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserLocation userlocation=document.toObject(UserLocation.class);
                                ArrayUserLocation.add(userlocation);
                                Log.d(TAG, "onComplete: User is added"+ userlocation.getUser().getUsername());

                            }  }

                            printer();
                            setCurrUserPos();
                            setCameraView();
                            setMarkers();


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

       /* CollectionReference usersRef = mDb
                .collection("User Location");


        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if(queryDocumentSnapshots != null){

                            // Clear the list and add all the users again
                            ArrayUserLocation.clear();
                            ArrayUserLocation = new ArrayList<>();

                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                UserLocation user = doc.toObject(UserLocation.class);
                                ArrayUserLocation.add(user);
                                Log.d(TAG, "User Added is " + user.getUser().getUsername());
                            }
                            printer();

                            Log.d(TAG, "onEvent: user list size: " + ArrayUserLocation.size());
                        }
                    }
                });*/





    }

    private void setMarkers() {

        for ( UserLocation userLocation : ArrayUserLocation)
            mGooglemap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude()))
                    .title(userLocation.getUser().getUsername()));


    }


    private void setCameraView() {

        double bottomBoundary = mCurrUserPos.getGeoPoint().getLatitude() - .01;
        double leftBoundary = mCurrUserPos.getGeoPoint().getLongitude() - .01;
        double topBoundary = mCurrUserPos.getGeoPoint().getLatitude() + .01;
        double rightBoundary = mCurrUserPos.getGeoPoint().getLongitude() + .01;

        mMapBoundary = new LatLngBounds(


                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGooglemap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }









    





    public void getUserDetails(){
        Log.d(TAG, "getUserDetails: IS CALLED");
        if(mUserLocation == null){
            mUserLocation = new UserLocation();
            DocumentReference userRef = mDb.collection("Users")
                    .document(FirebaseAuth.getInstance().getUid());

            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully set the user client.");
                        Users user = task.getResult().toObject(Users.class);
                        mUserLocation.setUser(user);
                        getLastKnownLocation();
                    }
                }
            });
        }
        else{
            getLastKnownLocation();
        }
    }


    public void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUserLocation.setGeoPoint(geoPoint);
                    mUserLocation.setTime(null);
                    saveUserLocation();
                }
            }
        });

    }


    public void saveUserLocation(){
        Log.d(TAG, "saveUserLocation: IS CALLED");

        if(mUserLocation != null){
            DocumentReference locationRef = mDb
                    .collection("User Location")
                    .document(FirebaseAuth.getInstance().getUid());

            locationRef.set(mUserLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: Location added");
                        getUserFromFireStore();
                    }
                }
            });

        }
        else {
            getLastKnownLocation(); } 
    }

    public void printer() {
        Log.d(TAG, "printer: IS called");
        for(UserLocation userlocation: ArrayUserLocation) {
            Log.d(TAG, "Printer: user location "+ userlocation.getUser().getUsername() + ",,,"+ userlocation.getGeoPoint().getLatitude()+ "," + userlocation.getGeoPoint().getLongitude());
        }

    }


    public void signOut(View view) {
        try {
            mAuth.signOut();
        } catch (Exception e) {
            Log.d(TAG, "signOut: catching");
            e.printStackTrace();
        }
        updateUI(null);


    }

    public void updateUI(FirebaseUser user) {
        if(user==null)
        {
            Log.d(TAG, "updateUI: Here i am");
            Intent intent = new Intent(Map.this, LoginActivity.class);
            startActivity(intent);
        }

    }


    private void setCurrUserPos () {
        for (UserLocation userlocation : ArrayUserLocation)
            if(userlocation.getUser().getUserid().equals(FirebaseAuth.getInstance().getUid()))
                mCurrUserPos=userlocation;




    }




    @Override
    public  void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMyLocationEnabled(true);
        mGooglemap = map;

    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
