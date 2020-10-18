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

        msgProfileImage=findViewById(R.id.msg_profile_image);
        msgUsername=findViewById(R.id.msg_username);
        intent= getIntent();

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        String userID=intent.getStringExtra("userid");

        messagesRef= FirebaseDatabase.getInstance().getReference("Messages");

        userRef=FirebaseDatabase.getInstance().getReference().child("User");

        final String listUserID=userRef.getKey();

        userRef.child(listUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    username = snapshot.child("Name").getValue().toString();
                    profileImage = snapshot.child("image").getValue().toString();

                    msgUsername.setText(username);
                    Picasso.get().load(profileImage).into(msgProfileImage);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}