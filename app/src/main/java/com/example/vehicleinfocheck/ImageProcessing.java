package com.example.vehicleinfocheck;

import android.graphics.Bitmap;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageProcessing {
    static{ System.loadLibrary("opencv_java3");}
    static Map<Integer, Mat> map = new HashMap<>();
    static Map<Integer, Character> characterMap = new HashMap<>();
    ImageActivity imgObject = new ImageActivity();
    static Mat plate;
    static Mat plateBW;
    static Bitmap bmp;
    public static String result;

    public void extractPlate(){
        Mat image = Imgcodecs.imread(imgObject.getCurrentPhotoPath());
        File cascadeDir = imgObject.getApplicationContext().getDir("indian_license_plate", imgObject.getApplicationContext().MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, "indian_license_plate.xml");

        CascadeClassifier licenseDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

        Rect rectCrop = null;
        MatOfRect Detections = new MatOfRect();
        licenseDetector.detectMultiScale(image, Detections, 1.3, 7);
        for (Rect rect : Detections.toArray()) {
            int a = (int) (image.height()*0.020);
            int b = (int) (image.width()*0.025);

            rectCrop = new Rect(rect.x, rect.y, rect.width+b , rect.height+a);
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width + b, rect.y + rect.height + a), new Scalar(51, 51, 255), 3);
        }
        plate = new Mat(image,rectCrop);
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
    public void findContour(){
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
    public void execution(){
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
                CharacterRecognitionModel model = CharacterRecognitionModel.newInstance(imgObject.getApplicationContext());

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
