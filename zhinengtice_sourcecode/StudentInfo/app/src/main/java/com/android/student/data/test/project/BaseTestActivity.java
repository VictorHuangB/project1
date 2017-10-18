package com.android.student.data.test.project;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.student.data.test.SerialPortActivity;
import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.IOException;

/**
 * 所有测试页面的通用逻辑
 * Created by hailong on 2016/11/19.
 */
public class BaseTestActivity extends SerialPortActivity {
    protected BaseHandler mHander = new BaseHandler();
    boolean deviceReady = false;
    int Max_Count = 7;
    int send_count = 0;
    int fail_count = 0;
    boolean connectSuccess = false;
    CountDownTimer countDownTimer;
    private static final int Max_Time = 2100;//三次提交结果
    private static final int Hold_Time = 300;//300ms发送一次

    private static final String Send_Connecting = "0113";//主机开机,向外设发送状态查询
    private static final String Send_Confirm = "0115";//主机确认外设状态
    private static final String Send_Biaoding = "0118";//进入标定

    private static final String RC_Connecting = "C3";//正在连接
    private static final String RC_Not_Ready = "D0";//没准备好
    private static final String RC_Connect_Success = "D1";//连接成功
    private static final String RC_Connect_Fail = "001";//连接失败

    protected class BaseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String obj = (String) msg.obj;
            Log.d("hailong14", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
                case RC_Connecting:
                    fail_count++;
                    if (send_count <= Max_Count) {
                        write(Send_Confirm);
                        send_count++;
                    }
                  //  handleConnecting();
                    break;
                case RC_Not_Ready:
                    fail_count++;
                   // handleConnecting();
                    break;
                case RC_Connect_Fail:
                    fail_count = 0;
                    //if (deviceConnectDialog != null) {
                   //     deviceConnectDialog.dismiss();
                  //  }
                    showReConnectDialog("连接失败，请确认外设是否正常");
                    break;
                case RC_Connect_Success:
                    fail_count = 0;
                    deviceReady = true;
                //    if (deviceConnectDialog != null) {
                //        deviceConnectDialog.dismiss();
                 //   }
                    toast("连接成功");
                    write(Send_Biaoding);
                    break;
            }
        }
    }


    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
//                String result = new String(buffer, 0, size);//AscII
//                String asc = AppUtils.asciiToString(result);
                String result = AppUtils.bytesToHexString(buffer);
                String code = result.replace("\n","").replace("\r","").substring(0,2);
//                try {
//                    for (int i = 0; i < result.length(); i += 2) {
//                        String spt = result.substring(i, i + 2);
//                        code += AppUtils.hexToStr(spt);
//                        Log.d("hailong15", "Base onDataReceived spt is " + spt);
//                    }
//                } catch (Exception e) {
//                }
                Log.d("hailong15", " code is " + code);
                if (!StringUtils.isEmpty(code)) {
                    receiveData(code);
                } else {
                    Toast.makeText(BaseTestActivity.this, "连接设备失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    protected void receiveData(String code) {
        Message message = new Message();
        message.obj = code;
        mHander.sendMessage(message);
    }

    protected boolean isDeviceReady() {
        return deviceReady;
    }

    protected void onTestDeviceBefore() {
        //1，发送0113，hold 300ms
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(Max_Time, Hold_Time) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d("hailong13", " onTick ");
                    write(Send_Connecting);
                }

                @Override
                public void onFinish() {
                    Log.d("hailong14", " onFinish ");
                    if (!connectSuccess) {
                        if(mOutputStream==null){
                            toast("数据无法通信，请保证设备有读写权限");
                        }else {
                            toast("设备连接失败");
                        }
//                        if (deviceConnectDialog != null) {
//                            deviceConnectDialog.dismiss();
//                        }
                    }
                }

            };
        }
        countDownTimer.start();
    }

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

//    protected void handleConnecting() {
//        if (deviceConnectDialog != null) {
//            deviceConnectDialog.setMessages("正在连接");
//            if (fail_count == Max_Count + 1) {
//                fail_count = 0;
//                countDownTimer.cancel();
//                deviceConnectDialog.dismiss();
//                showReConnectDialog("连接失败，请确认外设是否正常");
//            }
//        }
//    }


    protected void write(String data) {
        try {
            String target = "";
            Log.d("hailong13", "CC data length is " + data.length());
            for (int i = 0; i < data.length(); i++) {
                target += AppUtils.asciiToIntegerStr(String.valueOf(data.charAt(i)));
            }
//            Log.d("hailong12","AA data is "+data);
//            data = AppUtils.asciiToIntegerStr(data);
            Log.d("hailong12", "BB data is " + target + " mOutputStream " + mOutputStream);
            if (mOutputStream != null) {
                mOutputStream.write(target.getBytes());
                mOutputStream.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void showReConnectDialog(String msg) {
        final AppDialog appDialog = new AppDialog(this, "知道了", "再试一次", true);
        appDialog.setupContentView(msg);
        appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
            @Override
            public void operationLeft() {

                appDialog.dismiss();
            }

            @Override
            public void operationRight() {
                //testDevice();
                appDialog.dismiss();
            }

        });
        appDialog.show();

    }

}
