package com.example.wookie.Feed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Models.Document;
import com.example.wookie.Models.Post;
import com.example.wookie.Post.ReadPostActivity;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private ArrayList<Post> postList;
    private Context context;
    private static final int BAD=1, GOOD=2, RECOMMEND=3;

    public PostAdapter(ArrayList<Post> postList, Context context){
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_feed, parent, false);
        PostViewHolder holder = new PostViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostViewHolder holder, int position) {
        // 프로필 사진, 이름 설정
        String userId = postList.get(position).getUserId();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference("users").orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child(userId).child("userName").getValue().toString();
                String profile = snapshot.child(userId).child("userProfile").getValue().toString();

                holder.userName.setText(name);
                Glide.with(context).load(profile).circleCrop().into(holder.userImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // 글 작성 경과 시간 설정, 글 내용 설정
        holder.diffTime.setText(getDiffTime(postList.get(position).getPostDate()));
        holder.postContent.setText(postList.get(position).getContext());

        // 이미지 설정
        if(!(postList.get(position).getPostImg().equals("null"))){
            holder.postImage1.setVisibility(View.VISIBLE);
            Glide.with(context).load(postList.get(position).getPostImg())
                    .transform(new CenterCrop(),new RoundedCorners(25)).into(holder.postImage1);
        }

        // 리뷰 장소, 평점 설정
        if(postList.get(position).isReview()){
            DatabaseReference placeRef = database.getReference("Place").child(postList.get(position).getGroupId()).child(postList.get(position).getPlaceId());
            placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Document place = snapshot.getValue(Document.class);
                    holder.placeNameTxt.setText(place.getPlaceName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.placeReview.setVisibility(View.VISIBLE);
            switch (postList.get(position).getScore()){
                case BAD:
                    holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.bad));
                    break;
                case GOOD:
                    holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.good));
                    break;
                case RECOMMEND:
                    holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.recommend));
                    break;
            }
        }

        // 댓글 개수 설정
       holder.countComment.setText("댓글1개");
    }

    private static class TIME_MAXIMUM{
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    // 지난 시간 구하기
    public static String getDiffTime(String postDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = sdf.parse(postDate, new ParsePosition(0));
        long regTime = date.getTime();
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }

    @Override
    public int getItemCount() { return (postList != null ? postList.size() : 0); }


    public class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage, postImage1, scoreImage;
        ConstraintLayout placeReview;
        TextView userName, diffTime, postContent, placeName, countComment, placeNameTxt;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userImage = itemView.findViewById(R.id.user_image);
            this.postImage1 = itemView.findViewById(R.id.post_image1);
            this.userName = itemView.findViewById(R.id.user_name);
            this.diffTime = itemView.findViewById(R.id.diff_time);
            this.postContent = itemView.findViewById(R.id.post_content);
            this.placeReview = itemView.findViewById(R.id.placeReview_layout);
            this.placeName = itemView.findViewById(R.id.place_name);
            this.scoreImage = itemView.findViewById(R.id.score_image);
            this.countComment = itemView.findViewById(R.id.count_comment);
            this.placeNameTxt = itemView.findViewById(R.id.placeName_txt);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String groupId = postList.get(getLayoutPosition()).getGroupId();
                    String postId = postList.get(getLayoutPosition()).getPostId();

                    Intent intent = new Intent(itemView.getContext(), ReadPostActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("postId", postId);
                    itemView.getContext().startActivity(intent);
                    ((BottomNaviActivity)itemView.getContext()).finish();
                }
            });
        }
    }
}
