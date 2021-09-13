package com.example.wookie.Group;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.BottomNaviActivity;
import com.example.wookie.LoginActivity;
import com.example.wookie.R;

public class GroupListActivity extends AppCompatActivity {

    private String TAG = "GroupListActivity";
    private Button enterBtn, addGroupBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        enterBtn = findViewById(R.id.enter_btn);
        addGroupBtn = findViewById(R.id.add_group_btn);

        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupListActivity.this, BottomNaviActivity.class);
                startActivity(intent);
            }
        });

        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupListActivity.this, MakeGroupActivity.class);
                startActivity(intent);
            }
        });
    }
}