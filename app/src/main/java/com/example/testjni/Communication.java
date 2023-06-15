package com.example.testjni;

/**
 * @author caoyiliang
 * @date 2017/1/19
 * 通讯公共抽象类
 */

public abstract class Communication {

    /**
     * @param data 数据发送
     * @param <T>
     */
    public abstract <T> void sendData(T data);

    /**
     * @param data 数据存储buffer
     * @param <T>
     * @return
     */
    public abstract <T> T receiveData(T data);

    /**
     * 关闭串口
     */
    public abstract void close();

    /**
     * 串口是否关闭
     *
     * @return true 串口关闭 false:串口开
     */
    public abstract boolean isClose();
}
