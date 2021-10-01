package com.example.wookie.Post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.Map.ApiClient;
import com.example.wookie.Map.ApiInterface;
import com.example.wookie.Models.CategoryResult;
import com.example.wookie.Models.Document;
import com.example.wookie.R;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditSelectPlaceActivity extends AppCompatActivity {

    private Button cancelBtn;
    private EditText searchTxt;
    RecyclerView recyclerView;

    private double mCurrentLng; //Long = X, Lat = Y
    private double mCurrentLat;
    private double mSearchLng = -1;
    private double mSearchLat = -1;
    private String mSearchName, categoryGroupName;

    ArrayList<Document> documentArrayList = new ArrayList<>(); //지역명 검색 결과 리스트

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);

        searchTxt = findViewById(R.id.search_txt);
        cancelBtn = findViewById(R.id.back_btn);

        recyclerView = findViewById(R.id.map_recyclerview);
        EditLocationAdapter locationAdapter = new EditLocationAdapter(documentArrayList, getApplicationContext(), searchTxt, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(locationAdapter);

        Intent intent = new Intent(EditSelectPlaceActivity.this, EditLocationAdapter.class);

        if((Uri)getIntent().getParcelableExtra("imgUri")!=null){
            intent.putExtra("imgUri", (Uri)getIntent().getParcelableExtra("imgUri"));
        }
        if(getIntent().getStringExtra("postEditTxt")!=null){
            intent.putExtra("imgUri", getIntent().getStringExtra("postEditTxt"));
        }


        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // 입력하기 전에
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 1) {
                    // if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {

                    documentArrayList.clear();
                    locationAdapter.clear();
                    locationAdapter.notifyDataSetChanged();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                    Call<CategoryResult> call = apiInterface.getSearchLocation(getString(R.string.restapi_key), charSequence.toString(), 15);
                    call.enqueue(new Callback<CategoryResult>() {
                        @Override
                        public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                for (Document document : response.body().getDocuments()) {
                                    locationAdapter.addItem(document);
                                }
                                locationAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {

                        }
                    });
                    //}
                    //mLastClickTime = SystemClock.elapsedRealtime();
                } else {
                    if (charSequence.length() <= 0) {
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력이 끝났을 때
            }
        });

        searchTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
        searchTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FancyToast.makeText(getApplicationContext(), "검색리스트에서 장소를 선택해주세요", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Subscribe //검색예시 클릭시 이벤트 오토버스
    public void search(Document document) {//public항상 붙여줘야함
        mSearchName = document.getPlaceName();
        mSearchLng = Double.parseDouble(document.getX());
        mSearchLat = Double.parseDouble(document.getY());

    }
}

