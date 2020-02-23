package com.example.resqsquad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText mEmail, mPassword, mName, mCarNumberPlate, mCarModel, mPhno;
    Button registerBtn;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mEmail = (EditText)findViewById(R.id.email);
        mPassword = (EditText)findViewById(R.id.password);
        mName = (EditText)findViewById(R.id.name);
        mPhno = (EditText)findViewById(R.id.phno);
        mCarNumberPlate = (EditText)findViewById(R.id.numberPlate);
        mCarModel = (EditText)findViewById(R.id.carModel);
        registerBtn = (Button)findViewById(R.id.registerBtn);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                }
            }
        };

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Sign up error", Toast.LENGTH_SHORT).show();
                        }else{
                            String user_id = mAuth.getCurrentUser().getUid();
                            String name = mName.getText().toString();
                            String phno = mPhno.getText().toString();
                            String carNumberPlate = mCarNumberPlate.getText().toString();
                            String carModel = mCarModel.getText().toString();

                            DriverDetails driver = new DriverDetails(name, phno, carNumberPlate, carModel);
                            Map<String, Object> map = new HashMap<>();
                            map.put(user_id, driver);

                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference();
                            current_user_db.child("Users").child("Drivers").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUpActivity.this, "User registered", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
