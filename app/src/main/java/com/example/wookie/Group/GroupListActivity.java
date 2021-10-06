package com.example.wookie.Group;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.Models.Group;
import com.example.wookie.Models.GroupMem;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.ArrayList;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class GroupListActivity extends AppCompatActivity {

    private String TAG = "GroupListActivity";
    private Button addGroupBtn, enterDialogBtn, codeMenuBtn, groupMakeMenuBtn, cancelBtn, cancelBtn2;
    private EditText groupCodeInput, groupPwdInput;
    private String userLoginId;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Group> groupList;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Dialog mDialog, dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

//        enterBtn = findViewById(R.id.enter_btn);
        addGroupBtn = findViewById(R.id.add_group_btn);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)); // 구분선 추가
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null){//로그인 되면
                    userLoginId = String.valueOf(user.getId());
                    Log.e(TAG, "성공");
                }
                else{
                    Log.e(TAG, throwable.toString());
                }
                return null;
            }
        });


        groupList = new ArrayList<>(); // Group 객체를 담을 Array리스트

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("groupMem");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Query query = reference.orderByChild("userId").equalTo(userLoginId);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // 해당 유저가 가입된 그룹방이 존재하면
                                GroupMem groupMem = dataSnapshot.getValue(GroupMem.class);
                                String groupId = groupMem.getGroupId(); // 그룹방id를 가져와서
                                Query query1 = database.getReference("group").orderByChild("groupId").equalTo(groupId);
                                query1.addListenerForSingleValueEvent(new ValueEventListener() { // 그룹방의 정보를 가져온다
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Group group = snapshot.getValue(Group.class);
                                            groupList.add(group);
                                        }
                                        adapter.notifyDataSetChanged();
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
                } else {
                    Toast.makeText(GroupListActivity.this, "가입된 방 없음", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        adapter = new GroupListAdapter(groupList, this);
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 그룹 리스트 출력


        addGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog= new Dialog(GroupListActivity.this);
                mDialog.setContentView(R.layout.dialog_group_join_menu);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show();

                codeMenuBtn = mDialog.findViewById(R.id.group_code_menu_btn);
                groupMakeMenuBtn = mDialog.findViewById(R.id.group_add_menu_btn);
                cancelBtn = mDialog.findViewById(R.id.cancel_button);

                codeMenuBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                        dialog= new Dialog(GroupListActivity.this);
                        dialog.setContentView(R.layout.dialog_add_code);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        dialog.show();

                        enterDialogBtn = dialog.findViewById(R.id.enter_btn);
                        cancelBtn2 = dialog.findViewById(R.id.cancel_button2);
                        groupCodeInput = dialog.findViewById(R.id.group_code);
                        groupPwdInput = dialog.findViewById(R.id.group_pwd);

                        enterDialogBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String groupCode = groupCodeInput.getText().toString();
                                String groupPwd = groupPwdInput.getText().toString();
                                Log.e(TAG, groupCode+" "+groupPwd);
                                checkGroupCodeRight(groupCode, groupPwd);
                            }
                        });

                        cancelBtn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });

                groupMakeMenuBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(GroupListActivity.this, MakeGroupActivity.class);
                        startActivity(intent);
                        mDialog.dismiss();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });

            }
        });
    }
    private void checkGroupCodeRight(String code, String pwd) {
        if (code.trim().isEmpty() || pwd.trim().isEmpty()){
            Toast.makeText(GroupListActivity.this, "정보를 입력해주세요!", Toast.LENGTH_LONG).show();
        }
        else{
            database = FirebaseDatabase.getInstance();
            DatabaseReference groupRef = database.getReference("group");
            Query query1 = groupRef.orderByChild("inviteCode").equalTo(code);

            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Group group = dataSnapshot.getValue(Group.class);
                            String groupId = group.getGroupId();
                            Query query2 = groupRef.orderByChild("groupPwd").equalTo(pwd);
                            query2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        GroupMem groupMem = new GroupMem(groupId, userLoginId);
                                        DatabaseReference groupMemRef = database.getReference("groupMem");
                                        groupMemRef.push().setValue(groupMem);
                                        Intent intent = new Intent(GroupListActivity.this, GroupListActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(GroupListActivity.this, "비밀번호가 다릅니다!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    else{

                        Toast.makeText(GroupListActivity.this, "초대 코드 오류", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}

