package com.example.vehicleinfocheck;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Class that displays VAHAN website in the app
public class WebActivity extends AppCompatActivity {
    private Element eltVehicleNumber;  //Vehicle number input field in the website
    private Element eltSubmitBtn;      //Search button present in the website
    private WebScraper webScraper;     //Initialising an instance of the WebScraper class
    private EditText vehicleNumber;    //EditText for vehicle number in the app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); //fixes orientation to PORTRAIT mode
        vehicleNumber = findViewById(R.id.vehicle_plate);
        webScraper = new WebScraper(this);           //creating an instance of the WebScraper class
        webScraper.setUserAgentToDesktop(true);              //default: false
        webScraper.setLoadImages(true);                      //default: false
        LinearLayout webview= findViewById(R.id.webview);
        webview.addView(webScraper.getView());               //setting the LinearLayout to display the contents of the webscraper
        String FULL_URL = "https://vahan.nic.in/nrservices/faces/user/searchstatus.xhtml";
        webScraper.loadURL(FULL_URL);
        Toast.makeText(this, "Loading website...", Toast.LENGTH_SHORT).show();
        //webStart();  //Calling the method that accesses website elements
        //setSearchButtonListeners();  //Calling the method that sets values to the website elements
    }

    // This method is for accessing the elements of the website
    // However, this method is not working and hence is commented out above. WIP
    private void webStart() {
        webScraper.setOnPageLoadedListener(URL -> {
            eltVehicleNumber = webScraper.findElementById("regn_no1_exact");
            eltSubmitBtn = webScraper.findElementByClassName("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only", 0);
            webScraper.setOnPageLoadedListener(null);
            String focusScript =
                    "var children = document.getElementById('wrapper').children; " +
                            "for (var i=0; i<children.length; i++) {children[i].style.display='none';} " +
                            "document.getElementById('page-wrapper').style.display='block'; " +
                            "var children = document.getElementsByClassName('container')[2].children; " +
                            "for (var i=0; i<children.length; i++) {children[i].style.display='none';}" +
                            "document.getElementsByClassName('row bottom-space')[0].style.display='block';" +
                            "document.getElementsByClassName('logo-header-section display-print-none')[0].style.display='block';";
            webScraper.loadURL("javascript:{" + focusScript + "}void(0);");
        });
        Toast.makeText(this, "Loading website...", Toast.LENGTH_SHORT).show();
        String FULL_URL = "https://vahan.nic.in/nrservices/faces/user/searchstatus.xhtml";
        webScraper.loadURL(FULL_URL);
    }

    // This method is to automatically set values to website element (vehicle number field) without user's intervention
    // However, this method is not working and hence is commented out above. WIP
    private void setSearchButtonListeners() {
        Button searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(v -> {
            Toast.makeText(WebActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
            eltVehicleNumber.setText(vehicleNumber.getText().toString());
            eltVehicleNumber.setAttribute("style", "background-color:lightgreen !important");
            eltSubmitBtn.click();
            webScraper.setOnPageLoadedListener(URL -> {
                Toast.makeText(WebActivity.this, "Form Submitted", Toast.LENGTH_SHORT).show();
                String tableScript = "var table = document.getElementsByClassName('table')[0];" +
                        " for (var i = 0, row; typeof(table) != 'undefined' && row = table.rows[i]; i++) {" +
                        " row.style = 'display: table;  width:100%; word-break:break-all;';" +
                        " for (var j = 0, col; col = row.cells[j]; j++) {" +
                        " col.style='display: table-row;'" +
                        " }" +
                        " }";
                webScraper.setOnPageLoadedListener(null);
                webScraper.loadURL("javascript:{" + tableScript + "}void(0)");
            });
        });
    }
}