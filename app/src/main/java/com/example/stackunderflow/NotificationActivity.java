package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    private  RecyclerView NotificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        //test

        NotificationList=findViewById(R.id.notification_list);
        NotificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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