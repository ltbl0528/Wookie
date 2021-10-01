package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Feed.PostAdapter;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Group;
import com.example.wookie.Models.GroupMem;
import com.example.wookie.Models.Post;
import com.example.wookie.Models.Reply;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ReadPostActivity extends AppCompatActivity {

    private String TAG = "ReadPostActivity";
    private static final int BAD=1, GOOD=2, RECOMMEND=3;
    private ImageView userImage, postImage1, scoreImage;
    private TextView userName, postDate, postContent, placeNameTxt, placeAddressTxt;
    private LinearLayout placeReview;
    private Button editDelBtn;
    private Button backBtn;
    private Dialog editDelDialog;
    private Button postEditBtn, postDelBtn;

    private String userLoginId;
    private TextView replyCountTxt;
    private EditText replyEditTxt;
    private ImageView replySendBtn;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Reply> replyList;

    private FirebaseDatabase database;
    private DatabaseReference reference;
//    private FirebaseStorage storage;
//    private StorageReference storageReference;
    //TODO: 댓글 작성창 height에 따라 스크롤뷰 height marginBottom 실시간으로 수정되게
    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_read);

        userImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.user_name);
        postDate = findViewById(R.id.post_time_stamp);
        postContent = findViewById(R.id.post_content);
        postImage1 = findViewById(R.id.post_image1);
        placeReview = findViewById(R.id.placeReview_layout);
        scoreImage = findViewById(R.id.score_image);
        editDelBtn = findViewById(R.id.edit_del_btn);
        backBtn = findViewById(R.id.back_btn);
        editDelBtn = findViewById(R.id.edit_del_btn);
        replyCountTxt = findViewById(R.id.reply_count_txt);
        replyEditTxt = findViewById(R.id.reply_edit_text);
        replySendBtn = findViewById(R.id.reply_send_btn);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // PostAdapter에서 넘겨받은 값
        final String groupId = getIntent().getStringExtra("groupId");
        final String postId = getIntent().getStringExtra("postId");

        setPost(groupId, postId); // 작성글 내용 설정
        setReply(groupId, postId); // 댓글 목록 설정

        // 현재 사용자 id 받아오기
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//로그인 되면
                    userLoginId = String.valueOf(user.getId());
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
                finish();
            }
        });

        editDelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editDelDialog= new Dialog(ReadPostActivity.this);
                editDelDialog.setContentView(R.layout.dialog_edit_del);
                editDelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                editDelDialog.show();

                postEditBtn = editDelDialog.findViewById(R.id.post_edit_btn);
                postDelBtn = editDelDialog.findViewById(R.id.post_del_btn);

                postEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), EditPostActivity.class);
                        intent.putExtra("groupId", groupId);
                        intent.putExtra("postId", postId);
                        startActivity(intent);
                        editDelDialog.dismiss();
                        finish();
                    }
                });

                postDelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        postDel(groupId, postId); // db에서 해당 글 삭제
                        Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
                        intent.putExtra("groupId", groupId);
                        startActivity(intent);
                        editDelDialog.dismiss();
                        finish();
                    }
                });
            }
        });

        replySendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(replyEditTxt.getText().toString().trim() != null){
                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("reply").child(groupId);

                    Reply reply = new Reply();
                    reply.setReplyId(reference.push().getKey());
                    reply.setGroupId(groupId);
                    reply.setPostId(postId);
                    reply.setUserId(userLoginId);
                    reply.setReplyContext(replyEditTxt.getText().toString());
                    reply.setReplyDate(getDate());

                    reference.child(reply.getReplyId()).setValue(reply);
                }
                Intent intent = new Intent(getApplicationContext(), ReadPostActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("postId", postId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setReply(String groupId, String postId) {
        replyList = new ArrayList<>(); // Reply 객체를 담을 Array리스트

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("reply").child(groupId);
        Query query = reference.orderByChild("postId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int replyCount = 0;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Reply reply = snapshot.getValue(Reply.class);
                        replyList.add(reply);
                        replyCount++;
                    }
                    adapter.notifyDataSetChanged();
                    replyCountTxt.setText("댓글 " + replyCount+"개");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        adapter = new ReplyAdapter(replyList, this);
        recyclerView.setAdapter(adapter);
    }

    private String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String date = sdf.format(cal.getTime());

        return date;
    }

    //게시글 삭제
    private void postDel(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);
        reference.child(postId).removeValue();
        //TODO: 파이어베이스 storage 이미지도 같이 삭제
    }


    // 작성글 내용 설정 (작성자, 글 내용)
    private void setPost(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId).child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Post post = dataSnapshot.getValue(Post.class);

                    String userId = post.getUserId();
                    setPostUser(userId); // 글 작성자 프로필사진, 이름 설정
                    setEditDelBtn(userId); // 글 수정버튼 설정
                    postDate.setText(post.getPostDate()); // 글 작성날짜 설정
                    postContent.setText(post.getContext()); // 글 내용 설정
                    if(!(post.getPostImg().equals("null"))){ // 글 이미지 설정
                        postImage1.setVisibility(View.VISIBLE);
                        Glide.with(postImage1).load(post.getPostImg())
                                .transform(new CenterCrop(),new RoundedCorners(25)).into(postImage1);
                    }
                    if(post.isReview()){ // 글 장소 리뷰 설정
                        placeNameTxt = findViewById(R.id.placeName_txt);
                        placeAddressTxt = findViewById(R.id.placeAddress_txt);
                        DatabaseReference placeRef = database.getReference("Place").child(groupId).child(post.getPlaceId());
                        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Document place = snapshot.getValue(Document.class);
                                placeNameTxt.setText(place.getPlaceName());
                                placeAddressTxt.setText(place.getRoadAddressName());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        placeReview.setVisibility(View.VISIBLE);
                        switch (post.getScore()){
                            case BAD:
                                scoreImage.setImageDrawable(getDrawable(R.drawable.bad));
                                break;
                            case GOOD:
                                scoreImage.setImageDrawable(getDrawable(R.drawable.good));
                                break;
                            case RECOMMEND:
                                scoreImage.setImageDrawable(getDrawable(R.drawable.recommend));
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

    }

    // 본인 작성글일 경우 글수정 버튼 보이게 설정
    private void setEditDelBtn(String userId) {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){
                    String userLoginId = String.valueOf(user.getId());
                    if (userLoginId.equals(userId)){
                        editDelBtn.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });

    }

    // 글 작성자 프로필사진, 이름 설정
    private void setPostUser(String userId){
        Query query = database.getReference("users").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child(userId).child("userName").getValue().toString();
                String profile = snapshot.child(userId).child("userProfile").getValue().toString();

                userName.setText(name);
                Glide.with(userImage).load(profile).circleCrop().into(userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        final String groupId = getIntent().getStringExtra("groupId");
        Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
        finish();
    }
}
