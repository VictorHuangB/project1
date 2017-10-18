package com.android.student.data.test.rwusb.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.rwusb.DataUtils;
import com.android.student.data.test.rwusb.Global;
import com.android.student.data.test.rwusb.PL2303Driver;
import com.android.student.data.test.rwusb.USBDriver;
import com.android.student.data.test.rwusb.USBDriver.USBPort;
import com.android.student.data.test.rwusb.WorkService;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ConnectUSBActivity extends Activity implements OnClickListener {

    private static Handler mHandler = null;
    private static String TAG = "ConnectUSBActivity";

    private LinearLayout linearLayoutUSBDevices;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectusb);
        // 初始化字符串资源
//        InitGlobalString();


        if (null == WorkService.workThread) {
            Intent intent = new Intent(this, WorkService.class);
            startService(intent);
        }
        mContext = this;

        linearLayoutUSBDevices = (LinearLayout) findViewById(R.id.linearLayoutUSBDevices);
        findViewById(R.id.buttonPrintForm).setOnClickListener(this);
        ((TextView) findViewById(R.id.title)).setText("成绩打印");
        mHandler = new MHandler(this);
        WorkService.addHandler(mHandler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            probe();
        } else {
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        WorkService.delHandler(mHandler);
        mHandler = null;
    }

    private byte[] convertBytes(byte[] buf1, byte[] buf2) {
        byte[] lastByte1 = new byte[buf1.length + buf2.length];
        DataUtils.copyBytes(buf2, 0, lastByte1, 0, buf2.length);
        DataUtils.copyBytes(buf1, 0, lastByte1, buf2.length, buf1.length);
        return lastByte1;
    }

    private byte[] getBytes(String text) {
        byte[] strbuf = new byte[0];
        try {
            strbuf = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strbuf;
    }

    private String getRightResult(String result) {
        return result == null ? "0" : result;
    }

    private byte[] getSend(ArrayList<byte[][]> byteArray) {
        byte[] lastByte1 = new byte[0];
        byte[] buf;
        for (byte[][] item : byteArray) {
            buf = DataUtils.byteArraysToBytes(item);
            lastByte1 = convertBytes(buf, lastByte1);
        }
        return lastByte1;
    }

    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.buttonPrintForm: {
                byte[] setHT = {0x1b, 0x44, 0x18, 0x00};
                byte[] HT = {0x09};
                byte[] LF = {0x0d, 0x0a};
                int count = 2;
                //模拟打印前10位学生的成绩
                List<UserInfo> boyInfos;
                List<UserInfo> girlInfos;
                if (MainApplication.boyInfos.size() >= count) {
                    boyInfos = MainApplication.boyInfos.subList(0, count);
                } else {
                    boyInfos = new ArrayList<>(MainApplication.boyInfos);
                }
                if (MainApplication.girlInfos.size() >= count) {
                    girlInfos = MainApplication.girlInfos.subList(0, count);
                } else {
                    girlInfos = new ArrayList<>(MainApplication.girlInfos);
                }
                byte[][] head = new byte[][]{
                        setHT, getBytes("男生成绩"), HT, getBytes(""), LF, LF
                };

                ArrayList<byte[][]> arrayList = new ArrayList<>();
                arrayList.add(head);

                for (UserInfo info : boyInfos) {
                    byte[][] name = new byte[][]{
                            setHT, getBytes("姓名"), HT, getBytes(info.name), LF
                    };
                    byte[][] code = new byte[][]{
                            setHT, getBytes("学号"), HT, getBytes(info.code), LF
                    };
                    byte[][] claz = new byte[][]{
                            setHT, getBytes("年级"), HT, getBytes(info.classes), LF
                    };
                    byte[][] grade = new byte[][]{
                            setHT, getBytes("班级"), HT, getBytes(info.grade), LF
                    };

                    arrayList.add(name);
                    arrayList.add(code);
                    arrayList.add(claz);
                    arrayList.add(grade);
                    switch (MainApplication.currentProject) {
                        case MainApplication.Project_Feihuoliang:
                            byte[][] result = new byte[][]{
                                    setHT, getBytes("肺活量"), HT, getBytes(info.feihuoliang + MainApplication.per), LF, LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Weight:
                            result = new byte[][]{
                                    setHT, getBytes("身高"), HT, getBytes(getRightResult(info.height) + "cm"), LF
                            };
                            arrayList.add(result);
                            result = new byte[][]{
                                    setHT, getBytes("体重"), HT, getBytes(getRightResult(info.weight) + "kg"), LF, LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Yangwoqizuo:
                            result = new byte[][]{
                                    setHT, getBytes("仰卧起坐"), HT, getBytes(info.yangwoqizuo + MainApplication.per), LF, LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Yintixiangshang:
                            result = new byte[][]{
                                    setHT, getBytes("引体向上"), HT, getBytes(info.yintixiangshang + MainApplication.per), LF, LF
                            };
                            arrayList.add(result);
                            break;
                    }


                }
                byte[][] end = new byte[][]{
                        LF
                };

                arrayList.add(end);
                head = new byte[][]{
                        setHT, getBytes("女成绩"), HT, getBytes(""), LF, LF
                };

                arrayList.add(head);

                for (UserInfo info : girlInfos) {
                    byte[][] name = new byte[][]{
                            setHT, getBytes("姓名"), HT, getBytes(info.name), LF
                    };
                    byte[][] code = new byte[][]{
                            setHT, getBytes("学号"), HT, getBytes(info.code), LF
                    };
                    byte[][] claz = new byte[][]{
                            setHT, getBytes("年级"), HT, getBytes(info.classes), LF
                    };
                    byte[][] grade = new byte[][]{
                            setHT, getBytes("班级"), HT, getBytes(info.grade), LF
                    };

                    arrayList.add(name);
                    arrayList.add(code);
                    arrayList.add(claz);
                    arrayList.add(grade);
                    switch (MainApplication.currentProject) {
                        case MainApplication.Project_Feihuoliang:
                            byte[][] result = new byte[][]{
                                    setHT, getBytes("肺活量"), HT, getBytes(info.feihuoliang + MainApplication.per), LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Weight:
                            result = new byte[][]{
                                    setHT, getBytes("身高"), HT, getBytes(getRightResult(info.height) + "cm"), LF
                            };
                            arrayList.add(result);
                            result = new byte[][]{
                                    setHT, getBytes("体重"), HT, getBytes(getRightResult(info.weight) + "kg"), LF, LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Yangwoqizuo:
                            result = new byte[][]{
                                    setHT, getBytes("仰卧起坐"), HT, getBytes(info.yangwoqizuo + MainApplication.per), LF, LF
                            };
                            arrayList.add(result);
                            break;
                        case MainApplication.Project_Yintixiangshang:
                            result = new byte[][]{
                                    setHT, getBytes("引体向上"), HT, getBytes(info.yintixiangshang + MainApplication.per), LF, LF
                            };
                            arrayList.add(result);
                            break;
                    }


                }
                end = new byte[][]{
                        LF, LF
                };

                arrayList.add(end);

                byte[] buf = getSend(arrayList);

                if (WorkService.workThread.isConnected()) {
                    Bundle data = new Bundle();
                    data.putByteArray(Global.BYTESPARA1, buf);
                    data.putInt(Global.INTPARA1, 0);
                    data.putInt(Global.INTPARA2, buf.length);
                    WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
                } else {
                    Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    private void probe() {
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceList.size() > 0) {
            // 初始化选择对话框布局，并添加按钮和事件

            while (deviceIterator.hasNext()) { // 这里是if不是while，说明我只想支持一种device
                final UsbDevice device = deviceIterator.next();
                Button btDevice = new Button(
                        linearLayoutUSBDevices.getContext());
                btDevice.setLayoutParams(new LayoutParams(
                        LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.usb_device_height)));
                btDevice.setGravity(Gravity.CENTER_VERTICAL
                        | Gravity.LEFT);
                btDevice.setText(String.format(" VID:%04X PID:%04X",
                        device.getVendorId(), device.getProductId()));
                btDevice.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        PendingIntent mPermissionIntent = PendingIntent
                                .getBroadcast(
                                        ConnectUSBActivity.this,
                                        0,
                                        new Intent(
                                                ConnectUSBActivity.this
                                                        .getApplicationInfo().packageName),
                                        0);
                        if (!mUsbManager.hasPermission(device)) {
                            mUsbManager.requestPermission(device,
                                    mPermissionIntent);
                            Toast.makeText(getApplicationContext(),
                                    Global.toast_usbpermit, Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            USBDriver.USBPort port = new USBPort(mUsbManager, device);
                            PL2303Driver.TTYTermios serial = new PL2303Driver.TTYTermios(9600,
                                    PL2303Driver.TTYTermios.FlowControl.NONE, PL2303Driver.TTYTermios.Parity.NONE,
                                    PL2303Driver.TTYTermios.StopBits.ONE, 8);
                            WorkService.workThread.connectUsb(port, serial);
                        }
                    }
                });
                linearLayoutUSBDevices.addView(btDevice);
            }
        }
    }

    static class MHandler extends Handler {

        WeakReference<ConnectUSBActivity> mActivity;

        MHandler(ConnectUSBActivity activity) {
            mActivity = new WeakReference<ConnectUSBActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectUSBActivity theActivity = mActivity.get();
            switch (msg.what) {
                /**
                 * DrawerService 的 onStartCommand会发送这个消息
                 */
                case Global.CMD_POS_WRITERESULT: {
                    int result = msg.arg1;
                    Toast.makeText(theActivity, (result == 1) ? Global.toast_success : Global.toast_fail,
                            Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "Result: " + result);
                    break;
                }
                case Global.MSG_WORKTHREAD_SEND_CONNECTUSBRESULT: {
                    int result = msg.arg1;
                    Toast.makeText(
                            theActivity,
                            (result == 1) ? Global.toast_success
                                    : Global.toast_fail, Toast.LENGTH_SHORT).show();
                    Log.v(TAG, "Connect Result: " + result);
                    if (1 == result) {
                        //PrintTest();
                    }
                    break;
                }

            }
        }

    }

}