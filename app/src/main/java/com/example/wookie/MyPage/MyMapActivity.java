package com.example.wookie.MyPage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wookie.Map.ApiClient;
import com.example.wookie.Map.ApiInterface;
import com.example.wookie.Map.BusProvider;
import com.example.wookie.Map.IntentKey;
import com.example.wookie.Map.MapLocaAdapter;
import com.example.wookie.Map.PlaceDetailActivity;
import com.example.wookie.Models.CategoryResult;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyMapActivity extends Fragment implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener, View.OnClickListener, MapView.CurrentLocationEventListener {

    private String TAG = "MyMapActivity";

    //xml
    MapView mMapView;
    ViewGroup mMapViewContainer;
    private FloatingActionButton fab1, fab2;
    RelativeLayout mLoaderLayout;
    RecyclerView recyclerView;
    private View view;
    private Button backBtn;
    EditText mSearchEdit;
    private TextView userNameTxt;
    private String groupId, userId;

    //value
    MapPoint currentMapPoint;
    private double mCurrentLng; //Long = X, Lat = Y
    private double mCurrentLat;
    private double mSearchLng = -1;
    private double mSearchLat = -1;
    private String mSearchName;
    boolean isTrackingMode = false; //트래킹 모드인지 (3번째 버튼 현재위치 추적 눌렀을 경우 true되고 stop 버튼 누르면 false로 된다)
    Bus bus = BusProvider.getInstance();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference, placeRef;

    ArrayList<Document> documentArrayList = new ArrayList<>(); //지역명 검색 결과 리스트
    ArrayList<Document> reviewArrayList = new ArrayList<>();
    ArrayList<Post> postList = new ArrayList<>();//리뷰 장소 리스트


    MapPOIItem searchMarker = new MapPOIItem();

    public MyMapActivity(){
        // 비어있는 constructor 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_mymap, container, false);
//        Bundle bundle = new Bundle();
//        String groupId = bundle.getString("groupId");

        setMyMap();
        initView(view);

        return view;
    }


    private void initView(View view) {
        //binding
        fab1 = view.findViewById(R.id.fab1);
        fab2 = view.findViewById(R.id.fab2);
        backBtn = view.findViewById(R.id.back_btn);
        userNameTxt = view.findViewById(R.id.user_name);
        mLoaderLayout = view.findViewById(R.id.loaderLayout);
        mMapView = new MapView(view.getContext());
        mMapViewContainer = view.findViewById(R.id.map_mv_mapcontainer);
        mMapViewContainer.addView(mMapView);
        recyclerView = view.findViewById(R.id.map_recyclerview);
        MapLocaAdapter mapLocaAdapter = new MapLocaAdapter(documentArrayList, view.getContext(), mSearchEdit, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mapLocaAdapter);

        //맵 리스너
        mMapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현.
        mMapView.setPOIItemEventListener(this);
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);

        //버튼리스너
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);

        Toast.makeText(view.getContext(), "맵을 로딩중입니다", Toast.LENGTH_SHORT).show();

        //맵 리스너 (현재위치 업데이트)
        mMapView.setCurrentLocationEventListener(this);
        //setCurrentLocationTrackingMode (지도랑 현재위치 좌표 찍어주고 따라다닌다.)
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mLoaderLayout.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MyPageActivity myPageActivity = new MyPageActivity();
                transaction.replace(R.id.main_frame, myPageActivity);
                transaction.commit();
            }
        });
    }

    private void setMyMap(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){
                    userId = String.valueOf(user.getId());
                    // 프로필 정보 설정
                    userNameTxt.setText(user.getKakaoAccount().getProfile().getNickname()+"님의 지도");
                }
                else{
                    userNameTxt.setText(null);
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab2: //아래버튼에서부터 1~3임
                FancyToast.makeText(v.getContext(), "현재위치 추적 시작", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                mLoaderLayout.setVisibility(View.VISIBLE);
                isTrackingMode = true;
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                mLoaderLayout.setVisibility(View.GONE);
                break;
            case R.id.fab1:
                isTrackingMode = false;
                FancyToast.makeText(v.getContext(), "현재위치기준 1km 검색 시작", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                mLoaderLayout.setVisibility(View.VISIBLE);
                //현재 위치 기준으로 1km 검색
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestSearchLocal(mCurrentLng, mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                break;
        }
    }

    private void requestSearchLocal(double x, double y) {

        if (getArguments() != null) {
            groupId = getArguments().getString("groupId");
        }
        else{
            Toast.makeText(this.getContext(), "오류!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, getArguments().getString("groupId"));
        }

        reviewArrayList.clear();
        postList.clear();

        reference = database.getReference("Post").child(groupId);
        Query query = reference.orderByChild("userId").equalTo(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);

                        placeRef = database.getReference("Place").child(post.getGroupId());
                        Query query1 = placeRef.orderByChild("id").equalTo(post.getPlaceId());
                        Log.e(TAG, post.getPlaceId());
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                        Document document = dataSnapshot1.getValue(Document.class);
                                        reviewArrayList.add(document);
                                        MapPOIItem marker = new MapPOIItem();
                                        marker.setItemName(document.getPlaceName());
                                        double x1 = Double.parseDouble(document.getY());
                                        double y1 = Double.parseDouble(document.getX());
                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x1, y1);
                                        marker.setMapPoint(mapPoint);
                                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                        marker.setCustomImageResourceId(R.drawable.small_cat); // 마커 이미지.
                                        marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                        marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                        mMapView.addPOIItem(marker);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }


    @Override
    public void onMapViewInitialized(MapView mapView) {
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    //맵 한번 클릭시 호출
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        //검색창켜져있을때 맵클릭하면 검색창 사라지게함
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    // 길찾기 카카오맵 호출( 카카오맵앱이 없을 경우 플레이스토어 링크로 이동)
    public void showMap(Uri geoLocation) {
        Intent intent;
        try {
            FancyToast.makeText(getActivity().getApplicationContext(), "카카오맵으로 길찾기를 시도합니다.", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            FancyToast.makeText(getActivity().getApplicationContext(), "길찾기에는 카카오맵이 필요합니다. 다운받아주시길 바랍니다.", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    //말풍선(POLLITEM) 클릭시 호출
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        double lat = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude;
        double lng = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude;
        Toast.makeText(this.getContext(), mapPOIItem.getItemName(), Toast.LENGTH_SHORT).show();
        mLoaderLayout.setVisibility(View.VISIBLE);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchLocationDetail(getString(R.string.restapi_key), mapPOIItem.getItemName(), String.valueOf(lng), String.valueOf(lat), 1);
        Log.e(TAG, mapPOIItem.getItemName()+" "+lat+" "+lng);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                mLoaderLayout.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), PlaceDetailActivity.class);
                    assert response.body() != null;
                    intent.putExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA, response.body().getDocuments().get(0));
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {
                FancyToast.makeText(getActivity().getApplicationContext(), "해당장소에 대한 상세정보는 없습니다.", FancyToast.LENGTH_SHORT, FancyToast.ERROR, true).show();
                mLoaderLayout.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlaceDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    // 마커 드래그이동시 호출
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        mSearchName = "드래그한 장소";
        mSearchLng = mapPointGeo.longitude;
        mSearchLat = mapPointGeo.latitude;
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
        searchMarker.setItemName(mSearchName);
        MapPoint mapPoint2 = MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng);
        searchMarker.setMapPoint(mapPoint2);
        searchMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        searchMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        searchMarker.setDraggable(true);
        mMapView.addPOIItem(searchMarker);
    }

    /*
     *  현재 위치 업데이트(setCurrentLocationEventListener)
     */
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        //이 좌표로 지도 중심 이동
        mMapView.setMapCenterPoint(currentMapPoint, true);
        //전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        //1km 반경 마커 띄우기
        mapView.removeAllPOIItems();
        mapView.removeAllCircles();
        requestSearchLocal(mCurrentLng, mCurrentLat);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        Log.d(TAG, "현재위치 => " + mCurrentLat + "  " + mCurrentLng);
        mLoaderLayout.setVisibility(View.GONE);
        //트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
        if (!isTrackingMode) {
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        Log.i(TAG, "onCurrentLocationUpdateFailed");
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        Log.i(TAG, "onCurrentLocationUpdateCancelled");
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        bus.unregister(this);
//    }
}
