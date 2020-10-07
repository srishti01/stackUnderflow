package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText mUsername;
    private EditText mBio;
    private ImageView mProfileImage;

    private static int galleryPick = 1;
    private Uri imageUri;

    private StorageReference userProfileImgRef;
    private String downloadUrl;
    private DatabaseReference userRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        saveBtn = findViewById(R.id.save_button);
        mProfileImage = findViewById(R.id.settings_profile_image);
        mUsername = findViewById(R.id.username_settings);
        mBio = findViewById(R.id.bio_settings);
        progressDialog = new ProgressDialog(this);

        //now We will make and intent so that user can set DP by going to the gallery
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);//this action is what will open the gallery
                galleryIntent.setType("image/*");//here we are mentioning that we want to see only images(/* for all images)
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

        //Pressing Save button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData(); //image data gets stored in the imageUri, now we have to display the image
            mProfileImage.setImageURI(imageUri);//here we converted Uri data to Image View and now the image will be visible
        } else {
        }
    }

    //this function will be responsible for saving the dp,name,bio data
    private void saveUserData() {
        final String getUserName = mUsername.getText().toString();
        final String getUserBio = mBio.getText().toString();

        if (imageUri == null) {       //if user wants no dp

            //Let's check if the user had set a dp earlier and doesn't want one now
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image"))
                    //Here we are checking if the user already has previous dp,i.e, the "Image" child which we sent from here exist(hasChild)
                    //and just wants to change name and bio
                    {
                        saveInfoOnly();//saving only the new Name or bio and retaining the old dp
                    } else {
                        //DP is Mandatory
                        Toast.makeText(SettingsActivity.this, "Please Select Profile Image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (getUserName.isEmpty()) {    //if user lefts the name BLANK
            Toast.makeText(SettingsActivity.this, "userName is mandatory", Toast.LENGTH_SHORT).show();
        } else if (getUserBio.isEmpty()) {      //if user lefts the bio BLANK
            Toast.makeText(SettingsActivity.this, "Bio is mandatory", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Profile");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();

            //Here we have to save the user DP,name,bio in the firebase database
            final StorageReference filePath = userProfileImgRef.
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);

            //we have stored the image in the FirebaseStorage
            //Now we have to show the image in the RealTime Database
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    //Now we have to get the Url of the image we put inside the Firebase storage
                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();//we had to return task<Uri>
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        downloadUrl = task.getResult().toString();

                        //As We can put any kind of object into the RealTime Database, we will create a hash map
                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("Name", getUserName);
                        profileMap.put("Bio", getUserBio);
                        profileMap.put("image", downloadUrl);

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //Now if the setting up of name,dp,bio was successful
                                    //we will send the user to the homepage
                                Intent intent = new Intent(SettingsActivity.this, Contacts.class);
                                    startActivity(intent);
                                    finish();
                                    progressDialog.dismiss();

                                    Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(null, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoOnly() {//when user only changes name or bio and doesn't change the old dp
        final String getUserName = mUsername.getText().toString();
        final String getUserBio = mBio.getText().toString();



            if (getUserName.isEmpty()) {    //if user lefts the name BLANK
            Toast.makeText(SettingsActivity.this, "userName is mandatory", Toast.LENGTH_SHORT).show();
        } else if (getUserBio.isEmpty()) {      //if user lefts the bio BLANK
            Toast.makeText(SettingsActivity.this, "Bio is mandatory", Toast.LENGTH_SHORT).show();
        } else {

            progressDialog.setTitle("Profile");
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();

            //As We can put any kind of object into the RealTime Database, we will create a hash map
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("Name", getUserName);
            profileMap.put("Bio", getUserBio);
            //NOTE that we removed the "image" from hash map as we don't need to update the dp

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Now if the setting up of name,dp,bio was successful
                        //we will send the user to the homepage
                        Intent intent = new Intent(SettingsActivity.this, Contacts.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(null, "Something went wrong", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void retrieveUserInfo(){//Here we will show the old dp,name,bio when user visits their profile
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String imageFromDb = snapshot.child("image").toString();
                            String nameFromDb = snapshot.child("Name").toString();
                            String bioFromDb = snapshot.child("Bio").toString(); //Here we have taken the name already present int the database

                            mUsername.setText(nameFromDb);;
                            mBio.setText(bioFromDb);
                            Picasso.get().load(imageFromDb).placeholder(R.drawable.profile_image).into(mProfileImage);
                                  //getting the imageUrl           //The place to put image       //Object of the place
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}