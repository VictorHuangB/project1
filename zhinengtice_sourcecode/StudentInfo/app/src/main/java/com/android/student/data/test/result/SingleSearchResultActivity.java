package com.android.student.data.test.result;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.SerialPort;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataItemDisplayDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.rwusb.DataUtils;
import com.android.student.data.test.rwusb.Global;
import com.android.student.data.test.rwusb.PL2303Driver;
import com.android.student.data.test.rwusb.USBDriver;
import com.android.student.data.test.rwusb.WorkService;
import com.android.student.data.test.studentinfo.StudentColomn;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hailong on 2016/11/2 0002.
 */
public class SingleSearchResultActivity extends Activity {
    View back_container;//返回按钮

    ListView data_list;//学生信息
    DataAdapter dataAdapter;
    ArrayList<UserInfo> userInfos = new ArrayList<>();
    ArrayList<String> columns = new ArrayList<>();
    String title;

    private OutputStream mOutputStream;
    private InputStream mInputStream;
//    private SingleSearchResultActivity.ReadThread mReadThread;

    private SerialPort mSerialPort = null;
    private UserInfo writeUserInfo;
    //打印
    private static Handler mHandler = null;
    private static boolean connectSuccess;
//    boolean stop = false;

//    private class ReadThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            while (!isInterrupted() && !stop) {
//                int size;
//                try {
//                    byte[] buffer = new byte[32];
//                    if (mInputStream != null) {
//                        size = mInputStream.read(buffer);
//                        if (size > 0) {
//                            onDataReceived(buffer, size);
//                        }
//                    }
//                } catch (IOException e) {
//                    Log.d("hailong30", " IOException " + e.getMessage());
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        }
//    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            String path = "/dev/ttyS3/";
            int baudrate = 9600;
            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }


    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SingleSearchResultActivity.this.finish();
            }
        });
        b.show();
    }

    protected void write(String data) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try {
            Log.d("hailong13", "CC data length is " + data.length());
            if (data != null) {
                if (data.length() >= 4) {
                    byteArrayOS.write(Integer.decode("0X" + data.substring(0, 2)).byteValue());
                    byteArrayOS.write(Integer.decode("0X" + data.substring(2, 4)).byteValue());
                }


                if (mOutputStream != null && byteArrayOS != null) {
                    mOutputStream.write(byteArrayOS.toByteArray());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler ICHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        public void run() {
            int size = 0;
            try {
                byte[] buffer = new byte[32];
                if (mInputStream != null) {
                    size = mInputStream.read(buffer);
                    Log.d("hailong50", " mInputStream " + mInputStream + " size " + size);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                }
            } catch (IOException e) {
                Log.d("hailong30", " IOException " + e.getMessage());
                e.printStackTrace();
            }
            ICHandler.postDelayed(mRunnable, 100);
        }

    };

    private void onDataReceived(final byte[] buffer, final int size) {
        String result = AppUtils.bytesToHexString(buffer);
        String code = result.replace("\n", "").replace("\r", "");
        boolean success = "00".equals(code.substring(2, 4));
        Log.d("hailong30", " code is " + code);
        if (success) {
            if (writeDataOk) {
                ICHandler.removeCallbacks(mRunnable);
                closeSerialPort();
                try {
                    if (mInputStream != null) {
                        mInputStream.close();
                    }
                    if (mOutputStream != null) {
                        mOutputStream.flush();
                        mOutputStream.close();
                    }
                    mInputStream = null;
                    mOutputStream = null;
                } catch (Exception e) {

                }
                Log.d("hailong30", " write data OK ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SingleSearchResultActivity.this, "写入IC卡成功", Toast.LENGTH_LONG).show();
                    }
                });

            } else if (writeCodeSexOk) {
                if (writeUserInfo != null) {
                    writeData(writeUserInfo);
                } else {
                    toastMsg("没有用户信息");
                }
            } else if (isPinLoad) {
                if (writeUserInfo != null) {
                    writeCodeSex(writeUserInfo);
                } else {
                    toastMsg("没有用户信息");
                }

            } else {
                Log.d("hailong30", "Again");
                writeUserInfo = userInfos.get(0);
                isPinLoad = writeCodeSexOk = writeDataOk = false;
                startICSeriaport();
                loadPin();
            }
        }
    }

    private void toastMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SingleSearchResultActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    byte[] pinBytes = new byte[]{
            0X09, 0X06, 0X60, 0X00,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF//(06 为命令字, 60 为密码 A(61 为密码 B), 00为扇区号, 12 个 F 为密码)

    };
    byte[] write_code = {
            0X12, 0X12, 0X26, 0X02, 0X60, 0X00, 0X01, 0X01,
            (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF),
            0X12, 0X34, 0X56, 0X78, (byte) 0X90,//学号表示 1234567890
            0X02, //01 男 02 女
            // 0X00, 0X00, 0X00, 0X00, 0X00
    };
    //B8DFCFFECBC92020
    byte[] write_data_utf = {
            0X15, 0X12, 0X26, 0X02, 0X60, 0X00, 0X02, 0X02,
            (byte) (0X9A), (byte) (0XD8), (byte) (0X66), (byte) (0X53), (byte) (0X67), (byte) (0X7E), 0X20, (byte) 0X20,//姓名
            0X03,           //年级编号 三年级
            0X00, 0X12,     //班级编号0012
            0X12,           //班级12
            0X00, 0X01,     //序号 0001
            (byte) 0XFF, (byte) 0XFF
    };
    byte[] write_data_gbk = {
            0X15, 0X12, 0X26, 0X02, 0X60, 0X00, 0X02, 0X02,
            (byte) (0XB8), (byte) (0XDF), (byte) (0XCF), (byte) (0XFE), (byte) (0XCB), (byte) (0XC9), 0X20, (byte) 0X20,//姓名
            0X03,           //年级编号 三年级
            0X00, 0X12,     //班级编号0012
            0X12,           //班级12
            0X00, 0X01,     //序号 0001
            (byte) 0XFF, (byte) 0XFF
    };

    boolean isPinLoad = false;
    boolean writeCodeSexOk = false;
    boolean writeDataOk = false;

    //下载密码
    public void loadPin() {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(pinBytes);
                isPinLoad = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //写学号，性别
    private void writeCodeSex(UserInfo info) {
        String targetCode = "";
        String head = "1212260260000101";
        String code = AppUtils.getCompactCode(info.code);
        String sex = "男".equals(info.sex) ? "01" : "02";
        targetCode = head + code + sex;
        byte[] write_code = AppUtils.stringToBytes(targetCode);
//        byte[] write_code = {
//                0X12, 0X12, 0X26, 0X02, 0X60, 0X00, 0X01, 0X01,
//                (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF),
//                0X12, 0X34, 0X56, 0X78, (byte) 0X90,//学号表示 1234567890
//                0X02, //01 男 02 女
//                // 0X00, 0X00, 0X00, 0X00, 0X00
//        };
        try {
            if (mOutputStream != null) {
                mOutputStream.write(write_code);
                writeCodeSexOk = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //写其他信息
    private void writeData(UserInfo info) {
        String targetData = "";
        String head = "1512260260000202";
        String name = AppUtils.getCompactName(info.name);
        String clas = PatternUtils.getWriteICclass(info.classes);
        String clssCode = "0012";//TODO
        String grade = PatternUtils.getWriteICclass(info.grade);
        String order = "0001";
        String end = "FFFF";
        targetData = head + name + clas + clssCode + grade + order + end;
        byte[] write_data = AppUtils.stringToBytes(targetData);
//        byte[] write_data_gbk = {
//                0X15, 0X12, 0X26, 0X02, 0X60, 0X00, 0X02, 0X02,
//                (byte) (0XB8), (byte) (0XDF), (byte) (0XCF), (byte) (0XFE), (byte) (0XCB), (byte) (0XC9), 0X20, (byte) 0X20,//姓名
//                0X03,           //年级编号 三年级
//                0X00, 0X12,     //班级编号0012
//                0X12,           //班级12
//                0X00, 0X01,     //序号 0001
//                (byte) 0XFF, (byte) 0XFF
//        };
        try {
            if (mOutputStream != null) {
                Log.d("hailong30", " write data " + targetData);
                mOutputStream.write(write_data);
                writeDataOk = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        ICHandler.removeCallbacks(mRunnable);
        closeSerialPort();
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.flush();
                mOutputStream.close();
            }
            mInputStream = null;
            mOutputStream = null;
        } catch (Exception e) {

        }

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_search_result_layout);
        back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = getIntent().getStringExtra("title");
        ((TextView) findViewById(R.id.title)).setText(title);
//        userInfos.add(new UserInfo());
        columns = AppUtils.getColumns(SingleSearchResultActivity.this);
        userInfos.addAll(new ArrayList<>(MainApplication.searchInfos));
        if (userInfos != null && userInfos.size() == 1) {
            writeUserInfo = userInfos.get(0);
        }
        initTitles();
        data_list = (ListView) findViewById(R.id.data_list);
        dataAdapter = new DataAdapter(this, userInfos);
        data_list.setAdapter(dataAdapter);
        //打印
        mHandler = new MHandler(this);
        WorkService.addHandler(mHandler);

//        MainApplication.searchInfos.clear();
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
            strbuf = text.getBytes("gbk");
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

    public void print() {
        byte[] setHT = {0x1b, 0x44, 0x18, 0x00};
        byte[] HT = {0x09};
        byte[] LF = {0x0d, 0x0a};
        int count = userInfos.size();
        //模拟打印前10位学生的成绩
        List<UserInfo> boys = null;
        if (userInfos.size() >= count) {
            boys = this.userInfos.subList(0, count);
        } else {
            boys = new ArrayList<>(userInfos);
        }

        ArrayList<byte[][]> arrayList = new ArrayList<>();

        for (UserInfo info : boys) {
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

        byte[] buf = getSend(arrayList);

        if (WorkService.workThread.isConnected()) {
            Bundle data = new Bundle();
            data.putByteArray(Global.BYTESPARA1, buf);
            data.putInt(Global.INTPARA1, 0);
            data.putInt(Global.INTPARA2, buf.length);
            WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
        } else {
            Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
            connectSuccess = false;
            //连接打印机
            probe();
        }
    }

    public void probe() {
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceList.size() > 0) {
            final UsbDevice device = deviceIterator.next();

            PendingIntent mPermissionIntent = PendingIntent
                    .getBroadcast(
                            SingleSearchResultActivity.this,
                            0,
                            new Intent(
                                    SingleSearchResultActivity.this
                                            .getApplicationInfo().packageName),
                            0);
            if (!mUsbManager.hasPermission(device)) {
                mUsbManager.requestPermission(device,
                        mPermissionIntent);
                Toast.makeText(getApplicationContext(),
                        Global.toast_usbpermit, Toast.LENGTH_LONG)
                        .show();
            } else if (!connectSuccess) {
                USBDriver.USBPort port = new USBDriver.USBPort(mUsbManager, device);
                PL2303Driver.TTYTermios serial = new PL2303Driver.TTYTermios(9600,
                        PL2303Driver.TTYTermios.FlowControl.NONE, PL2303Driver.TTYTermios.Parity.NONE,
                        PL2303Driver.TTYTermios.StopBits.ONE, 8);
                WorkService.workThread.connectUsb(port, serial);
            } else {
                print();
            }
        } else {
            toastMsg("没有可用打印机");
        }
    }

    static class MHandler extends Handler {

        SingleSearchResultActivity mActivity;

        MHandler(SingleSearchResultActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null) {
                return;
            }
            switch (msg.what) {
                /**
                 * DrawerService 的 onStartCommand会发送这个消息
                 */
                case Global.CMD_POS_WRITERESULT: {
                    int result = msg.arg1;
//                    Toast.makeText(theActivity, (result == 1) ? Global.toast_success : Global.toast_fail,
//                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case Global.MSG_WORKTHREAD_SEND_CONNECTUSBRESULT: {
                    int result = msg.arg1;
                    Toast.makeText(
                            MainApplication.getInstance(),
                            (result == 1) ? "连接成功"
                                    : "连接失败", Toast.LENGTH_SHORT).show();
                    if (1 == result) {
                        connectSuccess = true;
                    } else {
                        connectSuccess = false;
                    }
                    mActivity.probe();
                    break;
                }

            }
        }

    }

    private void startICSeriaport() {
        try {
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
//                mReadThread = new SingleSearchResultActivity.ReadThread();
//                mReadThread.setDaemon(true);
//                mReadThread.start();
            ICHandler.post(mRunnable);

        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

    private void initTitles() {
        Log.d("hailong20", "initTitles ");
        View right_container = findViewById(R.id.right_container);
        right_container.setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.right_icon)).setImageResource(R.drawable.print);
        ((TextView) findViewById(R.id.right_label)).setText("打印成绩");
        right_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInfos.size() <= 0) {
                    Toast.makeText(SingleSearchResultActivity.this, "没有学生信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    probe();
                }
            }
        });
        if (userInfos != null && userInfos.size() == 1) {
            View right_container1 = findViewById(R.id.right_container_1);
            right_container1.setVisibility(View.VISIBLE);
            ((ImageView) findViewById(R.id.right_icon_1)).setImageResource(R.drawable.edit);
            ((TextView) findViewById(R.id.right_label_1)).setText("写入IC卡");

            right_container1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    writeUserInfo = userInfos.get(0);
                    isPinLoad = writeCodeSexOk = writeDataOk = false;
                    startICSeriaport();
                    loadPin();
                }
            });
        }
        int resId = getResources().getColor(R.color.excel_attribute_color);
        TextView avatar_label = (TextView) findViewById(R.id.avatar_label);
        avatar_label.setText("头像");
        avatar_label.setTextColor(resId);
        avatar_label.setVisibility(View.VISIBLE);

        TextView score = (TextView) findViewById(R.id.score);

        //水平
        TextView analysis = (TextView) findViewById(R.id.analysis);
        if (!AppUtils.isWeight()) {//不是身高体重的项目
            analysis.setTextColor(resId);
            analysis.setVisibility(View.VISIBLE);
            score.setText(StudentColomn.SCORE);
            score.setTextColor(resId);
            score.setVisibility(View.VISIBLE);
        } else {
            score.setVisibility(View.GONE);
            TextView weight = (TextView) findViewById(R.id.weight);
            weight.setTextColor(resId);
            weight.setVisibility(View.VISIBLE);

            TextView height = (TextView) findViewById(R.id.height);
            height.setTextColor(resId);
            height.setVisibility(View.VISIBLE);

        }
        for (String col : columns) {
            if (StudentColomn.NAME.equals(col)) {
                TextView name = (TextView) findViewById(R.id.name);
                name.setText(StudentColomn.NAME);
                name.setTextColor(resId);
                name.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CODE.equals(col)) {
                TextView code = (TextView) findViewById(R.id.code);
                code.setText(StudentColomn.CODE);
                code.setTextColor(resId);
                code.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.SEX.equals(col)) {
                TextView sex = (TextView) findViewById(R.id.sex);
                sex.setText(StudentColomn.SEX);
                sex.setTextColor(resId);
                sex.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CLASSES.equals(col)) {
                TextView classes = (TextView) findViewById(R.id.classes);
                classes.setText(StudentColomn.CLASSES);
                classes.setTextColor(resId);
                classes.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.GRADE.equals(col)) {
                TextView grade = (TextView) findViewById(R.id.grade);
                grade.setText(StudentColomn.GRADE);
                grade.setTextColor(resId);
                grade.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ID_CARD.equals(col)) {
                TextView idcard = (TextView) findViewById(R.id.idcard);
                idcard.setText(StudentColomn.ID_CARD);
                idcard.setTextColor(resId);
                idcard.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.RACE.equals(col)) {
                TextView race = (TextView) findViewById(R.id.race);
                race.setText(StudentColomn.RACE);
                race.setTextColor(resId);
                race.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JIGUAN.equals(col)) {
                TextView jiguan = (TextView) findViewById(R.id.jiguan);
                jiguan.setText(StudentColomn.JIGUAN);
                jiguan.setTextColor(resId);
                jiguan.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ADDRESS.equals(col)) {
                TextView address = (TextView) findViewById(R.id.address);
                address.setText(StudentColomn.ADDRESS);
                address.setTextColor(resId);
                address.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JOB.equals(col)) {
                TextView job = (TextView) findViewById(R.id.job);
                job.setText(StudentColomn.JOB);
                job.setTextColor(resId);
                job.setVisibility(View.VISIBLE);
            }
        }
    }

