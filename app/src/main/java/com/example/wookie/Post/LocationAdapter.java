package com.example.wookie.Post;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wookie.Models.Document;
import com.example.wookie.R;

import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    Context context;
    ArrayList<Document> items;
    EditText editText;
    RecyclerView recyclerView;
    int score;
    boolean isReview;

    public LocationAdapter(ArrayList<Document> items, Context context, EditText editText, RecyclerView recyclerView) {
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

    //TODO: Distance 정보 가져와야 함 (ApiInterface Query, SelectPlace의 apiInterface.getSearchLocation 수정)
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
                Button cancelDialogBtn, badBtn, goodBtn, recommendBtn;

                scoreDialog= new Dialog(view.getContext());
                scoreDialog.setContentView(R.layout.dialog_score);

                scoreDialog.show();
                placeNameTv = scoreDialog.findViewById(R.id.place_name);
                cancelDialogBtn = scoreDialog.findViewById(R.id.cancel_btn);
                badBtn = scoreDialog.findViewById(R.id.bad_btn);
                goodBtn = scoreDialog.findViewById(R.id.good_btn);
                recommendBtn = scoreDialog.findViewById(R.id.recommend_btn);

                placeNameTv.setText(model.getPlaceName());

                String editText = ((WritePostActivity)WritePostActivity.mContext).passEditText;
                String groupID = ((WritePostActivity)WritePostActivity.mContext).passGroupId;
                Uri imgUri = ((WritePostActivity)WritePostActivity.mContext).passUri;

                //취소버튼 클릭 시
                cancelDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        scoreDialog.dismiss();
                    }
                });
                // 평가버튼 '나쁨' 선택
                badBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // bad 이미지로 설정한 후 작성 중인 글에 띄움
                        score = 1;
                        isReview = true;
                        scoreDialog.dismiss();
                        Intent intent = new Intent(view.getContext(), WritePostActivity.class);
                        intent.putExtra("placeInfo", model);
                        intent.putExtra("score", score);
                        intent.putExtra("isReview", isReview);
                        intent.putExtra("editTxt", editText);
                        intent.putExtra("groupId", groupID);
                        intent.putExtra("imgUri", imgUri);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);

                        view.getContext().startActivity(intent);
                    }
                });
                // 평가버튼 '좋음' 선택
                goodBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        score = 2;
                        isReview = true;
                        scoreDialog.dismiss();
                        Intent intent = new Intent(view.getContext(), WritePostActivity.class);
                        intent.putExtra("placeInfo", model);
                        intent.putExtra("score", score);
                        intent.putExtra("isReview", isReview);
                        intent.putExtra("editTxt", editText);
                        intent.putExtra("groupId", groupID);
                        intent.putExtra("imgUri", imgUri);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);

                        view.getContext().startActivity(intent);
                    }
                });
                // 평가버튼 '추천' 선택
                recommendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        score = 3;
                        isReview = true;
                        scoreDialog.dismiss();
                        Intent intent = new Intent(view.getContext(), WritePostActivity.class);
                        intent.putExtra("placeInfo", model);
                        intent.putExtra("score", score);
                        intent.putExtra("isReview", isReview);
                        intent.putExtra("editTxt", editText);
                        intent.putExtra("groupId", groupID);
                        intent.putExtra("imgUri", imgUri);
                        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);

                        view.getContext().startActivity(intent);
                    }
                });
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
