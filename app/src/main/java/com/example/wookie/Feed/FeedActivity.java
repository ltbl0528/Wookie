package com.example.wookie.Feed;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.Post.ReadPostActivity;
import com.example.wookie.R;

public class FeedActivity extends Fragment {

    private View view;
    private TextView contentText;

    public FeedActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_feed, container, false);

        contentText = view.findViewById(R.id.post_content);

        contentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ReadPostActivity.class);
                startActivity(intent);

            }
        });

        return view;
    }
}
