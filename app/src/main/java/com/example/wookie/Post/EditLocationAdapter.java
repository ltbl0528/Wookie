package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.Models.Document;
import com.example.wookie.Models.Post;
import com.example.wookie.R;

import java.io.Serializable;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class EditLocationAdapter extends RecyclerView.Adapter<EditLocationAdapter.LocationViewHolder> {
    String TAG = "EditLocationAdapter";
    Context context;
    ArrayList<Document> items;
    EditText editText;
    RecyclerView recyclerView;
    int score;
    boolean isReview;

    public EditLocationAdapter(ArrayList<Document> items, Context context, EditText editText, RecyclerView recyclerView) {
        this.context = context;
        this.items = items;
        this.editText = editText;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public void addItem(Document item) {
        items.add(item);
    }


    public void clear() {
        items.clear();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(items.get(position).getId());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_select_place, viewGroup, false);
        return new LocationViewHolder(view);
    }

    //TODO: Distance ?????? ???????????? ??? (ApiInterface Query, SelectPlace??? apiInterface.getSearchLocation ??????)
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int i) {
        final Document model = items.get(i);

        String placeName = model.getPlaceName();
        String address = model.getRoadAddressName();
        String distance = model.getDistance();
        String category = model.getCategoryGroupName();

        holder.placeNameText.setText(placeName);
        holder.addressText.setText(address);
        holder.distanceText.setText(distance);
        holder.categoryText.setText(category);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                editText.setText(model.getPlaceName());
//                recyclerView.setVisibility(View.GONE);
//                BusProvider.getInstance().post(model);
                Dialog scoreDialog;
                TextView placeNameTv;
                RatingBar ratingBar;
                Button submitDialogBtn;
//                badBtn, goodBtn, recommendBtn;

                scoreDialog= new Dialog(view.getContext());
                scoreDialog.setContentView(R.layout.dialog_score);

                scoreDialog.show();
                placeNameTv = scoreDialog.findViewById(R.id.place_name);
                ratingBar = scoreDialog.findViewById(R.id.rating_bar);
                submitDialogBtn = scoreDialog.findViewById(R.id.submit_btn);
//                badBtn = scoreDialog.findViewById(R.id.bad_btn);
//                goodBtn = scoreDialog.findViewById(R.id.good_btn);
//                recommendBtn = scoreDialog.findViewById(R.id.recommend_btn);

                placeNameTv.setText(model.getPlaceName());

                String editText = ((EditPostActivity)EditPostActivity.mContext).passEditText;
                String groupID = ((EditPostActivity)EditPostActivity.mContext).passGroupId;
                String postID = ((EditPostActivity)EditPostActivity.mContext).passPostId;
                Uri imgUri = ((EditPostActivity)EditPostActivity.mContext).passUri;
                Post EditPost = ((EditPostActivity)EditPostActivity.mContext).passPost;
                boolean isNewImg = ((EditPostActivity)EditPostActivity.mContext).passIsImg;

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        if(rating<1.0f)
                            ratingBar.setRating(1.0f);
                    }
                });

                //???????????? ?????? ???
                submitDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        score = (int) ratingBar.getRating();
                        Log.e(TAG, Float.toString(score));
                        isReview = true;
                        EditPost.setScore(score);
                        EditPost.setReview(true);
                        scoreDialog.dismiss();
                        Intent intent = new Intent(view.getContext(), EditPostActivity.class);
                        intent.putExtra("post", (Serializable) EditPost);
                        intent.putExtra("placeInfo", model);
                        intent.putExtra("score", score);
                        intent.putExtra("isReview", isReview);
                        intent.putExtra("editTxt", editText);
                        intent.putExtra("groupId", groupID);
                        intent.putExtra("postId", postID);
                        intent.putExtra("imgUri", imgUri);
                        intent.putExtra("isNewImg", isNewImg);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                        Log.e(TAG, "score1");
                        view.getContext().startActivity(intent);
                    }
                });
                // ???????????? '??????' ??????
//                badBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        // bad ???????????? ????????? ??? ?????? ?????? ?????? ??????
//                        score = 1;
//                        isReview = true;
//                        EditPost.setScore(1);
//                        EditPost.setReview(true);
//                        scoreDialog.dismiss();
//                        Intent intent = new Intent(view.getContext(), EditPostActivity.class);
//                        intent.putExtra("post", (Serializable) EditPost);
//                        intent.putExtra("placeInfo", model);
//                        intent.putExtra("score", score);
//                        intent.putExtra("isReview", isReview);
//                        intent.putExtra("editTxt", editText);
//                        intent.putExtra("groupId", groupID);
//                        intent.putExtra("postId", postID);
//                        intent.putExtra("imgUri", imgUri);
//                        intent.putExtra("isNewImg", isNewImg);
//                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
//                        Log.e(TAG, "score1");
//                        view.getContext().startActivity(intent);
//                    }
//                });
//                // ???????????? '??????' ??????
//                goodBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        score = 2;
//                        isReview = true;
//                        EditPost.setScore(2);
//                        EditPost.setReview(true);
//                        scoreDialog.dismiss();
//                        Intent intent = new Intent(view.getContext(), EditPostActivity.class);
//                        intent.putExtra("post", (Serializable) EditPost);
//                        intent.putExtra("placeInfo", model);
//                        intent.putExtra("score", score);
//                        intent.putExtra("isReview", isReview);
//                        intent.putExtra("editTxt", editText);
//                        intent.putExtra("groupId", groupID);
//                        intent.putExtra("postId", postID);
//                        intent.putExtra("imgUri", imgUri);
//                        intent.putExtra("isNewImg", isNewImg);
//                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
//                        Log.e(TAG, "score2");
//                        view.getContext().startActivity(intent);
//                    }
//                });
//                // ???????????? '??????' ??????
//                recommendBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        score = 3;
//                        isReview = true;
//                        EditPost.setScore(3);
//                        EditPost.setReview(true);
//                        scoreDialog.dismiss();
//                        Intent intent = new Intent(view.getContext(), EditPostActivity.class);
//                        intent.putExtra("placeInfo", model);
//                        intent.putExtra("score", score);
//                        intent.putExtra("isReview", isReview);
//                        intent.putExtra("editTxt", editText);
//                        intent.putExtra("groupId", groupID);
//                        intent.putExtra("postId", postID);
//                        intent.putExtra("imgUri", imgUri);
//                        intent.putExtra("post", (Serializable) EditPost);
//                        intent.putExtra("isNewImg", isNewImg);
//                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
//                        Log.e(TAG, "score3");
//                        view.getContext().startActivity(intent);
//                    }
//                });
            }
        });
    }


    public class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameText, addressText, categoryText, distanceText;

        public LocationViewHolder(@NonNull final View itemView) {
            super(itemView);
            placeNameText = itemView.findViewById(R.id.placeName_txt);
            addressText = itemView.findViewById(R.id.placeAddress_txt);
            categoryText = itemView.findViewById(R.id.placeCategory_txt);
            distanceText = itemView.findViewById(R.id.placeDistance_txt);
        }
    }
}
