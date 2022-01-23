package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.AdapterParticipantAdd;
import com.rakib.soberpoint.items.ItemFriendList;
import com.squareup.picasso.Picasso;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddPerticipantActivity extends AppCompatActivity {
    RecyclerView recycler_add;
    String groupId,myGroupRole;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    CircleImageView profile;
    TextView name;
    List<ItemFriendList> itemFriendListList=new ArrayList<>();
    AdapterParticipantAdd adapterParticipantAdd;
    ImageView imageView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_perticipant);
        recycler_add=findViewById(R.id.recycler_add);
        recycler_add.setHasFixedSize(true);
        groupId=getIntent().getStringExtra("groupId");
        myGroupRole=getIntent().getStringExtra("myrole");
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        imageView2=findViewById(R.id.imageView2);
        imageView2.setOnClickListener(v -> finish());
        profile=findViewById(R.id.profile);
        name=findViewById(R.id.name);
        itemFriendListList.clear();
        firebaseFirestore.collection("Groups").document(groupId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String groupTitle=task.getResult().getString("groupTitle");
                String groupIcon=task.getResult().getString("groupIcon");
                Picasso.get().load(groupIcon).into(profile);
                firebaseFirestore.collection("Groups").document(groupId).collection("Participants").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            myGroupRole=task.getResult().getString("role");
                            name.setText(groupTitle+" ("+myGroupRole+")");
                        }
                    }
                });
            }
        });
        try {
            firebaseFirestore.collection("Friend").document(firebaseAuth.getCurrentUser().getUid()).collection("friends").addSnapshotListener((value, error) -> {
                try {
                    for (DocumentChange documentSnapshot : value.getDocumentChanges()) {
                        ItemFriendList itemPost = documentSnapshot.getDocument().toObject(ItemFriendList.class);
                        itemFriendListList.add(itemPost);
                    }
                    adapterParticipantAdd = new AdapterParticipantAdd(this, itemFriendListList, groupId, myGroupRole);
                    recycler_add.setAdapter(adapterParticipantAdd);
                    adapterParticipantAdd.notifyDataSetChanged();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
}