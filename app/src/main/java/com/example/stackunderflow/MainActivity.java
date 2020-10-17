package com.example.stackunderflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.google.firebase.auth.FirebaseUser;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;

    RecyclerView myContactsList;     //to show contact list when find_people button is clicked
    ImageView findPeopleBtn;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String userName="", profileImage="";

    private String calledBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mAuth =FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        myContactsList=findViewById(R.id.contact_list);
        findPeopleBtn=findViewById(R.id.find_people_btn);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersRef=FirebaseDatabase.getInstance().getReference().child("User");

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {            //to show contact list when find_people button is clicked
            @Override
            public void onClick(View view) {
                Intent findPeople = new Intent(MainActivity.this,FindPeopleActivity.class);
                startActivity(findPeople);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    //As we are originally in the main activity so the Intent will just Refresh the mainActivity
                    Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    break;
                }
                case R.id.navigation_dashboard: {
                    //Setting Intent to show what would happen when notification icon is pressed
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                }
                case R.id.navigation_notifications: {
                    Intent notificationIntent = new Intent(MainActivity.this, NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;
                }
//              case R.id.navigation_logout:{
//                    Intent logoutIntent = new Intent(MainActivity.this,RegistrationActivity.class);//Registration activity will be given by ss
//                    startActivity(logoutIntent);
//                    finish();
//                    break;
//              }
                case R.id.navigation_logout:{
                    FirebaseAuth.getInstance().signOut();

                    Intent logoutIntent = new Intent(MainActivity.this,RegistrationActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
                }
            }
            return true;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();
        
        validateUser();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts , ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int i, @NonNull Contacts model)
            {
                final String listUserID=getRef(i).getKey();

                usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            userName = snapshot.child("Name").getValue().toString();
                            profileImage = snapshot.child("image").getValue().toString();

                            holder.userNameText.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);

                        }
                        holder.callBtn.setOnClickListener(new View.OnClickListener() {//when user clicks on the video call button
                            @Override
                            public void onClick(View v) {
                                Intent callingIntent = new Intent(MainActivity.this,CallingActivity.class);
                                callingIntent.putExtra("visit_user_id",listUserID); //here we are sending the listUserID to the Callingactivity
                                startActivity(callingIntent);                               //so that we will see who we have called
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    private void validateUser() {
        //Here we are going to check if user has dp,name and bio or not
        //We will create a reference and check if the prfile pic and name exist
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("User").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class); //If the user does not exist
                                                                                                        //he/she will not be able to go to mainActivity
                    startActivity(settingsIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        Button callBtn;
        ImageView profileImageView;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText=itemView.findViewById(R.id.name_contact);
            callBtn=itemView.findViewById(R.id.call_btn);
            profileImageView=itemView.findViewById(R.id.image_contact);


        }
    }

    private void checkForReceivingCall() {
        usersRef.child(currentUserID)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild("ringing")){//"ringing" was the senderID which the receiver receives so we need this to show receiver the name of sender
                            calledBy = snapshot.child("ringing").getValue().toString();

                            Intent callingIntent = new Intent(MainActivity.this,CallingActivity.class);
                            callingIntent.putExtra("visit_user_id",calledBy);
                            startActivity(callingIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


}

