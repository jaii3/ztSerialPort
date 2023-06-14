package com.example.testjni;

import android.content.Context;

import java.lang.reflect.Method;

public class StatubarManager {
    public static final int STATUS_BAR_DISABLE_HOME = 0x00200000;
    public static final int STATUS_BAR_DISABLE_BACK = 0x00400000;
    public static final int STATUS_BAR_DISABLE_RECENT = 0x01000000;
    public static final int STATUS_BAR_DISABLE_EXPAND = 0x00010000;//4.2以上的整形标识
    public static final int STATUS_BAR_DISABLE_NONE = 0x00000000;//取消StatusBar所有disable属性，即还原到最最原始状态

    /**
     * 禁止状态栏
     * @param context
     * @param disable_status
     */
    public static void setStatusBarDisable(Context context, int disable_status) {//调用statusBar的disable方法
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName
                    ("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, disable_status);
        } catch (Exception e) {
            unBanStatusBar(context);
            e.printStackTrace();
        }
    }

    /**
     * 恢复状态栏
     * @param context
     */
    public static void unBanStatusBar(Context context) {//利用反射解除状态栏禁止下拉
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName
                    ("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, STATUS_BAR_DISABLE_NONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
