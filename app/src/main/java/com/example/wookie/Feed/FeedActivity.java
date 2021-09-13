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

        contentText = view.findViewById(R.id.content_text);

        contentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //보낼 데이터 있을 경우 주석 풀어서 쓰기
                //Bundle bundle = new Bundle();
                //bundle.putString("fromFeedActivity", "여기에는 글 ID 같은 거");
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                ReadPostActivity readPostActivity = new ReadPostActivity();
//                //readPostActivity.setArguments(bundle);
//                transaction.replace(R.id.main_frame, readPostActivity);
//                transaction.commit(); //fragment 상태 저장
                Intent intent = new Intent(getActivity(), ReadPostActivity.class);
                startActivity(intent);

            }
        });

        return view;
    }
}
