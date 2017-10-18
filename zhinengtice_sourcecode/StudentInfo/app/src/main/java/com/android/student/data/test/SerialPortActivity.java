/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.android.student.data.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.ToastUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import static com.android.student.data.test.utils.AppUtils.bytesToHexString;

public abstract class SerialPortActivity extends Activity {
    //读取IC卡
    //01 03无卡退出
    //01 02未知卡类型
    //01 01异常出错
    //读卡错误
    public static String error_nocard = "0103";
    public static String error_unkown = "0102";
    public static String error_exception = "0101";

    //姓名（8个Byte），如果姓名是三个的，第四Byte用0X20 0X20代替，
    //年级编号1-16，例如 0x01—小学一年级，0x02—小学二年级，。。。。。依次为到0x1616-大学四年级
    //班号（2个），用BCD代替，例如0X12，0X34，代表1234，如果低于两位，前面用00代替。
    //班级（1个）,就是一班,二班这样，为0x01,0x02…0x9,0x10，0x11…0x99

    //返回数据
    //01 03 无卡退出
    //01 02 未知卡类型
    //01 01 异常出错
    //01 ff 无效命令字
    public static byte[] reponse_success = new byte[]{0X01, 0X00};
    public static byte[] reponse_exception = new byte[]{0X01, 0X01};
    public static byte[] reponse_unkowncard = new byte[]{0X01, 0X02};
    public static byte[] reponse_nocard = new byte[]{0X01, 0X03};
    public static byte[] reponse_invalid = new byte[]{0X01, (byte) 0XFF};

    //写
    // (02 为长度,0B 为命令字, 测试凤鸣器,0F 凤鸣器响的时间)
    public static byte[] alarm_write = new byte[]{0X02, 0X0B, 0X0F};

    //读
    //  07 12 26 01 60 00 01 01
    public static byte[] code_read = new byte[]{0X07, 0X12, 0X26, 0X01, 0X60, 0X00, 0X01, 0X01};
    public static byte[] data_read = new byte[]{0X07, 0X12, 0X26, 0X01, 0X60, 0X00, 0X02, 0X02};
    //public static byte[] data_read = new byte[]{0X02, 0X01, 0X01};
    //09 06 60 00 FF FF FF FF FF FF (06 为命令字, 60 为密码 A(61 为密码 B), 00为扇区号, 12 个 F 为密码)
    public static byte[] pin_read = new byte[]{0X09, 0X06, 0X60, 0X00, (byte) 0XFF, (byte) 0XFF, (byte) 0XFF, (byte) 0XFF, (byte) 0XFF, (byte) 0XFF};
    public static byte[] height_read = new byte[]{0X01, 0X00};//第四块(扇区1，第0块）
    public static byte[] weight_read = new byte[]{0X01, 0X01};//第四块(扇区1，第1块）
    public static byte[] feihuoliang_read = new byte[]{0X01, 0X02};//第四块(扇区1，第1块）

