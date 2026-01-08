package com.example.aimtionary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class aboutActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private com.google.android.material.bottomnavigation.BottomNavigationView bottom_navigation_view;
    private com.google.android.material.floatingactionbutton.FloatingActionButton camera_button;
    private RelativeLayout email_link, whatsapp_link, instagram_link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageSlider imageSlider=findViewById(R.id.slider);
        List<SlideModel> slideModels=new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.tentang_pengembang));
        slideModels.add(new SlideModel(R.drawable.tentang_aplikasi));
        imageSlider.setImageList(slideModels,true);

        email_link=findViewById(R.id.email_link);
        email_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW
                , Uri.parse("mailto:"+"mighwaarfaaris89@gmail.com"));
                startActivity(intent);
            }
        });

        whatsapp_link=findViewById(R.id.whatsapp_link);
        whatsapp_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sNumber = "+6281287976160";
                Uri uri = Uri.parse(String.format(
                        "https://api.whatsapp.com/send?phone=%s",sNumber
                ));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        instagram_link=findViewById(R.id.instagram_link);
        instagram_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sAppLink = "https://www.instagram.com/farismighwar";
                String sPackage = "com.instagram.android";

                openLink(sAppLink,sPackage,sAppLink);
            }
        });

//        bottom_navigation_view=findViewById(R.id.bottom_navigation_view);
//        bottom_navigation_view.setItemIconTintList(null);
//        bottom_navigation_view.setBackgroundTintList(null);
//        bottom_navigation_view.setOnNavigationItemSelectedListener(this);
//
//        camera_button=findViewById(R.id.camera_button);
//        camera_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(aboutActivity.this,CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
//            }
//        });

    }

    private void openLink(String sAppLink, String sPackage, String sWebLink) {
        try {
            Uri uri = Uri.parse(sAppLink);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(sPackage);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException activityNotFoundException){
            Uri uri = Uri.parse(sWebLink);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.home:
                startActivity(new Intent(aboutActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_ANIMATION));

                return true;
        }

        return false;
    }



}