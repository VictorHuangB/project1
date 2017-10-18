package com.android.student.data.test.project.weightheight;

import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
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
 * 身高体重测试界面
 * Created by hailong on 2016/10/30.
 */
public class WeightHeightSingleActivity extends SerialPortActivity implements View.OnClickListener, HeightTestRule.RuleChangeListener {
    //学生信息输入区域
    View result_settings;
    View result_clear, result_ok;
    EditText code_et;

    //学生信息展示区域
    View result_container;
    ImageView test_avatar;
    TextView result_name;
    TextView result_class;
    TextView result_code;
    View test_delete;

    //连接测试区域
    View configure_area;
    TextView device_status;
    EditText biaoding_et;
    TextView biaoding_confirm;
    View device_connect;
    View menu_start;
    ImageView menu_start_icon;
    TextView menu_start_label;
    //结果展示区域
    View result_area;
    TextView height_result;
    TextView weight_result;
    View result_cancel;
    View result_save;

    //展示效果区域
    ImageView animate_view;
    AnimationDrawable animationDrawable;
    HeightTestRule height_view;
    WeightTestView weight_view;
    TextView test_result;
    View weight_point;

    //数据处理
    private static final int Max_Connect_Time = 6000;//等待连接结果的时间
    private static final int Max_Result_Time = 3 * 80 * 1000;//等待结果的时间可能会有点长，这个时间不好控制,暂定两分钟
    private static final int Hold_Time = 800;//300ms发送一次
    boolean deviceReady = true;
    boolean biaodingOver = false;
    boolean biaodingFirst = false;
    int playTime = 0;
    int biaodingCount = 0;
    boolean isTesting;
    boolean testOk;
    boolean testComplete;
    CountDownTimer connectCountDownTimer;
    CountDownTimer resultCountDownTimer;
    FHLHandler mHandler = new FHLHandler();
    LocalLoadTask loadTask;
    DataImportDialog dataImportDialog;

    UserInfo userInfo;
    //连接外设发送数据
//    private static final String Send_Connecting = "0113";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm = "0115";//主机确认外设状态

    //连接外设接收数据
//    private static final String RC_Connecting = "01C3";//正在连接
//    private static final String RC_Not_Ready = "01D0";//没准备好
//    private static final String RC_Connect_Success = "01D1";//连接成功
//    private static final String RC_Connect_Fail = "001";//连接失败
    //标定
    private static final String Send_Biaoding_Start_1 = "0118";//发送标定 回 01C8 进入标定
    private static final String Send_Biaoding_first_1 = "0119";//第一次标定,带输入值 回 01C9 完成第一次标定
    private static final String Send_Biaoding_second_1 = "011A";//第二次标定,带输入值  回01CA 完成标定

    //测试
    private static final String Send_Start_Test = "0116";//开始测试 0.3s发送一次
    private static final String Send_Get_Result = "0114";//获取测试数据 0.3s发送一次

    private static final String RC_Biaoding_Enter_1 = "01C8";//进入标定
    private static final String RC_Biaoding_first_1 = "01C9";//第一次标定结束
    private static final String RC_Biaoding_second_1 = "01CA";//第二次标定结束

    private static final String RC_Testing = "01C4";//测试中
    private static final String RC_Problem = "01C1";//外设故障
    private static final String RC_Success = "01C5";//测试完毕

    class FHLHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
//                case RC_Connecting:
//                    write(Send_Confirm);
//                    break;
//                case RC_Not_Ready:
//                    device_status.setText("设备没准备好");
//                    break;
//                case RC_Connect_Fail:
//                    toast("连接失败");
//                    device_status.setText("连接失败");
//                    if (connectCountDownTimer != null) {
//                        connectCountDownTimer.cancel();
//                    }
//                    break;
//                case RC_Connect_Success:
//                    deviceReady = true;
//                    device_status.setText("连接成功");
//                    if (connectCountDownTimer != null) {
//                        connectCountDownTimer.cancel();
//                    }
//                    toast("连接成功");
//                    if (biaodingCount == 0) {
//                        //直接进入标定
//                        write(Send_Biaoding_Start_1);
//                    }
//                    break;
                case RC_Testing:
                    isTesting = true;
//                    testOk = false;
                    device_status.setText("测试中...");
                    break;
                case RC_Problem:
                    device_status.setText("设备故障");
                    toast("设备出故障了");
                    break;
                case RC_Biaoding_Enter_1:

