# ztSerialPort 串口驱动包

## 驱动环境

RK3566 Android 11

## 硬件环境：

硬件PCB板：
846B板：  
232:ttyS1 \ ttyS3 \ ttyS4 \ ttyS5  
485:ttyS6 \ ttyS7 \ ttyS8 \ ttyS9

## 软件环境：

zt1.0.1

## 串口设备查询

adb shell  
ls -l /dev/ttyS*

## 代码说明

    1、此串口驱动包编译后生成zt_serail.aar 包，可将此包应用到项目中进行串口支持、RK3566 Android 11 系统功能支持
    2、gradle 引用

    ```
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    implementation 'com.github.jaii3:ztSerialPort:1.0.0'
    ```

### 串口支持说明

    1、打开串口
    串口通信前需要打开串口；
    `ztSerialPortS0 = new ZtSerialPort("/dev/ttyS0", speed, dataBits, 1, parity, 1);`

    2、发送数据
    打开串口后，使用 sendData 方法向下位机硬件设备发送数据
    ```
    ztSerialPortS0.sendData(data);
    ````
    3、接收数据
    ```
    ztSerialPortS0.receiveData(data);
    ````
    4、关闭串口
     ztSerialPortS0.close();

