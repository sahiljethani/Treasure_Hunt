package com.example.treasurehunt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.treasurehunt.Models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


public class NewProfile extends AppCompatActivity {

    private static final String TAG = "NewProfile";

    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword, mUsername;
    String email;
    String username;
    String password;
    Button select_photo_bt;
    CircleImageView select_photo_view;
    Uri Imageuri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.emailId);
        mPassword = findViewById(R.id.Password);
        mUsername = findViewById(R.id.username);
        select_photo_bt = findViewById(R.id.select_photo_bt);
        select_photo_view= findViewById(R.id.select_photo_view);

    }

    public void select_photo(View view) {

        Log.d("NewProfile","Button selected");
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Photo"),1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1 && resultCode== Activity.RESULT_OK && data!=null) {
            Log.d("NewProfile","Photo was selected");
            Imageuri=data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Imageuri);
                select_photo_view.setImageBitmap(bitmap);
                select_photo_bt.setAlpha(0f);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

public void uploadImage() {
        if(Imageuri==null) return;
        String filename= UUID.randomUUID().toString();

    final StorageReference ref= FirebaseStorage.getInstance().getReference("/images/"+filename);
     ref.putFile(Imageuri)
             .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                 @Override
                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                     Log.d(TAG,"Image Uploaded: "+taskSnapshot.getMetadata().getPath());

                     ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {
                             Log.d(TAG,"Image url is :" +uri);
                             save(email,password,uri.toString());


                         }
                     });

                 }
             }).addOnFailureListener(new OnFailureListener() {
         @Override
         public void onFailure(@NonNull Exception e) {
             Log.d(TAG,"Failed to upload Image"+e.getMessage());

         }
     });

    }


    public void createAccount(View view) {

        email = mEmail.getText().toString();
        password = mPassword.getText().toString();
        username = mUsername.getText().toString();



        if (!validateForm(email, password, username)) {
            return;
        }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                uploadImage();
                              //  save(email,username);


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(NewProfile.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: I AM HERE");
                }
            });


        // [END create_user_with_email]
    }


    public void save(String email,String username, String profileImageUrl) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        DocumentReference newUser = db.collection("Users").document(userid);


        Log.d(TAG, "save:  "+ userid);


        Users user = new Users();
        user.setEmailid(email);
        user.setUserid(userid);
        user.setUsername(username);
        user.setProfileImageUrl(profileImageUrl);

        newUser.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: User added");
                } else
                    Log.d(TAG, "onComplete: Not able to add user");


            }
        });


        Log.d(TAG, " Account and data are saved");

        ((UserClient)(getApplicationContext())).setUser(user);


        Log.d(TAG, "save: USER CLIENT "+user.getUsername());




        Intent intent = new Intent(NewProfile.this, Map.class);
        startActivity(intent);


    }





    private boolean validateForm(String email, String password, String username) {
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


        if (TextUtils.isEmpty(username)) {
            mUsername.setError("Required.");
            valid = false;
        } else {
            mUsername.setError(null);
        }

        return valid;
    }


}

