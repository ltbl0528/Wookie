package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Group.GroupListActivity;
import com.example.wookie.Models.GroupMem;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class ReadPostActivity extends AppCompatActivity {

    private String TAG = "ReadPostActivity";

    private ImageView userImage, postImage1;
    private TextView userName, postDate, postContent;
    private LinearLayout placeReview;
    private Button editDelBtn;
    private Button backBtn;
    private Dialog editDelDialog;
    private Button postEditBtn, postDelBtn;

    private FirebaseDatabase database;
    private DatabaseReference reference;

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
        editDelBtn = findViewById(R.id.edit_del_btn);
        backBtn = findViewById(R.id.back_btn);
        editDelBtn = findViewById(R.id.edit_del_btn);

        // PostAdapter에서 넘겨받은 값
        final String groupId = getIntent().getStringExtra("groupId");
        final String postId = getIntent().getStringExtra("postId");

        setPost(groupId, postId); // 작성글 내용 설정

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        editDelDialog.dismiss();
                    }
                });

                postDelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        postDel(groupId, postId);
                        editDelDialog.dismiss();
                    }
                });
            }
        });
    }

    //게시글 삭제
    private void postDel(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId);
        reference.child(postId).removeValue();
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
                    postDate.setText(post.getPostDate()); // 글 작성날짜 설정
                    postContent.setText(post.getContext()); // 글 내용 설정
                    if(!(post.getPostImg().equals("null"))){ // 글 이미지 설정
                        postImage1.setVisibility(View.VISIBLE);
                        Glide.with(postImage1).load(post.getPostImg())
                                .transform(new CenterCrop(),new RoundedCorners(25)).into(postImage1);
                    }
                    if(post.isReview()){ // 글 장소 리뷰 설정
                        placeReview.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
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

}
