package com.example.wookie.Feed;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.wookie.Models.GroupMem;
import com.example.wookie.Models.Post;
import com.example.wookie.Models.Reply;
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

import static java.security.AccessController.getContext;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private String TAG = "PostAdapter";
    private ArrayList<Post> postList;
    private Context context;
    private static final int BAD=1, GOOD=2, RECOMMEND=3;

    private FirebaseDatabase database;
    private Query query;

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
        database = FirebaseDatabase.getInstance();
        query = database.getReference("users").orderByChild("userId").equalTo(userId);
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
        else{
            holder.postImage1.setVisibility(View.GONE);
        }

        // 리뷰 장소, 평점 설정
        if(postList.get(position).isReview()){
            DatabaseReference placeRef = database.getReference("Place").child(postList.get(position).getGroupId()).child(postList.get(position).getPlaceId());
            placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Document place = dataSnapshot.getValue(Document.class);
                    holder.placeNameTxt.setText(place.getPlaceName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            holder.placeReview.setVisibility(View.VISIBLE);
            int score = postList.get(position).getScore();
            holder.scoreTxt.setText(Integer.toString(score)+".0");
            if (score > 0 && score <= 1) {
                holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.bad_rating));
            } else if (score > 1 && score < 4) {
                holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.good_rating));
            } else if (score >= 4 && score <= 5) {
                holder.scoreImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_rating_fill));
            }
        }
        else{
            holder.placeReview.setVisibility(View.GONE);
        }

        // 댓글 개수 설정
        DatabaseReference reference = database.getReference("reply").child(postList.get(position).getGroupId());
        Query query1 = reference.orderByChild("postId").equalTo(postList.get(position).getPostId());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int replyCount = 0;
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        replyCount++;
                    }
                }
                holder.replyCountTxt.setText("댓글 " + replyCount + "개");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
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
        TextView userName, diffTime, postContent, placeName, replyCountTxt, placeNameTxt, scoreTxt;

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
            this.replyCountTxt = itemView.findViewById(R.id.reply_count_txt);
            this.placeNameTxt = itemView.findViewById(R.id.placeName_txt);
            this.scoreTxt = itemView.findViewById(R.id.score_txt);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String groupId = postList.get(getLayoutPosition()).getGroupId();
                    String postId = postList.get(getLayoutPosition()).getPostId();

                    Intent intent = new Intent(itemView.getContext(), ReadPostActivity.class);
                    intent.putExtra("groupId", groupId);
                    intent.putExtra("postId", postId);
                    itemView.getContext().startActivity(intent);
                    ((BottomNaviActivity)itemView.getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    //TODO: readpostactivity에서 이전 액티비티에 따라 다른 화면 뜨게 만들기
//                    ((BottomNaviActivity)itemView.getContext()).finish();
                }
            });
        }
    }
}