package com.rakib.soberpoint.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.FriendRequest;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.MyViewHolder> {
    Context context;
    List<FriendRequest> friendRequestList;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String current_user_id;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public FriendRequestAdapter(Context context, List<FriendRequest> friendRequestList) {
        this.context = context;
        this.friendRequestList = friendRequestList;
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        try {
            current_user_id = firebaseAuth.getCurrentUser().getUid();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.friend_request_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String friend_id=friendRequestList.get(position).getCurrent_id();
        String documentId=friendRequestList.get(position).BlogPostId;
        String type=friendRequestList.get(position).getType();
        String group_id=friendRequestList.get(position).getGroup_id();
        firebaseFirestore.collection("Users").document(friend_id).get().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               String image=task.getResult().getString("image");
               String name=task.getResult().getString("name");
               Picasso.get().load(image).into(holder.image);
               holder.name.setText(name+" want to make a connection");

           }
        });
        long time=friendRequestList.get(position).getTime().getTime();
        holder.ago.setText(getTimeAgo(time));
        holder.btn_confirm.setOnClickListener(v -> {
            if (type.equals("request")){
                Map<String,Object> map=new HashMap<>();
                map.put("friend_id",friend_id);
                map.put("time", FieldValue.serverTimestamp());
                firebaseFirestore.collection("Friend").document(current_user_id).collection("friends").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()){
                            Map<String,Object> map1=new HashMap<>();
                            map1.put("friend_id",current_user_id);
                            map1.put("time", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Friend").document("Friend").collection("friends").add(map1);
                            firebaseFirestore.collection("Friend").document(friend_id).collection("friends").add(map1).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    Toast.makeText(context,"Connection request confirmed",Toast.LENGTH_LONG).show();
                                    firebaseFirestore.collection("Request").document(current_user_id).collection("receive").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task1) {
                                            if (task1.isSuccessful()){
                                                //friendRequestList.remove(position);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });

                                }
                            });
                        }

                    }
                });
            }else {
                Map<String,Object> map=new HashMap<>();
                map.put("role","participant");
                map.put("time", FieldValue.serverTimestamp());
                map.put("uid",friend_id);
                firebaseFirestore.collection("Groups").document(group_id).collection("Participants").document(friend_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(context,"Connection request confirmed",Toast.LENGTH_LONG).show();
                            Toast.makeText(context,"Added in this group",Toast.LENGTH_LONG).show();
                            Map<String,Object> map1=new HashMap<>();
                            map1.put("friend_id",current_user_id);
                            map1.put("time", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Friend").document("Friend").collection("friends").add(map1);
                            firebaseFirestore.collection("Friend").document(friend_id).collection("friends").add(map1).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    firebaseFirestore.collection("Request").document(current_user_id).collection("receive").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task1) {
                                            if (task1.isSuccessful()){
                                                //friendRequestList.remove(position);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });

                                }
                            });
                        }
                    }
                });
            }

            });


        holder.btn_delete.setOnClickListener(v -> firebaseFirestore.collection("Request").document(current_user_id).collection("receive").document(documentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //friendRequestList.remove(position);
                    notifyDataSetChanged();
                }
            }
        }));

    }

    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name,ago;
        MaterialButton btn_confirm,btn_delete;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.circleImageView2);
            name=itemView.findViewById(R.id.textView4);
            ago=itemView.findViewById(R.id.textView5);
            btn_confirm=itemView.findViewById(R.id.btn_confirm);
            btn_delete=itemView.findViewById(R.id.btn_delete);
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
