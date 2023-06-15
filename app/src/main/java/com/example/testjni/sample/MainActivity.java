package com.example.testjni.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testjni.Communication;
import com.example.testjni.R;
import com.example.testjni.StatubarManager;
import com.example.testjni.ZTSerialPortTest;
import com.example.testjni.ZTSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.testjni.StatubarManager.setStatusBarDisable;
import static com.example.testjni.ZTSerialPortTest.bytesToHexString;


public class MainActivity extends AppCompatActivity {

    char[] text22 = new char[1];
    char[] text = new char[1];
    public static Communication S7;
    byte[] buffer = new byte[1024];
    private ReadThread mReadThread;
    public static TextView textView1;
    static ZTSystem zTSystem;

    static int num = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zTSystem = ZTSystem.getInstance(MainActivity.this);

        //禁止状态栏
        setStatusBarDisable(MainActivity.this, StatubarManager.STATUS_BAR_DISABLE_EXPAND);
        //恢复状态栏
        //unBanStatusBar(MainActivity.this);

        textView1 = findViewById(R.id.textView1);
        textView1.setText("123");

        String path = "/dev/ttyS3";
        S7 = new ZTSerialPortTest(path, 115200, 8, 1, 110);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                saveRunInfo2File("test-" + (++num));
                S7.sendData(("AABB-" + num + "\r\n").getBytes());
                if (num > 170000) {
                    this.cancel();
                }
            }
        }, 0, 60 * 1000);//经过多长时间关闭该任务

        //String path = "/dev/ttyS6";
        //S7 = new ZTSerialPortTest(path, 115200, 8, 1, 110);
        //Create a receiving thread
        /*mReadThread = new ReadThread();
        mReadThread.start();*/
        text[0] = 48;

        EditText Emission = (EditText) findViewById(R.id.EditTextEmission);

        Emission.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                } else {
                    // 此处为失去焦点时的处理内容
                    int i;
                    CharSequence t = ((TextView) v).getText();
                    text = new char[t.length()];
                    for (i = 0; i < t.length(); i++) {
                        text[i] = t.charAt(i);
                    }
                    S7.sendData(new String(text).getBytes());
                    S7.sendData('\n');
                }
            }
        });
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                S7.sendData(new String(text).getBytes());
                S7.sendData('\n');
            }
        });


        /*for (int i = 0; i < 100; i++) {
            S7.sendData(new String("123456789").getBytes());
            S7.sendData('\n');
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        Button buttonhide = findViewById(R.id.buttonhide);
        buttonhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zTSystem.setNavigationBar(false);
                //zTSystem.NavigationBarStatusBar(MainActivity.this, false);
            }
        });
        Button buttonshow = findViewById(R.id.buttonshow);
        buttonshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zTSystem.setNavigationBar(true);
                //zTSystem.NavigationBarStatusBar(MainActivity.this, true);
            }
        });
        Button buttontime = findViewById(R.id.buttontime);
        buttontime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    zTSystem.setSystemTime("2024-07-07 08:08:08");
                }catch (Exception e){
                    e.printStackTrace();
                }


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String string = getTimeHaveSec();
                        Message fMag = new Message();
                        fMag.what = 0;
                        fMag.obj = string;
                        rxHandler.sendMessage(fMag);
                    }
                }).start();

            }
        });

        Button buttontime2 = findViewById(R.id.buttontime2);
        buttontime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zTSystem.setSystemTime(2022, 04, 23, 11, 11, 11);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        String string = getTimeHaveSec();
                        Message fMag = new Message();
                        fMag.what = 0;
                        fMag.obj = string;
                        rxHandler.sendMessage(fMag);
                    }
                }).start();
            }
        });

        Button buttonpath1 = findViewById(R.id.buttonpath1);
        buttonpath1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPath = zTSystem.getExtStorageDirectoryPath();
                Message fMag = new Message();
                fMag.what = 0;
                fMag.obj = strPath;
                rxHandler.sendMessage(fMag);
            }
        });

        Button buttonpath2 = findViewById(R.id.buttonpath2);
        buttonpath2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPath = zTSystem.getExtSdCardPath();
                Message fMag = new Message();
                fMag.what = 0;
                fMag.obj = strPath;
                rxHandler.sendMessage(fMag);
            }
        });

        Button buttonpath3 = findViewById(R.id.buttonpath3);
        buttonpath3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPath = zTSystem.getUSBPathFirst();
                Message fMag = new Message();
                fMag.what = 0;
                fMag.obj = strPath;
                rxHandler.sendMessage(fMag);
            }
        });

        // 6.0以上版本不能读取外部存储权限的问题
        //isGrantExternalRW(this);

        saveRunInfo2File("test");


        /*String strPath = "usb1: " + zTSystem.getUSBPath(1) + "             usb5: " + zTSystem.getUSBPath(5);
        Message fMag = new Message();
        fMag.what = 0;
        fMag.obj = strPath;
        rxHandler.sendMessage(fMag);*/
    }

    /**
     * 解决安卓6.0以上版本不能读取外部存储权限的问题
     *
     * @param activity
     * @return
     */
    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

            return false;
        }

        return true;
    }

    /*
     * 保存运行信息到文件中
     */
    public static String saveRunInfo2File(String runStr) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String time = formatter.format(new Date());
            String fileName = "run-" + time + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String path = "/sdcard/waterrun/";
                String path = zTSystem.getUSBPathFirst();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                runStr = sDateFormat.format(new Date()) + "\t" + runStr + "\r\n";

                saveFile(runStr, fileName, path, true);

                getFileContent(new File(path + fileName));
            }
            return fileName;

        } catch (Exception e) {
            android.util.Log.e("Global", "an error occured while writing file2...", e);
        }
        return null;
    }

    //读取指定目录下的所有TXT文件的文件内容
    public static String getFileContent(File file) {
        String content = "";
        if (file.isDirectory()) {    //检查此路径名的文件是否是一个目录(文件夹)
        } else {
            /*if (file.getName().endsWith(".txt")||file.getName().endsWith(".doc"))*/
            {//文件格式为txt文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader
                                = new InputStreamReader(instream, "GBK");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();        //关闭输入流
                    }
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            }
        }
        return content;
    }


    public static void saveFile(String runStr, String fileName, String path, boolean b) throws IOException {
        FileOutputStream fos = new FileOutputStream(path + fileName, b);
        fos.write(runStr.getBytes());
        fos.flush();
        fos.close();
    }

    protected int onRead(byte buf[]) {

        if (S7 == null) {

        } else {
            buffer = S7.receiveData(buffer);
            buf = buffer;
            String string = bytesToHexString(buffer, buffer.length);
            Message fMag = new Message();
            fMag.what = 0;
            fMag.obj = string;
            rxHandler.sendMessage(fMag);

        }
        return buf.length;
    }
//    private Application mApplication;
//    private SerialPortFinder mSerialPortFinder;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mApplication = new Application();;
//        mSerialPortFinder = mApplication.mSerialPortFinder;
//
//        String[] entries = mSerialPortFinder.getAllDevices();
//        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
//
//    }

    static String getTimeHaveSec() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        String temp = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "." + cal.get(Calendar.MILLISECOND);
        try {
            temp = new Timestamp(format.parse(temp).getTime()).toString();
            /*if (temp.contains(".")) {
                temp = temp.substring(0, temp.indexOf("."));
            }*/
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * 接收线程
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                byte[] buffer = new byte[64];
                size = onRead(buffer);


            }
        }
    }

    static public Handler rxHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0: {
                        textView1.setText((String) msg.obj);
                    }
                    break;
                }
            } catch (Exception e) {

            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}