package com.example.wookie.Map;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wookie.Feed.FeedActivity;
import com.example.wookie.Models.CategoryResult;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Pin;
import com.example.wookie.Models.Post;

import com.example.wookie.MyPage.MyMapActivity;
import com.example.wookie.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
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

public class MapActivity extends Fragment implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener, View.OnClickListener, MapView.CurrentLocationEventListener {
    final static String TAG = "MapTAG";
    private static final int LOCATION_PERMISSION = 1004; //?????? ??????
    //xml
    MapView mMapView;
    ViewGroup mMapViewContainer;
    EditText mSearchEdit;
    private FloatingActionButton fab1, fab2, fabStat;
    RelativeLayout mLoaderLayout;
    RecyclerView recyclerView;

    //value
    MapPoint currentMapPoint;
    private double mCurrentLng; //Long = X, Lat = Y
    private double mCurrentLat;
    private double mSearchLng = -1;
    private double mSearchLat = -1;
    private String mSearchName, groupId, userId;
    boolean isTrackingMode = false; //????????? ???????????? ???????????? ??????
    Bus bus = BusProvider.getInstance();
    private View view;
    boolean isClickedOne = true; // ????????? ????????? ??????(?????? ?????? ?????????)??? ???????????? ???????????? ??????
    boolean isClickedTwo = true; // ????????? ????????? ??????(??? ??????)??? ???????????? ???????????? ??????

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference, placeRef, pinRef;

    ArrayList<Document> documentArrayList = new ArrayList<>(); //????????? ?????? ?????? ?????????
    ArrayList<Document> reviewArrayList = new ArrayList<>();
    ArrayList<Post> postList = new ArrayList<>();//?????? ?????? ?????????
    ArrayList<Pin> pinList = new ArrayList<>();


    MapPOIItem searchMarker = new MapPOIItem();

