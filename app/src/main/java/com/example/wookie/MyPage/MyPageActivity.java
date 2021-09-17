package com.example.wookie.MyPage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.LoginActivity;
import com.example.wookie.R;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MyPageActivity extends Fragment {

    private String TAG = "MyPageActivity";
    private View view;
    private ImageView userImageView;
    private TextView userNameTxt, postCntTxt;
    private Button myMapBtn;

    public MyPageActivity(){
        // 비어있는 constructor 필요
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_mypage, container, false);

        myMapBtn = view.findViewById(R.id.my_map_btn);
        userImageView = view.findViewById(R.id.user_image);
        userNameTxt = view.findViewById(R.id.user_name);
        postCntTxt = view.findViewById(R.id.post_count);

        setUserData();

        myMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //보낼 데이터 있을 경우 주석 풀어서 쓰기
                //Bundle bundle = new Bundle();
                //bundle.putString("fromFeedActivity", "여기에는 글 ID 같은 거");
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MyMapActivity myMapActivity = new MyMapActivity();
                //readPostActivity.setArguments(bundle);
                transaction.replace(R.id.main_frame, myMapActivity);
                transaction.commit(); //fragment 상태 저장
            }
        });
        return view;
    }
    private void setUserData(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//로그인 되면
                    userNameTxt.setText(user.getKakaoAccount().getProfile().getNickname());
                    Glide.with(userImageView).load(user.getKakaoAccount().getProfile().getThumbnailImageUrl()).circleCrop().into(userImageView);
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
}
