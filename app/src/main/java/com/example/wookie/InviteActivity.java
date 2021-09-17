package com.example.wookie;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

import java.util.HashMap;
import java.util.Map;


public class InviteActivity extends AppCompatActivity {
    private Button sendBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        sendBtn = findViewById(R.id.btnSend);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextTemplate params = TextTemplate
                        .newBuilder("김유진님이 우리끼리방에 초대했습니다.",
                                LinkObject.newBuilder().setMobileWebUrl("https://play.google.com/store/apps/details?id=com.example.wookie").build())
                        .addButton(new ButtonObject("그룹 초대 수락", LinkObject.newBuilder()
                                .setMobileWebUrl("https://play.google.com/store/apps/details?id=com.example.wookie").build()))
                        .build();
//                FeedTemplate params = FeedTemplate
//                        .newBuilder(ContentObject.newBuilder("우리끼리",
//                                "https://이미지url",
//                                LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
//                                        .setMobileWebUrl("https://developers.kakao.com").build())
//                                .setDescrption("그룹에 초대합니다")
//                                .build())
//                        .addButton(new ButtonObject("그룹 초대 수락", LinkObject.newBuilder()
//                                .setWebUrl("https://developers.kakao.com")
//                                .setMobileWebUrl("https://developers.kakao.com")
//                                .setAndroidExecutionParams("key1=value1")
//                                .setIosExecutionParams("key1=value1")
//                                .build()))
//                        .build();
//
//                Map<String, String> serverCallbackArgs = new HashMap<String, String>();
//                serverCallbackArgs.put("user_id", "${current_user_id}");


                KakaoLinkService.getInstance().sendDefault(InviteActivity.this, params, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("test","실패");
                    }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        Log.e("test","성공");
                    }
                });
            }
        });

    }
}
