package com.example.testjni;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import static com.example.testjni.ZTSerialPortTest.bytesToHexString;
import static java.lang.Thread.sleep;


@SuppressWarnings("unchecked")
class SocketConnect {

    private Socket socket = null;
    private String sIp;
    private int iPort;
    private InputStream is = null;
    private OutputStream os = null;
    private Thread connectThread;       // 连接线程
    private Thread connectOnceThread = null;//第一次连接到时候未连接上，进行执行尝试
    private boolean isCloseAlive = false;


    public SocketConnect(String sIp, int iPort) {
        this.sIp = sIp;
        this.iPort = iPort;
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Client()) {
                    if (connectOnceThread == null) {
                        connectOnceThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!isCloseAlive) {
                                    try {
                                        if (socket != null || Client()) {
                                            connectOnceThread = null;
                                            break;
                                        }
                                        try {
                                            sleep(5000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (Exception e) {
                                        Log.i("Tcp exception", e.toString());
                                    }
                                }
                            }
                        });
                        connectOnceThread.start();
                    }
                }
                connectThread = null;
            }
        });
        connectThread.start();
    }


    private Boolean isServerClose(Socket socket, byte[] data) {
        try {
            //socket.sendUrgentData(0);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            os = socket.getOutputStream();
            // 步骤2：写入需要发送的数据到输出流对象中
            os.write(data);
            // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
            // 步骤3：发送数据到服务端
            os.flush();
            return false;
        } catch (Exception se) {
            return true;
        }
    }

    /*
     * 连接服务器
     * **/
    private <T> Boolean Client() {
        boolean connect = false;
        try {
            //IP地址和端口号（对应服务端），我这的IP是本地路由器的IP地址
            socket = new Socket(sIp, iPort);
            // 判断客户端和服务器是否连接成功
            connect = socket.isConnected();

        } catch (UnknownHostException e1) {
            Log.e("Tcp Client", "Client:Socket UnknownHostException " + sIp + ":" + iPort + e1.toString());
        } catch (IOException e) {
            Log.e("Tcp Client", "Client:Socket IOException " + sIp + ":" + iPort + e.toString());
        } catch (Exception e) {
            Log.e("Tcp Client", "Client:Socket Exception " + sIp + ":" + iPort + e.toString());
        }
        return connect;
    }

    /*
    发送数据
    * */
    <T> void sendData(T data) {
        try {
            try {
                if (socket == null || data == null) {
                    Log.i("Tcp Send", "Client:Socket null " + sIp + ":" + iPort);
                    return;
                }
                if (socket.isConnected()) {//这里有问题，当网突然断开的时候，此处判断还是true，不会去报异常，重连
                    // 步骤1：从Socket 获得输出流对象OutputStream
                    // 该对象作用：发送数据
                    os = socket.getOutputStream();
                    // 步骤2：写入需要发送的数据到输出流对象中
                    os.write((byte[]) data);
                    // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                    // 步骤3：发送数据到服务端
                    os.flush();
                } else {
                    Log.i("Tcp Send", "socket 连接错误，请重试...." + sIp + ":" + iPort);
                }
            } catch (SocketException se) {
                socket = new Socket(sIp, iPort);
                Log.e("Tcp Send", "Client:Socket  正在重连1...." + sIp + ":" + iPort);
                if (isServerClose(socket, (byte[]) data)) {
                    socket = new Socket(sIp, iPort);
                    Log.e("Tcp Send", "Client:Socket  正在重连2...." + sIp + ":" + iPort);
                }
            } catch (Exception e) {
                Log.e("Tcp Send", "Client:Socket Exception " + sIp + ":" + iPort + e.toString());
            } finally {
                /* Log.i("Tcp Send", "Client:Socket closed " + sIp + ":" + iPort);
                 */
            }
        } catch (Exception e) {
            Log.e("Tcp Send", "Client:Socket Exception " + sIp + ":" + iPort + e.toString());
        }
    }


    /*
    接收服务器数据
    * */
    <T> T Receive(T data) {
        try {
            if (socket != null) {
                is = socket.getInputStream();
                byte[] buf = new byte[4096];
                if (socket.isConnected()) {
                    int size = is.read(buf);
                    if (size > 0) {
                        try {
                            if (data instanceof byte[]) {
                                byte[] e = new byte[size];
                                System.arraycopy(buf, 0, e, 0, size);
                                data = (T) e;
                            } else {
                                data = (T) (new String(buf, 0, size, "gb2312"));
                            }
                        } catch (UnsupportedEncodingException var5) {
                            Log.e("exception", var5.toString());
                        }
                        if (data instanceof byte[]) {
                            Log.i("Tcp Receive", sIp + ":" + iPort + ":" + bytesToHexString((byte[]) data, ((byte[]) data).length));
                        }
                        return data;
                    } else if (size == -1) {
                        socket = new Socket(sIp, iPort);
                        Log.e("Tcp Send", "Client:Socket  正在重连1...." + sIp + ":" + iPort);
                        if (isServerClose(socket, (byte[]) data)) {
                            socket = new Socket(sIp, iPort);
                            Log.e("Tcp Send", "Client:Socket  正在重连2...." + sIp + ":" + iPort);
                        }
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return null;
    }

    <T> T disconnect() {
        try {
            isCloseAlive = true;
            if (socket != null) {
                if (!socket.isClosed()) {
                    if (!socket.isInputShutdown()) {
                        socket.shutdownInput();
                    }
                    if (!socket.isOutputShutdown()) {
                        socket.shutdownOutput();
                    }
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                    socket.close();
                }
                socket = null;
                Log.e("log", "onDisconnected");
            }
        } catch (Exception e) {
            Log.e("exception", "onError");
        }
        return null;
    }

    /**
     * =========通过ip ping 来判断ip是否通
     *
     * @param ip
     */
    private boolean judgeTheConnect(String ip) {

        try {
            if (ip != null) {
                //代表ping 3 次 超时时间为10秒
                Process p = Runtime.getRuntime().exec("ping -c 3 -w 10 " + ip);//ping3次
                int status = p.waitFor();
                if (status == 0) {
                    //代表成功
                    //Toast.makeText(mActivity, "成功", Toast.LENGTH_SHORT).show();
                    Log.i("Tcp Send", "Client:ping " + sIp + "      result 成功 0");
                    return true;
                } else {
                    //代表失败
                    //Toast.makeText(mActivity, "失败", Toast.LENGTH_SHORT).show();
                    Log.i("Tcp Send", "Client:ping " + sIp + "      result 失败 1");
                    return false;
                }
            } else {
                //代表失败
                //Toast.makeText(mActivity, "IP为空", Toast.LENGTH_SHORT).show();
                Log.i("Tcp Send", "Client:ping " + sIp + "      result IP为空 2");
                return false;
            }
        } catch (Exception e) {
        }
        return false;
    }
}

