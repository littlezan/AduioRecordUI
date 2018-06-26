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

    public static void createDir(String... dirPath) {
        File dir;
        for (String aDirPath : dirPath) {
            dir = new File(aDirPath);
            if (!dir.exists() && !dir.isDirectory()) {
                dir.mkdirs();
            }
        }
    }

    public static String getRootFilePath(Context context) {
        String path;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName() + File.separator;
        } else {
            path = context.getCacheDir().getPath() + File.separator;
        }
        createDir(path);
        return path;
    }

}
