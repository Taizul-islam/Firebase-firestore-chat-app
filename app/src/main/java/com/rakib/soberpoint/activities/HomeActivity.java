package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.easywaylocation.EasyWayLocation;
import com.example.easywaylocation.Listener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.fragment.CounterFragment;
import com.rakib.soberpoint.fragment.FriendListFragment;
import com.rakib.soberpoint.fragment.HomeFragment;
import com.rakib.soberpoint.fragment.MyProfileFragment;
import com.rakib.soberpoint.fragment.NotificationFragment;
import com.rakib.soberpoint.utils.BackPressedFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.easywaylocation.EasyWayLocation.LOCATION_SETTING_REQUEST_CODE;

public class HomeActivity extends AppCompatActivity implements Listener {
    HomeFragment homeFragment;
    MyProfileFragment myProfileFragment;
    NotificationFragment notificationFragment;
    CounterFragment counterFragment;
    FriendListFragment chatFragment;
    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    String token,useridfortoken;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String type;
    EasyWayLocation easyWayLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        easyWayLocation = new EasyWayLocation(HomeActivity.this, false, this);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        type=getIntent().getStringExtra("type");
        firebaseAuth=FirebaseAuth.getInstance();
        try {
            useridfortoken = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        firebaseFirestore=FirebaseFirestore.getInstance();
        homeFragment=new HomeFragment();
        myProfileFragment=new MyProfileFragment();
        notificationFragment=new NotificationFragment();
        chatFragment=new FriendListFragment();
        counterFragment=new CounterFragment();
        bottomNavigationView=findViewById(R.id.bottomNavigationView);
        frameLayout=findViewById(R.id.main_frame);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId()==R.id.home){
                replaceFragment(homeFragment);

                return true;
            }
            if (item.getItemId()==R.id.profile){
                replaceFragment(myProfileFragment);
                return true;
            }
            if (item.getItemId()==R.id.counter){
                replaceFragment(counterFragment);
                return true;
            }
            if (item.getItemId()==R.id.notification){
                replaceFragment(notificationFragment);
                return true;
            }
            if (item.getItemId()==R.id.chat){
                replaceFragment(chatFragment);
                return true;
            }
            return false;
        });
        if (type.equals("request")){
            replaceFragment(notificationFragment);
            bottomNavigationView.setSelectedItemId(R.id.notification);
        }
        else if (type.equals("counter")){
            replaceFragment(counterFragment);
            bottomNavigationView.setSelectedItemId(R.id.counter);
        }
        else {
            replaceFragment(homeFragment);
            bottomNavigationView.setSelectedItemId(R.id.home);
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()){
                token = task1.getResult();
                GenerateToken(token);

            }

        }).addOnFailureListener(e -> Log.d("failed", "Failed"));

        getPermission();



    }
    private void GenerateToken(String token) {



        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", token);
        try {
            firebaseFirestore.collection("Tokens").document(useridfortoken).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTING_REQUEST_CODE) {
            easyWayLocation.onActivityResult(resultCode);
            easyWayLocation.endUpdates();
        }
    }
    private void getPermission() {
        Dexter.withContext(HomeActivity.this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {

                if (report.areAllPermissionsGranted()) {



                } else {

                    Toast.makeText(HomeActivity.this, "Sorry. Permission not granted", Toast.LENGTH_SHORT).show();
                }
                if (report.isAnyPermissionPermanentlyDenied()) {
                    showSettingsDialog();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).withErrorListener(error -> Toast.makeText(HomeActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
    }
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
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
        Uri uri = Uri.fromParts("package", HomeActivity.this.getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.homeContainer,fragment);
        transaction.addToBackStack("my_fragment");
        transaction.commit();
    }
    @Override
    public void onResume() {
        super.onResume();

        easyWayLocation.startLocation();
    }



    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() >0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        easyWayLocation.endUpdates();
    }

    @Override
    public void locationOn() {
        Toast.makeText(HomeActivity.this, "Location ON", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void currentLocation(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            setLocation(lat, lng);
            easyWayLocation.endUpdates();
        }
    }

    private void setLocation(double lat, double lng) {
        Map<String,Object> map1=new HashMap<>();
        map1.put("lat",lat);
        map1.put("lng",lng);
        map1.put("id",useridfortoken);
        firebaseFirestore.collection("Location").document(useridfortoken).update(map1).addOnCompleteListener(task2 -> {

        });
    }

    @Override
    public void locationCancelled() {
        Toast.makeText(HomeActivity.this, "Location Cancelled", Toast.LENGTH_SHORT).show();
    }
}