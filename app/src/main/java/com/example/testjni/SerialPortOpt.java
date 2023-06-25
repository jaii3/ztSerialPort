/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.testjni;

import android.util.Log;

import com.example.testjni.SerialPortJNI;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author HP
 */
public class SerialPortOpt extends SerialPortJNI {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public FileDescriptor mFd;

    public String mDevNum; //串口号
    public int mSpeed;     //波特率
    public int mDataBits;  //数据位
    public int mStopBits;  //停止位
    public int mParity;    //奇偶校验位

    /**
     * @param device     节点路径 "/dev/ttyS1" "/dev/ttyS2" "/dev/ttyS3" "/dev/ttyS4" "/dev/ttyS5"...
     * @param baud    波特率  2400/9600/115200 ...
     * @param dataBits 数据位，5 ~ 8  （默认8）
     * @param stopBits 停止位，1 或 2  （默认 1）
     * @param parity   奇偶校验，‘O' 'N' 'E'
     * @param flags     阻塞非阻塞 1:非阻塞  0 ：阻塞
     */
    public SerialPortOpt(File device, int baud, int dataBits, int stopBits, char parity, int flags) {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec("/system/bin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                throw new SecurityException();
            }
        }
        mFd = open(device.getAbsolutePath(), baud, dataBits, stopBits, parity, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


}
