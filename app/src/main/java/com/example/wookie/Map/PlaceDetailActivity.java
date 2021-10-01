package com.example.wookie.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wookie.Feed.PostAdapter;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Pin;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaceDetailActivity extends AppCompatActivity {
    private String TAG = "PlaceDetailActivity";
    private TextView placeNameText, addressText, categoryText, phoneText, postCnt;
    private Button backBtn, findPathBtn;
    private ImageView pinBtn;
    private String groupId, userId;
    private double lat, lng;
    private double mCurrentLat, mCurrentLng;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_info);
        placeNameText = findViewById(R.id.placeName_txt);
        addressText = findViewById(R.id.placeAddress_txt);
        categoryText = findViewById(R.id.placeCategory_txt);
//        urlText = findViewById(R.id.placedetail_tv_url);
        phoneText = findViewById(R.id.placeNumber_txt);
        postCnt = findViewById(R.id.post_count);
        pinBtn = findViewById(R.id.pin_button);
        findPathBtn = findViewById(R.id.find_path_button);
        backBtn = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.recyclerView);
        processIntent();
    }

    private void processIntent(){
        Intent processIntent = getIntent();
        Document document = processIntent.getParcelableExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA);
        groupId = processIntent.getStringExtra("groupId");
        userId = processIntent.getStringExtra("userId");
        placeNameText.setText(document.getPlaceName());
        addressText.setText(document.getRoadAddressName());
        categoryText.setText(document.getCategoryName());
//        urlText.setText(document.getPlaceUrl());
        phoneText.setText(document.getPhone());

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this);
        // 내림차순으로 정렬
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        String placeId = document.getId();

        setPlaceFeed(placeId);

        Pin pin = new Pin();
        pin.setPlaceName(document.getPlaceName());
        pin.setX(document.getX());
        pin.setY(document.getY());
        pin.setGroupId(groupId);
        pin.setUserId(userId);
        pin.setPinId(userId+"_"+document.getPlaceName());

        reference = database.getReference("Pin").child(groupId).child(pin.getPinId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    pinBtn.setActivated(!pinBtn.isActivated());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setActivated(!view.isActivated());
                if(view.isActivated()){
                    reference.setValue(pin);
                }
                else{
                    reference.removeValue();
                }
            }
        });

        findPathBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentLat = processIntent.getDoubleExtra("mCurrentLat", 0);
                mCurrentLng = processIntent.getDoubleExtra("mCurrentLng", 0);
                lat = processIntent.getDoubleExtra("lat", 0);
                lng = processIntent.getDoubleExtra("lng", 0);
                showMap(Uri.parse("daummaps://route?sp=" + mCurrentLat + "," + mCurrentLng + "&ep=" + lat + "," + lng + "&by=FOOT"));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: 이전 액티비티에 따라 다르게 설정
                finish();
            }
        });
    }

    private void setPlaceFeed(String placeId) {
        postList = new ArrayList<>(); // Post 객체를 담을 Array리스트

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);

        Query query = reference.orderByChild("placeId").equalTo(placeId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);
                    }
                    postCnt.setText("포스트 " + postList.size()+"개");
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        adapter = new PostAdapter(postList, this);
        recyclerView.setAdapter(adapter);
    }

    public void showMap(Uri geoLocation) {
        Intent intent;
        try {
            Toast.makeText(this, "카카오맵으로 길찾기를 시도합니다.", Toast.LENGTH_SHORT).show();
            intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "카카오맵이 설치되어있지 않습니다. 스토어에서 설치해주세요.", Toast.LENGTH_LONG).show();
            intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    //TODO: onBackPressed 이전 액티비티에 따라 다르게 설정
}
