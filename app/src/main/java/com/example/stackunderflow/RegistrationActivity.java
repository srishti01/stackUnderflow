package com.example.stackunderflow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker mCcp;
    private EditText PhoneText;
    private EditText CodeText;
    private String checker="";
    private String PhoneNumber="";
    private RelativeLayout relativeLayout;
    private Button continueAndNextBtn;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        mProgressBar=new ProgressDialog(this);

        PhoneText=findViewById(R.id.phoneText);
        CodeText=findViewById(R.id.codeText);
        continueAndNextBtn=findViewById(R.id.continueNextButton);
        relativeLayout=findViewById(R.id.phoneAuth);
        mCcp =(CountryCodePicker)findViewById(R.id.ccp);

        mCcp.registerCarrierNumberEditText(PhoneText);

        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(continueAndNextBtn.getText().equals("Submit")|| checker.equals("Code Sent"))    //when the text on button changes to submit we will verify the code
                {
                    String VerificationCode=CodeText.getText().toString();  //code verification
                    if(VerificationCode!=null)
                    {
                        mProgressBar.setTitle("Code Verification");
                        mProgressBar.setMessage("Please Wait");
                        mProgressBar.setCanceledOnTouchOutside(false);
                        mProgressBar.show();

                        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationID,VerificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                    else {
                        Toast.makeText(RegistrationActivity.this,"Write Verification code",Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                        PhoneNumber=mCcp.getFullNumberWithPlus();       //to get phone number with ccp

                        if(PhoneNumber!=null)        //(!PhoneNumber.equals("")
                        {
                             mProgressBar.setTitle("Phone Number Verification");        //enable the progressbar
                             mProgressBar.setMessage("Please Wait");
                             mProgressBar.setCanceledOnTouchOutside(false);
                             mProgressBar.show();

                            PhoneAuthProvider.getInstance().verifyPhoneNumber(PhoneNumber,60,
                                    TimeUnit.SECONDS,                   //phone number authentication code from firebase
                                   RegistrationActivity.this,
                                    mCallbacks);
                        }
                        else {
                            Toast.makeText(RegistrationActivity.this,"Invalid Phone Number",Toast.LENGTH_SHORT).show();
                        }
                }
            }
        });

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);     ///when code if verified we call this method

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {    //if verification fails we set code to gone and text on btn to continue
                Toast.makeText(RegistrationActivity.this,"Verification Unsuccessful!",Toast.LENGTH_SHORT).show();
                mProgressBar.dismiss();
                relativeLayout.setVisibility(View.VISIBLE);
                continueAndNextBtn.setText("Continue");
                CodeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mResendToken=forceResendingToken;
                mVerificationID=s;                                //to get verification code in other phone
                relativeLayout.setVisibility(View.GONE);
                checker="Code Sent";
                continueAndNextBtn.setText("Submit");
                CodeText.setVisibility(View.VISIBLE);
                mProgressBar.dismiss();
            }
        };

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            Intent homeIntent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information and send user to main activity
                            mProgressBar.dismiss();
                            Toast.makeText(RegistrationActivity.this,"Sign in Successful!",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(RegistrationActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            // Sign in failed, display a message and update the UI and make a toast msg
                            mProgressBar.dismiss();
                            Toast.makeText(RegistrationActivity.this,"Error! Sign in Unsuccessful!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}


