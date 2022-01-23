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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.utils.OptionsUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class SingleImageUploadActivity extends AppCompatActivity {

    RoundedImageView profile_image;
    RoundedImageView profile_image1;
    ImageButton back;
    Uri postImageUri;
    int CHOOSE_IMAGE=2;
    final static int PERMISSION_CODE=123;
    TextView title;
    String data;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    String user_id;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image_upload);
        data=getIntent().getStringExtra("title");
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        progressBar=findViewById(R.id.progress_horizontal);
        profile_image = findViewById(R.id.profile_image);
        profile_image.setImageResource(R.drawable.ic_user);
        profile_image1=findViewById(R.id.profile_image1);
        title=findViewById(R.id.textview999);
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> {
            finish();
        });
        if (data.equals("cover")){
            title.setText("Upload cover photo");
            profile_image1.setVisibility(View.VISIBLE);
        }
        if (data.equals("profile")){
            title.setText("Upload profile photo");
            profile_image.setVisibility(View.VISIBLE);
        }
        try {
            user_id=firebaseAuth.getCurrentUser().getUid();
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    public void choose_image(View view) {
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

    }

    public void btn_update(View view) {
        progressBar.setVisibility(View.VISIBLE);
        if (postImageUri==null){
            progressBar.setVisibility(View.GONE);
            Toast.makeText(SingleImageUploadActivity.this, "Please choose one picture.", Toast.LENGTH_LONG).show();
            return;
        }
        String random= UUID.randomUUID().toString();
        StorageReference filePath=storageReference.child("user_cover_profile_image").child(random+".jpg");
        UploadTask uploadTask = filePath.putFile(postImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                task -> {
                    String fileLink = task.getResult().toString();
                    OptionsUtils.savePrefs(SingleImageUploadActivity.this, "IMAGE", fileLink);
                    if (data.equals("cover")){
                        firebaseFirestore.collection("Users").document(user_id).update("cover",fileLink).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                Intent intent=new Intent(SingleImageUploadActivity.this,HomeActivity.class);
                                intent.putExtra("type","home");
                                startActivity(intent);
                                Toast.makeText(SingleImageUploadActivity.this,"Updated",Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }else {
                        firebaseFirestore.collection("Users").document(user_id).update("image",fileLink).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                Intent intent=new Intent(SingleImageUploadActivity.this,HomeActivity.class);
                                intent.putExtra("type","home");
                                startActivity(intent);
                                finish();
                                Toast.makeText(SingleImageUploadActivity.this,"Updated",Toast.LENGTH_LONG).show();
                            }
                        });
                    }



                }));






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
            postImageUri =  data.getData();
            UCrop.of(postImageUri, Uri.fromFile(new File(getCacheDir(), ".jpg")))
                    .withAspectRatio(10, 10)
                    .withMaxResultSize(200, 200)
                    .start(SingleImageUploadActivity.this);


        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            postImageUri = UCrop.getOutput(data);
            profile_image.setImageURI(postImageUri);
            profile_image1.setImageURI(postImageUri);

        }
    }
}