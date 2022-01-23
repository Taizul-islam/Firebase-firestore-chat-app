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
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GroupEditActivity extends AppCompatActivity {
    String groupId;
    ImageButton back;
    FirebaseFirestore firebaseFirestore;
    EditText group_title,group_description;
    String title="";
    String description="";
    String groupIcon="";
    RoundedImageView post_image;
    MaterialButton btn_create_group;
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    Uri postImageUri;
    int CHOOSE_IMAGE = 2;
    final static int PERMISSION_CODE=23;
    ProgressBar progress_horizontal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        groupId=getIntent().getStringExtra("id");
        group_title=findViewById(R.id.group_title);
        group_description=findViewById(R.id.group_description);
        post_image=findViewById(R.id.post_image);
        progress_horizontal=findViewById(R.id.progress_horizontal);
        btn_create_group=findViewById(R.id.btn_create_group);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loadGroupInfo();
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
        btn_create_group.setOnClickListener(v -> {
            progress_horizontal.setVisibility(View.VISIBLE);
            title=group_title.getText().toString();
            description=group_description.getText().toString();
            if (TextUtils.isEmpty(title)){
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(GroupEditActivity.this,"Please enter group title",Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(description)){
                progress_horizontal.setVisibility(View.GONE);
                Toast.makeText(GroupEditActivity.this,"Please enter group description",Toast.LENGTH_LONG).show();
                return;
            }
            if (postImageUri==null){
                Map<String,Object> map=new HashMap<>();
                map.put("groupTitle",title);
                map.put("groupDescription",description);
                map.put("groupIcon",groupIcon);
                firebaseFirestore.collection("Groups").document(groupId).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                       if (task.isSuccessful()){
                           progress_horizontal.setVisibility(View.GONE);
                           finish();
                           Toast.makeText(GroupEditActivity.this,"Group updated",Toast.LENGTH_LONG).show();
                       }
                    }
                });
            }else {
                String random= UUID.randomUUID().toString();
                StorageReference filePath=storageReference.child("group_image").child(random+".jpg");
                UploadTask uploadTask = filePath.putFile(postImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            String fileLink = task.getResult().toString();
                            Map<String,Object> map=new HashMap<>();
                            map.put("groupTitle",title);
                            map.put("groupDescription",description);
                            map.put("groupIcon",fileLink);
                            firebaseFirestore.collection("Groups").document(groupId).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        progress_horizontal.setVisibility(View.GONE);
                                        finish();
                                        Toast.makeText(GroupEditActivity.this,"Group updated",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }));

            }




        });

    }

    private void loadGroupInfo() {
        firebaseFirestore.collection("Groups").document(groupId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
              if (task.isSuccessful()){
                 title= task.getResult().getString("groupTitle");
                 description= task.getResult().getString("groupDescription");
                  groupIcon= task.getResult().getString("groupIcon");
                  group_title.setText(title);
                  group_description.setText(description);
                  Picasso.get().load(groupIcon).into(post_image);

              }
            }
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
                    .start(GroupEditActivity.this);

        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            postImageUri = UCrop.getOutput(data);
            post_image.setImageURI(postImageUri);

        }
    }
}