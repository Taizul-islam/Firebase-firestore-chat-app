package com.rakib.soberpoint.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.ItemFriendList;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.HolderParticipantAdd> {
    Context context;
    List<ItemFriendList> itemFriendLists;
    String groupId,myGroupRole;
    FirebaseFirestore firebaseFirestore;

    public AdapterParticipantAdd(Context context, List<ItemFriendList> itemFriendLists, String groupId, String myGroupRole) {
        this.context = context;
        this.itemFriendLists = itemFriendLists;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_participant_add_list,parent,false);
        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HolderParticipantAdd holder, int position) {
        String friend_id=itemFriendLists.get(position).getFriend_id();
        firebaseFirestore.collection("Users").document(friend_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String image=task.getResult().getString("image");
                Picasso.get().load(image).into(holder.avaterIv);
                String name=task.getResult().getString("name");
                holder.nameTv.setText(name);
                String email=task.getResult().getString("email");
                holder.emailTv.setText(email);
                checkIfAlreadyExist(friend_id,holder);
            }

        });
        holder.itemView.setOnClickListener(v -> firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(friend_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String role=task.getResult().getString("role");
                        String [] option;
                        AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        builder.setTitle("Choose Option");
                        if (myGroupRole.equals("creator")){
                            if (role.equals("admin")){
                                option=new String[]{"Remove admin","Remove user"};
                                builder.setItems(option, (dialog, which) -> {
                                  if (which==0){
                                      removeAdmin(friend_id);
                                  }else {
                                      removeParticipant(friend_id);
                                  }
                                }).show();
                            }
                            else if (role.equals("participant")){
                                option=new String[]{"Make admin","Remove user"};
                                builder.setItems(option, (dialog, which) -> {
                                    if (which==0){
                                        makeAdmin(friend_id);
                                    }else {
                                        removeParticipant(friend_id);
                                    }
                                }).show();
                            }
                        }
                        else if (myGroupRole.equals("admin")){
                            if (role.equals("creator")){
                                Toast.makeText(context,"Creator of the group",Toast.LENGTH_LONG).show();
                            }
                            else if (role.equals("admin")){
                                option=new String[]{"Remove admin","Remove user"};
                                builder.setItems(option, (dialog, which) -> {
                                    if (which==0){
                                        removeAdmin(friend_id);
                                    }else {
                                        removeParticipant(friend_id);
                                    }
                                }).show();
                            }
                            else if (role.equals("participant")){
                                option=new String[]{"Make admin","Remove user"};
                                builder.setItems(option, (dialog, which) -> {
                                    if (which==0){
                                        makeAdmin(friend_id);
                                    }else {
                                        removeParticipant(friend_id);
                                    }
                                }).show();
                            }
                        }
                    }else {
                        AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        builder.setTitle("Choose Option");
                        builder.setMessage("Add this person in this group");
                        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                  addPerticipant(friend_id);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              dialog.dismiss();
                            }
                        });
                        builder.show();

                    }
                }
            }
        }));
    }

    private void removeAdmin(String friend_id) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "participant");
            map.put("time", FieldValue.serverTimestamp());
            map.put("uid", friend_id);
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(friend_id).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "This person is no longer admin now", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    private void removeParticipant(String friend_id) {
        try {
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(friend_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "this user is removed from this group", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void makeAdmin(String friend_id) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "admin");
            map.put("time", FieldValue.serverTimestamp());
            map.put("uid", friend_id);
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(friend_id).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "this user is now admin", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void addPerticipant(String friend_id) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("role", "participant");
            map.put("time", FieldValue.serverTimestamp());
            map.put("uid", friend_id);
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(friend_id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Added in this group", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void checkIfAlreadyExist(String id,HolderParticipantAdd holderParticipantAdd) {
        try {
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String role = task.getResult().getString("role");
                            holderParticipantAdd.statusTv.setText(role);

                        } else {
                            holderParticipantAdd.statusTv.setText("");
                        }
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return itemFriendLists.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder{
        CircleImageView avaterIv;
        TextView nameTv,emailTv,statusTv;
        public HolderParticipantAdd(@NonNull @NotNull View itemView) {
            super(itemView);
            avaterIv=itemView.findViewById(R.id.circleImageView3);
            nameTv=itemView.findViewById(R.id.textView6);
            emailTv=itemView.findViewById(R.id.textView7);
            statusTv=itemView.findViewById(R.id.imageView3);
        }
    }
}
