package com.example.testjni;


import java.util.concurrent.ExecutorService;

public class TcpPort extends Communication {

    private SocketConnect myTcp;
    private boolean enable;      // 使能

    @Override
    public <T> void sendData(T data) {
        myTcp.sendData(data);
    }

    @Override
    public <T> T receiveData(T data) {
        return myTcp.Receive(data);
    }

    @Override
    public void close() {
        if (myTcp != null) {
            myTcp.disconnect();
        }
        enable = false;
    }

    @Override
    public boolean isClose() {
        return enable;
    }

    public TcpPort(String ip, int port) {
        try {
            myTcp = new SocketConnect(ip, port);
            enable = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
