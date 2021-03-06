package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Map.IntentKey;
import com.example.wookie.Map.PlaceDetailActivity;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Post;
import com.example.wookie.Models.Reply;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ReadPostActivity extends AppCompatActivity {

    private String TAG = "ReadPostActivity";
    private static final int BAD=1, GOOD=2, RECOMMEND=3;
    private ImageView userImage, postImage1, scoreImage;
    private TextView userName, postDate, postContent, placeNameTxt, placeAddressTxt, scoreTxt;
    private LinearLayout placeReview;
    private Button editDelBtn;
    private Button backBtn;
    private Dialog editDelDialog;
    private Button postEditBtn, postDelBtn;

    public static Context mContext;

    private String userLoginId;
    public String passUserLoginId;
    private TextView replyCountTxt;
    private ConstraintLayout replyTxtLayout;
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
    private NestedScrollView nestedScrollView;

    private String imageUrl;

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
        scoreTxt = findViewById(R.id.score_txt);
        editDelBtn = findViewById(R.id.edit_del_btn);
        backBtn = findViewById(R.id.back_btn);
        editDelBtn = findViewById(R.id.edit_del_btn);
        replyCountTxt = findViewById(R.id.reply_count_txt);
        replyTxtLayout = findViewById(R.id.reply_text_layout);
        replyEditTxt = findViewById(R.id.reply_edit_text);
        replySendBtn = findViewById(R.id.reply_send_btn);
        recyclerView = findViewById(R.id.recyclerView);
        nestedScrollView = findViewById(R.id.ScrollView);

        mContext = this;

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // ????????? ??????
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // PostAdapter, ReplyAdapter ?????? ???????????? ???
        final String groupId = getIntent().getStringExtra("groupId");
        final String postId = getIntent().getStringExtra("postId");

        setPost(groupId, postId); // ????????? ?????? ??????
        setReply(groupId, postId); // ?????? ?????? ??????

        // ?????? ????????? id ????????????
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//????????? ??????
                    userLoginId = String.valueOf(user.getId());
                    passUserLoginId = userLoginId;
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });

        postImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ShowImgFullActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                startActivity(intent);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
//                intent.putExtra("groupId", groupId);
//                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
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
                        postDel(groupId, postId); // db?????? ?????? ??? ??????
                        Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
                        intent.putExtra("groupId", groupId);
                        startActivity(intent);
                        editDelDialog.dismiss();
                        finish();
                    }
                });
            }
        });

        replyEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // ?????? ????????? ????????? ?????? ???????????? ?????? ??????
                int height = replyTxtLayout.getHeight();
                LinearLayout.LayoutParams scrollViewParams = (LinearLayout.LayoutParams) nestedScrollView.getLayoutParams();
                scrollViewParams.bottomMargin = height;
                nestedScrollView.setLayoutParams(scrollViewParams);
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
        replyList = new ArrayList<>(); // Reply ????????? ?????? Array?????????

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
                    replyCountTxt.setText("?????? " + replyCount+"???");
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

    //????????? ??????
    private void postDel(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);
        reference.child(postId).removeValue();
        //TODO: ?????????????????? storage ???????????? ?????? ??????
    }


    // ????????? ?????? ?????? (?????????, ??? ??????)
    private void setPost(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId).child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Post post = dataSnapshot.getValue(Post.class);

                    String userId = post.getUserId();
                    setPostUser(userId); // ??? ????????? ???????????????, ?????? ??????
                    setEditDelBtn(userId); // ??? ???????????? ??????
                    postDate.setText(parseDate(post.getPostDate())); // ??? ???????????? ??????
                    postContent.setText(post.getContext()); // ??? ?????? ??????
                    imageUrl = post.getPostImg();
                    if(!(imageUrl.equals("null"))){ // ??? ????????? ??????
                        postImage1.setVisibility(View.VISIBLE);
                        Glide.with(postImage1).load(imageUrl)
                                .transform(new CenterCrop(),new RoundedCorners(25)).into(postImage1);
                    }
                    if(post.isReview()){ // ??? ?????? ?????? ??????
                        placeNameTxt = findViewById(R.id.placeName_txt);
                        placeAddressTxt = findViewById(R.id.placeAddress_txt);
                        DatabaseReference placeRef = database.getReference("Place").child(groupId).child(post.getPlaceId());
                        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Document place = snapshot.getValue(Document.class);
                                placeNameTxt.setText(place.getPlaceName());
                                placeAddressTxt.setText(place.getRoadAddressName());
                                placeNameTxt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ReadPostActivity.this, PlaceDetailActivity.class);
                                        intent.putExtra(IntentKey.PLACE_SEARCH_DETAIL_EXTRA, place);
                                        intent.putExtra("groupId", groupId);
                                        intent.putExtra("userId", userId);
//                                        intent.putExtra("lat", place.getX());
//                                        intent.putExtra("lng", place.getY());
//                                        Log.e(TAG, place.getX()+" "+place.getY());
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        placeReview.setVisibility(View.VISIBLE);
                        int score = post.getScore();
                        scoreTxt.setText(Integer.toString(score)+".0");
                        if (score > 0 && score <= 1) {
                            scoreImage.setImageDrawable(getDrawable(R.drawable.bad_rating));
                        } else if (score > 1 && score < 4) {
                            scoreImage.setImageDrawable(getDrawable(R.drawable.good_rating));
                        } else if (score >= 4 && score <= 5) {
                            scoreImage.setImageDrawable(getDrawable(R.drawable.ic_rating_fill));
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

    // ???(minute)????????? ??????
    private String parseDate(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

        Date oldDate = inputFormat.parse(date, new ParsePosition(0));
        String newDate = outputFormat.format(oldDate);

        return newDate;
    }

    // ?????? ???????????? ?????? ????????? ?????? ????????? ??????
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

    // ??? ????????? ???????????????, ?????? ??????
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

//    @Override
//    public void onBackPressed() {
//        final String groupId = getIntent().getStringExtra("groupId");
//        Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
//        intent.putExtra("groupId", groupId);
//        startActivity(intent);
//        finish();
//    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
    }
}
