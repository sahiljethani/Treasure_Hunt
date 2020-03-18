package com.example.treasurehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.treasurehunt.Models.UserLocation;
import com.example.treasurehunt.Models.Users;
import com.example.treasurehunt.Services.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
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
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private MapView mMapView;

    private GeoPoint geoPoint;

    private ArrayList <UserLocation> ArrayUserLocation = new ArrayList<>();
    private ArrayList <Marker> mMarkers = new ArrayList<>();
    private ArrayList <Target> mtargets = new ArrayList<>();

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



    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
//        this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

               Map.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
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

                                Marker marker=mGooglemap.addMarker(new MarkerOptions()
                                        .position(new LatLng(userlocation.getGeoPoint().getLatitude(),userlocation.getGeoPoint().getLongitude()))
                                        .title(userlocation.getUser().getUsername()));
                                mMarkers.add(marker);


                                Log.d(TAG, "onComplete: User is added"+ userlocation.getUser().getUsername());

                            }  }

                            printer();
                            setMarkerIcon();
                            setCurrUserPos();
                            setCameraView();
                            startLocationService();




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

    private void setMarkerIcon() {


        for(UserLocation userlocation: ArrayUserLocation)
        {
            for( Marker marker : mMarkers)
            {
                if(marker.getTitle().equals(userlocation.getUser().getUsername()))
                {
                    Target target = new PicassoMarker(marker);
                    mtargets.add(target);
                    Picasso.get().load(userlocation.getUser().getProfileImageUrl()).resize(84, 125).into(target);


                }


            }



        }

    }





    /*private void setMarkers() {

        for ( UserLocation userLocation : ArrayUserLocation)
            mGooglemap.addMarker(new MarkerOptions()
                    .position(new LatLng(userLocation.getGeoPoint().getLatitude(),userLocation.getGeoPoint().getLongitude()))
                    .title(userLocation.getUser().getUsername()));


    }*/


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
                        ((UserClient)(getApplicationContext())).setUser(user);
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







    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for(final UserLocation userLocation : ArrayUserLocation){

                DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                        .collection("User Location")
                        .document(userLocation.getUser().getUserid());

                userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                            // update the location

                                try {
                                    if (userLocation.getUser().getUserid().equals(updatedUserLocation.getUser().getUserid())) {

                                        LatLng updatedLatLng = new LatLng(
                                                updatedUserLocation.getGeoPoint().getLatitude(),
                                                updatedUserLocation.getGeoPoint().getLongitude()

                                        );

                                        addingMarkers(userLocation,updatedLatLng);

                                       /* Mar

                                        if(marker!=null)
                                            marker.remove();

                                        marker= mGooglemap.addMarker(new MarkerOptions()
                                                .position(updatedLatLng)
                                                .title(userLocation.getUser().getUsername()));
                                        marker.setVisible(true);*/



                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }

                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }


    private void addingMarkers(UserLocation userLocation, LatLng updatedLatLng ){

        for( Marker marker:mMarkers)
        {
            if(marker.getTitle().equals(userLocation.getUser().getUsername()))
            {

             marker.setPosition(updatedLatLng);

            }

        }

    }




    @Override
    public  void onResume() {
        super.onResume();
        mMapView.onResume();
        startUserLocationsRunnable();



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
        stopLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
