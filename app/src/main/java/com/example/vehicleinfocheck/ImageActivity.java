package com.example.vehicleinfocheck;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;  //camera request code
    public static final int GALLERY_REQUEST_CODE = 105;  //gallery request code
    ImageView selectedImage;  //import Imageview as selectedImage
    Button cameraBtn,galleryBtn, ScanBtn; //import buttons
    TextView sampleImgText;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);  //fixes orientation to PORTRAIT mode

        selectedImage = findViewById(R.id.displayImageView); //finding imageview
        sampleImgText = findViewById(R.id.SampleImgMsg);
        cameraBtn = findViewById(R.id.cameraBtn); 
        galleryBtn = findViewById(R.id.galleryBtn);
        ScanBtn = findViewById(R.id.ScanBtn); 

        cameraBtn.setOnClickListener(new View.OnClickListener(){
            //OnClickListener will be triggered when camera button is clicked
            //directs to askCameraPermissions to get Camera Permissions
            @Override
            public void onClick(View v){
                askCameraPermissions();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListener will be triggered when gallery button is clicked
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  //creating new intent to select photo from media storage
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        ScanBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListener will be triggered when scan button is clicked
            //The function of this button is to send the selected image to the deep learning model, extract the license plate number and then redirect to WebActivity
            //For now, it redirects to WebActivity. It will perform the above function after OCR process is complete
            @Override
            public void onClick(View v) {
                Intent webIntent = new Intent(ImageActivity.this, WebActivity.class);
                startActivity(webIntent);
            }
        });

    }

   
    //using checkSelfPermission method of ContextCompact checks whether permission is granted or not
    private void askCameraPermissions() {
        //checks for permission from manifest file using PackageManager.PERMISSION_GRANTED
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //Activitycompat class requestPermissions method requests permission during runtime
            //permission for camera is passed within the string
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            //user has given permission to the app
            dispatchTakePictureIntent();  
        }
    }
    
    //if camera permissions are not given override the askCameraPermission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking  permission  by comparing request code of camera with request code passed to onRequestPermissionResult
        if (requestCode == CAMERA_PERM_CODE) {
            //checking grantResults array is empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){  //camera permission given
                dispatchTakePictureIntent();
            } else {
                //toast message appear when both conditions are false
                Toast.makeText(this, "Camera permission is required to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //onActivityResult method is used to display and save image as data of this app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File newfile = new File(currentPhotoPath);  //creating file  from currentPhotoPath
                selectedImage.setImageURI(Uri.fromFile(newfile));  //set image to imageview using uri
                sampleImgText.setVisibility(View.INVISIBLE);
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(newfile));  //display absolute url of the file

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(newfile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if(data != null) {
                    Uri contentUri = data.getData();  //creating content URI using intent data
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  
                    String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                    Log.d("tag", "onActivityResult: Gallery Image Uri: " + imageFileName);
                    selectedImage.setImageURI(contentUri);
                    sampleImgText.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
    //extracts extension of image picked from gallery
    public String getFileExt(Uri contentUri) {
        ContentResolver content = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(content.getType(contentUri));
    }
    
    //This method creates a unique file name for new photo using data time stamp
    //This method is used when photoFile is null
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  //creates time stamp
        String imageFileName = ";JPEG_" + timeStamp + "_";  //creating image file
        //Standard directory to place image
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //creating image file using method of CreateTempFile
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        //gets absolute path of image
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //this method opens the camera and save our image file into the directory
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //ensures camera is ready
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create the File where the photo should go
            File photoFile = null;
            //to prevent IOException
            try {
                photoFile = createImageFile();  //this returns image
            } catch (IOException ex) {
                 System.out.println(ex.getMessage());
            }
             //Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.FileProvider", photoFile);  //using file provider create URI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);  //can add extra input
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);  //restarting the activity 
            }
        }
    }
}