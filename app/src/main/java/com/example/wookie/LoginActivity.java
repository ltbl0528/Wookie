package com.example.wookie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;


import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";
    private View loginBtn;

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
                //카카오톡 설치 되었는지? 안녕?
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
                    //id = user.getId()
                    //userProfile = user.getKakaoAccount().getProfile();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // intent.putExtra("id", user.getId());
                    startActivity(intent);
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
    // 로딩 스피너
//    public void showProgressDialog(){
//        ProgressDialog dialog = new ProgressDialog(this);
//        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        dialog.setMessage("잠시만 기다려주세요...");
//        dialog.show();
//    }
}
