package com.example.wookie.Post;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Group;
import com.example.wookie.Models.GroupMem;
import com.example.wookie.Models.Post;
import com.example.wookie.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class EditPostActivity extends AppCompatActivity {

    private String TAG = "EditPostActivity";
    private TextView topBarTxt, scoreText;
    private Button cancelBtn, selectImgBtn, selectPlaceBtn;
    private TextView postSubmitBtn, placeName;
    private EditText postEditTxt;
    private RelativeLayout postImgLayout;
    private ImageView postImg1;
    private ImageButton delPostImg1Btn;
    private CheckBox addToGallery;

    private FirebaseDatabase database;
    private DatabaseReference reference, placeRef;;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String userLoginId;
    private Post post;
    private Document placeInfo = new Document();

//    private static final int BAD=1, GOOD=2, RECOMMEND=3;
    private ConstraintLayout placeReview;
    private ImageView deletePlaceBtn;
    private Dialog scoreDialog;
    public static Context mContext;
    public Post passPost;
    public String passEditText, passGroupId, passPostId, placeID;
    public boolean passIsImg;
    public Uri passUri, passedUri;
    private ImageView scoreImg;

    private boolean isNewImg=false, isReView=false, isReViewEdit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write);

        topBarTxt = findViewById(R.id.topBar_txt);
        scoreText = findViewById(R.id.score_txt);
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
        placeName = findViewById(R.id.placeName_txt);
        deletePlaceBtn = findViewById(R.id.delete_place_btn);
        scoreDialog= new Dialog(EditPostActivity.this);
        scoreDialog.setContentView(R.layout.dialog_score);
        scoreImg = findViewById(R.id.score_image);

        // ?????? ?????????id ????????????
        mContext = this;

        // ReadPostActivity?????? ????????? ???
        final String groupId = getIntent().getStringExtra("groupId");
        final String postId = getIntent().getStringExtra("postId");

        if(getIntent().getParcelableExtra("placeInfo") != null){
            placeInfo = getIntent().getParcelableExtra("placeInfo");
            post = (Post) getIntent().getSerializableExtra("post");
            final int score = getIntent().getIntExtra("score",0);
            final boolean isReview = getIntent().getBooleanExtra("isReview", false);
            final String passedText = getIntent().getStringExtra("editTxt");
            passedUri = getIntent().getParcelableExtra("imgUri");
            isNewImg = getIntent().getBooleanExtra("isNewImg", false);
            isReViewEdit = true;

            if(passedText != null){
                postEditTxt.setText(passedText);
                postSubmitBtn.setEnabled(true);
                postSubmitBtn.setTextColor(Color.BLUE);
            }

            if(passedUri != null){
                imageUri = passedUri;
                postImgLayout.setVisibility(View.VISIBLE);
                Glide.with(this).load(passedUri).transform(new CenterCrop(),new RoundedCorners(25)).into(postImg1);
                // ???????????? ?????? ?????????
                addToGallery.setVisibility(View.VISIBLE);
            }


            if(placeInfo.getPlaceName() != null){
                placeName.setText(placeInfo.getPlaceName());
                scoreText.setText(Integer.toString(score)+".0");
                if (score > 0 && score <= 1) {
                    scoreImg.setImageDrawable(getDrawable(R.drawable.bad_rating));
                    placeReview.setVisibility(View.VISIBLE);
                    post.setScore(score);
                    post.setReview(isReview);
                } else if (score > 1 && score < 4) {
                    scoreImg.setImageDrawable(getDrawable(R.drawable.good_rating));
                    placeReview.setVisibility(View.VISIBLE);
                    post.setScore(score);
                    post.setReview(isReview);
                } else if (score >= 4 && score <= 5) {
                    scoreImg.setImageDrawable(getDrawable(R.drawable.ic_rating_fill));
                    placeReview.setVisibility(View.VISIBLE);
                    post.setScore(score);
                    post.setReview(isReview);
                }
            }
        }

        // ?????? ????????? id ????????????
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//????????? ??????
                    userLoginId = String.valueOf(user.getId());
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });

        topBarTxt.setText("?????????");
        postSubmitBtn.setText("??????");
        if(isReViewEdit == false){
            setPost(groupId, postId);
        }

        postEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0 && postEditTxt.getText().toString().trim() != null){ // ????????? ?????? ????????? ?????? ?????? ?????????
                    postSubmitBtn.setEnabled(true);
                    postSubmitBtn.setTextColor(Color.BLUE);

                } else {
                    postSubmitBtn.setEnabled(false);
                    postSubmitBtn.setTextColor(Color.GRAY);
                }
            }
        });

        // ????????? ??????(??????) ??????
        postSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("Post").child(groupId).child(postId);
                placeRef = database.getReference("Place").child(groupId);

//                post.setPostId(postId);
//                post.setGroupId(groupId);
//                post.setUserId(userLoginId);
                post.setContext(postEditTxt.getText().toString());
