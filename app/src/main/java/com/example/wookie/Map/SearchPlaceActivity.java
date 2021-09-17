package com.example.wookie.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.R;

public class SearchPlaceActivity extends AppCompatActivity {

    private EditText searchText;
    private Button showMapBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_search);

        searchText = findViewById(R.id.search_txt);
        showMapBtn = findViewById(R.id.showMap_btn);

        searchText.isFocusableInTouchMode();
        searchText.setFocusable(true);
        searchText.requestFocus();

        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchPlaceActivity.this, ShowMapActivity.class);
                startActivity(intent);
            }
        });
    }
}

