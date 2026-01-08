package com.example.aimtionary;
//package com.example.imagepro;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.checkerframework.checker.units.qual.A;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class objectDetectorClass {
    // should start from small letter

    // this is used to load model and predict
    private Interpreter interpreter;
    // store all label in array
    private List<String> labelList;
    private List<String> labelText;
    private int INPUT_SIZE;
    private int PIXEL_SIZE=3; // for RGB
    private int IMAGE_MEAN=0;
    private  float IMAGE_STD=255.0f;
    // use to initialize gpu in app
    private GpuDelegate gpuDelegate;
    private int height=0;
    private  int width=0;
    private TextToSpeech textToSpeech;
    private String language="";
    private Context context;
    private String final_text="";
    private String final_text_label="";
    private TextView label_text;
    private boolean objectDetected = false;


    objectDetectorClass(Context context, Button text_speech_button, Button languageButton, PopupMenu popupMenu, TextView label_text, AssetManager assetManager, String modelPath, String labelPathIndo, String labelPath, String labelPathLatin, String labelPathArab, int inputSize) throws IOException{
        INPUT_SIZE=inputSize;
        // use to define gpu or cpu // no. of threads
        Interpreter.Options options=new Interpreter.Options();
        gpuDelegate=new GpuDelegate();
//        options.addDelegate(gpuDelegate);
        options.setNumThreads(4); // set it according to your phone
        // loading model
        interpreter=new Interpreter(loadModelFile(assetManager,modelPath),options);
        // load labelmap
        labelList=loadLabelList(assetManager,labelPath);
        labelText=loadLabelText(assetManager,labelPath);
        this.label_text = label_text;
        this.context = context;
        popupMenu.setGravity(Gravity.TOP);


        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status !=TextToSpeech.ERROR){
//                    textToSpeech.setLanguage(new Locale("id","ID"));
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the popup menu

                popupMenu.show();
            }
        });

        // Set the click listener for the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        try {
                            labelList=loadLabelList(assetManager,labelPathIndo);
                            labelText=loadLabelText(assetManager,labelPathIndo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(new Locale("id","ID"));
                                }
                            }
                        });
                        updateLabelText();
                        Toast.makeText(context, "Bahasa Indonesia Selected", Toast.LENGTH_SHORT).show();
                        return true;


                    case R.id.item2:
                        try {
                            labelList=loadLabelList(assetManager,labelPath);
                            labelText=loadLabelText(assetManager,labelPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(Locale.ENGLISH);
                                }
                            }
                        });
                        updateLabelText();
                        Toast.makeText(context, "English Language Selected", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.item3:
                        try {
                            labelList=loadLabelList(assetManager,labelPathLatin);
                            labelText=loadLabelText(assetManager,labelPathArab);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(new Locale("ar","SA"));
                                }
                            }
                        });
                        updateLabelText();
                        Toast.makeText(context, "Arabic Language Selected", Toast.LENGTH_SHORT).show();
                        return true;

                    default:
                        try {
                            labelList=loadLabelList(assetManager,labelPath);
                            labelText=loadLabelText(assetManager,labelPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                if(status !=TextToSpeech.ERROR){
                                    textToSpeech.setLanguage(Locale.ENGLISH);
                                }
                            }
                        });
                        updateLabelText();
