package com.rakib.soberpoint.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;


import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEditCounterActivity extends AppCompatActivity {
    ImageButton back;
    EditText et_date,et_money,et_time;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    boolean isUpdate=false;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_counter);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        back=findViewById(R.id.back);
        back.setOnClickListener(v -> {
            Intent intent=new Intent(AddEditCounterActivity.this,HomeActivity.class);
            intent.putExtra("type","counter");
            startActivity(intent);
            finish();
        });
        et_date=findViewById(R.id.et_date);
        et_date.setOnClickListener(v -> {
            Calendar mcurrentDate=Calendar.getInstance();
           int year=mcurrentDate.get(Calendar.YEAR);
           int month=mcurrentDate.get(Calendar.MONTH);
            int day=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            final DatePickerDialog mDatePicker =new DatePickerDialog(AddEditCounterActivity.this, (datepicker, selectedyear, selectedmonth, selectedday) -> {
                et_date.setText(new StringBuilder().append(selectedyear).append("/").append(selectedmonth+1).append("/").append(selectedday));

            },year, month, day);
            mDatePicker.setTitle("Please select date");
            // TODO Hide Future Date Here
            mDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());

            // TODO Hide Past Date Here
            //  mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            mDatePicker.show();
        });
        et_money=findViewById(R.id.et_money);
        et_time=findViewById(R.id.et_time);
        getData();

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            String date=et_date.getText().toString();
            if (TextUtils.isEmpty(date)){
                Toast.makeText(AddEditCounterActivity.this,"Please enter date",Toast.LENGTH_LONG).show();
                return;
            }
            String money=et_money.getText().toString();
            if (TextUtils.isEmpty(money)){
                Toast.makeText(AddEditCounterActivity.this,"Please enter some money",Toast.LENGTH_LONG).show();
                return;
            }
            String time=et_time.getText().toString();
            if (TextUtils.isEmpty(time)){
                Toast.makeText(AddEditCounterActivity.this,"Please enter time",Toast.LENGTH_LONG).show();
                return;
            }
            Map<String,Object> map=new HashMap<>();
            map.put("date",date);
            map.put("money",money);
            map.put("time",time);
            if (isUpdate){
                firebaseFirestore.collection("Counter").document(firebaseAuth.getCurrentUser().getUid()).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent=new Intent(AddEditCounterActivity.this,HomeActivity.class);
                            intent.putExtra("type","counter");
                            startActivity(intent);
                           finish();
                        }
                    }
                });
            }else {
                firebaseFirestore.collection("Counter").document(firebaseAuth.getCurrentUser().getUid()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Intent intent=new Intent(AddEditCounterActivity.this,HomeActivity.class);
                            intent.putExtra("type","counter");
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }

    private void getData() {
        try {
            firebaseFirestore.collection("Counter").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            isUpdate = true;
                            et_date.setText(task.getResult().getString("date"));
                            et_money.setText(task.getResult().getString("money"));
                            et_time.setText(task.getResult().getString("time"));

                        } else {
                            String date = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                            et_date.setText(date);
                            isUpdate = false;
                        }
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

}