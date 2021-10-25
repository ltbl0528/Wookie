package com.example.wookie.Post;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.Map.IntentKey;
import com.example.wookie.Map.PlaceDetailActivity;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShowImgFullActivity extends AppCompatActivity {
    
    private String TAG = "ShowImgFullActivity";
    private ImageView imageView;
    private Button backBtn;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_full);

        imageView = findViewById(R.id.imageView);
        backBtn = findViewById(R.id.back_btn);
        
        final String imageUrl = getIntent().getStringExtra("imageUrl");

        Glide.with(imageView).load(imageUrl).into(imageView);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
