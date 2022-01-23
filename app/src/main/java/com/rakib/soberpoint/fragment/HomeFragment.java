package com.rakib.soberpoint.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rakib.soberpoint.activities.DailyInspirationActivity;
import com.rakib.soberpoint.activities.GroupHomeActivity;
import com.rakib.soberpoint.activities.HomeActivity;
import com.rakib.soberpoint.activities.NearbyActivity;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.AddPostActivity;
import com.rakib.soberpoint.adapter.PostAdapter;
import com.rakib.soberpoint.items.ItemPost;
import com.rakib.soberpoint.utils.BackPressedFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    Context context;
    RecyclerView postRecycler;
    List<ItemPost> itemPostList=new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    PostAdapter postAdapter;
    TabLayout tablayout;
    SwipeRefreshLayout swipRefresh;
    FloatingActionButton fab;
    int counter=0;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView= inflater.inflate(R.layout.fragment_home, container, false);
        context=container.getContext();
        swipRefresh=itemView.findViewById(R.id.swipRefresh);
        swipRefresh.setOnRefreshListener(this);
        fab=itemView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, AddPostActivity.class));
            }
        });
        itemPostList.clear();
        tablayout=itemView.findViewById(R.id.tablayout);
        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos=tablayout.getSelectedTabPosition();
                switch (pos){
                    case 1:
                        startActivity(new Intent(context, GroupHomeActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(context, NearbyActivity.class));
                        break;
                    case 3:
                        startActivity(new Intent(context, DailyInspirationActivity.class));
                        break;


                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        postRecycler=itemView.findViewById(R.id.postRecycler);
        postRecycler.setHasFixedSize(true);
        postRecycler.setLayoutManager(new LinearLayoutManager(context));
        firebaseFirestore=FirebaseFirestore.getInstance();
        postAdapter=new PostAdapter(context,itemPostList);
        postRecycler.setAdapter(postAdapter);
        postRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() !=View.VISIBLE) {
                    fab.show();
                }
            }
        });
        getData();


        return itemView;
    }

    private void getData() {
        itemPostList.clear();
        Query firstQuery=firebaseFirestore.collection("Post").orderBy("time", Query.Direction.DESCENDING);
//        firstQuery.addSnapshotListener((value, error) -> {
//            try {
//
//                    for (DocumentChange documentSnapshot:value.getDocumentChanges()){
//                        if (documentSnapshot.getType()== DocumentChange.Type.ADDED){
//
//                                String blogpostid=documentSnapshot.getDocument().getId();
//                                ItemPost itemPost=documentSnapshot.getDocument().toObject(ItemPost.class).withId(blogpostid);
//                                itemPostList.add(itemPost);
//                                postAdapter.notifyDataSetChanged();
//
//
//                        }
//                    }
//
//
//
//            }catch (NullPointerException e){
//                e.printStackTrace();
//            }
//
//
//
//        });
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException e) {
                if (e != null) {

                    return;
                }
                try {
                    itemPostList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String blogpostid=doc.getId();
                        ItemPost itemPost=doc.toObject(ItemPost.class).withId(blogpostid);
                        itemPostList.add(itemPost);

                    }
                    postAdapter.notifyDataSetChanged();
                }catch (NullPointerException e1){
                    e1.printStackTrace();
                }
            }
        });
        swipRefresh.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        itemPostList.clear();
        swipRefresh.setRefreshing(true);
        postAdapter.notifyDataSetChanged();
        getData();
    }


}