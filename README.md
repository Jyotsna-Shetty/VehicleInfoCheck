# VehicleInfoCheck
An Android application that scans Indian license plate images and extracts the details of the vehicle from the national vehicle registry, VAHAN.

Status: _In progress_
## Description
The app can capture an image using the device camera or choose an image from the device’s gallery which can be cropped as per the user’s preference.
This image is then processed using OpenCV for Java. This involves:
*	Extracting the license plate from the image
*	Conversion of image to greyscale
*	OpenCV operations such as erosion and dilation to aid character segmentation 
*	Conversion of image to binary 
*	Finding the contour of each character present in the license plate
*	Cropping the license plate to get individual images for each character

The processed images are sent to the Tensorflow deep learning model which performs optical character recognition to identify the license plate number.

Next, the user is redirected to the page which displays the [VAHAN website](https://vahan.nic.in/nrservices/faces/user/login.xhtml), where the detected license plate number can be used to obtain details such as vehicle model, fuel type, manufacturer details, among others.
## To import the project into Android Studio 
1.	Open Android Studio, click **File --> New --> Project from Version Control**.
2.	Choose Git from the Version control dropdown.
3.	Copy the hyperlink present when you click on the Code button in the repository and paste in the URL tab.
4.	Click Clone.
5.	Sync project and run.
## System requirements
Android Studio – this project was built on version 4.1.0

Git installed on the device.

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
1. Download OpenCV version 3.4.12 from https://opencv.org/releases/.
2. Extract the .zip file into a folder.
3. In Android Studio, open **File --> New --> Import Module** and in the pop-up that asks for the Source directory select `'the folder in which you extracted OpenCV'` --> `sdk` --> `java`; click Next and Finish.
4. Open `build.gradle` from the OpenCV library module present in the Project window of Android Studio, check if the `compileSdkVersion` and `targetSdkVersion` match the version in your PC. If not, change it to your version and click on Sync now.
5. After syncing, open **File --> Project Structure --> app module**, click on the **+** in the right end to add **Module Dependency**, select the OpenCV library module and click OK.
6. Open `'folder in which OpenCV was extracted'` --> `sdk` --> `native` and copy the folder `libs` present there. Paste it in `'your AndroidStudioProject'` --> `app` --> `src` --> `main` in File Explorer. Rename the pasted `libs` to `jniLibs`.
## Credits
Deep learning model used - https://github.com/SarthakV7/AI-based-indian-license-plate-detection.

Reference used for WebScraper class - https://github.com/Udayraj123/VehicleInfoOCR. 
