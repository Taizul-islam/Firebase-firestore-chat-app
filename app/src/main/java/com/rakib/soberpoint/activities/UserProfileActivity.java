package com.rakib.soberpoint.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.Notification.APISERVICESHIT;
import com.rakib.soberpoint.Notification.Client;
import com.rakib.soberpoint.Notification.Data;
import com.rakib.soberpoint.Notification.Response;
import com.rakib.soberpoint.Notification.Sender;
import com.rakib.soberpoint.Notification.Token;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.UserPostAdapter;
import com.rakib.soberpoint.items.ItemPost;
import com.rakib.soberpoint.utils.OptionsUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

import static android.content.ContentValues.TAG;

public class UserProfileActivity extends AppCompatActivity {
    RecyclerView recycler_my_post;
    List<ItemPost> itemPostList=new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    UserPostAdapter postAdapter;
    String user_id,current_user_id,sender_name;
    TextView profile_name,bio,ssc,hsc,graduation,from,add_connection;
    CircleImageView cover,profile,profile_image;
    RoundedImageView user_cover_image;
    FirebaseAuth firebaseAuth;
    boolean isSent=true;
    String token;
    String useridfortoken;
    APISERVICESHIT apiserviceshit;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        apiserviceshit = Client.getRetrofit("https://fcm.googleapis.com/").create(APISERVICESHIT.class);
        sender_name= OptionsUtils.getStringPrefs(UserProfileActivity.this,"NAME","");
        user_id=getIntent().getStringExtra("id");
        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();
        recycler_my_post = findViewById(R.id.recycler_my_post);
        recycler_my_post.setHasFixedSize(true);
        recycler_my_post.setLayoutManager(new LinearLayoutManager(UserProfileActivity.this));
        firebaseFirestore = FirebaseFirestore.getInstance();
        postAdapter = new UserPostAdapter(UserProfileActivity.this, itemPostList);
        recycler_my_post.setAdapter(postAdapter);
        try {
            Query firstQuery = firebaseFirestore.collection("Post")
                    .whereEqualTo("user_id", user_id)
                    .orderBy("time", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.d("error", error.getLocalizedMessage());
                    return;
                }
                try {

                    itemPostList.clear();
                    for (QueryDocumentSnapshot doc : value) {

                            String blogpostid = doc.getId();
                            ItemPost itemPost = doc.toObject(ItemPost.class).withId(blogpostid);
                            itemPostList.add(itemPost);


                    }
                    postAdapter.notifyDataSetChanged();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        profile_image=findViewById(R.id.profile_image);
        user_cover_image=findViewById(R.id.user_cover_image);
        profile_name=findViewById(R.id.profile_name);
        bio=findViewById(R.id.bio);
        ssc=findViewById(R.id.ssc);
        hsc=findViewById(R.id.hsc);
        graduation=findViewById(R.id.graduation);
        from=findViewById(R.id.from);
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String cover_image=task.getResult().getString("cover");
                String user_profile_image=task.getResult().getString("image");
                if (!TextUtils.isEmpty(cover_image)){
                    Picasso.get().load(cover_image).placeholder(R.drawable.launcher_image).error(R.drawable.launcher_image).into(user_cover_image);
                }
                if (user_profile_image.equals("")){
                    profile_image.setImageResource(R.drawable.launcher_image);
                }else {
                    Picasso.get().load(user_profile_image).placeholder(R.drawable.ic_user).error(R.drawable.ic_user).into(profile_image);
                }

                String name=task.getResult().getString("name");
                String biodata=task.getResult().getString("bio");
                String sscdata=task.getResult().getString("ssc");
                String hscdata=task.getResult().getString("hsc");
                String graduationdata=task.getResult().getString("graduation");
                String fromdata=task.getResult().getString("from");
                profile_name.setText(name);
                if (!biodata.equals("")){
                    bio.setText(biodata);
                }
                if (!sscdata.equals("")){
                    ssc.setText(sscdata);
                }
                if (!hscdata.equals("")){
                    hsc.setText(hscdata);
                }
                if (!graduationdata.equals("")){
                    graduation.setText(graduationdata);
                }
                if (!fromdata.equals("")){
                    from.setText(fromdata);
                }


            }
        });
        add_connection=findViewById(R.id.add_connection);
        add_connection.setOnClickListener(v -> {
            Map<String,Object> map=new HashMap<>();
            map.put("user_id",user_id);
            map.put("current_id",current_user_id);
            map.put("time", FieldValue.serverTimestamp());
            map.put("type","request");
            map.put("group_id","");
            if (isSent){
                firebaseFirestore.collection("Request").document(user_id).collection("receive").add(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        firebaseFirestore.collection("Request").document(current_user_id).collection("sent").add(map).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Toast.makeText(UserProfileActivity.this,"request sent",Toast.LENGTH_LONG).show();
                                add_connection.setText("Connection request sent");
                                isSent=false;
                                SendNotification(user_id,sender_name);
                            }
                        });
                    }
                });
            }

        });
        firebaseFirestore.collection("Request").document(current_user_id).collection("sent").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (DocumentSnapshot documentSnapshot:task.getResult()){
                    String id=documentSnapshot.getString("user_id");
                    if (id.equals(user_id)){
                       add_connection.setText("Connection request sent");
                       isSent=false;
                    }
                }
            }
        });
    }
    private void SendNotification(String friendid, String nameofsender) {


        useridfortoken = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Tokens").document(friendid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                try {


                    assert value != null;
                    Token objectotoken = value.toObject(Token.class);
                    assert objectotoken != null;
                    token = objectotoken.getToken();

                    Log.d(TAG, "onEvent: " + token);


                    Data data = new Data(nameofsender + " sent you connection request", "New connection request!!!", current_user_id, "request");

                    Sender sender = new Sender(data, token);
                    apiserviceshit.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            if (response.code() == 200) {

                                if (response.body().success != 1) {

                                    Toast.makeText(UserProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                                }


                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }catch (AssertionError e){
                    e.printStackTrace();
                }


            }
        });
    }
}