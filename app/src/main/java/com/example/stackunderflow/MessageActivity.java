package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageActivity extends AppCompatActivity {

    private CircleImageView msgProfileImage;
    private TextView msgUsername;
    private FirebaseUser fuser;
    private DatabaseReference messagesRef,userRef;

    Intent intent;
    private FirebaseAuth mAuth;
    private String currentUserID;

    String username,profileImage;
    private String senderUserId="",recieverUserId="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fuser=FirebaseAuth.getInstance().getCurrentUser();


        msgProfileImage=findViewById(R.id.msg_profile_image);
        msgUsername=findViewById(R.id.msg_username);
        intent= getIntent();


        recieverUserId = getIntent().getExtras().get("visit_user_id").toString();//we now have the userID from the last activity, now we can get the name and dp
        userRef = FirebaseDatabase.getInstance().getReference().child("User");
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messagesRef= FirebaseDatabase.getInstance().getReference("Chats");  //we create new child for Chats



        final String listUserID=userRef.getKey();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profileImage = snapshot.child(recieverUserId).child("image").getValue().toString();
                    username = snapshot.child(recieverUserId).child("Name").getValue().toString();
                    //NOW we have retrieved the Name and profile image from the database using snapshot

                    //NEXT we will have to show this data to user in the MessageActivity
                    msgUsername.setText(username);
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(msgProfileImage);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}