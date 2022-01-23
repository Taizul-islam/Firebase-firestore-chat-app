package com.rakib.soberpoint.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;


import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class OptionsUtils {

    private static SharedPreferences getPreferences(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static void savePrefs(Context context, String keyword, boolean value)
    {
        if (context != null) {
            SharedPreferences.Editor editor = getPreferences(context.getApplicationContext()).edit();
            editor.putBoolean(keyword, value);
            editor.apply();
        }
    }

    public static void savePrefs(Context context, String keyword, String value)
    {
        if (context != null) {
            SharedPreferences.Editor editor = getPreferences(context.getApplicationContext()).edit();
            editor.putString(keyword, value);
            editor.apply();
        }
    }
    public static void clear(Context context){
        if (context != null) {
            SharedPreferences.Editor editor = getPreferences(context.getApplicationContext()).edit();
            editor.clear().apply();
        }
    }



    public static String getStringPrefs(Context context, String keyword, String defvalue)
    {
        return getPreferences(context.getApplicationContext()).getString(keyword, defvalue);
    }

    public static boolean getBooleanPref(Context context, String keyword, boolean defvalue)
    {
        return getPreferences(context.getApplicationContext()).getBoolean(keyword, defvalue);
    }



}
