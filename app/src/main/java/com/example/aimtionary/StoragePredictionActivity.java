package com.example.aimtionary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.Locale;

public class StoragePredictionActivity extends AppCompatActivity {
    private Button select_image;
    private ImageView image_v;
    private objectDetectorClass objectDetectorClass;
    int SELECT_PICTURE=200;

    private Mat selected_image;


    private Button text_speech_button;
    private TextView label_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_prediction);
        hideNotificationBar();

        select_image=findViewById(R.id.select_image);
        image_v=findViewById(R.id.image_view);

        text_speech_button=findViewById(R.id.text_speech_button);
        final Button languageButton = findViewById(R.id.language_popup_menu);
        PopupMenu popupMenu = new PopupMenu(this, languageButton);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        label_text = findViewById(R.id.label_text);

        ImageView hanger = findViewById(R.id.hanger);
        ImageView headphones = findViewById(R.id.headphone);
        ImageView kasuwari = findViewById(R.id.kasuwari);
        ImageView rafflesia = findViewById(R.id.rafflesia);
        ImageView rollerskate = findViewById(R.id.rollerskate);

        hanger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load and perform object detection on the hanger image
                Bitmap hangerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hanger_preset);
                performObjectDetection(hangerBitmap);
            }
        });

        headphones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load and perform object detection on the headphones image
                Bitmap headphonesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.headphone_preset);
                performObjectDetection(headphonesBitmap);
            }
        });

        kasuwari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load and perform object detection on the headphones image
                Bitmap headphonesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kasuwari_preset);
                performObjectDetection(headphonesBitmap);
            }
        });

        rafflesia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load and perform object detection on the headphones image
                Bitmap headphonesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rafflesia_preset);
                performObjectDetection(headphonesBitmap);
            }
        });

        rollerskate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load and perform object detection on the hanger image
                Bitmap hangerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rollerskate_preset);
                performObjectDetection(hangerBitmap);
            }
        });

        try{
            objectDetectorClass=new objectDetectorClass(StoragePredictionActivity.this,text_speech_button,languageButton,popupMenu,label_text, getAssets(), "model.tflite","labelmap_indo.txt","labelmap.txt", "labelmap_latin.txt", "labelmap_arab.txt",320);
            Log.d("MainActivity","Model is successfully loaded");
        }
        catch (IOException e){
            Log.d("MainActivity","Getting some error");
            e.printStackTrace();
        }

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                label_text.setText("");
                image_chooser();
            }
        });


//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.item1:
//                        performtranslation();
//                        return true;
//
//                    case R.id.item2:
//                        performtranslation();
//                        return true;
//
//                    case R.id.item3:
//                        performtranslation();
//                        return true;
//
//                    default:
//                        performtranslation();
//                        return false;
//                }
//            }
//        });

    }

    private void hideNotificationBar() {
        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Hide the action bar (if your app uses the old ActionBar)
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void image_chooser() {
        Intent i =new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "select Picture"),SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK) {
            if(requestCode==SELECT_PICTURE){
                Uri selectedImageUri=data.getData();
                if(selectedImageUri !=null){
                    Log.d("StoragePrediction", "Output_uri: "+selectedImageUri);
                    Bitmap bitmap=null;
                    try{
                        bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    selected_image = new Mat(bitmap.getHeight(),bitmap.getWidth(), CvType.CV_8UC4);
                    Utils.bitmapToMat(bitmap,selected_image);
                    selected_image=objectDetectorClass.recognizePhoto(selected_image);
                    Bitmap bitmap1=null;
                    bitmap1=Bitmap.createBitmap(selected_image.cols(),selected_image.rows(),Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(selected_image,bitmap1);
                    image_v.setImageBitmap(bitmap1);
                }
            }
        }
    }
    // Function to perform object detection and display the result
    private void performObjectDetection(Bitmap inputBitmap) {
        // Perform object detection on the inputBitmap using your objectDetectorClass
        Mat inputMat = new Mat(inputBitmap.getHeight(), inputBitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(inputBitmap, inputMat);
        selected_image = objectDetectorClass.recognizePhoto(inputMat);

        // Convert the detectedImage Mat back to a Bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(selected_image.cols(), selected_image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(selected_image, outputBitmap);

        // Display the result in the image_v ImageView
        image_v.setImageBitmap(outputBitmap);
    }

    private void performtranslation() {
        // Check if selected_image is not null before using it
        if (selected_image != null) {
            selected_image = objectDetectorClass.recognizePhoto(selected_image);
            Bitmap bitmap1 = null;
            bitmap1 = Bitmap.createBitmap(selected_image.cols(), selected_image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(selected_image, bitmap1);
            image_v.setImageBitmap(bitmap1);
        } else {
            // Handle the case where selected_image is null (not initialized)
            // You might want to show a message or perform some action
        }
    }
}