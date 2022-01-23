package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.GroupChatActivity;
import com.rakib.soberpoint.activities.GroupInfoActivity;
import com.rakib.soberpoint.items.ItemGroup;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.HmmHolder> {

    Context context;
    List<ItemGroup> itemGroupList;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public GroupListAdapter(Context context, List<ItemGroup> itemGroupList) {
        this.context = context;
        this.itemGroupList = itemGroupList;
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HmmHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_group_list,parent,false);
        return new HmmHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HmmHolder holder, int position) {
        ItemGroup group=itemGroupList.get(position);
        holder.title.setVisibility(View.VISIBLE);
        holder.icon.setVisibility(View.VISIBLE);
        holder.message.setVisibility(View.VISIBLE);
        String groupId=group.getGroupId();
        String groupIcon=group.getGroupIcon();
        String groupTitle=group.getGroupTitle();

        holder.title.setText(groupTitle);
        Picasso.get().load(groupIcon).into(holder.icon);
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(context, GroupInfoActivity.class);
            intent.putExtra("groupId",groupId);
            context.startActivity(intent);
        });




    }

//    private void loadLastMessage(ItemGroup group, HmmHolder holder) {
//        try {
//            firebaseFirestore.collection("Groups").document(group.getGroupId()).collection("Messages").orderBy("time", Query.Direction.ASCENDING).limitToLast(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        try {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                if (document.exists()){
//                                    holder.message.setText(document.getData().get("message").toString());
//                                    String time = document.getData().get("last").toString();
//                                    Calendar calendar = Calendar.getInstance();
//                                    calendar.setTimeInMillis(Long.parseLong(time));
//                                    String dateTime = DateFormat.format("hh:mm aa", calendar).toString();
//                                    holder.time.setText(dateTime);
//                                    firebaseFirestore.collection("Users").document(document.getData().get("sender").toString()).get().addOnCompleteListener(task1 -> {
//                                        if (task1.isSuccessful()) {
//                                            String name = task1.getResult().getString("name");
//                                            holder.name.setText(name);
//
//                                        } else {
//                                            Log.d("error in image", task.getException().getLocalizedMessage());
//                                        }
//                                    });
//                                }else {
//                                    holder.name.setText("Tap to message");
//                                    holder.message.setText("");
//                                    holder.time.setText("");
//                                }
//
//                            }
//                        }catch (NullPointerException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }
//    }

    @Override
    public int getItemCount() {
        return itemGroupList.size();
    }

    public class HmmHolder extends RecyclerView.ViewHolder{
        CircleImageView icon;
        TextView title,name,message,time;
        public HmmHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.circleImageView4);
            title=itemView.findViewById(R.id.textView8);
            name=itemView.findViewById(R.id.textView9);
            message=itemView.findViewById(R.id.textView10);
        }
    }
}