//                post.setPostDate(getDate());

                if(addToGallery.isChecked()){
                    post.setGallery(true);
                }
                else{
                    post.setGallery(false);
                }

                // ????????? ???????????? ?????? ?????? Storage??? ????????? ??? db??? ??????
                if(postImgLayout.getVisibility() == View.VISIBLE && isNewImg==true){
                    imageUpload(groupId, postId, imageUri);
                }
                else {
                    if(post.isReview() == true && isReView == false){
                        placeID = placeInfo.getId();
                        placeRef.child(placeID).setValue(placeInfo);
                        post.setPlaceId(placeID);
                        reference.setValue(post);
                    }
                    else{
                        reference.setValue(post);
                    }

                    //Toast.makeText(WritePostActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ReadPostActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("postId", postId);
                    startActivity(intent);
                    finish();
                }

            }
        });

        // ????????? ?????? ??????
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ???????????? ??????
        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        // ?????? ?????? ??????
        delPostImg1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNewImg = true;
                post.setPostImg("null");
                postImgLayout.setVisibility(View.GONE);
                post.setGallery(false);
                addToGallery.setVisibility(View.GONE);
            }
        });

        // ???????????? ??????
        selectPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditPostActivity.this, EditSelectPlaceActivity.class);
                if(postImgLayout.getVisibility() == View.VISIBLE){
                    passUri = imageUri;
                }
                if(!postEditTxt.getText().toString().trim().isEmpty()){
                    passEditText = postEditTxt.getText().toString();
                }
                passPost = post;
                passGroupId = groupId;
                passPostId = postId;
                passIsImg = isNewImg;
                startActivity(intent);
            }
        });


        // ?????? ?????? ??????
        deletePlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                post.setScore(0);
                post.setReview(false);
                placeReview.setVisibility(View.GONE);
            }
        });
    }

    // ????????? ??????
    private void setPost(String groupId, String postId) {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Post").child(groupId).child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    post = dataSnapshot.getValue(Post.class);
                    postEditTxt.setText(post.getContext());
                    if(!(post.getPostImg().equals("null"))){ // ??? ????????? ??????
                        isNewImg = false;
                        imageUri = Uri.parse(post.getPostImg());
                        postImgLayout.setVisibility(View.VISIBLE);
                        Glide.with(postImg1).load(post.getPostImg()).transform(new CenterCrop(),new RoundedCorners(25)).into(postImg1);
                        addToGallery.setVisibility(View.VISIBLE);
                    }
                    if(post.isReview()){ // ??? ?????? ?????? ??????
                        isReView = true;
                        placeReview.setVisibility(View.VISIBLE);
                        setPlaceNameTxt(groupId, postId);
                        int score = post.getScore();
                        scoreText.setText(Integer.toString(score)+".0");
                        if (score > 0 && score <= 1) {
                            scoreImg.setImageDrawable(getDrawable(R.drawable.bad_rating));
                        } else if (score > 1 && score < 4) {
                            scoreImg.setImageDrawable(getDrawable(R.drawable.good_rating));
                        } else if (score >= 4 && score <= 5) {
                            scoreImg.setImageDrawable(getDrawable(R.drawable.ic_rating_fill));
                        }
                    }

                    if(post.isGallery()){
                        addToGallery.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "setPost:"+error.toString());
            }
        });
    }

    private void setPlaceNameTxt(String groupId, String postId) {
        reference = database.getReference("Place").child(groupId);
        Query query = reference.orderByChild("id").equalTo(post.getPlaceId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Document placeInfo = snapshot.getValue(Document.class);
                        placeName.setText(placeInfo.getPlaceName());
                        Log.e(TAG, "?????????"+placeInfo.getPlaceName());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ??????+?????? ??????
    private String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String date = sdf.format(cal.getTime());
//        //????????? long ???????????? ???????????? ??????
//        Date curDate = sdf.parse(date, new ParsePosition(0));
//        Long curDateLong = curDate.getTime();

        return date;
    }

    private void openFileChooser(){ //?????? ??????
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //?????? ?????? ?????? ??????????????? ??????
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null){
            imageUri = data.getData();

            // ?????? ?????? ???????????? ????????? ?????????
            postImgLayout.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUri).transform(new CenterCrop(),new RoundedCorners(25)).into(postImg1);
            // ???????????? ?????? ?????????
            addToGallery.setVisibility(View.VISIBLE);

            isNewImg = true;

        }
    }

//    private String getFileExtension(Uri uri){ //?????? ???????????? ????????????
//        ContentResolver cr = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        return mime.getExtensionFromMimeType(cr.getType(uri));
//    }

    // ????????? ????????? + ????????? db??? ??????
    private void imageUpload(String groupId, String postId, Uri imageUri){

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        StorageReference fileRef = storageReference.child("postImg/" + groupId + "/" + postId);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                post.setPostImg(uri.toString());
                                //Toast.makeText(WritePostActivity.this, "??????,????????? ????????? ", Toast.LENGTH_SHORT).show();
                                if(post.isReview() == true && isReView == false){
                                    placeID = placeInfo.getId();
                                    placeRef.child(placeID).setValue(placeInfo);
                                    post.setPlaceId(placeID);
                                    reference.setValue(post);
                                }
                                else{
                                    reference.setValue(post);
                                }
                                Intent intent = new Intent(getApplicationContext(), ReadPostActivity.class);
                                intent.putExtra("groupId", groupId);
                                intent.putExtra("postId", postId);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPostActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}