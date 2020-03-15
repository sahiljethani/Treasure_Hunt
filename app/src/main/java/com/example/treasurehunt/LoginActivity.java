package com.example.treasurehunt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.treasurehunt.Models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: CREATED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.emailId);
       mPassword = findViewById(R.id.Password);

    }
    @Override
    public void onStart() {
        Log.d(TAG, "onStart: I am here");
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }



    public void createAccount(View view) {
        Log.d(TAG, "createAccount: I AM HERE");
        Intent intent = new Intent(LoginActivity.this, NewProfile.class);
        startActivity(intent);


    }

    public void signIn(View view) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        Log.d(TAG, "signIn:" + email);
        if (!validateForm(email,password)) {
            return;
        }


        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
        // [END sign_in_with_email]
    }

    public void signOut(View view) {
        mAuth.signOut();
        updateUI(null);
    }




    private boolean validateForm(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }


        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {

        Log.d(TAG, "updateUI: I am here");

           if ( user!=null) {

               FirebaseFirestore db = FirebaseFirestore.getInstance();
               String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();


               DocumentReference newUser = db.collection("Users").document(userid);


               newUser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()) {

                           if(task.getResult()!=null) {


                                   Users user=task.getResult().toObject(Users.class);

                                     Log.d(TAG, "onComplete: User is added"+user.getUsername());
                                     ((UserClient)(getApplicationContext())).setUser(user);



                               }


                           Intent intent = new Intent(LoginActivity.this, Map.class);
                           startActivity(intent);


                       }
                   }
               });



           }



    }


}
