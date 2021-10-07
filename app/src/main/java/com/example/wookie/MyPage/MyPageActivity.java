package com.example.wookie.MyPage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wookie.Feed.FeedActivity;
import com.example.wookie.Feed.PostAdapter;
import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.LoginActivity;
import com.example.wookie.Models.Post;
import com.example.wookie.Post.EditPostActivity;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MyPageActivity extends Fragment {

    private String TAG = "MyPageActivity";
    private View view;
    private ImageView userImageView;
    private TextView userNameTxt, postCntTxt;
    private Button myMapBtn, logOutBtn;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> postList;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String userLoginId;

    public MyPageActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_mypage, container, false);

        myMapBtn = view.findViewById(R.id.my_map_btn);
        logOutBtn = view.findViewById(R.id.logout_btn);
        userImageView = view.findViewById(R.id.user_image);
        userNameTxt = view.findViewById(R.id.user_name);
        postCntTxt = view.findViewById(R.id.post_count);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this.getContext());
        // 내림차순으로 정렬
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        final String groupId = getActivity().getIntent().getStringExtra("groupId");

//        setUserData();
//        setUserFeed(groupId);
        setMyPage(groupId);


        myMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //보낼 데이터 있을 경우 주석 풀어서 쓰기
                Bundle bundle = new Bundle();
                bundle.putString("groupId", groupId);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MyMapActivity myMapActivity = new MyMapActivity();
                myMapActivity.setArguments(bundle);
                transaction.replace(R.id.main_frame, myMapActivity);
                transaction.commit(); //fragment 상태 저장
            }
        });
        // 로그아웃 버튼
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("로그아웃");
                alertDialog.setMessage("정말 로그아웃 하시겠습니까?");
                // 권한설정 클릭 시 이벤트 발생
                alertDialog.setPositiveButton("로그아웃",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                                    @Override
                                    public Unit invoke(Throwable throwable) {
                                        if (throwable != null) {
                                            Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", throwable);
                                        }
                                        else {
                                            Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨");
                                            ActivityCompat.finishAffinity(getActivity());
                                            Intent intent = new Intent(view.getContext(), LoginActivity.class);
                                            view.getContext().startActivity(intent);
                                        }
                                        return null;
                                    }
                                });
                            }
                        });
                //취소
                alertDialog.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
            }
        });


        return view;
    }

    private void setMyPage(String groupId){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){
                    userLoginId = String.valueOf(user.getId());
                    // 프로필 정보 설정
                    userNameTxt.setText(user.getKakaoAccount().getProfile().getNickname());
                    Glide.with(userImageView).load(user.getKakaoAccount().getProfile().getThumbnailImageUrl()).circleCrop().into(userImageView);
                    // 피드에 본인 작성글 출력
                    setUserFeed(groupId);
                }
                else{
                    userNameTxt.setText(null);
                    userImageView.setImageBitmap(null);
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });
    }

    private void setUserFeed(String groupId) {
        postList = new ArrayList<>(); // Post 객체를 담을 Array리스트

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);

        Query query = reference.orderByChild("userId").equalTo(userLoginId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);
                    }
                    postCntTxt.setText("작성글 " + postList.size());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        adapter = new PostAdapter(postList, this.getContext());
        recyclerView.setAdapter(adapter);
    }

//    private void setUserData(String groupId){
//        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
//            @Override
//            public Unit invoke(User user, Throwable throwable) {
//                if (user != null){//로그인 되면
//                    userLoginId = String.valueOf(user.getId());
//                    userNameTxt.setText(user.getKakaoAccount().getProfile().getNickname());
//                    Glide.with(userImageView).load(user.getKakaoAccount().getProfile().getThumbnailImageUrl()).circleCrop().into(userImageView);
//                    setUserFeed(groupId);
//                }
//                else{
//                    userNameTxt.setText(null);
//                    userImageView.setImageBitmap(null);
//                    Log.e(TAG, throwable.toString());
//                }
//                return null;
//            }
//        });
//    }

}
