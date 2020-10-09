package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity {

    private  RecyclerView NotificationList;
    private DatabaseReference friendRequestRef, contactsRef,usersRef;

    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        //test


        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");   //friend request ref stores the reference to friend requests
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");   //contacts is storing the reference to already added friends
        usersRef=FirebaseDatabase.getInstance().getReference().child("User");   //users stores reference to users in the database


        NotificationList=findViewById(R.id.notification_list);
        NotificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


    }

    @Override
    protected void onStart() {
        super.onStart();



      FirebaseRecyclerOptions  options = new FirebaseRecyclerOptions.Builder<Contacts>()     //to show list of request received
                .setQuery(friendRequestRef.child(currentUserID), Contacts.class)             // by the user.
                .build();          //in database we will go to friendRequest child and show requests (received and sent) of user with given userid


        FirebaseRecyclerAdapter<Contacts,notificationViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Contacts, notificationViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull notificationViewHolder holder, int i, @NonNull Contacts model)
            {

                holder.acceptBtn.setVisibility(View.VISIBLE);  // in the notification activity we need to display accept and cancel buttons
                holder.cancelBtn.setVisibility(View.VISIBLE);

               final String listUserID=getRef(i).getKey();     //we get the  user id of people in the notifications list
                                                              // for reference as every item has its own unique id

                              //reference to the database to get the type of request(sent or received)
                DatabaseReference requestTypeRef=getRef(i).child("request_type").getRef();

                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        if(snapshot.exists())   // we check if there is any request then we get its type(sent or received)
                        {
                            String type=snapshot.getValue().toString();

                            if(type.equals("received"))   //if the request type if received we make cardview visible
                            {

                                holder.CardView.setVisibility(View.VISIBLE);

                                usersRef.child(listUserID).addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot)
                                    {

                                        if(snapshot.hasChild("image")) //if the profile contains image we need to display it
                                        {
                                            final String imageStr=snapshot.child("image").getValue().toString();

                                            Picasso.get().load(imageStr).into(holder.profileImage);

                                        }

                                            final String nameStr=snapshot.child("name").getValue().toString();  //tp display username
                                            holder.userNameText.setText(nameStr);


                                            //when the user click on the accept button
                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                contactsRef.child(currentUserID).child(listUserID).child("Contact").setValue("saved") //the status of the user is being updates to saved
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful())
                                                                {
                                                                    contactsRef.child(listUserID).child(currentUserID).child("Contact").setValue("saved")  //saved to contacts for both the user
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        friendRequestRef.child(currentUserID).child(listUserID).removeValue()      //removing the value receiverid from frndrequests section as the sender
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(NotificationActivity.this,"Contact saved.",Toast.LENGTH_SHORT).show();
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

                                        });

                                        //when user click on the cancel button
                                        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                        friendRequestRef.child(currentUserID).child(listUserID).removeValue()      //removing the vaalue receiverid from frndrequests section os the sender
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            Toast.makeText(NotificationActivity.this,"Friend request cancelled.",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                    }



                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else
                                {
                                    holder.CardView.setVisibility(View.GONE);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });


            }

            @NonNull
            @Override
            public notificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)  //set view acc to find_people_design
            {
               View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_people_design,parent,false);
               notificationViewHolder viewHolder=new notificationViewHolder(view);
               return viewHolder;
            }
        };
        NotificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class notificationViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        Button acceptBtn,cancelBtn;
        ImageView profileImage;
        RelativeLayout CardView;


        public notificationViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText=itemView.findViewById(R.id.name_notification);
            acceptBtn=itemView.findViewById(R.id.request_accept_btn);
            cancelBtn=itemView.findViewById(R.id.request_cancel_btn);
            CardView=itemView.findViewById(R.id.card_view);
            profileImage=itemView.findViewById(R.id.image_notification);

        }
    }


}