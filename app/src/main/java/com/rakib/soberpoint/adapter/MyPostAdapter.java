package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.ItemPost;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyViewHolder> {
   public Context context;
    List<ItemPost> itemPostList;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String current_user_id;

    public MyPostAdapter(Context context, List<ItemPost> itemPostList) {
        this.context = context;
        this.itemPostList = itemPostList;
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.my_single_post_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String documentId=itemPostList.get(position).BlogPostId;
        String description=itemPostList.get(position).getMessage();
        holder.setDesc(description);
        String image=itemPostList.get(position).getImage();
        holder.setImage(image);
        if (itemPostList.get(position).getTime()==null){
            long millis= Calendar.getInstance().getTimeInMillis();
            holder.setTime(millis);
        }else {
            long millis= itemPostList.get(position).getTime().getTime();
            holder.setTime(millis);
        }

        String user_id=itemPostList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
             if (task.isSuccessful()){
                 String name=task.getResult().getString("name");
                 String feel=itemPostList.get(position).getFeeling();
                 holder.setName(name,feel);
                 String profile=task.getResult().getString("image");
                 holder.setProfile(profile);

             }
        });
        firebaseFirestore.collection("Post").document(documentId).collection("Likes").addSnapshotListener((value, error) -> {
            try {
                if (!value.isEmpty()) {
                    int count = value.size();
                    holder.count.setText(count + " Likes");
                } else {
                    holder.count.setText("0 Likes");
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        });
        firebaseFirestore.collection("Post").document(documentId).collection("Likes").document(current_user_id).addSnapshotListener((value, error) -> {
            try {


                if (value.exists()) {
                    holder.like.setImageDrawable(context.getDrawable(R.drawable.ic_like));
                } else {
                    holder.like.setImageDrawable(context.getDrawable(R.drawable.ic_unlike));
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        });
        holder.like.setOnClickListener(v -> firebaseFirestore.collection("Post").document(documentId).collection("Likes").document(current_user_id).get().addOnCompleteListener(task -> {
            try {
                if (task.getResult().exists()) {
                    firebaseFirestore.collection("Post").document(documentId).collection("Likes").document(current_user_id).delete();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timeStamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Post").document(documentId).collection("Likes").document(current_user_id).set(map);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }));
        holder.imageView2.setOnClickListener(v -> {
            final PopupMenu popup = new PopupMenu(context, holder.imageView2);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.delete_popup){
                    firebaseFirestore.collection("Post").document(documentId).delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                         // itemPostList.remove(position);
                          notifyDataSetChanged();
                            Toast.makeText(context, "Post Deleted!!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                    return false;
            });
            popup.show();

        });


    }

    @Override
    public int getItemCount() {
        return itemPostList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView desc,timedate,name1,count;
        RoundedImageView image;
        ImageView postImage;
        View view;
        ImageView like,imageView2;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            like=view.findViewById(R.id.like);
            count=view.findViewById(R.id.count);
            imageView2=view.findViewById(R.id.imageView2);
        }
        public void setDesc(String description){
            desc=view.findViewById(R.id.desc);
            if (!description.equals("null")){
                desc.setVisibility(View.VISIBLE);
                desc.setText(description);
            }else {
                desc.setVisibility(View.GONE);
            }

        }
        public void setImage(String image){
            postImage=view.findViewById(R.id.postImage);
            if (!image.equals("null")){
                Glide.with(context)
                        .asBitmap()
                        .load(image)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                                postImage.setImageBitmap(bitmap);
                            }
                        });
            }


        }
        public void setTime(long millis){
            timedate=view.findViewById(R.id.timedate);
            timedate.setText(getTimeAgo(millis));
        }
        public void setName(String name,String feel){
            name1=view.findViewById(R.id.name);
            if (!feel.equals("null")){
                name1.setText(name+"--- feeling "+feel);
            }else {
                name1.setText(name);
            }

        }
        public void setProfile(String name){
            image=view.findViewById(R.id.image);
            Glide.with(context).load(name).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(image);
        }

    }
    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
