package com.example.stackunderflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import com.opentok.android.Session;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.opentok.android.PublisherKit.*;

public class VideoChatActivity extends AppCompatActivity implements com.opentok.android.Session.SessionListener,
        PublisherListener {

    private static String API_key="46950994",SESSION_ID="2_MX40Njk1MDk5NH5-MTYwMjQ4OTM1MDc5MH4ybkFkT2pGNllQdmtjYzhvN3ZGKzdYdnV-fg",
            TOKEN="T1==cGFydG5lcl9pZD00Njk1MDk5NCZzaWc9OTVjNDI5YjAzZjdkNGY4YzZmZmRmNTRhZWRkMGE3YjM2NjA2ODI2ODpzZXNzaW9uX2lkPTJfTVg0ME5qazFNRGs1Tkg1LU1UWXdNalE0T1RNMU1EYzVNSDR5YmtGa1QycEdObGxRZG10all6aHZOM1pHS3pkWWRuVi1mZyZjcmVhdGVfdGltZT0xNjAyNDg5NDM1Jm5vbmNlPTAuNzE0MTg1ODA1ODc1Njc0OSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNjA1MDg1MDM0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";


    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String userID="";

    private FrameLayout mPublisherViewController, mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private AudioManager audioManager;
    private ImageView micOn,micOff,cameraOn,cameraOff;

    private ProgressBar progressBar;
    private Timer timer;
    int count=0;

    private ImageView screenShare, cancelScreenShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("User");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);

        micOn = findViewById(R.id.mic_on);
        micOff=findViewById(R.id.mic_off);

        cameraOn=findViewById(R.id.camera_on);
        cameraOff=findViewById(R.id.camera_off);

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        timer = new Timer();

        closeVideoChatBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child(userID).hasChild("Ringing"))
                        {
                            usersRef.child(userID).child("Ringing").removeValue();
                            if(mPublisher!=null)
                            {
                                mSession.unpublish(mPublisher);
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSession.unsubscribe(mSubscriber);
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finishAffinity();

                        }
                        if(snapshot.child(userID).hasChild("Calling"))
                        {
                            usersRef.child(userID).child("Calling").removeValue();
                            if(mPublisher!=null)
                            {
                                mSession.unpublish(mPublisher);
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSubscriber.destroy();
                                mSession.unsubscribe(mSubscriber);
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finishAffinity();
                        }
                        else
                        {
                            if(mPublisher!=null)
                            {
                                mSession.unpublish(mPublisher);
                                mPublisher.destroy();
                            }
                            if(mSubscriber!=null)
                            {
                                mSession.unsubscribe(mSubscriber);
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        requestPermissions();

        micOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMicroPhoneMute();
            }
        });

        micOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMicroPhoneMute();
            }
        });

        cameraOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCameraOff();
            }
        });

        cameraOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCameraOn();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)    // to request permission for camera and audio
    private void requestPermissions()
    {
        String[] perms = {Manifest.permission.INTERNET , Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this,perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //now we are going to initialise and connect to the session

            mSession = new Session.Builder(this, API_key, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else
        {
            //else this msg will be displayed
            EasyPermissions.requestPermissions(this,"this app needs mic and camera permissions,Please Allow", RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    protected void onPause() {

        Log.i(LOG_TAG, "onPause");

        super.onPause();

        if (mSession != null) {
            mSession.onPause();
        }

    }

    @Override
    protected void onResume() {

        Log.i(LOG_TAG, "onResume");

        super.onResume();

        if (mSession != null) {
            mSession.onResume();
        }
    }



    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream)
    {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }


    //publishing a stream to the session
    @Override
    public void onConnected(com.opentok.android.Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());  //to get and display publisher video

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                count++;
                progressBar.setProgress(count);
                if(count==100)
                    timer.cancel();
            }
        };
        timer.schedule(timerTask,0,100);

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);  //to put publisher into the session
    }

    @Override
    public void onDisconnected(com.opentok.android.Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
        mSession.disconnect();
    }


    //subscribing to the streams which have been published already
    @Override
    public void onStreamReceived(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");

        count=0;
        progressBar.setVisibility(View.GONE);

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this , stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());   //to get and display subscriber video
        }
    }

    @Override
    public void onStreamDropped(com.opentok.android.Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        if(mSubscriber!=null)
        {
            mSubscriber=null;
            mSubscriberViewController.removeAllViews();   //this will remove view from the container
        }
    }

    @Override
    public void onError(com.opentok.android.Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    //MUTE AUDIO
    //following is the method to set the user's audio to mute
    public void setMicroPhoneMute(){
        boolean wasMuted = audioManager.isMicrophoneMute();
        if(wasMuted)
        {
            audioManager.setMicrophoneMute(false);
            micOn.setVisibility(View.VISIBLE);
            micOff.setVisibility(View.GONE);
        }
        else
        {
            audioManager.setMicrophoneMute(true);
            micOn.setVisibility(View.GONE);
            micOff.setVisibility(View.VISIBLE);

        }
    }

    public  void setCameraOff()
    {
        mPublisher.setPublishVideo(false);
        cameraOn.setVisibility(View.GONE);
        cameraOff.setVisibility(View.VISIBLE);
    }
    public void setCameraOn()
    {
        mPublisher.setPublishVideo(true);
        cameraOn.setVisibility(View.VISIBLE);
        cameraOff.setVisibility(View.GONE);
    }
}
