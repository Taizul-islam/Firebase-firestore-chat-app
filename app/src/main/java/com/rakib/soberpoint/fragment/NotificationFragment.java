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
import com.google.firebase.firestore.Query;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.HomeActivity;
import com.rakib.soberpoint.adapter.FriendRequestAdapter;
import com.rakib.soberpoint.items.FriendRequest;
import com.rakib.soberpoint.utils.BackPressedFragment;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    Context context;
    RecyclerView friend_request_recycler;
    List<FriendRequest> friendRequestList = new ArrayList<>();
    FriendRequestAdapter adapter;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public NotificationFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_notification, container, false);
        context = container.getContext();
        friend_request_recycler = itemView.findViewById(R.id.friend_request_recycler);
        friend_request_recycler.setHasFixedSize(true);
        friend_request_recycler.setLayoutManager(new LinearLayoutManager(context));
        friendRequestList.clear();
        adapter = new FriendRequestAdapter(context, friendRequestList);
        friend_request_recycler.setAdapter(adapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            firebaseFirestore.collection("Request")
                    .document(firebaseAuth.getCurrentUser().getUid())
                    .collection("receive")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        try {
                            friendRequestList.clear();
                            for (DocumentChange documentSnapshot : value.getDocumentChanges()) {


                                if (documentSnapshot.getType() == DocumentChange.Type.ADDED) {
                                    String blogpostid1 = documentSnapshot.getDocument().getId();
                                    FriendRequest itemPost1 = documentSnapshot.getDocument().toObject(FriendRequest.class).withId(blogpostid1);
                                    friendRequestList.add(itemPost1);
                                    adapter.notifyDataSetChanged();

                                }


                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                    });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return itemView;
    }


}