                    //进入标定
                    toast("开始标定，请输入第一次标定值");
                    break;
                case RC_Biaoding_first_1:
                    biaodingFirst = true;
                    biaoding_et.setText("");
                    biaoding_et.setHint("请再次输入的标定值");
                    biaoding_confirm.setText("再次标定");
                    break;
                case RC_Biaoding_second_1:
                    biaoding_et.setHint("标定完毕");
                    biaoding_confirm.setText("标定完毕");
                    biaoding_confirm.setEnabled(false);
                    biaodingOver = true;
                    toast("标定完毕");
                    break;
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weight_height_test_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initView();
        initData();
    }

    @Override
    public void onBackPressed() {
        boolean testing = isTesting;
        boolean notsave = testOk;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备正在测试中，退出将取消当前成绩，确认退出？";
        } else if (notsave) {
            msg = "测试数据没有保存，退出将取消当前成绩，确认退出？";
        } else if (!isTesting && !testOk) {
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightSingleActivity.this, "取消", "确定", true);

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

    void initView() {

        View test_a = findViewById(R.id.test_a);
        test_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  testConnect(view);
            }
        });
        View test_b = findViewById(R.id.test_b);
        test_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testBiaoding1(view);
            }
        });
        View test_c = findViewById(R.id.test_c);
        test_c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testBiaoding2(view);
            }
        });
        View test_d = findViewById(R.id.test_d);
        test_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testTest(view);
            }
        });
        View test_score = findViewById(R.id.test_score);
        test_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testResult(view);
            }
        });
//        View test_m = findViewById(R.id.test_score);
//        test_m.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Message message = new Message();
//                message.obj = RC_Biaoding_Enter_1;
//                mHandler.sendMessage(message);
//            }
//        });

        result_settings = findViewById(R.id.result_settings);
        code_et = (EditText) findViewById(R.id.code_et);
        code_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok.performClick();
                return false;
            }
        });
        result_clear = findViewById(R.id.result_clear);
        result_ok = findViewById(R.id.result_ok);

        result_container = findViewById(R.id.result_container);
        test_avatar = (ImageView) findViewById(R.id.test_avatar);
        result_name = (TextView) findViewById(R.id.result_name);
        result_class = (TextView) findViewById(R.id.result_class);
        result_code = (TextView) findViewById(R.id.result_code);
        test_delete = findViewById(R.id.test_delete);

        menu_start = findViewById(R.id.menu_start);
        configure_area = findViewById(R.id.configure_area);
        menu_start_icon = (ImageView) findViewById(R.id.menu_start_icon);
        menu_start_label = (TextView) findViewById(R.id.menu_start_label);

        device_status = (TextView) findViewById(R.id.device_status);
        device_connect = findViewById(R.id.device_connect);
        biaoding_et = (EditText) findViewById(R.id.biaoding_et);
        biaoding_confirm = (TextView) findViewById(R.id.biaoding_confirm);

        result_area = findViewById(R.id.result_area);
        result_cancel = findViewById(R.id.result_cancel);
        result_save = findViewById(R.id.result_save);
        height_result = (TextView) findViewById(R.id.height_result);
        weight_result = (TextView) findViewById(R.id.weight_result);

        test_result = (TextView) findViewById(R.id.test_result);
        height_view = (HeightTestRule) findViewById(R.id.height_view);
        weight_view = (WeightTestView) findViewById(R.id.weight_view);
        weight_point = findViewById(R.id.weight_point);

        animate_view = (ImageView) findViewById(R.id.animate_view);
        animate_view.setBackgroundResource(R.drawable.height_anim);
        animationDrawable = (AnimationDrawable) animate_view.getBackground();
    }

    void initData() {
        result_clear.setOnClickListener(this);
        result_ok.setOnClickListener(this);
        menu_start.setOnClickListener(this);
        test_delete.setOnClickListener(this);
        device_connect.setOnClickListener(this);
        biaoding_confirm.setOnClickListener(this);
        result_cancel.setOnClickListener(this);
        result_save.setOnClickListener(this);
        height_view.setRuleChangeListener(this);
    }

