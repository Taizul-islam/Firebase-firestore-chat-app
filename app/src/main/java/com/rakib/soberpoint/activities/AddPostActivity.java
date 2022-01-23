package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {
    MaterialCardView getPhoto;
    Uri postImageUri;
    int CHOOSE_IMAGE = 2;
    final static int PERMISSION_CODE=23;
    RoundedImageView image;
    ImageView post_image;
    EditText et_say_something;
    String msg;
    MaterialButton btn_post;
    ImageButton back;
    TextView name,email;
    ProgressBar progress_horizontal;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String feeling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        getPhoto=findViewById(R.id.getPhoto);
        post_image=findViewById(R.id.post_image);
        et_say_something=findViewById(R.id.et_say_something);
        btn_post=findViewById(R.id.btn_post);
        btn_post.setEnabled(false);
        btn_post.setClickable(false);
        back=findViewById(R.id.back);
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
                                Glide.with(AddPostActivity.this).load(image1).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(image);
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

        back.setOnClickListener(v -> finish());
        getPhoto.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_IMAGE);
//                    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).
//                            setMinCropResultSize(512, 512).
//                            setAspectRatio(1, 1).start(AddPostActivity.this);
                }
            }
            else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_IMAGE);
//                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).
//                        setMinCropResultSize(512, 512).
//                        setAspectRatio(1, 1).start(AddPostActivity.this);
            }
        });
        et_say_something.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
              if (!TextUtils.isEmpty(s)){
                  btn_post.setBackgroundColor(Color.BLUE);
                  btn_post.setEnabled(true);
                  btn_post.setClickable(true);
              }else {
                  btn_post.setBackgroundColor(Color.parseColor("#D8D8D8"));
                  btn_post.setEnabled(false);
                  btn_post.setClickable(false);
              }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_post.setOnClickListener(v -> {
            progress_horizontal.setVisibility(View.VISIBLE);
            String user=firebaseAuth.getCurrentUser().getUid();
            String msg1=et_say_something.getText().toString();
            String random= UUID.randomUUID().toString();
            if (!TextUtils.isEmpty(msg1)&&!TextUtils.isEmpty(feeling)&&postImageUri!=null){
                StorageReference filePath=storageReference.child("post_image").child(random+".jpg");
                UploadTask uploadTask = filePath.putFile(postImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            String fileLink = task.getResult().toString();
                            Map<String,Object> map=new HashMap<>();
                            map.put("image",fileLink);
                            map.put("message",msg1);
                            map.put("user_id",user);
                            map.put("feeling",feeling);
                            map.put("time",FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    finishAffinity();
                                    Intent intent=new Intent(AddPostActivity.this,HomeActivity.class);
                                    intent.putExtra("type","home");
                                    startActivity(intent);
                                    overridePendingTransition(0,0);
                                    Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                                }
                            });


                        }));
            }
            if (!TextUtils.isEmpty(msg1)&&!TextUtils.isEmpty(feeling)&&postImageUri==null){
                Map<String,Object> map=new HashMap<>();
                map.put("image","null");
                map.put("message",msg1);
                map.put("user_id",user);
                map.put("feeling",feeling);
                map.put("time",FieldValue.serverTimestamp());

                firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        finish();
                        Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                    }
                });
            }
            if (!TextUtils.isEmpty(msg1)&&TextUtils.isEmpty(feeling)&&postImageUri!=null){
                StorageReference filePath=storageReference.child("post_image").child(random+".jpg");
                UploadTask uploadTask = filePath.putFile(postImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            String fileLink = task.getResult().toString();
                            Map<String,Object> map=new HashMap<>();
                            map.put("image",fileLink);
                            map.put("message",msg1);
                            map.put("user_id",user);
                            map.put("feeling","null");
                            map.put("time",FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    finish();
                                    Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                                }
                            });


                        }));
            }
            if (!TextUtils.isEmpty(msg1)&&TextUtils.isEmpty(feeling)&&postImageUri==null){
                Map<String,Object> map=new HashMap<>();
                map.put("image","null");
                map.put("message",msg1);
                map.put("user_id",user);
                map.put("feeling","null");
                map.put("time",FieldValue.serverTimestamp());

                firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()){
                        finish();
                        Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                    }
                });
            }
            if (TextUtils.isEmpty(msg1)&&!TextUtils.isEmpty(feeling)&&postImageUri!=null){
                StorageReference filePath=storageReference.child("post_image").child(random+".jpg");
                UploadTask uploadTask = filePath.putFile(postImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            String fileLink = task.getResult().toString();
                            Map<String,Object> map=new HashMap<>();
                            map.put("image",fileLink);
                            map.put("message","null");
                            map.put("user_id",user);
                            map.put("feeling",feeling);
                            map.put("time",FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    finish();
                                    Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                                }
                            });


                        }));
            }
            if (TextUtils.isEmpty(msg1)&&TextUtils.isEmpty(feeling)&&postImageUri!=null){
                StorageReference filePath=storageReference.child("post_image").child(random+".jpg");
                UploadTask uploadTask = filePath.putFile(postImageUri);
                uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            String fileLink = task.getResult().toString();
                            Map<String,Object> map=new HashMap<>();
                            map.put("image",fileLink);
                            map.put("message","null");
                            map.put("user_id",user);
                            map.put("feeling","null");
                            map.put("time",FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Post").add(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()){
                                    finish();
                                    Toast.makeText(AddPostActivity.this,"Post uploaded",Toast.LENGTH_LONG).show();
                                }
                            });


                        }));
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
              //  CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMinCropResultSize(512, 512).setAspectRatio(1, 1).start(AddPostActivity.this);
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
            CropImage.activity(postImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .setRequestedSize(1000,1000)
                    .setMinCropResultSize(1000,1000)
                    .start(AddPostActivity.this);
//            UCrop.Options options = new UCrop.Options();
//            options.useSourceImageAspectRatio();
//            options.setAspectRatioOptions(1,
//                    new AspectRatio("WOW", 1, 2),
//                    new AspectRatio("MUCH", 3, 4),
//                    new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
//                    new AspectRatio("SO", 16, 9),
//                    new AspectRatio("ASPECT", 1, 1));
//            UCrop.of(postImageUri, Uri.fromFile(new File(getCacheDir(), ".jpg")))
//                    .withOptions(options)
//                    .start(AddPostActivity.this);



        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri = result.getUri();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                String path = postImageUri.getPath().toString();
                BitmapFactory.decodeFile(path, options);
                int imageHeight = options.outHeight;
                int imageWidth = options.outWidth;
                Log.d("height",""+imageHeight);
                Log.d("width",""+imageWidth);
                post_image.setImageURI(postImageUri);
                post_image.setMaxHeight(imageHeight);
                post_image.setMaxWidth(imageWidth);
                btn_post.setBackgroundColor(Color.BLUE);
                btn_post.setEnabled(true);
                btn_post.setClickable(true);
                msg=et_say_something.getText().toString();
                if (TextUtils.isEmpty(msg)){
                    et_say_something.setHint("Please say something about your photo.");
                }else {
                    et_say_something.setHint("Whats on your mind?");
                }
            }
        }
//        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
//            assert data != null;
//            postImageUri = UCrop.getOutput(data);
//            post_image.setImageURI(postImageUri);

//
//        }
    }

}