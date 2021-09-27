package com.example.wookie.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wookie.Models.Document;
import com.example.wookie.R;

import java.util.ArrayList;

public class MapLocaAdapter extends RecyclerView.Adapter<MapLocaAdapter.MapLocaViewHolder> {
    Context context;
    ArrayList<Document> items;
    EditText editText;
    RecyclerView recyclerView;

    public MapLocaAdapter(ArrayList<Document> items, Context context, EditText editText, RecyclerView recyclerView) {
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
    public MapLocaAdapter.MapLocaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_select_place, viewGroup, false);
        return new MapLocaAdapter.MapLocaViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MapLocaAdapter.MapLocaViewHolder holder, int i) {
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
                recyclerView.setVisibility(View.GONE);
                BusProvider.getInstance().post(model);
            }
        });
    }


    public class MapLocaViewHolder extends RecyclerView.ViewHolder {
        TextView placeNameText, addressText, categoryText, distanceText;

        public MapLocaViewHolder(@NonNull final View itemView) {
            super(itemView);
            placeNameText = itemView.findViewById(R.id.placeName_txt);
            addressText = itemView.findViewById(R.id.placeAddress_txt);
            categoryText = itemView.findViewById(R.id.placeCategory_txt);
            distanceText = itemView.findViewById(R.id.placeDistance_txt);
        }
    }
}
