package com.example.aimtionary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class storageRecognitionActivity extends AppCompatActivity {

    private Button select_image;
    private ImageView recognizeImage_button;
    private ImageView image_view;
    private TextView text_view;
    private TextRecognizer textRecognizer;
    private String show_image_or_text="image";
    int Selected_Picture=200;
    Bitmap bitmap=null;
    private TextToSpeech textToSpeech;
    private Button read_text_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_recognition);

        image_view=findViewById(R.id.image_view);
        select_image=findViewById(R.id.select_image);
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_chooser();
            }
        });

        textRecognizer= TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());
        text_view=findViewById(R.id.text_view);
        text_view.setVisibility(View.VISIBLE);

        read_text_button=findViewById(R.id.read_text_button);

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });

        read_text_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = text_view.getText().toString();
                Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    void image_chooser() {
        Intent i=new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i,"Selected Picture"),Selected_Picture);
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK){
            if(requestCode==Selected_Picture){
                Uri selectedImageUri=data.getData();
                if(selectedImageUri !=null){
                    Log.d("storage_Activity","Output Uri: "+selectedImageUri);

                    try {
                        bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);

                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    InputImage image=InputImage.fromBitmap(bitmap,0);

                    Task<Text> result=textRecognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text text) {
                                    text_view.setText(text.getText());
                                    Log.d("Storage_activity", "Out: "+text.getText());

                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("text:",text.getText());
                                    clipboard.setPrimaryClip(clip);

                                    Toast.makeText(storageRecognitionActivity.this,"Copied to clipboard",
                                            Toast.LENGTH_LONG).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }
            }
        }
    }
}