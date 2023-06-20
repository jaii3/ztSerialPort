package com.example.testjni;

import static java.lang.System.arraycopy;
import static java.lang.Thread.sleep;

import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author caoyiliang
 * @date 2017/1/19
 * 通讯公共抽象类
 */

public abstract class Communication {

    /**
     * 发送串口数据
     *
     * @param data 数据buffer
     * @param <T>
     */
    public abstract <T> void sendData(T data);

    /**
     * 接收串口数据
     *
     * @param <T>
     * @return
     */
    public abstract <T> T receiveData();

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
