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
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.rakib.soberpoint.adapter.GroupChatAdapter;
import com.rakib.soberpoint.items.ItemGroupMessage;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    GroupChatAdapter adapter;
    ArrayList<ItemGroupMessage> messages=new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;
    ProgressDialog dialog;
    TextView name,status;
    CircleImageView profile;
    ImageView imageView2,sendBtn,attachment;
    RecyclerView recyclerView;
    EditText messageBox;
    String groupId,myGroupRole;
    ImageView imageView5,imageView7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        firebaseAuth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name=findViewById(R.id.name);
        imageView5=findViewById(R.id.imageView5);
        imageView7=findViewById(R.id.imageView7);

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
        groupId = getIntent().getStringExtra("id");
        try {
            firebaseFirestore.collection("Groups").document(groupId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String groupTitle = task.getResult().getString("groupTitle");
                        name.setText(groupTitle);
                        String groupIcon = task.getResult().getString("groupIcon");
                        Picasso.get().load(groupIcon).into(profile);
                        firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    myGroupRole = task.getResult().getString("role");

                                }
                            }
                        });
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        adapter = new GroupChatAdapter(this, messages);
        recyclerView.setAdapter(adapter);
        Query query=firebaseFirestore.collection("Groups").document(groupId).collection("Messages").orderBy("time", Query.Direction.ASCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                    if (error != null) {
                        return;
                    }
                    messages.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        ItemGroupMessage message = doc.toObject(ItemGroupMessage.class);
                        messages.add(message);
                    }
                    adapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
        imageView5.setOnClickListener(v -> {
            if (myGroupRole.equals("creator")||myGroupRole.equals("admin")){
               Intent intent=new Intent(GroupChatActivity.this, AddPerticipantActivity.class);
               intent.putExtra("groupId",groupId);
               intent.putExtra("myrole",myGroupRole);
               startActivity(intent);
            }else {
                Toast.makeText(GroupChatActivity.this,"You are not admin or creator of this group",Toast.LENGTH_LONG).show();
            }

        });
        imageView7.setOnClickListener(v -> {
            Intent intent=new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        });



        sendBtn.setOnClickListener(v -> {
            sendBtn.setEnabled(false);
            sendBtn.setClickable(false);
            String messageTxt = messageBox.getText().toString();
            if (TextUtils.isEmpty(messageTxt)){
                Toast.makeText(GroupChatActivity.this,"Cannot send empty message",Toast.LENGTH_LONG).show();
                return;
            }
            messageBox.setText("");
            String randomKey = UUID.randomUUID().toString();
            Map<String,Object> map=new HashMap<>();
            map.put("sender",firebaseAuth.getCurrentUser().getUid());
            map.put("message",messageTxt);
            map.put("time",FieldValue.serverTimestamp());
            map.put("last",""+Calendar.getInstance().getTimeInMillis());
            map.put("type","text");

            firebaseFirestore.collection("Groups").document(groupId).collection("Messages").document(randomKey).set(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    sendBtn.setEnabled(true);
                    sendBtn.setClickable(true);
                    scrollToBottom();
                }
            });


        });

        attachment.setOnClickListener(v -> {
            sendBtn.setEnabled(false);
            sendBtn.setClickable(false);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
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
                    StorageReference reference = storage.getReference().child("groups").child(calendar.getTimeInMillis() + "");
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
                                        messageBox.setText("");

                                        String randomKey = UUID.randomUUID().toString();
                                        Map<String,Object> map=new HashMap<>();
                                        map.put("sender",firebaseAuth.getCurrentUser().getUid());
                                        map.put("message",filePath);
                                        map.put("time",FieldValue.serverTimestamp());
                                        map.put("last",""+Calendar.getInstance().getTimeInMillis());
                                        map.put("type","image");

                                        firebaseFirestore.collection("Groups").document(groupId).collection("Messages").document(randomKey).set(map).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()){
                                                sendBtn.setEnabled(true);
                                                sendBtn.setClickable(true);
                                                scrollToBottom();
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