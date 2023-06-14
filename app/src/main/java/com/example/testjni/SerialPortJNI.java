package com.example.testjni;

import java.io.FileDescriptor;

public class SerialPortJNI {
    static {
        System.loadLibrary("SerialPort");//加载.C
    }
    // JNI
    public native static FileDescriptor open(String path, int baudrate, int flags);

    // JNI
    public native FileDescriptor open(String path, int baudrate, int dataBits, int parity,
                                       int stopBits, int flags);
    public native void close();
}
