package com.example.vehicleinfocheck;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
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
    public static final int CAMERA_REQUEST_CODE = 102;//camera request code
    public static final int GALLERY_REQUEST_CODE = 105;//gallery request code
    ImageView selectedImage;//import Imageview variable as selectedImage
    Button cameraBtn,galleryBtn;//import camera and gallery button
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        selectedImage = findViewById(R.id.displayImageView);//selecting imageview using id from xml resources
        cameraBtn = findViewById(R.id.cameraBtn);//selecting camera using id from xml resources
        galleryBtn = findViewById(R.id.galleryBtn);//selecting gallery using id from xml resorces

        cameraBtn.setOnClickListener(new View.OnClickListener(){
            //OnClickListener will be triggered when camera button is clicked
            @Override
            public void onClick(View v){
                askCameraPermissions();//directs to askCameraPermissions method to get camera permission
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListerner will be triggered when gallery button is clicked
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//creating new intent to select photo from media storage
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });
    }
    //using check self permission method of ContextCompact checks whether permission is granted or not
    private void askCameraPermissions() {
        //PERMISSION_GRANTED gives binary output if is it equal to PackageManager.PERMISSION_GRANTED then the user has already given the access
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //Activitycompat class requests permission during runtime
            //Permission is passed within the string from manifest
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();//directs to dispatchTakePictureIntent method
        }
    }
    
    //if camera permissions are not given override the askCameraPermission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking whether permission is given for camera by comparing request code of camera with request code passed to onRequestPermissionResult
        if (requestCode == CAMERA_PERM_CODE) {
            //checking grantResults array is empty
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){//if both the conditions are true then camera permission is given
                dispatchTakePictureIntent();
            } else {//toast message appear when both conditions are false
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // onActivityResult method is used to display and save image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            //checking resultCode
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);//creating new file f from the currentPhotoPath
                selectedImage.setVisibility(View.VISIBLE);
                selectedImage.setImageURI(Uri.fromFile(f));//set image to imageview using uri
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(f));//display absolute url of the file

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            }
        }
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                Uri contentUri = data.getData();//creating content URI using intent data
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//creating file name using time stamp
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("tag", "onActivityResult: Gallery Image Uri: " + imageFileName);
                selectedImage.setVisibility(View.VISIBLE);
                selectedImage.setImageURI(contentUri);
            }
        }
    }

    public String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }
    
    //creating collision resistant file name.This method creates a unique file name for new photo using data time stamp
    //recommended method of creating image file from the camera
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());//creates time stamp
        String imageFileName = ";JPEG_" + timeStamp + "_";//file name starts with JPEG followed by timestamp
        // File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) to save our file 
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(//creating image file using the method of CreateTempFile we are passing image parameters
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();//using AbsolutePath of image ,image can be displayed in imageview
        return image;
    }
    //open the camera and save our image file into the directory
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there is a camera activity to handle the intent , whether camera permission isgiven to the activity
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();//createImageFile can throw IOException so put inside try catch block.Creating photoFile assigning to Create image file which returns image
            } catch (IOException ex) {
            }
// Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);//using file provider create URI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);//using Mediastore.EXTRA-OUTPUT we can add extra input to our intent and can get data
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);//restarting the activity using camera permission code.
            }
        }
    }
}