//                        Toast.makeText(MainActivity.this, "English Language Selected", Toast.LENGTH_SHORT).show();
                        return false;
                }
            }
        });

        text_speech_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(final_text,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }


    private void updateLabelText() {
        label_text.post(new Runnable() {
            @Override
            public void run() {
                label_text.setText(final_text_label);
            }
        });
    }



    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        // to store label
        List<String> labelList=new ArrayList<>();
        // create a new reader
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        // loop through each line and store it to labelList
        while ((line=reader.readLine())!=null){
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private List<String> loadLabelText(AssetManager assetManager, String labelPathText) throws IOException {
        List<String> labelText=new ArrayList<>();
        BufferedReader reader=new BufferedReader(new InputStreamReader(assetManager.open(labelPathText)));
        String line;
        while ((line=reader.readLine())!=null){
            labelText.add(line);
        }
        reader.close();
        return labelText;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // use to get description of file
        AssetFileDescriptor fileDescriptor=assetManager.openFd(modelPath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset =fileDescriptor.getStartOffset();
        long declaredLength=fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    // create new Mat function
    public Mat recognizeImage(Mat mat_image){

        boolean noObjectsDetected = true;
        // Rotate original image by 90 degree get get portrait frame

        // This change was done in video: Does Your App Keep Crashing? | Watch This Video For Solution.
        // This will fix crashing problem of the app

        Mat rotated_mat_image=new Mat();

        Mat a=mat_image.t();
        Core.flip(a,rotated_mat_image,1);
        // Release mat
        a.release();

        // if you do not do this process you will get improper prediction, less no. of object
        // now convert it to bitmap
        Bitmap bitmap=null;
        bitmap=Bitmap.createBitmap(rotated_mat_image.cols(),rotated_mat_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotated_mat_image,bitmap);
        // define height and width
        height=bitmap.getHeight();
        width=bitmap.getWidth();

        // scale the bitmap to input size of model
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);

        // convert bitmap to bytebuffer as model input should be in it
        ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);

        // defining output
        // 10: top 10 object detected
        // 4: there coordinate in image
        //  float[][][]result=new float[1][10][4];
        Object[] input=new Object[1];
        input[0]=byteBuffer;

        Map<Integer,Object> output_map=new TreeMap<>();
        // we are not going to use this method of output
        // instead we create treemap of three array (boxes,score,classes)

        float[][][]boxes =new float[1][10][4];
        // 10: top 10 object detected
        // 4: there coordinate in image
        float[][] scores=new float[1][10];
        // stores scores of 10 object
        float[][] classes=new float[1][10];
        // stores class of object

        // add it to object_map;
//        output_map.put(0,boxes);
//        output_map.put(1,classes);
//        output_map.put(2,scores);
        output_map.put(1,boxes);
        output_map.put(3,classes);
        output_map.put(0,scores);


        // now predict
        interpreter.runForMultipleInputsOutputs(input,output_map);
        // Before watching this video please watch my previous 2 video of
        //      1. Loading tensorflow lite model
        //      2. Predicting object
        // In this video we will draw boxes and label it with it's name

//        Object value=output_map.get(0);
//        Object Object_class=output_map.get(1);
//        Object score=output_map.get(2);
        Object value=output_map.get(1);
        Object Object_class=output_map.get(3);
        Object score=output_map.get(0);

        // loop through each object
        // as output has only 10 boxes
        for (int i=0;i<10;i++){
            float class_value=(float) Array.get(Array.get(Object_class,0),i);
            float score_value=(float) Array.get(Array.get(score,0),i);
            // define threshold for score

            // Here you can change threshold according to your model
            // Now we will do some change to improve app
            if(score_value>0.5){
                noObjectsDetected = false;
                Object box1=Array.get(Array.get(value,0),i);
                // we are multiplying it with Original height and width of frame

                float top=(float) Array.get(box1,0)*height;
                float left=(float) Array.get(box1,1)*width;
                float bottom=(float) Array.get(box1,2)*height;
                float right=(float) Array.get(box1,3)*width;

                final_text = labelList.get(((int) class_value));
                final_text_label = labelText.get(((int) class_value));

                updateLabelText();

                // draw rectangle in Original frame //  starting point    // ending point of box  // color of box       thickness
                Imgproc.rectangle(rotated_mat_image,new Point(left,top),new Point(right,bottom),new Scalar(0, 255, 0, 255),2);
                // write text on frame
                // string of class name of object  // starting point                         // color of text           // size of text
                Imgproc.putText(rotated_mat_image,labelList.get((int) class_value),new Point(left,top),3,1,new Scalar(255, 255, 255, 255),5);
                Imgproc.putText(rotated_mat_image,labelList.get((int) class_value),new Point(left,top),3,1,new Scalar(2505, 0, 0, 255),2);
            }

        }

        if (noObjectsDetected) {
            final_text = ""; // Clear the text
            final_text_label = ""; // Clear the label
            updateLabelText();
        }

        // select device and run

        // before returning rotate back by -90 degree

        // Do same here
        Mat b=rotated_mat_image.t();
        Core.flip(b,mat_image,0);
        b.release();
        // Now for second change go to CameraBridgeViewBase
        return mat_image;
    }

    public Mat recognizePhoto(Mat mat_image){

        // Rotate original image by 90 degree get get portrait frame

        // This change was done in video: Does Your App Keep Crashing? | Watch This Video For Solution.
        // This will fix crashing problem of the app


        // if you do not do this process you will get improper prediction, less no. of object
        // now convert it to bitmap
        Bitmap bitmap=null;
        bitmap=Bitmap.createBitmap(mat_image.cols(),mat_image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat_image,bitmap);
        // define height and width
        height=bitmap.getHeight();
        width=bitmap.getWidth();

        // scale the bitmap to input size of model
        Bitmap scaledBitmap=Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);

        // convert bitmap to bytebuffer as model input should be in it
        ByteBuffer byteBuffer=convertBitmapToByteBuffer(scaledBitmap);

        // defining output
        // 10: top 10 object detected
        // 4: there coordinate in image
        //  float[][][]result=new float[1][10][4];
        Object[] input=new Object[1];
        input[0]=byteBuffer;

        Map<Integer,Object> output_map=new TreeMap<>();
        // we are not going to use this method of output
        // instead we create treemap of three array (boxes,score,classes)

        float[][][]boxes =new float[1][10][4];
        // 10: top 10 object detected
        // 4: there coordinate in image
        float[][] scores=new float[1][10];
        // stores scores of 10 object
        float[][] classes=new float[1][10];
        // stores class of object

        // add it to object_map;
//        output_map.put(0,boxes);
//        output_map.put(1,classes);
//        output_map.put(2,scores);
        output_map.put(1,boxes);
        output_map.put(3,classes);
        output_map.put(0,scores);


        // now predict
        interpreter.runForMultipleInputsOutputs(input,output_map);
        // Before watching this video please watch my previous 2 video of
        //      1. Loading tensorflow lite model
        //      2. Predicting object
        // In this video we will draw boxes and label it with it's name

//        Object value=output_map.get(0);
//        Object Object_class=output_map.get(1);
//        Object score=output_map.get(2);
        Object value=output_map.get(1);
        Object Object_class=output_map.get(3);
        Object score=output_map.get(0);

        // loop through each object
        // as output has only 10 boxes
        for (int i=0;i<10;i++){
            float class_value=(float) Array.get(Array.get(Object_class,0),i);
            float score_value=(float) Array.get(Array.get(score,0),i);
            // define threshold for score

            // Here you can change threshold according to your model
            // Now we will do some change to improve app
            if(score_value>0.5){
                Object box1=Array.get(Array.get(value,0),i);
                // we are multiplying it with Original height and width of frame

                float top=(float) Array.get(box1,0)*height;
                float left=(float) Array.get(box1,1)*width;
                float bottom=(float) Array.get(box1,2)*height;
                float right=(float) Array.get(box1,3)*width;

                final_text = labelList.get(((int) class_value));
                final_text_label = labelText.get(((int) class_value));

                updateLabelText();

                // draw rectangle in Original frame //  starting point    // ending point of box  // color of box       thickness
                Imgproc.rectangle(mat_image,new Point(left,top),new Point(right,bottom),new Scalar(0, 255, 0, 255),10);
                // write text on frame
                // string of class name of object  // starting point                         // color of text           // size of text
                Imgproc.putText(mat_image,labelList.get((int) class_value),new Point(left,top),3,2,new Scalar(255, 255, 255, 255),10);
                Imgproc.putText(mat_image,labelList.get((int) class_value),new Point(left,top),3,2,new Scalar(2505, 0, 0, 255),5);

            }

        }

        return mat_image;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        // some model input should be quant=0  for some quant=1
        // for this quant=0
        // Change quant=1
        // As we are scaling image from 0-255 to 0-1
        int quant=1;
        int size_images=INPUT_SIZE;
        if(quant==0){
            byteBuffer=ByteBuffer.allocateDirect(1*size_images*size_images*3);
        }
        else {
            byteBuffer=ByteBuffer.allocateDirect(4*1*size_images*size_images*3);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues=new int[size_images*size_images];
        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        int pixel=0;

        // some error
        //now run
        for (int i=0;i<size_images;++i){
            for (int j=0;j<size_images;++j){
                final  int val=intValues[pixel++];
                if(quant==0){
                    byteBuffer.put((byte) ((val>>16)&0xFF));
                    byteBuffer.put((byte) ((val>>8)&0xFF));
                    byteBuffer.put((byte) (val&0xFF));
                }
                else {
                    // paste this
                    byteBuffer.putFloat((((val >> 16) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val >> 8) & 0xFF))/255.0f);
                    byteBuffer.putFloat((((val) & 0xFF))/255.0f);
                }
            }
        }
        return byteBuffer;
    }
}