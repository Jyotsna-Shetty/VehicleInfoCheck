package com.example.vehicleinfocheck;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialization of variables for later use
        EditText UsernameText = findViewById(R.id.EmailAddress);
        EditText Pass1Text = findViewById(R.id.PasswordS);
        EditText Pass2Text = findViewById(R.id.PasswordS2);
        EditText PhoneNoText = findViewById(R.id.PhoneS);
    }


    /*boolean ValidateInput() {
        if (PhoneEditText.getText().toString().equals("") || PhoneEditText.getText().toString().contains(" ")) {
            PhoneEditText.setError("Invalid phone number (check for spaces)");
            return false;
        }
        if (PasswordEditText.getText().toString().equals("")) {
            PasswordEditText.setError("Enter Password");
            return false;
        }
        else return true;
    }*/

}