package com.example.testjni;

import java.io.FileDescriptor;

/**
 * @author HP
 */
public class SerialPortJNI {

    static {//加载.C
        System.loadLibrary("ZtSerialPort");
    }

    /**
     * @param path     节点路径 "/dev/ttyS1" "/dev/ttyS2" "/dev/ttyS3" "/dev/ttyS4" "/dev/ttyS5"...
     * @param baud    波特率  2400/9600/115200 ...
     * @param dataBits 数据位，5 ~ 8  （默认8）
     * @param stopBits 停止位，1 或 2  （默认 1）
     * @param parity   奇偶校验，‘O' 'N' 'E'
     * @param flags     阻塞非阻塞 1:非阻塞  0 ：阻塞
     * @return
     */
    // JNI
    public native FileDescriptor open(String path, int baud, int dataBits, int stopBits, char parity, int flags);


    /**
     *
     */
    public native void close();
}
