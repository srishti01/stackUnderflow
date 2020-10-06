package com.example.stackunderflow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText mUsername;
    private  EditText mBio;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveBtn=findViewById(R.id.save_button);
        mProfileImage=findViewById(R.id.settings_profile_image);
        mUsername=findViewById(R.id.username_settings);
        mBio=findViewById(R.id.bio_settings);



    }
}