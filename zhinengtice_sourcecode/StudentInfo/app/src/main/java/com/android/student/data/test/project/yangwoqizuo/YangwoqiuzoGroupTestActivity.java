package com.android.student.data.test.project.yangwoqizuo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.SerialPort;
import com.android.student.data.test.SerialPortActivity;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.dialog.IcSameUserDialog;
import com.android.student.data.test.dialog.TimeCounterDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;


/**
 * 仰卧起坐多人测试页面
 * Created by hailong on 2017/2/14 0014.
 */
public class YangwoqiuzoGroupTestActivity extends SerialPortActivity implements View.OnClickListener {
    //IC卡读取
    /************
     * 读卡开始
     **************************/
    boolean icPart1, icPart2, icPart3, icPart4;//标记是那个设备在刷卡
    private String icCode = "";//学号 10byte
    private String icSex = ""; //性别

    private String icName = "";//名字 8byte
    private String icGradeCode = "";//年级编号 2byte
    private String icClas = "";//年级 1byte
    private String icGrade = "";//班级  1byte
    private String icOrderCode = "";
    //IC读取
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    int slideSeconds1 = 60;
    int slideSeconds2 = 60;
    int slideSeconds3 = 60;
    int slideSeconds4 = 60;

    CountDownTimer timerCounter1;//倒计时
    CountDownTimer timerCounter2;//倒计时
    CountDownTimer timerCounter3;//倒计时
    CountDownTimer timerCounter4;//倒计时

    private SerialPort mSerialPort = null;
    boolean isPinLoad = false;//密码下载成功
    boolean isCodeOk = false;//IC卡学号读取完毕
    boolean isDataOk = false;//IC卡数据读取完毕
    byte[] pinBytes = new byte[]{
            0X09, 0X06, 0X60, 0X00,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF//(06 为命令字, 60 为密码 A(61 为密码 B), 00为扇区号, 12 个 F 为密码)

    };

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

