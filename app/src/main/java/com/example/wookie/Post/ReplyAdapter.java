package com.example.wookie.Post;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        holder.replyDate.setText(parseDate(replyList.get(position).getReplyDate()));
        holder.replyContext.setText(replyList.get(position).getReplyContext());

    }

    // 분(minute)까지만 표시
    private String parseDate(String date) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

        Date oldDate = inputFormat.parse(date, new ParsePosition(0));
        String newDate = outputFormat.format(oldDate);

        return newDate;
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

            String userLoginId = ((ReadPostActivity)ReadPostActivity.mContext).passUserLoginId;

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // 본인이 작성한 댓글일 경우
                    if(userLoginId.equals(replyList.get(getLayoutPosition()).getUserId())){

                        AlertDialog.Builder oDialog = new AlertDialog.Builder(itemView.getContext(),
                                android.R.style.Theme_DeviceDefault_Light_Dialog);
                        oDialog.setMessage("댓글을 삭제하시겠습니까?")
                                .setPositiveButton("아니요", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        Log.i("Dialog", "취소");
                                    }
                                })
                                .setNegativeButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i("Dialog", "댓글 삭제");

                                        String groupId = replyList.get(getLayoutPosition()).getGroupId();
                                        String replyId = replyList.get(getLayoutPosition()).getReplyId();
                                        String postId = replyList.get(getLayoutPosition()).getPostId();

                                        delReply(groupId, replyId); // 댓글 삭제

                                        Intent intent = new Intent(itemView.getContext(), ReadPostActivity.class);
                                        intent.putExtra("groupId", groupId);
                                        intent.putExtra("postId", postId);
                                        itemView.getContext().startActivity(intent);

                                        ((ReadPostActivity) itemView.getContext()).finish();
                                    }
                                })
                                .setCancelable(true)
                                .show();
                        
                        return true;
                    }
                    // 본인이 작성한 댓글이 아닐 경우
                    else{
                        return false;
                    }
                }
            });

        }

        // 댓글 삭제
        private void delReply(String groupId, String replyId) {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference("reply").child(groupId);
            reference.child(replyId).removeValue();
        }
    }
}