//    protected void onTestConnect() {
//        //1，发送0113，hold 300ms
//        if (connectCountDownTimer != null) {
//            connectCountDownTimer.cancel();
//        }
//        connectCountDownTimer = new CountDownTimer(Max_Connect_Time, Hold_Time) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                Log.d("hailong13", " onTick ");
//                write(Send_Connecting);
//            }
//
//            @Override
//            public void onFinish() {
//                Log.d("hailong14", " onFinish ");
//                if (!deviceReady) {
//                    if (mOutputStream == null) {
//                        toast("数据无法通信，请保证设备有读写权限");
//                    } else {
//                        toast("设备连接失败");
//                    }
//                    device_status.setText("连接失败");
//                }
//            }
//
//        };
//        connectCountDownTimer.start();
//    }

    void handleSearchResult() {
        String str = code_et.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg");
            return;
        }
        dataImportDialog = new DataImportDialog(WeightHeightSingleActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);

        loadTask = new LocalLoadTask();
        loadTask.execute(str);
    }

    class LocalLoadTask extends AsyncTask<String, Void, ArrayList<UserInfo>> {
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
                userInfos = UserModel.queryUserInfoByCode(WeightHeightSingleActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg");
            } else {
                userInfo = userInfos.get(0);
                result_settings.setVisibility(View.GONE);
                result_container.setVisibility(View.VISIBLE);
                try {
                    if (StringUtils.isEmpty(userInfo.avater_label)) {
                        test_avatar.setImageBitmap(MainApplication.defaultBitmap);
                    } else {
                        test_avatar.setImageBitmap(BitmapFactory.decodeFile(userInfo.avater_label));
                    }
                } catch (Exception e) {
                    test_avatar.setImageBitmap(MainApplication.defaultBitmap);
                }
                result_name.setText(userInfo.name);
                result_class.setText(userInfo.classes + userInfo.grade);
                result_code.setText(userInfo.code);

            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    @Override
    protected void onDataReceived(final byte[] buffer, int size) {
        String result = AppUtils.bytesToHexString(buffer);
        String code = result.replace("\n", "").replace("\r", "");
        Log.d("hailong12", " code is " + code);
        if (!StringUtils.isEmpty(code)) {
            //如果是C5开头，则取后10位数字,前十位身高，后十位体重
            //回01C5+data1+data2+data3+data4+data5+data6+data7+data8+data9+data10
            //data1~data5，每个1个byte，对应一个数, ASCII码形式
            String data = "";
            if (code.startsWith(RC_Success)) {
                String data1 = code.substring(4, 24);
                //讲ASCII转换成十进制再转换成对应的字符
                for (int i = 0; i < data1.length(); i += 2) {
                    String convert = data1.substring(i, i + 2);
                    data += AppUtils.asciiToStr(convert);
                }
                if (data.startsWith("01C50000000000")) {
                    //正在测试
                    menu_start_icon.setVisibility(View.GONE);
                    menu_start_label.setVisibility(View.VISIBLE);
                    return;
                } else {
                    menu_start_icon.setVisibility(View.VISIBLE);
                    menu_start_label.setVisibility(View.GONE);
                    toast("测试完毕");
                }
                //getResultSuccess(data);
                getResult(data);
            } else {
                Message message = new Message();
                message.obj = code.substring(0, 4);
                mHandler.sendMessage(message);
            }
        } else {
            toast("连接设备失败");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
            resultCountDownTimer = null;
        }
    }

    private void getResult(String data) {
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
            countDownTimer1 = null;
        }
        if (countDownTimer2 != null) {
            countDownTimer2.cancel();
            countDownTimer2 = null;
        }
        if (countDownTimer3 != null) {
            countDownTimer3.cancel();
            countDownTimer3 = null;
        }
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
            resultCountDownTimer = null;
        }
        playTime = 0;
        resultThread = new ResultThread(data);
        resultThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
            countDownTimer1 = null;
        }
        if (countDownTimer2 != null) {
            countDownTimer2.cancel();
            countDownTimer2 = null;
        }
        if (countDownTimer3 != null) {
            countDownTimer3.cancel();
            countDownTimer3 = null;
        }
    }

    CountDownTimer countDownTimer1;
    CountDownTimer countDownTimer2;
    CountDownTimer countDownTimer3;
    ResultThread resultThread;
    Object obj = new Object();

    class ResultThread extends Thread {
        String data;

        public ResultThread(String data) {
            this.data = data;
        }

        private void playWeight(String weightData, final int duration) {
            ensureMp("height_weight.ogg");
            synchronized (resultThread) {
                try {
                    wait(1000);
                } catch (Exception e) {

                }
            }
            final ArrayList<String> weightsounds = AppUtils.getSoundList(weightData);
            weightsounds.add("kg.ogg");
            final int size1 = weightsounds.size();

            if (size1 > 0) {
                countDownTimer3 = new CountDownTimer((duration) * (size1 + 1), duration) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (playTime < size1) {
                            String path = weightsounds.get(playTime);
                            if (!StringUtils.isEmpty(path)) {
                                ensureMp(path);
                                playTime++;
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        playTime = 0;
                        countDownTimer3 = null;
                    }

                };
                countDownTimer3.start();
            }
        }


        @Override
        public void run() {
            super.run();
            String height = data.substring(0, 5);
            String subHeight = height;
            int pos = height.indexOf(".");
            if (pos >= 0) {
                subHeight = height.substring(0, pos);
            }
            final String heightData = subHeight;

            String weight = data.substring(5, data.length());
            String subWeight = weight;
            pos = weight.indexOf(".");
            if (pos >= 0) {
                subWeight = weight.substring(0, pos);
            }
            final String weightData = subWeight;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (animationDrawable.isRunning()) {
                        animationDrawable.stop();
                    }
                    String resultHeight = String.valueOf(Integer.parseInt(heightData));
                    String resultWeight = String.valueOf(Integer.parseInt(weightData));
                    try {
                        if (userInfo != null) {
                            userInfo.height = resultHeight;
                            userInfo.weight = resultWeight;
                        }
                    } catch (Exception e) {

                    }
                    device_status.setText("测试完毕");
                    try {
                        int number = Integer.parseInt(heightData);
                        height_view.scroll(number);
                    } catch (Exception e) {
                    }
                    try {
                        float number = Float.parseFloat(weightData);
                        weight_point.setPivotX(weight_point.getWidth());
                        weight_point.setPivotY(weight_point.getHeight() / 2);
                        weight_point.animate().rotation(360.0f * (number * 1.0f / weight_view.Max)).setDuration(500).start();
                    } catch (Exception e) {
                        // Toast.makeText(WeightHeightSingleActivity.this, "请输入体重", Toast.LENGTH_SHORT).show();
                    }

                    configure_area.setVisibility(View.GONE);
                    result_area.setVisibility(View.VISIBLE);

                    try {
                        height_result.setText(resultHeight);
                    } catch (Exception e) {

                    }
                    try {
                        weight_result.setText(resultWeight);
                    } catch (Exception e) {

                    }
                }
            });


            isTesting = false;
            testOk = true;
            ensureMp("test_score.ogg");
            synchronized (resultThread) {
                try {
                    wait(2000);
                } catch (Exception e) {

                }
            }

            ensureMp("height_height.ogg");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countDownTimer2 = new CountDownTimer(1000, 1000) {
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
                                countDownTimer1 = new CountDownTimer((duration) * (size + 1), duration) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (playTime < size) {
                                            String path = heightsounds.get(playTime);
                                            if (!StringUtils.isEmpty(path)) {
                                                ensureMp(path);
                                                playTime++;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        playTime = 0;
                                        synchronized (obj) {
                                            try {
                                                wait(500);
                                            } catch (Exception e) {

                                            }
                                        }

                                        playWeight(weightData, 700);
                                        countDownTimer1 = null;
                                    }


                                };
                                countDownTimer1.start();
                            }
                            countDownTimer2 = null;

                        }


                    };
                    countDownTimer2.start();
                }
            });


        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.device_connect:
