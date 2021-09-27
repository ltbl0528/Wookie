package com.example.wookie.Map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.wookie.Models.Document;
import com.example.wookie.R;

public class PlaceDetailActivity extends AppCompatActivity {

    TextView placeNameText;
    TextView addressText;
    TextView categoryText;
 //   TextView urlText;
    TextView phoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);
        placeNameText = findViewById(R.id.placeName_txt);
        addressText = findViewById(R.id.placeAddress_txt);
        categoryText = findViewById(R.id.placeCategory_txt);
//        urlText = findViewById(R.id.placedetail_tv_url);
        phoneText = findViewById(R.id.placeNumber_txt);
        processIntent();
    }

    private void processIntent(){
        Intent processIntent = getIntent();
        Document document = processIntent.getParcelableExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA);
        placeNameText.setText(document.getPlaceName());
        addressText.setText(document.getRoadAddressName());
        categoryText.setText(document.getCategoryName());
//        urlText.setText(document.getPlaceUrl());
        phoneText.setText(document.getPhone());
    }
}
