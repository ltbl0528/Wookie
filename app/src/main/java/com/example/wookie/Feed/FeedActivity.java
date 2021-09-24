package com.example.wookie.Feed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.Models.Post;
import com.example.wookie.Post.ReadPostActivity;
import com.example.wookie.Post.WritePostActivity;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FeedActivity extends Fragment {

    private String TAG = "FeedActivity";
    private View view;
    private TextView groupNameTxt;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> postList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Query query;

    public FeedActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_feed, container, false);

        groupNameTxt = view.findViewById(R.id.group_name);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this.getContext());
        // 내림차순으로 정렬
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        final String groupId = getActivity().getIntent().getStringExtra("groupId");

        setGroupName(groupId); // 방제 설정
        setFeed(groupId); // 피드 목록 설정

        return view;
    }

    //피드 목록 설정
    private void setFeed(String groupId) {
        postList = new ArrayList<>(); // Post 객체를 담을 Array리스트

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        adapter = new PostAdapter(postList, this.getContext());
        recyclerView.setAdapter(adapter);
    }

    // 방제 설정
    private void setGroupName(String groupId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("group");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String groupName = dataSnapshot.child(groupId).child("groupName").getValue().toString();
                    groupNameTxt.setText(groupName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }
}
