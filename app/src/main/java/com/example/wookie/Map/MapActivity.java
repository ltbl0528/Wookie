package com.example.wookie.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.wookie.R;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class MapActivity extends Fragment {

    private View view;
    private MapView mapView;
    private EditText searchText;

    public MapActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_map, container, false);

        // 카카오맵 띄우기
        mapView = new MapView(getActivity());
        ViewGroup mapViewContainer = (ViewGroup)view.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        // 좌표 지정
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.45106423452633, 127.12713721912326);

        // 지정된 좌표로 카카오맵 중심점 변경
        mapView.setMapCenterPoint(MARKER_POINT, true);
        // 줌 인
        mapView.zoomIn(true);
        // 줌 아웃
        //mapView.zoomOut(true);

        // 마커 표시
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker);


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
