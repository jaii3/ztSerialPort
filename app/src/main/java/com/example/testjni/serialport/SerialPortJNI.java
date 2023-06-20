package com.example.testjni.serialport;

import java.io.FileDescriptor;

/**
 * @author HP
 */
public class SerialPortJNI {

    static {//加载.C
        System.loadLibrary("ZtSerialPort");
    }

    /**
     * @param path
     * @param baud
     * @param dataBits
     * @param stopBits
     * @param parity
     * @param flags
     * @return
     */
    // JNI
    public native FileDescriptor open(String path, int baud, int dataBits, int stopBits, char parity, int flags);


    /**
     *
     */
    public native void close();
}
