package com.rakib.soberpoint.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.adapter.IntroViewPagerAdapter;
import com.rakib.soberpoint.items.ScreenItem;
import com.rakib.soberpoint.sliderIndicator.SliderIndicator;
import com.rakib.soberpoint.utils.OptionsUtils;

import java.util.ArrayList;
import java.util.List;


public class IntroActivity extends AppCompatActivity {
    Button btn_start;
    Button btn_skip;
    List<ScreenItem> mList = new ArrayList<>();
    SliderIndicator sliderIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getPermission();
        ViewPager viewPager = findViewById(R.id.slider);
        btn_start = findViewById(R.id.btn_get_started);
        btn_skip=findViewById(R.id.btn_skip);
        sliderIndicator=findViewById(R.id.main_slide_indicator);
        mList.add(new ScreenItem(R.drawable.life, "Make better your life"));
        mList.add(new ScreenItem(R.drawable.share, "Share your life with others"));
        mList.add(new ScreenItem(R.drawable.counter, "Weekly counter help you to improve"));
        IntroViewPagerAdapter introViewPagerAdapter = new IntroViewPagerAdapter(this, mList);
        viewPager.setAdapter(introViewPagerAdapter);
        setCurrentIndicator(0);
        sliderIndicator.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btn_start.setOnClickListener(v -> {
            if (viewPager.getCurrentItem()+1<mList.size()){
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }else {
                Intent intent=new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                OptionsUtils.savePrefs(IntroActivity.this,"FIRST",false);
            }

        });
        btn_skip.setOnClickListener(v -> {
            Intent intent=new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            OptionsUtils.savePrefs(IntroActivity.this,"FIRST",false);
        });
    }

    private void setCurrentIndicator(int index){

        if (index==mList.size()-1){
            btn_start.setText("Lets start");
            btn_skip.setVisibility(View.INVISIBLE);
            btn_skip.animate().alpha(0f).setDuration(1000);
        }else {
            btn_start.setText("Next");
            btn_skip.setVisibility(View.VISIBLE);
            btn_skip.animate().alpha(1f).setDuration(1000);
        }
    }

    private void getPermission() {
        Dexter.withContext(this).withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                    Log.d("msg","msg");
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).withErrorListener(error -> Toast.makeText(IntroActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IntroActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }



}