package com.example.testjni.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testjni.Communication;
import com.example.testjni.R;
import com.example.testjni.SerialPortFinder;
import com.example.testjni.TcpPort;
import com.example.testjni.ZTSerialPortTest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.testjni.ZTSerialPortTest.bytesToHexString;
import static com.example.testjni.sample.MainActivity.saveRunInfo2File;


public class ComActivity extends AppCompatActivity {

    public static ZTSerialPortTest S0;
    public static TcpPort TCP;
    private Thread mReadThread;
    Timer sendTimer;

    /**
     * 存储的文件名
     */
    public static final String DATABASE = "ZT_Tool";

    Spinner mspDevices, mspDevicesBaud;
    static TextView txReceTest;
    Button buttonSwitch, buttonSend;
    EditText editSend, editTCPIp, editTCPPort, editAuto;
    RadioButton radioButtonShowSwitch, radioButtonCOM, radioButtonTCP, radioButtonMode1, radioButtonMode2;
    RadioGroup rdioGroup1;
    CheckBox checkBoxShow, checkBoxAuto;
    TextView text4, text5, text6, text7;
    boolean bReadThread = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.com_test);

        //软键盘遮挡edittext情况的处理
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //获取所用串口地址
        String[] devicesPath = new SerialPortFinder().getAllDevicesPath();

        Button buttonRTN = findViewById(R.id.button_return);
        buttonRTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mReadThread != null) {
                    bReadThread = false;
                    mReadThread = null;
                }
                if (sendTimer != null) {
                    sendTimer.cancel();
                    sendTimer = null;
                }
                finish();
            }
        });
        Button buttonClearR = findViewById(R.id.button_clear_receive);
        buttonClearR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message fMag = new Message();
                fMag.what = 1;
                cAHandler.sendMessage(fMag);
            }
        });

        txReceTest = findViewById(R.id.txReceTest);
        txReceTest.setMovementMethod(ScrollingMovementMethod.getInstance());
        txReceTest.setScrollbarFadingEnabled(false);

        editSend = findViewById(R.id.edit_send);
        //editSend.addTextChangedListener(new CustomTextWatcher(editSend));

        editTCPIp = findViewById(R.id.edit_tcp_ip);
        //默认直接显示数字键盘
        editTCPIp.setInputType(InputType.TYPE_CLASS_TEXT);
        editTCPIp.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editTCPPort = findViewById(R.id.edit_tcp_port);
        //默认直接显示数字键盘
        editTCPPort.setInputType(InputType.TYPE_CLASS_TEXT);
        editTCPPort.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editAuto = findViewById(R.id.edit_auto);
        //默认直接显示数字键盘
        editAuto.setInputType(InputType.TYPE_CLASS_TEXT);
        editAuto.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editAuto.setOnFocusChangeListener(new editTextFocusChange());
        editAuto.setOnEditorActionListener(new editTextFocusChange());

        rdioGroup1 = findViewById(R.id.RadioGroup1);
        checkBoxShow = findViewById(R.id.checkBox_show);
        checkBoxAuto = findViewById(R.id.checkBox_auto);
        checkBoxAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxAuto.isChecked()) {
                    if ("开启".equals(buttonSwitch.getText().toString())) {
                        Toast.makeText(ComActivity.this, "请先打开串口/TCP通讯！", Toast.LENGTH_SHORT).show();
                        checkBoxAuto.setChecked(false);
                        return;
                    }
                    if ("".equals(editAuto.getText().toString().trim())) {
                        Toast.makeText(ComActivity.this, "请先输入间隔时间！", Toast.LENGTH_SHORT).show();
                        checkBoxAuto.setChecked(false);
                        return;
                    }
                    int iPeriod = Integer.parseInt(editAuto.getText().toString().trim());
                    if (sendTimer == null) {
                        sendTimer = new Timer();
                        sendTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                SendMessage();
                            }
                        }, iPeriod, iPeriod);
                    }
                } else {
                    sendTimer.cancel();
                    sendTimer = null;
                }
            }
        });

        mspDevices = (Spinner) findViewById(R.id.spinner_com_name);
        List<String> list = new ArrayList<>();
        for (String device : devicesPath) {
            list.add(device);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspDevices.setAdapter(dataAdapter);

        mspDevicesBaud = (Spinner) findViewById(R.id.spinner_com_baud);
        List<String> listBaud = new ArrayList<>();
        for (String device : getResources().getStringArray(R.array.baud)) {
            listBaud.add(device);
        }
        ArrayAdapter<String> dataAdapterBaud = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listBaud);
        dataAdapterBaud.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspDevicesBaud.setAdapter(dataAdapterBaud);
        mspDevicesBaud.setSelection(1);

        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        text7 = findViewById(R.id.text7);
        radioButtonShowSwitch = findViewById(R.id.radioButton_show_switch);
        radioButtonCOM = findViewById(R.id.radioButton_COM);
        radioButtonCOM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioValChange(0);
            }
        });
        radioButtonTCP = findViewById(R.id.radioButton_TCP);
        radioButtonTCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioValChange(1);
            }
        });
        RadioValChange(0);

        String strCom_name = getValue("com_name", "/dev/ttyS3");
        String strCom_baud = getValue("com_baud", "9600bps");
        for (int i = 0; i < list.size(); i++) {
            if (strCom_name.equals(list.get(i))) {
                mspDevices.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < listBaud.size(); i++) {
            if (strCom_baud.equals(listBaud.get(i))) {
                mspDevicesBaud.setSelection(i);
                break;
            }
        }
        editTCPIp.setText(getValue("tcp_ip", "168.182.0.102"));
        editTCPPort.setText(getValue("tcp_port", "777"));
        editAuto.setText(getValue("auto_time", "500"));

        radioButtonMode1 = findViewById(R.id.radioButton_mode1);
        radioButtonMode2 = findViewById(R.id.radioButton_mode2);

        buttonSwitch = findViewById(R.id.button_switch);
        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("打开".equals(buttonSwitch.getText().toString())) {
                    bReadThread = true;
                    if (radioButtonCOM.isChecked()) {
                        radioButtonTCP.setEnabled(false);
                        S0 = new ZTSerialPortTest(mspDevices.getSelectedItem().toString(), Integer.parseInt(mspDevicesBaud.getSelectedItem().toString().replace("bps", "")), 8, 1, 110);
                        mspDevices.setEnabled(false);
                        mspDevicesBaud.setEnabled(false);
                        mReadThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (S0 != null && bReadThread) {
                                        byte[] buffer = new byte[1024];
                                        buffer = S0.receiveData(buffer);
                                        if (buffer != null) {
                                            String rtnStr = bytesToHexString(buffer, buffer.length);
                                            if (checkBoxShow.isChecked()) {
                                                onRead(getTimeHaveSec() + "  接收: " + rtnStr + '\n');
                                            } else {
                                                String rtnStrA = new String(buffer, 0, buffer.length, "GB2312");
                                                onRead(getTimeHaveSec() + "  接收: " + rtnStrA + '\n');
                                            }
                                            if (radioButtonMode1.isChecked()) {
                                                S0.sendData(copybyte(buffer, new byte[]{(byte) 0xAA, (byte) 0xBB}));
                                                onRead(getTimeHaveSec() + "  发送: " + rtnStr + " AA BB" + '\n');
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    mReadThread = null;
                                }
                            }
                        });
                        mReadThread.start();
                    } else {
                        if (TCP == null) {
                            try {
                                if ("".equals(editTCPIp.getText().toString()) || "".equals(editTCPPort.getText().toString())) {
                                    Toast.makeText(ComActivity.this, "请先输入ip和端口！", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                radioButtonCOM.setEnabled(false);
                                editTCPIp.setEnabled(false);
                                editTCPPort.setEnabled(false);
                                TCP = new TcpPort(editTCPIp.getText().toString().trim(), Integer.parseInt(editTCPPort.getText().toString().trim()));
                                mReadThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            byte[] rs;
                                            while (TCP != null && bReadThread) {
                                                try {
                                                    rs = new byte[4096];
                                                    rs = TCP.receiveData(rs);
                                                    if (rs != null) {
                                                        String rtnStr = bytesToHexString(rs, rs.length);
                                                        if (checkBoxShow.isChecked()) {
                                                            onRead(getTimeHaveSec() + "  接收: " + rtnStr + '\n');
                                                        } else {
                                                            String rtnStrA = new String(rs, 0, rs.length, "GB2312");
                                                            onRead(getTimeHaveSec() + "  接收: " + rtnStrA + '\n');
                                                        }
                                                        if (radioButtonMode1.isChecked()) {
                                                            TCP.sendData(copybyte(rs, new byte[]{(byte) 0xAA, (byte) 0xBB}));
                                                            onRead(getTimeHaveSec() + "  发送: " + rtnStr + " AA BB" + '\n');
                                                        }
                                                    }
                                                    try {
                                                        Thread.sleep(10);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                } catch (Exception e) {
                                                    Log.e("exception", e.toString());
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            mReadThread = null;
                                        }
                                    }
                                });
                                mReadThread.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    buttonSwitch.setText("关闭");
                    radioButtonShowSwitch.setEnabled(true);

                    SharedPreferencesData("com_name", mspDevices.getSelectedItem().toString());
                    SharedPreferencesData("com_baud", mspDevicesBaud.getSelectedItem().toString());
                    SharedPreferencesData("tcp_ip", editTCPIp.getText().toString());
                    SharedPreferencesData("tcp_port", editTCPPort.getText().toString());
                } else {
                    if (radioButtonCOM.isChecked()) {
                        S0.close();
                        S0 = null;
                        mspDevices.setEnabled(true);
                        mspDevicesBaud.setEnabled(true);
                    } else {

                        TCP.close();
                        TCP = null;
                        editTCPIp.setEnabled(true);
                        editTCPPort.setEnabled(true);
                    }
                    radioButtonCOM.setEnabled(true);
                    radioButtonTCP.setEnabled(true);
                    buttonSwitch.setText("打开");
                    radioButtonShowSwitch.setEnabled(false);
                    bReadThread = false;
                }
            }
        });

        buttonSend = findViewById(R.id.button_send);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });


    }

    private void RadioValChange(int type) {
        if (type == 0) {
            text4.setVisibility(View.VISIBLE);
            mspDevices.setVisibility(View.VISIBLE);
            text5.setVisibility(View.VISIBLE);
            mspDevicesBaud.setVisibility(View.VISIBLE);
            text6.setVisibility(View.INVISIBLE);
            editTCPIp.setVisibility(View.INVISIBLE);
            text7.setVisibility(View.INVISIBLE);
            editTCPPort.setVisibility(View.INVISIBLE);
        } else {
            text4.setVisibility(View.INVISIBLE);
            mspDevices.setVisibility(View.INVISIBLE);
            text5.setVisibility(View.INVISIBLE);
            mspDevicesBaud.setVisibility(View.INVISIBLE);
            text6.setVisibility(View.VISIBLE);
            editTCPIp.setVisibility(View.VISIBLE);
            text7.setVisibility(View.VISIBLE);
            editTCPPort.setVisibility(View.VISIBLE);
        }

    }

    /*protected class onRadioButtonClicked implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radioButton_COM:
                    break;
                case R.id.radioButton_TCP:
                    break;
                default:
                    break;
            }
        }
    }*/

    private class editTextFocusChange implements View.OnFocusChangeListener, TextView.OnEditorActionListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                SharedPreferencesData("auto_time", editAuto.getText().toString());
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus();
                return true;
            }
            return false;
        }
    }

    private void SendMessage() {
        String rtnStr = editSend.getText().toString().trim();
        boolean bSend = false;
        if (radioButtonCOM.isChecked()) {
            if (S0 != null && S0.isClose()) {
                S0.sendData(rtnStr.getBytes());
                bSend = true;
            }
        } else {
            if (TCP != null && TCP.isClose()) {
                TCP.sendData(rtnStr.getBytes());
                bSend = true;
            }
        }
        if (checkBoxShow.isChecked()) {
            byte[] buffer = rtnStr.getBytes();
            rtnStr = bytesToHexString(buffer, buffer.length);
        }
        if (bSend) {
            onRead(getTimeHaveSec() + "  发送: " + rtnStr + '\n');
        } else {
            //Toast.makeText(ComActivity.this, "请先打开串口/TCP通讯！", Toast.LENGTH_SHORT).show();
        }
    }

    static String getTimeHaveSec() {
        try {
            long currentTime = System.currentTimeMillis();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = new Date(currentTime);
            return formatter.format(date) + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void onRead(String str) {
        Message fMag = new Message();
        fMag.what = 0;
        fMag.obj = str;
        cAHandler.sendMessage(fMag);
    }

    @SuppressLint("HandlerLeak")
    static public Handler cAHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 0: {
                        txReceTest.append((String) msg.obj);
                        int offset = getTextViewContentHeight(txReceTest);//调用getTextViewContentHeight
                        if (offset > txReceTest.getHeight()) {
                            txReceTest.scrollTo(0, offset - txReceTest.getHeight());
                        }
                    }
                    break;
                    case 1: {
                        txReceTest.setText("");
                    }
                    break;
                    default:
                        break;
                }
            } catch (
                    Exception e) {

            }
        }
    };

    /**
     * 获取TextView中内容的高度
     *
     * @param textView
     * @return
     */
    public static int getTextViewContentHeight(TextView textView) {
        Layout layout = textView.getLayout();
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    public static byte[] copybyte(byte[] a, byte[] b) {
        byte[] rsbyte = new byte[a.length + b.length];
        System.arraycopy(a, 0, rsbyte, 0, a.length);
        System.arraycopy(b, 0, rsbyte, a.length, b.length);
        return rsbyte;
    }

    public static byte[] copybyte(byte[]... byteArray) {
        byte[] rsbyte = new byte[0];
        byte[][] var2 = byteArray;
        int var3 = byteArray.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            byte[] item = var2[var4];
            rsbyte = copybyte(rsbyte, item);
        }

        return rsbyte;
    }


    public void SharedPreferencesData(String key, String value) {
// 获取SharedPreferences对象
        SharedPreferences sp = getSharedPreferences(DATABASE,
                Activity.MODE_PRIVATE);
        // 获取Editor对象
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(key, value);
        editor.apply();
        //editor.commit();

    }

    public String getValue(String key, String defValue) {
        SharedPreferences sp = getSharedPreferences(DATABASE, Context.MODE_PRIVATE);
        String value = sp.getString(key, defValue);
        return value;
    }

}