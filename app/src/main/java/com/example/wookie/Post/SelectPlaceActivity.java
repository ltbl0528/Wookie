package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.R;

public class SelectPlaceActivity extends AppCompatActivity {

    private Button dialogBtn, cancleBtn, goodBtn;
    private Dialog testDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);

        dialogBtn = findViewById(R.id.dialog_btn);
        testDialog= new Dialog(SelectPlaceActivity.this);

        testDialog.setContentView(R.layout.dialog_score);

        dialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testDialog.show();
                cancleBtn = testDialog.findViewById(R.id.cancel_btn);
                goodBtn = testDialog.findViewById(R.id.good_btn);
                cancleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testDialog.dismiss();
                    }
                });

                goodBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SelectPlaceActivity.this, WritePostActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}