//    //下载密码
//    public void loadPin(View view) {
//        try {
//            if (mOutputStream != null) {
//                mOutputStream.write(pinBytes);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void checkIfExist(DataViewHolder viewHolder) {
        if (!AppUtils.isWeight()) {
            viewHolder.analysis.setVisibility(View.VISIBLE);
            viewHolder.score.setVisibility(View.VISIBLE);
        } else {
            viewHolder.weight.setVisibility(View.VISIBLE);
            viewHolder.height.setVisibility(View.VISIBLE);
        }
        for (String col : columns) {
            if (StudentColomn.NAME.equals(col)) {
                viewHolder.name.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CODE.equals(col)) {
                viewHolder.code.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.SEX.equals(col)) {
                viewHolder.sex.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CLASSES.equals(col)) {
                viewHolder.classes.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.GRADE.equals(col)) {
                viewHolder.grade.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ID_CARD.equals(col)) {
                viewHolder.idcard.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.RACE.equals(col)) {
                viewHolder.race.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JIGUAN.equals(col)) {
                viewHolder.jiguan.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ADDRESS.equals(col)) {
                viewHolder.adress.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JOB.equals(col)) {
                viewHolder.job.setVisibility(View.VISIBLE);
            }
        }


    }

    class DataAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;
        ArrayList<UserInfo> infos = new ArrayList<>();

        public DataAdapter(Context mContext, ArrayList<UserInfo> infos) {
            this.mContext = mContext;
            inflater = LayoutInflater.from(mContext);
            this.infos = infos;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DataViewHolder viewHolder = new DataViewHolder();
            final UserInfo studentInfo = infos.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.student_info_avatar, null);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.avatar_label = (TextView) convertView.findViewById(R.id.avatar_label);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.code = (TextView) convertView.findViewById(R.id.code);
                viewHolder.sex = (TextView) convertView.findViewById(R.id.sex);
                viewHolder.classes = (TextView) convertView.findViewById(R.id.classes);
                viewHolder.grade = (TextView) convertView.findViewById(R.id.grade);
                viewHolder.score = (TextView) convertView.findViewById(R.id.score);
                viewHolder.analysis = (TextView) convertView.findViewById(R.id.analysis);
                viewHolder.weight = (TextView) convertView.findViewById(R.id.weight);
                viewHolder.height = (TextView) convertView.findViewById(R.id.height);
                viewHolder.idcard = (TextView) convertView.findViewById(R.id.idcard);
                viewHolder.race = (TextView) convertView.findViewById(R.id.race);
                viewHolder.jiguan = (TextView) convertView.findViewById(R.id.jiguan);
                viewHolder.adress = (TextView) convertView.findViewById(R.id.address);
                viewHolder.job = (TextView) convertView.findViewById(R.id.job);
                viewHolder.writeic = (TextView) convertView.findViewById(R.id.writeic);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DataViewHolder) convertView.getTag();
            }
            checkIfExist(viewHolder);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AvatarDisplayDialog displayDialog = new AvatarDisplayDialog(SingleSearchResultActivity.this);