    private void startICSeriaport() {
        try {
//            if (mSerialPort == null) {
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
//                mReadThread_ic = new YangwoqiuzoGroupTestActivity.ReadThread_IC();
//                mReadThread_ic.setDaemon(true);
//            }
//            mReadThread_ic.start();
            ICHandler.post(mRunnable);
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }


    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("错误");
        b.setMessage(resourceId);
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
//                YangwoqiuzoGroupTestActivity.this.finish();
            }
        });
        b.show();
    }

    //下载密码
    public void loadPin() {
        try {
            if (mOutputStream != null) {
                isPinLoad = true;
                mOutputStream.write(pinBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkICInfo() {
        //校验code
        if (StringUtils.isEmpty(icCode)) {
            return false;
        }
        if (StringUtils.isEmpty(icName)) {
            return false;
        }
        if (StringUtils.isEmpty(icClas)) {
            return false;
        }
        if (StringUtils.isEmpty(icGrade)) {
            return false;
        }
        return true;
    }

    private void showUserInfo1(UserInfo info) {
        userInfo1 = info;
        result_settings_1.setVisibility(View.GONE);
        result_container_1.setVisibility(View.VISIBLE);
        try {
            if (StringUtils.isEmpty(userInfo1.avater_label)) {
                test_avatar_1.setImageBitmap(MainApplication.defaultBitmap);
            } else {
                test_avatar_1.setImageBitmap(BitmapFactory.decodeFile(userInfo1.avater_label));
            }
        } catch (Exception e) {
            test_avatar_1.setImageBitmap(MainApplication.defaultBitmap);
        }
        result_name_1.setText(userInfo1.name);
        result_sex_1.setText(AppUtils.getSex(userInfo1));
        result_class_1.setText(userInfo1.classes + userInfo1.grade);
        result_code_1.setText(userInfo1.code);
    }

    private void showSameUserDialog1(final UserInfo oldUserInfo, final UserInfo newUserInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IcSameUserDialog selectDialog = new IcSameUserDialog(YangwoqiuzoGroupTestActivity.this, oldUserInfo, newUserInfo);
                selectDialog.setDialogClickListener(new IcSameUserDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        //选择本地的
                        //显示学生信息
                        showUserInfo1(oldUserInfo);
                    }

                    @Override
                    public void operationRight() {
                        //选择IC卡的
                        //数据库更新
                        coverUserInfo(oldUserInfo, newUserInfo);
                        //显示学生信息
                        showUserInfo1(newUserInfo);
                    }
                });
                selectDialog.show();
            }
        });

    }

    //开始读卡
    private void startIcDetect() {
        isCodeOk = isDataOk = isPinLoad = false;
        startICSeriaport();
        loadPin();
    }

    private void applyIcInfo1(UserInfo info) {
        //是否有两个同样的学号
        if (UserModel.checkSameUser(this, icCode)) {
            //拿到本地相同code的信息
            ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(this, icCode);
            if (userInfos != null && userInfos.size() > 0) {
                toast("学号重复，请选择");
                showSameUserDialog1(userInfos.get(0), info);
            }
            return;
        } else {
            showUserInfo1(info);
            UserModel.addItemToDatabase(this, info);
        }
    }

    private void showUserInfo2(UserInfo info) {
        userInfo2 = info;
        result_settings_2.setVisibility(View.GONE);
        result_container_2.setVisibility(View.VISIBLE);
        try {
            if (StringUtils.isEmpty(userInfo2.avater_label)) {
                test_avatar_2.setImageBitmap(MainApplication.defaultBitmap);
            } else {
                test_avatar_2.setImageBitmap(BitmapFactory.decodeFile(userInfo2.avater_label));
            }
        } catch (Exception e) {
            test_avatar_2.setImageBitmap(MainApplication.defaultBitmap);
        }
        result_name_2.setText(userInfo2.name);
        result_sex_2.setText(AppUtils.getSex(userInfo2));
        result_class_2.setText(userInfo2.classes + userInfo2.grade);
        result_code_2.setText(userInfo2.code);
    }

    private void showSameUserDialog2(final UserInfo oldUserInfo, final UserInfo newUserInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IcSameUserDialog selectDialog = new IcSameUserDialog(YangwoqiuzoGroupTestActivity.this, oldUserInfo, newUserInfo);
                selectDialog.setDialogClickListener(new IcSameUserDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        //选择本地的
                        //显示学生信息
                        showUserInfo2(oldUserInfo);
                    }

                    @Override
                    public void operationRight() {
                        //选择IC卡的
                        //数据库更新
                        coverUserInfo(oldUserInfo, newUserInfo);
                        //显示学生信息
                        showUserInfo2(newUserInfo);
                    }
                });
                selectDialog.show();
            }
        });

    }

    private void applyIcInfo2(UserInfo info) {
        //是否有两个同样的学号
        if (UserModel.checkSameUser(this, icCode)) {
            //拿到本地相同code的信息
            ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(this, icCode);
            if (userInfos != null && userInfos.size() > 0) {
                toast("学号重复，请选择");
                showSameUserDialog2(userInfos.get(0), info);
            }
            return;
        } else {
            showUserInfo2(info);
            UserModel.addItemToDatabase(this, info);
        }
    }

    private void showUserInfo3(UserInfo info) {
        userInfo3 = info;
        result_settings_3.setVisibility(View.GONE);
        result_container_3.setVisibility(View.VISIBLE);
        try {
            if (StringUtils.isEmpty(userInfo3.avater_label)) {
                test_avatar_3.setImageBitmap(MainApplication.defaultBitmap);
            } else {
                test_avatar_3.setImageBitmap(BitmapFactory.decodeFile(userInfo3.avater_label));
            }
        } catch (Exception e) {
            test_avatar_3.setImageBitmap(MainApplication.defaultBitmap);
        }
        result_name_3.setText(userInfo3.name);
        result_sex_3.setText(AppUtils.getSex(userInfo3));
        result_class_3.setText(userInfo3.classes + userInfo3.grade);
        result_code_3.setText(userInfo3.code);
    }

    private void showSameUserDialog3(final UserInfo oldUserInfo, final UserInfo newUserInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IcSameUserDialog selectDialog = new IcSameUserDialog(YangwoqiuzoGroupTestActivity.this, oldUserInfo, newUserInfo);
                selectDialog.setDialogClickListener(new IcSameUserDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        //选择本地的
                        //显示学生信息
                        showUserInfo3(oldUserInfo);
                    }

                    @Override
                    public void operationRight() {
                        //选择IC卡的
                        //数据库更新
                        coverUserInfo(oldUserInfo, newUserInfo);
                        //显示学生信息
                        showUserInfo3(newUserInfo);
                    }
                });
                selectDialog.show();
            }
        });

    }

    private void applyIcInfo3(UserInfo info) {
        //是否有两个同样的学号
        if (UserModel.checkSameUser(this, icCode)) {
            //拿到本地相同code的信息
            ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(this, icCode);
            if (userInfos != null && userInfos.size() > 0) {
                toast("学号重复，请选择");
                showSameUserDialog3(userInfos.get(0), info);
            }
            return;
        } else {
            showUserInfo3(info);
            UserModel.addItemToDatabase(this, info);
        }
    }

    private void showUserInfo4(UserInfo info) {
        userInfo4 = info;
        result_settings_4.setVisibility(View.GONE);
        result_container_4.setVisibility(View.VISIBLE);
        try {
            if (StringUtils.isEmpty(userInfo4.avater_label)) {
                test_avatar_4.setImageBitmap(MainApplication.defaultBitmap);
            } else {
                test_avatar_4.setImageBitmap(BitmapFactory.decodeFile(userInfo4.avater_label));
            }
        } catch (Exception e) {
            test_avatar_4.setImageBitmap(MainApplication.defaultBitmap);
        }
        result_name_4.setText(userInfo4.name);
        result_sex_4.setText(AppUtils.getSex(userInfo4));
        result_class_4.setText(userInfo4.classes + userInfo4.grade);
        result_code_4.setText(userInfo4.code);
    }

    private void showSameUserDialog4(final UserInfo oldUserInfo, final UserInfo newUserInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IcSameUserDialog selectDialog = new IcSameUserDialog(YangwoqiuzoGroupTestActivity.this, oldUserInfo, newUserInfo);
                selectDialog.setDialogClickListener(new IcSameUserDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        //选择本地的
                        //显示学生信息
                        showUserInfo4(oldUserInfo);
                    }

                    @Override
                    public void operationRight() {
                        //选择IC卡的
                        //数据库更新
                        coverUserInfo(oldUserInfo, newUserInfo);
                        //显示学生信息
                        showUserInfo4(newUserInfo);
                    }
                });
                selectDialog.show();
            }
        });

    }

    private void applyIcInfo4(UserInfo info) {
        //是否有两个同样的学号
        if (UserModel.checkSameUser(this, icCode)) {
            //拿到本地相同code的信息
            ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(this, icCode);
            if (userInfos != null && userInfos.size() > 0) {
                toast("学号重复，请选择");
                showSameUserDialog4(userInfos.get(0), info);
            }
            return;
        } else {
            showUserInfo4(info);
            UserModel.addItemToDatabase(this, info);
        }
    }

    //覆盖信息
    private void coverUserInfo(UserInfo oldUserInfo, UserInfo newUserInfo) {
        UserModel.exchangeUserInfo(this, oldUserInfo, newUserInfo);
    }

    //解决线程不销毁的bug
    private Handler ICHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        public void run() {
            int size = 0;
            try {
                byte[] buffer = new byte[32];
                if (mInputStream != null) {
                    size = mInputStream.read(buffer);
                    Log.d("hailong30", " mInputStream " + mInputStream + " size " + size);
                    if (size > 0) {
                        onDataReceived_IC(buffer, size);
                    }
                }
            } catch (IOException e) {
                Log.d("hailong30", " IOException " + e.getMessage());
                e.printStackTrace();
            }

        }

    };
    Runnable resultSuccessRunnable1 = new Runnable() {
        @Override
        public void run() {
            Log.d("hailong88", "resultSuccessRunnable1 finish ");
            //测试完毕
            isTesting1 = false;
            testOk1 = true;
            if (timerCounter1 != null) {
                timerCounter1.cancel();
            }
            if (resultCountDownTimer1 != null) {
                resultCountDownTimer1.cancel();
            }
            try {
                device_result_1.setText(String.valueOf(Integer.parseInt(currentResult_1)));
            } catch (Exception e) {

            }

            dismissStatus_1();
            time_counter_1.setText(String.valueOf(currentSeconds_1));
        }
    };
    Runnable resultSuccessRunnable2 = new Runnable() {
        @Override
        public void run() {
            //测试完毕
            isTesting2 = false;
            testOk2 = true;
            if (timerCounter2 != null) {
                timerCounter2.cancel();
            }
            if (resultCountDownTimer2 != null) {
                resultCountDownTimer2.cancel();
            }
            try {
                device_result_2.setText(String.valueOf(Integer.parseInt(currentResult_2)));
            } catch (Exception e) {

            }
            dismissStatus_2();
            time_counter_2.setText(String.valueOf(currentSeconds_2));
        }
    };

    Runnable resultSuccessRunnable3 = new Runnable() {
        @Override
        public void run() {
            //测试完毕
            isTesting3 = false;
            testOk3 = true;
            if (timerCounter3 != null) {
                timerCounter3.cancel();
            }
            if (resultCountDownTimer3 != null) {
                resultCountDownTimer3.cancel();
            }
            try {
                device_result_3.setText(String.valueOf(Integer.parseInt(currentResult_3)));
            } catch (Exception e) {

            }
            dismissStatus_3();
            menu_start_label_3.setVisibility(View.GONE);
            time_counter_3.setText(String.valueOf(currentSeconds_3));
        }
    };

    Runnable resultSuccessRunnable4 = new Runnable() {
        @Override
        public void run() {
            //测试完毕
            isTesting4 = false;
            testOk4 = true;
            if (timerCounter4 != null) {
                timerCounter4.cancel();
            }
            if (resultCountDownTimer4 != null) {
                resultCountDownTimer4.cancel();
            }
            try {
                device_result_4.setText(String.valueOf(Integer.parseInt(currentResult_4)));
            } catch (Exception e) {

            }
            dismissStatus_4();
            ;
            menu_start_label_4.setVisibility(View.GONE);
            time_counter_4.setText(String.valueOf(currentSeconds_4));
        }
    };


    CountDownTimer displayStatusTimer_1 = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus_1();
        }
    };
    CountDownTimer devIdleTimer_1 = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            deviceOk1 = false;
            displayStatus_1("请打开外设");
            displayStatusTimer_2.cancel();
            displayStatusTimer_2.start();
        }
    };
    CountDownTimer displayStatusTimer_2 = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus_2();
        }
    };
    CountDownTimer devIdleTimer_2 = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            displayStatus_2("请打开外设");
            displayStatusTimer_2.cancel();
            displayStatusTimer_2.start();
        }
    };
    CountDownTimer displayStatusTimer_3 = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus_3();
        }
    };
    CountDownTimer devIdleTimer_3 = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            displayStatus_3("请打开外设");
            displayStatusTimer_3.cancel();
            displayStatusTimer_3.start();
        }
    };
    CountDownTimer displayStatusTimer_4 = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus_4();
        }
    };

    CountDownTimer devIdleTimer_4 = new CountDownTimer(5000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            displayStatus_4("请打开外设");
            displayStatusTimer_4.cancel();
            displayStatusTimer_4.start();
        }
    };

    CountDownTimer ShutDownTimer_1 = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTesting1 = false;
            testOk1 = false;
            needTest1 = true;
            clickReceive1 = false;
            displayStatus_1("请打开外设");
            displayStatusTimer_1.cancel();
            displayStatusTimer_1.start();
        }
    };
    CountDownTimer ShutDownTimer_2 = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTesting1 = false;
            testOk1 = false;
            needTest1 = true;
            clickReceive1 = false;
            displayStatus_1("请打开外设");
            displayStatusTimer_1.cancel();
            displayStatusTimer_1.start();
        }
    };
    CountDownTimer ShutDownTimer_3 = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTesting3 = false;
            testOk3 = false;
            needTest3 = true;
            clickReceive3 = false;
            displayStatus_3("请打开外设");
            displayStatusTimer_3.cancel();
            displayStatusTimer_3.start();
        }
    };
    CountDownTimer ShutDownTimer_4 = new CountDownTimer(30000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTesting4 = false;
            testOk4 = false;
            needTest4 = true;
            clickReceive4 = false;
            displayStatus_4("请打开外设");
            displayStatusTimer_4.cancel();
            displayStatusTimer_4.start();
        }
    };

    private void dismissStatus_1() {
        menu_start_icon_1.setVisibility(View.GONE);
        menu_start_label_1.setVisibility(View.GONE);
    }

    private void dismissStatus_2() {
        menu_start_icon_2.setVisibility(View.GONE);
        menu_start_label_2.setVisibility(View.GONE);
    }

    private void dismissStatus_3() {
        menu_start_icon_3.setVisibility(View.GONE);
        menu_start_label_3.setVisibility(View.GONE);
    }

    private void dismissStatus_4() {
        menu_start_icon_4.setVisibility(View.GONE);
        menu_start_label_4.setVisibility(View.GONE);
    }

    private void displayStatus_1(String msg) {
        menu_start_icon_1.setVisibility(View.GONE);
        menu_start_label_1.setVisibility(View.VISIBLE);
        menu_start_label_1.setText(msg);
    }


    private void displayStatus_2(String msg) {
        menu_start_icon_2.setVisibility(View.GONE);
        menu_start_label_2.setVisibility(View.VISIBLE);
        menu_start_label_2.setText(msg);
    }

    private void displayStatus_3(String msg) {
        menu_start_icon_3.setVisibility(View.GONE);
        menu_start_label_3.setVisibility(View.VISIBLE);
        menu_start_label_3.setText(msg);
    }

    private void displayStatus_4(String msg) {
        menu_start_icon_4.setVisibility(View.GONE);
        menu_start_label_4.setVisibility(View.VISIBLE);
        menu_start_label_4.setText(msg);
    }


    private void startTimerCount1() {
        if (result_container_1.getVisibility() != View.VISIBLE || isTesting1 || testOk1) {
            return;
        }
        final int seconds = Integer.parseInt(time_counter_1.getText().toString());
        slideSeconds1 = seconds;
        if (timerCounter1 != null) {
            timerCounter1.cancel();
        }
        timerCounter1 = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                slideSeconds1--;
                if (slideSeconds1 < 0) {
                    slideSeconds1 = 0;
                }
                time_counter_1.setText(String.valueOf(slideSeconds1));
            }

            @Override
            public void onFinish() {
                Log.d("hailong12", "Result onFinish ");
                mHandler.removeCallbacks(resultSuccessRunnable1);
                mHandler.post(resultSuccessRunnable1);
                time_counter_1.setText(String.valueOf(seconds));
            }

        };
        timerCounter1.start();
    }

    private void startTimerCount2() {
        if (result_container_2.getVisibility() != View.VISIBLE || isTesting2 || testOk2) {
            return;
        }
        final int seconds = Integer.parseInt(time_counter_2.getText().toString());
        slideSeconds2 = seconds;
        if (timerCounter2 != null) {
            timerCounter2.cancel();
        }
        timerCounter2 = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong22", " onTick ");
                slideSeconds2--;
                if (slideSeconds2 < 0) {
                    slideSeconds2 = 0;
                }
                time_counter_2.setText(String.valueOf(slideSeconds2));
            }

            @Override
            public void onFinish() {
                Log.d("hailong22", "Result onFinish ");
                mHandler.removeCallbacks(resultSuccessRunnable2);
                mHandler.post(resultSuccessRunnable2);
                time_counter_2.setText(String.valueOf(seconds));
            }

        };
        timerCounter2.start();
    }

    private void startTimerCount3() {
        if (result_container_3.getVisibility() != View.VISIBLE || isTesting3 || testOk3) {
            return;
        }
        final int seconds = Integer.parseInt(time_counter_3.getText().toString());
        slideSeconds3 = seconds;
        if (timerCounter3 != null) {
            timerCounter3.cancel();
        }
        timerCounter3 = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong33", " onTick ");
                slideSeconds3--;
                if (slideSeconds3 < 0) {
                    slideSeconds3 = 0;
                }
                time_counter_3.setText(String.valueOf(slideSeconds3));
            }

            @Override
            public void onFinish() {
                mHandler.removeCallbacks(resultSuccessRunnable3);
                mHandler.post(resultSuccessRunnable3);
                time_counter_3.setText(String.valueOf(seconds));
            }

        };
        timerCounter3.start();
    }

    private void startTimerCount4() {
        if (result_container_4.getVisibility() != View.VISIBLE || isTesting4 || testOk4) {
            return;
        }
        final int seconds = Integer.parseInt(time_counter_4.getText().toString());
        slideSeconds4 = seconds;
        if (timerCounter4 != null) {
            timerCounter4.cancel();
        }
        timerCounter4 = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong44", " onTick ");
                slideSeconds4--;
                if (slideSeconds4 < 0) {
                    slideSeconds4 = 0;
                }
                time_counter_4.setText(String.valueOf(slideSeconds4));
            }

            @Override
            public void onFinish() {
                mHandler.removeCallbacks(resultSuccessRunnable4);
                mHandler.post(resultSuccessRunnable4);
                time_counter_4.setText(String.valueOf(seconds));
            }

        };
        timerCounter4.start();
    }

    private void onDataReceived_IC(final byte[] buffer, final int size) {
        String result = AppUtils.bytesToHexString(buffer);
        String code = result.replace("\n", "").replace("\r", "");
        boolean success = "00".equals(code.substring(2, 4));
        if (success) {
            if (isPinLoad) {//密码下载成功
//                toast("密码下载成功");
                //获取0扇区01块的学号
                Log.d("hailong30", " isPinLoadOk code " + code);
                try {
                    if (mOutputStream != null) {
                        isCodeOk = true;
                        mOutputStream.write(code_read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isPinLoad = false;
                ICHandler.postDelayed(mRunnable, 100);
            } else if (isCodeOk) {
                int startPos = 0;
                String startStr = AppUtils.bytesToHexString(subBytes(buffer, 0, 2));
                if (startStr.endsWith("00")) {
                    startPos = 2;
                }
                int codeEndPos = startPos * 2 + 10 * 2;
                icCode = getICCode(buffer, codeEndPos);
                icSex = "01".equals(AppUtils.bytesToHexString(subBytes(buffer, codeEndPos + 1, 1))) ? "男" : "女";
                Log.d("hailong30", " isCodeOk icCode " + icCode + " icSex is " + icSex);
                try {
                    if (mOutputStream != null) {
                        isDataOk = true;
                        mOutputStream.write(data_read);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                isCodeOk = false;
                ICHandler.postDelayed(mRunnable, 100);
            } else if (isDataOk) {
                int startPos = 0;
                String startStr = AppUtils.bytesToHexString(subBytes(buffer, 0, 2));
                if (startStr.endsWith("00")) {
                    startPos = 2;
                }
                byte[] nameBytes;
                byte[] lastNameBytes = subBytes(buffer, startPos + 6, 2);
                if (AppUtils.isEqual(new byte[]{0X20, 0X20}, lastNameBytes)) {
                    nameBytes = subBytes(buffer, startPos, 6);
                } else {
                    nameBytes = subBytes(buffer, startPos, 8);
                }
                icName = getICName(nameBytes);
                icClas = getICClass(subBytes(buffer, startPos + 8, 1)[0]);
                icGradeCode = AppUtils.bytesToHexString(subBytes(buffer, startPos + 9, 2));
                icGrade = PatternUtils.replaceWithPinyin(String.valueOf(getICGrade(subBytes(buffer, startPos + 11, 1)))) + "班";
                icOrderCode = AppUtils.bytesToHexString(subBytes(buffer, startPos + 12, 2));
                Log.d("hailong30", " clas " + icClas + " gradeCode " + icGradeCode + " grade " + icGrade + " orderCode " + icOrderCode);
                Log.d("hailong30", " isDataOk code " + code);
                //保存学生信息,暂且不保存ordercode和gradecode
                //保存
                UserInfo info = new UserInfo();
                info.code = icCode;
                info.name = icName;
                info.classes = icClas;
                info.grade = icGrade;
                info.sex = icSex;
                closeIc();
                if (checkICInfo()) {
                    if (icPart1) {
                        applyIcInfo1(info);
                        icPart1 = false;
                    } else if (icPart2) {
                        applyIcInfo2(info);
                        icPart2 = false;
                    } else if (icPart3) {
                        applyIcInfo3(info);
                        icPart3 = false;
                    } else if (icPart4) {
                        applyIcInfo4(info);
                        icPart4 = false;
                    }
                    toast("读卡成功");
                }
                isDataOk = false;
            }
        }
    }

    /************
     * 读卡完毕
     **************************/
    //数据通用
    YangwoqiuzoGroupTestActivity.FHLHandler mHandler = new YangwoqiuzoGroupTestActivity.FHLHandler();
    DataImportDialog dataImportDialog;//数据查询可能较慢，弹出框体验更好
    private static final int Max_Connect_Time = 6000;//等待连接结果的时间
    private static final int Max_Result_Time = 3 * 80 * 1000;//等待结果的时间可能会有点长，这个时间不好控制,暂定两分钟
    private static final int Hold_Time = 500;//500ms发送一次

    //设备1数据和视图
    boolean isTesting1;//正在测试中
    boolean testOk1;//测试完毕
    boolean testComplete1;//测试完毕并且保存完毕

    //模拟按钮
    View test_a;
    View test_b;
    View test_c;
    View test_d;
    View test_e;
    View test_f;
    View test_g;
    View test_h;
    View test_i;
    View test_j;
    View test_k;
    View test_l;

    View connect_all;
    //学生信息 输入
    View result_settings_1;//输入学生信息
    EditText code_et_1;//输入学号
    View code_delete_1;
    View result_icread_1;//删除输入框学生信息
    View result_ok_1;//确认学生信息
    View test_delete_1;//删除当学学生信息

    //学生信息 展现
    View result_container_1;//学生信息展现区域
    ImageView test_avatar_1;//学生头像
    TextView result_name_1;//学生名字
    TextView result_sex_1;//学生性别
    TextView result_class_1;//学生年级和班级
    TextView result_code_1;//学生学号
    //设备状态检测
    boolean deviceReady1 = true;//设备是否准备好
    TextView device_result_1;//当前设备状态
    TextView device_status_1;//设备连接状态
    View menu_connect_1;//连接外设
    View menu_start_1;//开始测试
    ImageView menu_start_icon_1;
    TextView menu_start_label_1;
    ImageView menu_start_icon_2;
    TextView menu_start_label_2;
    ImageView menu_start_icon_3;
    TextView menu_start_label_3;
    ImageView menu_start_icon_4;
    TextView menu_start_label_4;

    View result_operation_1;//重测 保存 区域
    View result_test_again_1;//重测一次
    View result_best_save_1;//保存成绩

    View bobao_1, bobao_2, bobao_3, bobao_4;//播报按钮

    UserInfo userInfo1;//查询到的学生
    YangwoqiuzoGroupTestActivity.LocalLoadTask1 loadTask1;//查询学生信息

    CountDownTimer resultCountDownTimer1;//测试计时器
    String currentResult_1;
    int currentSeconds_1 = 30;
    String currentResult_2;
    int currentSeconds_2 = 30;
    String currentResult_3;
    int currentSeconds_3 = 30;
    String currentResult_4;
    int currentSeconds_4 = 30;
    private static final String Send_Start_Test_1 = "0116";//开始测试1
    private static final String Send_Get_Result_1 = "0114";//获取测试数据1
    //连接外设1接收数据
    private static final String RC_Not_Ready_1 = "01D0";//没准备好1
    private static final String RC_Connect_Fail_1 = "001";//连接失败1
    private static final String RC_Testing_1 = "01C4";//测试中1
    private static final String RC_Problem_1 = "01C1";//外设故障1
    private static final String RC_Success_1 = "01C5";//测试完毕1
    private static final String RC_Connect_Success_1 = "01D1";//连接成功1
    View start_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.yangwoqizuo_group_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        moni();
        start_test = findViewById(R.id.start_test);
        start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
                CountDownTimer timer = new CountDownTimer(200, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        startTimerCount1();
                        startTimerCount2();
                        startTimerCount3();
                        startTimerCount4();
                        Log.d("hailong88", " onFinish ");
                        waitResult();
                    }
                };
                timer.start();
            }
        });
        initView1();
        initView2();
        initView3();
        initView4();

        initData1();
        initData2();
        initData3();
        initData4();
//        startICSeriaport();
    }

    CountDownTimer sendCountDownTimer, resultCountDownTimer;
    int sendAutoGenarator = 0;
    int getAutoGenarator = 0;

    protected void sendRequest() {
        clickReceive1 = clickReceive2 = clickReceive3 = clickReceive4 = false;
        needTest1 = isTesting1;
        needTest2 = isTesting2;
        needTest3 = isTesting3;
        needTest4 = isTesting4;

        sendAutoGenarator = 0;
        if (sendCountDownTimer != null) {
            sendCountDownTimer.cancel();
        }
        //400ms内发两次
        sendCountDownTimer = new CountDownTimer(1000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                //轮询发送
                if (sendAutoGenarator % 4 == 0) {
                    //设备1
                    //学生信息输入OK
                    if (result_container_1.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        if (!isTesting1) {
                            if (!testOk1) {
                                //如果测试次数大于三次，不允许
                                //设备1开始测试
                                write(Send_Start_Test_1);
                                waitResult1();
                                devIdleTimer_1.cancel();
                                devIdleTimer_1.start();
                            }
                        }
                    }
                }
                if (sendAutoGenarator % 4 == 1) {
                    //设备2
                    //学生信息输入OK
                    if (result_container_2.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        if (!isTesting2) {
                            if (!testOk2) {
                                //如果测试次数大于三次，不允许
                                //设备2开始测试
                                write(Send_Start_Test_2);
                                waitResult2();
                                devIdleTimer_2.cancel();
                                devIdleTimer_2.start();
                            }
                        }
                    }
                }
                if (sendAutoGenarator % 4 == 2) {
                    //设备3
                    //学生信息输入OK
                    if (result_container_3.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        if (!isTesting3) {
                            //如果测试次数大于三次，不允许
                            //设备3开始测试
                            write(Send_Start_Test_3);
                            devIdleTimer_3.cancel();
                            devIdleTimer_3.start();
                        }
                    }
                }
                if (sendAutoGenarator % 4 == 3) {
                    //设备3
                    //学生信息输入OK
                    if (result_container_4.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        if (!isTesting4) {
                            //如果测试次数大于三次，不允许
                            //设备2开始测试
                            write(Send_Start_Test_4);
                            devIdleTimer_4.cancel();
                            devIdleTimer_4.start();
                        }
                    }
                }
                sendAutoGenarator++;
            }

            @Override
            public void onFinish() {
                sendAutoGenarator = 0;
            }

        };
        sendCountDownTimer.start();
    }

    private void getResult1() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        resultCountDownTimer = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_1);
            }

            @Override
            public void onFinish() {
                menu_start_icon_1.setVisibility(View.VISIBLE);
                menu_start_label_1.setVisibility(View.GONE);
                Log.d("hailong12", "Result onFinish ");
            }

        };
        resultCountDownTimer.start();
    }


    protected void waitResult() {
        clickReceive1 = clickReceive2 = clickReceive3 = clickReceive4 = true;
        //1，发送0114，hold 300ms
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        resultCountDownTimer = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                //轮询发送
                if (getAutoGenarator % 4 == 0) {
                    Log.d("hailong8", " 设备1收数据 ");
                    //设备1
                    //学生信息输入OK
                    if (result_container_1.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        //如果测试次数大于三次，不允许
                        //设备正常通信
                        if (!needTest1 && deviceOk1 && !testOk1) {
                            Log.d("hailong88", " 设备1收数据 ");
                            displayStatus_1("设备1等待成绩...");
                            write(Send_Get_Result_1);
                        }
                    }
                }

                if (getAutoGenarator % 4 == 1) {
                    //设备2
                    //学生信息输入OK
                    if (result_container_2.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        //如果测试次数大于三次，不允许
                        //设备正常通信
                        if (!needTest2 && deviceOk2 && !testOk2) {
                            displayStatus_2("设备2等待成绩...");
                            write(Send_Get_Result_2);
                        }
                    }
                }
                if (getAutoGenarator % 4 == 2) {
                    //设备3
                    //学生信息输入OK
                    if (result_container_3.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        //如果测试次数大于三次，不允许
                        //设备正常通信
                        if (!needTest3 && deviceOk3) {
                            displayStatus_3("设备3等待成绩...");
                            write(Send_Get_Result_3);
                        }
                    }
                }
                if (getAutoGenarator % 4 == 3) {
                    //设备3
                    //学生信息输入OK
                    if (result_container_4.getVisibility() == View.VISIBLE) {
                        //如果正在测试中，不允许
                        //如果测试次数大于三次，不允许
                        //设备正常通信
                        if (!needTest4 && deviceOk4) {
                            displayStatus_4("设备4等待成绩...");
                            write(Send_Get_Result_4);
                        }
                    }
                }
                getAutoGenarator++;
            }

            @Override
            public void onFinish() {
                dismissStatus_1();
                dismissStatus_2();
                dismissStatus_3();
                dismissStatus_4();
                getAutoGenarator = 0;
            }

        };

        resultCountDownTimer.start();
    }

    @Override
    public void onBackPressed() {
        boolean testing1 = isTesting1 && !testOk1;
        boolean notsave1 = !saveScore1 && testOk1;

        boolean testing2 = isTesting2 && !testOk2;
        boolean notsave2 = !saveScore2 && testOk2;

        boolean testing3 = isTesting3 && !testOk3;
        boolean notsave3 = !saveScore3 && testOk3;

        boolean testing4 = isTesting4 && !testOk4;
        boolean notsave4 = !saveScore4 && testOk4;

        boolean isTesting = testing1 || testing2 || testing3 || testing4;
        boolean isNotSave = notsave1 || notsave2 || notsave3 || notsave4;

        boolean finishNotAllow1 = testing1 || notsave1 || testing2 || notsave2
                || testing3 || notsave3 || testing4 || notsave4;
        String msg = "";
        if (isTesting) {
            if (testing1) {
                msg = "设备1正在测试中，退出将取消当前成绩，确认退出？";
            } else if (testing2) {
                msg = "设备2正在测试中，退出将取消当前成绩，确认退出？";
            } else if (testing3) {
                msg = "设备3正在测试中，退出将取消当前成绩，确认退出？";
            } else if (testing4) {
                msg = "设备4正在测试中，退出将取消当前成绩，确认退出？";
            }
        } else if (isNotSave) {
            if (notsave1) {
                msg = "设备1测试数据没有保存，退出将取消当前成绩，确认退出？";
            } else if (notsave2) {
                msg = "设备2测试数据没有保存，退出将取消当前成绩，确认退出？";
            } else if (notsave3) {
                msg = "设备3测试数据没有保存，退出将取消当前成绩，确认退出？";
            } else if (notsave4) {
                msg = "设备4测试数据没有保存，退出将取消当前成绩，确认退出？";
            }
        }
        if (finishNotAllow1) {
            final AppDialog appDialog = new AppDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    //delete in DB
                    finish();
                }

            });
            appDialog.show();
        } else {
            super.onBackPressed();
        }

    }

    private void closeIc() {
        closeSerialPort();
        isDataOk = isCodeOk = isPinLoad = false;
        ICHandler.removeCallbacks(mRunnable);
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mInputStream != null) {
                mInputStream.close();
            }
            mInputStream = null;
            mOutputStream = null;
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //设备1
        closeIc();
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }
        //设备2
        if (connectCountDownTimer2 != null) {
            connectCountDownTimer2.cancel();
        }
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
        //设备3
        if (connectCountDownTimer3 != null) {
            connectCountDownTimer3.cancel();
        }
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
        //设备4
        if (connectCountDownTimer4 != null) {
            connectCountDownTimer4.cancel();
        }
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
        if (timerCounter1 != null) {
            timerCounter1.cancel();
        }
        if (timerCounter2 != null) {
            timerCounter2.cancel();
        }
        if (timerCounter3 != null) {
            timerCounter3.cancel();
        }
        if (timerCounter4 != null) {
            timerCounter4.cancel();
        }
        if (sendCountDownTimer != null) {
            sendCountDownTimer.cancel();
        }
        if (ShutDownTimer_1 != null) {
            ShutDownTimer_1.cancel();
        }
        if (ShutDownTimer_2 != null) {
            ShutDownTimer_2.cancel();
        }
        if (ShutDownTimer_3 != null) {
            ShutDownTimer_3.cancel();
        }
        if (ShutDownTimer_4 != null) {
            ShutDownTimer_4.cancel();
        }
    }

    class FHLHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String code = (String) msg.obj;

            devIdleTimer_1.cancel();
            if (code.startsWith("01")) {
                if (!(RC_Testing_1.equals(code) || RC_Success_1.equals(code) || RC_Problem_1.equals(code)) && !RC_Connect_Success_1.equals(code)) {
                    deviceOk1 = false;
                    displayStatus_1("请打开外设");
                    displayStatusTimer_2.cancel();
                    displayStatusTimer_2.start();
                } else {
                    Log.d("hailong88", " devIdleTimer_1 " + code);
                    deviceOk1 = true;
                }
            }

            devIdleTimer_2.cancel();
            if (code.startsWith("02")) {
                if (!(RC_Testing_2.equals(code) || RC_Success_2.equals(code) || RC_Problem_2.equals(code)) && !RC_Connect_Success_2.equals(code)) {
                    deviceOk2 = false;
                    displayStatus_2("请打开外设");
                } else {
                    deviceOk2 = true;
                }
            }

            devIdleTimer_3.cancel();
            if (code.startsWith("03")) {
                if (!(RC_Testing_3.equals(code) || RC_Success_3.equals(code) || RC_Problem_3.equals(code)) && !RC_Connect_Success_3.equals(code)) {
                    deviceOk3 = false;
                    displayStatus_3("请打开外设");
                } else {
                    deviceOk3 = true;
                }
            }
            devIdleTimer_4.cancel();
            if (code.startsWith("04")) {
                if (!(RC_Testing_4.equals(code) || RC_Success_4.equals(code) || RC_Problem_4.equals(code)) && !RC_Connect_Success_4.equals(code)) {
                    deviceOk4 = false;
                    displayStatus_4("请打开外设");
                } else {
                    deviceOk4 = true;
                }
            }
            switch ((String) msg.obj) {
                case RC_Not_Ready_1:
                case RC_Problem_1:
                    displayStatus_1("设备故障");
                    displayStatusTimer_1.cancel();
                    displayStatusTimer_1.start();
                    break;
                case RC_Connect_Fail_1:
                    displayStatus_1("连接失败");
                    displayStatusTimer_1.cancel();
                    displayStatusTimer_1.start();
                    break;
                case RC_Connect_Success_1:
                    deviceReady1 = true;
                    displayStatus_1("连接成功");
                    displayStatusTimer_1.cancel();
                    displayStatusTimer_1.start();
                    break;
                case RC_Testing_1:
                    isTesting1 = true;
                    displayStatusTimer_1.cancel();
                    ShutDownTimer_1.cancel();
                    ShutDownTimer_1.start();
                    displayStatus_1("测试中...");
                    break;
                //设备2
                case RC_Not_Ready_2:
                case RC_Problem_2:
                    displayStatus_2("设备故障");
                    displayStatusTimer_2.cancel();
                    displayStatusTimer_2.start();
                    break;
                case RC_Connect_Fail_2:
                    displayStatus_2("连接失败");
                    displayStatusTimer_2.cancel();
                    displayStatusTimer_2.start();
                    break;
                case RC_Connect_Success_2:
                    deviceReady2 = true;
                    displayStatus_2("连接成功");
                    displayStatusTimer_2.cancel();
                    displayStatusTimer_2.start();
                    break;
                case RC_Testing_2:
                    isTesting2 = true;
                    displayStatusTimer_2.cancel();
                    ShutDownTimer_2.cancel();
                    ShutDownTimer_2.start();
                    displayStatus_2("测试中...");
                    break;
                //设备3
                case RC_Not_Ready_3:
                case RC_Problem_3:
                    displayStatus_3("设备故障");
                    displayStatusTimer_3.cancel();
                    displayStatusTimer_3.start();
                    break;
                case RC_Connect_Fail_3:
                    displayStatus_3("连接失败");
                    displayStatusTimer_3.cancel();
                    displayStatusTimer_3.start();
                    break;
                case RC_Connect_Success_3:
                    deviceReady3 = true;
                    displayStatus_3("连接成功");
                    displayStatusTimer_3.cancel();
                    displayStatusTimer_3.start();
                    break;
                case RC_Testing_3:
                    isTesting3 = true;
                    displayStatusTimer_3.cancel();
                    ShutDownTimer_3.cancel();
                    ShutDownTimer_3.start();
                    displayStatus_3("测试中...");
                    break;

                //设备4
                case RC_Not_Ready_4:
                case RC_Problem_4:
                    displayStatus_4("设备故障");
                    displayStatusTimer_4.cancel();
                    displayStatusTimer_4.start();
                    break;
                case RC_Connect_Fail_4:
                    displayStatus_4("连接失败");
                    displayStatusTimer_4.cancel();
                    displayStatusTimer_4.start();
                    break;
                case RC_Connect_Success_4:
                    deviceReady4 = true;
                    displayStatus_4("连接成功");
                    displayStatusTimer_4.cancel();
                    displayStatusTimer_4.start();
                    break;
                case RC_Testing_4:
                    isTesting4 = true;
                    displayStatusTimer_4.cancel();
                    ShutDownTimer_4.cancel();
                    ShutDownTimer_4.start();
                    displayStatus_4("测试中...");
                    break;
            }

        }
    }

    protected void waitResult1() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }
        final int seconds = Integer.parseInt(time_counter_1.getText().toString());
        resultCountDownTimer1 = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_1);
            }

            @Override
            public void onFinish() {
                Log.d("hailong12", "Result onFinish ");
                dismissStatus_1();
            }

        };
        resultCountDownTimer1.start();
    }

    void initData1() {
        connect_all.setOnClickListener(this);
        //设备1
        result_icread_1.setOnClickListener(this);
        result_ok_1.setOnClickListener(this);
        menu_connect_1.setOnClickListener(this);
//        menu_start_1.setOnClickListener(this);
        test_delete_1.setOnClickListener(this);
        result_test_again_1.setOnClickListener(this);
        result_best_save_1.setOnClickListener(this);
        time_modify_1.setOnClickListener(this);
        time_modify_2.setOnClickListener(this);
        time_modify_3.setOnClickListener(this);
        time_modify_4.setOnClickListener(this);
        bobao_1.setOnClickListener(this);
        bobao_2.setOnClickListener(this);
        bobao_3.setOnClickListener(this);
        bobao_4.setOnClickListener(this);
    }

    void moni() {
        test_a = findViewById(R.id.test_a);
        test_b = findViewById(R.id.test_b);
        test_c = findViewById(R.id.test_c);
        test_d = findViewById(R.id.test_d);
        test_e = findViewById(R.id.test_e);

        test_f = findViewById(R.id.test_f);
        test_g = findViewById(R.id.test_g);
        test_h = findViewById(R.id.test_h);
        test_i = findViewById(R.id.test_i);
        test_j = findViewById(R.id.test_j);
        test_k = findViewById(R.id.test_k);
        test_l = findViewById(R.id.test_l);

        test_a.setOnClickListener(moniListener);
        test_b.setOnClickListener(moniListener);
        test_c.setOnClickListener(moniListener);
        test_d.setOnClickListener(moniListener);
        test_e.setOnClickListener(moniListener);
        test_f.setOnClickListener(moniListener);
        test_g.setOnClickListener(moniListener);
        test_h.setOnClickListener(moniListener);

        test_i.setOnClickListener(moniListener);
        test_j.setOnClickListener(moniListener);
        test_k.setOnClickListener(moniListener);
        test_l.setOnClickListener(moniListener);
    }

    View.OnClickListener moniListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.test_e://1模拟第一次成绩
                    onPause();
                    playTime = 0;
                    currentResult_3 = "12";
                    mHandler.removeCallbacks(resultSuccessRunnable3);
                    mHandler.post(resultSuccessRunnable3);
                    break;
                case R.id.test_f://1模拟第二次成绩
