package com.example.testjni;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.storage.StorageManager;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.STORAGE_SERVICE;

public class ZTSystem {
    private static String TAG = "ZTSystem";
    private static ZTSystem instance = null;

    public static final String ACTION_API_HIDE_NAVIGATION = "action.ACTION_API_HIDE_NAVIGATION"; //隐藏导航栏广播
    public static final String ACTION_API_SHOW_NAVIGATION = "action.ACTION_API_SHOW_NAVIGATION"; //显示导航栏广播
    public static final String ACTION_API_HIDE_DROP_DOWN_BOX = "action.ACTION_API_HIDE_DROP_DOWN_BOX"; // 状态栏广播

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ZTSystem.context = context;
    }

    private static Context context;

    private String[] strUSBName = new String[]{"usb1", "usb2", "usb3", "usb4", "usb5"};

    public static ZTSystem getInstance(Context context) {
        if (instance == null) {
            synchronized (ZTSystem.class) {
                if (instance == null) {
                    instance = new ZTSystem();
                    setContext(context);
                }
            }
        }
        return instance;
    }

    /**
     * 设置 导航栏
     *
     * @param hide
     */
    public void setNavigationBar(boolean hide) {
        if (!hide) {
            Intent intent = new Intent(ACTION_API_HIDE_NAVIGATION);
            context.sendBroadcast(intent); //当点击“隐藏导航栏”按钮时，发送隐藏导航栏广播

            Intent intent1 = new Intent(ACTION_API_HIDE_DROP_DOWN_BOX);
            intent1.putExtra("state", "true"); //禁止下拉框
            context.sendBroadcast(intent1);
        } else {
            Intent intent = new Intent(ACTION_API_SHOW_NAVIGATION);
            context.sendBroadcast(intent); //当点击“显示导航栏”按钮时，发送显示导航栏广播

            Intent intent1 = new Intent(ACTION_API_HIDE_DROP_DOWN_BOX);
            intent1.putExtra("state", "false"); //允许下拉框
            context.sendBroadcast(intent);
        }
    }


    /**
     * 设置时间
     *
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     */
    public void setSystemTime(int year, int month, int day, int hour, int minute, int second) {
        String time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
        setSystemTime(time);
    }

    /**
     * 设置时间
     *
     * @param time "yyyy-MM-dd HH:mm:ss"
     */
    public void setSystemTime(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(time);
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String formatDate;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                format = new SimpleDateFormat("MMddHHmmyyyy.ss");
                formatDate = format.format(date);
                os.writeBytes("date " + formatDate + " set \n");
                os.writeBytes("busybox hwclock -w\n");
            } else {
                format = new SimpleDateFormat("yyyyMMdd.HHmmss");
                formatDate = format.format(date);
                os.writeBytes("/system/bin/date -s " + formatDate + "\n");
                os.writeBytes("clock -w\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getSdCardPath() {
        String sdCardPath;
        List<String> sdList2 = getStoragePath(context, false, false);

        if (sdList2.size() == 0) {
            sdCardPath = null;
        } else {
            sdCardPath = sdList2.get(0) + File.separator;
        }
        return sdCardPath;
    }

    public String getExtSdCardPath() {
        String extSdcardPath;
        //List<String> sdList1 = getStoragePath(context, true, false);
        List<FileStoreDevices> sdList1 = getStoragePath2(context, false);
        if (sdList1.size() == 0) {
            extSdcardPath = null;
        } else {
            extSdcardPath = sdList1.get(0).path + File.separator;
        }

        return extSdcardPath;
    }

    /**
     * 获取第一个USB设备
     *
     * @return
     */
    public String getUSBPathFirst() {
        String usbPath;
        List<FileStoreDevices> sdList3 = getStoragePath2(context, true);
        if (sdList3.size() == 0) {
            usbPath = null;
        } else {
            usbPath = sdList3.get(0).path + File.separator;
        }
        return usbPath;
    }

    /**
     * @param usb_num 1:usb1  2:usb2  3:usb3  4:usb4  5:usb5 ....
     * @return
     */
    public String getUSBPath(int usb_num) {
        String usbPath;
        List<FileStoreDevices> sdList3 = getStoragePath2(context, true);
        if (sdList3.size() == 0) {
            usbPath = null;
        } else {
            for (FileStoreDevices fsd : sdList3) {
                if (fsd.name.equals("usb" + usb_num)) {
                    usbPath = fsd.path + File.separator;
                    return usbPath;
                }
            }
            usbPath = null;
        }
        return usbPath;
    }

    public List<FileStoreDevices> getAllUSBPath() {
        return getStoragePath2(context, true);
    }

    /**
     * 获取 SD卡 路径
     *
     * @param mContext    context
     * @param is_removale true: 外置SD卡路径
     * @param is_usb      true: 外置USB路径
     * @return
     */
    List<String> getStoragePath(Context mContext, boolean is_removale, boolean is_usb) {
        List<String> rs = new ArrayList();
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(STORAGE_SERVICE);
        try {
            Class storageVolumeClazz = null;
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Method isEmulated = storageVolumeClazz.getMethod("isEmulated");
            Object result = getVolumeList.invoke(mStorageManager);
            int length = Array.getLength(result);


            for (int i = 0; i < length; ++i) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    if (is_usb && !path.contains("usb")) {
                        continue;
                    }
                    /*File sdFile = new File(path);
                    if (sdFile.canWrite()) */
                    {
                        rs.add(path);
                    }
                }
            }
        } catch (ClassNotFoundException var16) {
            var16.printStackTrace();
        } catch (InvocationTargetException var17) {
            var17.printStackTrace();
        } catch (NoSuchMethodException var18) {
            var18.printStackTrace();
        } catch (IllegalAccessException var19) {
            var19.printStackTrace();
        }

        return rs;
    }

    private List<FileStoreDevices> getStoragePath2(Context context, boolean isUsb) {
        String path = "";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClaszz;
        List<FileStoreDevices> fileStoreDevices = new ArrayList();
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClaszz = Class.forName("android.os.storage.DiskInfo");
            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClaszz.getMethod("isUsb");
            Method DiskInfo_IsSd = diskInfoClaszz.getMethod("isSd");
            Method VolumeInfo_GetDescription = volumeInfoClazz.getMethod("getDescription");
            Method DiskInfo_GetDescription = diskInfoClaszz.getMethod("getDescription");
            Method DiskInfo_GetSysPath = diskInfoClaszz.getMethod("getSysPath");

            List<Object> List_VolumeInfo = (List<Object>) StorageManager_getVolumes.invoke(mStorageManager);
            assert List_VolumeInfo != null;
            for (int i = 0; i < List_VolumeInfo.size(); i++) {
                Object volumeInfo = List_VolumeInfo.get(i);
                Object diskInfo = VolumeInfo_GetDisk.invoke(volumeInfo);
                if (diskInfo == null) {
                    continue;
                }
                boolean sd = (boolean) DiskInfo_IsSd.invoke(diskInfo);
                boolean usb = (boolean) DiskInfo_IsUsb.invoke(diskInfo);
                String VolumeInfo_getDescription = (String) VolumeInfo_GetDescription.invoke(volumeInfo);
                String DiskInfo_getDescription = (String) DiskInfo_GetDescription.invoke(diskInfo);
                String DiskInfo_getSysPath = (String) DiskInfo_GetSysPath.invoke(diskInfo);
                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);

                if (isUsb == usb && usb) {
                    assert file != null;
                    path = file.getAbsolutePath();
                    for (String strusb : strUSBName) {
                        if (DiskInfo_getSysPath.contains(strusb)) {
                            fileStoreDevices.add(new FileStoreDevices(strusb, path));
                        }
                    }
                } else if (!isUsb == sd) {
                    assert file != null;
                    path = file.getAbsolutePath();
                    fileStoreDevices.add(new FileStoreDevices("sd", path));
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileStoreDevices;
    }


    /**
     * 导航栏，状态栏隐藏
     *
     * @param activity
     */
    public void NavigationBarStatusBar(Activity activity, boolean hasFocus) {
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * 执行adb 命令
     *
     * @param cmd "input keyevent 26"
     * @return
     */
    public static int execRootCmdSilent(String cmd) {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

}
