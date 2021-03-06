package com.example.treasurehunt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.TextView;

import com.example.treasurehunt.Models.Treasure;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
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
import java.util.List;
import java.util.UUID;

import static com.example.treasurehunt.util.Constants.MAPVIEW_BUNDLE_KEY;

public class Map extends AppCompatActivity implements OnMapReadyCallback, TreasureDialog.OnInputListener {



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
    private ArrayList <Treasure> mtreasures = new ArrayList<>();
    private ArrayList <Marker> mTreasureMarkers = new ArrayList<>();

    public TextView mTreasureMessage;
    private String mInput;
    private GeoPoint treasureGeoPoint;



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
        getTreasureFromFirebase();




    }


    // Getting User Information and Saving it in firebase

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
                        Log.d(TAG, "User Location added in save user location method");
                        setCameraView();
                        getUserFromFireStore();
                        startLocationService();
                    }
                }
            });

        }
        else {
            getLastKnownLocation(); }
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
            if("com.example.treasurehunt.Services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }



    private void setCameraView() {

        double bottomBoundary = mUserLocation.getGeoPoint().getLatitude() - .01;
        double leftBoundary = mUserLocation.getGeoPoint().getLongitude() - .01;
        double topBoundary = mUserLocation.getGeoPoint().getLatitude() + .01;
        double rightBoundary = mUserLocation.getGeoPoint().getLongitude() + .01;

        mMapBoundary = new LatLngBounds(


                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGooglemap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }


    public void getUserFromFireStore (){
        Log.d(TAG, "getUserFromFireStore: It is called");

        mDb.collection("User Location")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "listen:error", e);
                            return;
                        }


                        if(snapshots!=null)
                        {

                            Log.d(TAG, "onEvent: -----------------------------------------------------");
                            List<DocumentChange> documentChangeList=snapshots.getDocumentChanges();
                            for ( DocumentChange documentChange: documentChangeList)
                            {
                                Log.d(TAG, "USER: THE TYPE OF CHANGE IS "+documentChange.getType());
                                String source = snapshots.getMetadata().hasPendingWrites()
                                        ? "Local" : "Server";
                                Log.d(TAG, "USER: THE SOURCE OF THIS CHANGE IS "+source);

                                String isCache = snapshots.getMetadata().isFromCache()?"true":"false";
                                Log.d(TAG, "USER: The change is from cache?   " +isCache);

                                Log.d(TAG, "USER: THE NAME OF THE NEW USER IS " + documentChange
                                        .getDocument()
                                        .toObject(UserLocation.class)
                                         .getUser()
                                          .getUsername());


                                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                    stopLocationUpdates();
                                    UserLocation userlocation = documentChange.getDocument().toObject(UserLocation.class);


                                        ArrayUserLocation.add(userlocation);
                                        Log.d(TAG, "Adding the user in the list  " + userlocation.getUser().getUsername());

                                        Marker marker = mGooglemap.addMarker(new MarkerOptions()
                                                .position(new LatLng(userlocation.getGeoPoint().getLatitude(), userlocation.getGeoPoint().getLongitude()))
                                                .title(userlocation.getUser().getUsername()));
                                        if (userlocation == mUserLocation)
                                            marker.setSnippet("ONLINE");
                                        mMarkers.add(marker);
                                        if (setUserMarkerIcon()) {
                                            startUserLocationsRunnable();
                                        }

                                }
                            }
                        }
                        else{


                            Log.d(TAG, "onEvent: QUERY SNAPSHOT IS NULL");
                        }


                            Log.d(TAG, "onEvent: Array size of user is " + ArrayUserLocation.size() + " SIZE OF MARKER IS " + mMarkers.size());

                        }




                });


    }



    private boolean setUserMarkerIcon() {


        for(UserLocation userlocation: ArrayUserLocation)

        {
            Log.d(TAG, "DISTANCE: The distance between "+userlocation.getUser().getUsername()+ " and "
            +mUserLocation.getUser().getUsername() + " is "+ distancebetween(userlocation.getGeoPoint(),mUserLocation.getGeoPoint()));


            for( Marker marker : mMarkers)
            {


                if(marker.getTitle().equals(userlocation.getUser().getUsername()))
                {
                    Target target = new PicassoMarker(marker);
                    mtargets.add(target);
                    Picasso.get().load(userlocation.getUser().getProfileImageUrl()).resize(84, 125).into(target);
                    Log.d(TAG, "setUserMarkerIcon: MAKING THE ICON FOR "+userlocation.getUser().getUsername());

                }
            }
        }

        return true;
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
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users.");

        try{

                 for (final UserLocation userLocation : ArrayUserLocation) {

                     DocumentReference userLocationRef = FirebaseFirestore.getInstance()
                             .collection("User Location")
                             .document(userLocation.getUser().getUserid());

                     userLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                             if (task.isSuccessful()) {

                                 final UserLocation updatedUserLocation = task.getResult().toObject(UserLocation.class);

                                 // update the location

                                 try {
                                     if (userLocation.getUser().getUserid().equals(updatedUserLocation.getUser().getUserid())) {

                                         LatLng updatedLatLng = new LatLng(
                                                 updatedUserLocation.getGeoPoint().getLatitude(),
                                                 updatedUserLocation.getGeoPoint().getLongitude()

                                         );

                                         userLocation.setGeoPoint(updatedUserLocation.getGeoPoint());

                                         UpdateMarkerPosition(userLocation, updatedLatLng);
                                         if(updatedUserLocation.getUser().getUserid().equals(mUserLocation.getUser().getUserid())){
                                             mUserLocation.setGeoPoint(updatedUserLocation.getGeoPoint());
                                             if( findingAlgo())
                                                Log.d(TAG, "onComplete: Moving On");


                                         }


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


    private void UpdateMarkerPosition(UserLocation userLocation, LatLng updatedLatLng ){

        for( Marker marker:mMarkers)
        {
            if(marker.getTitle().equals(userLocation.getUser().getUsername()))
            {

                marker.setPosition(updatedLatLng);

            }

        }

    }




    //ALL the methods related to treasure



    public void HideTheTreasure ( View view ){




        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    treasureGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                }
            }
        });

        Log.d(TAG, "onClick: opening dialog.");

        TreasureDialog dialog = new TreasureDialog();
        dialog.show(getSupportFragmentManager(), "Hide Treasure Dialog");




    }

    @Override
    public void sendInput(String input) {

        Log.d(TAG, "sendInput: Input is "+input);
        saveTreasure(input);




    }


    private void saveTreasure(String input) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String treasureId= UUID.randomUUID().toString();

        DocumentReference newTreasure = db.collection("Treasures").document(treasureId);





        final Treasure treasure= new Treasure();
        treasure.setTreasureId(treasureId);
        treasure.setGeoPoint(treasureGeoPoint);
        treasure.setUser(mUserLocation.getUser());
        treasure.setMessage(input);

        newTreasure.set(treasure).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Treasure Added");


                } else
                    Log.d(TAG, "onComplete: Not able to add Treasure");

            }
        });


    }
    private void getTreasureFromFirebase() {
        Log.d(TAG, "getTreasureFromFirebase: is called now ");


        mDb.collection("Treasures")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("TAG", "listen:error", e);
                            return;
                        }


                        if(snapshots!=null)
                        {

                            Log.d(TAG, "onEvent: -----------------------------------------------------");
                            List<DocumentChange> documentChangeList=snapshots.getDocumentChanges();
                            for ( DocumentChange documentChange: documentChangeList)
                            {
                                Log.d(TAG, "TREASURE: THE TYPE OF CHANGE IS "+documentChange.getType());
                                String source = snapshots.getMetadata().hasPendingWrites()
                                        ? "Local" : "Server";
                                Log.d(TAG, "TREASURE: THE SOURCE OF THIS CHANGE IS "+source);

                                String isCache = snapshots.getMetadata().isFromCache()?"true":"false";
                                Log.d(TAG, "TREASURE: The change is from cache?   " +isCache);

                                Treasure treasure=documentChange.getDocument().toObject(Treasure.class);


                                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                  //  Treasure treasure=documentChange.getDocument().toObject(Treasure.class);



                                    mtreasures.add(treasure);
                                    Log.d(TAG, "Adding the treasure with the message  " + treasure.getMessage());

                                    int height = 100;
                                    int width = 100;
                                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.cross);
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                    BitmapDescriptor crossimage = BitmapDescriptorFactory.fromBitmap(smallMarker);

                                    Marker marker= mGooglemap.addMarker(new MarkerOptions()
                                            .position(new LatLng(treasure.getGeoPoint().getLatitude(),treasure.getGeoPoint().getLongitude()))
                                            .title("Treasure")
                                            .icon(crossimage));
                                    marker.setTag(treasure);
                                    marker.setVisible(true);
                                    mTreasureMarkers.add(marker);


                                } else if(documentChange.getType() == DocumentChange.Type.REMOVED)
                                   {
                                    mtreasures.remove(treasure);
                                    for (Marker marker : mTreasureMarkers) {

                                        if (marker.getTag().equals(treasure)) {

                                            mTreasureMarkers.remove(marker);
                                            marker.remove();
                                            break;

                                        }
                                    }



                                   }


                            }
                        }
                        else{


                            Log.d(TAG, "onEvent: QUERY SNAPSHOT IS NULL");
                        }


                        Log.d(TAG, "onEvent: Array size of treasure is " + mtreasures.size());

                    }


                });

    }

   public boolean findingAlgo(){
       Log.d(TAG, "findingAlgo: is called");
        if(!mtreasures.isEmpty()) {

            for (Treasure treasure : mtreasures) {
                Log.d(TAG, "findingAlgo: DISTANCE BETWEEN THE TREASURE ID " + treasure.getTreasureId() + " is "+ distancebetween(mUserLocation.getGeoPoint(), treasure.getGeoPoint()));
                if ((distancebetween(mUserLocation.getGeoPoint(), treasure.getGeoPoint())<=2)
                        && !(treasure.getUser().getUserid().equals(mUserLocation.getUser().getUserid())) &&(!treasure.getTreasureId().equals("done"))) {
                    MessageDialog dialog = new MessageDialog(treasure);
                    dialog.show(getSupportFragmentManager(), "Message Dialog");
                    Log.d(TAG, "findingAlgo: THE MESSAGE IS " + treasure.getMessage());

                    for (Marker marker : mTreasureMarkers) {

                        if (marker.getTag().equals(treasure)) {
                           // mTreasureMarkers.remove(marker);
                            marker.setVisible(false);
                            break;

                        }
                    }

                    String treasureid=treasure.getTreasureId();
                    treasure.setTreasureId("done");

                    mDb.collection("Treasures").document(treasureid)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Treasure deleted! ");

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });


                }



            }


        }
        
        return true;

    }

    public float distancebetween(GeoPoint geoPointA,GeoPoint geoPointB){



        Location locationA = new Location("point A");

        locationA.setLatitude(geoPointA.getLatitude());
        locationA.setLongitude(geoPointA.getLongitude());

        Location locationB = new Location("point B");

        locationB.setLatitude(geoPointB.getLatitude());
        locationB.setLongitude(geoPointB.getLongitude());

        return locationA.distanceTo(locationB);

    }

    public void signOut(View view) {
        try {

            stopLocationUpdates();

            if(!mMarkers.isEmpty())
                setOfflineSnippet();

            Intent serviceIntent = new Intent(this, LocationService.class);
            stopService(serviceIntent);


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
            finish();
        }

    }


     // Map related methods

    @Override
    public  void onResume() {
        super.onResume();
        mMapView.onResume();
       // startUserLocationsRunnable();



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
        if(!mMarkers.isEmpty())
            setOfflineSnippet();
        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void setOfflineSnippet (){
         for( Marker marker: mMarkers)
             marker.setSnippet("OFFLINE");
    }




}
