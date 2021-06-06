package com.example.vehicleinfocheck;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {
    private Element eltVehicleNumber;
    private Element eltCaptchaInput;
    private Element eltSubmitBtn;
    private WebScraper webScraper;
    private EditText vehicleNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        vehicleNumber = findViewById(R.id.vehicle_plate);

        webScraper = new WebScraper(this);
        webScraper.setUserAgentToDesktop(true); //default: false
        webScraper.setLoadImages(true); //default: false
        LinearLayout webview= findViewById(R.id.webview);
        webview.addView(webScraper.getView());
        webstart();
        setSearchButtonListeners();
    }

    private void webstart() {
        webScraper.setOnPageLoadedListener(new WebScraper.onPageLoadedListener() {
            @Override
            public void loaded(String URL) {
                eltVehicleNumber = webScraper.findElementById("regn_no1_exact");
                eltCaptchaInput = webScraper.findElementById("txt_ALPHA_NUMERIC");
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
            }
        });
        Toast.makeText(this, "Loading website...", Toast.LENGTH_SHORT).show();
        String VEHICLE_URL = "/nrservices/faces/user/searchstatus.xhtml";
        String BASE_URL = "https://vahan.nic.in";
        String FULL_URL = BASE_URL + VEHICLE_URL;
        webScraper.loadURL(FULL_URL);
    }

    private void setSearchButtonListeners() {
        Button searchBtn = findViewById(R.id.search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WebActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
                eltVehicleNumber.setText(vehicleNumber.getText().toString());
                eltVehicleNumber.setAttribute("style", "background-color:lightgreen !important");
                eltSubmitBtn.click();
                webScraper.setOnPageLoadedListener(new WebScraper.onPageLoadedListener() {
                    @Override
                    public void loaded(String URL) {
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
                    }
                });
            }
        });
    }
}