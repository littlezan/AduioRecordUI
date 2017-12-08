package com.example.administrator.aduiorecordui;

import android.app.Application;

/**
 * ClassName: App
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2017-12-07  16:52
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StethoUtil.init(getApplicationContext());
    }
}
