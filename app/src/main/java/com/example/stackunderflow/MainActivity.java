package com.example.stackunderflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView navView;

    RecyclerView contactList;     //to show contact list when find_people button is clicked
    ImageView findPeopleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        contactList=findViewById(R.id.contact_list);
        findPeopleBtn=findViewById(R.id.find_people_btn);
        contactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null){
            Intent homeIntent = new Intent(MainActivity.this,RegistrationActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }
}

