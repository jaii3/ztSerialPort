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

## 功能引用说明

    可将此包引用到实际项目中进行串口支持、RK3566 Android 11 系统功能支持的开发

### aar包导入

    1、此串口驱动包编译后生成 zt_serail-x.x.x.aar 包

````
    app build.gradle
    android{
    
         repositories {
            flatDir {
                dir 'libs'
            }
        }
    }
    
    dependencies {
       implementation(name:'zt_serail-1.0.0', ext:'aar')  
    }
````

### gradle应用

    2、gradle 引用(受jitpack环境、网络环境等影响，此方法并不推荐使用)

````
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    implementation 'com.github.jaii3:ztSerialPort:3.0.0'
````

### 1、串口支持说明

    1、打开串口
    串口通信前需要打开串口；

````
    /**
     * @param path     节点路径 "/dev/ttyS1" "/dev/ttyS2" "/dev/ttyS3" "/dev/ttyS4" "/dev/ttyS5"...
     * @param speed    波特率  2400/9600/115200 ...
     * @param dataBits 数据位，5 ~ 8  （默认8）
     * @param stopBits 停止位，1 或 2  （默认 1）
     * @param parity   奇偶校验，‘O' 'N' 'E'
     * @param flag     阻塞非阻塞 1:非阻塞  0 ：阻塞
     */
    ztSerialPortS0 = new ZtSerialPort("/dev/ttyS0", speed, dataBits, 1, parity, 1);
````

    2、发送数据
    打开串口后，使用 sendData 方法向下位机硬件设备发送数据

````
    ztSerialPortS0.sendData(data);
````

    3、接收数据

````
    ztSerialPortS0.receiveData();
````

    4、关闭串口

````
     ztSerialPortS0.close();
````

### 2、功能支持说明

#### 2.1定义：

````
        ZtSystem zTSystem;
````

#### 2.2初始化：

````
        zTSystem = ZtSystem.getInstance(getApplicationContext());
````

#### 2.3存储路径

     1、获取内部存储路径

````
        /**
         * 获取内部存储路径 （表示应用的内部存储目录）
         *
         * @return 例:/data/user/0/your_package/files
         */
        zTSystem.getInternalStorageDirectoryPath();
````

     2、获取外置SD卡存储路径 

````
         /**
         * 获取外部存储路径
         *
         * @return 例:/storage/emulated/0
         */
        zTSystem.getExtStorageDirectoryPath();
````

     3、获取外置SD卡存储路径

````
         /**
         * 获取外置SD卡存储路径
         *
         * @return 例:/storage/36BC-A2FB/
         */
        zTSystem.getExtSdCardPath();
````

     4、获取指定USB存储路径

````
        /**
         * 获取指定USB存储路径
         *
         * @param usbNum 1:usb1  2:usb2  3:usb3  4:usb4  5:usb5 ....
         * @return 例：/storage/4434-5102/
         */
        zTSystem.getUsbPath(1);
````

     5、获取第一个USB设备的存储路径

````
        /**
         * 获取第一个USB设备的存储路径
         *
         * @return 例：/storage/4434-5102/
         */
        zTSystem.getUsbPathFirst();
````

     6、获取全部USB存储路径

````
         /**
         * 获取全部USB存储路径
         *
         * @return List<FileStoreDevices>
         */
        zTSystem.getAllUsbPath();
````

#### 2.4熄屏/亮屏 操作

````
        /**
         * 熄屏/亮屏 操作
         *
         * @return
         */
        zTSystem.setScreenOffOn();
````

#### 2.5时间设置 操作

        1、设置时间

````
        /**
         * 设置时间
         *
         * @param time "yyyy-MM-dd HH:mm:ss"
         */
        zTSystem.setSystemTime("2023-01-02 12:00:30")
````

         2、设置时间

````
        /**
         * 设置时间
         *
         * @param year   年
         * @param month  月
         * @param day    日
         * @param hour   时
         * @param minute 分
         * @param second 秒
         */
        zTSystem.setSystemTime(2023,01,02,12,00,30);
````

#### 2.6导航栏/状态栏 操作

    1、永久隐藏显示

````
        /**
         * 设置 导航栏和状态栏
         * 注：该方法永久生效，重启也生效
         *
         * @param hide true: 隐藏导航栏 false: 显示导航栏
         */
        zTSystem.setNavigationBar(true);
````

    2、临时隐藏显示

````
        /**
         * 导航栏，状态栏隐藏
         * 注：控制系统导航栏的临时 隐藏/显示， 使用此方法隐藏导航栏后，可以通过下拉或者上滑唤出状态栏
         *
         * @param activity activity
         * @param hide     true: 隐藏 false: 显示
         */
        zTSystem.setNavigationBarStatusBar(Activity activity, boolean hide);
````


