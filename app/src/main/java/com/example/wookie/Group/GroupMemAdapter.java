package com.example.wookie.Group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wookie.Models.Users;
import com.example.wookie.R;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class GroupMemAdapter extends RecyclerView.Adapter<GroupMemAdapter.UserViewHolder> {
    private String TAG = "GroupMemAdapter";
    private ArrayList<Users> userList;
    private Context context, ctx;

    private FirebaseDatabase database;
    private Query query;

    public GroupMemAdapter(ArrayList<Users> userList, Context context){
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        UserViewHolder holder = new UserViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        // 유저 프로필 이미지 설정
        Glide.with(context).load(userList.get(position).getUserProfile()).circleCrop().into(holder.userProfile);
        // 유저 이름 설정
        holder.userName.setText(userList.get(position).getUserName());

    }

    @Override
    public int getItemCount() { return (userList != null ? userList.size() : 0); }


    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userProfile;
        TextView userName, groupManager;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            this.userProfile = itemView.findViewById(R.id.iv_userProfile);
            this.userName = itemView.findViewById(R.id.tv_userName);
            this.groupManager = itemView.findViewById(R.id.tv_groupManager);

        }
    }
}
