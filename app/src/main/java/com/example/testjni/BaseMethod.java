package com.example.testjni;

import android.os.SystemClock;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class BaseMethod {
    void setDate(int year, int month, int day) throws IOException, InterruptedException {
        requestPermission();
        Calendar c = Calendar.getInstance();
        c.set(1, year);
        c.set(2, month);
        c.set(5, day);
        long when = c.getTimeInMillis();
        if (when / 1000L < 2147483647L) {
            SystemClock.setCurrentTimeMillis(when);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (now - when > 1000L) {
            throw new IOException("failed to set Date.");
        }
    }

    void setTime(int hour, int minute, int second) throws IOException, InterruptedException {
        requestPermission();
        Calendar c = Calendar.getInstance();
        c.set(11, hour);
        c.set(12, minute);
        c.set(13, second);
        long when = c.getTimeInMillis();
        if (when / 1000L < 2147483647L) {
            SystemClock.setCurrentTimeMillis(when);
        }

        long now = Calendar.getInstance().getTimeInMillis();
        if (now - when > 1000L) {
            throw new IOException("failed to set Time.");
        }
    }

    void requestPermission() throws InterruptedException, IOException {
        createSuProcess("chmod 666 /dev/alarm").waitFor();
    }

    Process createSuProcess() throws IOException {
        File rootUser = new File("/system/xbin/ru");
        return rootUser.exists() ? Runtime.getRuntime().exec(rootUser.getAbsolutePath()) : Runtime.getRuntime().exec("su");
    }

    Process createSuProcess(String cmd) throws IOException {
        DataOutputStream os = null;
        Process process = createSuProcess();

        try {
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException var9) {
                }
            }

        }

        return process;
    }
}
