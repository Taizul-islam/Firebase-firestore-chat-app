package com.rakib.soberpoint.items;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class PostId {
    @Exclude
    public String BlogPostId;
    public <T extends PostId> T withId(@NonNull final String id){
        this.BlogPostId=id;
        return (T)this;
    }
}