//                onTestConnect();
                device_status.setText("正在连接设备...");
                break;
            case R.id.biaoding_confirm:
                if (result_container.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                if (!deviceReady) {
                    toast("请先连接设备再测试");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting) {
                    toast("正在测试中");
                    return;
                }
                if (biaodingCount >= 2) {
                    toast("最多标定2次");
                    return;
                }
                if (!biaodingOver && biaodingCount == 0) {
//                        //直接进入标定
                    write(Send_Biaoding_Start_1);
                    return;
                }
                String biaoding = biaoding_et.getText().toString();
                boolean write = false;
                if (biaodingCount == 0) {
                    write = true;
                    write(Send_Biaoding_first_1 + biaoding);
                } else if (biaodingFirst && biaodingCount == 1) {
                    write = true;
                    write(Send_Biaoding_second_1 + biaoding);
                }
                if (write) {
                    biaodingCount++;
                }
                if (biaodingCount == 2) {
                    biaodingCount = 0;
                }
                break;
            case R.id.result_clear:
                code_et.setText("");
                break;
            case R.id.test_delete:
                if (isTesting || testOk) {
                    exChangeUser();
                    return;
                }
                reset();
                break;
            case R.id.result_ok:
                handleSearchResult();
                break;
            case R.id.result_cancel:
                reset();
                break;
            case R.id.result_save:
                //保存数据
                if (userInfo != null) {
                    UserModel.updateUserInfoInHeightAndWeight(WeightHeightSingleActivity.this, userInfo, userInfo.height, userInfo.weight);
                    toast("保存成功");
                }
                break;
            case R.id.menu_start:
                if (result_container.getVisibility() != View.VISIBLE) {
                    toast("请确认学生信息");
                    return;
                }
                if (!deviceReady) {
                    toast("请先连接设备再测试");
                    return;
                }
                //如果正在测试中，不允许
                if (isTesting) {
                    toast("正在测试中");
                    return;
                }
//                if (!biaodingOver) {
//                    toast("还未完成标定");
//                    return;
//                }
                device_status.setText("测试中...");
                animationDrawable.start();
                ensureMp("start_test.ogg");
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ensureMp("height_keep_straight.ogg");
                    }
                }, 1500);
                //开始测试
                menu_start_icon.setVisibility(View.GONE);
                menu_start_label.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult();
                    }
                }, 3000);
                break;
        }
    }

