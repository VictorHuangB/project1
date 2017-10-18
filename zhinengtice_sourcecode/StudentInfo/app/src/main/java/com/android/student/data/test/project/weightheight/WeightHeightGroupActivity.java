package com.android.student.data.test.project.weightheight;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.SerialPortActivity;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.util.ArrayList;

/**
 * 身高体重多人测试页面，只有手动成绩播报，还有一些错误提示
 * Created by hailong on 2016/11/28 0028.
 */

public class WeightHeightGroupActivity extends SerialPortActivity {
    //模拟按钮
    View test_a;
    View test_b;
    View test_c;
    View test_d;
    View test_e;
    View test_h;
    View test_k;
    View test_l;
    View test_m;
    View test_n;
    View test_o;
    View test_p;

    View connect_all;
    //数据通用
    Object obj = new Object();
    DataImportDialog dataImportDialog;//数据查询可能较慢，弹出框体验更好
    private static final int Max_Connect_Time = 6000;//等待连接结果的时间
    private static final int Max_Result_Time = 3 * 80 * 1000;//等待结果的时间可能会有点长，这个时间不好控制,暂定两分钟
    private static final int Hold_Time = 800;//300ms发送一次

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_test_group_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        moni();
        initView_1();
        initView_2();
        initView_3();
        initView_4();

        initData_1();
        initData_2();
        initData_3();
        initData_4();
    }

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
                    if (code.startsWith(RC_Success_1)) {
                        String data1 = code.substring(4, 24);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("01C50000000000")) {
                            //正在测试
                            menu_start_icon_1.setVisibility(View.GONE);
                            menu_start_label_1.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            menu_start_icon_1.setVisibility(View.VISIBLE);
                            menu_start_label_1.setVisibility(View.GONE);
                            toast("设备1测试完毕");
                        }
                        currentResult_1 = data;
                        getResult_1(data, false, true);
                    } else if (code.startsWith(RC_Success_2)) {
                        String data1 = code.substring(4, 24);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("02C50000000000")) {
                            //正在测试
                            menu_start_icon_2.setVisibility(View.GONE);
                            menu_start_label_2.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            menu_start_icon_2.setVisibility(View.VISIBLE);
                            menu_start_label_2.setVisibility(View.GONE);
                            toast("设备2测试完毕");
                        }
                        currentResult_2 = data;
                        getResult_2(data, false, true);
                    } else if (code.startsWith(RC_Success_3)) {
                        String data1 = code.substring(4, 24);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("03C50000000000")) {
                            //正在测试
                            menu_start_icon_3.setVisibility(View.GONE);
                            menu_start_label_3.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            menu_start_icon_3.setVisibility(View.VISIBLE);
                            menu_start_label_3.setVisibility(View.GONE);
                            toast("设备3测试完毕");
                        }
                        currentResult_3 = data;
                        getResult_3(data, false, true);
                    } else if (code.startsWith(RC_Success_4)) {
                        String data1 = code.substring(4, 24);
                        //讲ASCII转换成十进制再转换成对应的字符
                        for (int i = 0; i < data1.length(); i += 2) {
                            String convert = data1.substring(i, i + 2);
                            data += AppUtils.asciiToStr(convert);
                        }
                        if (data.startsWith("04C50000000000")) {
                            //正在测试
                            menu_start_icon_4.setVisibility(View.GONE);
                            menu_start_label_4.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            menu_start_icon_4.setVisibility(View.VISIBLE);
                            menu_start_label_4.setVisibility(View.GONE);
                            toast("设备4测试完毕");
                        }
                        currentResult_4 = data;
                        getResult_4(data, false, true);
                    } else {
                        Message message = new Message();
                        message.obj = code.substring(0, 4);
                        mHandler_1.sendMessage(message);
                    }
                }
            }
        });
    }

    void moni() {
        test_a = findViewById(R.id.test_a);
        test_b = findViewById(R.id.test_b);
        test_c = findViewById(R.id.test_c);
        test_d = findViewById(R.id.test_d);
        test_e = findViewById(R.id.test_e);

        test_h = findViewById(R.id.test_h);
        test_k = findViewById(R.id.test_k);
        test_l = findViewById(R.id.test_l);
        test_m = findViewById(R.id.test_m);
        test_n = findViewById(R.id.test_n);
        test_o = findViewById(R.id.test_o);
        test_p = findViewById(R.id.test_p);

        test_a.setOnClickListener(moniListener);
        test_b.setOnClickListener(moniListener);
        test_c.setOnClickListener(moniListener);
        test_d.setOnClickListener(moniListener);
        test_e.setOnClickListener(moniListener);
        test_h.setOnClickListener(moniListener);

        test_k.setOnClickListener(moniListener);
        test_l.setOnClickListener(moniListener);
        test_m.setOnClickListener(moniListener);
        test_n.setOnClickListener(moniListener);
        test_o.setOnClickListener(moniListener);
        test_p.setOnClickListener(moniListener);
    }

    View.OnClickListener moniListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.test_a://1连接成功
                    Message message = new Message();
                    // message.obj = RC_Connect_Success_1;
                    mHandler_1.sendMessage(message);
                    break;
                case R.id.test_b://2连接成功
                    message = new Message();
                    // message.obj = RC_Connect_Success_2;
                    mHandler_2.sendMessage(message);
                    break;
                case R.id.test_c://3连接成功
                    message = new Message();
                    //message.obj = RC_Connect_Success_3;
                    mHandler_3.sendMessage(message);
                    break;
                case R.id.test_d://4连接成功
                    message = new Message();
                    //  message.obj = RC_Connect_Success_4;
                    mHandler_4.sendMessage(message);
                    break;
                case R.id.test_e://1模拟第一次成绩
                    currentResult_1 = "0016000089";
                    getResult_1("0016000089", false, true);
                    break;
                case R.id.test_h://2模拟第一次成绩
                    currentResult_2 = "0018000069";
                    getResult_2("0016000089", false, true);
                    break;
                case R.id.test_k:
                    if (StringUtils.isEmpty(currentResult_1)) {
                        toast("没有测试成绩");
                        return;
                    }
                    getResult_1(currentResult_1, true, false);
                    break;
                case R.id.test_l:
                    if (StringUtils.isEmpty(currentResult_2)) {
                        toast("没有测试成绩");
                        return;
                    }
                    getResult_2(currentResult_2, true, false);
                    break;
                case R.id.test_m:
                    message = new Message();
                    message.obj = RC_Biaoding_Enter_1;
                    mHandler_1.sendMessage(message);
                    break;
                case R.id.test_o:
                    message = new Message();
                    message.obj = RC_Biaoding_first_1;
                    mHandler_1.sendMessage(message);
                    break;
                case R.id.test_p:
                    message = new Message();
                    message.obj = RC_Biaoding_second_1;
                    mHandler_1.sendMessage(message);
                    break;
                case R.id.test_n:
                    message = new Message();
                    message.obj = RC_Biaoding_Enter_2;
                    mHandler_2.sendMessage(message);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        boolean testing1 = isTesting_1 && !testOk_1;
        boolean notsave1 = !isTesting_1 && testOk_1;

        boolean testing2 = isTesting_2 && !testOk_2;
        boolean notsave2 = !isTesting_2 && testOk_2;

        boolean testing3 = isTesting_3 && !testOk_3;
        boolean notsave3 = !isTesting_3 && testOk_3;

        boolean testing4 = isTesting_4 && !testOk_4;
        boolean notsave4 = !isTesting_4 && testOk_4;

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
            final AppDialog appDialog = new AppDialog(WeightHeightGroupActivity.this, "取消", "确定", true);

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

    @Override
    protected void onPause() {
        super.onPause();
        terminal_1();
        terminal_2();
        terminal_3();
        terminal_4();
    }

    void terminal_1() {
        //设备1
        if (connectCountDownTimer_1 != null) {
            connectCountDownTimer_1.cancel();
            connectCountDownTimer_1 = null;
        }
        if (resultCountDownTimer_1 != null) {
            resultCountDownTimer_1.cancel();
            resultCountDownTimer_1 = null;
        }
        if (countDownTimer1_1 != null) {
            countDownTimer1_1.cancel();
            countDownTimer1_1 = null;
        }
        if (countDownTimer2_1 != null) {
            countDownTimer2_1.cancel();
            countDownTimer2_1 = null;
        }
        if (countDownTimer3_1 != null) {
            countDownTimer3_1.cancel();
            countDownTimer3_1 = null;
        }

    }

    FHLHandler_1 mHandler_1 = new FHLHandler_1();
    //设备1数据和视图
    boolean isTesting_1;//正在测试中
    boolean testOk_1;//测试完毕
    boolean testComplete_1;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_1;//输入学生信息
    EditText code_et_1;//输入学号
    View result_clear_1;//删除输入框学生信息
    View result_ok_1;//确认学生信息
    View test_delete_1;//删除当学学生信息

    //学生信息 展现
    View result_container_1;//学生信息展现区域
    ImageView test_avatar_1;//学生头像
    TextView result_name_1;//学生名字
    TextView result_class_1;//学生年级和班级
    TextView result_code_1;//学生学号
    //设备状态检测
    boolean deviceReady_1 = true;//设备是否准备好
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
    EditText biaoding_et_1;//输入标定值
    TextView biaoding_confirm_1;//标定确认
    TextView device_result_height_1;//身高
    TextView device_result_weight_1;//体重

    View result_operation_1;//重测 保存 区域
    View result_test_cancel_1;//重测一次
    View result_best_save_1;//保存成绩

    UserInfo userInfo_1;//查询到的学生
    LocalLoadTask_1 loadTask_1;//查询学生信息

    int playTime_1 = 0;
    boolean biaodingOver_1;
    boolean enterBiaoding_1;
    int biaodingCount_1 = 0;
    boolean biaodingFirst_1 = false;
    boolean biaodingFirst_2 = false;
    boolean biaodingFirst_3 = false;
    boolean biaodingFirst_4 = false;
    String currentResult_1 = "";
    CountDownTimer connectCountDownTimer_1;//连接设备计时器
    CountDownTimer resultCountDownTimer_1;//测试计时器
    CountDownTimer countDownTimer1_1;
    CountDownTimer countDownTimer2_1;
    CountDownTimer countDownTimer3_1;
    ResultThread_1 resultThread_1;


    //连接外设发送数据
