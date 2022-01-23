package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.MessageActivity;
import com.rakib.soberpoint.items.ItemFriendList;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.MyViewHolder> {
    Context context;
    List<ItemFriendList> itemFriendListList;
    FirebaseFirestore firebaseFirestore;
    public FriendListAdapter(Context context, List<ItemFriendList> itemFriendListList) {
        this.context = context;
        this.itemFriendListList = itemFriendListList;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_friend_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
       String friend_id=itemFriendListList.get(position).getFriend_id();
       firebaseFirestore.collection("Users").document(friend_id).get().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               String image=task.getResult().getString("image");
               if (image.equals("")){
                   holder.image.setImageResource(R.drawable.launcher_image);
               }else {
                   Picasso.get().load(image).into(holder.image);
               }

               String name=task.getResult().getString("name");
               holder.name.setText(name);
               String status=task.getResult().getString("status");
               if (status.equals("Online")){
                   holder.status.setImageDrawable(context.getDrawable(R.drawable.active));
               }else {
                   holder.status.setImageDrawable(context.getDrawable(R.drawable.in_active));
               }

           }

       });
       holder.itemView.setOnClickListener(v -> {
           Intent intent=new Intent(context, MessageActivity.class);
           intent.putExtra("uid",friend_id);
           context.startActivity(intent);
       });

    }

    @Override
    public int getItemCount() {
        return itemFriendListList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name;
        ImageView status;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.circleImageView3);
            name=itemView.findViewById(R.id.textView6);
            status=itemView.findViewById(R.id.imageView3);
        }
    }
}
