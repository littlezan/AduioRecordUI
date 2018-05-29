package com.example.administrator.aduiorecordui;

import android.app.Application;
import android.content.Context;

/**
 * ClassName: App
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-29  11:47
 */
public class App extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
