package com.example.wookie.Post;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wookie.Models.Reply;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private String TAG = "ReplyAdapter";
    private ArrayList<Reply> replyList;
    private Context context;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    public ReplyAdapter(ArrayList<Reply> replyList, Context context){
        this.replyList = replyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        ReplyViewHolder holder = new ReplyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        // 프로필 사진, 이름 설정
        String userId = replyList.get(position).getUserId();
        database = FirebaseDatabase.getInstance();
        Query query = database.getReference("users").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child(userId).child("userName").getValue().toString();
                String profile = snapshot.child(userId).child("userProfile").getValue().toString();

                holder.replyUserName.setText(name);
                Glide.with(context).load(profile).circleCrop().into(holder.replyUser_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        holder.replyDate.setText(replyList.get(position).getReplyDate());
        holder.replyContext.setText(replyList.get(position).getReplyContext());

    }

    @Override
    public int getItemCount() {
        return (replyList != null ? replyList.size() : 0);
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        ImageView replyUser_image;
        TextView replyUserName;
        TextView replyDate;
        TextView replyContext;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            this.replyUser_image = itemView.findViewById(R.id.reply_user_image);
            this.replyUserName = itemView.findViewById(R.id.reply_user_name);
            this.replyDate = itemView.findViewById(R.id.reply_date);
            this.replyContext = itemView.findViewById(R.id.reply_context);

//            // 그룹방 클릭하면 해당 그룹방의 메인피드 화면으로 넘어감
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String groupId = groupList.get(getLayoutPosition()).getGroupId();
//
//                    Intent intent = new Intent(itemView.getContext(), BottomNaviActivity.class);
//                    intent.putExtra("groupId", groupId);
//                    Toast.makeText(context, "방ID:"+ groupId, Toast.LENGTH_SHORT).show();
//
//                    itemView.getContext().startActivity(intent);
//                }
//            });


        }
    }
}