//                    getResultSuccess1("1234");
                    playTime = 0;
                    currentResult_3 = "12";
                    mHandler.removeCallbacks(resultSuccessRunnable3);
                    mHandler.postDelayed(resultSuccessRunnable3, 10 * 1000);
                    break;
                case R.id.test_g://1模拟第三次成绩
                    getResultSuccess1("1256");
                    break;
                case R.id.test_h://2模拟第一次成绩
                    getResultSuccess2("888");
                    break;
                case R.id.test_i://2模拟第二次成绩
                    getResultSuccess2("999");
                    break;
                case R.id.test_j://2模拟第三次成绩
                    getResultSuccess2("346");
                    break;
                case R.id.test_k:
                    //1最好的成绩算出来
                    onPause();
                    playTime = 0;
                    resultThread = new YangwoqiuzoGroupTestActivity.BobaoThread(currentResult_1);
                    resultThread.start();
                    break;
                case R.id.test_l:
                    //1最好的成绩算出来
                    onPause();
                    playTime = 0;
                    resultThread = new YangwoqiuzoGroupTestActivity.BobaoThread(currentResult_2);
                    resultThread.start();
                    break;
            }
        }
    };
    CountDownTimer countDownTimer;
    int playTime = 0;
    TextView time_counter_1;
    View time_modify_1;
    TextView time_counter_2;
    View time_modify_2;
    TextView time_counter_3;
    View time_modify_3;
    TextView time_counter_4;
    View time_modify_4;

    void handleResultCancel_1() {
        if (timerCounter1 != null) {
            timerCounter1.cancel();
        }
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }
        currentResult_1 = null;
        mHandler.removeCallbacks(resultSuccessRunnable1);
        dismissStatus_1();
        device_result_1.setText("0");
        time_counter_1.setText(String.valueOf(currentSeconds_1));
        testOk1 = testComplete1 = isTesting1 = false;
        deviceOk1 = false;
        handleTestCount1();
    }

    void handleResultCancel_2() {
        if (timerCounter2 != null) {
            timerCounter2.cancel();
        }
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
        currentResult_2 = null;
        mHandler.removeCallbacks(resultSuccessRunnable2);
        dismissStatus_2();
        device_result_2.setText("0");
        time_counter_2.setText(String.valueOf(currentSeconds_2));
        deviceOk2 = false;
        testOk2 = testComplete2 = isTesting2 = false;
        handleTestCount2();
    }

    void handleResultCancel_3() {
        if (timerCounter3 != null) {
            timerCounter3.cancel();
        }
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
        currentResult_3 = null;
        mHandler.removeCallbacks(resultSuccessRunnable3);
        dismissStatus_3();
        menu_start_label_3.setVisibility(View.GONE);
        device_result_3.setText("0");
        time_counter_3.setText(String.valueOf(currentSeconds_3));
        deviceOk3 = true;
        testOk3 = testComplete3 = isTesting3 = false;
        handleTestCount3();
    }

    void handleResultCancel_4() {
        if (timerCounter4 != null) {
            timerCounter4.cancel();
        }
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
        currentResult_4 = null;
        mHandler.removeCallbacks(resultSuccessRunnable4);
        dismissStatus_4();
        ;
        menu_start_label_4.setVisibility(View.GONE);
        device_result_4.setText("0");
        time_counter_4.setText(String.valueOf(currentSeconds_4));
        deviceOk4 = true;
        testOk4 = testComplete4 = isTesting4 = false;
        handleTestCount4();
    }

    BobaoThread resultThread;

    class BobaoThread extends Thread {
        String data;

        public BobaoThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            playVoice(data);
        }

        private void playVoice(final String data) {
            ensureMp("test_score_is.ogg");
            synchronized (resultThread) {
                try {
                    wait(2000);
                } catch (Exception e) {

                }
            }

            final ArrayList<String> sounds = AppUtils.getSoundList(data);
            final int size = sounds.size();
            final int duration = 700;
            if (size > 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer = new CountDownTimer((duration) * (size + 1), duration) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (playTime < size) {
                                    String path = sounds.get(playTime);
                                    if (!StringUtils.isEmpty(path)) {
                                        ensureMp(path);
                                        playTime++;
                                    }
                                }
                            }

                            @Override
                            public void onFinish() {
                                playTime = 0;
                            }

                        };
                        countDownTimer.start();
                    }
                });

            }

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    TextView test_score_des_1;
    TextView test_score_des_2;
    TextView test_score_des_3;
    TextView test_score_des_4;


    void initView1() {
        test_score_des_1 = (TextView) findViewById(R.id.test_score_des_1);
        test_score_des_2 = (TextView) findViewById(R.id.test_score_des_2);
        test_score_des_3 = (TextView) findViewById(R.id.test_score_des_3);
        test_score_des_4 = (TextView) findViewById(R.id.test_score_des_4);

        //设备1
        connect_all = findViewById(R.id.connect_all);
        result_settings_1 = findViewById(R.id.result_settings_1);
        code_et_1 = (EditText) findViewById(R.id.code_et_1);
        code_delete_1 = findViewById(R.id.code_delete_1);
        code_et_1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok_1.performClick();
                return false;
            }
        });
        result_icread_1 = findViewById(R.id.result_icread_1);
        result_ok_1 = findViewById(R.id.result_ok_1);
        test_delete_1 = findViewById(R.id.test_delete_1);
        //学生信息 展现
        result_container_1 = findViewById(R.id.result_container_1);
        test_avatar_1 = (ImageView) findViewById(R.id.test_avatar_1);
        result_name_1 = (TextView) findViewById(R.id.result_name_1);
        result_sex_1 = (TextView) findViewById(R.id.result_sex_1);
        result_class_1 = (TextView) findViewById(R.id.result_class_1);
        result_code_1 = (TextView) findViewById(R.id.result_code_1);
        //设备状态检测
        device_result_1 = (TextView) findViewById(R.id.device_result_1);
        device_status_1 = (TextView) findViewById(R.id.device_status_1);
        menu_connect_1 = findViewById(R.id.menu_connect_1);
        menu_start_1 = findViewById(R.id.menu_start_1);
        result_operation_1 = findViewById(R.id.result_operation_1);
        result_test_again_1 = findViewById(R.id.result_test_again_1);
        result_best_save_1 = findViewById(R.id.result_best_save_1);
        menu_start_icon_1 = (ImageView) findViewById(R.id.menu_start_icon_1);
        menu_start_label_1 = (TextView) findViewById(R.id.menu_start_label_1);
        menu_start_icon_2 = (ImageView) findViewById(R.id.menu_start_icon_2);
        menu_start_label_2 = (TextView) findViewById(R.id.menu_start_label_2);
        menu_start_icon_3 = (ImageView) findViewById(R.id.menu_start_icon_3);
        menu_start_label_3 = (TextView) findViewById(R.id.menu_start_label_3);
        menu_start_icon_4 = (ImageView) findViewById(R.id.menu_start_icon_4);
        menu_start_label_4 = (TextView) findViewById(R.id.menu_start_label_4);

        time_counter_1 = (TextView) findViewById(R.id.time_counter_1);
        time_counter_2 = (TextView) findViewById(R.id.time_counter_2);
        time_counter_3 = (TextView) findViewById(R.id.time_counter_3);
        time_counter_4 = (TextView) findViewById(R.id.time_counter_4);

        time_modify_1 = findViewById(R.id.time_modify_1);
        time_modify_2 = findViewById(R.id.time_modify_2);
        time_modify_3 = findViewById(R.id.time_modify_3);
        time_modify_4 = findViewById(R.id.time_modify_4);

        bobao_1 = findViewById(R.id.bobao_1);
        bobao_2 = findViewById(R.id.bobao_2);
        bobao_3 = findViewById(R.id.bobao_3);
        bobao_4 = findViewById(R.id.bobao_4);
    }

    private boolean needTest1, needTest2, needTest3, needTest4, clickReceive1, clickReceive2, clickReceive3, clickReceive4;
    boolean deviceOk1, deviceOk2, deviceOk3, deviceOk4;
    boolean saveScore1, saveScore2, saveScore3, saveScore4;

    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                String result = AppUtils.bytesToHexString(buffer);
                String code = result.replace("\n", "").replace("\r", "");
                Log.d("hailong12", " code is " + code);
                if (!StringUtils.isEmpty(code)) {
                    //如果是C5开头，则取后五位数字
                    //回0XC5+data1+data2+data3+data4+data5
                    //data1~data5，每个1个byte，对应一个数, ASCII码形式
                    String data = "";
                    if (!needTest1 && clickReceive1 && code.startsWith(RC_Success_1)) {
                        String data1 = code.substring(6, 16);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }

                        if (data.startsWith("0000000000")) {
                            //正在测试
                            displayStatus_1("正在测试...");
                            return;
                        }
                        String score = data;
                        int pos = data.indexOf(".");
                        if (pos >= 0) {
                            score = data.substring(0, pos);
                        }
                        try {
                            score = String.valueOf(Integer.parseInt(score));
                        } catch (Exception e) {
                            return;
                        }
                        if ("0.0".equals(score) || "0".equals(score)) {
                            return;
                        }
                        try {
                            String lastScore = device_result_1.getText().toString();
                            String curScore = String.valueOf(Integer.parseInt(score));
                            if (lastScore != null && lastScore.equals(curScore)) {
                                return;
                            }
                        } catch (Exception e) {
                            return;
                        }

                        mHandler.removeCallbacks(resultSuccessRunnable1);
                        mHandler.postDelayed(resultSuccessRunnable1, 10 * 1000);
                        ShutDownTimer_1.cancel();
                        try {
                            device_result_1.setText(String.valueOf(score));
                        } catch (Exception e) {

                        }
                    } else if (!needTest2 && clickReceive2 && code.startsWith(RC_Success_2)) {
                        String data1 = code.substring(6, 16);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }

                        if (data.startsWith("0000000000")) {
                            //正在测试
                            displayStatus_1("正在测试...");
                            return;
                        }
                        String score = data;
                        int pos = data.indexOf(".");
                        if (pos >= 0) {
                            score = data.substring(0, pos);
                        }
                        try {
                            score = String.valueOf(Integer.parseInt(score));
                        } catch (Exception e) {
                            return;
                        }
                        if ("0.0".equals(score) || "0".equals(score)) {
                            return;
                        }
                        try {
                            String lastScore = device_result_2.getText().toString();
                            String curScore = String.valueOf(Integer.parseInt(score));
                            if (lastScore != null && lastScore.equals(curScore)) {
                                return;
                            }
                        } catch (Exception e) {
                            return;
                        }

                        mHandler.removeCallbacks(resultSuccessRunnable2);
                        mHandler.postDelayed(resultSuccessRunnable2, 10 * 1000);
                        ShutDownTimer_2.cancel();
                        try {
                            device_result_2.setText(String.valueOf(score));
                        } catch (Exception e) {

                        }
                    } else if (code.startsWith(RC_Success_3)) {
                        String data1 = code.substring(4, 14);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("0000000000")) {
                            //正在测试
                            menu_start_icon_3.setVisibility(View.GONE);
                            menu_start_label_3.setVisibility(View.VISIBLE);
                            return;
                        }
                        int pos = data.indexOf(".");
                        if (pos >= 0) {
                            data = data.substring(0, pos);
                        }
                        currentResult_3 = data;
                        mHandler.removeCallbacks(resultSuccessRunnable3);
                        mHandler.postDelayed(resultSuccessRunnable3, 10 * 1000);
                    } else if (code.startsWith(RC_Success_4)) {
                        String data1 = code.substring(4, 14);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("0000000000")) {
                            //正在测试
                            menu_start_icon_4.setVisibility(View.GONE);
                            menu_start_label_4.setVisibility(View.VISIBLE);
                            return;
                        }
                        int pos = data.indexOf(".");
                        if (pos >= 0) {
                            data = data.substring(0, pos);
                        }
                        currentResult_4 = data;
                        mHandler.removeCallbacks(resultSuccessRunnable4);
                        mHandler.postDelayed(resultSuccessRunnable4, 10 * 1000);
                    } else {
                        Message message = new Message();
                        message.obj = code.substring(0, 4);
                        mHandler.sendMessage(message);
                    }
                } else {
                    toast("连接设备失败");
                }
            }
        });
    }

    private void getResultSuccess1(String data) {
        if (timerCounter1 != null) {
            timerCounter1.cancel();
        }
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }
        dismissStatus_1();
        try {
            device_result_1.setText(String.valueOf(data));
        } catch (Exception e) {

        }
        Log.d("hailong12", "FHL result is " + data);
        isTesting1 = false;
        testOk1 = true;
        time_counter_1.setText(String.valueOf(currentSeconds_1));
        handleTestCount1();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.connect_all:
                menu_connect_1.performClick();
                menu_connect_2.performClick();
                menu_connect_3.performClick();
                menu_connect_4.performClick();
                break;
            //设备1
            case R.id.result_icread_1://清楚输入框信息
                icPart1 = true;
                icPart2 = icPart3 = icPart4 = false;
                startIcDetect();
                break;
            case R.id.result_icread_2:
                icPart2 = true;
                icPart1 = icPart3 = icPart4 = false;
                startIcDetect();
                break;
            case R.id.result_icread_3:
                icPart3 = true;
                icPart2 = icPart1 = icPart4 = false;
                startIcDetect();
                break;
            case R.id.result_icread_4:
                icPart4 = true;
                icPart2 = icPart3 = icPart1 = false;
                startIcDetect();
                break;
            case R.id.test_delete_1://删除学生信息
                //如果有成绩未保存，提示保存
                if (allowDeleUser1()) {
                    result_settings_1.setVisibility(View.VISIBLE);
                    result_container_1.setVisibility(View.GONE);
                }
                break;

            case R.id.result_ok_1://确定
                handleSearchResult1();
                break;
            case R.id.menu_start_1://开始测试
                if (result_container_1.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                if (!deviceReady1) {
                    toast("请先连接设备再测试");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting1) {
                    toast("正在测试，请稍候");
                    return;
                }
                isTesting1 = true;
                ensureMp("start_test.ogg", tip);
                device_status_1.setText("测试中...");
                menu_start_icon_1.setVisibility(View.GONE);
                menu_start_label_1.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test_1);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult1();
                    }
                }, 2000);
                startTimerCount1();
                break;

            case R.id.menu_connect_1://连接设备
