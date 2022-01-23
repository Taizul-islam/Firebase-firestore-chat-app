package com.rakib.soberpoint.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.utils.OptionsUtils;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        FirebaseApp.initializeApp(LauncherActivity.this);

    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent=new Intent(LauncherActivity.this, HomeActivity.class);
                intent.putExtra("type","home");
                startActivity(intent);
                finish();
            },3000);
        }else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                boolean first= OptionsUtils.getBooleanPref(LauncherActivity.this,"FIRST",true);
                if (first){
                    startActivity(new Intent(LauncherActivity.this, IntroActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
                    finish();
                }

            },3000);
        }


    }
}