package com.example.testjni;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ZTSerialPortTest extends Communication {
    private final SerailPortOpt serialportopt;
    private InputStream mInputStream;
    protected OutputStream mOutputStream;
    public boolean isOpen = false;

    private byte[] rsBuffer = new byte[1024];

    /**
     * @param path     串口号
     * @param baudrate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity   奇偶校验位
     */
    public ZTSerialPortTest(String path, int baudrate, int dataBits, int stopBits,
                            int parity) {
        serialportopt = new SerailPortOpt(new File(path), baudrate, 0);
        openSerial(path, baudrate, dataBits, stopBits, parity);
    }


    private boolean openSerial(String devNum, int speed, int dataBits,
                               int stopBits, int parity) {
        if (serialportopt == null) {
            return false;
        }
        serialportopt.mDevNum = devNum;
        serialportopt.mDataBits = dataBits;
        serialportopt.mSpeed = speed;
        serialportopt.mStopBits = stopBits;
        serialportopt.mParity = parity;

        mInputStream = serialportopt.getInputStream();
        mOutputStream = serialportopt.getOutputStream();
        isOpen = true;
        return true;

    }


    private String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }


    private static byte[] hexString2Bytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < tmp.length / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }


    public static String bytesToHexString(byte[] src, int size) {
        String ret = "";
        if (src == null || size <= 0) {
            return null;
        }
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(src[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            hex += " ";
            ret += hex;
        }
        return ret.toUpperCase();
    }


    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0}))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1}))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    @Override
    public <T> void sendData(T data) {
        try {
            mOutputStream.write((byte[]) data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T receiveData(T data) {
        int size;
        if (mInputStream == null) {
            return null;
        }
        try {
            size = mInputStream.read(rsBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (size > 0) {
            byte[] cutBuffer = new byte[size];
            try {
                arraycopy(rsBuffer, 0, cutBuffer, 0, size);
                data = (T) cutBuffer;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        serialportopt.close();
        isOpen = false;
    }

    @Override
    public boolean isClose() {
        return isOpen;
    }
}
