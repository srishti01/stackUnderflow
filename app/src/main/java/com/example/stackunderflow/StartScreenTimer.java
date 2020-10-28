package com.example.stackunderflow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class StartScreenTimer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen_timer);

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                Intent intent = new Intent(StartScreenTimer.this,RegistrationActivity.class);
                startActivity(intent);
                finish();
            }
        };
        Timer t = new Timer();
        t.schedule(task, 2500);

    }
}