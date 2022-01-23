package com.rakib.soberpoint.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.HomeActivity;
import com.rakib.soberpoint.adapter.FriendListAdapter;
import com.rakib.soberpoint.items.ItemFriendList;
import com.rakib.soberpoint.utils.BackPressedFragment;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends Fragment {
    Context context;
    RecyclerView friend_list_recycler;
    List<ItemFriendList> itemFriendListList=new ArrayList<>();
    FriendListAdapter adapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public FriendListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView= inflater.inflate(R.layout.fragment_friend_list, container, false);
        context=container.getContext();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        friend_list_recycler=itemView.findViewById(R.id.friend_list_recycler);
        friend_list_recycler.setHasFixedSize(true);
        friend_list_recycler.setLayoutManager(new LinearLayoutManager(context));
        adapter=new FriendListAdapter(context,itemFriendListList);
        friend_list_recycler.setAdapter(adapter);

        try {
            firebaseFirestore.collection("Friend").document(firebaseAuth.getCurrentUser().getUid()).collection("friends").addSnapshotListener((value, error) -> {
                try {
                    itemFriendListList.clear();
                    for (DocumentChange documentSnapshot : value.getDocumentChanges()) {
                        if (documentSnapshot.getType()== DocumentChange.Type.ADDED) {
                            ItemFriendList itemPost = documentSnapshot.getDocument().toObject(ItemFriendList.class);
                            itemFriendListList.add(itemPost);
                            adapter.notifyDataSetChanged();
                        }
                    }

                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }




        return itemView;
    }


}