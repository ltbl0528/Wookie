package com.example.wookie.Gallery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.Models.Post;
import com.example.wookie.Post.ReadPostActivity;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PostViewHolder> {
    private String TAG = "GalleryAdapter";
    private ArrayList<Post> postList;
    private Context context;

    private FirebaseDatabase database;
    private Query query;

    public GalleryAdapter(ArrayList<Post> postList, Context context){
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        PostViewHolder holder = new PostViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        // 작성자 설정
        String userId = postList.get(position).getUserId();
        database = FirebaseDatabase.getInstance();
        query = database.getReference("users").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child(userId).child("userName").getValue().toString();

                holder.postUserNameTxt.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

        // 작성일 설정
        holder.postDateTxt.setText(parseDate(postList.get(position).getPostDate()));
        // 이미지 설정
        Glide.with(context).load(postList.get(position).getPostImg())
                .transform(new CenterCrop(), new RoundedCorners(1)).into(holder.galleryImage);
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
    public int getItemCount() { return (postList != null ? postList.size() : 0); }


    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView galleryImage;
        TextView postUserNameTxt, postDateTxt;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            this.galleryImage = itemView.findViewById(R.id.gallery_image);
            this.postUserNameTxt = itemView.findViewById(R.id.post_user_name_txt);
            this.postDateTxt = itemView.findViewById(R.id.post_date_txt);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String groupId = postList.get(getLayoutPosition()).getGroupId();
                    String postId = postList.get(getLayoutPosition()).getPostId();

                    Intent intent = new Intent(itemView.getContext(), ReadPostActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("postId", postId);
                    itemView.getContext().startActivity(intent);
                    //TODO: readpostactivity에서 이전 액티비티에 따라 다른 화면 뜨게 만들기
//                    ((BottomNaviActivity)itemView.getContext()).finish();
                }
            });
        }
    }
}