    public MapActivity(){
        // ???????????? constructor ??????
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_map, container, false);
        bus.register(this); //????????? ??????
        //????????? ????????? ??????
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {//????????????
            //?????? ?????? ??????
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
        } else {//????????????
            initView(view);
        }
        return view;
    }

    private void initView(View view) {
        //binding
        mSearchEdit = view.findViewById(R.id.search_txt);
        fab1 = view.findViewById(R.id.fab1);
        fab2 = view.findViewById(R.id.fab2);
        fabStat = view.findViewById(R.id.fab_stat);
        mLoaderLayout = view.findViewById(R.id.loaderLayout);
        mMapView = new MapView(view.getContext());
        mMapViewContainer = view.findViewById(R.id.map_mv_mapcontainer);
        mMapViewContainer.addView(mMapView);
        recyclerView = view.findViewById(R.id.map_recyclerview);
        MapLocaAdapter mapLocaAdapter = new MapLocaAdapter(documentArrayList, view.getContext(), mSearchEdit, recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false); //????????????????????? ??????
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL)); //??????????????? ??????
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mapLocaAdapter);

        //??? ?????????
        mMapView.setMapViewEventListener(this); // this??? MapView.MapViewEventListener ??????.
        mMapView.setPOIItemEventListener(this);
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);

        //???????????????
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fabStat.setOnClickListener(this);

        fab1.setEnabled(false);
        fab2.setEnabled(false);
        fabStat.setEnabled(false);

        Toast.makeText(view.getContext(), "?????? ??????????????????", Toast.LENGTH_SHORT).show();

        //??? ????????? (???????????? ????????????)
        mMapView.setCurrentLocationEventListener(this);
        //setCurrentLocationTrackingMode (????????? ???????????? ?????? ???????????? ???????????????.)
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mLoaderLayout.setVisibility(View.VISIBLE);

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//????????? ??????
                    userId = String.valueOf(user.getId());
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });

        // editText ?????? ??????????????????
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // ???????????? ??????
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 1) {
                    // if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {

                    documentArrayList.clear();
                    mapLocaAdapter.clear();
                    mapLocaAdapter.notifyDataSetChanged();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                    Call<CategoryResult> call = apiInterface.getSearchLocation(getString(R.string.restapi_key), charSequence.toString(), 15);
                    call.enqueue(new Callback<CategoryResult>() {
                        @Override
                        public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                for (Document document : response.body().getDocuments()) {
                                    mapLocaAdapter.addItem(document);
                                }
                                mapLocaAdapter.notifyDataSetChanged();
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
                // ????????? ????????? ???
            }
        });

        mSearchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab2: //???????????????????????? 1~3???
                if(isClickedTwo){
                    fab2.setImageResource(R.drawable.ic_push_pin_clicked);
                    fab1.setEnabled(false); //?????? ?????? ??????????????? ??????
                    mLoaderLayout.setVisibility(View.VISIBLE);
                    mMapView.removeAllPOIItems();
                    mMapView.removeAllCircles();
                    pinRef = database.getReference("Pin").child(groupId);
                    pinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    Pin pin = dataSnapshot.getValue(Pin.class);
                                    pinList.add(pin);
                                    MapPOIItem marker = new MapPOIItem();
                                    marker.setItemName(pin.getPlaceName());
                                    double x2 = Double.parseDouble(pin.getY());
                                    double y2 = Double.parseDouble(pin.getX());
                                    //??????????????? ????????? new MapPoint()???  ????????????. ??????????????? ???????????? ????????? ???????????? ???????????????
                                    MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x2, y2);
                                    marker.setMapPoint(mapPoint);
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // ??????????????? ????????? ????????? ??????.
                                    marker.setCustomImageResourceId(R.drawable.pin_marker_35); // ?????? ?????????.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi ??? ??????????????? ???????????? ???????????? ????????? ?????? ?????? ?????????????????? ????????? ????????? ??????.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // ?????? ???????????? ????????? ?????? ??????(???????????????) ?????? - ?????? ????????? ?????? ?????? ?????? x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) ???.
                                    mMapView.addPOIItem(marker);
                                    mLoaderLayout.setVisibility(View.GONE);
                                }
                            }
                            else{
                                Toast.makeText(getActivity().getApplicationContext(), "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                                mLoaderLayout.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    isClickedTwo = false;
                }
                else{
                    fab2.setImageResource(R.drawable.ic_push_pin);
                    fab1.setEnabled(true);
                    mLoaderLayout.setVisibility(View.VISIBLE);
                    mMapView.removeAllPOIItems();
                    mMapView.removeAllCircles();
                    requestSearchLocal(mCurrentLng, mCurrentLat);
                    mLoaderLayout.setVisibility(View.GONE);
                    isClickedTwo = true;
                }
//                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                break;
            case R.id.fab1:
                if(isClickedOne){
                    fab1.setImageResource(R.drawable.ic_my_location_clicked);
                    isTrackingMode = true;
                    mLoaderLayout.setVisibility(View.VISIBLE);
                    mMapView.removeAllPOIItems();
                    mMapView.removeAllCircles();
                    requestSearchLocal(mCurrentLng, mCurrentLat);
                    mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                    mLoaderLayout.setVisibility(View.GONE);
                    isClickedOne = false;
                }
                else{
                    fab1.setImageResource(R.drawable.ic_my_location);
                    isTrackingMode = false;
                    mLoaderLayout.setVisibility(View.VISIBLE);
                    mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                    mLoaderLayout.setVisibility(View.GONE);
                    isClickedOne = true;
                }
                break;
            case R.id.fab_stat:
                Intent detailIntent = new Intent(getContext(), MapSearchDetailActivity.class);
                detailIntent.putParcelableArrayListExtra(IntentKey.CATEGOTY_SEARCH_MODEL_EXTRA1, reviewArrayList);
                getActivity().overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
                startActivity(detailIntent);
        }
    }

    private void requestSearchLocal(double x, double y) {
        groupId = getActivity().getIntent().getStringExtra("groupId");
        postList.clear();
        pinList.clear();

        reference = database.getReference("Post").child(groupId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);

                        placeRef = database.getReference("Place").child(post.getGroupId());
                        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    reviewArrayList.clear();
                                    for(DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                        Document document = dataSnapshot1.getValue(Document.class);
                                        reviewArrayList.add(document);
                                        MapPOIItem marker = new MapPOIItem();
                                        marker.setItemName(document.getPlaceName());
                                        double x1 = Double.parseDouble(document.getY());
                                        double y1 = Double.parseDouble(document.getX());
                                        //??????????????? ????????? new MapPoint()???  ????????????. ??????????????? ???????????? ????????? ???????????? ???????????????
                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x1, y1);
                                        marker.setMapPoint(mapPoint);
                                        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // ??????????????? ????????? ????????? ??????.
                                        marker.setCustomImageResourceId(R.drawable.marker_basic_35); // ?????? ?????????.
                                        marker.setCustomImageAutoscale(false); // hdpi, xhdpi ??? ??????????????? ???????????? ???????????? ????????? ?????? ?????? ?????????????????? ????????? ????????? ??????.
                                        marker.setCustomImageAnchor(0.5f, 1.0f); // ?????? ???????????? ????????? ?????? ??????(???????????????) ?????? - ?????? ????????? ?????? ?????? ?????? x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) ???.
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

    //??? ?????? ????????? ??????
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        //???????????????????????? ??????????????? ????????? ???????????????
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

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    //?????????(POLLITEM) ????????? ??????
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
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("userId", userId);
                    intent.putExtra("mCurrentLat", mCurrentLat);
                    intent.putExtra("mCurrentLng", mCurrentLng);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {
                mLoaderLayout.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlaceDetailActivity.class);
                startActivity(intent);
            }
        });
    }

    // ?????? ?????????????????? ??????
    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        mSearchName = "???????????? ??????";
        mSearchLng = mapPointGeo.longitude;
        mSearchLat = mapPointGeo.latitude;
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
        searchMarker.setItemName(mSearchName);
        MapPoint mapPoint2 = MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng);
        searchMarker.setMapPoint(mapPoint2);
        searchMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
        searchMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
        searchMarker.setDraggable(true);
        mMapView.addPOIItem(searchMarker);
    }

    /*
     *  ?????? ?????? ????????????(setCurrentLocationEventListener)
     */
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        //??? ????????? ?????? ?????? ??????
        mMapView.setMapCenterPoint(currentMapPoint, true);
        //??????????????? ?????? ?????? ??????
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        //?????? ?????????
        mapView.removeAllPOIItems();
        mapView.removeAllCircles();
        requestSearchLocal(mCurrentLng, mCurrentLat);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        Log.d(TAG, "???????????? => " + mCurrentLat + "  " + mCurrentLng);
        mLoaderLayout.setVisibility(View.GONE);
        fab1.setEnabled(true);
        fab2.setEnabled(true);
        fabStat.setEnabled(true);
        //????????? ????????? ?????? ?????? ???????????? ??????????????? ??????, ????????? ?????? ?????????????????? ???????????? ??????????????? ?????? ??????
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

    @Subscribe //???????????? ????????? ????????? ????????????
    public void search(Document document) {//public?????? ???????????????
        Toast.makeText(this.getContext(), document.getPlaceName() + " ??????", Toast.LENGTH_SHORT).show();
//        FancyToast.makeText(getActivity().getApplicationContext(), document.getPlaceName() + " ??????", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
        mSearchName = document.getPlaceName();
        mSearchLng = Double.parseDouble(document.getX());
        mSearchLat = Double.parseDouble(document.getY());
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
        mMapView.removePOIItem(searchMarker);
        searchMarker.setItemName(mSearchName);
        searchMarker.setTag(10000);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng);
        searchMarker.setMapPoint(mapPoint);
        searchMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
        searchMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
        //?????? ????????? ???????????? ??????
        searchMarker.setDraggable(true);
        mMapView.addPOIItem(searchMarker);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView(view);
                } else {
                    // ?????? ?????? ??????
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext());
                    alertDialog.setTitle("??? ??????");
                    alertDialog.setMessage("?????? ?????? ?????? ????????? ?????????????????? ?????????????????? ??????>???????????? ?????? ????????? ????????? ????????????");
                    // ???????????? ?????? ??? ????????? ??????
                    alertDialog.setPositiveButton("?????? ??????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getActivity().getApplicationContext().getPackageName()));
                                    startActivity(intent);
                                    dialog.cancel();
                                }
                            });
                    //??????
                    alertDialog.setNegativeButton("??????",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    Toast.makeText(getContext(), "?????? ?????? ???????????? ???????????????.", Toast.LENGTH_LONG).show();
                                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                    FeedActivity feedActivity = new FeedActivity();
                                    transaction.replace(R.id.main_frame, feedActivity);
                                }
                            });
                    alertDialog.show();
                }
                return;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this); //??? ???????????? ????????? ????????? ????????????
    }
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        bus.unregister(this); //??????????????? ????????? ????????? ????????????
//    }
}
