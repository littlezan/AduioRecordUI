package com.example.administrator.aduiorecordui.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: FastClickLimitUtil
 * Description: 防止重复点击工具类
 *
 * @author : 彭赞
 * @version : 1.0
 * @since : 2017-03-29  11:36
 */

public class FastClickLimitUtil {
    private static long lastClickTime;
    private final static int SPACE_TIME = 500;
    private static Map<Integer, Long> viewMap = new HashMap<>();


    public synchronized static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        isClick2 = currentTime - lastClickTime <= SPACE_TIME;
        lastClickTime = currentTime;
        return isClick2;
    }

    public synchronized static boolean isFastClick(int viewId, int internal) {
        boolean isClick2 = false;
        if (viewMap.containsKey(viewId)) {
            long lastClickTime = viewMap.get(viewId);
            long currentTime = System.currentTimeMillis();
            isClick2 = currentTime - lastClickTime <= internal;
            if (!isClick2) {
                viewMap.remove(viewId);
            }
        } else {
            viewMap.put(viewId, System.currentTimeMillis());
        }
        return isClick2;
    }

}
