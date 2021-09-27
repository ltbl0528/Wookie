package com.example.wookie.Post;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class WritePostActivity extends AppCompatActivity{

    private String TAG = "WritePostActivity";
    private Button cancelBtn, selectImgBtn, selectPlaceBtn;
    private TextView postSubmitBtn;
    private EditText postEditTxt;
    private RelativeLayout postImgLayout;
    private ImageView postImg1;
    private ImageButton delPostImg1Btn;
    private CheckBox addToGallery;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String userLoginId;
    private Post post = new Post(); // 생성과 동시에 초기화

    private ConstraintLayout placeReview;
    private ImageView deletePlaceBtn;
    private Dialog scoreDialog;
    private Button cancelDialogBtn, badBtn, goodBtn, recommendBtn;
    private ImageView scoreImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write);

        cancelBtn = findViewById(R.id.cancel_btn);
        postSubmitBtn = findViewById(R.id.post_submit_btn);
        selectImgBtn = findViewById(R.id.select_image_btn);
        selectPlaceBtn = findViewById(R.id.select_place_btn);
        postEditTxt = findViewById(R.id.post_edit_txt);
        postImgLayout = findViewById(R.id.post_image_layout);
        postImg1 = findViewById(R.id.post_image1);
        delPostImg1Btn = findViewById(R.id.delete_post_image1);
        addToGallery = findViewById(R.id.add_to_gallery);

        placeReview = findViewById(R.id.placeReview_layout);
        deletePlaceBtn = findViewById(R.id.delete_place_btn);
        scoreDialog= new Dialog(WritePostActivity.this);
        scoreDialog.setContentView(R.layout.dialog_score);
        scoreImg = findViewById(R.id.score_image);


        // 해당 그룹방id 받아오기
        final String groupId = getIntent().getStringExtra("groupId");

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

        postEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){ // 작성된 글이 있을시 게시 버튼 활성화
                    postSubmitBtn.setEnabled(true);
                    postSubmitBtn.setTextColor(Color.BLUE);

                } else {
                    postSubmitBtn.setEnabled(false);
                    postSubmitBtn.setTextColor(Color.GRAY);
                }
            }
        });

        // 작성글 저장(게시) 버튼
        postSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("Post").child(groupId);

                post.setPostId(reference.push().getKey());
                post.setGroupId(groupId);
                post.setUserId(userLoginId);
                post.setContext(postEditTxt.getText().toString());
                post.setPostDate(getDate());

                if(addToGallery.isChecked()){
                    post.setGallery(true);
                }

                // 이미지 등록되어 있을 경우 Storage에 업로드 후 db에 저장
                if(postImgLayout.getVisibility() == View.VISIBLE){
                    imageUpload(groupId, post.getPostId(), imageUri);
                }
                else {
                    reference.child(post.getPostId()).setValue(post);
                    //Toast.makeText(WritePostActivity.this, "게시글 업로드", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                    finish();
                }

            }
        });

        // 작성글 취소 버튼
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 사진추가 버튼
        //TODO: 사진 최대 4개 올릴 수 있게 수정 (레이아웃도 수정 필요)
        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // 사진 삭제 버튼
        delPostImg1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setPostImg("null");
                postImgLayout.setVisibility(View.GONE);
                post.setGallery(false);
                addToGallery.setVisibility(View.GONE);
            }
        });

        // 장소추가 버튼
        // TODO: 카카오맵 연동 필요 ( 현재 임의로 장소 평가 다이얼로그 띄움)
        selectPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scoreDialog.show();
                cancelDialogBtn = scoreDialog.findViewById(R.id.cancel_btn);
                badBtn = scoreDialog.findViewById(R.id.bad_btn);
                goodBtn = scoreDialog.findViewById(R.id.good_btn);
                recommendBtn = scoreDialog.findViewById(R.id.recommend_btn);

                //취소버튼 클릭 시
                cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scoreDialog.dismiss();
                    }
                });
                // 평가버튼 '나쁨' 선택
                badBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // bad 이미지로 설정한 후 작성 중인 글에 띄움
                        scoreImg.setImageDrawable(getDrawable(R.drawable.bad));
                        placeReview.setVisibility(View.VISIBLE);
                        post.setScore(1);
                        post.setReview(true);
                        scoreDialog.dismiss();
                    }
                });
                // 평가버튼 '좋음' 선택
                goodBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scoreImg.setImageDrawable(getDrawable(R.drawable.good));
                        placeReview.setVisibility(View.VISIBLE);
                        post.setScore(2);
                        post.setReview(true);
                        scoreDialog.dismiss();
                    }
                });
                // 평가버튼 '추천' 선택
                recommendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scoreImg.setImageDrawable(getDrawable(R.drawable.recommend));
                        placeReview.setVisibility(View.VISIBLE);
                        post.setScore(3);
                        post.setReview(true);
                        scoreDialog.dismiss();
                    }
                });

//                Intent intent = new Intent(WritePostActivity.this, SelectPlaceActivity.class);
//                startActivity(intent);
            }
        });


        // 장소 삭제 버튼
        deletePlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setScore(0);
                post.setReview(false);
                placeReview.setVisibility(View.GONE);
            }
        });
    }

    // 날짜+시간 반환
    private String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String date = sdf.format(cal.getTime());
//        //날짜를 long 타입으로 변환하는 방법
//        Date curDate = sdf.parse(date, new ParsePosition(0));
//        Long curDateLong = curDate.getTime();

        return date;
    }

    private void openFileChooser(){ //파일 선택
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //파일 선택 결과 이미지뷰에 세팅
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null){
            imageUri = data.getData();

            // 작성 중인 게시글에 이미지 띄우기
            postImgLayout.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).transform(new CenterCrop(),new RoundedCorners(25)).into(postImg1);
            // 앨범추가 버튼 띄우기
            addToGallery.setVisibility(View.VISIBLE);

        }
    }

    private String getFileExtension(Uri uri){ //파일 확장자명 받아내기
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    // 이미지 업로드 + 작성글 db에 저장
    private void imageUpload(String groupId, String postId, Uri imageUri){

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        StorageReference fileRef = storageReference.child("postImg/" + groupId + "/" + postId + "." + getFileExtension(imageUri));

        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                post.setPostImg(uri.toString());
                                //Toast.makeText(WritePostActivity.this, "사진,게시글 업로드 ", Toast.LENGTH_SHORT).show();
                                reference.child(postId).setValue(post);
                                Intent intent = new Intent(getApplicationContext(), BottomNaviActivity.class);
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WritePostActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }



}