//                onTestConnect1();
                device_status_1.setText("正在连接...");
                break;

            case R.id.result_test_again_1://重新测试
                handleResultCancel_1();
                break;
            case R.id.result_best_save_1:
                //保存数据
                handleResultSave1();
                break;

            case R.id.test_delete_2://删除学生信息
                //如果有成绩未保存，提示保存
                if (allowDeleUser2()) {
                    result_settings_2.setVisibility(View.VISIBLE);
                    result_container_2.setVisibility(View.GONE);
                }
                break;

            case R.id.result_ok_2://确定
                handleSearchResult2();
                break;
            case R.id.menu_start_2://开始测试
                if (result_container_2.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                if (!deviceReady2) {
                    toast("请先连接设备再测试");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting2) {
                    toast("正在测试，请稍候");
                    return;
                }
                isTesting2 = true;
                ensureMp("start_test.ogg", tip);
                device_status_2.setText("测试中...");
                menu_start_icon_2.setVisibility(View.GONE);
                menu_start_label_2.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test_2);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult2();
                    }
                }, 2000);
                startTimerCount2();
                break;

            case R.id.menu_connect_2://连接设备
//                onTestConnect2();
                device_status_2.setText("正在连接...");
                break;

            case R.id.result_test_again_2://重新测试
