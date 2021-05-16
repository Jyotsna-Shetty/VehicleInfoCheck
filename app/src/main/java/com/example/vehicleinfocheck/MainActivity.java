package com.example.vehicleinfocheck;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;
    ImageView selectedImage = findViewById(R.id.displayImageView);
    Button cameraBtn = findViewById(R.id.cameraBtn);
    Button galleryBtn = findViewById(R.id.galleryBtn);
    String currentPhotoPath;



    /*int MIN_PASSWORD_LENGTH = 7;
    EditText PhoneEditText = findViewById(R.id.Phone);
    EditText PasswordEditText = findViewById(R.id.Password);
    Button LoginButton = findViewById(R.id.LoginButton);
    TextView HeadingTextView = findViewById(R.id.HeadingText);*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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