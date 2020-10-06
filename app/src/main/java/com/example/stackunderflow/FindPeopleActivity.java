package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findPeopleList;
    private EditText searchUserText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        findPeopleList=findViewById(R.id.find_people_list);
        searchUserText=findViewById(R.id.search_user_text);
        findPeopleList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    public static class notificationViewHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        Button videoCallBtn;
        ImageView profileImage;
        RelativeLayout CardView1;


        public notificationViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameText=itemView.findViewById(R.id.name_notification);
            videoCallBtn=itemView.findViewById(R.id.request_accept_btn);
            CardView1=itemView.findViewById(R.id.card_view1);
            profileImage=itemView.findViewById(R.id.image_notification);

        }
    }
}