package com.rakib.soberpoint.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.Notification.APISERVICESHIT;
import com.rakib.soberpoint.Notification.Client;
import com.rakib.soberpoint.Notification.Data;
import com.rakib.soberpoint.Notification.Response;
import com.rakib.soberpoint.Notification.Sender;
import com.rakib.soberpoint.Notification.Token;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.CommentAdapter;
import com.rakib.soberpoint.items.CommentItem;
import com.rakib.soberpoint.utils.OptionsUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class CommentActivity extends AppCompatActivity {
    String documentId, user_id, feeling, description, image1, name, sender_name;
    TextView desc, timedate, name1, count, comment_count, textView2;
    RoundedImageView image;
    ImageView postImage;
    ImageView like, comment;
    long milli;
    FirebaseFirestore firebaseFirestore;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    Button btn_send_sms;
    ImageButton back;
    EditText et_send_msg;
    FirebaseAuth firebaseAuth;
    APISERVICESHIT apiserviceshit;
    RecyclerView recyclerView;
    List<CommentItem> commentItemList = new ArrayList<>();
    CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        apiserviceshit = Client.getRetrofit("https://fcm.googleapis.com/").create(APISERVICESHIT.class);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        documentId = getIntent().getStringExtra("id");
        user_id = getIntent().getStringExtra("user_id");
        feeling = getIntent().getStringExtra("feeling");
        description = getIntent().getStringExtra("description");
        image1 = getIntent().getStringExtra("image");
        milli = getIntent().getLongExtra("milli", -1);
        sender_name = OptionsUtils.getStringPrefs(CommentActivity.this, "NAME", "");
        like = findViewById(R.id.like);
        count = findViewById(R.id.count);
        image = findViewById(R.id.image);
        comment = findViewById(R.id.iv_comment);
        comment_count = findViewById(R.id.comment_count);
        desc = findViewById(R.id.desc);
        textView2 = findViewById(R.id.textView2);
        postImage = findViewById(R.id.postImage);
        timedate = findViewById(R.id.timedate);
        name1 = findViewById(R.id.name);
        if (!description.equals("null")) {
            desc.setVisibility(View.VISIBLE);
            desc.setText(description);
        } else {
            desc.setVisibility(View.GONE);
        }
        if (!image1.equals("null")) {
            Picasso.get().load(image1)
                    .into(postImage);
        } else {
            postImage.setVisibility(View.GONE);
        }
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                name = task.getResult().getString("name");

                textView2.setText(name + "'s post");
                String profile = task.getResult().getString("image");
                if (!feeling.equals("null")) {
                    name1.setText(name);
                } else {
                    name1.setText(name);
                }
                Glide.with(CommentActivity.this).load(profile).into(image);

            } else {
                Log.d("error in image", task.getException().getLocalizedMessage());
            }
        });
        timedate.setText(getTimeAgo(milli));

        try {
            firebaseFirestore.collection("Post").document(documentId).collection("Likes").addSnapshotListener((value, error) -> {

                try {
                    if (!value.isEmpty()) {
                        int count1 = value.size();
                        count.setText("" + count1);
                    } else {
                        count.setText("0");
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        recyclerView = findViewById(R.id.recycler_comment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new CommentAdapter(this, commentItemList);
        recyclerView.setAdapter(adapter);
        back = findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        et_send_msg = findViewById(R.id.et_send_msg);
        btn_send_sms = findViewById(R.id.btn_send_sms);
        try {
            firebaseFirestore.collection("Post")
                    .document(documentId)
                    .collection("Comments")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        try {


                            for (DocumentChange documentSnapshot : value.getDocumentChanges()) {
                                try {
                                    if (documentSnapshot.getType() == DocumentChange.Type.ADDED) {
                                        CommentItem itemPost = documentSnapshot.getDocument().toObject(CommentItem.class);
                                        commentItemList.add(itemPost);
                                        adapter.notifyDataSetChanged();
                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (commentItemList.size() > 0) {
                                comment_count.setText("" + commentItemList.size());

                            } else {
                                comment_count.setText("0");
                            }
                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        btn_send_sms.setOnClickListener(v -> {
            btn_send_sms.setEnabled(false);
            btn_send_sms.setClickable(false);
            String comment = et_send_msg.getText().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("comment", comment);
            map.put("user_id", firebaseAuth.getCurrentUser().getUid());
            map.put("time", FieldValue.serverTimestamp());
            firebaseFirestore.collection("Post").document(documentId).collection("Comments").add(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    btn_send_sms.setEnabled(true);
                    btn_send_sms.setClickable(true);
                    et_send_msg.setText("");
                    adapter.notifyDataSetChanged();
                    SendNotification(user_id, sender_name);

                }
            });
        });


    }

    private void SendNotification(String friendid, String nameofsender) {

        String current_user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Tokens").document(friendid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                try {


                    assert value != null;
                    Token objectotoken = value.toObject(Token.class);
                    assert objectotoken != null;
                    String token = objectotoken.getToken();


                    Data data = new Data(nameofsender + " comment on your post", "New comment posted!!!", current_user_id, "comment");

                    Sender sender = new Sender(data, token);
                    apiserviceshit.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            if (response.code() == 200) {

                                if (response.body().success != 1) {


                                }


                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });

                }catch (AssertionError ee){
                    ee.printStackTrace();
                }


            }
        });
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