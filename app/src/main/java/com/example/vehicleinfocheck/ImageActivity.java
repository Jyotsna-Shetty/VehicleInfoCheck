package com.example.vehicleinfocheck;

import android.Manifest;
import android.app.Activity;
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

import com.example.vehicleinfocheck.ml.BmpCharacterRecognitionModel;
import com.example.vehicleinfocheck.ml.CharacterRecognitionModel;
import com.theartofdev.edmodo.cropper.CropImage;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
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
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImageActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;   //camera request code
    public static final int GALLERY_REQUEST_CODE = 105;  //gallery request code
    ImageView selectedImage;  //import Imageview as selectedImage
    Button cameraBtn,galleryBtn, ScanBtn; //import buttons
    TextView sampleImgText;
    String currentPhotoPath;
    //String galleryPhotoPath;
    static{ System.loadLibrary("opencv_java3");}
    static Map<Integer, Mat> map = new HashMap<>();
    static Map<Integer, Character> characterMap = new HashMap<>();
    static Mat plate;
    static Mat plateBW;
    Mat char_image;
    static Bitmap bmp,imgCheck;
    public static String result;
    static char character;
    static float[] classifyArray = new float[36];
    static int letter;
    static Mat cropped;
    static int rows, cols;
    static int[] data;
    static int[] shape;

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
            //directs to askCameraPermissions to get Camera Permissions
            @Override
            public void onClick(View v){
                askCameraPermissions();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);  //intent to select photo from media storage
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });

        ScanBtn.setOnClickListener(new View.OnClickListener() {
            //OnClickListener will be triggered when scan button is clicked
            //Sends the selected image to the deep learning model, extracts the license plate number and then redirects to WebActivity
            @Override
            public void onClick(View v) {
                if (currentPhotoPath != null) {
                    try {
                        execution();
                        Log.d("FINAL RESULT","License Plate Num: " + result);
                        Log.d("FINAL RESULT","RES= "+ character);
                        Intent webIntent = new Intent(ImageActivity.this, WebActivity.class); //Intent to redirect from ImageActivity to WebActivity
                        startActivity(webIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        recreate();
                        Toast.makeText(ImageActivity.this,"Unable to identify license plate number", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ImageActivity.this,"Add an image to proceed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //using checkSelfPermission method of ContextCompact checks whether permission is granted or not
    private void askCameraPermissions() {
        //checks for permission from manifest file using PackageManager.PERMISSION_GRANTED
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            //permission for camera is passed within the string
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }
    }
    
    //if camera permissions are not given override the askCameraPermission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //checking  permission by comparing request code of camera with request code passed to onRequestPermissionResult
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){  //camera permission given
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission is required to use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //onActivityResult method is used to display and save image as data of this app
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            //checking resultCode
            if (resultCode == Activity.RESULT_OK && currentPhotoPath != null) {
                File newfile = new File(currentPhotoPath);    //creating new file  from currentPhotoPath
                CropImage.activity(Uri.fromFile(newfile)).start(this);
                //selectedImage.setImageURI(Uri.fromFile(newfile));   //set image to imageview using uri
                sampleImgText.setVisibility(View.INVISIBLE);
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(newfile));    //display absolute url of the file

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(newfile);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);
            } else {
                Toast.makeText(ImageActivity.this,"Unknown error has occured, try again", Toast.LENGTH_SHORT).show();
            }   
        }
        //for cropping the image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();
                selectedImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                Log.d("tag", "ERROR: " + error);

            }
        }
        //for cropping the image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();
                selectedImage.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                assert result != null;
                Exception error = result.getError();
                Log.d("tag", "ERROR: " + error);

            }
        }
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri contentUri = data.getData();    //creating content URI using intent data
                File newFile = null;
                try {
                    newFile = getGalleryImage(contentUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("tag", "Absolute Url of Image is " + Uri.fromFile(newFile)); //display absolute url of the file
                CropImage.activity(contentUri).start(this);
                sampleImgText.setVisibility(View.INVISIBLE);
            }
        }
    }

    public File getGalleryImage(Uri uri) throws IOException {
        InputStream in =  getContentResolver().openInputStream(uri);
        File photoFile = createImageFile();
        FileProvider.getUriForFile(this,"com.example.android.FileProvider", photoFile);
        OutputStream out = new FileOutputStream(photoFile);
        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0){
            out.write(buf,0,len);
        }
        out.close();
        in.close();
        return photoFile;
    }
    
    //This method creates a unique file name for new photo using data time stamp
    //This method is used when photoFile is null
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  //creates time stamp
        String imageFileName = ";JPEG_" + timeStamp + "_";  //creating image file
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //creating image file using the method of CreateTempFile
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        );
        //gets absolute path of image
        currentPhotoPath = image.getAbsolutePath();//using AbsolutePath of image ,image can be displayed in imageview
        return image;
    }
    //this method opens the camera and save our image file into the directory
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there is a camera activity to handle the intent
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
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.FileProvider", photoFile);   //using file provider create URI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);  //can add extra input
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);   //restarting the activity
            }
        }
    }
    //This method extracts and crops the license plate from the image captured or selected
    public void extractPlate() {
        Mat image = Imgcodecs.imread(currentPhotoPath);
        Log.d("PATH",currentPhotoPath);
        if (image.empty()) {
            Log.d("IMAGE STAT","EMPTY");
        } else {
            Log.d("IMAGE STAT","NOT EMPTY");
        }
        MatOfRect Detections = new MatOfRect();
        try {
            InputStream is = getResources().openRawResource(R.raw.indian_license_plate);
            File cascadeDir = getDir("cascade", MODE_PRIVATE);
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
                Log.d("ImageActivity","--(!)Error loading A\n");
                return;
            }
            else
            {
                Log.d("ImageActivity",
                        "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ImageActivity", "Failed to load cascade. Exception thrown: " + e);
        }

        Rect rectCrop = new Rect();

        for (Rect rect : Detections.toArray()) {
            int a = (int) (image.height()*0.020);
            int b = (int) (image.width()*0.025);
            rectCrop = new Rect(rect.x, rect.y, rect.width+b , rect.height+a);
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width + b, rect.y + rect.height + a), new Scalar(51, 51, 255), 3);
            plate = new Mat(image, rectCrop);
            if (plate.empty()) {
                Log.d("PLATE","NOT DETECTED");
            } else {
                Log.d("PLATE","DETECTED");
            }
        }

    }
    //This method applies a few basic OpenCV operations to aid the process of obtaining the individual characters in the findContour method
    public void preProcessing(){
        Mat src = plate;
        Mat dst = new Mat(); //New matrix to store the final image where the input image is supposed to be written
        Imgproc.resize(src, dst, new Size(333, 75));//Scaling the Image using Resize function
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
        Log.d("PRE PROCESSING","WORKS");
    }
    //This method finds the contours of the letters in the license plate, crops and stores them in a map individually
    public void findContour() throws Exception {
        Mat src = plateBW;
        //Converting the source image to binary
        //Mat gray = new Mat(src.rows(), src.cols(),src.type());
        //Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
        Imgproc.threshold(src, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
        //Finding Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("TESTING","findContour");
        Rect charCrop = null;
        for ( int contourIdx=0; contourIdx < contours.size(); contourIdx++ )
        {
            //Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(contourIdx).toArray() );
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
            Log.d("FIND CONTOUR","For loop executed");
            //Convert back to MatOfPoint
            MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

            //Get bounding rect of contour
            Rect rect = Imgproc.boundingRect(points);
            if((Imgproc.contourArea(contours.get(contourIdx))>100) & (Imgproc.contourArea(contours.get(contourIdx))<1500)){

                charCrop = new Rect(rect.x, rect.y, rect.width, rect.height);
                char_image = new Mat(src,charCrop);

                Imgproc.resize(char_image, char_image, new Size(20,40));
                Mat invertcolormatrix= new Mat(char_image.rows(),char_image.cols(), char_image.type(), new Scalar(255,255,255));
                Core.subtract(invertcolormatrix, char_image, char_image);
                Core.copyMakeBorder(char_image, char_image, 4, 4, 4, 4, Core.BORDER_CONSTANT);

                map.put(rect.x, char_image);

                Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0, 255), 2);
                if (!charCrop.empty()) {
                    Log.d("CHARS","FIND CONTOUR CHAR CROP NOT EMPTY");
                } else {
                    Log.d("CHARS","IS EMPTY");
                }
                Log.d("FIND CONTOUR","ENDS HERE");
            } else {
                Log.d("PROBLEM","Find contour if block not executed");
            }
        }
    }
    //This method is responsible for the overall execution of image processing. It calls the extractPlate, characterSegmentation and findContour methods
    //and obtains the final license plate number with the help of a deep learning model to obtain individual characters
    public void execution() throws Exception {
        extractPlate();
        preProcessing();
        findContour();
        setCharacterMap();
        Log.d("EXECUTION","Methods completed");
        if (map.isEmpty()) {
            Log.d("MAP","EMPTY");
        }
        ArrayList<Integer> sortedKeys = new ArrayList<Integer>(map.keySet());
        Collections.sort(sortedKeys);
        result = "";
        for(Integer x : sortedKeys){
            Log.d("TESTING","For loop starts");
            bmp = Bitmap.createBitmap(map.get(x).cols(), map.get(x).rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(map.get(x), bmp);
            bmp = Bitmap.createScaledBitmap(bmp, 28,28,true);

            try {
                BmpCharacterRecognitionModel model = BmpCharacterRecognitionModel.newInstance(getApplicationContext());
                Log.d("TESTING","Try block");
                //Creates inputs for reference
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 28, 28, 3}, DataType.FLOAT32);
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(bmp);
                ByteBuffer byteBuffer = tensorImage.getBuffer();

                inputFeature0.loadBuffer(byteBuffer);

                //Runs model inference and gets result.
                BmpCharacterRecognitionModel.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                for(int i = 0; i<36; i++ ){
                    classifyArray[i] = outputFeature0.getFloatArray()[i];
                    if((int)classifyArray[i] == 1){
                        letter = i;
                        Log.d("INDEX","i= " + i);
                        break;
                    }
                }

                //Releases model resources if no longer used.
                model.close();
                //String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                //character = characters.charAt(outputFeature0.getIntArray()[0]);
                character = characterMap.get(letter);
                result = result + character;
                Log.d("SUCCESS","License Plate Num: " + result);
                Log.d("TESTING","Output Float Array :" + Arrays.toString(classifyArray));

            } catch (IOException e) {
                Log.d("FAIL","NO OUTPUT");
            }

        }
        /*String filename = "external.png";
        File sd = Environment.getExternalStorageDirectory();
        File dest = new File(sd, filename);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    //This method enumerates the deep learning model labels with the help of a map
    public void setCharacterMap(){
        Integer i = 0;
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(Character x : characters.toCharArray()){
            characterMap.put(i, x);
            ++i;
            Log.d("SET CHAR MAP","FOR LOOP");
        }
        Log.d("SET CHAR MAP","WORKS");
    }
}