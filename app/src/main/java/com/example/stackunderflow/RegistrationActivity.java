package com.example.stackunderflow;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.data.model.PhoneNumber;
import com.hbb20.CountryCodePicker;

public class RegistrationActivity extends AppCompatActivity {
    private CountryCodePicker cpp;
    private EditText PhoneText;
    private EditText CodeText;
    private String checker="";
    private String PhoneNumber="";
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
