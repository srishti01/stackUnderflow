package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private ImageButton btn_send;
    private EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        final Vibrator vibrator = (Vibrator) MessageActivity.this.getSystemService(Context.VIBRATOR_SERVICE);//initializing vibrator

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

        recyclerView = findViewById(R.id.recycler_view_msg);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());  //to set the layout for chat between users
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        fuser=FirebaseAuth.getInstance().getCurrentUser();


        msgProfileImage=findViewById(R.id.msg_profile_image);
        msgUsername=findViewById(R.id.msg_username);
        btn_send = findViewById(R.id.btn_send_msg);
        text_send = findViewById(R.id.text_send_msg);
        intent= getIntent();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrator.vibrate(100); //vibration on hitting send button

                String msg = text_send.getText().toString();    //here we've stored the msg typed
                if(!msg.isEmpty()){
                    sendMessage(senderUserId,recieverUserId,msg);   //calling sendMessage method if the string is not empty
                } else {
                    Toast.makeText(MessageActivity.this,"Can't send Empty Message",Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");      //TODO: its important to empty the edit text after the msg has been sent
            }
        });


        recieverUserId = getIntent().getExtras().get("visit_user_id").toString();//we now have the userID of the receiver from the last activity, now we can get the name and dp
        userRef = FirebaseDatabase.getInstance().getReference().child("User");

        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();   //we get the userid of the sender
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

                    Contacts user=snapshot.getValue(Contacts.class);   //get snapshot as Contacts class(we defined)

                    readMesagges(senderUserId, recieverUserId,user.getImage());   //call the method to read msg from database using receiverid and senderid
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);

    }

    private void readMesagges(String myid, String userid, String imageurl)
    {
        mchat = new ArrayList<>();

        userRef = FirebaseDatabase.getInstance().getReference("Chats");  //refernce to chats child of database
        userRef.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mchat.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren())    //run loop to show all the msgs
                {
                    Chat chat = snapshot1.getValue(Chat.class);
                    if((chat.getReceiver().equals(myid) && chat.getSender().equals(userid)) || (chat.getReceiver().equals(userid)&&
                    chat.getSender().equals(myid)))
                    {
                        mchat.add(chat);
                    }

                    //set adapter for display
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}