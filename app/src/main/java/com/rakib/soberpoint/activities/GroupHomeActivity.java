package com.rakib.soberpoint.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.GroupListAdapter;
import com.rakib.soberpoint.items.ItemGroup;

import java.util.ArrayList;
import java.util.List;

public class GroupHomeActivity extends AppCompatActivity {
    ImageView imageView4;
    FirebaseFirestore firebaseFirestore;
    RecyclerView group_list_recycler;
    List<ItemGroup> itemGroupList=new ArrayList<>();
    GroupListAdapter groupListAdapter;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_home);
        firebaseFirestore=FirebaseFirestore.getInstance();
        imageView4=findViewById(R.id.imageView4);
        imageView4.setOnClickListener(v -> startActivity(new Intent(GroupHomeActivity.this, CreateGroupActivity.class)));
        group_list_recycler=findViewById(R.id.group_list_recycler);
        group_list_recycler.setHasFixedSize(true);
        itemGroupList.clear();
        group_list_recycler.setLayoutManager(new LinearLayoutManager(GroupHomeActivity.this));
        groupListAdapter=new GroupListAdapter(GroupHomeActivity.this,itemGroupList);
        group_list_recycler.setAdapter(groupListAdapter);

            firebaseFirestore.collection("Groups").addSnapshotListener((value, error) -> {
                try {

                    for (QueryDocumentSnapshot documentSnapshot : value) {

                            ItemGroup itemPost = documentSnapshot.toObject(ItemGroup.class);
                            itemGroupList.add(itemPost);


                    }
                    groupListAdapter.notifyDataSetChanged();

                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            });

        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
    }
}