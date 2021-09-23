package com.example.wookie.Group;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.LoginActivity;
import com.example.wookie.Models.Group;
import com.example.wookie.Models.GroupMem;
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

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MakeGroupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private String TAG = "MakeGroupActivity";

    private ImageButton selectImgBtn;
    private Button groupMakeBtn, cancleBtn;
    private EditText groupName, groupPwd;

    private Uri imageUri;

    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String gName, gPwd, groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_make);

        selectImgBtn = findViewById(R.id.select_image_btn);
        groupMakeBtn = findViewById(R.id.group_make_btn);
        cancleBtn = findViewById(R.id.cancel_button);
        groupName = findViewById(R.id.input_group_name);
        groupPwd = findViewById(R.id.input_group_pwd);

        selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        groupMakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeGroup();
            }
        });

        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void openFileChooser(){ //파일 선택
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //파일 선택 결과 이미지 버튼에 세팅
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
                && data.getData() != null){
            imageUri = data.getData();

            Glide.with(this).load(imageUri).transform(new CenterCrop(),new RoundedCorners(25)).into(selectImgBtn);
        }
    }

    private void makeGroup() { // 그룹 이름 중복 확인 (같은 유저가 같은 그룹 이름의 Admin이 될 수 X) 및 그룹 생성

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("group");

        gName = groupName.getText().toString();
        gPwd = groupPwd.getText().toString();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String groupDate = sdf.format(cal.getTime());

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {//로그인 되면

                    groupId = gName + "_" + user.getId(); // group ID

                    Query queryID = reference.orderByChild("groupId").equalTo(groupId);

                    queryID.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(MakeGroupActivity.this, "중복되는 그룹 이름이 존재합니다!", Toast.LENGTH_LONG).show();
                            } else {
                                if (imageUri != null && validateNameAndPwd()){
                                    String inviteCode = createInviteCode();
                                    imageUpload(groupId, String.valueOf(user.getId()), gName, gPwd, imageUri, groupDate, inviteCode);
                                }
                                else{
                                    Toast.makeText(MakeGroupActivity.this, "필수 정보를 채워주세요!", Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG,error.toString());
                        }
                    });

                } else {
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });
    }

    // 초대코드 생성
    private String createInviteCode() {
        SecureRandom random = new SecureRandom();
        String randomCode = new BigInteger(30, random).toString(32).toUpperCase(); //대문자+숫자 6글자 비밀번호 생성
        return randomCode;
    }

    private void imageUpload(String id, String adminId, String name, String pwd, Uri imageUri, String date, String inviteCode){ //이미지 업로드 & 데이터베이스 적재

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        StorageReference fileRef = storageReference.child("groupImg/" + groupId + "." + getFileExtension(imageUri));

        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               Group group = new Group(id, adminId, name, pwd, uri.toString(), date, inviteCode);
                               reference.child(id).setValue(group);
                               setMember(id, adminId);
                               Log.e(TAG, "생성완료!");
                               Intent intent = new Intent(MakeGroupActivity.this, GroupListActivity.class);
                               startActivity(intent);
                               finish();
                           }
                       });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MakeGroupActivity.this, "업로드 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 그룹방 멤버 초기 설정
    private void setMember(String groupId, String userId){
        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("groupMem");

        GroupMem groupMem = new GroupMem(groupId, userId);
        reference.push().setValue(groupMem);
    }

    private String getFileExtension(Uri uri){ //파일 확장자명 받아내기
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private Boolean validateNameAndPwd() { //그룹 이름 & 패스워드 빈칸 아닌지 확인
        String val1 = groupName.getText().toString().trim();
        String val2 = groupPwd.getText().toString().trim();

        if (val1.isEmpty() || val2.isEmpty()) {
            return false;
        }
        else {
            return true;
        }
    }
}
