package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CallingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, makeCallBtn;

    private String recieverUserId="", recieverUserImage="", recieverUserName="";
    private String senderUserId="", senderUserImage="", senderUserName="";
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        recieverUserId = getIntent().getExtras().get("visit_user_id").toString();//we now have the userID from the last activity, now we can get the name and dp
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameContact = (TextView) findViewById(R.id.name_contact);
        profileImage = (ImageView) findViewById(R.id.profile_image_calling);
        cancelCallBtn = (ImageView) findViewById(R.id.cancel_call);
        makeCallBtn = (ImageView) findViewById(R.id.make_call);

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//DataSnapshot represent our users node so we can use it to retrieve info from the dataase
                if(snapshot.child(recieverUserId).exists()){
                    recieverUserImage = snapshot.child(recieverUserId).child("image").getValue().toString();
                    recieverUserName = snapshot.child(recieverUserId).child("Name").getValue().toString();
                    //NOW we have retrieved the Name and profile image from the database using snapshot

                    //NEXT we will have to show this data to user in the CallingActivity
                    nameContact.setText(recieverUserName);
                    Picasso.get().load(recieverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                    //Place holder here will be used to display the default profile image, until the user profile image is retrieved,i.e, if it takes time to load
                }
                if(snapshot.child(senderUserId).exists()){
                    senderUserImage = snapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName = snapshot.child(senderUserId).child("Name").getValue().toString();//we've stored the sender userID and name
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}