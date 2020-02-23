package com.example.resqsquad;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String USER_ID = "user_id";
    public SharedPreferences sharedPreferences;

    private static LocationService instance = null;

    private String user_id = "";

    NotificationCompat.Builder mBuilder;
    NotificationManagerCompat notificationManager;
    private static final int NOTIFICATION_ID = 321;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("ResQ Squad server connection active...")
                .setOngoing(true)
                .setContentText("you are online")
                .setPriority(NotificationCompat.PRIORITY_HIGH);;

        if(instance==null){
            buildGoogleApiClient();
            instance = this;
        }

        getAssignedCustomer();
    }

    private String customerId = "";
    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println("I m the Driver :"+driverId);
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    customerId = dataSnapshot.getValue().toString();
                    Toast.makeText(LocationService.this, "Medical Assistance required for "+customerId, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LocationService.this, MapsActivity.class);
                    i.putExtra("customerRideId", customerId);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);

                }else{
                    customerId = "";
                    
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
        user_id = sharedPreferences.getString(USER_ID, "HAHAHA");

        System.out.println("SERVICE has user_id " +user_id);


        notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

//        Log.i(LOGSERVICE, "onStartCommand "+user_id);

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
//        Log.i(LOGSERVICE, "onConnected" + bundle);

        Location l = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (l != null) {
//            Log.i(LOGSERVICE, "lat " + l.getLatitude());
//            Log.i(LOGSERVICE, "lng " + l.getLongitude());

        }

        startLocationUpdate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {
        return;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.i(LOGSERVICE, "lat " + location.getLatitude());
//        Log.i(LOGSERVICE, "lng " + location.getLongitude());
        LatLng mLocation = (new LatLng(location.getLatitude(), location.getLongitude()));

        System.out.println("+++++++++++++++++++++++++++++Location lat:"+location.getLatitude()+" lng:"+location.getLongitude());
        FirebaseApp.initializeApp(this);

        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
        DatabaseReference refWorking= FirebaseDatabase.getInstance().getReference("driversWorking");

        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireWorking = new GeoFire(refWorking);

        System.out.println("Customer ID ______________"+customerId);

        switch(customerId){
            case "":
                geoFireWorking.removeLocation(user_id, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        System.out.println("000000000000000000000");
                    }
                });

                geoFireAvailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        System.out.println("1111111111111111111");
                    }
                });
                break;

            default:
                geoFireAvailable.removeLocation(user_id, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        System.out.println("333333333333333333333");
                    }
                });

                geoFireWorking.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        System.out.println("222222222222222222222");
                    }
                });
                break;
        }

//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener(){
//            @Override
//            public void onComplete(String key, DatabaseError error) {
//                //location update on server
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.i(LOGSERVICE, "onDestroy ");
        stopLocationUpdate();
        instance = null;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //removed
            }
        });

        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startLocationUpdate() {
        initLocationRequest();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

    }
}
