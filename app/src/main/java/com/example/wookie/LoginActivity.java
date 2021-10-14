package com.example.wookie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.Models.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;


import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";
    private String userLoginId, userName, userProfile;
    private View loginBtn;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.login);

        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) { //토큰이 전달 됐으면 로그인 성공, 전달 실패면 로그인 실패
                if (oAuthToken != null) { //로그인 됐을 때 수행해야 할 일들을 수행

                }
                if (throwable != null) { //결과에 오류가 있을 경우
                    Log.e(TAG, throwable.toString());
                    finish();
                }
                checkKakaoLoginUi();
                return null;
            }
        };

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //카카오톡 설치 되었는지?
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)){
                    UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                }
                else{ //카카오톡 설치 x -> 카카오 홈페이지를 통해 로그인 후 결과에 대한 처리는 설치된 것과 동일하게 콜백으로 전달
                    UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
            }
        });
    }

    private void checkKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//로그인 되면

                    userLoginId = String.valueOf(user.getId());
                    userName = user.getKakaoAccount().getProfile().getNickname();
                    userProfile = user.getKakaoAccount().getProfile().getProfileImageUrl();

                    hasUserId(userLoginId, userName, userProfile);

                    Intent intent = new Intent(LoginActivity.this, GroupListActivity.class);
//                    intent.putExtra("uid", userLoginId);
//                    intent.putExtra("uName", userName);
//                    intent.putExtra("uProfile", userProfile);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    finish();
                    Log.e(TAG, "성공");
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });
    }

    private void hasUserId(String uid, String name, String profile){
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("users");

        Query queryID = reference.orderByChild("user_id").equalTo(uid);

        queryID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.e(TAG,"OK");
                }
                else {
                    Users users = new Users(uid, name, profile);
                    reference.child(uid).setValue(users);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    // 로딩 스피너
//    public void showProgressDialog(){
//        ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        dialog.setMessage("잠시만 기다려주세요...");
//        dialog.show();
//    }
}