    protected MediaPlayer mp;
    protected MainApplication mMainApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    protected boolean bobao = true;
    protected boolean tip = true;
    protected int volume = -1;
    //    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
//    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    protected Handler mHandler = new Handler();
    protected Runnable ReadTimerRunnable = new Runnable() {
        @Override
        public void run() {
            int size;
            try {
                byte[] buffer = new byte[32];
                if (mInputStream == null) return;
                size = mInputStream.read(buffer);
                if (size > 0 /*&& !pause*/) {
                    onDataReceived(buffer, size);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            mHandler.postDelayed(ReadTimerRunnable, 100);

        }
    };
    private volatile boolean stop = false;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!interrupted() && !stop) {
                int size;
                try {
                    byte[] buffer = new byte[32];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0 /*&& !pause*/) {
                        onDataReceived(buffer, size);
                    }
                    sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    boolean pause = false;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        bobao = sharedPreferences.getBoolean("bobao", bobao);
        tip = sharedPreferences.getBoolean("tip", tip);
        pause = false;
    }


    protected void ensureMp(String fileName, boolean tip) {
        if (tip) {
            if (mp != null) {
                mp.release();
                mp = null;
            }
            if (mp == null) {
                mp = new MediaPlayer();
            }
            try {
                AssetFileDescriptor fileDescriptor = getAssets().openFd(fileName);
                mp.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                if (mp != null) {
                    mp.prepare();
                    mp.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void ensureMp(String fileName) {
        if (bobao) {
            ensureMp(fileName, true);
        }
    }

    //FFFFFFFFFF12345678909ad86653677e2020 03 0012 12 0001
    //信息就是：学号1234567890 高晓松 小学三年级 0012  12班 序号0001
    //获取学号
    public String getICCode(byte[] bytes, int endPos) {
        if (bytes == null) {
            return "";
        }
        //将bytes拿出来
        String byteStr = bytesToHexString(bytes);
        //从头开始找到最后一个F记录位置
        byteStr = byteStr.substring(0, 24);
        int pos = byteStr.lastIndexOf('F');
        Log.d("hailong22", " pos " + pos + " byteStr " + byteStr + " endPos " + endPos);
        String subStr = byteStr.substring(pos + 1, endPos);
        return subStr;
    }

    //获取姓名
    public String getICName(byte[] bytes) {
        try {
            return new String(bytes, "gbk");
        } catch (Exception e) {

        }
        return "";
    }


    //得到年级
    public String getICClass(byte bytes) {
        String claz = "";
        switch (bytes) {
            case 0X01://小学一年级
                claz = "一年级";
                break;
            case 0X02://小学二年级
                claz = "二年级";
                break;
            case 0X03://小学三年级
                claz = "三年级";
                break;
            case 0X04://小学四年级
                claz = "四年级";
                break;
            case 0X05://小学五年级
                claz = "五年级";
                break;
            case 0X06://小学六年级
                claz = "六年级";
                break;

            case 0X07://初中一年级
                claz = "初一";
                break;
            case 0X08://初中二年级
                claz = "初二";
                break;
            case 0X09://初中三年级
                claz = "初三";
                break;

            case 0X10://高中一年级
                claz = "高一";
                break;
            case 0X11://高中二年级
                claz = "高二";
                break;
            case 0X12://高中三年级
                claz = "高三";
                break;

            case 0X13://大一
                claz = "大一";
                break;
            case 0X14://大二
                claz = "大二";
                break;
            case 0X15://大三
                claz = "大三";
                break;
            case 0X16://大四
                claz = "大四";
                break;
        }
        return claz;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        if (begin + count > src.length) {
            count = src.length - begin;
        }
        for (int i = begin; i < begin + count; i++)
            bs[i - begin] = src[i];
        return bs;
    }

    //得到班级
    public int getICGrade(byte[] bytes) {
        String grade = bytesToHexString(bytes);
        grade = grade.replace("F", "");
        int grades = 0;
        try {

        } catch (Exception e) {
            grades = Integer.parseInt(grade);
        }
        return grades;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mp != null) {
                mp.reset();
                mp.release();
                mp = null;
            }
        } catch (Exception e) {

        }
        pause = true;
    }

    protected void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(SerialPortActivity.this, msg);
            }
        });

    }


    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
             //  SerialPortActivity.this.finish();
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainApplication = (MainApplication) getApplication();
        resume();
        mp = new MediaPlayer();
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        volume = sharedPreferences.getInt("volume", -1);
        if (volume == -1) {
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
        }
    }

    protected void write(String data) {
        //data转成0XAA,0XBB的样式
        byte[] bytes = AppUtils.stringToBytes(data);
        try {
            if (mOutputStream != null && bytes != null) {
                mOutputStream.write(bytes);
            }
        } catch (Exception e) {

        }
//        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
//        try {
//            Log.d("hailong13", "CC data length is " + data.length());
//            if (data != null) {
//                if (data.length() >= 4) {
//                    byteArrayOS.write(Integer.decode("0X" + data.substring(0, 2)).byteValue());
//                    byteArrayOS.write(Integer.decode("0X" + data.substring(2, 4)).byteValue());
//                }
//
//
//                if (mOutputStream != null && byteArrayOS != null) {
//                    mOutputStream.write(byteArrayOS.toByteArray());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    protected boolean resume() {
        boolean ok = true;
        try {
            mSerialPort = mMainApplication.getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
            mReadThread = new ReadThread();
            mReadThread.start();
        } catch (SecurityException e) {
            ok = false;
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            ok = false;
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            ok = false;
            DisplayError(R.string.error_configuration);
        }

        return  ok;
    }

    protected void pause() {
        if (mReadThread != null)
            mReadThread.interrupt();
        mMainApplication.closeSerialPort();
        mSerialPort = null;
    }

    @Override
    protected void onDestroy() {
//        mHandler.removeCallbacks(ReadTimerRunnable);
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        stop = true;
        mMainApplication.closeSerialPort();
        mSerialPort = null;
        super.onDestroy();
    }
}
