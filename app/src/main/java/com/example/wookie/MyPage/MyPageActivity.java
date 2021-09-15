package com.example.wookie.MyPage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wookie.R;

public class MyPageActivity extends Fragment {

    private View view;
    private Button myMapBtn;

    public MyPageActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_mypage, container, false);
        //보낼 데이터 있을 경우 주석 풀어서 쓰기
        //Bundle bundle = new Bundle();
        //bundle.putString("fromFeedActivity", "여기에는 글 ID 같은 거");
        myMapBtn = view.findViewById(R.id.my_map_btn);
        myMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MyMapActivity myMapActivity = new MyMapActivity();
                //readPostActivity.setArguments(bundle);
                transaction.replace(R.id.main_frame, myMapActivity);
                transaction.commit(); //fragment 상태 저장
            }
        });
        return view;
    }
}
