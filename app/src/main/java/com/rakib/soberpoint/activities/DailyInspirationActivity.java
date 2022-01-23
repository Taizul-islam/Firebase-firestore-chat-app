package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

public class DailyInspirationActivity extends AppCompatActivity {
    TextView tv_ins;
    ImageView iv_ins;
    ProgressBar progress_horizontal;
    FirebaseFirestore firebaseFirestore;
    ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_inspiration);
        tv_ins=findViewById(R.id.tv_ins);
        iv_ins=findViewById(R.id.iv_ins);
        progress_horizontal=findViewById(R.id.progress_horizontal);
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Inspiration").document("daily").get().addOnCompleteListener(task -> {
             progress_horizontal.setVisibility(View.GONE);
            Picasso.get().load(task.getResult().getString("iv_ins")).into(iv_ins);
            tv_ins.setText(task.getResult().getString("tv_ins"));
        });
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());


    }
}