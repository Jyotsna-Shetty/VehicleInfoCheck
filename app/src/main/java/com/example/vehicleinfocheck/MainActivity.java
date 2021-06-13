package com.example.vehicleinfocheck;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;

import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

// Class that contains layout of the complete app
public class MainActivity extends AppCompatActivity {
    //splash screen appears for 2 seconds
    private static int SPLASH_SCREEN_TIME_OUT=2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//to appear in full screen
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent image=new Intent(MainActivity.this,
                        ImageActivity.class);
                //Intent is used to switch from one activity to another.

                startActivity(image);
                //invoke the ImageActivity

                finish();
                //the current activity will get finished.
            }
        }, SPLASH_SCREEN_TIME_OUT);



    }

}