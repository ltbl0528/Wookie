package com.example.wookie.Gallery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.wookie.InviteActivity;
import com.example.wookie.R;


public class GalleryActivity extends Fragment {
    public GalleryActivity(){
        // 비어있는 constructor 필요
    }

    private View view;
    private Button testBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_gallery, container, false);

        testBtn = view.findViewById(R.id.test_button);

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), InviteActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
