package com.example.wookie;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wookie.Models.Group;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class InviteActivity extends AppCompatActivity {
    private String TAG = "InviteActivity";
    private Button sendBtn;

    private Group group;
    private String userLoginName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        sendBtn = findViewById(R.id.btnSend);

        final String groupId = getIntent().getStringExtra("groupId");
        setUserInfo();
        setGroupInfo(groupId);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                TextTemplate params = TextTemplate
//                        .newBuilder("김유진님이 우리끼리방에 초대했습니다.",
//                                LinkObject.newBuilder().setMobileWebUrl("https://play.google.com/store/apps/details?id=com.example.wookie").build())
//                        .addButton(new ButtonObject("그룹 초대 수락", LinkObject.newBuilder()
//                                .setMobileWebUrl("https://play.google.com/store/apps/details?id=com.example.wookie").build()))
//                        .build();
                FeedTemplate params = FeedTemplate
                        .newBuilder(ContentObject.newBuilder(userLoginName+"님이 " + group.getGroupName()+"방에 초대했습니다.",
                                group.getGroupImg(),
                                LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
                                        .setMobileWebUrl("https://developers.kakao.com").build())
                                .setDescrption(
                                        "초대코드 : " + group.getInviteCode()+"\n"+
                                        "비밀번호 : " + group.getGroupPwd())
                                .build())
                        .addButton(new ButtonObject("그룹 가입하기", LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .setAndroidExecutionParams("key1=value1")
                                .setIosExecutionParams("key1=value1")
                                .build()))
                        .build();
//
//                Map<String, String> serverCallbackArgs = new HashMap<String, String>();
//                serverCallbackArgs.put("user_id", "${current_user_id}");


                KakaoLinkService.getInstance().sendDefault(InviteActivity.this, params, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) { Log.e(TAG, "초대링크 전송 실패"); }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        Log.e(TAG, "초대링크 전송 성공");
                    }
                });
            }
        });

    }

    private void setUserInfo() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//로그인 되면
                    userLoginName = user.getKakaoAccount().getProfile().getNickname();
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });
    }

    private void setGroupInfo(String groupId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference("group").orderByChild("groupId").equalTo(groupId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        group = snapshot.getValue(Group.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }
}
