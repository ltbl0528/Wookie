package com.example.wookie.Feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.Models.Group;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedActivity extends Fragment {

    private String TAG = "FeedActivity";
    private View view;
    private TextView groupNameTxt, postNoneTxt;
    private ImageButton addUserBtn;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Post> postList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Query query;
    private Group group;
    private String userId, userLoginName;

    public FeedActivity(){
        // ???????????? constructor ??????
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_feed, container, false);

        groupNameTxt = view.findViewById(R.id.group_name);
        postNoneTxt = view.findViewById(R.id.post_none_txt);
        addUserBtn = view.findViewById(R.id.add_user_btn);
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // ????????? ??????
        layoutManager = new LinearLayoutManager(this.getContext());
        // ?????????????????? ??????
        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        final String groupId = getActivity().getIntent().getStringExtra("groupId");

        setGroupName(groupId); // ?????? ??????
        setFeed(groupId); // ?????? ?????? ??????

        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedTemplate params = FeedTemplate
                        .newBuilder(ContentObject.newBuilder(userLoginName+"?????? " + group.getGroupName()+"?????? ??????????????????.",
                                group.getGroupImg(),
                                LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
                                        .setMobileWebUrl("https://developers.kakao.com").build())
                                .setDescrption(
                                        "???????????? : " + group.getInviteCode()+"\n"+
                                                "???????????? : " + group.getGroupPwd())
                                .build())
                        .addButton(new ButtonObject("?????? ????????????", LinkObject.newBuilder()
                                .setWebUrl("https://developers.kakao.com")
                                .setMobileWebUrl("https://developers.kakao.com")
                                .setAndroidExecutionParams("key1=value1")
                                .setIosExecutionParams("key1=value1")
                                .build()))
                        .build();
//
//                Map<String, String> serverCallbackArgs = new HashMap<String, String>();
//                serverCallbackArgs.put("user_id", "${current_user_id}");


                KakaoLinkService.getInstance().sendDefault(getContext(), params, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) { Log.e(TAG, "???????????? ?????? ??????"); }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {
                        Log.e(TAG, "???????????? ?????? ??????");
                    }
                });

            }
        });

        return view;
    }

    //?????? ?????? ??????
    private void setFeed(String groupId) {
        postList = new ArrayList<>(); // Post ????????? ?????? Array?????????

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Post post = snapshot.getValue(Post.class);
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                }
                else{
                    postNoneTxt.setVisibility(View.VISIBLE);
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

    // ?????? ??????
    private void setGroupName(String groupId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("group");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String groupName = dataSnapshot.child(groupId).child("groupName").getValue().toString();
                    groupNameTxt.setText(groupName);

                    UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                        @Override
                        public Unit invoke(User user, Throwable throwable) {
                            if (user != null){
                                userId = String.valueOf(user.getId());
                                userLoginName = user.getKakaoAccount().getProfile().getNickname();
                                if(userId.equals(dataSnapshot.child(groupId).child("groupAdminId").getValue().toString())){
                                    addUserBtn.setVisibility(View.VISIBLE);
                                }
                            }
                            else{
                                Toast.makeText(getContext(),"????????? ????????????", Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    });

                    query = database.getReference("group").orderByChild("groupId").equalTo(groupId);

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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }
}
