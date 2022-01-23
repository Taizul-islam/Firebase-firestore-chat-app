package com.rakib.soberpoint.adapter;



import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.rakib.soberpoint.R;
import com.rakib.soberpoint.items.ScreenItem;

import java.util.List;


public class IntroViewPagerAdapter extends PagerAdapter {

    Context mContext ;
    List<ScreenItem> mListScreen;

    public IntroViewPagerAdapter(Context mContext, List<ScreenItem> mListScreen) {
        this.mContext = mContext;
        this.mListScreen = mListScreen;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View layoutScreen = inflater.inflate(R.layout.slider_layout,null);
        ImageView imgSlide = layoutScreen.findViewById(R.id.slider_image);
        imgSlide.setImageResource(mListScreen.get(position).getImage());
        TextView textView=layoutScreen.findViewById(R.id.slider_text);
        textView.setText(mListScreen.get(position).getTitle());
        container.addView(layoutScreen);
        return layoutScreen;

    }
    @Override
    public int getCount() {
        return mListScreen.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
