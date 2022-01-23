package com.rakib.soberpoint.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.Message;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;
    FirebaseFirestore firebaseFirestore;
    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);









        if(holder.getClass() == SentViewHolder.class) {
            SentViewHolder viewHolder = (SentViewHolder)holder;

            if(message.getMessage().equals("photo")) {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.launcher_image)
                        .into(viewHolder.image);
            }

            viewHolder.message.setText(message.getMessage());



            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    TextView everyone=view.findViewById(R.id.everyone);
                    TextView delete=view.findViewById(R.id.delete);
                    TextView cancel=view.findViewById(R.id.cancel);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(view)
                            .create();

                    everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(message.getMessageId()).set(message).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    firebaseFirestore.collection("chats").document(receiverRoom).collection("messages").document(message.getMessageId()).set(message).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){
                                            notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                            dialog.dismiss();
                        }
                    });

                    delete.setOnClickListener(v1 -> {
                        firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(message.getMessageId()).set("null").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                        dialog.dismiss();
                    });

                    cancel.setOnClickListener(v12 -> dialog.dismiss());

                    dialog.show();

                    return false;
                }
            });
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            if(message.getMessage().equals("photo")) {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.launcher_image)
                        .into(viewHolder.image);
            }
            viewHolder.message.setText(message.getMessage());



            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                    TextView everyone=view.findViewById(R.id.everyone);
                    TextView delete=view.findViewById(R.id.delete);
                    TextView cancel=view.findViewById(R.id.cancel);
                    AlertDialog dialog = new AlertDialog.Builder(context)
                            .setTitle("Delete Message")
                            .setView(view)
                            .create();

                    everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(message.getMessageId()).set(message).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    firebaseFirestore.collection("chats").document(receiverRoom).collection("messages").document(message.getMessageId()).set(message).addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){

                                        }
                                    });
                                }
                            });
                            dialog.dismiss();
                        }
                    });

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(message.getMessageId()).set("null").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                            dialog.dismiss();
                        }
                    });

                   cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView image,feeling;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            message=itemView.findViewById(R.id.message);
            image=itemView.findViewById(R.id.image);
            feeling=itemView.findViewById(R.id.feeling);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView message;
        ImageView image,feeling;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            message=itemView.findViewById(R.id.message);
            image=itemView.findViewById(R.id.image);
            feeling=itemView.findViewById(R.id.feeling);
        }
    }

}
