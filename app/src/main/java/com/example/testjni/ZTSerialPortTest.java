package com.example.testjni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import static java.lang.System.arraycopy;


public class ZTSerialPortTest extends Communication {
    private SerailPortOpt serialportopt;
    private InputStream mInputStream;
    protected OutputStream mOutputStream;
    public boolean isOpen = false;

    /**
     *
     * @param path 串口号
     * @param baudrate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity 奇偶校验位
     */
    public ZTSerialPortTest(String path, int baudrate, int dataBits, int stopBits,
                            int parity) {
        serialportopt = new SerailPortOpt(new File(path), baudrate, 0);
        openSerial(path, baudrate, dataBits, stopBits, parity);
    }


    private boolean openSerial(String devNum, int speed, int dataBits,
                               int stopBits, int parity) {
        serialportopt.mDevNum = devNum;
        serialportopt.mDataBits = dataBits;
        serialportopt.mSpeed = speed;
        serialportopt.mStopBits = stopBits;
        serialportopt.mParity = parity;


        if (serialportopt == null) {
            /* Read serial port parameters */
            String path = "/dev/ttyS5";
            int baudrate = 115200;

            /* Check parameters */
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

            /* Open the serial port */
            serialportopt = new SerailPortOpt(new File(path), baudrate, 0);
            return false;
        } else {
            mInputStream = serialportopt.getInputStream();
            mOutputStream = serialportopt.getOutputStream();
            isOpen = true;
            return true;
        }
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


    private static byte[] HexString2Bytes(String src) {
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
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    @Override
    public <T> void sendData(T data) {
        try {
            mOutputStream.write((byte[])data);
        } catch (Exception e) {

        }
    }

    @Override
    public <T> T receiveData(T data) {
        byte[] buffer = new byte[1024];
        int size;
        if (mInputStream == null) {
            return null;
        }
        try {
            size = mInputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (size > 0) {
            byte[] cutBuffer = new byte[size];
            try {
                arraycopy(buffer, 0, cutBuffer, 0, size);
                data = (T) cutBuffer;
                //data = (T) (new String(buffer, 0, size, "gb2312")).toString();
            } catch (Exception e){
                e.printStackTrace();
            }
//            catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
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
