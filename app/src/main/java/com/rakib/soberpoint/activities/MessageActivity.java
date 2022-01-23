package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.MessagesAdapter;
import com.rakib.soberpoint.items.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    Toolbar toolbar;
    MessagesAdapter adapter;
    ArrayList<Message> messages=new ArrayList<>();
    String senderRoom, receiverRoom;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderUid;
    String receiverUid;
    TextView name,status;
    CircleImageView profile;
    ImageView imageView2,sendBtn,attachment;
    RecyclerView recyclerView;
    EditText messageBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name=findViewById(R.id.name);
        status=findViewById(R.id.status);
        profile=findViewById(R.id.profile);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        imageView2=findViewById(R.id.imageView2);
        sendBtn=findViewById(R.id.sendBtn);
        messageBox=findViewById(R.id.messageBox);
        attachment=findViewById(R.id.attachment);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        imageView2.setOnClickListener(v -> finish());

        receiverUid = getIntent().getStringExtra("uid");
        try {
            senderUid = FirebaseAuth.getInstance().getUid();
            firebaseFirestore.collection("Users").document(receiverUid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String friend_name = task.getResult().getString("name");
                    name.setText(friend_name);
                    String image = task.getResult().getString("image");
                    if (image.equals("")){
                        profile.setImageResource(R.drawable.launcher_image);
                    }else {
                        Picasso.get().load(image).into(profile);
                    }

                    String status1 = task.getResult().getString("status");
                    if (status1.equals("Offline")) {
                        status.setVisibility(View.GONE);
                    } else {
                        status.setText(status1);
                        status.setVisibility(View.VISIBLE);
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        recyclerView.setAdapter(adapter);

        try {
            Query query=firebaseFirestore.collection("chats").document(senderRoom).collection("messages").orderBy("timestamp", Query.Direction.ASCENDING);
            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    messages.clear();
                    if (error != null) {
                        return;
                    }
                    try {


                        for (QueryDocumentSnapshot doc : value) {
                            Message message = doc.toObject(Message.class);
                            message.setMessageId(doc.getId());
                            messages.add(message);
                            adapter.notifyDataSetChanged();
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    recyclerView.scrollToPosition(messages.size()-1);
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }




        sendBtn.setOnClickListener(v -> {
            sendBtn.setEnabled(false);
            sendBtn.setClickable(false);
            String messageTxt = messageBox.getText().toString();
            Message message = new Message(messageTxt, senderUid);
            messageBox.setText("");
            String randomKey = UUID.randomUUID().toString();
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", FieldValue.serverTimestamp());
            firebaseFirestore.collection("chats").document(senderRoom).update(lastMsgObj);
            firebaseFirestore.collection("chats").document(receiverRoom).update(lastMsgObj);
            firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(randomKey).set(message).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    firebaseFirestore.collection("chats").document(receiverRoom).collection("messages").document(randomKey).set(message).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()){
                            sendBtn.setEnabled(true);
                            sendBtn.setClickable(true);
                            scrollToBottom();
                        }
                    });
                }
            });


        });

        attachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });

        final Handler handler = new Handler();
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                firebaseFirestore.collection("Users").document(senderUid).update("status", "Typing....").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    firebaseFirestore.collection("Users").document(senderUid).update("status", "Online").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }
            };
        });


        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25) {
            if(data != null) {
                if(data.getData() != null) {
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String messageTxt = messageBox.getText().toString();

                                        Date date = new Date();
                                        Message message = new Message(messageTxt, senderUid);
                                        message.setMessage("photo");
                                        message.setImageUrl(filePath);
                                        messageBox.setText("");

                                        String randomKey = UUID.randomUUID().toString();

                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", message.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());
                                        firebaseFirestore.collection("chats").document(senderRoom).update(lastMsgObj);
                                        firebaseFirestore.collection("chats").document(receiverRoom).update(lastMsgObj);
                                        firebaseFirestore.collection("chats").document(senderRoom).collection("messages").document(randomKey).set(message).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()){
                                                firebaseFirestore.collection("chats").document(receiverRoom).collection("messages").document(randomKey).set(message).addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()){

                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    private void scrollToBottom() {

        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 1)
            Objects.requireNonNull(recyclerView.getLayoutManager()).smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
    }
}