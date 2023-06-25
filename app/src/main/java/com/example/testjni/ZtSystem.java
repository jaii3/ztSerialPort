package com.example.testjni;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author HP
 */
public class ZtSystem {
    private static final String TAG = "ZTSystem";
    private static volatile ZtSystem instance = null;

    /**
     * 隐藏导航栏广播
     */
    public static final String ACTION_API_HIDE_NAVIGATION = "action.ACTION_API_HIDE_NAVIGATION";

    /**
     * 显示导航栏广播
     */
    public static final String ACTION_API_SHOW_NAVIGATION = "action.ACTION_API_SHOW_NAVIGATION";

    /**
     * 状态栏广播
     */
    public static final String ACTION_API_HIDE_DROP_DOWN_BOX = "action.ACTION_API_HIDE_DROP_DOWN_BOX";

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ZtSystem.context = context;
    }

    private static Context context;

    private final String[] strUSBName = new String[]{"usb1", "usb2", "usb3", "usb4", "usb5"};

    public static ZtSystem getInstance(Context context) {
        if (instance == null) {
            synchronized (ZtSystem.class) {
                if (instance == null) {
                    instance = new ZtSystem();
                    setContext(context);
                }
            }
        }
        return instance;
    }


    /**
     * 设置 导航栏和状态栏
     * 注：该方法永久生效，重启也生效
     *
     * @param hide true: 隐藏导航栏 false: 显示导航栏
     */
    public void setNavigationBar(boolean hide) {
        if (hide) {
            //发送隐藏导航栏广播
            Intent intent = new Intent(ACTION_API_HIDE_NAVIGATION);
            context.sendBroadcast(intent);

            //状态栏禁止下拉框
            Intent intent1 = new Intent(ACTION_API_HIDE_DROP_DOWN_BOX);
            intent1.putExtra("state", "true");
            context.sendBroadcast(intent1);
        } else {
            //发送显示导航栏广播
            Intent intent = new Intent(ACTION_API_SHOW_NAVIGATION);
            context.sendBroadcast(intent);

            //状态栏允许下拉框
            Intent intent1 = new Intent(ACTION_API_HIDE_DROP_DOWN_BOX);
            intent1.putExtra("state", "false");
            context.sendBroadcast(intent1);
        }
    }

    /**
     * 导航栏，状态栏隐藏
     * 注：控制系统导航栏的临时 隐藏/显示， 使用此方法隐藏导航栏后，可以通过下拉或者上滑唤出状态栏
     *
     * @param activity activity
     * @param hide     true: 隐藏 false: 显示
     */
    public void setNavigationBarStatusBar(Activity activity, boolean hide) {
        if (hide && Build.VERSION.SDK_INT >= 19) {
            View decorView = activity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
        DataOutputStream os = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = format.parse(time);
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            String formatDate;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                format = new SimpleDateFormat("MMddHHmmyyyy.ss", Locale.getDefault());
                formatDate = format.format(date);
                os.writeBytes("date " + formatDate + " set \n");
                os.writeBytes("busybox hwclock -w\n");
            } else {
                format = new SimpleDateFormat("yyyyMMdd.HHmmss", Locale.getDefault());
                formatDate = format.format(date);
                os.writeBytes("/system/bin/date -s " + formatDate + "\n");
                os.writeBytes("clock -w\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    /**
     * 获取内部存储路径 （表示应用的内部存储目录）
     *
     * @return 例:/data/user/0/your_package/files
     */
    public String getInternalStorageDirectoryPath() {
        File internalStorage = context.getFilesDir();
        return internalStorage.getAbsolutePath();
    }

    /**
     * 获取外部存储路径
     *
     * @return 例:/storage/emulated/0
     */
    public String getExtStorageDirectoryPath() {

        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取外置SD卡存储路径
     *
     * @return 例:/storage/36BC-A2FB/
     */
    public String getExtSdCardPath() {
        String extSdcardPath;
        List<FileStoreDevices> sdList1 = getStoragePath(context, false);
        if (sdList1.size() == 0) {
            extSdcardPath = null;
        } else {
            extSdcardPath = sdList1.get(0).path + File.separator;
        }
        return extSdcardPath;
    }

    /**
     * 获取指定USB存储路径
     *
     * @param usbNum 1:usb1  2:usb2  3:usb3  4:usb4  5:usb5 ....
     * @return 例：/storage/4434-5102/
     */
    public String getUsbPath(int usbNum) {
        String usbPath;
        List<FileStoreDevices> devicesList = getStoragePath(context, true);
        if (devicesList.size() == 0) {
            usbPath = null;
        } else {
            for (FileStoreDevices fsd : devicesList) {
                if (fsd.name.equals("usb" + usbNum)) {
                    usbPath = fsd.path + File.separator;
                    return usbPath;
                }
            }
            usbPath = null;
        }
        return usbPath;
    }

    /**
     * 获取第一个USB设备的存储路径
     *
     * @return 例：/storage/4434-5102/
     */
    public String getUsbPathFirst() {
        String usbPath;
        List<FileStoreDevices> devicesList = getStoragePath(context, true);
        if (devicesList.size() == 0) {
            usbPath = null;
        } else {
            usbPath = devicesList.get(0).path + File.separator;
        }
        return usbPath;
    }


    /**
     * 获取全部USB存储路径
     *
     * @return List<FileStoreDevices>
     */
    public List<FileStoreDevices> getAllUsbPath() {
        return getStoragePath(context, true);
    }

    /**
     * 获取外部存储路径（SD、USB)
     *
     * @param context context
     * @param isUsb   true:USB false: SD
     * @return List<FileStoreDevices>
     */
    private List<FileStoreDevices> getStoragePath(Context context, boolean isUsb) {
        String path = "";
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> volumeInfoClazz;
        Class<?> diskInfoClazz;
        List<FileStoreDevices> fileStoreDevices = new ArrayList();
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            diskInfoClazz = Class.forName("android.os.storage.DiskInfo");
            Method StorageManager_getVolumes = Class.forName("android.os.storage.StorageManager").getMethod("getVolumes");
            Method VolumeInfo_GetDisk = volumeInfoClazz.getMethod("getDisk");
            Method VolumeInfo_GetPath = volumeInfoClazz.getMethod("getPath");
            Method DiskInfo_IsUsb = diskInfoClazz.getMethod("isUsb");
            Method DiskInfo_IsSd = diskInfoClazz.getMethod("isSd");
            Method VolumeInfo_GetDescription = volumeInfoClazz.getMethod("getDescription");
            Method DiskInfo_GetDescription = diskInfoClazz.getMethod("getDescription");
            Method DiskInfo_GetSysPath = diskInfoClazz.getMethod("getSysPath");

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
                String volumeInfoGetDescription = (String) VolumeInfo_GetDescription.invoke(volumeInfo);
                String diskInfoGetDescription = (String) DiskInfo_GetDescription.invoke(diskInfo);
                String DiskInfo_getSysPath = (String) DiskInfo_GetSysPath.invoke(diskInfo);
                File file = (File) VolumeInfo_GetPath.invoke(volumeInfo);

                if (isUsb == usb && usb) {
                    assert file != null;
                    path = file.getAbsolutePath();
                    for (String sUsb : strUSBName) {
                        if (DiskInfo_getSysPath.contains(sUsb)) {
                            fileStoreDevices.add(new FileStoreDevices(sUsb, path));
                        }
                    }
                } else if (!isUsb == sd) {
                    assert file != null;
                    path = file.getAbsolutePath();
                    fileStoreDevices.add(new FileStoreDevices("sd", path));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return fileStoreDevices;
    }


    /**
     * 熄屏/亮屏 操作
     *
     * @return
     */
    public int setScreenOffOn() {
        return execRootCmdSilent("input keyevent 26");
    }

    /**
     * 执行adb 命令
     *
     * @param cmd "input keyevent 26"
     * @return
     */
    private int execRootCmdSilent(String cmd) {
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
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return result;
    }

}
