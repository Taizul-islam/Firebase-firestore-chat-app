package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.utils.OptionsUtils;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity {
    ImageButton back;
    TextView name,email;
    RoundedImageView post_image,image;
    ProgressBar progress_horizontal;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    Uri postImageUri;
    int CHOOSE_IMAGE = 2;
    final static int PERMISSION_CODE=23;
    MaterialButton btn_create_group;
    EditText group_title,group_description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        image=findViewById(R.id.image);
        name=findViewById(R.id.name);
        email=findViewById(R.id.email);
        try {
            firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    try {


                        if (task.isSuccessful()) {
                            String image1 = task.getResult().getString("image");
                            if (image1.equals("")) {
                                image.setImageResource(R.drawable.launcher_image);
                            } else {
                                Glide.with(CreateGroupActivity.this).load(image1).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(image);
                            }
                            String nam = task.getResult().getString("name");
                            name.setText(nam);
                            String ema = task.getResult().getString("email");
                            email.setText(ema);
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        progress_horizontal=findViewById(R.id.progress_horizontal);
        progress_horizontal.setVisibility(View.GONE);
        post_image=findViewById(R.id.post_image);
        post_image.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_IMAGE);
                }
            }
            else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_IMAGE);
            }
        });
        btn_create_group=findViewById(R.id.btn_create_group);
        group_title=findViewById(R.id.group_title);
        group_description=findViewById(R.id.group_description);
        btn_create_group.setOnClickListener(v -> {
            progress_horizontal.setVisibility(View.VISIBLE);
           String title=group_title.getText().toString();
           String description=group_description.getText().toString();
           if (TextUtils.isEmpty(title)){
             progress_horizontal.setVisibility(View.GONE);
             Toast.makeText(CreateGroupActivity.this,"Please enter group title",Toast.LENGTH_LONG).show();
             return;
           }
            if (TextUtils.isEmpty(description)){
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(CreateGroupActivity.this,"Please enter group description",Toast.LENGTH_LONG).show();
                return;
            }
            if (postImageUri==null){
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(CreateGroupActivity.this,"Please enter group profile image",Toast.LENGTH_LONG).show();
                return;
            }
            String random= UUID.randomUUID().toString();
            StorageReference filePath=storageReference.child("group_image").child(random+".jpg");
            UploadTask uploadTask = filePath.putFile(postImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                    task -> {
                        String fileLink = task.getResult().toString();
                        Map<String,Object> map=new HashMap<>();
                        map.put("groupId",random);
                        map.put("groupTitle",title);
                        map.put("groupDescription",description);
                        map.put("groupIcon",fileLink);
                        map.put("time", FieldValue.serverTimestamp());
                        map.put("createdBy",firebaseAuth.getCurrentUser().getUid());

                        firebaseFirestore.collection("Groups").document(random).set(map).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                Map<String,Object> map1=new HashMap<>();
                                map1.put("uid",firebaseAuth.getCurrentUser().getUid());
                                map1.put("role","creator");
                                map1.put("time",FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Groups").document(random).collection("Participants").document(firebaseAuth.getCurrentUser().getUid()).set(map1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            finish();
                                            Toast.makeText(CreateGroupActivity.this,"Group created",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }
                        });


                    }));



        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_IMAGE);
            } else {
                Toast.makeText(this, "Permission denied...!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_IMAGE) {
            assert data != null;
            postImageUri = data.getData();
            UCrop.of(postImageUri, Uri.fromFile(new File(getCacheDir(), ".jpg")))
                    .withAspectRatio(10, 10)
                    .withMaxResultSize(600, 600)
                    .start(CreateGroupActivity.this);

        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            postImageUri = UCrop.getOutput(data);
            post_image.setImageURI(postImageUri);

        }
    }
}