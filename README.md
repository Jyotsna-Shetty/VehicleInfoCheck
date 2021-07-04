# VehicleInfoCheck
An Android application that scans Indian license plate images and extracts the details of the vehicle from the national vehicle registry, VAHAN.

## Description
<img src="https://user-images.githubusercontent.com/81788169/124381452-51e9e480-dce0-11eb-8a7e-be483d1d0fe1.gif" height="500">

The app can capture an image using the device camera or choose an image from the device’s gallery which can be cropped as per the user’s preference.
This image is then processed using OpenCV for Java. This involves:
*	Extracting the license plate from the image
*	Conversion of image to greyscale
*	OpenCV operations such as erosion and dilation to aid character segmentation 
*	Conversion of image to binary 
*	Finding the contour of each character present in the license plate
*	Cropping the license plate to get individual images for each character

The processed images are sent to the Tensorflow deep learning model which performs optical character recognition to identify the license plate number.
> Presently, the deep learning model used in the project gives an incorrect prediction of the license plate number.

Next, the user is redirected to the page which displays the [VAHAN website](https://vahan.nic.in/nrservices/faces/user/login.xhtml), where the detected license plate number can be used to obtain details such as vehicle model, fuel type, manufacturer details, among others.
## To import the project into Android Studio 
1.	Open Android Studio, click **File --> New --> Project from Version Control**.
2.	Choose Git from the Version control dropdown.
3.	Copy the hyperlink present when you click on the Code button in the repository and paste in the URL tab.
4.	Click Clone.
5.	Sync project and run.
## System requirements
Android Studio – this project was built on version 4.1.0

External library – OpenCV (see [Enabling OpenCV](https://github.com/Jyotsna-Shetty/VehicleInfoCheck#enabling-opencv))

RAM – preferably 8 GB and above (users with 4 GB RAM will have issues running the emulator).

Available disk space – 8 GB and above (IDE + Android SDK + Android Emulator).

Operating system:
*	64-bit Microsoft Windows 8/10
*	MacOS 10.14 (Mojave) or higher
* 64-bit Linux distribution that supports Gnome, KDE, or Unity DE; GNU C Library (glibc) 2.31 or later

Processor – 2nd generation Intel Core or newer.

Complete list of system requirements for Android Studio can be found [here.](https://developer.android.com/studio)
### To run the app on an Android phone, the requirements are:
Android version – 5.0 (Lollipop, API 21) and above.

Available storage space on device – 50 MB +
## Enabling OpenCV
1. Open `build.gradle` from the OpenCV library module present in the Project panel of Android Studio, check if the `compileSdkVersion` and `targetSdkVersion` match the version in your PC. If not, change it to your version.
2. Go to **File --> Sync project with Gradle files**. The library should now be visible in the Android panel of Android Studio. 
## Folders
### app
This folder contains the main code and gradle file for the app, along with auto-generated .gitignore and proguard-rules files.

#### src
Contains `main` folder (all Android Studio files and folders present in it) and two test folders (not used in this project). Inside `main`, we have:

***java/com/example/vehicleinfocheck***: Contains all java classes defining the function of the app.
1. Activity classes: MainActivity- loading screen; ImageActivity- image selection and processing; WebActivity- Website display and function.
2. WebScraper and Element classes: Contain methods for accessing and displaying the website within the app. 

***ml***: Contains the deep learning tflite model.

***res***: 
1. `drawable`: All the pre-existing and imported drawables (images and xml) are present here.
2. `layout`: Layout files for every activity of the are present here.
3. `mipmap`: App icon files are present in these folders.
4. `font`: The fonts added manually to the project are present here.
5. `values`: Contains folders for strings, colours, dimensions and preloaded fonts.
6. `xml`: File path for saving images is present here.

***AndroidManifest.xml***: Manifest file with all neccesary information regarding the app, such as app package name, activites, permissions the app needs and content providers.

***vic_logo-playstore.png***: Logo of the app.

### Gradle files and .idea
The gradle/wrapper folder, build.gradle (outside of app folder), gradle.properties, gradlew, gradlew.bat, settings.gradle, .idea are all gradles files generated by Android studio and are added to .gitignore since no changes are made to them.
### LICENSE
This project is licensed under the GNU General Public License v3.0. The file contains the complete description of the license, its permissions, limitations and  conditions.
## Credits
Deep learning model used - https://github.com/SarthakV7/AI-based-indian-license-plate-detection.

Library used for cropping images - https://github.com/ArthurHub/Android-Image-Cropper

Reference used for WebScraper class - https://github.com/Udayraj123/VehicleInfoOCR. 
