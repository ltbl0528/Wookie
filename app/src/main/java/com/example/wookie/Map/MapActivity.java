package com.example.wookie.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.wookie.R;

public class MapActivity extends Fragment {

    private View view;
    private EditText searchText;

    public MapActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_map, container, false);

        searchText = view.findViewById(R.id.search_txt);
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), SearchPlaceActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }
}
