package com.example.administrator.aduiorecordui.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * ClassName: FileUtls
 * Description:
 *
 * @author 彭赞
 * @version 1.0
 * @since 2018-05-29  11:45
 */
public class FileUtils {

    public static String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                cachePath = externalCacheDir.getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
