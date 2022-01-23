package com.rakib.soberpoint.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.HomeActivity;
import com.rakib.soberpoint.activities.LoginActivity;
import com.rakib.soberpoint.activities.SingleImageUploadActivity;
import com.rakib.soberpoint.adapter.MyPostAdapter;
import com.rakib.soberpoint.items.ItemPost;
import com.rakib.soberpoint.utils.BackPressedFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileFragment extends Fragment  {
    Context context;
    RecyclerView recycler_my_post;
    List<ItemPost> itemPostList=new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    MyPostAdapter postAdapter;
    FirebaseAuth firebaseAuth;
    String user_id;
    TextView profile_name,bio,ssc,hsc,graduation,from;
    CircleImageView cover,profile,profile_image;
    RoundedImageView user_cover_image;
    MaterialButton logout;
    public MyProfileFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_my_profile, container, false);
        context = container.getContext();
        recycler_my_post = itemView.findViewById(R.id.recycler_my_post);
        recycler_my_post.setHasFixedSize(true);

        recycler_my_post.setLayoutManager(new LinearLayoutManager(context));
        firebaseFirestore = FirebaseFirestore.getInstance();
        postAdapter = new MyPostAdapter(context, itemPostList);
        recycler_my_post.setAdapter(postAdapter);
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            user_id = firebaseAuth.getCurrentUser().getUid();

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

        logout=itemView.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            startActivity(new Intent(context, LoginActivity.class));
            ((Activity)context).finish();
        });
        profile_image=itemView.findViewById(R.id.profile_image);
        user_cover_image=itemView.findViewById(R.id.user_cover_image);
        profile_name=itemView.findViewById(R.id.profile_name);
        profile_name.setOnClickListener(v -> showDialogView("Enter profile name", "name"));
        bio=itemView.findViewById(R.id.bio);
        bio.setOnClickListener(v -> showDialogView("Write about yourself", "bio"));
        ssc=itemView.findViewById(R.id.ssc);
        ssc.setOnClickListener(v -> showDialogView("Write your ssc institute", "ssc"));
        hsc=itemView.findViewById(R.id.hsc);
        hsc.setOnClickListener(v -> showDialogView("Write your hsc institute", "hsc"));
        graduation=itemView.findViewById(R.id.graduation);
        graduation.setOnClickListener(v -> showDialogView("Write your graduation institute", "graduation"));
        from=itemView.findViewById(R.id.from);
        from.setOnClickListener(v -> showDialogView("Where are you from?", "from"));
        cover=itemView.findViewById(R.id.cover);
        cover.setOnClickListener(v -> {
            Intent intent=new Intent(context, SingleImageUploadActivity.class);
            intent.putExtra("title","cover");
            startActivity(intent);
        });
        profile=itemView.findViewById(R.id.profile);
        profile.setOnClickListener(v -> {
            Intent intent=new Intent(context, SingleImageUploadActivity.class);
            intent.putExtra("title","profile");
            startActivity(intent);
        });
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    String cover_image = task.getResult().getString("cover");
                    String user_profile_image = task.getResult().getString("image");
                    if (!TextUtils.isEmpty(cover_image)) {
                        Picasso.get().load(cover_image).placeholder(R.drawable.launcher_image).error(R.drawable.launcher_image).into(user_cover_image);
                    } else {
                        //nothing
                    }
                    if (user_profile_image.equals("")) {
                        profile_image.setImageResource(R.drawable.launcher_image);
                    } else {
                        Picasso.get().load(user_profile_image).placeholder(R.drawable.ic_user).error(R.drawable.ic_user).into(profile_image);
                    }

                    String name = task.getResult().getString("name");
                    String biodata = task.getResult().getString("bio");
                    String sscdata = task.getResult().getString("ssc");
                    String hscdata = task.getResult().getString("hsc");
                    String graduationdata = task.getResult().getString("graduation");
                    String fromdata = task.getResult().getString("from");
                    profile_name.setText(name);
                    if (!biodata.equals("")) {
                        bio.setText(biodata);
                    }
                    if (!sscdata.equals("")) {
                        ssc.setText(sscdata);
                    }
                    if (!hscdata.equals("")) {
                        hsc.setText(hscdata);
                    }
                    if (!graduationdata.equals("")) {
                        graduation.setText(graduationdata);
                    }
                    if (!fromdata.equals("")) {
                        from.setText(fromdata);
                    }


                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        });



        return itemView;
    }
    private void showDialogView(String hint, String key) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.background_register, context.getTheme()));
        View view = LayoutInflater.from(context).inflate(R.layout.single_edit_text_view_layout, null);
        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(hint);
        EditText editText = view.findViewById(R.id.common_edit_text);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        if (view.getParent() != null)
            ((ViewGroup) view.getParent()).removeView(view);
        builder.setView(view);
        builder.setPositiveButton("Save", (DialogInterface dialog, int which) -> {
            String name = editText.getText().toString();
            if (TextUtils.isEmpty(name) && key.equals("name")) {
                Toast.makeText(context, "Please enter your father's name", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name) && key.equals("bio")) {
                Toast.makeText(context, "Please write some word about yourself ", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name) && key.equals("ssc")) {
                Toast.makeText(context, "Please write your ssc institute", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name) && key.equals("hsc")) {
                Toast.makeText(context, "Please write your hsc institute", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name) && key.equals("graduation")) {
                Toast.makeText(context, "Please write your graduation institute", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(name) && key.equals("from")) {
                Toast.makeText(context, "Please write Where are you from?", Toast.LENGTH_LONG).show();
                return;
            }



            pushData(user_id, name, key, dialog);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void pushData(String user_id, String data, String key, DialogInterface dialogInterface) {

            firebaseFirestore.collection("Users").document(user_id).update(key,data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        dialogInterface.dismiss();
                        Toast.makeText(context,"Updated",Toast.LENGTH_LONG).show();
                        if (key.equals("name")){

                            profile_name.setText(data);
                        }
                        if (key.equals("bio")){
                            bio.setText(data);
                        }
                        if (key.equals("ssc")){
                            ssc.setText(data);
                        }
                        if (key.equals("hsc")){
                            hsc.setText(data);
                        }
                        if (key.equals("graduation")){
                            graduation.setText(data);
                        }
                        if (key.equals("from")){
                            from.setText(data);
                        }

                    }
                }
            });


    }


}