//                    displayDialog.setImageBitmap(studentInfo.headImage);
//                    displayDialog.show();
                    DataItemDisplayDialog displayDialog = new DataItemDisplayDialog(SingleSearchResultActivity.this, studentInfo);
                    displayDialog.show();
                }
            });
            viewHolder.writeic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startICSeriaport();
                    writeUserInfo = studentInfo;
                    isPinLoad = writeCodeSexOk = writeDataOk = false;
                    loadPin();
                }
            });
            viewHolder.avatar.setVisibility(View.VISIBLE);
            viewHolder.avatar_label.setVisibility(View.GONE);
            try {
                if (StringUtils.isEmpty(studentInfo.avater_label)) {
                    viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
                } else {
                    viewHolder.avatar.setImageBitmap(BitmapFactory.decodeFile(studentInfo.avater_label));
                }
            } catch (Exception e) {
                viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
            }
            viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.sex.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.classes.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.grade.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.name.setText(studentInfo.name);
            viewHolder.code.setText(studentInfo.code);
            viewHolder.sex.setText(studentInfo.sex);
            viewHolder.classes.setText(studentInfo.classes);
            viewHolder.grade.setText(studentInfo.grade);
//            String[] scores = UserModel.getScore(studentInfo);
//            if (scores != null && scores.length > 0) {
//                if (MainApplication.per.equals("ml")) {
//                    viewHolder.score.setText(AppUtils.getResult(studentInfo.feihuoliang));
//                }
//                if (viewHolder.analysis.getVisibility() == View.VISIBLE) {
//                    viewHolder.analysis.setText(AppUtils.getRule(SingleSearchResultActivity.this, scores[0], "男".equals(studentInfo.sex)));
//                }
//            }

            String[] scores = UserModel.getScore(studentInfo);
            if (AppUtils.isWeight()) {
                if (!StringUtils.isEmpty(scores[0])) {
                    viewHolder.weight.setText(scores[0] + "kg");
                } else {
                    viewHolder.weight.setText("---kg");
                }
                if (!StringUtils.isEmpty(scores[1])) {
                    viewHolder.height.setText(scores[1] + "cm");
                } else {
                    viewHolder.height.setText("---cm");
                }
            } else {
                viewHolder.score.setText(AppUtils.getResult(scores[0]));
                viewHolder.analysis.setText(AppUtils.getRule(SingleSearchResultActivity.this, scores[0], "男".equals(studentInfo.sex)));
            }
            viewHolder.idcard.setText(studentInfo.id_card);
            viewHolder.race.setText(studentInfo.race);
            viewHolder.jiguan.setText(studentInfo.jiguan);
            viewHolder.adress.setText(studentInfo.address);
            viewHolder.job.setText(studentInfo.job);

            return convertView;
        }
    }

    class DataViewHolder {
        ImageView avatar;
        TextView avatar_label;//头像
        TextView name;//姓名
        TextView code;//学号
        TextView sex;//性别
        TextView classes;//年级
        TextView grade;//班级
        TextView score;//成绩
        TextView analysis;//评级
        TextView weight;//体重
        TextView height;//身高
        TextView idcard;//身份证
        TextView race;//民族
        TextView jiguan;//籍贯
        TextView adress;//居住地址
        TextView job;//职务
        TextView writeic;//写IC卡
    }

}
