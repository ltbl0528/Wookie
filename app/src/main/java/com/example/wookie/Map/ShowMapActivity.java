package com.example.wookie.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.R;

public class ShowMapActivity extends AppCompatActivity {

    private EditText searchText;
    private Button placeInfoBtn;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_show);

        searchText=findViewById(R.id.search_txt);
        placeInfoBtn=findViewById(R.id.placeInfo_btn);

        searchText.setText("신전떡볶이 xx점");

        placeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowMapActivity.this, PlaceInfoActivity.class);
                startActivity(intent);
            }
        });
    }
}
