package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import com.rakib.soberpoint.Notification.APISERVICESHIT;
import com.rakib.soberpoint.Notification.Client;
import com.rakib.soberpoint.Notification.Data;
import com.rakib.soberpoint.Notification.Response;
import com.rakib.soberpoint.Notification.Sender;
import com.rakib.soberpoint.Notification.Token;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.AdapterParticipantAdd;
import com.rakib.soberpoint.items.ItemFriendList;
import com.rakib.soberpoint.utils.OptionsUtils;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static android.content.ContentValues.TAG;

public class GroupInfoActivity extends AppCompatActivity {
    String groupId, myGroupRole;
    ImageButton back;
    RoundedImageView roundedImageView;
    TextView descritionTv, createdByTv, edit, add, leave, list;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    List<ItemFriendList> itemFriendListList = new ArrayList<>();
    AdapterParticipantAdd adapterParticipantAdd;
    RecyclerView recyclerView;
    Button btn_group_info_button;
    APISERVICESHIT apiserviceshit;
    String createdBy = "", nam = "", current_user_id;
    String groupTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        groupId = getIntent().getStringExtra("groupId");
        apiserviceshit = Client.getRetrofit("https://fcm.googleapis.com/").create(APISERVICESHIT.class);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        edit = findViewById(R.id.edit);

        add = findViewById(R.id.add);
        btn_group_info_button = findViewById(R.id.btn_group_info_button);

        leave = findViewById(R.id.leave);
        list = findViewById(R.id.list);
        recyclerView = findViewById(R.id.recycler_participants);
        recyclerView.setHasFixedSize(true);
        roundedImageView = findViewById(R.id.roundedImageView);
        descritionTv = findViewById(R.id.descritionTv);
        createdByTv = findViewById(R.id.createdByTv);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            current_user_id = firebaseAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            firebaseFirestore.collection("Groups").document(groupId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    groupTitle = task.getResult().getString("groupTitle");
                    String groupIcon = task.getResult().getString("groupIcon");
                    String groupDescription = task.getResult().getString("groupDescription");
                    createdBy = task.getResult().getString("createdBy");
                    Timestamp timestamp = task.getResult().getTimestamp("time");
                    Date date = timestamp.toDate();
                    Picasso.get().load(groupIcon).into(roundedImageView);
                    descritionTv.setText("Group Title: " + groupTitle + "\nGroup Description: " + groupDescription);
                    firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {

                            try {
                                if (task.isSuccessful()) {

                                    myGroupRole = task.getResult().getString("role");

                                    if (myGroupRole.equals("participant")) {
                                        edit.setVisibility(View.GONE);
                                        add.setVisibility(View.GONE);
                                        leave.setVisibility(View.VISIBLE);
                                        leave.setText("Leave Group");


                                    } else if (myGroupRole.equals("admin")) {
                                        edit.setVisibility(View.GONE);
                                        add.setVisibility(View.VISIBLE);
                                        leave.setVisibility(View.VISIBLE);
                                        leave.setText("Leave Group");

                                    } else if (myGroupRole.equals("creator")) {
                                        edit.setVisibility(View.VISIBLE);
                                        add.setVisibility(View.VISIBLE);
                                        leave.setVisibility(View.VISIBLE);
                                        leave.setText("Delete Group");

                                    }
                                    firebaseFirestore.collection("Users").document(createdBy).get().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            String name = task1.getResult().getString("name");
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTimeInMillis(date.getTime());
                                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                            createdByTv.setText(name + " created this group on " + dateTime);
                                            loadParticipants();

                                        } else {
                                            Log.d("error in image", task.getException().getLocalizedMessage());
                                        }
                                    });


                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        try {
            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    try {


                        if (task.isSuccessful()) {
                            nam = task.getResult().getString("name");

                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        btn_group_info_button.setOnClickListener(v -> {
            if (btn_group_info_button.getText().equals("Send message")) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupChatActivity.class);
                intent.putExtra("id", groupId);
                startActivity(intent);
            } else {
                Map<String, Object> map = new HashMap<>();
                map.put("user_id", createdBy);
                map.put("current_id", current_user_id);
                map.put("time", FieldValue.serverTimestamp());
                map.put("type", "group");
                map.put("group_id", groupId);
                try {
                    firebaseFirestore.collection("Request").document(createdBy).collection("receive").add(map).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseFirestore.collection("Request").document(current_user_id).collection("sent").add(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(GroupInfoActivity.this, "request sent", Toast.LENGTH_LONG).show();
                                    btn_group_info_button.setText("Connection request sent");
                                    btn_group_info_button.setClickable(false);
                                    SendNotification();
                                }
                            });
                        }
                    });
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("id", groupId);
                startActivity(intent);
            }
        });
        add.setOnClickListener(v -> {
            Intent intent = new Intent(GroupInfoActivity.this, AddPerticipantActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("myrole", myGroupRole);
            startActivity(intent);
        });
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = "";
                String desc = "";
                String posi = "";
                if (myGroupRole.equals("creator")) {
                    title = "Delete group";
                    desc = "Are you sure want to delete this group permanently";
                    posi = "DELETE";
                } else {
                    title = "Leave group";
                    desc = "Are you sure want to leave this group permanently";
                    posi = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(title);
                builder.setMessage(desc);
                builder.setPositiveButton(posi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (myGroupRole.equals("creator")) {
                            deleteGroup();
                        } else {
                            leaveGroup();
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });


    }

    private void leaveGroup() {
        try {
            firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(firebaseAuth.getCurrentUser().getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(GroupInfoActivity.this, "this user is removed from this group", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void deleteGroup() {
        try {


            firebaseFirestore.collection("Groups").document(groupId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(GroupInfoActivity.this, "Delete", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void loadParticipants() {
        itemFriendListList.clear();
        firebaseFirestore.collection("Groups").document(groupId).collection("Participants").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                for (QueryDocumentSnapshot doc : value) {

                    String uid = doc.getString("uid");
                    if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                        btn_group_info_button.setText("Send message");
                    }
                    firebaseFirestore.collection("Friend").document("Friend").collection("friends").whereEqualTo("friend_id", uid).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                return;
                            }
                            itemFriendListList.clear();
                            for (QueryDocumentSnapshot documentSnapshot : value) {
                                ItemFriendList itemPost = documentSnapshot.toObject(ItemFriendList.class);
                                itemFriendListList.add(itemPost);

                            }
                            adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, itemFriendListList, groupId, myGroupRole);
                            list.setText("Participant (" + itemFriendListList.size() + ")");
                            recyclerView.setAdapter(adapterParticipantAdd);
                        }
                    });
                }

            }
        });

    }

    private void SendNotification() {
        firebaseFirestore.collection("Tokens").document(createdBy).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                try {


                    assert value != null;
                    Token objectotoken = value.toObject(Token.class);
                    assert objectotoken != null;
                    String token = objectotoken.getToken();

                    Log.d(TAG, "onEvent: " + token);


                    Data data = new Data(nam + " want to add your " + groupTitle, "Want to add group", current_user_id, "request");

                    Sender sender = new Sender(data, token);
                    apiserviceshit.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            if (response.code() == 200) {

                                if (response.body().success != 1) {

                                    Toast.makeText(GroupInfoActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                                }


                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {
                            Toast.makeText(GroupInfoActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (AssertionError e){
                    e.printStackTrace();
                }


            }
        });
    }
}