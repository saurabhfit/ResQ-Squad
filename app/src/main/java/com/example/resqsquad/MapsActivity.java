package com.example.resqsquad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    Intent locationServiceIntent;

    private Button logout;

    private String customerId = "";
    private String userId = "";

    TextView customerName, customerAge, customerBlood, customerAddress, customerLocation, customerRelative1, customerRelative2, customerRelative3;

    LatLng customerLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        customerName = (TextView)findViewById(R.id.customerName);
        customerAge = (TextView)findViewById(R.id.customerAge);
        customerBlood = (TextView)findViewById(R.id.customerBloodGroup);
        customerAddress = (TextView)findViewById(R.id.customerAddress);
        customerLocation = (TextView)findViewById(R.id.customerLocationCoordinates);
        customerRelative1 = (TextView)findViewById(R.id.customerRelative1);
        customerRelative2 = (TextView)findViewById(R.id.customerRelative2);
        customerRelative3 = (TextView)findViewById(R.id.customerRelative3);


        locationServiceIntent = new Intent(getApplicationContext(), LocationService.class);

        logout = (Button)findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                stopService(locationServiceIntent);
                startActivity(new Intent(MapsActivity.this, LoginActivity.class));
                finish();
                return;
            }
        });

        customerId = getIntent().getStringExtra("customerRideId");

        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 4123);
        }

        getAssignedCustomerLocation();
    }

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;

    private void getAssignedCustomerLocation(){

        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("request").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0)!=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    customerLatLng = new LatLng(locationLat, locationLng);
//                    Toast.makeText(MapsActivity.this, "accident of "+customerId, Toast.LENGTH_SHORT).show();
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(customerLatLng).title("accident location")
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLatLng, 15.0f));
                    fetchDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchDetails(){
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        customerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        customerName.setText("Name : "+map.get("name").toString());
                    }
                    if(map.get("age")!=null){
                        customerAge.setText("Age : "+map.get("age").toString());
                    }
                    if(map.get("bloodGrp")!=null){
                        customerBlood.setText("Blood Group : "+map.get("bloodGrp").toString());
                    }
                    if(map.get("address")!=null){
                        customerAddress.setText("Address : "+map.get("address").toString());
                    }
                    if(map.get("relative1")!=null){
                        customerRelative1.setText("Relative 1 : "+map.get("relative1").toString());
                    }
                    if(map.get("relative2")!=null){
                        customerRelative2.setText("Relative 2 : "+map.get("relative2").toString());
                    }
                    if(map.get("relative3")!=null){
                        customerRelative3.setText("Relative 3 : "+map.get("relative3").toString());
                    }
                    customerRelative3.setText("Location : Lat:"+customerLatLng.latitude+" Lng:"+customerLatLng.longitude);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

}
