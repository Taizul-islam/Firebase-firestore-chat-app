package com.rakib.soberpoint.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.NearbyAdapter;
import com.rakib.soberpoint.items.ItemNearby;

import java.util.ArrayList;
import java.util.List;

public class NearbyActivity extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    List<ItemNearby> itemNearbyList=new ArrayList<>();
    NearbyAdapter adapter;
    RecyclerView recyclerView;
    ImageButton back;
    EditText input_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        input_search=findViewById(R.id.input_search);
        input_search.setOnClickListener(v -> startActivity(new Intent(NearbyActivity.this,SearchActivity.class)));
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView=findViewById(R.id.nearby_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseFirestore=FirebaseFirestore.getInstance();
        try {
            firebaseFirestore.collection("Location").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    double lat=task.getResult().getDouble("lat");
                    double lng=task.getResult().getDouble("lng");
                    adapter = new NearbyAdapter(NearbyActivity.this, itemNearbyList, lat, lng);
                    recyclerView.setAdapter(adapter);
                    firebaseFirestore.collection("Location").addSnapshotListener((value, error) -> {
                        itemNearbyList.clear();
                        try {
                            for (QueryDocumentSnapshot documentSnapshot : value) {

                                    ItemNearby itemPost = documentSnapshot.toObject(ItemNearby.class);
                                    itemNearbyList.add(itemPost);


                            }
                            adapter.notifyDataSetChanged();

                        }catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    });

                }
            });

        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
}