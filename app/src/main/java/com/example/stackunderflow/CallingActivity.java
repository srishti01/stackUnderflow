package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn, acceptCallBtn;

    private String recieverUserId="", recieverUserImage="", recieverUserName="",checker="";
    private String senderUserId="", senderUserImage="", senderUserName="";
    private DatabaseReference usersRef;
    private String callingID="",ringingID="";



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
        acceptCallBtn = (ImageView) findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checker="clicked";  //value to check if the  button is clicked
                //when user click on cancel button we need to remove calling nad ringing child in the database of the user node

                cancelCallingUser();
            }
        });

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

    @Override
    protected void onStart() {
        super.onStart();

        usersRef.child(recieverUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {//SingleValueEvent = only one time
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!checker.equals("clicked") && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing"))//THis line ensure that user is not busy on another call
                        {
                            final HashMap<String, Object> callingInfo = new HashMap<>();
                            callingInfo.put("calling",recieverUserId); //uid(senderUserId will be make call to calling(receiverUserId)

                            usersRef.child(senderUserId).child("Calling")
                                    .updateChildren(callingInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                final HashMap<String, Object> ringingInfo = new HashMap<>();
                                                ringingInfo.put("ringing",senderUserId);//the receiver wil need sender id to see who's calling

                                                usersRef.child(recieverUserId).child("Ringing")
                                                        .updateChildren(ringingInfo);
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child(senderUserId).hasChild("Ringing") && !snapshot.child(senderUserId).hasChild("Calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void  cancelCallingUser()
    {
        //to remove calling nad ringing child in the database, of the user node

        //from the sender side
        usersRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists() && snapshot.hasChild("calling")) //if the sender cancel firsts
                {
                    callingID=snapshot.child("calling").getValue().toString();
                    usersRef.child(callingID).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                usersRef.child(senderUserId).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        //when we remove it we send user to home
                                        startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
                else //if the receiver cancels first we just send user to home
                {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //from the receiver side

        usersRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists() && snapshot.hasChild("ringing")) //if the sender cancel firsts
                {
                    ringingID=snapshot.child("ringing").getValue().toString();  //we get the ringingID

                    usersRef.child(ringingID).child("Calling").removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                usersRef.child(senderUserId).child("Ringing").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        //when we remove it we send user to home
                                        startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
                else //if the receiver cancels first we just send user to home
                {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    }