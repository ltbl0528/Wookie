package com.example.wookie.MyPage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wookie.R;

public class MyMapActivity extends Fragment {

    private View view;
    private Button backBtn;

    public MyMapActivity(){
        // 비어있는 constructor 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_mymap, container, false);

        backBtn = view.findViewById(R.id.back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MyPageActivity myPageActivity = new MyPageActivity();
                transaction.replace(R.id.main_frame, myPageActivity);
                transaction.commit();
            }
        });

        return view;
    }
}
