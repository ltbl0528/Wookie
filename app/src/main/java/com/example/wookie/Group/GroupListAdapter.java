package com.example.wookie.Group;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.wookie.BottomNaviActivity;
import com.example.wookie.Models.Group;
import com.example.wookie.Models.GroupMem;
import com.example.wookie.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {

    private String TAG = "GroupListAdapter";
    private ArrayList<Group> groupList;
    private Context context;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    public GroupListAdapter(ArrayList<Group> groupList, Context context){
        this.groupList = groupList;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        GroupViewHolder holder = new GroupViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Glide.with(context).load(groupList.get(position).getGroupImg())
                .transform(new CenterCrop(),new RoundedCorners(25)).into(holder.group_image);
        holder.group_name.setText(groupList.get(position).getGroupName());
        holder.group_create_date.setText(groupList.get(position).getGroupDate());

        String groupId = groupList.get(position).getGroupId();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("groupMem");

        Query query = reference.orderByChild("groupId").equalTo(groupId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int memCount=0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    memCount++;
                }
                holder.member_count.setText( memCount+"명 참가");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return (groupList != null ? groupList.size() : 0);
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView group_image;
        TextView group_name;
        TextView group_create_date;
        TextView member_count;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            this.group_image = itemView.findViewById(R.id.group_image);
            this.group_name = itemView.findViewById(R.id.group_name);
            this.group_create_date = itemView.findViewById(R.id.group_create_date);
            this.member_count = itemView.findViewById(R.id.member_count);

            // 그룹방 클릭하면 해당 그룹방의 메인피드 화면으로 넘어감
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String groupId = groupList.get(getLayoutPosition()).getGroupId();

                    Intent intent = new Intent(itemView.getContext(), BottomNaviActivity.class);
                    intent.putExtra("groupId", groupId);
                    Toast.makeText(context, "방ID:"+ groupId, Toast.LENGTH_SHORT).show();

                    itemView.getContext().startActivity(intent);
                }
            });


        }
    }
}