//                menu_start_2.performClick();
                handleResultCancel_2();
                break;
            case R.id.result_best_save_2:
                //保存数据
                handleResultSave2();
                break;
            case R.id.test_delete_3://删除学生信息
                //如果有成绩未保存，提示保存
                if (allowDeleUser3()) {
                    result_settings_3.setVisibility(View.VISIBLE);
                    result_container_3.setVisibility(View.GONE);
                }
                break;

            case R.id.result_ok_3://确定
                handleSearchResult3();
                break;
            case R.id.menu_start_3://开始测试
                if (result_container_3.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                if (!deviceReady3) {
                    toast("请先连接设备再测试");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting3) {
                    toast("正在测试，请稍候");
                    return;
                }
                isTesting3 = true;
                ensureMp("start_test.ogg", tip);
                device_status_3.setText("测试中...");
                menu_start_icon_3.setVisibility(View.GONE);
                menu_start_label_3.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test_3);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult3();
                    }
                }, 2000);
                startTimerCount3();
                break;

            case R.id.menu_connect_3://连接设备
//                onTestConnect3();
                device_status_3.setText("正在连接...");
                break;

            case R.id.result_test_again_3://重新测试
//                menu_start_3.performClick();
                handleResultCancel_3();
                break;
            case R.id.result_best_save_3:
                //保存数据
                handleResultSave3();
                break;
            case R.id.test_delete_4://删除学生信息
                //如果有成绩未保存，提示保存
                if (allowDeleUser4()) {
                    result_settings_4.setVisibility(View.VISIBLE);
                    result_container_4.setVisibility(View.GONE);
                }
                break;

            case R.id.result_ok_4://确定
                handleSearchResult4();
                break;
            case R.id.menu_start_4://开始测试
                if (result_container_4.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting4) {
                    toast("正在测试，请稍候");
                    return;
                }
                if (!deviceReady4) {
                    toast("请先连接设备再测试");
                    return;
                }
                isTesting4 = true;
                ensureMp("start_test.ogg", tip);
                //如果测试次数大于三次，不允许
                device_status_4.setText("测试中...");
                menu_start_icon_4.setVisibility(View.GONE);
                menu_start_label_4.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test_4);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult4();
                    }
                }, 2000);
                startTimerCount4();
                break;

            case R.id.menu_connect_4://连接设备
