package com.example.wookie.Post;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.R;

public class WritePostActivity extends AppCompatActivity{

    private Button selectPlaceBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write);

        selectPlaceBtn = findViewById(R.id.select_place_btn);

        selectPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WritePostActivity.this, SelectPlaceActivity.class);
                startActivity(intent);
            }
        });
    }
}
