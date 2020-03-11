package com.example.treasurehunt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signout extends AppCompatActivity {
    private static final String TAG = "signout";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signout);
        mAuth = FirebaseAuth.getInstance();

    }

    public void signOut(View view) {
        Log.d(TAG, "signOut: i am here");
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
            Intent intent = new Intent(signout.this, LoginActivity.class);
            startActivity(intent);
        }

    }
}
