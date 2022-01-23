package com.rakib.soberpoint.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rakib.soberpoint.R;
import com.rakib.soberpoint.activities.UserProfileActivity;
import com.rakib.soberpoint.items.ItemNearby;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NearbyAdapter extends RecyclerView.Adapter<NearbyAdapter.HmmRakib> {
    Context context;
    List<ItemNearby> itemNearbyList;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    double lat,lng;
    public NearbyAdapter(Context context, List<ItemNearby> itemNearbyList,double lat,double lng) {
        this.context = context;
        this.itemNearbyList = itemNearbyList;
        this.lat=lat;
        this.lng=lng;
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HmmRakib onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_friend_nearby,parent,false);
        return new HmmRakib(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HmmRakib holder, int position) {
        String friend_id=itemNearbyList.get(position).getId();
        firebaseFirestore.collection("Users").document(friend_id).get().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()){
                    String status=task.getResult().getString("status");

                        String image=task.getResult().getString("image");
                        if (image.equals("")){
                            holder.image.setImageResource(R.drawable.launcher_image);
                        }else {
                            Picasso.get().load(image).placeholder(R.drawable.launcher_image).error(R.drawable.launcher_image).into(holder.image);
                        }

                        String name=task.getResult().getString("name");
                        holder.name.setText(name);

                        if (status.equals("Online")){
                            holder.status.setImageDrawable(context.getDrawable(R.drawable.active));
                        }else {
                            holder.status.setImageDrawable(context.getDrawable(R.drawable.in_active));
                        }



                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }


        });
        double dis=distance(lat,lng,itemNearbyList.get(position).getLat(),itemNearbyList.get(position).getLng());
        dis=dis / 0.62137;
        holder.tv_km.setText(""+(int)dis+" km");
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(context, UserProfileActivity.class);
            intent.putExtra("id",friend_id);
            context.startActivity(intent);
        });
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    @Override
    public int getItemCount() {
        return itemNearbyList.size();
    }

    public class HmmRakib extends RecyclerView.ViewHolder {
        CircleImageView image;
        TextView name,tv_km;
        ImageView status;
        public HmmRakib(@NonNull @NotNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.circleImageView3);
            name=itemView.findViewById(R.id.textView6);
            status=itemView.findViewById(R.id.imageView3);
            tv_km=itemView.findViewById(R.id.tv_km);
        }
    }
}
