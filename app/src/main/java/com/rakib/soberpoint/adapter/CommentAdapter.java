package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.CommentItem;

import java.util.Calendar;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder>{
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    Context context;
    List<CommentItem> commentItemList;
    FirebaseFirestore firebaseFirestore;

    public CommentAdapter(Context context, List<CommentItem> commentItemList) {
        this.context = context;
        this.commentItemList = commentItemList;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.single_comment_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String description=commentItemList.get(position).getComment();
        holder.desc.setText(description);
        if (commentItemList.get(position).getTime()==null){
            long millis= Calendar.getInstance().getTimeInMillis();
            holder.timedate.setText(getTimeAgo(millis));
        }else {
            long milli= commentItemList.get(position).getTime().getTime();
            holder.timedate.setText(getTimeAgo(milli));
        }

        String user_id=commentItemList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String  name=task.getResult().getString("name");
                holder.name1.setText(name);
                String profile =task.getResult().getString("image");
                if (profile.equals("")){
                    holder.image.setImageResource(R.drawable.launcher_image);
                }else {
                    Glide.with(context).load(profile).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(holder.image);
                }


            }else {
                Log.d("error in image",task.getException().getLocalizedMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView desc,timedate,name1;
        RoundedImageView image;
        View view;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
            image=view.findViewById(R.id.image);
            desc=view.findViewById(R.id.desc);
            timedate=view.findViewById(R.id.timedate);
            name1=view.findViewById(R.id.name);
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
