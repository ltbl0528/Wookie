package com.example.wookie.Feed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.wookie.Feed.PostAdapter;
import com.example.wookie.Feed.PostShortAdapter;
import com.example.wookie.Map.IntentKey;
import com.example.wookie.Map.PlaceDetailActivity;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Group;
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
import java.util.Collections;

public class PlaceFeedActivity extends AppCompatActivity {
    private String TAG = "PlaceFeedActivity";
    private TextView placeNameTxt;
    private Button backBtn;
    private Document document;
    private String groupId, userId;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Post> postList;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        placeNameTxt = findViewById(R.id.group_name);
        backBtn = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this);
        // 내림차순으로 정렬
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        backBtn.setVisibility(View.VISIBLE);
        processIntent();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
                intent.putExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA, document);
                intent.putExtra("groupId", groupId);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void processIntent(){
        Intent processIntent = getIntent();
        document = processIntent.getParcelableExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA);
        groupId = processIntent.getStringExtra("groupId");
        userId = processIntent.getStringExtra("userId");
        placeNameTxt.setText(document.getPlaceName());

        String placeId = document.getId();
        setFeed(placeId);

    }

    private void setFeed(String placeId) {
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
                    placeNameTxt.append(" (" + postList.size() + "개)");
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


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), PlaceDetailActivity.class);
        intent.putExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA, document);
        intent.putExtra("groupId", groupId);
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }
}