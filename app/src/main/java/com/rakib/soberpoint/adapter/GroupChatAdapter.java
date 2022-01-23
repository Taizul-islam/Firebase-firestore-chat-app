package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.ItemGroupMessage;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.GroupHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ItemGroupMessage> itemGroupMessageList;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public GroupChatAdapter(Context context, List<ItemGroupMessage> itemGroupMessageList) {
        this.context = context;
        this.itemGroupMessageList = itemGroupMessageList;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        if (itemGroupMessageList.get(position).getSender().equals(firebaseAuth.getCurrentUser().getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }

    }

    @NonNull
    @NotNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.group_right, parent, false);
            return new GroupHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_left, parent, false);
            return new GroupHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupHolder holder, int position) {
        ItemGroupMessage itemGroupMessage = itemGroupMessageList.get(position);
        String message = itemGroupMessage.getMessage();
        String sender = itemGroupMessage.getSender();

        if (itemGroupMessage.getType().equals("text")) {
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        } else {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            Picasso.get().load(message).into(holder.messageIv);
            holder.messageTv.setText(message);
        }
        if (itemGroupMessage.getTime() == null) {
            long millis = Calendar.getInstance().getTimeInMillis();
            holder.timeTv.setText(getTimeAgo(millis));
        } else {
            long milli = itemGroupMessage.getTime().getTime();
            holder.timeTv.setText(getTimeAgo(milli));

        }

        firebaseFirestore.collection("Users").document(sender).get().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    String name = task.getResult().getString("name");
                    holder.nameTv.setText(name);


                } else {
                    Log.d("error in image", task.getException().getLocalizedMessage());
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        });


    }

    @Override
    public int getItemCount() {
        return itemGroupMessageList.size();
    }

    class GroupHolder extends RecyclerView.ViewHolder {
        TextView nameTv, messageTv, timeTv;
        ImageView messageIv;

        public GroupHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.imageView6);

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
