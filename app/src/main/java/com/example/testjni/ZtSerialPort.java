package com.example.testjni;

import static java.lang.System.arraycopy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author HP
 */
public class ZtSerialPort extends Communication {
    private final SerialPortOpt serialPortOpt;
    private InputStream mInputStream;
    protected OutputStream mOutputStream;
    public boolean isOpen = false;

    private final byte[] rsBuffer = new byte[1024];

    /**
     * @param path     串口号
     * @param baud     波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity   奇偶校验位
     */
    public ZtSerialPort(String path, int baud, int dataBits, int stopBits, int parity) {
        serialPortOpt = new SerialPortOpt(new File(path), baud, 0);
        openSerial(path, baud, dataBits, stopBits, parity);
    }


    private boolean openSerial(String devNum, int speed, int dataBits,
                               int stopBits, int parity) {
        if (serialPortOpt == null) {
            return false;
        }
        serialPortOpt.mDevNum = devNum;
        serialPortOpt.mDataBits = dataBits;
        serialPortOpt.mSpeed = speed;
        serialPortOpt.mStopBits = stopBits;
        serialPortOpt.mParity = parity;

        mInputStream = serialPortOpt.getInputStream();
        mOutputStream = serialPortOpt.getOutputStream();
        isOpen = true;
        return true;

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
        serialPortOpt.close();
        isOpen = false;
    }

    @Override
    public boolean isClose() {
        return isOpen;
    }
}
