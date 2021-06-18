package com.example.vehicleinfocheck;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Class that displays VAHAN website in the app
public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // Fixes orientation to PORTRAIT mode
        EditText vehicleNumber = findViewById(R.id.VehicleNum);
        ImageButton CopyButton = findViewById(R.id.CopyBtn);
        LinearLayout webview= findViewById(R.id.Webview);
        WebScraper webScraper = new WebScraper(this);   // Creating an instance of the WebScraper class

        webScraper.setUserAgentToDesktop(true);                 // Default: false
        webScraper.setLoadImages(true);                         // Default: false
        webview.addView(webScraper.getView());                  //Setting the LinearLayout to display the contents of the webScraper
        String FULL_URL = "https://vahan.nic.in/nrservices/faces/user/searchstatus.xhtml";
        webScraper.loadURL(FULL_URL);
        Toast.makeText(this, "Loading website...", Toast.LENGTH_SHORT).show();

        // OnClickListener triggered when CopyButton is clicked
        CopyButton.setOnClickListener(v -> {
            // Creating an instance of ClipboardManager class
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); // Clip is made only if there is text to copy
            if (!vehicleNumber.getText().toString().trim().equals("")) {
                ClipData clip = ClipData.newPlainText("simple text", vehicleNumber.getText()); // Creates a clip with value = vehicleNumber
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });
    }
}