//                onTestConnect4();
                device_status_4.setText("正在连接...");
                break;

            case R.id.result_test_again_4://重新测试
//                menu_start_4.performClick();
                handleResultCancel_4();
                break;
            case R.id.result_best_save_4:
                //保存数据
                handleResultSave4();
                break;

            case R.id.time_modify_1:
                if (isTesting1) {
                    toast("设备1正在测试中，请稍候再试");
                    return;
                }
                //弹出框修改秒数
                final TimeCounterDialog appDialog = new TimeCounterDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true, time_counter_1.getText().toString());
                appDialog.setupContentView();
                appDialog.setDialogClickListener(new TimeCounterDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog.dismiss();
                    }

                    @Override
                    public void operationRight(String seconds) {
                        time_counter_1.setText(seconds);
                        currentSeconds_1 = Integer.parseInt(seconds);
                    }

                });
                appDialog.show();
                break;
            case R.id.time_modify_2:
                if (isTesting2) {
                    toast("设备2正在测试中，请稍候再试");
                    return;
                }
                //弹出框修改秒数
                final TimeCounterDialog appDialog2 = new TimeCounterDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true, time_counter_2.getText().toString());
                appDialog2.setupContentView();
                appDialog2.setDialogClickListener(new TimeCounterDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog2.dismiss();
                    }

                    @Override
                    public void operationRight(String seconds) {
                        time_counter_2.setText(seconds);
                        currentSeconds_2 = Integer.parseInt(seconds);
                    }

                });
                appDialog2.show();
                break;
            case R.id.time_modify_3:
                if (isTesting3) {
                    toast("设备3正在测试中，请稍候再试");
                    return;
                }
                //弹出框修改秒数
                final TimeCounterDialog appDialog3 = new TimeCounterDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true, time_counter_3.getText().toString());
                appDialog3.setupContentView();
                appDialog3.setDialogClickListener(new TimeCounterDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog3.dismiss();
                    }

                    @Override
                    public void operationRight(String seconds) {
                        time_counter_3.setText(seconds);
                        currentSeconds_3 = Integer.parseInt(seconds);
                    }

                });
                appDialog3.show();
                break;
            case R.id.time_modify_4:
                if (isTesting4) {
                    toast("设备4正在测试中，请稍候再试");
                    return;
                }
                //弹出框修改秒数
                final TimeCounterDialog appDialog4 = new TimeCounterDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true, time_counter_4.getText().toString());
                appDialog4.setupContentView();
                appDialog4.setDialogClickListener(new TimeCounterDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog4.dismiss();
                    }

                    @Override
                    public void operationRight(String seconds) {
                        time_counter_4.setText(seconds);
                        currentSeconds_4 = Integer.parseInt(seconds);
                    }

                });
                appDialog4.show();
                break;
            case R.id.bobao_1:
                if (StringUtils.isEmpty(currentResult_1)) {
                    toast("没有测试成绩");
                    return;
                }
                bobao1();
                break;
            case R.id.bobao_2:
                if (StringUtils.isEmpty(currentResult_2)) {
                    toast("没有测试成绩");
                    return;
                }
                bobao2();
                break;
            case R.id.bobao_3:
                if (StringUtils.isEmpty(currentResult_3)) {
                    toast("没有测试成绩");
                    return;
                }
                bobao3();
                break;
            case R.id.bobao_4:
                if (StringUtils.isEmpty(currentResult_4)) {
                    toast("没有测试成绩");
                    return;
                }
                bobao4();
                break;

        }
    }

    private void bobao1() {
        onPause();
        playTime = 0;
        resultThread = new BobaoThread(currentResult_1);
        resultThread.start();
    }

    private void bobao2() {
        onPause();
        playTime = 0;
        resultThread = new BobaoThread(currentResult_2);
        resultThread.start();
    }

    private void bobao3() {
        onPause();
        playTime = 0;
        resultThread = new BobaoThread(currentResult_3);
        resultThread.start();
    }

    private void bobao4() {
        onPause();
        playTime = 0;
        resultThread = new BobaoThread(currentResult_4);
        resultThread.start();
    }

    private boolean allowDeleUser1() {
        boolean testing = isTesting1;
        boolean notsave = testOk1;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备1正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备1测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset1();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //任务独立完成，互不干扰
    class LocalLoadTask1 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
                Log.d("hailong13", " show ");
                dataImportDialog.setCancelable(false);
                dataImportDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            //查询信息
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            String search = params[0];
            if (search != null) {
                userInfos = UserModel.queryUserInfoByCode(YangwoqiuzoGroupTestActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg", tip);
            } else {
                showUserInfo1(userInfos.get(0));
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void handleTestCount1() {
        isTesting1 = false;
        needTest1 = true;
        clickReceive1 = false;
        ShutDownTimer_1.cancel();
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }

    }

    //保存成绩到数据库
    void handleResultSave1() {
        currentResult_1 = device_result_1.getText().toString();
        if (StringUtils.isEmpty(currentResult_1) || Integer.parseInt(currentResult_1) <= 0) {
            toast("无效测试成绩");
            return;
        }
        Log.d("hailong", " currentResult_1 is " + currentResult_1);
        if (userInfo1 != null) {
            UserModel.updateUserInfoInYangwoqizuo(YangwoqiuzoGroupTestActivity.this, userInfo1, currentResult_1);
            toast("设备1成绩保存成功");
            saveScore1 = true;
            deviceOk1 = false;
            reset1();
        } else {
            ensureMp("invalid_code.ogg", tip);
        }
    }

    void reset1() {
        if (timerCounter1 != null) {
            timerCounter1.cancel();
        }
        if (resultCountDownTimer1 != null) {
            resultCountDownTimer1.cancel();
        }
        currentResult_1 = null;
        mHandler.removeCallbacks(resultSuccessRunnable1);
        result_settings_1.setVisibility(View.VISIBLE);
        result_container_1.setVisibility(View.GONE);
        isTesting1 = testComplete1 = testOk1 = false;
        device_result_1.setText("0");
    }

    void handleSearchResult1() {
        String str = code_et_1.getText().toString().replace(" ", "");
        if (str.isEmpty()) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(YangwoqiuzoGroupTestActivity.this, "正在查询...");
        dataImportDialog.setShowCancel(false);
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
        }
        loadTask1 = new YangwoqiuzoGroupTestActivity.LocalLoadTask1();
        loadTask1.execute(str);
    }


    //设备2
    //设备2数据和视图
    boolean isTesting2;//正在测试中
    boolean testOk2;//测试完毕
    boolean testComplete2;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_2;//输入学生信息
    EditText code_et_2;//输入学号
    View code_delete_2;
    View result_icread_2;//删除输入框学生信息
    View result_ok_2;//确认学生信息
    View test_delete_2;//删除当学学生信息

    //学生信息 展现
    View result_container_2;//学生信息展现区域
    ImageView test_avatar_2;//学生头像
    TextView result_name_2;//学生名字
    TextView result_sex_2;//学生性别
    TextView result_class_2;//学生年级和班级
    TextView result_code_2;//学生学号
    //设备状态检测
    boolean deviceReady2 = true;//设备是否准备好
    TextView device_result_2;//当前设备状态
    TextView device_status_2;//设备连接状态
    View menu_connect_2;//连接外设
    View menu_start_2;//开始测试

    View result_operation_2;//重测 保存 区域
    View result_test_again_2;//重测一次
    View result_best_save_2;//保存成绩

    UserInfo userInfo2;//查询到的学生
    YangwoqiuzoGroupTestActivity.LocalLoadTask2 loadTask2;//查询学生信息

    CountDownTimer connectCountDownTimer2;//连接设备计时器
    CountDownTimer resultCountDownTimer2;//测试计时器

    private static final String Send_Start_Test_2 = "0216";//开始测试2
    private static final String Send_Get_Result_2 = "0214";//获取测试数据2

    private static final String RC_Not_Ready_2 = "02D0";//没准备好
    private static final String RC_Connect_Fail_2 = "002";//连接失败
    private static final String RC_Testing_2 = "02C4";//测试中2
    private static final String RC_Problem_2 = "02C1";//外设故障2
    private static final String RC_Success_2 = "02C5";//测试完毕2
    private static final String RC_Connect_Success_2 = "02D1";//连接成功

    protected void waitResult2() {
        //2，发送0224，hold 300ms
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
        final int seconds = Integer.parseInt(time_counter_2.getText().toString());
        resultCountDownTimer2 = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong22", " onTick ");
                write(Send_Get_Result_2);
            }

            @Override
            public void onFinish() {
                Log.d("hailong22", "Result onFinish ");
                dismissStatus_2();
            }

        };
        resultCountDownTimer2.start();
    }

    void initData2() {
        //设备2
        result_icread_2.setOnClickListener(this);
        result_ok_2.setOnClickListener(this);
        menu_connect_2.setOnClickListener(this);
//        menu_start_2.setOnClickListener(this);
        test_delete_2.setOnClickListener(this);
        result_test_again_2.setOnClickListener(this);
        result_best_save_2.setOnClickListener(this);
    }

    void initView2() {
        //设备2
        result_settings_2 = findViewById(R.id.result_settings_2);
        code_et_2 = (EditText) findViewById(R.id.code_et_2);
        code_delete_2 = findViewById(R.id.code_delete_2);
        code_et_2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok_2.performClick();
                return false;
            }
        });
        result_icread_2 = findViewById(R.id.result_icread_2);
        result_ok_2 = findViewById(R.id.result_ok_2);
        test_delete_2 = findViewById(R.id.test_delete_2);
        //学生信息 展现
        result_container_2 = findViewById(R.id.result_container_2);
        test_avatar_2 = (ImageView) findViewById(R.id.test_avatar_2);
        result_name_2 = (TextView) findViewById(R.id.result_name_2);
        result_sex_2 = (TextView) findViewById(R.id.result_sex_2);
        result_class_2 = (TextView) findViewById(R.id.result_class_2);
        result_code_2 = (TextView) findViewById(R.id.result_code_2);
        //设备状态检测
        device_result_2 = (TextView) findViewById(R.id.device_result_2);
        device_status_2 = (TextView) findViewById(R.id.device_status_2);
        menu_connect_2 = findViewById(R.id.menu_connect_2);
        menu_start_2 = findViewById(R.id.menu_start_2);
        result_operation_2 = findViewById(R.id.result_operation_2);
        result_test_again_2 = findViewById(R.id.result_test_again_2);
        result_best_save_2 = findViewById(R.id.result_best_save_2);
    }

    private void getResultSuccess2(String data) {
        if (timerCounter2 != null) {
            timerCounter2.cancel();
        }
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
        try {
            device_result_2.setText(String.valueOf(data));
        } catch (Exception e) {

        }
        Log.d("hailong22", "FHL result is " + data);
        isTesting2 = false;
        testOk2 = true;
        time_counter_2.setText(String.valueOf(currentSeconds_2));
        dismissStatus_2();
        handleTestCount2();
    }


    private boolean allowDeleUser2() {
        boolean testing = isTesting2;
        boolean notsave = testOk2;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备2正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备2测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset2();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //保存成绩到数据库
    void handleResultSave2() {
        currentResult_2 = device_result_2.getText().toString();
        if (StringUtils.isEmpty(currentResult_2) || Integer.parseInt(currentResult_2) <= 0) {
            toast("无效测试成绩");
            return;
        }
        Log.d("hailong", " currentResult_2 is " + currentResult_2);
        if (userInfo2 != null) {
            UserModel.updateUserInfoInYangwoqizuo(YangwoqiuzoGroupTestActivity.this, userInfo2, currentResult_2);
            toast("设备2成绩保存成功");
            saveScore2 = true;
            deviceOk2 = false;
            reset2();
        } else {
            ensureMp("invalid_code.ogg", tip);
        }

    }

    void reset2() {
        if (timerCounter2 != null) {
            timerCounter2.cancel();
        }
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
        currentResult_2 = null;
        mHandler.removeCallbacks(resultSuccessRunnable2);
        result_settings_2.setVisibility(View.VISIBLE);
        result_container_2.setVisibility(View.GONE);
        isTesting2 = testComplete2 = testOk2 = false;
        device_result_2.setText("0");
        handleTestCount2();
    }

    void handleSearchResult2() {
        String str = code_et_2.getText().toString().replace(" ", "");
        if (str.isEmpty()) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(YangwoqiuzoGroupTestActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 2, str.length() - 2);
        }
        loadTask2 = new YangwoqiuzoGroupTestActivity.LocalLoadTask2();
        loadTask2.execute(str);
    }

    private void handleTestCount2() {
        isTesting2 = false;
        needTest2 = true;
        clickReceive2 = false;
        ShutDownTimer_2.cancel();
        if (resultCountDownTimer2 != null) {
            resultCountDownTimer2.cancel();
        }
    }


    class LocalLoadTask2 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
                Log.d("hailong23", " show ");
                dataImportDialog.setCancelable(false);
                dataImportDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            //查询信息
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            String search = params[0];
            if (search != null) {
                userInfos = UserModel.queryUserInfoByCode(YangwoqiuzoGroupTestActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg", tip);
//                Toast.makeText(YangwoqiuzoGroupTestActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                showUserInfo2(userInfos.get(0));
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    //设备3数据和视图
    boolean isTesting3;//正在测试中
    boolean testOk3;//测试完毕
    boolean testComplete3;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_3;//输入学生信息
    EditText code_et_3;//输入学号
    View code_delete_3;
    View result_icread_3;//删除输入框学生信息
    View result_ok_3;//确认学生信息
    View test_delete_3;//删除当学学生信息

    //学生信息 展现
    View result_container_3;//学生信息展现区域
    ImageView test_avatar_3;//学生头像
    TextView result_name_3;//学生名字
    TextView result_sex_3;//学生性别
    TextView result_class_3;//学生年级和班级
    TextView result_code_3;//学生学号
    //设备状态检测
    boolean deviceReady3 = true;//设备是否准备好
    TextView device_result_3;//当前设备状态
    TextView device_status_3;//设备连接状态
    View menu_connect_3;//连接外设
    View menu_start_3;//开始测试

    View result_operation_3;//重测 保存 区域
    View result_test_again_3;//重测一次
    View result_best_save_3;//保存成绩

    UserInfo userInfo3;//查询到的学生
    YangwoqiuzoGroupTestActivity.LocalLoadTask3 loadTask3;//查询学生信息

    CountDownTimer connectCountDownTimer3;//连接设备计时器
    CountDownTimer resultCountDownTimer3;//测试计时器

    private static final String Send_Start_Test_3 = "0316";//开始测试3
    private static final String Send_Get_Result_3 = "0314";//获取测试数据3

    private static final String RC_Not_Ready_3 = "03D0";//没准备好
    private static final String RC_Connect_Fail_3 = "003";//连接失败
    private static final String RC_Testing_3 = "03C4";//测试中3
    private static final String RC_Problem_3 = "03C1";//外设故障3
    private static final String RC_Success_3 = "03C5";//测试完毕3
    private static final String RC_Connect_Success_3 = "03D1";//连接成功

    protected void waitResult3() {
        //3，发送0334，hold 300ms
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
        final int seconds = Integer.parseInt(time_counter_3.getText().toString());
        resultCountDownTimer3 = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong33", " onTick ");
                write(Send_Get_Result_3);
            }

            @Override
            public void onFinish() {
                Log.d("hailong33", "Result onFinish ");
                dismissStatus_3();
                menu_start_label_3.setVisibility(View.GONE);
            }

        };
        resultCountDownTimer3.start();
    }

    void initData3() {
        //设备3
        result_icread_3.setOnClickListener(this);
        result_ok_3.setOnClickListener(this);
        menu_connect_3.setOnClickListener(this);
//        menu_start_3.setOnClickListener(this);
        test_delete_3.setOnClickListener(this);
        result_test_again_3.setOnClickListener(this);
        result_best_save_3.setOnClickListener(this);
    }

    void initView3() {
        //设备3
        result_settings_3 = findViewById(R.id.result_settings_3);
        code_et_3 = (EditText) findViewById(R.id.code_et_3);
        code_delete_3 = findViewById(R.id.code_delete_3);
        code_et_3.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok_3.performClick();
                return false;
            }
        });
        result_icread_3 = findViewById(R.id.result_icread_3);
        result_ok_3 = findViewById(R.id.result_ok_3);
        test_delete_3 = findViewById(R.id.test_delete_3);
        //学生信息 展现
        result_container_3 = findViewById(R.id.result_container_3);
        test_avatar_3 = (ImageView) findViewById(R.id.test_avatar_3);
        result_name_3 = (TextView) findViewById(R.id.result_name_3);
        result_sex_3 = (TextView) findViewById(R.id.result_sex_3);
        result_class_3 = (TextView) findViewById(R.id.result_class_3);
        result_code_3 = (TextView) findViewById(R.id.result_code_3);
        //设备状态检测
        device_result_3 = (TextView) findViewById(R.id.device_result_3);
        device_status_3 = (TextView) findViewById(R.id.device_status_3);
        menu_connect_3 = findViewById(R.id.menu_connect_3);
        menu_start_3 = findViewById(R.id.menu_start_3);
        result_operation_3 = findViewById(R.id.result_operation_3);
        result_test_again_3 = findViewById(R.id.result_test_again_3);
        result_best_save_3 = findViewById(R.id.result_best_save_3);
    }

    private void getResultSuccess3(String data) {
        if (timerCounter3 != null) {
            timerCounter3.cancel();
        }
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
        try {
            device_result_3.setText(String.valueOf(data));
        } catch (Exception e) {

        }
        Log.d("hailong33", "FHL result is " + data);
        isTesting3 = false;
        testOk3 = true;
        time_counter_3.setText(String.valueOf(currentSeconds_3));
        dismissStatus_3();
        menu_start_label_3.setVisibility(View.GONE);
        handleTestCount3();
    }


    private boolean allowDeleUser3() {
        boolean testing = isTesting3;
        boolean notsave = testOk3;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备3正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备3测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset3();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //保存成绩到数据库
    void handleResultSave3() {
        currentResult_3 = device_result_3.getText().toString();
        if (StringUtils.isEmpty(currentResult_3) || Integer.parseInt(currentResult_3) <= 0) {
            toast("无效测试成绩");
            return;
        }
        Log.d("hailong", " currentResult_3 is " + currentResult_3);
        if (userInfo3 != null) {
            UserModel.updateUserInfoInYangwoqizuo(YangwoqiuzoGroupTestActivity.this, userInfo3, currentResult_3);
            toast("设备3成绩保存成功");
            saveScore3 = true;
            deviceOk3 = true;
            reset3();
        } else {
            ensureMp("invalid_code.ogg", tip);
        }

    }

    void reset3() {
        if (timerCounter3 != null) {
            timerCounter3.cancel();
        }
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
        currentResult_3 = null;
        mHandler.removeCallbacks(resultSuccessRunnable3);
        result_settings_3.setVisibility(View.VISIBLE);
        result_container_3.setVisibility(View.GONE);
        isTesting3 = testComplete3 = testOk3 = false;
        device_result_3.setText("0");
        handleTestCount2();
    }

    void handleSearchResult3() {
        String str = code_et_3.getText().toString().replace(" ", "");
        if (str.isEmpty()) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(YangwoqiuzoGroupTestActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 3, str.length() - 3);
        }
        loadTask3 = new YangwoqiuzoGroupTestActivity.LocalLoadTask3();
        loadTask3.execute(str);
    }

    private void handleTestCount3() {
        isTesting3 = false;
        needTest3 = true;
        clickReceive3 = false;
        ShutDownTimer_3.cancel();
        result_operation_3.setVisibility(View.VISIBLE);
        if (resultCountDownTimer3 != null) {
            resultCountDownTimer3.cancel();
        }
    }


    class LocalLoadTask3 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
                Log.d("hailong33", " show ");
                dataImportDialog.setCancelable(false);
                dataImportDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            //查询信息
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            String search = params[0];
            if (search != null) {
                userInfos = UserModel.queryUserInfoByCode(YangwoqiuzoGroupTestActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg", tip);
//                Toast.makeText(YangwoqiuzoGroupTestActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                showUserInfo3(userInfos.get(0));
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    //设备4数据和视图
    boolean isTesting4;//正在测试中
    boolean testOk4;//测试完毕
    boolean testComplete4;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_4;//输入学生信息
    EditText code_et_4;//输入学号
    View code_delete_4;
    View result_icread_4;//删除输入框学生信息
    View result_ok_4;//确认学生信息
    View test_delete_4;//删除当学学生信息

    //学生信息 展现
    View result_container_4;//学生信息展现区域
    ImageView test_avatar_4;//学生头像
    TextView result_name_4;//学生名字
    TextView result_sex_4;//学生性别
    TextView result_class_4;//学生年级和班级
    TextView result_code_4;//学生学号
    //设备状态检测
    boolean deviceReady4 = true;//设备是否准备好
    TextView device_result_4;//当前设备状态
    TextView device_status_4;//设备连接状态
    View menu_connect_4;//连接外设
    View menu_start_4;//开始测试

    View result_operation_4;//重测 保存 区域
    View result_test_again_4;//重测一次
    View result_best_save_4;//保存成绩

    UserInfo userInfo4;//查询到的学生
    YangwoqiuzoGroupTestActivity.LocalLoadTask4 loadTask4;//查询学生信息

    CountDownTimer connectCountDownTimer4;//连接设备计时器
    CountDownTimer resultCountDownTimer4;//测试计时器

    private static final String Send_Start_Test_4 = "0416";//开始测试4
    private static final String Send_Get_Result_4 = "0414";//获取测试数据4

    private static final String RC_Not_Ready_4 = "04D0";//没准备好
    private static final String RC_Connect_Fail_4 = "004";//连接失败
    private static final String RC_Testing_4 = "04C4";//测试中4
    private static final String RC_Problem_4 = "04C1";//外设故障4
    private static final String RC_Success_4 = "04C5";//测试完毕4
    private static final String RC_Connect_Success_4 = "04D1";//连接成功

    protected void waitResult4() {
        //4，发送0444，hold 400ms
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
        final int seconds = Integer.parseInt(time_counter_4.getText().toString());
        resultCountDownTimer4 = new CountDownTimer((seconds + 1) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong44", " onTick ");
                write(Send_Get_Result_4);
            }

            @Override
            public void onFinish() {
                Log.d("hailong44", "Result onFinish ");
                dismissStatus_4();
                menu_start_label_4.setVisibility(View.GONE);
            }

        };
        resultCountDownTimer4.start();

    }

    void initData4() {
        //设备4
        result_icread_4.setOnClickListener(this);
        result_ok_4.setOnClickListener(this);
        menu_connect_4.setOnClickListener(this);
//        menu_start_4.setOnClickListener(this);
        test_delete_4.setOnClickListener(this);
        result_test_again_4.setOnClickListener(this);
        result_best_save_4.setOnClickListener(this);
    }

    void initView4() {
        //设备4
        result_settings_4 = findViewById(R.id.result_settings_4);
        code_et_4 = (EditText) findViewById(R.id.code_et_4);
        code_delete_4 = findViewById(R.id.code_delete_4);
        code_et_4.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok_4.performClick();
                return false;
            }
        });
        result_icread_4 = findViewById(R.id.result_icread_4);
        result_ok_4 = findViewById(R.id.result_ok_4);
        test_delete_4 = findViewById(R.id.test_delete_4);
        //学生信息 展现
        result_container_4 = findViewById(R.id.result_container_4);
        test_avatar_4 = (ImageView) findViewById(R.id.test_avatar_4);
        result_name_4 = (TextView) findViewById(R.id.result_name_4);
        result_sex_4 = (TextView) findViewById(R.id.result_sex_4);
        result_class_4 = (TextView) findViewById(R.id.result_class_4);
        result_code_4 = (TextView) findViewById(R.id.result_code_4);
        //设备状态检测
        device_result_4 = (TextView) findViewById(R.id.device_result_4);
        device_status_4 = (TextView) findViewById(R.id.device_status_4);
        menu_connect_4 = findViewById(R.id.menu_connect_4);
        menu_start_4 = findViewById(R.id.menu_start_4);
        result_operation_4 = findViewById(R.id.result_operation_4);
        result_test_again_4 = findViewById(R.id.result_test_again_4);
        result_best_save_4 = findViewById(R.id.result_best_save_4);

        code_delete_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_et_1.setText("");
            }
        });
        code_delete_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_et_2.setText("");
            }
        });
        code_delete_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_et_3.setText("");
            }
        });
        code_delete_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_et_4.setText("");
            }
        });
    }

    private void getResultSuccess4(String data) {
        if (timerCounter4 != null) {
            timerCounter4.cancel();
        }
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
        try {
            device_result_4.setText(String.valueOf(data));
        } catch (Exception e) {

        }
        isTesting4 = false;
        testOk4 = true;
        time_counter_4.setText(String.valueOf(currentSeconds_4));
        dismissStatus_4();
        ;
        menu_start_label_4.setVisibility(View.GONE);
        handleTestCount4();
    }


    private boolean allowDeleUser4() {
        boolean testing = isTesting4 && !testOk4 && !testComplete4;
        boolean notsave = !isTesting4 && testOk4 && !testComplete4;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备4正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备4测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(YangwoqiuzoGroupTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset4();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //保存成绩到数据库
    void handleResultSave4() {
        currentResult_4 = device_result_4.getText().toString();
        if (StringUtils.isEmpty(currentResult_4) || Integer.parseInt(currentResult_4) <= 0) {
            toast("无效测试成绩");
            return;
        }
        Log.d("hailong", " currentResult_4 is " + currentResult_4);
        if (userInfo4 != null) {
            UserModel.updateUserInfoInYangwoqizuo(YangwoqiuzoGroupTestActivity.this, userInfo4, currentResult_4);
            toast("设备4成绩保存成功");
            saveScore4 = true;
            deviceOk4 = true;
            reset4();
        } else {
            ensureMp("invalid_code.ogg", tip);
        }

    }

    void reset4() {
        if (timerCounter4 != null) {
            timerCounter4.cancel();
        }
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
        currentResult_4 = null;
        mHandler.removeCallbacks(resultSuccessRunnable4);
        result_settings_4.setVisibility(View.VISIBLE);
        result_container_4.setVisibility(View.GONE);
        isTesting4 = testComplete4 = testOk4 = false;
        device_result_4.setText("0");
        handleTestCount4();
    }

    void handleSearchResult4() {
        String str = code_et_4.getText().toString().replace(" ", "");
        if (str.isEmpty()) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(YangwoqiuzoGroupTestActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 4, str.length() - 4);
        }
        loadTask4 = new YangwoqiuzoGroupTestActivity.LocalLoadTask4();
        loadTask4.execute(str);
    }

    private void handleTestCount4() {
        isTesting4 = false;
        needTest4 = true;
        clickReceive4 = false;
        ShutDownTimer_4.cancel();
        result_operation_4.setVisibility(View.VISIBLE);
        if (resultCountDownTimer4 != null) {
            resultCountDownTimer4.cancel();
        }
    }


    class LocalLoadTask4 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
                Log.d("hailong44", " show ");
                dataImportDialog.setCancelable(false);
                dataImportDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            //查询信息
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            String search = params[0];
            if (search != null) {
                userInfos = UserModel.queryUserInfoByCode(YangwoqiuzoGroupTestActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg", tip);
            } else {
                showUserInfo4(userInfos.get(0));
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

}

