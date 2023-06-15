package com.example.testjni;

import java.io.FileDescriptor;

/**
 * @author HP
 */
public class SerialPortJNI {
    static {//加载.C
        System.loadLibrary("SerialPort");
    }

    /**
     * @param path
     * @param baudrate
     * @param flags
     * @return
     */
    // JNI
    public native static FileDescriptor open(String path, int baudrate, int flags);

    /**
     * @param path
     * @param baud
     * @param dataBits
     * @param parity
     * @param stopBits
     * @param flags
     * @return
     */
    // JNI
    public native FileDescriptor open(String path, int baud, int dataBits, int parity, int stopBits, int flags);

    /**
     *
     */
    public native void close();
}
