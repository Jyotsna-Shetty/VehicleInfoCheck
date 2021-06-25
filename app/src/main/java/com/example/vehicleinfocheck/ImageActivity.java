package com.example.vehicleinfocheck;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import com.example.vehicleinfocheck.ml.CharacterRecognitionModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;//camera request code
    public static final int GALLERY_REQUEST_CODE = 105;//gallery request code
    ImageView selectedImage;//import Imageview variable as selectedImage
    Button cameraBtn,galleryBtn, ScanBtn;//import camera and gallery button
    TextView sampleImgText;
    String currentPhotoPath;
    static{ System.loadLibrary("opencv_java3");}
    static Map<Integer, Mat> map = new HashMap<>();
    static Map<Integer, Character> characterMap = new HashMap<>();
    //ImageActivity imgObject = new ImageActivity();
    static Mat plate;
    static Mat plateBW;
    static Bitmap bmp;
    public static String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); //fixes orientation to PORTRAIT mode

        selectedImage = findViewById(R.id.displayImageView); //selecting imageview using id from xml resources
        sampleImgText = findViewById(R.id.SampleImgMsg);
        cameraBtn = findViewById(R.id.cameraBtn); //selecting camera using id from xml resources
        galleryBtn = findViewById(R.id.galleryBtn); //selecting gallery using id from xml resources
        ScanBtn = findViewById(R.id.ScanBtn); //selecting scan button using id from xml resources

        cameraBtn.setOnClickListener(new View.OnClickListener(){
            //OnClickListener will be triggered when camera button is clicked
            @Override
            public void onClick(View v){
                askCameraPermissions();//directs to askCameraPermissions method to get camera permission
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListener will be triggered when gallery button is clicked
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//creating new intent to select photo from media storage
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        ScanBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListener will be triggered when scan button is clicked
            //The function of this button is to send the selected image to the deep learning model, extract the license plate number and then redirect to WebActivity
            //For now, it redirects to WebActivity. It will perform the above function after OCR process is complete
            @Override
            public void onClick(View v) {
                try {
                    execution();
                    Intent webIntent = new Intent(ImageActivity.this, WebActivity.class); //Intent to redirect from ImageActivity to WebActivity
                    startActivity(webIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    recreate();
                    Toast.makeText(ImageActivity.this,"Unable to identify license plate number",Toast.LENGTH_LONG).show();
                }
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
                selectedImage.setImageURI(Uri.fromFile(f));//set image to imageview using uri
                sampleImgText.setVisibility(View.INVISIBLE);
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
                selectedImage.setImageURI(contentUri);
                sampleImgText.setVisibility(View.INVISIBLE);
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
        // Ensure that there is a camera activity to handle the intent , whether camera permission is given to the activity
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();//createImageFile can throw IOException so put inside try catch block.Creating photoFile assigning to Create image file which returns image
            } catch (IOException ex) {
            }
// Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.FileProvider", photoFile);//using file provider create URI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);//using Mediastore.EXTRA-OUTPUT we can add extra input to our intent and can get data
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);//restarting the activity using camera permission code.
            }
        }
    }

    public void extractPlate() {
        Mat image = Imgcodecs.imread(currentPhotoPath);
        MatOfRect Detections = new MatOfRect();
        try {
            InputStream is = getResources().openRawResource(R.raw.indian_license_plate);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "indian_license_plate.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            CascadeClassifier licenseDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            licenseDetector.detectMultiScale(image, Detections, 1.3, 7);
            if(licenseDetector.empty())
            {
                Log.v("ImageActivity","--(!)Error loading A\n");
                return;
            }
            else
            {
                Log.v("ImageActivity",
                        "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("ImageActivity", "Failed to load cascade. Exception thrown: " + e);
        }

        Rect rectCrop = new Rect();

        for (Rect rect : Detections.toArray()) {
            int a = (int) (image.height()*0.020);
            int b = (int) (image.width()*0.025);
            rectCrop = new Rect(rect.x, rect.y, rect.width+b , rect.height+a);
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width + b, rect.y + rect.height + a), new Scalar(51, 51, 255), 3);
            plate = new Mat(image, rectCrop);
        }

    }
    public void characterSegmentation(){
        Mat src = plate;
        Mat dst = new Mat(); // New matrix to store the final image where the input image is supposed to be written
        Imgproc.resize(src, dst, new Size(333, 75));// Scaling the Image using Resize function
        //Converting the source image to binary
        Mat gray = new Mat(dst.rows(), dst.cols(), dst.type());
        Imgproc.cvtColor(dst, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(dst.rows(), dst.cols(), dst.type(), new Scalar(0));
        Imgproc.threshold(gray, binary, 200, 255, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);

        //Applying dilate on the Image
        int erosion_size = 1;
        int dilation_size = 1;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_size + 1,2*dilation_size + 1));//Preparing the kernel matrix object
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*erosion_size + 1,2*dilation_size + 1));
        Imgproc.erode(binary, binary, element);
        Imgproc.dilate(binary, binary, element1);
        Imgproc.rectangle(binary, new Point(0,0), new Point(binary.width(),binary.height()), new Scalar(255, 255, 255), 3);
        plateBW = binary;
    }
    public void findContour() throws Exception {
        Mat src = plateBW;
        //Converting the source image to binary
        Mat gray = new Mat(src.rows(), src.cols(), src.type());
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
        //Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect charCrop = null;
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            // Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            //Convert back to MatOfPoint
            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

            // Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);
            if((Imgproc.contourArea(contours.get(contourIdx))>100) & (Imgproc.contourArea(contours.get(contourIdx))<1500)){

                charCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
                Mat char_image = new Mat(src,charCrop);

                Imgproc.resize(char_image, char_image, new Size(20,40));
                Mat invertcolormatrix= new Mat(char_image.rows(),char_image.cols(), char_image.type(), new Scalar(255,255,255));
                Core.subtract(invertcolormatrix, char_image, char_image);
                Core.copyMakeBorder(char_image, char_image, 4, 4, 4, 4, Core.BORDER_CONSTANT);

                map.put(rect.x, char_image);

                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0, 255), 2);

            }
        }
        execution();
    }
    public void execution() throws Exception {
        extractPlate();
        characterSegmentation();
        findContour();
        setCharacterMap();
        ArrayList<Integer> sortedKeys = new ArrayList<Integer>(map.keySet());
        Collections.sort(sortedKeys);
        for(Integer x : sortedKeys){
            Utils.matToBitmap(map.get(x), bmp);
            bmp = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
            try {
                CharacterRecognitionModel model = CharacterRecognitionModel.newInstance(getApplicationContext());

                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28, 28, 3}, DataType.FLOAT32);
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(bmp);
                ByteBuffer byteBuffer = tensorImage.getBuffer();

                inputFeature0.loadBuffer(byteBuffer);
                // Runs model inference and gets result.
                CharacterRecognitionModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                // Releases model resources if no longer used.
                model.close();
                char character = characterMap.get(outputFeature0.getIntArray()[0]);
                result = result + character;

            } catch (IOException e) {
                // TODO Handle the exception
            }

        }

    }
    public void setCharacterMap(){
        Integer i = 0;
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(Character x : characters.toCharArray()){
            characterMap.put(i, x);
            i++;
        }
    }
    public String returnResult(){
        return result;
    }
}
