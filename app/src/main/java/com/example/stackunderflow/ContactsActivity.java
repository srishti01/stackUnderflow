package com.example.stackunderflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;

    RecyclerView myContactsList;     //to show contact list when find_people button is clicked
    ImageView findPeopleBtn;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String userName="", profileImage="";

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
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {            //to show contact list when find_people button is clicked
            @Override
            public void onClick(View view) {
                Intent findPeople = new Intent(ContactsActivity.this,FindPeopleActivity.class);
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
                    Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainIntent);
                    break;
                }
                case R.id.navigation_dashboard: {
                    //Setting Intent to show what would happen when notification icon is pressed
                    Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
                }
                case R.id.navigation_notifications: {
                    Intent notificationIntent = new Intent(ContactsActivity.this, NotificationActivity.class);
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

                    Intent logoutIntent = new Intent(ContactsActivity.this,RegistrationActivity.class);
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

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserID), Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts , ContactsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int i, @NonNull Contacts model) {
                final String listUserID=getRef(i).getKey();
                usersRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            userName = snapshot.child("name").getValue().toString();
                            profileImage = snapshot.child("image").getValue().toString();

                            holder.userNameText.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

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
}

