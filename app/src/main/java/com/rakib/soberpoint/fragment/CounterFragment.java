package com.rakib.soberpoint.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.AddEditCounterActivity;
import com.rakib.soberpoint.activities.HomeActivity;
import com.rakib.soberpoint.utils.BackPressedFragment;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CounterFragment extends Fragment {
    Context context;
    ImageButton back;
    ImageView bring;
    TextView tv_date, tv_since, tv_time, tv_money;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    public CounterFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_counter, container, false);
        context = container.getContext();
        back = itemView.findViewById(R.id.back);
        back.setOnClickListener(v -> startActivity(new Intent(context, AddEditCounterActivity.class)));
        bring=itemView.findViewById(R.id.image);
        bring.bringToFront();

        tv_date = itemView.findViewById(R.id.tv_date);
        tv_since = itemView.findViewById(R.id.tv_since);
        tv_time = itemView.findViewById(R.id.tv_time);
        tv_money = itemView.findViewById(R.id.tv_money);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        try {
            firebaseFirestore.collection("Counter").document(firebaseAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String dbDate = task.getResult().getString("date");
                            String currentDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date());
                            Date date1 = null;
                            Date date2 = null;

                            SimpleDateFormat dates = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

                            try {
                                date1 = dates.parse(currentDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            try {
                                assert dbDate != null;
                                date2 = dates.parse(dbDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            assert date1 != null;
                            assert date2 != null;
                            long difference = Math.abs(date1.getTime() - date2.getTime());
                            long differenceDates = difference / (24 * 60 * 60 * 1000);
                            String dayDifference = Long.toString(differenceDates);
                            tv_date.setText(dayDifference + " Days");
                            SimpleDateFormat dateFormatprev = new SimpleDateFormat("yyyy/MM/dd");
                            Date d = null;
                            try {
                                d = dateFormatprev.parse(dbDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
                            String changedDate = dateFormat.format(d);
                            tv_since.setText(dayDifference + " Day since " + changedDate);
                            double hour = Double.parseDouble(task.getResult().getString("time"));
                            int money = Integer.parseInt(task.getResult().getString("money"));
                            if (differenceDates < 7) {
                                int money_save = (int) ((money / 7) * differenceDates);
                                Log.d("hour",""+hour);
                                Log.d("diff",""+differenceDates);
                               // int hour_save =  (int)(differenceDates*(hour / 7));
                                double i= hour/7;
                                Log.d("hour",""+i);
                                int j= (int) (i*differenceDates);
                                Log.d("hour",""+j);
                                tv_money.setText(money_save + " saved");
                                tv_time.setText(j + " hour saved");
                            } else {
                                int week_count = (int) (differenceDates / 7);
                                int hour_save = (int) (week_count * hour);
                                int money_save = week_count * money;
                                tv_money.setText(money_save + " saved");
                                tv_time.setText(hour_save + " hour saved");
                            }

                        }else {
                            tv_since.setText("0 Day since start");
                            tv_money.setText("0 saved");
                            tv_time.setText("0 hour saved");
                        }
                    }
                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        return itemView;
    }


}