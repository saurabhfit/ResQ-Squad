package com.example.resqsquad;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 123;

    FirebaseAuth mAuth;
    Button signoutBtn, serviceBtn;
    boolean switcher = true;

    Intent locationServiceIntent;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String USER_ID = "user_id";

    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    public String Uid = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uid = mAuth.getInstance().getCurrentUser().getUid();

        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }

        signoutBtn = (Button)findViewById(R.id.signout);
        serviceBtn = (Button)findViewById(R.id.start_service);

            locationServiceIntent = new Intent(getApplicationContext(), LocationService.class);

        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                stopService(locationServiceIntent);
                finish();
            }
        });

        serviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switcher == true){   //starting location service
                    serviceBtn.setText("Stop service");

                    sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_MULTI_PROCESS);
                    editor = sharedPreferences.edit();

                    Uid = mAuth.getInstance().getCurrentUser().getUid();

                    editor.putString(USER_ID, Uid);
                    editor.apply();

                    switcher = false;
                    startService(locationServiceIntent);
                }else{  //stopping location service
                    serviceBtn.setText("Start service");
                    stopService(locationServiceIntent);
                    switcher = true;
                }
            }
        });

    }
}