//    public void testConnect(View view) {
//        Message message = new Message();
//        message.obj = RC_Connect_Success;
//        mHandler.sendMessage(message);
////        getResultSuccess();
//    }

    public void testBiaoding1(View view) {
        Message message = new Message();
        message.obj = RC_Biaoding_first_1;
        mHandler.sendMessage(message);
    }

    public void testBiaoding2(View view) {
        Message message = new Message();
        message.obj = RC_Biaoding_second_1;
        mHandler.sendMessage(message);

    }

    public void testTest(View view) {
        menu_start.performClick();
    }

    public void testResult(View view) {
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
            countDownTimer1 = null;
        }
        if (countDownTimer2 != null) {
            countDownTimer2.cancel();
            countDownTimer2 = null;
        }
        if (countDownTimer3 != null) {
            countDownTimer3.cancel();
            countDownTimer3 = null;
        }
        playTime = 0;
        resultThread = new ResultThread("0016000089");
        resultThread.start();
    }

    protected void waitResult() {
        //1，发送0114，hold 300ms
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        resultCountDownTimer = new CountDownTimer(Max_Result_Time, Hold_Time) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("hailong12", " onTick ");
                write(Send_Get_Result);
            }

            @Override
            public void onFinish() {
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                menu_start_icon.setVisibility(View.VISIBLE);
                menu_start_label.setVisibility(View.GONE);
                toast("测试超时");
            }

        };
        resultCountDownTimer.start();
    }

    private void exChangeUser() {
        boolean testing = isTesting;
        boolean notsave = testOk;
        boolean finishNotAllow = testing || notsave;
        String msg = "";
        if (testing) {
            msg = "设备正在测试中，删除将取消当前成绩，确认删除？";
        } else if (notsave) {
            msg = "测试数据没有保存，删除将取消当前成绩，确认删除？";
        }
        if (finishNotAllow) {
            final AppDialog appDialog = new AppDialog(WeightHeightSingleActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    reset();
                }

            });
            appDialog.show();
        } else {
            finish();
        }

    }

    private void reset() {
        height_view.reset();
        weight_point.setPivotX(weight_point.getWidth());
        weight_point.setPivotY(weight_point.getHeight() / 2);
        weight_point.animate().rotation(360.0f * (0 * 1.0f / weight_view.Max)).setDuration(500).start();
        configure_area.setVisibility(View.VISIBLE);
        result_area.setVisibility(View.GONE);
        result_settings.setVisibility(View.VISIBLE);
        result_container.setVisibility(View.GONE);
        biaoding_et.setText("");
        biaoding_et.setHint("标定完毕");
        biaoding_confirm.setText("开始标定");
        biaoding_confirm.setEnabled(true);
        biaodingCount = 0;
        biaodingOver = false;
        biaodingFirst = false;
        isTesting = testComplete = testOk = false;
    }

    @Override
    public void onChange(int level) {
        test_result.setText(String.valueOf(level));
    }

}
