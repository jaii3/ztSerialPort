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

    private final byte[] rsBuffer = new byte[2000];

    /**
     * @param path     节点路径 "/dev/ttyS1" "/dev/ttyS2" "/dev/ttyS3" "/dev/ttyS4" "/dev/ttyS5"...
     * @param speed    波特率  2400/9600115200 ...
     * @param dataBits 数据位，5 ~ 8  （默认8）
     * @param stopBits 停止位，1 或 2  （默认 1）
     * @param parity   奇偶校验，‘O' 'N' 'E'
     */
    public ZtSerialPort(String path, int speed, int dataBits, int stopBits, char parity) {
        serialPortOpt = new SerialPortOpt(new File(path), speed, dataBits, stopBits, parity, 0);
        openSerial(path, speed, dataBits, stopBits, parity);
    }

    /**
     * @param path     节点路径 "/dev/ttyS1" "/dev/ttyS2" "/dev/ttyS3" "/dev/ttyS4" "/dev/ttyS5"...
     * @param speed    波特率  2400/9600115200 ...
     * @param dataBits 数据位，5 ~ 8  （默认8）
     * @param stopBits 停止位，1 或 2  （默认 1）
     * @param parity   奇偶校验，‘O' 'N' 'E'
     * @return
     */
    private boolean openSerial(String path, int speed, int dataBits, int stopBits, int parity) {
        if (serialPortOpt == null) {
            return false;
        }
        serialPortOpt.mDevNum = path;
        serialPortOpt.mDataBits = dataBits;
        serialPortOpt.mSpeed = speed;
        serialPortOpt.mStopBits = stopBits;
        serialPortOpt.mParity = parity;

        mInputStream = serialPortOpt.getInputStream();
        mOutputStream = serialPortOpt.getOutputStream();
        isOpen = true;
        return true;
    }

    public SerialPortOpt getSerialPortOpt() {
        return serialPortOpt;
    }

    @Override
    public <T> void sendData(T data) {
        try {
            mOutputStream.write((byte[]) data);
            mOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T receiveData() {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return (T) cutBuffer;
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
