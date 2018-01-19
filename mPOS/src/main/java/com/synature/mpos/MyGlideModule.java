package com.synature.mpos;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by jitth on 1/19/2018.
 */

public class MyGlideModule implements GlideModule {
    @Override public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
    }

    @Override public void registerComponents(Context context, Glide glide) {
    }
}