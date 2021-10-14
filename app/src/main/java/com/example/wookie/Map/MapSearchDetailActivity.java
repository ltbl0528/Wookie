package com.example.wookie.Map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;


import com.example.wookie.Models.Document;
import com.example.wookie.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

public class MapSearchDetailActivity extends AppCompatActivity {
    final static String TAG = "MapSearchDetailTAG";

    //xml
    PieChart pieChart;
    TextView ratingScore;
    RatingBar ratingBar;
    int itemCnt1 = 0; //편의점
    int itemCnt2 = 0; //문화시설
    int itemCnt3 = 0; //관광명소
    int itemCnt4 = 0; //숙소
    int itemCnt5 = 0; //음식점
    int itemCnt6 = 0; //카페

    //value
    ArrayList<Document> reviewArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search_detail);
//        itemCntText7 = findViewById(R.id.mapsearchdetail_tv_itemcount7);
//        itemCntText8 = findViewById(R.id.mapsearchdetail_tv_itemcount8);
//        itemCntText9 = findViewById(R.id.mapsearchdetail_tv_itemcount9);
        pieChart = findViewById(R.id.mapsearchdetail_pie_chart);
        pieChart.setNoDataText("데이터가 없습니다");
        pieChart.getPaint(Chart.PAINT_INFO).setTextSize(Utils.convertDpToPixel(22f));
        processIntent();
        makeChart();
        initView();
    }

    //인텐트처리
    private void processIntent() {
        Intent getIntent = getIntent();
        reviewArrayList = getIntent.getParcelableArrayListExtra(IntentKey.CATEGOTY_SEARCH_MODEL_EXTRA1);
        for(int i = 0; i < reviewArrayList.size(); i++){
            if(reviewArrayList.get(i).getCategoryGroupCode().equals("CS2")){
                itemCnt1 = itemCnt1 + 1;
                continue;
            }
            else if(reviewArrayList.get(i).getCategoryGroupCode().equals("CT1")){
                itemCnt2 = itemCnt2 + 1;
                continue;
            }
            else if(reviewArrayList.get(i).getCategoryGroupCode().equals("AT4")){
                itemCnt3 = itemCnt3 + 1;
                continue;
            }
            else if(reviewArrayList.get(i).getCategoryGroupCode().equals("AD5")){
                itemCnt4 = itemCnt4 + 1;
                continue;
            }
            else if(reviewArrayList.get(i).getCategoryGroupCode().equals("FD6")){
                itemCnt5 = itemCnt5 + 1;
                continue;
            }
            else if(reviewArrayList.get(i).getCategoryGroupCode().equals("CE7")){
                itemCnt6 = itemCnt6 + 1;
                continue;
            }
            else{
                continue;
            }
        }
        Log.e(TAG, "size:"+String.valueOf(reviewArrayList.size())+" itemCnt5:"+itemCnt5);
    }

    private void initView(){

    }

    //차트생성
    private void makeChart() {
        PieDataSet dataSet = new PieDataSet(dataValue(), "그룹 리뷰 데이터");
        PieData data = new PieData();
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        data.addDataSet(dataSet);
        data.setValueTextSize(15f);
        data.setValueTextColor(Color.BLACK);
        data.setHighlightEnabled(false);
        data.setDrawValues(true);


        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.animateY(1000, Easing.EaseInOutCubic);

        ValueFormatter vf = new ValueFormatter() { //value format here, here is the overridden method
            @Override
            public String getFormattedValue(float value) {
                return ""+(int)value;
            }
        };
        data.setValueFormatter(vf);

        pieChart.setData(data);
//        String[] labels = {"편의점", "문화시설", "관광명소", "숙박", "음식점", "카페"};
//        XAxis xAxis = radarChart.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
//        radarChart.setData(data);
    }

    //차트 데이터 생성
    private ArrayList<PieEntry> dataValue() {
        ArrayList<PieEntry> dataVals = new ArrayList<>();
        if(itemCnt1 > 0){
            dataVals.add(new PieEntry(itemCnt1, "편의점"));
        }
        else{

        }
        if(itemCnt2 > 0){
            dataVals.add(new PieEntry(itemCnt2, "문화시설"));
        }
        else{

        }
        if(itemCnt3 > 0){
            dataVals.add(new PieEntry(itemCnt3, "관광명소"));
        }
        else{

        }
        if(itemCnt4 > 0){
            dataVals.add(new PieEntry(itemCnt4, "숙박"));
        }
        else{

        }
        if(itemCnt5 > 0){
            dataVals.add(new PieEntry(itemCnt5, "음식점"));
        }
        else{

        }
        if(itemCnt6 > 0){
            dataVals.add(new PieEntry(itemCnt6, "카페"));
        }
        else{

        }
//        dataVals.add(new PieEntry(hospitalList.size()));
//        dataVals.add(new PieEntry(pharmacyList.size()));
//        dataVals.add(new PieEntry(cafeList.size()));
        return dataVals;
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }
}
