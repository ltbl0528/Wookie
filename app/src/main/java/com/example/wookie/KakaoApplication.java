package com.example.wookie;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this,"6ec2a9b1aa3163fb2406386466af4038");
    }
}
