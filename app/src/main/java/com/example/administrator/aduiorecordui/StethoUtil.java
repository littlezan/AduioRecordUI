package com.example.administrator.aduiorecordui;

import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * ClassName: StethoUtil
 * Description: Stetho
 * @author 彭赞
 * @since 2017-09-29  14:20
 * @version 1.0
 */
public class StethoUtil {

    public static void init(Context context) {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(context);
        }
    }
}
