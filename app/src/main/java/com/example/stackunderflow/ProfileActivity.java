package com.example.stackunderflow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    String receiverUserID="" , receiverUserImage="" ,receiverUserName="";
    private ImageView background_profile_view;
    private TextView name_profile;
    private Button add_friend;
    private Button decline_friend_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();  //visit_user_id reference is accessed in receiverUserID
        receiverUserImage = getIntent().getExtras().get("profile_image").toString(); //access image of the reference
        receiverUserName = getIntent().getExtras().get("profile_name").toString();  //access name of the reference

        background_profile_view=findViewById(R.id.background_profile_view);
        name_profile=findViewById(R.id.name_profile);
        add_friend=findViewById(R.id.add_friend);
        decline_friend_request=findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(background_profile_view);  //the image is being set as the receiveruserimage
        name_profile.setText(receiverUserName);  //name_profile textbox text is being set to the username
    }
}