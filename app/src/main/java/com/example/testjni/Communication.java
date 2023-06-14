package com.example.testjni;

/**
 * Created by caoyiliang on 2017/1/19.
 * 通讯公共抽象类
 */

public abstract class Communication {

    public abstract <T> void sendData(T data);

    public abstract <T> T receiveData(T data);

    public abstract void close();

    public abstract boolean isClose();

    public enum Port {
        DwinPort,
        WeiQianPort,
        SerialPortMC,
        YangChuangPort,
        HaoJuPort,
        SerialPort,
        TCP
    }
}
