package com.example.wookie;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.wookie.Feed.FeedActivity;
import com.example.wookie.Gallery.GalleryActivity;
import com.example.wookie.Map.MapActivity;
import com.example.wookie.MyPage.MyPageActivity;
import com.example.wookie.Post.WritePostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class BottomNaviActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;

    private FeedActivity feedActivity;
    private MapActivity mapActivity;
    private GalleryActivity galleryActivity;
    private MyPageActivity myPageActivity;

    private int actionBarHeight = 0;
    private FloatingActionButton writeBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        writeBtn = findViewById(R.id.write);
        FrameLayout mf = findViewById(R.id.main_frame);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mf.getLayoutParams();

        // 하단 ActionBar (bottomNavigation) 높이 저장
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        }

        final String groupId = getIntent().getStringExtra("groupId");

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BottomNaviActivity.this, WritePostActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(2).setEnabled(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.feed:
                        setFrag(0);
                        layoutParams.bottomMargin = actionBarHeight;
                        mf.setLayoutParams(layoutParams);
                        break;
                    case R.id.map:
                        setFrag(1);
                        layoutParams.bottomMargin = 0;
                        mf.setLayoutParams(layoutParams);
                        break;
                    case R.id.placeholder:
                        setFrag(2);
                        break;
                    case R.id.gallery:
                        setFrag(3);
                        layoutParams.bottomMargin = actionBarHeight;
                        mf.setLayoutParams(layoutParams);
                        break;
                    case R.id.mypage:
                        setFrag(4);
                        layoutParams.bottomMargin = 0;
                        mf.setLayoutParams(layoutParams);
                        break;
                }
                return true;
            }
        });

        feedActivity = new FeedActivity();
        mapActivity = new MapActivity();
        galleryActivity = new GalleryActivity();
        myPageActivity = new MyPageActivity();

        setFrag(0); //첫 fragment 화면 뭘로 지정할 건지 선택
    }

    //fragment 교체가 일어나는 실행문
    private void setFrag(int n){

        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        switch (n){

            case 0:
                ft.replace(R.id.main_frame, feedActivity);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame, mapActivity);
                ft.commit();
                break;
            case 2:
                break;
            case 3:
                ft.replace(R.id.main_frame, galleryActivity);
                ft.commit();
                break;
            case 4:
                ft.replace(R.id.main_frame, myPageActivity);
                ft.commit();
                break;
        }
    }
}
