package com.example.stackunderflow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    String receiverUserID="" , receiverUserImage="" ,receiverUserName="";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend;
    private Button decline_friend_request;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState="new";
    private DatabaseReference friendRequestRef, contactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();  //senderUserId has stored the id of current user through mAuth
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");   //friend request ref stores the reference to friend requests
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");   //contacts is storing the reference to already added friends

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();  //visit_user_id reference is accessed in receiverUserID
        receiverUserImage = getIntent().getExtras().get("profile_image").toString(); //access image of the reference
        receiverUserName = getIntent().getExtras().get("profile_name").toString();  //access name of the reference

        background_profile_view=findViewById(R.id.background_profile_view);
        name_profile=findViewById(R.id.name_profile);
        add_friend=findViewById(R.id.add_friend);
        decline_friend_request=findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);  //the image is being set as the receiveruserimage
        name_profile.setText(receiverUserName);  //name_profile textbox text is being set to the username

        manageClickEvents() ;     //after the results of search are displayed and a profile has been clicked upon,this method is called
    }

    private void manageClickEvents() {

        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserID))   //checking if recieveruserid exists as a field
                {
                    String requestType= snapshot.child(receiverUserID).child("request_type").getValue().toString();  //requestType stores thr request_type field of recieveruserid

                    if(requestType.equals("sent"))
                    {
                        currentState = "request_sent";      //it is setting currentState to sent so that it can be checked further
                        add_friend.setText("Cancel Friend Request");   //button changed to cancel frnd request for the UI which is shown always
                        //point to remember is that below also we are changing the button names but that changes are seen when the specific method is called but
                        //all the time when search is performed then also it should maintain the status of the buttons
                    }
                    else if(requestType.equals("received"))
                    {
                        currentState = "request_received";
                        add_friend.setText("Accept Friend Request");
                        decline_friend_request.setVisibility(View.VISIBLE);
                        decline_friend_request.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                    else
                    {
                        contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.hasChild(receiverUserID))
                                {
                                    currentState = "friends";    //senderuserid's database already contains recieverid then currentstate is friends
                                    add_friend.setText("Delete Contact");
                                }
                                else
                                {
                                    currentState = "new";
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(senderUserId.equals(receiverUserID))     //checking if the user clicked upon is the current user itself
        {
            add_friend.setVisibility(View.GONE);     //add friend button becomes invisible
        }
        else
        {
            add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentState.equals("new"))
                    {
                        SendFriendRequest();       //only when currentstate is new ie no frnd request has been sent or recieved before,
                    }                             //then this method invokes
                    if(currentState.equals("request_Sent"))
                    {
                        CancelFriendRequest();  //request is already sent this method deletes the frnd request current user has sent
                    }
                    if(currentState.equals("request_received"))
                    {
                        AcceptFriendRequest();   //currentState is telling that the user has already sent rqst to current user
                    }
                    if(currentState.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                }
            });
        }
    }

    private void AcceptFriendRequest() {
        contactsRef.child(senderUserId).child(receiverUserID).child("Contact").setValue("saved") //the status of the user is being updates to saved
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            contactsRef.child(receiverUserID).child(senderUserId).child("Contact").setValue("saved")  //saved to contacts for both the user
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                friendRequestRef.child(senderUserId).child(receiverUserID).removeValue()      //removing the vaalue receiverid from frndrequests section as the sender
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    friendRequestRef.child(receiverUserID).child(senderUserId).removeValue();   //remove the frnd rqst for the reciever as well
                                                                    add_friend.setText("Delete Contact");    //after deletion the user can again send frnd request
                                                                    currentState="friends";             //currentstate is set new for next time
                                                                    decline_friend_request.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserID).removeValue()      //removing the vaalue receiverid from frndrequests section os the sender
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestRef.child(receiverUserID).child(senderUserId).removeValue();   //remove the frnd rqst for the reciever as well
                            add_friend.setText("Add Friend");    //after deletion the user can again send frnd request
                            currentState="new";             //currentstate is set new for next time
                        }
                    }
                });
    }

    private void SendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserID).child("request_type").setValue("sent")   //request type of the user is set to sent for the current user
                                                                                                                //request type is sent
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    currentState = "request_sent";   //currentState stores the state request sent as a string bcoz the send frnd rqst has been sent
                    add_friend.setText("Cancel Friend Request"); //the moment frnd request is sent the button changes to cancel it
                    Toast.makeText(ProfileActivity.this,"Friend Request has been sent",Toast.LENGTH_SHORT).show();
                    friendRequestRef.child(receiverUserID).child(senderUserId).child("request_type").setValue("received");
                }
            }
        });

    }
}