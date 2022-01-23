package com.rakib.soberpoint.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppController  extends Application implements Application.ActivityLifecycleCallbacks{
    int numStarted = 0;
    String user_id;
    FirebaseFirestore firebaseFirestore;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseFirestore=FirebaseFirestore.getInstance();
        registerActivityLifecycleCallbacks(this);
        try{
            user_id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (NullPointerException e){
            e.printStackTrace();
        }

    }
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (numStarted == 0) {
            setStatus("Online");

        }
        numStarted++;
    }

    public void setStatus(String status) {
        try {
            firebaseFirestore.collection("Users").document(user_id).update("status", status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }





    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        numStarted--;
        if (numStarted == 0) {
            setStatus("Offline");

        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