//    private static final String Send_Connecting_1 = "0113";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm_1 = "0115";//主机确认外设1状态

    //标定
    private static final String Send_Biaoding_Start_1 = "0118";//发送标定 回 01C8 进入标定
    private static final String Send_Biaoding_first_1 = "0119";//第一次标定,带输入值 回 01C9 完成第一次标定
    private static final String Send_Biaoding_second_1 = "011A";//第二次标定,带输入值  回01CA 完成标定

    private static final String Send_Start_Test_1 = "0116";//开始测试1
    private static final String Send_Get_Result_1 = "0114";//获取测试数据1

    //连接外设1接收数据
//    private static final String RC_Connecting_1 = "01C3";//正在连接1
//    private static final String RC_Not_Ready_1 = "01D0";//没准备好1
//    private static final String RC_Connect_Success_1 = "01D1";//连接成功1

    private static final String RC_Biaoding_Enter_1 = "01C8";//进入标定
    private static final String RC_Biaoding_first_1 = "01C9";//第一次标定结束
    private static final String RC_Biaoding_second_1 = "01CA";//第二次标定结束
    private static final String RC_Testing_1 = "01C4";//测试中1
    private static final String RC_Problem_1 = "01C1";//外设故障1
    private static final String RC_Success_1 = "01C5";//测试完毕1

    class FHLHandler_1 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
                //设备1
//                case RC_Connecting_1:
//                    write(Send_Confirm_1);
//                    break;
//                case RC_Not_Ready_1:
//                    device_status_1.setText("设备没准备好");
//                    break;
//                case RC_Connect_Success_1:
//                    deviceReady_1 = true;
//                    device_status_1.setText("连接成功");
//                    if (connectCountDownTimer_1 != null) {
//                        connectCountDownTimer_1.cancel();
//                    }
//                    toast("设备1连接成功");
//                    if (biaodingCount_1 == 0) {
//                        //直接进入标定
//                        write(Send_Biaoding_Start_1);
//                    }
//                    break;
                case RC_Testing_1:
                    isTesting_1 = true;
                    testOk_1 = false;
                    device_status_1.setText("测试中...");
                    waitResult_1();
                    break;
                case RC_Problem_1:
                    device_status_1.setText("设备故障");
                    toast("设备1出故障了");
                    break;
                case RC_Biaoding_Enter_1:
                    //进入标定
                    enterBiaoding_1 = true;
                    toast("开始标定，请输入第一次标定值");
                    break;
                case RC_Biaoding_first_1:
                    biaodingCount_1++;
                    biaodingFirst_1 = true;
                    biaoding_et_1.setText("");
                    biaoding_et_1.setHint("请再次输入的标定值");
                    biaoding_confirm_1.setText("再次标定");
                    break;
                case RC_Biaoding_second_1:
                    biaodingCount_1++;
                    biaoding_et_1.setHint("标定完毕");
                    biaoding_confirm_1.setText("标定完毕");
                    biaoding_confirm_1.setEnabled(false);
                    biaodingOver_1 = true;
                    toast("标定完毕，请开始测试");
                    break;
            }

        }
    }

    protected void waitResult_1() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer_1 != null) {
            resultCountDownTimer_1.cancel();
        }
        resultCountDownTimer_1 = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_1);
            }

            @Override
            public void onFinish() {
                Log.d("hailong12", "Result onFinish ");
                menu_start_icon_1.setVisibility(View.VISIBLE);
                menu_start_label_1.setVisibility(View.GONE);
                toast("设备1测试超时");
            }

        };
        resultCountDownTimer_1.start();
    }

    void initData_1() {
        connect_all.setOnClickListener(clickListener_1);
        //设备1
        result_clear_1.setOnClickListener(clickListener_1);
        result_ok_1.setOnClickListener(clickListener_1);
        menu_connect_1.setOnClickListener(clickListener_1);
        menu_start_1.setOnClickListener(clickListener_1);
        biaoding_confirm_1.setOnClickListener(clickListener_1);
        test_delete_1.setOnClickListener(clickListener_1);
        result_test_cancel_1.setOnClickListener(clickListener_1);
        result_best_save_1.setOnClickListener(clickListener_1);
    }

    class ResultThread_1 extends Thread {
        String data;
        boolean playVoice;
        boolean uiFresh;

        public ResultThread_1(String data, boolean playVoice, boolean uiFresh) {
            this.data = data;
            this.playVoice = playVoice;
            this.uiFresh = uiFresh;
        }

        private void playWeight(String weightData, final int duration) {
            ensureMp("height_weight.ogg");
            synchronized (resultThread_1) {
                try {
                    wait(1000);
                } catch (Exception e) {

                }
            }
            final ArrayList<String> weightsounds = AppUtils.getSoundList(weightData);
            weightsounds.add("kg.ogg");
            final int size1 = weightsounds.size();

            if (size1 > 0) {
                countDownTimer3_1 = new CountDownTimer((duration) * (size1 + 1), duration) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (playTime_1 < size1) {
                            String path = weightsounds.get(playTime_1);
                            if (!StringUtils.isEmpty(path)) {
                                ensureMp(path);
                                playTime_1++;
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        playTime_1 = 0;
                        countDownTimer3_1 = null;
                    }

                };
                countDownTimer3_1.start();
            }
        }

        @Override
        public void run() {
            super.run();
            if (data == null || data.length() < 5) {
                return;
            }
            final String heightData = data.substring(0, 5);
            final String weightData = data.substring(5, data.length());
            try {
                if (userInfo_1 != null) {
                    userInfo_1.height = String.valueOf(Float.parseFloat(heightData));
                    userInfo_1.weight = String.valueOf(Float.parseFloat(weightData));
                }
            } catch (Exception e) {

            }
            if (uiFresh) {
                resultUi_1(heightData, weightData);
                isTesting_1 = false;
                testOk_1 = true;
            }
            if (playVoice) {
                ensureMp("test_score.ogg");
                synchronized (resultThread_1) {
                    try {
                        wait(2000);
                    } catch (Exception e) {

                    }
                }

                ensureMp("height_height.ogg");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer2_1 = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                final ArrayList<String> heightsounds = AppUtils.getSoundList(heightData);
                                heightsounds.add("cm.ogg");
                                final int size = heightsounds.size();
                                final int duration = 700;
                                if (size > 0) {
                                    countDownTimer1_1 = new CountDownTimer((duration) * (size + 1), duration) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            if (playTime_1 < size) {
                                                String path = heightsounds.get(playTime_1);
                                                if (!StringUtils.isEmpty(path)) {
                                                    ensureMp(path);
                                                    playTime_1++;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            playTime_1 = 0;
                                            synchronized (obj) {
                                                try {
                                                    wait(500);
                                                } catch (Exception e) {

                                                }
                                            }

                                            playWeight(weightData, 700);
                                            countDownTimer1_1 = null;
                                        }


                                    };
                                    countDownTimer1_1.start();
                                }
                                countDownTimer2_1 = null;

                            }


                        };
                        countDownTimer2_1.start();
                    }
                });
            }

        }
    }

    private void resultUi_1(final String heightData, final String weightData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                device_status_1.setText("测试完毕");
                try {
                    device_result_height_1.setText(String.valueOf(Integer.parseInt(heightData)));
                } catch (Exception e) {

                }
                try {
                    float number = Float.parseFloat(weightData);
                    device_result_weight_1.setText(String.valueOf(number));
                } catch (Exception e) {

                }
                result_operation_1.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getResult_1(String data, boolean playVoice, boolean uiFresh) {
        terminal_1();
        playTime_1 = 0;
        resultThread_1 = new ResultThread_1(data, playVoice, uiFresh);
        resultThread_1.start();
    }

    void initView_1() {
        //设备1
        connect_all = findViewById(R.id.connect_all);
        result_settings_1 = findViewById(R.id.result_settings_1);
        code_et_1 = (EditText) findViewById(R.id.code_et_1);
        result_clear_1 = findViewById(R.id.result_clear_1);
        result_ok_1 = findViewById(R.id.result_ok_1);
        test_delete_1 = findViewById(R.id.test_delete_1);
        //学生信息 展现
        result_container_1 = findViewById(R.id.result_container_1);
        test_avatar_1 = (ImageView) findViewById(R.id.test_avatar_1);
        result_name_1 = (TextView) findViewById(R.id.result_name_1);
        result_class_1 = (TextView) findViewById(R.id.result_class_1);
        result_code_1 = (TextView) findViewById(R.id.result_code_1);
        //设备状态检测
        device_status_1 = (TextView) findViewById(R.id.device_status_1);
        menu_connect_1 = findViewById(R.id.menu_connect_1);
        menu_start_1 = findViewById(R.id.menu_start_1);
        biaoding_et_1 = (EditText) findViewById(R.id.biaoding_et_1);
        biaoding_confirm_1 = (TextView) findViewById(R.id.biaoding_confirm_1);
        device_result_height_1 = (TextView) findViewById(R.id.device_result_height_1);
        device_result_weight_1 = (TextView) findViewById(R.id.device_result_weight_1);
        result_operation_1 = findViewById(R.id.result_operation_1);
        result_test_cancel_1 = findViewById(R.id.result_test_cancel_1);
        result_best_save_1 = findViewById(R.id.result_best_save_1);

        menu_start_icon_1 = (ImageView) findViewById(R.id.menu_start_icon_1);
        menu_start_label_1 = (TextView) findViewById(R.id.menu_start_label_1);
        menu_start_icon_2 = (ImageView) findViewById(R.id.menu_start_icon_2);
        menu_start_label_2 = (TextView) findViewById(R.id.menu_start_label_2);
        menu_start_icon_3 = (ImageView) findViewById(R.id.menu_start_icon_3);
        menu_start_label_3 = (TextView) findViewById(R.id.menu_start_label_3);
        menu_start_icon_4 = (ImageView) findViewById(R.id.menu_start_icon_4);
        menu_start_label_4 = (TextView) findViewById(R.id.menu_start_label_4);
    }

    View.OnClickListener clickListener_1 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_all:
                    menu_connect_1.performClick();
                    break;
                //设备1
                case R.id.result_clear_1://清楚输入框信息
                    code_et_1.setText("");
                    break;
                case R.id.test_delete_1://删除学生信息
                    //如果有成绩未保存，提示保存
                    if (allowDeleUser_1()) {
                        result_settings_1.setVisibility(View.VISIBLE);
                        result_container_1.setVisibility(View.GONE);
                    }
                    break;

                case R.id.result_ok_1://确定
                    handleSearchResult_1();
                    break;
                case R.id.menu_start_1://开始测试
                    if (result_container_1.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_1) {
                        toast("请先连接设备再测试");
                        return;
                    }

//                    if (!enterBiaoding_1) {
//                        toast("设备没有标定响应");
//                        return;
//                    }

//                    if (!biaodingOver_1) {
//                        toast("设备未完成标定");
//                        return;
//                    }
                    //如果正在测试中，不允许
                    if (isTesting_1) {
                        toast("正在测试，请稍候");
                        return;
                    }
                    ensureMp("start_test.ogg");
                    device_status_1.setText("测试中...");

                    //开始测试
                    menu_start_icon_1.setVisibility(View.GONE);
                    menu_start_label_1.setVisibility(View.VISIBLE);
                    //开始测试
                    write(Send_Start_Test_1);
                    mHandler_1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            waitResult_1();
                        }
                    }, 3000);
                    break;

                case R.id.menu_connect_1://连接设备
//                    onTestConnect_1();
                    device_status_1.setText("正在连接...");
                    break;

                case R.id.biaoding_confirm_1:
                    if (result_container_1.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_1) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    //如果正在测试中，不允许
                    if (isTesting_1) {
                        toast("正在测试中");
                        return;
                    }

                    if (biaodingCount_1 >= 2) {
                        toast("最多标定2次");
                        return;
                    }

                    if (!biaodingOver_1 && biaodingCount_1 == 0) {
                        //直接进入标定
                        write(Send_Biaoding_Start_1);
                        return;
                    }
                    String biaoding = biaoding_et_1.getText().toString();
                    boolean write = false;
                    if (biaodingCount_1 == 0) {
                        write = true;
                        write(Send_Biaoding_first_1 + biaoding);
                    } else if (biaodingFirst_1 && biaodingCount_1 == 1) {
                        write = true;
                        write(Send_Biaoding_second_1 + biaoding);
                    }
                    if (write) {
                        biaodingCount_1++;
                    }
                    if (biaodingCount_1 == 2) {
                        biaodingCount_1 = 0;
                    }
                    break;

                case R.id.result_test_cancel_1://重新测试
                    //取消成绩
                    test_delete_1.performClick();
                    break;
                case R.id.result_best_save_1:
                    //保存数据
                    if (userInfo_1 != null) {
                        if (StringUtils.isEmpty(userInfo_1.height) || StringUtils.isEmpty(userInfo_1.weight)) {
                            toast("没有测试数据");
                        } else {
                            UserModel.updateUserInfoInHeightAndWeight(WeightHeightGroupActivity.this, userInfo_1, userInfo_1.height, userInfo_1.weight);
                            toast("保存成功");
                            reset_1();
                        }
                    }
                    break;
            }
        }
    };

    private boolean allowDeleUser_1() {
        boolean testing = isTesting_1;
        boolean notsave = testOk_1;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备1正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备1测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightGroupActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset_1();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

//    protected void onTestConnect_1() {
//        //1，发送0113，hold 300ms
//        if (connectCountDownTimer_1 != null) {
//            connectCountDownTimer_1.cancel();
//        }
//        connectCountDownTimer_1 = new CountDownTimer(Max_Connect_Time, Hold_Time) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                Log.d("hailong13", " onTick ");
//                write(Send_Connecting_1);
//            }
//
//            @Override
//            public void onFinish() {
//                Log.d("hailong14", " onFinish ");
//                if (!deviceReady_1) {
//                    if (mOutputStream == null) {
//                        toast("数据无法通信，请保证设备有读写权限");
//                    } else {
//                        toast("设备1连接失败");
//                    }
//                    device_status_1.setText("连接失败");
//                }
//            }
//
//        };
//        connectCountDownTimer_1.start();
//    }

    //任务独立完成，互不干扰
    class LocalLoadTask_1 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
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
                userInfos = UserModel.queryUserInfoByCode(WeightHeightGroupActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg");
//                Toast.makeText(WeightHeightGroupActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                userInfo_1 = userInfos.get(0);
                result_settings_1.setVisibility(View.GONE);
                result_container_1.setVisibility(View.VISIBLE);
                try {
                    if (StringUtils.isEmpty(userInfo_1.avater_label)) {
                        test_avatar_1.setImageBitmap(MainApplication.defaultBitmap);
                    } else {
                        test_avatar_1.setImageBitmap(BitmapFactory.decodeFile(userInfo_1.avater_label));
                    }
                } catch (Exception e) {
                    test_avatar_1.setImageBitmap(MainApplication.defaultBitmap);
                }
                result_name_1.setText(userInfo_1.name);
                result_class_1.setText(userInfo_1.classes + userInfo_1.grade);
                result_code_1.setText(userInfo_1.code);

            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void handleTestCount_1() {
        if (userInfo_1 != null && !StringUtils.isEmpty(userInfo_1.weight)) {
            device_status_1.setText("测试完毕");
            result_operation_1.setVisibility(View.VISIBLE);
            if (resultCountDownTimer_1 != null) {
                resultCountDownTimer_1.cancel();
            }
        } else {
            result_operation_1.setVisibility(View.GONE);
        }

    }

    void reset_1() {
        result_settings_1.setVisibility(View.VISIBLE);
        result_container_1.setVisibility(View.GONE);
        biaoding_et_1.setText("");
        biaoding_et_1.setHint("标定完毕");
        biaoding_confirm_1.setText("开始标定");
        biaoding_confirm_1.setEnabled(true);
        userInfo_1 = null;
        currentResult_1 = "";
        biaodingCount_1 = 0;
        biaodingOver_1 = false;
        enterBiaoding_1 = false;
        biaodingFirst_1 = false;
        isTesting_1 = testComplete_1 = testOk_1 = false;
        device_result_height_1.setText("0");
        device_result_weight_1.setText("0");
        handleTestCount_1();
    }

    void handleSearchResult_1() {
        String str = code_et_1.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg");
            return;
        }
        dataImportDialog = new DataImportDialog(WeightHeightGroupActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
        }
        if (str.length() < 2) {
            Toast.makeText(WeightHeightGroupActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        loadTask_1 = new LocalLoadTask_1();
        loadTask_1.execute(str);
    }


    void terminal_2() {
        //设备1
        if (connectCountDownTimer_2 != null) {
            connectCountDownTimer_2.cancel();
            connectCountDownTimer_2 = null;
        }
        if (resultCountDownTimer_2 != null) {
            resultCountDownTimer_2.cancel();
            resultCountDownTimer_2 = null;
        }
        if (countDownTimer1_2 != null) {
            countDownTimer1_2.cancel();
            countDownTimer1_2 = null;
        }
        if (countDownTimer2_2 != null) {
            countDownTimer2_2.cancel();
            countDownTimer2_2 = null;
        }
        if (countDownTimer3_2 != null) {
            countDownTimer3_2.cancel();
            countDownTimer3_2 = null;
        }

    }

    FHLHandler_2 mHandler_2 = new FHLHandler_2();
    //设备1数据和视图
    boolean isTesting_2;//正在测试中
    boolean testOk_2;//测试完毕
    boolean testComplete_2;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_2;//输入学生信息
    EditText code_et_2;//输入学号
    View result_clear_2;//删除输入框学生信息
    View result_ok_2;//确认学生信息
    View test_delete_2;//删除当学学生信息

    //学生信息 展现
    View result_container_2;//学生信息展现区域
    ImageView test_avatar_2;//学生头像
    TextView result_name_2;//学生名字
    TextView result_class_2;//学生年级和班级
    TextView result_code_2;//学生学号
    //设备状态检测
    boolean deviceReady_2 = true;//设备是否准备好
    TextView device_status_2;//设备连接状态
    View menu_connect_2;//连接外设
    View menu_start_2;//开始测试
    EditText biaoding_et_2;//输入标定值
    TextView biaoding_confirm_2;//标定确认
    TextView device_result_height_2;//身高
    TextView device_result_weight_2;//体重

    View result_operation_2;//重测 保存 区域
    View result_test_cancel_2;//重测一次
    View result_best_save_2;//保存成绩

    UserInfo userInfo_2;//查询到的学生
    LocalLoadTask_2 loadTask_2;//查询学生信息

    int playTime_2 = 0;
    boolean biaodingOver_2;
    boolean enterBiaoding_2;
    int biaodingCount_2 = 0;
    String currentResult_2 = "";
    CountDownTimer connectCountDownTimer_2;//连接设备计时器
    CountDownTimer resultCountDownTimer_2;//测试计时器
    CountDownTimer countDownTimer1_2;
    CountDownTimer countDownTimer2_2;
    CountDownTimer countDownTimer3_2;
    ResultThread_2 resultThread_2;


    //连接外设发送数据
//    private static final String Send_Connecting_2 = "0213";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm_2 = "0215";//主机确认外设状态

    //标定
    private static final String Send_Biaoding_Start_2 = "0218";//发送标定 回 02C8 进入标定
    private static final String Send_Biaoding_first_2 = "0219";//第一次标定,带输入值 回 02C9 完成第一次标定
    private static final String Send_Biaoding_second_2 = "021A";//第二次标定,带输入值  回02CA 完成标定

    private static final String Send_Start_Test_2 = "0216";//开始测试
    private static final String Send_Get_Result_2 = "0214";//获取测试数据

    //连接外设1接收数据
//    private static final String RC_Connecting_2 = "02C3";//正在连接
//    private static final String RC_Not_Ready_2 = "02D0";//没准备好
//    private static final String RC_Connect_Success_2 = "02D1";//连接成功

    private static final String RC_Biaoding_Enter_2 = "02C8";//进入标定
    private static final String RC_Biaoding_first_2 = "02C9";//第一次标定结束
    private static final String RC_Biaoding_second_2 = "02CA";//第二次标定结束
    private static final String RC_Testing_2 = "02C4";//测试中
    private static final String RC_Problem_2 = "02C1";//外设故障
    private static final String RC_Success_2 = "02C5";//测试完毕

    class FHLHandler_2 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
                //设备1
//                case RC_Connecting_2:
//                    write(Send_Confirm_2);
//                    break;
//                case RC_Not_Ready_2:
//                    device_status_2.setText("设备没准备好");
//                    break;
//                case RC_Connect_Success_2:
//                    deviceReady_2 = true;
//                    device_status_2.setText("连接成功");
//                    if (connectCountDownTimer_2 != null) {
//                        connectCountDownTimer_2.cancel();
//                    }
//                    toast("设备1连接成功");
//                    if (biaodingCount_2 == 0) {
//                        //直接进入标定
//                        write(Send_Biaoding_Start_2);
//                    }
//                    break;
                case RC_Testing_2:
                    isTesting_2 = true;
                    testOk_2 = false;
                    device_status_2.setText("测试中...");
                    waitResult_2();
                    break;
                case RC_Problem_2:
                    device_status_2.setText("设备故障");
                    toast("设备1出故障了");
                    break;
                case RC_Biaoding_Enter_2:
                    //进入标定
                    enterBiaoding_2 = true;
                    toast("开始标定，请输入第一次标定值");
                    break;
                case RC_Biaoding_first_2:
                    biaodingCount_2++;
                    biaodingFirst_2 = true;
                    biaoding_et_2.setText("");
                    biaoding_et_2.setHint("请再次输入的标定值");
                    biaoding_confirm_2.setText("再次标定");
                    break;
                case RC_Biaoding_second_2:
                    biaodingCount_2++;
                    biaoding_et_2.setHint("标定完毕");
                    biaoding_confirm_2.setText("标定完毕");
                    biaoding_confirm_2.setEnabled(false);
                    biaodingOver_2 = true;
                    toast("标定完毕，请开始测试");
                    break;
            }

        }
    }

    protected void waitResult_2() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer_2 != null) {
            resultCountDownTimer_2.cancel();
        }
        resultCountDownTimer_2 = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_2);
            }

            @Override
            public void onFinish() {
                menu_start_icon_2.setVisibility(View.VISIBLE);
                menu_start_label_2.setVisibility(View.GONE);
                toast("设备2测试超时");
            }

        };
        resultCountDownTimer_2.start();
    }

    void initData_2() {
        connect_all.setOnClickListener(clickListener_2);
        //设备1
        result_clear_2.setOnClickListener(clickListener_2);
        result_ok_2.setOnClickListener(clickListener_2);
        menu_connect_2.setOnClickListener(clickListener_2);
        menu_start_2.setOnClickListener(clickListener_2);
        biaoding_confirm_2.setOnClickListener(clickListener_2);
        test_delete_2.setOnClickListener(clickListener_2);
        result_test_cancel_2.setOnClickListener(clickListener_2);
        result_best_save_2.setOnClickListener(clickListener_2);
    }

    class ResultThread_2 extends Thread {
        String data;
        boolean playVoice;
        boolean uiFresh;

        public ResultThread_2(String data, boolean playVoice, boolean uiFresh) {
            this.data = data;
            this.playVoice = playVoice;
            this.uiFresh = uiFresh;
        }

        private void playWeight(String weightData, final int duration) {
            ensureMp("height_weight.ogg");
            synchronized (resultThread_2) {
                try {
                    wait(1000);
                } catch (Exception e) {

                }
            }
            final ArrayList<String> weightsounds = AppUtils.getSoundList(weightData);
            weightsounds.add("kg.ogg");
            final int size1 = weightsounds.size();

            if (size1 > 0) {
                countDownTimer3_2 = new CountDownTimer((duration) * (size1 + 1), duration) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (playTime_2 < size1) {
                            String path = weightsounds.get(playTime_2);
                            if (!StringUtils.isEmpty(path)) {
                                ensureMp(path);
                                playTime_2++;
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        playTime_2 = 0;
                        countDownTimer3_2 = null;
                    }

                };
                countDownTimer3_2.start();
            }
        }

        @Override
        public void run() {
            super.run();
            if (data == null || data.length() < 5) {
                return;
            }
            final String heightData = data.substring(0, 5);
            final String weightData = data.substring(5, data.length());
            try {
                if (userInfo_2 != null) {
                    userInfo_2.height = String.valueOf(Float.parseFloat(heightData));
                    userInfo_2.weight = String.valueOf(Float.parseFloat(weightData));
                }
            } catch (Exception e) {

            }
            if (uiFresh) {
                resultUi_2(heightData, weightData);
                isTesting_2 = false;
                testOk_2 = true;
            }
            if (playVoice) {
                ensureMp("test_score.ogg");
                synchronized (resultThread_2) {
                    try {
                        wait(2000);
                    } catch (Exception e) {

                    }
                }

                ensureMp("height_height.ogg");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer2_2 = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                final ArrayList<String> heightsounds = AppUtils.getSoundList(heightData);
                                heightsounds.add("cm.ogg");
                                final int size = heightsounds.size();
                                final int duration = 700;
                                if (size > 0) {
                                    countDownTimer1_2 = new CountDownTimer((duration) * (size + 1), duration) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            if (playTime_2 < size) {
                                                String path = heightsounds.get(playTime_2);
                                                if (!StringUtils.isEmpty(path)) {
                                                    ensureMp(path);
                                                    playTime_2++;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            playTime_2 = 0;
                                            synchronized (obj) {
                                                try {
                                                    wait(500);
                                                } catch (Exception e) {

                                                }
                                            }

                                            playWeight(weightData, 700);
                                            countDownTimer1_2 = null;
                                        }


                                    };
                                    countDownTimer1_2.start();
                                }
                                countDownTimer2_2 = null;

                            }


                        };
                        countDownTimer2_2.start();
                    }
                });
            }

        }
    }

    private void resultUi_2(final String heightData, final String weightData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                device_status_2.setText("测试完毕");
                try {
                    device_result_height_2.setText(String.valueOf(Integer.parseInt(heightData)));
                } catch (Exception e) {

                }
                try {
                    float number = Float.parseFloat(weightData);
                    device_result_weight_2.setText(String.valueOf(number));
                } catch (Exception e) {

                }
                result_operation_2.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getResult_2(String data, boolean playVoice, boolean uiFresh) {
        terminal_2();
        playTime_2 = 0;
        resultThread_2 = new ResultThread_2(data, playVoice, uiFresh);
        resultThread_2.start();
    }

    void initView_2() {
        //设备1
        connect_all = findViewById(R.id.connect_all);
        result_settings_2 = findViewById(R.id.result_settings_2);
        code_et_2 = (EditText) findViewById(R.id.code_et_2);
        result_clear_2 = findViewById(R.id.result_clear_2);
        result_ok_2 = findViewById(R.id.result_ok_2);
        test_delete_2 = findViewById(R.id.test_delete_2);
        //学生信息 展现
        result_container_2 = findViewById(R.id.result_container_2);
        test_avatar_2 = (ImageView) findViewById(R.id.test_avatar_2);
        result_name_2 = (TextView) findViewById(R.id.result_name_2);
        result_class_2 = (TextView) findViewById(R.id.result_class_2);
        result_code_2 = (TextView) findViewById(R.id.result_code_2);
        //设备状态检测
        device_status_2 = (TextView) findViewById(R.id.device_status_2);
        menu_connect_2 = findViewById(R.id.menu_connect_2);
        menu_start_2 = findViewById(R.id.menu_start_2);
        biaoding_et_2 = (EditText) findViewById(R.id.biaoding_et_2);
        biaoding_confirm_2 = (TextView) findViewById(R.id.biaoding_confirm_2);
        device_result_height_2 = (TextView) findViewById(R.id.device_result_height_2);
        device_result_weight_2 = (TextView) findViewById(R.id.device_result_weight_2);
        result_operation_2 = findViewById(R.id.result_operation_2);
        result_test_cancel_2 = findViewById(R.id.result_test_cancel_2);
        result_best_save_2 = findViewById(R.id.result_best_save_2);
    }

    View.OnClickListener clickListener_2 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_all:
                    menu_connect_2.performClick();
                    break;
                //设备1
                case R.id.result_clear_2://清楚输入框信息
                    code_et_2.setText("");
                    break;
                case R.id.test_delete_2://删除学生信息
                    //如果有成绩未保存，提示保存
                    if (allowDeleUser_2()) {
                        result_settings_2.setVisibility(View.VISIBLE);
                        result_container_2.setVisibility(View.GONE);
                    }
                    break;

                case R.id.result_ok_2://确定
                    handleSearchResult_2();
                    break;
                case R.id.menu_start_2://开始测试
                    if (result_container_2.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_2) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    if (!enterBiaoding_2) {
                        toast("设备没有标定响应");
                        return;
                    }

                    if (!biaodingOver_2) {
                        toast("设备未完成标定");
                        return;
                    }
                    //如果正在测试中，不允许
                    if (isTesting_2) {
                        toast("正在测试，请稍候");
                        return;
                    }
                    ensureMp("start_test.ogg");
                    device_status_2.setText("测试中...");
                    //开始测试
                    write(Send_Start_Test_2);
                    break;

                case R.id.menu_connect_2://连接设备
//                    onTestConnect_2();
                    device_status_2.setText("正在连接...");
                    break;

                case R.id.biaoding_confirm_2:
                    if (result_container_2.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_2) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    //如果正在测试中，不允许
                    if (isTesting_2) {
                        toast("正在测试中");
                        return;
                    }

                    if (biaodingCount_2 >= 2) {
                        toast("最多标定2次");
                        return;
                    }

                    if (!biaodingOver_2 && biaodingCount_2 == 0) {
                        //直接进入标定
                        write(Send_Biaoding_Start_2);
                        return;
                    }
                    String biaoding = biaoding_et_2.getText().toString();
                    boolean write = false;
                    if (biaodingCount_2 == 0) {
                        write = true;
                        write(Send_Biaoding_first_2 + biaoding);
                    } else if (biaodingFirst_2 && biaodingCount_2 == 1) {
                        write = true;
                        write(Send_Biaoding_second_2 + biaoding);
                    }
                    if (write) {
                        biaodingCount_2++;
                    }
                    if (biaodingCount_2 == 2) {
                        biaodingCount_2 = 0;
                    }
                    break;

                case R.id.result_test_cancel_2://重新测试
                    //取消成绩
                    test_delete_2.performClick();
                    break;
                case R.id.result_best_save_2:
                    //保存数据
                    if (userInfo_2 != null) {
                        if (StringUtils.isEmpty(userInfo_2.height) || StringUtils.isEmpty(userInfo_2.weight)) {
                            toast("没有测试数据");
                        } else {
                            UserModel.updateUserInfoInHeightAndWeight(WeightHeightGroupActivity.this, userInfo_2, userInfo_2.height, userInfo_2.weight);
                            toast("保存成功");
                            reset_2();
                        }
                    }
                    break;
            }
        }
    };

    private boolean allowDeleUser_2() {
        boolean testing = isTesting_2;
        boolean notsave = testOk_2;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备1正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备1测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightGroupActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset_2();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }


    //任务独立完成，互不干扰
    class LocalLoadTask_2 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
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
                userInfos = UserModel.queryUserInfoByCode(WeightHeightGroupActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg");
//                Toast.makeText(WeightHeightGroupActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                userInfo_2 = userInfos.get(0);
                result_settings_2.setVisibility(View.GONE);
                result_container_2.setVisibility(View.VISIBLE);
                try {
                    if (StringUtils.isEmpty(userInfo_2.avater_label)) {
                        test_avatar_2.setImageBitmap(MainApplication.defaultBitmap);
                    } else {
                        test_avatar_2.setImageBitmap(BitmapFactory.decodeFile(userInfo_2.avater_label));
                    }
                } catch (Exception e) {
                    test_avatar_2.setImageBitmap(MainApplication.defaultBitmap);
                }
                result_name_2.setText(userInfo_2.name);
                result_class_2.setText(userInfo_2.classes + userInfo_2.grade);
                result_code_2.setText(userInfo_2.code);

            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void handleTestCount_2() {
        if (userInfo_2 != null && !StringUtils.isEmpty(userInfo_2.weight)) {
            device_status_2.setText("测试完毕");
            result_operation_2.setVisibility(View.VISIBLE);
            if (resultCountDownTimer_2 != null) {
                resultCountDownTimer_2.cancel();
            }
        } else {
            result_operation_2.setVisibility(View.GONE);
        }

    }

    void reset_2() {
        result_settings_2.setVisibility(View.VISIBLE);
        result_container_2.setVisibility(View.GONE);
        biaoding_et_2.setText("");
        biaoding_et_2.setHint("标定完毕");
        biaoding_confirm_2.setText("开始标定");
        biaoding_confirm_2.setEnabled(true);
        userInfo_2 = null;
        currentResult_2 = "";
        biaodingCount_2 = 0;
        biaodingOver_2 = false;
        enterBiaoding_2 = false;
        biaodingFirst_2 = false;
        isTesting_2 = testComplete_2 = testOk_2 = false;
        device_result_height_2.setText("0");
        device_result_weight_2.setText("0");
        handleTestCount_2();
    }

    void handleSearchResult_2() {
        String str = code_et_2.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg");
            return;
        }
        dataImportDialog = new DataImportDialog(WeightHeightGroupActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
        }
        if (str.length() < 2) {
            Toast.makeText(WeightHeightGroupActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        loadTask_2 = new LocalLoadTask_2();
        loadTask_2.execute(str);
    }

    void terminal_3() {
        //设备1
        if (connectCountDownTimer_3 != null) {
            connectCountDownTimer_3.cancel();
            connectCountDownTimer_3 = null;
        }
        if (resultCountDownTimer_3 != null) {
            resultCountDownTimer_3.cancel();
            resultCountDownTimer_3 = null;
        }
        if (countDownTimer1_3 != null) {
            countDownTimer1_3.cancel();
            countDownTimer1_3 = null;
        }
        if (countDownTimer2_3 != null) {
            countDownTimer2_3.cancel();
            countDownTimer2_3 = null;
        }
        if (countDownTimer3_3 != null) {
            countDownTimer3_3.cancel();
            countDownTimer3_3 = null;
        }

    }

    FHLHandler_3 mHandler_3 = new FHLHandler_3();
    //设备1数据和视图
    boolean isTesting_3;//正在测试中
    boolean testOk_3;//测试完毕
    boolean testComplete_3;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_3;//输入学生信息
    EditText code_et_3;//输入学号
    View result_clear_3;//删除输入框学生信息
    View result_ok_3;//确认学生信息
    View test_delete_3;//删除当学学生信息

    //学生信息 展现
    View result_container_3;//学生信息展现区域
    ImageView test_avatar_3;//学生头像
    TextView result_name_3;//学生名字
    TextView result_class_3;//学生年级和班级
    TextView result_code_3;//学生学号
    //设备状态检测
    boolean deviceReady_3 = true;//设备是否准备好
    TextView device_status_3;//设备连接状态
    View menu_connect_3;//连接外设
    View menu_start_3;//开始测试
    EditText biaoding_et_3;//输入标定值
    TextView biaoding_confirm_3;//标定确认
    TextView device_result_height_3;//身高
    TextView device_result_weight_3;//体重

    View result_operation_3;//重测 保存 区域
    View result_test_cancel_3;//重测一次
    View result_best_save_3;//保存成绩

    UserInfo userInfo_3;//查询到的学生
    LocalLoadTask_3 loadTask_3;//查询学生信息

    int playTime_3 = 0;
    boolean biaodingOver_3;
    boolean enterBiaoding_3;
    int biaodingCount_3 = 0;
    String currentResult_3 = "";
    CountDownTimer connectCountDownTimer_3;//连接设备计时器
    CountDownTimer resultCountDownTimer_3;//测试计时器
    CountDownTimer countDownTimer1_3;
    CountDownTimer countDownTimer2_3;
    CountDownTimer countDownTimer3_3;
    ResultThread_3 resultThread_3;


    //连接外设发送数据
//    private static final String Send_Connecting_3 = "0313";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm_3 = "0315";//主机确认外设状态

    //标定
    private static final String Send_Biaoding_Start_3 = "0318";//发送标定 回 03C8 进入标定
    private static final String Send_Biaoding_first_3 = "0319";//第一次标定,带输入值 回 03C9 完成第一次标定
    private static final String Send_Biaoding_second_3 = "031A";//第二次标定,带输入值  回03CA 完成标定

    private static final String Send_Start_Test_3 = "0316";//开始测试
    private static final String Send_Get_Result_3 = "0314";//获取测试数据

    //连接外设1接收数据
//    private static final String RC_Connecting_3 = "03C3";//正在连接
//    private static final String RC_Not_Ready_3 = "03D0";//没准备好
//    private static final String RC_Connect_Success_3 = "03D1";//连接成功

    private static final String RC_Biaoding_Enter_3 = "03C8";//进入标定
    private static final String RC_Biaoding_first_3 = "03C9";//第一次标定结束
    private static final String RC_Biaoding_second_3 = "03CA";//第二次标定结束
    private static final String RC_Testing_3 = "03C4";//测试中
    private static final String RC_Problem_3 = "03C1";//外设故障
    private static final String RC_Success_3 = "03C5";//测试完毕

    class FHLHandler_3 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
//                //设备1
//                case RC_Connecting_3:
//                    write(Send_Confirm_3);
//                    break;
//                case RC_Not_Ready_3:
//                    device_status_3.setText("设备没准备好");
//                    break;
//                case RC_Connect_Success_3:
//                    deviceReady_3 = true;
//                    device_status_3.setText("连接成功");
//                    if (connectCountDownTimer_3 != null) {
//                        connectCountDownTimer_3.cancel();
//                    }
//                    toast("设备1连接成功");
//                    if (biaodingCount_3 == 0) {
//                        //直接进入标定
//                        write(Send_Biaoding_Start_3);
//                    }
//                    break;
                case RC_Testing_3:
                    isTesting_3 = true;
                    testOk_3 = false;
                    device_status_3.setText("测试中...");
                    waitResult_3();
                    break;
                case RC_Problem_3:
                    device_status_3.setText("设备故障");
                    toast("设备1出故障了");
                    break;
                case RC_Biaoding_Enter_3:
                    //进入标定
                    enterBiaoding_3 = true;
                    toast("开始标定，请输入第一次标定值");
                    break;
                case RC_Biaoding_first_3:
                    biaodingCount_3++;
                    biaodingFirst_3 = true;
                    biaoding_et_3.setText("");
                    biaoding_et_3.setHint("请再次输入的标定值");
                    biaoding_confirm_3.setText("再次标定");
                    break;
                case RC_Biaoding_second_3:
                    biaodingCount_3++;
                    biaoding_et_3.setHint("标定完毕");
                    biaoding_confirm_3.setText("标定完毕");
                    biaoding_confirm_3.setEnabled(false);
                    biaodingOver_3 = true;
                    toast("标定完毕，请开始测试");
                    break;
            }

        }
    }

    protected void waitResult_3() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer_3 != null) {
            resultCountDownTimer_3.cancel();
        }
        resultCountDownTimer_3 = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_3);
            }

            @Override
            public void onFinish() {
                menu_start_icon_3.setVisibility(View.VISIBLE);
                menu_start_label_3.setVisibility(View.GONE);
                toast("设备3测试超时");
            }

        };
        resultCountDownTimer_3.start();
    }

    void initData_3() {
        connect_all.setOnClickListener(clickListener_3);
        //设备1
        result_clear_3.setOnClickListener(clickListener_3);
        result_ok_3.setOnClickListener(clickListener_3);
        menu_connect_3.setOnClickListener(clickListener_3);
        menu_start_3.setOnClickListener(clickListener_3);
        biaoding_confirm_3.setOnClickListener(clickListener_3);
        test_delete_3.setOnClickListener(clickListener_3);
        result_test_cancel_3.setOnClickListener(clickListener_3);
        result_best_save_3.setOnClickListener(clickListener_3);
    }

    class ResultThread_3 extends Thread {
        String data;
        boolean playVoice;
        boolean uiFresh;

        public ResultThread_3(String data, boolean playVoice, boolean uiFresh) {
            this.data = data;
            this.playVoice = playVoice;
            this.uiFresh = uiFresh;
        }

        private void playWeight(String weightData, final int duration) {
            ensureMp("height_weight.ogg");
            synchronized (resultThread_3) {
                try {
                    wait(1000);
                } catch (Exception e) {

                }
            }
            final ArrayList<String> weightsounds = AppUtils.getSoundList(weightData);
            weightsounds.add("kg.ogg");
            final int size1 = weightsounds.size();

            if (size1 > 0) {
                countDownTimer3_3 = new CountDownTimer((duration) * (size1 + 1), duration) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (playTime_3 < size1) {
                            String path = weightsounds.get(playTime_3);
                            if (!StringUtils.isEmpty(path)) {
                                ensureMp(path);
                                playTime_3++;
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        playTime_3 = 0;
                        countDownTimer3_3 = null;
                    }

                };
                countDownTimer3_3.start();
            }
        }

        @Override
        public void run() {
            super.run();
            if (data == null || data.length() < 5) {
                return;
            }
            final String heightData = data.substring(0, 5);
            final String weightData = data.substring(5, data.length());
            try {
                if (userInfo_3 != null) {
                    userInfo_3.height = String.valueOf(Float.parseFloat(heightData));
                    userInfo_3.weight = String.valueOf(Float.parseFloat(weightData));
                }
            } catch (Exception e) {

            }
            if (uiFresh) {
                resultUi_3(heightData, weightData);
                isTesting_3 = false;
                testOk_3 = true;
            }
            if (playVoice) {
                ensureMp("test_score.ogg");
                synchronized (resultThread_3) {
                    try {
                        wait(2000);
                    } catch (Exception e) {

                    }
                }

                ensureMp("height_height.ogg");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer2_3 = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                final ArrayList<String> heightsounds = AppUtils.getSoundList(heightData);
                                heightsounds.add("cm.ogg");
                                final int size = heightsounds.size();
                                final int duration = 700;
                                if (size > 0) {
                                    countDownTimer1_3 = new CountDownTimer((duration) * (size + 1), duration) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            if (playTime_3 < size) {
                                                String path = heightsounds.get(playTime_3);
                                                if (!StringUtils.isEmpty(path)) {
                                                    ensureMp(path);
                                                    playTime_3++;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            playTime_3 = 0;
                                            synchronized (obj) {
                                                try {
                                                    wait(500);
                                                } catch (Exception e) {

                                                }
                                            }

                                            playWeight(weightData, 700);
                                            countDownTimer1_3 = null;
                                        }


                                    };
                                    countDownTimer1_3.start();
                                }
                                countDownTimer2_3 = null;

                            }


                        };
                        countDownTimer2_3.start();
                    }
                });
            }

        }
    }

    private void resultUi_3(final String heightData, final String weightData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                device_status_3.setText("测试完毕");
                try {
                    device_result_height_3.setText(String.valueOf(Integer.parseInt(heightData)));
                } catch (Exception e) {

                }
                try {
                    float number = Float.parseFloat(weightData);
                    device_result_weight_3.setText(String.valueOf(number));
                } catch (Exception e) {

                }
                result_operation_3.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getResult_3(String data, boolean playVoice, boolean uiFresh) {
        terminal_3();
        playTime_3 = 0;
        resultThread_3 = new ResultThread_3(data, playVoice, uiFresh);
        resultThread_3.start();
    }

    void initView_3() {
        //设备1
        connect_all = findViewById(R.id.connect_all);
        result_settings_3 = findViewById(R.id.result_settings_3);
        code_et_3 = (EditText) findViewById(R.id.code_et_3);
        result_clear_3 = findViewById(R.id.result_clear_3);
        result_ok_3 = findViewById(R.id.result_ok_3);
        test_delete_3 = findViewById(R.id.test_delete_3);
        //学生信息 展现
        result_container_3 = findViewById(R.id.result_container_3);
        test_avatar_3 = (ImageView) findViewById(R.id.test_avatar_3);
        result_name_3 = (TextView) findViewById(R.id.result_name_3);
        result_class_3 = (TextView) findViewById(R.id.result_class_3);
        result_code_3 = (TextView) findViewById(R.id.result_code_3);
        //设备状态检测
        device_status_3 = (TextView) findViewById(R.id.device_status_3);
        menu_connect_3 = findViewById(R.id.menu_connect_3);
        menu_start_3 = findViewById(R.id.menu_start_3);
        biaoding_et_3 = (EditText) findViewById(R.id.biaoding_et_3);
        biaoding_confirm_3 = (TextView) findViewById(R.id.biaoding_confirm_3);
        device_result_height_3 = (TextView) findViewById(R.id.device_result_height_3);
        device_result_weight_3 = (TextView) findViewById(R.id.device_result_weight_3);
        result_operation_3 = findViewById(R.id.result_operation_3);
        result_test_cancel_3 = findViewById(R.id.result_test_cancel_3);
        result_best_save_3 = findViewById(R.id.result_best_save_3);
    }

    View.OnClickListener clickListener_3 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_all:
                    menu_connect_3.performClick();
                    break;
                //设备1
                case R.id.result_clear_3://清楚输入框信息
                    code_et_3.setText("");
                    break;
                case R.id.test_delete_3://删除学生信息
                    //如果有成绩未保存，提示保存
                    if (allowDeleUser_3()) {
                        result_settings_3.setVisibility(View.VISIBLE);
                        result_container_3.setVisibility(View.GONE);
                    }
                    break;

                case R.id.result_ok_3://确定
                    handleSearchResult_3();
                    break;
                case R.id.menu_start_3://开始测试
                    if (result_container_3.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_3) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    if (!enterBiaoding_3) {
                        toast("设备没有标定响应");
                        return;
                    }

                    if (!biaodingOver_3) {
                        toast("设备未完成标定");
                        return;
                    }
                    //如果正在测试中，不允许
                    if (isTesting_3) {
                        toast("正在测试，请稍候");
                        return;
                    }
                    ensureMp("start_test.ogg");
                    device_status_3.setText("测试中...");
                    //开始测试
                    write(Send_Start_Test_3);
                    break;

                case R.id.menu_connect_3://连接设备
//                    onTestConnect_3();
                    device_status_3.setText("正在连接...");
                    break;

                case R.id.biaoding_confirm_3:
                    if (result_container_3.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_3) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    //如果正在测试中，不允许
                    if (isTesting_3) {
                        toast("正在测试中");
                        return;
                    }

                    if (biaodingCount_3 >= 2) {
                        toast("最多标定2次");
                        return;
                    }

                    if (!biaodingOver_3 && biaodingCount_3 == 0) {
                        //直接进入标定
                        write(Send_Biaoding_Start_3);
                        return;
                    }
                    String biaoding = biaoding_et_3.getText().toString();
                    boolean write = false;
                    if (biaodingCount_3 == 0) {
                        write = true;
                        write(Send_Biaoding_first_3 + biaoding);
                    } else if (biaodingFirst_3 && biaodingCount_3 == 1) {
                        write = true;
                        write(Send_Biaoding_second_3 + biaoding);
                    }
                    if (write) {
                        biaodingCount_3++;
                    }
                    if (biaodingCount_3 == 2) {
                        biaodingCount_3 = 0;
                    }
                    break;

                case R.id.result_test_cancel_3://重新测试
                    //取消成绩
                    test_delete_3.performClick();
                    break;
                case R.id.result_best_save_3:
                    //保存数据
                    if (userInfo_3 != null) {
                        if (StringUtils.isEmpty(userInfo_3.height) || StringUtils.isEmpty(userInfo_3.weight)) {
                            toast("没有测试数据");
                        } else {
                            UserModel.updateUserInfoInHeightAndWeight(WeightHeightGroupActivity.this, userInfo_3, userInfo_3.height, userInfo_3.weight);
                            toast("保存成功");
                            reset_3();
                        }
                    }
                    break;
            }
        }
    };

    private boolean allowDeleUser_3() {
        boolean testing = isTesting_3;
        boolean notsave = testOk_3;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备1正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备1测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightGroupActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset_3();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //任务独立完成，互不干扰
    class LocalLoadTask_3 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
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
                userInfos = UserModel.queryUserInfoByCode(WeightHeightGroupActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg");
//                Toast.makeText(WeightHeightGroupActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                userInfo_3 = userInfos.get(0);
                result_settings_3.setVisibility(View.GONE);
                result_container_3.setVisibility(View.VISIBLE);
                try {
                    if (StringUtils.isEmpty(userInfo_3.avater_label)) {
                        test_avatar_3.setImageBitmap(MainApplication.defaultBitmap);
                    } else {
                        test_avatar_3.setImageBitmap(BitmapFactory.decodeFile(userInfo_3.avater_label));
                    }
                } catch (Exception e) {
                    test_avatar_3.setImageBitmap(MainApplication.defaultBitmap);
                }
                result_name_3.setText(userInfo_3.name);
                result_class_3.setText(userInfo_3.classes + userInfo_3.grade);
                result_code_3.setText(userInfo_3.code);

            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void handleTestCount_3() {
        if (userInfo_3 != null && !StringUtils.isEmpty(userInfo_3.weight)) {
            device_status_3.setText("测试完毕");
            result_operation_3.setVisibility(View.VISIBLE);
            if (resultCountDownTimer_3 != null) {
                resultCountDownTimer_3.cancel();
            }
        } else {
            result_operation_3.setVisibility(View.GONE);
        }

    }

    void reset_3() {
        result_settings_3.setVisibility(View.VISIBLE);
        result_container_3.setVisibility(View.GONE);
        biaoding_et_3.setText("");
        biaoding_et_3.setHint("标定完毕");
        biaoding_confirm_3.setText("开始标定");
        biaoding_confirm_3.setEnabled(true);
        userInfo_3 = null;
        currentResult_3 = "";
        biaodingCount_3 = 0;
        biaodingOver_3 = false;
        enterBiaoding_3 = false;
        biaodingFirst_3 = false;
        isTesting_3 = testComplete_3 = testOk_3 = false;
        device_result_height_3.setText("0");
        device_result_weight_3.setText("0");
        handleTestCount_3();
    }

    void handleSearchResult_3() {
        String str = code_et_3.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg");
            return;
        }
        dataImportDialog = new DataImportDialog(WeightHeightGroupActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
        }
        if (str.length() < 2) {
            Toast.makeText(WeightHeightGroupActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        loadTask_3 = new LocalLoadTask_3();
        loadTask_3.execute(str);
    }

    void terminal_4() {
        //设备1
        if (connectCountDownTimer_4 != null) {
            connectCountDownTimer_4.cancel();
            connectCountDownTimer_4 = null;
        }
        if (resultCountDownTimer_4 != null) {
            resultCountDownTimer_4.cancel();
            resultCountDownTimer_4 = null;
        }
        if (countDownTimer1_4 != null) {
            countDownTimer1_4.cancel();
            countDownTimer1_4 = null;
        }
        if (countDownTimer2_4 != null) {
            countDownTimer2_4.cancel();
            countDownTimer2_4 = null;
        }
        if (countDownTimer3_4 != null) {
            countDownTimer3_4.cancel();
            countDownTimer3_4 = null;
        }

    }

    FHLHandler_4 mHandler_4 = new FHLHandler_4();
    //设备1数据和视图
    boolean isTesting_4;//正在测试中
    boolean testOk_4;//测试完毕
    boolean testComplete_4;//测试完毕并且保存完毕

    //学生信息 输入
    View result_settings_4;//输入学生信息
    EditText code_et_4;//输入学号
    View result_clear_4;//删除输入框学生信息
    View result_ok_4;//确认学生信息
    View test_delete_4;//删除当学学生信息

    //学生信息 展现
    View result_container_4;//学生信息展现区域
    ImageView test_avatar_4;//学生头像
    TextView result_name_4;//学生名字
    TextView result_class_4;//学生年级和班级
    TextView result_code_4;//学生学号
    //设备状态检测
    boolean deviceReady_4 = true;//设备是否准备好
    TextView device_status_4;//设备连接状态
    View menu_connect_4;//连接外设
    View menu_start_4;//开始测试
    EditText biaoding_et_4;//输入标定值
    TextView biaoding_confirm_4;//标定确认
    TextView device_result_height_4;//身高
    TextView device_result_weight_4;//体重

    View result_operation_4;//重测 保存 区域
    View result_test_cancel_4;//重测一次
    View result_best_save_4;//保存成绩

    UserInfo userInfo_4;//查询到的学生
    LocalLoadTask_4 loadTask_4;//查询学生信息

    int playTime_4 = 0;
    boolean biaodingOver_4;
    boolean enterBiaoding_4;
    int biaodingCount_4 = 0;
    String currentResult_4 = "";
    CountDownTimer connectCountDownTimer_4;//连接设备计时器
    CountDownTimer resultCountDownTimer_4;//测试计时器
    CountDownTimer countDownTimer1_4;
    CountDownTimer countDownTimer2_4;
    CountDownTimer countDownTimer3_4;
    ResultThread_4 resultThread_4;

    //连接外设发送数据
//    private static final String Send_Connecting_4 = "0413";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm_4 = "0415";//主机确认外设状态

    //标定
    private static final String Send_Biaoding_Start_4 = "0418";//发送标定 回 04C8 进入标定
    private static final String Send_Biaoding_first_4 = "0419";//第一次标定,带输入值 回 04C9 完成第一次标定
    private static final String Send_Biaoding_second_4 = "041A";//第二次标定,带输入值  回04CA 完成标定

    private static final String Send_Start_Test_4 = "0416";//开始测试
    private static final String Send_Get_Result_4 = "0414";//获取测试数据

    //连接外设1接收数据
//    private static final String RC_Connecting_4 = "04C3";//正在连接
//    private static final String RC_Not_Ready_4 = "04D0";//没准备好
//    private static final String RC_Connect_Success_4 = "04D1";//连接成功

    private static final String RC_Biaoding_Enter_4 = "04C8";//进入标定
    private static final String RC_Biaoding_first_4 = "04C9";//第一次标定结束
    private static final String RC_Biaoding_second_4 = "04CA";//第二次标定结束
    private static final String RC_Testing_4 = "04C4";//测试中
    private static final String RC_Problem_4 = "04C1";//外设故障
    private static final String RC_Success_4 = "04C5";//测试完毕

    class FHLHandler_4 extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
//                //设备1
//                case RC_Connecting_4:
//                    write(Send_Confirm_4);
//                    break;
//                case RC_Not_Ready_4:
//                    device_status_4.setText("设备没准备好");
//                    break;
//                case RC_Connect_Success_4:
//                    deviceReady_4 = true;
//                    device_status_4.setText("连接成功");
//                    if (connectCountDownTimer_4 != null) {
//                        connectCountDownTimer_4.cancel();
//                    }
//                    toast("设备1连接成功");
//                    if (biaodingCount_4 == 0) {
//                        //直接进入标定
//                        write(Send_Biaoding_Start_4);
//                    }
//                    break;
                case RC_Testing_4:
                    isTesting_4 = true;
                    testOk_4 = false;
                    device_status_4.setText("测试中...");
                    waitResult_4();
                    break;
                case RC_Problem_4:
                    device_status_4.setText("设备故障");
                    toast("设备1出故障了");
                    break;
                case RC_Biaoding_Enter_4:
                    //进入标定
                    enterBiaoding_4 = true;
                    toast("开始标定，请输入第一次标定值");
                    break;
                case RC_Biaoding_first_4:
                    biaodingCount_4++;
                    biaodingFirst_4 = true;
                    biaoding_et_4.setText("");
                    biaoding_et_4.setHint("请再次输入的标定值");
                    biaoding_confirm_4.setText("再次标定");
                    break;
                case RC_Biaoding_second_4:
                    biaodingCount_4++;
                    biaoding_et_4.setHint("标定完毕");
                    biaoding_confirm_4.setText("标定完毕");
                    biaoding_confirm_4.setEnabled(false);
                    biaodingOver_4 = true;
                    toast("标定完毕，请开始测试");
                    break;
            }

        }
    }

    protected void waitResult_4() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer_4 != null) {
            resultCountDownTimer_4.cancel();
        }
        resultCountDownTimer_4 = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result_4);
            }

            @Override
            public void onFinish() {
                menu_start_icon_4.setVisibility(View.VISIBLE);
                menu_start_label_4.setVisibility(View.GONE);
                toast("设备4测试超时");
            }

        };
        resultCountDownTimer_4.start();
    }

    void initData_4() {
        connect_all.setOnClickListener(clickListener_4);
        //设备1
        result_clear_4.setOnClickListener(clickListener_4);
        result_ok_4.setOnClickListener(clickListener_4);
        menu_connect_4.setOnClickListener(clickListener_4);
        menu_start_4.setOnClickListener(clickListener_4);
        biaoding_confirm_4.setOnClickListener(clickListener_4);
        test_delete_4.setOnClickListener(clickListener_4);
        result_test_cancel_4.setOnClickListener(clickListener_4);
        result_best_save_4.setOnClickListener(clickListener_4);
    }

    class ResultThread_4 extends Thread {
        String data;
        boolean playVoice;
        boolean uiFresh;

        public ResultThread_4(String data, boolean playVoice, boolean uiFresh) {
            this.data = data;
            this.playVoice = playVoice;
            this.uiFresh = uiFresh;
        }

        private void playWeight(String weightData, final int duration) {
            ensureMp("height_weight.ogg");
            synchronized (resultThread_4) {
                try {
                    wait(1000);
                } catch (Exception e) {

                }
            }
            final ArrayList<String> weightsounds = AppUtils.getSoundList(weightData);
            weightsounds.add("kg.ogg");
            final int size1 = weightsounds.size();

            if (size1 > 0) {
                countDownTimer3_4 = new CountDownTimer((duration) * (size1 + 1), duration) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (playTime_4 < size1) {
                            String path = weightsounds.get(playTime_4);
                            if (!StringUtils.isEmpty(path)) {
                                ensureMp(path);
                                playTime_4++;
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        playTime_4 = 0;
                        countDownTimer3_4 = null;
                    }

                };
                countDownTimer3_4.start();
            }
        }

        @Override
        public void run() {
            super.run();
            if (data == null || data.length() < 5) {
                return;
            }
            final String heightData = data.substring(0, 5);
            final String weightData = data.substring(5, data.length());
            try {
                if (userInfo_4 != null) {
                    userInfo_4.height = String.valueOf(Float.parseFloat(heightData));
                    userInfo_4.weight = String.valueOf(Float.parseFloat(weightData));
                }
            } catch (Exception e) {

            }
            if (uiFresh) {
                resultUi_4(heightData, weightData);
                isTesting_4 = false;
                testOk_4 = true;
            }
            if (playVoice) {
                ensureMp("test_score.ogg");
                synchronized (resultThread_4) {
                    try {
                        wait(2000);
                    } catch (Exception e) {

                    }
                }

                ensureMp("height_height.ogg");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        countDownTimer2_4 = new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                final ArrayList<String> heightsounds = AppUtils.getSoundList(heightData);
                                heightsounds.add("cm.ogg");
                                final int size = heightsounds.size();
                                final int duration = 700;
                                if (size > 0) {
                                    countDownTimer1_4 = new CountDownTimer((duration) * (size + 1), duration) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            if (playTime_4 < size) {
                                                String path = heightsounds.get(playTime_4);
                                                if (!StringUtils.isEmpty(path)) {
                                                    ensureMp(path);
                                                    playTime_4++;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            playTime_4 = 0;
                                            synchronized (obj) {
                                                try {
                                                    wait(500);
                                                } catch (Exception e) {

                                                }
                                            }

                                            playWeight(weightData, 700);
                                            countDownTimer1_4 = null;
                                        }


                                    };
                                    countDownTimer1_4.start();
                                }
                                countDownTimer2_4 = null;

                            }


                        };
                        countDownTimer2_4.start();
                    }
                });
            }

        }
    }

    private void resultUi_4(final String heightData, final String weightData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                device_status_4.setText("测试完毕");
                try {
                    device_result_height_4.setText(String.valueOf(Integer.parseInt(heightData)));
                } catch (Exception e) {

                }
                try {
                    float number = Float.parseFloat(weightData);
                    device_result_weight_4.setText(String.valueOf(number));
                } catch (Exception e) {

                }
                result_operation_4.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getResult_4(String data, boolean playVoice, boolean uiFresh) {
        terminal_4();
        playTime_4 = 0;
        resultThread_4 = new ResultThread_4(data, playVoice, uiFresh);
        resultThread_4.start();
    }

    void initView_4() {
        //设备1
        connect_all = findViewById(R.id.connect_all);
        result_settings_4 = findViewById(R.id.result_settings_4);
        code_et_4 = (EditText) findViewById(R.id.code_et_4);
        result_clear_4 = findViewById(R.id.result_clear_4);
        result_ok_4 = findViewById(R.id.result_ok_4);
        test_delete_4 = findViewById(R.id.test_delete_4);
        //学生信息 展现
        result_container_4 = findViewById(R.id.result_container_4);
        test_avatar_4 = (ImageView) findViewById(R.id.test_avatar_4);
        result_name_4 = (TextView) findViewById(R.id.result_name_4);
        result_class_4 = (TextView) findViewById(R.id.result_class_4);
        result_code_4 = (TextView) findViewById(R.id.result_code_4);
        //设备状态检测
        device_status_4 = (TextView) findViewById(R.id.device_status_4);
        menu_connect_4 = findViewById(R.id.menu_connect_4);
        menu_start_4 = findViewById(R.id.menu_start_4);
        biaoding_et_4 = (EditText) findViewById(R.id.biaoding_et_4);
        biaoding_confirm_4 = (TextView) findViewById(R.id.biaoding_confirm_4);
        device_result_height_4 = (TextView) findViewById(R.id.device_result_height_4);
        device_result_weight_4 = (TextView) findViewById(R.id.device_result_weight_4);
        result_operation_4 = findViewById(R.id.result_operation_4);
        result_test_cancel_4 = findViewById(R.id.result_test_cancel_4);
        result_best_save_4 = findViewById(R.id.result_best_save_4);
    }

    View.OnClickListener clickListener_4 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_all:
                    menu_connect_4.performClick();
                    break;
                //设备1
                case R.id.result_clear_4://清楚输入框信息
                    code_et_4.setText("");
                    break;
                case R.id.test_delete_4://删除学生信息
                    //如果有成绩未保存，提示保存
                    if (allowDeleUser_4()) {
                        result_settings_4.setVisibility(View.VISIBLE);
                        result_container_4.setVisibility(View.GONE);
                    }
                    break;

                case R.id.result_ok_4://确定
                    handleSearchResult_4();
                    break;
                case R.id.menu_start_4://开始测试
                    if (result_container_4.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_4) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    if (!enterBiaoding_4) {
                        toast("设备没有标定响应");
                        return;
                    }

                    if (!biaodingOver_4) {
                        toast("设备未完成标定");
                        return;
                    }
                    //如果正在测试中，不允许
                    if (isTesting_4) {
                        toast("正在测试，请稍候");
                        return;
                    }
                    ensureMp("start_test.ogg");
                    device_status_4.setText("测试中...");
                    //开始测试
                    write(Send_Start_Test_4);
                    break;

                case R.id.menu_connect_4://连接设备
//                    onTestConnect_4();
                    device_status_4.setText("正在连接...");
                    break;

                case R.id.biaoding_confirm_4:
                    if (result_container_4.getVisibility() != View.VISIBLE) {
                        toast("请确认学生信息");
                        return;
                    }
                    if (!deviceReady_4) {
                        toast("请先连接设备再测试");
                        return;
                    }

                    //如果正在测试中，不允许
                    if (isTesting_4) {
                        toast("正在测试中");
                        return;
                    }

                    if (biaodingCount_4 >= 2) {
                        toast("最多标定2次");
                        return;
                    }

                    if (!biaodingOver_4 && biaodingCount_4 == 0) {
                        //直接进入标定
                        write(Send_Biaoding_Start_4);
                        return;
                    }
                    String biaoding = biaoding_et_4.getText().toString();
                    boolean write = false;
                    if (biaodingCount_4 == 0) {
                        write = true;
                        write(Send_Biaoding_first_4 + biaoding);
                    } else if (biaodingFirst_4 && biaodingCount_4 == 1) {
                        write = true;
                        write(Send_Biaoding_second_4 + biaoding);
                    }
                    if (write) {
                        biaodingCount_4++;
                    }
                    if (biaodingCount_4 == 2) {
                        biaodingCount_4 = 0;
                    }
                    break;

                case R.id.result_test_cancel_4://重新测试
                    //取消成绩
                    test_delete_4.performClick();
                    break;
                case R.id.result_best_save_4:
                    //保存数据
                    if (userInfo_4 != null) {
                        if (StringUtils.isEmpty(userInfo_4.height) || StringUtils.isEmpty(userInfo_4.weight)) {
                            toast("没有测试数据");
                        } else {
                            UserModel.updateUserInfoInHeightAndWeight(WeightHeightGroupActivity.this, userInfo_4, userInfo_4.height, userInfo_4.weight);
                            toast("保存成功");
                            reset_4();
                        }
                    }
                    break;
            }
        }
    };

    private boolean allowDeleUser_4() {
        boolean testing = isTesting_4;
        boolean notsave = testOk_4;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备1正在测试中，确认切换学生信息？";
        } else if (notsave) {
            msg = "设备1测试数据没有保存,确认切换学生信息？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightGroupActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset_4();
                    appDialog.dismiss();
                }

            });
            appDialog.show();
        }
        return !finishNotAllow;
    }

    //任务独立完成，互不干扰
    class LocalLoadTask_4 extends AsyncTask<String, Void, ArrayList<UserInfo>> {
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
                userInfos = UserModel.queryUserInfoByCode(WeightHeightGroupActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg");
//                Toast.makeText(WeightHeightGroupActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                userInfo_4 = userInfos.get(0);
                result_settings_4.setVisibility(View.GONE);
                result_container_4.setVisibility(View.VISIBLE);
                try {
                    if (StringUtils.isEmpty(userInfo_4.avater_label)) {
                        test_avatar_4.setImageBitmap(MainApplication.defaultBitmap);
                    } else {
                        test_avatar_4.setImageBitmap(BitmapFactory.decodeFile(userInfo_4.avater_label));
                    }
                } catch (Exception e) {
                    test_avatar_4.setImageBitmap(MainApplication.defaultBitmap);
                }
                result_name_4.setText(userInfo_4.name);
                result_class_4.setText(userInfo_4.classes + userInfo_4.grade);
                result_code_4.setText(userInfo_4.code);

            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void handleTestCount_4() {
        if (userInfo_4 != null && !StringUtils.isEmpty(userInfo_4.weight)) {
            device_status_4.setText("测试完毕");
            result_operation_4.setVisibility(View.VISIBLE);
            if (resultCountDownTimer_4 != null) {
                resultCountDownTimer_4.cancel();
            }
        } else {
            result_operation_4.setVisibility(View.GONE);
        }

    }

    void reset_4() {
        result_settings_4.setVisibility(View.VISIBLE);
        result_container_4.setVisibility(View.GONE);
        biaoding_et_4.setText("");
        biaoding_et_4.setHint("标定完毕");
        biaoding_confirm_4.setText("开始标定");
        biaoding_confirm_4.setEnabled(true);
        userInfo_4 = null;
        currentResult_4 = "";
        biaodingCount_4 = 0;
        biaodingOver_4 = false;
        enterBiaoding_4 = false;
        biaodingFirst_4 = false;
        isTesting_4 = testComplete_4 = testOk_4 = false;
        device_result_height_4.setText("0");
        device_result_weight_4.setText("0");
        handleTestCount_4();
    }

    void handleSearchResult_4() {
        String str = code_et_4.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg");
            return;
        }
        dataImportDialog = new DataImportDialog(WeightHeightGroupActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);
        if (str.contains("(")) {
            str = str.substring(str.indexOf("(") + 1, str.length() - 1);
        }
        if (str.length() < 2) {
            Toast.makeText(WeightHeightGroupActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        loadTask_4 = new LocalLoadTask_4();
        loadTask_4.execute(str);
    }

}
