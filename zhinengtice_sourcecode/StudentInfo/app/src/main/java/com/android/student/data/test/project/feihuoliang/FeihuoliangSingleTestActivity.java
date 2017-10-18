package com.android.student.data.test.project.feihuoliang;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.android.student.data.test.SerialPort;
import com.android.student.data.test.SerialPortActivity;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.dialog.IcSameUserDialog;
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
 * 肺活量单人测试页面
 * Created by hailong on 2016/11/9 0009.
 */
public class FeihuoliangSingleTestActivity extends SerialPortActivity implements View.OnClickListener {
    int testCount = 0;
    boolean isTesting;
    boolean testOk;
    boolean testComplete;
    TextView start_label;
    View result_settings;
    View result_ic, result_ok;
    EditText code_et;
    View code_delete;

    View start_test;
    //    TextView score_count;
    TextView test_score_des;
    TextView score_label;

    View menu_test_1;//第一次测试visible
    View menu_test_2;//第二次测试visible
    View menu_test_3;//第三次测试visible

    View result_container;
    ImageView test_avatar;
    TextView result_name;
    TextView result_sex;
    TextView result_class;
    TextView result_code;
    View test_delete;

    View result_operation;
    TextView device_status;//设备连接状态
    TextView dec_status;
    //    View menu_connect;//连接外设
    View device_reconnect;//连接失败，重新连接
    View menu_start;//开始测试
    ImageView menu_start_icon;
    TextView menu_start_label;
    //    View result_cancel;
    View result_test_again;
    View result_best_save;

    ImageView animate_view;
    AnimationDrawable animationDrawable;
    LocalLoadTask loadTask;
    DataImportDialog dataImportDialog;

    UserInfo userInfo;

    //    CountDownTimer connectCountDownTimer;
    CountDownTimer resultCountDownTimer;
    FHLHandler mHandler = new FHLHandler();
    ArrayList<String> resultList = new ArrayList<>();
    boolean deviceReady = true;

    //    private static final int Max_Connect_Time = 6000;//等待连接结果的时间
    private static final int Max_Result_Time = 3 * 80 * 1000;//等待结果的时间可能会有点长，这个时间不好控制,暂定3分钟
    private static final int Hold_Time = 800;//800ms发送一次
    //连接外设发送数据
//    private static final String Send_Connecting = "0113";//主机开机,向外设发送状态查询
//    private static final String Send_Confirm = "0115";//主机确认外设状态

    private static final String Send_Start_Test = "0116";//开始测试
    private static final String Send_Get_Result = "0114";//获取测试数据
    //连接外设接收数据
    private static final String RC_Connecting = "01C3";//正在连接
    private static final String RC_Not_Ready = "01D0";//没准备好
    private static final String RC_Connect_Success = "01D1";//连接成功
    private static final String RC_Connect_Fail = "001";//连接失败

    private static final String RC_Testing = "01C4";//测试中
    private static final String RC_Problem = "01C1";//外设故障
    private static final String RC_Success = "01C5";//测试完毕


    boolean isPinLoad = false;//密码下载成功
    boolean isCodeOk = false;//IC卡学号读取完毕
    boolean isDataOk = false;//IC卡数据读取完毕
    private OutputStream mOutputStream;
    private InputStream mInputStream;
//    private ReadThread_IC mReadThread_ic;

    private SerialPort mSerialPort = null;
    //模拟0扇区02块的基本信息（除了姓名和学号）

    //UTF
    //Write UTF-16
    byte[] write_code = {
            0X17, 0X00, //状态成功
            (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF), (byte) (0XFF),
            0X12, 0X34, 0X56, 0X78, (byte) 0X90,//学号表示 1234567890
            0X02, //01 男 02 女
            0X00, 0X00, 0X00, 0X00, 0X00
    };
    byte[] write_data_utf = {
            0X17, 0X00,//状态成功
            (byte) (0X9A), (byte) (0XD8), (byte) (0X66), (byte) (0X53), (byte) (0X67), (byte) (0X7E), 0X20, (byte) 0X20,//姓名
            0X03,           //年级编号 三年级
            0X00, 0X12,     //班级编号0012
            0X12,           //班级12
            0X00, 0X01,     //序号 0001
            (byte) 0XFF, (byte) 0XFF
    };


    byte[] write_data_gbk = {
            0X17, 0X00,     //状态成功
            (byte) (0XB8), (byte) (0XDF), (byte) (0XCF), (byte) (0XFE), (byte) (0XCB), (byte) (0XC9), 0X20, (byte) 0X20,//姓名
            0X03,           //年级编号 三年级
            0X00, 0X12,     //班级编号0012
            0X12,           //班级12
            0X00, 0X01,     //序号 0001
            (byte) 0XFF, (byte) 0XFF
    };


//    class ReadThread_IC extends Thread {
//        //IC读取
//        @Override
//        public void run() {
//            while (!stop) {
//                int size = 0;
//                try {
//                    byte[] buffer = new byte[32];
//                    if (mInputStream != null) {
//                        size = mInputStream.read(buffer);
//                        Log.d("hailong30", " mInputStream " + mInputStream + " size " + size);
//                        if (size > 0) {
//                            onDataReceived_IC(buffer, size);
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
        b.setTitle("错误");
        b.setMessage(resourceId);
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
//                FeihuoliangSingleTestActivity.this.finish();
            }
        });
        b.show();
    }


    private String icCode = "";//学号 10byte
    private String icSex = ""; //性别

    private String icName = "";//名字 8byte
    private String icGradeCode = "";//年级编号 2byte
    private String icClas = "";//年级 1byte
    private String icGrade = "";//班级  1byte
    private String icOrderCode = "";

    private boolean checkICInfo() {
        //校验code
        if (StringUtils.isEmpty(icCode)) {
//            toast("学号为空");
            return false;
        }
        if (StringUtils.isEmpty(icName)) {
//            toast("姓名为空");
            return false;
        }
        if (StringUtils.isEmpty(icClas)) {
//            toast("年级为空");
            return false;
        }
        if (StringUtils.isEmpty(icGrade)) {
//            toast("班级为空");
            return false;
        }
        return true;
    }

    private void showUserInfo(UserInfo info) {
        userInfo = info;
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
        result_sex.setText(AppUtils.getSex(userInfo));
        result_class.setText(userInfo.classes + userInfo.grade);
        result_code.setText(userInfo.code);

    }

    private void showSameUserDialog(final UserInfo oldUserInfo, final UserInfo newUserInfo) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IcSameUserDialog selectDialog = new IcSameUserDialog(FeihuoliangSingleTestActivity.this, oldUserInfo, newUserInfo);
                selectDialog.setDialogClickListener(new IcSameUserDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        //选择本地的
                        //显示学生信息
                        showUserInfo(oldUserInfo);
                    }

                    @Override
                    public void operationRight() {
                        //选择IC卡的
                        //数据库更新
                        coverUserInfo(oldUserInfo, newUserInfo);
                        //显示学生信息
                        showUserInfo(newUserInfo);
                    }
                });
                selectDialog.show();
            }
        });

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

    private void onDataReceived_IC(final byte[] buffer, final int size) {
        String result = AppUtils.bytesToHexString(buffer);
        String code = result.replace("\n", "").replace("\r", "");
        if (code.startsWith(error_nocard) || code.startsWith(error_unkown) || code.startsWith(error_exception)) {
            ensureMp("ic_read_error.ogg", tip);
            return;
        }
        Log.d("hailong60", " this is " + this + " isPinLoad " + isPinLoad);
        boolean success = "00".equals(code.substring(2, 4));
        if (success) {
            if (isPinLoad) {//密码下载成功
                //toast("密码下载成功");
                //获取0扇区01块的学号
                Log.d("hailong30", " isPinLoadOk code " + code);
                Log.d("hailong50", "onDataReceived_IC mOutputStream " + mOutputStream);
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
                    //是否有两个同样的学号
                    if (UserModel.checkSameUser(FeihuoliangSingleTestActivity.this, icCode)) {
                        //拿到本地相同code的信息
                        ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(FeihuoliangSingleTestActivity.this, icCode);
                        if (userInfos != null && userInfos.size() > 0) {
                            toast("学号重复，请选择");
                            showSameUserDialog(userInfos.get(0), info);
                        }
                        return;
                    }
                    UserModel.addItemToDatabase(FeihuoliangSingleTestActivity.this, info);
                    showUserInfo(info);
                    toast("读卡成功");
//                    ((TextView)result_ic).setText("读卡成功");
                    //ensureMp();
                } /*else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("hailong40", " CBA ");
                            result_ic.performClick();
                        }
                    });

                }*/
                isDataOk = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feihuoliang_test_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initView();
        initData();
        Log.d("hailong40", " onCreate ");
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
            final AppDialog appDialog = new AppDialog(FeihuoliangSingleTestActivity.this, "取消", "确定", true);

            appDialog.setupContentView(msg);
            appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                @Override
                public void operationLeft() {
                    appDialog.dismiss();
                }

                @Override
                public void operationRight() {
                    //delete in DB
                    if (resultCountDownTimer != null) {
                        resultCountDownTimer.cancel();
                    }
                    if (animationDrawable.isRunning()) {
                        animationDrawable.stop();
                    }
                    finish();
                }

            });
            appDialog.show();
        } else {
            if (resultCountDownTimer != null) {
                resultCountDownTimer.cancel();
            }
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            super.onBackPressed();
        }

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
            final AppDialog appDialog = new AppDialog(FeihuoliangSingleTestActivity.this, "取消", "确定", true);

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

    private void closeIc() {
        closeSerialPort();
        isDataOk = isCodeOk = isPinLoad = false;
        ICHandler.removeCallbacks(mRunnable);
//        mReadThread_ic.interrupt();
//        mReadThread_ic = null;
        try {
            if (mOutputStream != null) {
//                mOutputStream.flush();
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
        closeIc();
        Log.d("hailong30", " onDestroy ");
//        if (connectCountDownTimer != null) {
//            connectCountDownTimer.cancel();
//        }
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        super.onDestroy();
    }

    CountDownTimer ShutDownTimer_1 = new CountDownTimer(20000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            isTesting = false;
            testOk = false;
            displayStatus("请打开外设");
            displayStatusTimer1.cancel();
            displayStatusTimer1.start();
        }
    };
    CountDownTimer displayStatusTimer_1 = new CountDownTimer(4000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus();
        }
    };

    class FHLHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String code = (String) msg.obj;
            devIdleTimer1.cancel();
            if (!(RC_Testing.equals(code) || RC_Problem.equals(code)) && !RC_Connect_Success.equals(code)) {
                displayStatus("请打开外设");
                return;
            }
            Log.d("hailong12", " msg obj is " + (String) msg.obj);
            switch ((String) msg.obj) {
                case RC_Not_Ready:
                case RC_Problem:
                    displayStatus("设备故障");
                    displayStatusTimer1.cancel();
                    displayStatusTimer1.start();
                    break;
                case RC_Connect_Fail:
                    displayStatus("连接失败");
                    displayStatusTimer1.cancel();
                    displayStatusTimer1.start();
                    break;
                case RC_Connect_Success:
                    deviceReady = true;
                    displayStatus("连接成功");
                    displayStatusTimer1.cancel();
                    displayStatusTimer1.start();
                    break;
                case RC_Testing:
                    isTesting = true;
                    displayStatusTimer_1.cancel();
                    ShutDownTimer_1.cancel();
                    ShutDownTimer_1.start();
                    displayStatus("测试中...");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            waitResult();
                        }
                    }, 1000);
                    break;

            }

        }
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
//                toast("测试超时");
                dec_status.setText("请打开外设");
                Log.d("hailong12", "Result onFinish ");
            }

        };
        resultCountDownTimer.start();
    }

    //3s
    CountDownTimer icReadTimer;

    void icRead() {
        icReadTimer = new CountDownTimer(3000, 200) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }

        };
    }

    void initData() {
        result_ic.setOnClickListener(this);
        result_ok.setOnClickListener(this);
//        menu_connect.setOnClickListener(this);
        device_reconnect.setOnClickListener(this);
        menu_start.setOnClickListener(this);
        test_delete.setOnClickListener(this);
        result_test_again.setOnClickListener(this);
//        result_cancel.setOnClickListener(this);
        result_best_save.setOnClickListener(this);
        menu_test_1.setOnClickListener(this);
        menu_test_2.setOnClickListener(this);
        menu_test_3.setOnClickListener(this);
//        onTestConnect();
        device_status.setText("正在连接设备...");
    }

    void initView() {
        View lianjie = findViewById(R.id.lianjie);
        lianjie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lianjie(view);
            }
        });
        View aaa = findViewById(R.id.aaa);
        aaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aaa(view);
            }
        });
        View bbb = findViewById(R.id.bbb);
        bbb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bbb(view);
            }
        });
        View ccc = findViewById(R.id.ccc);
        ccc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ccc(view);
            }
        });
        start_label = (TextView) findViewById(R.id.start_label);
        result_settings = findViewById(R.id.result_settings);
        code_et = (EditText) findViewById(R.id.code_et);
        code_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                result_ok.performClick();
                return false;
            }
        });
        code_delete = findViewById(R.id.code_delete);
        code_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code_et.setText("");
            }
        });
        result_ic = findViewById(R.id.result_ic);
        result_ok = findViewById(R.id.result_ok);

        device_status = (TextView) findViewById(R.id.device_status);
        dec_status = (TextView) findViewById(R.id.dec_status);
//        menu_connect = findViewById(R.id.menu_connect);
        device_reconnect = findViewById(R.id.device_reconnect);
        menu_start = findViewById(R.id.menu_start);
        menu_start_icon = (ImageView) findViewById(R.id.menu_start_icon);
        menu_start_label = (TextView) findViewById(R.id.menu_start_label);
        start_test = findViewById(R.id.start_test);
        test_score_des = (TextView) findViewById(R.id.test_score_des);
        result_container = findViewById(R.id.result_container);
        test_avatar = (ImageView) findViewById(R.id.test_avatar);
        result_name = (TextView) findViewById(R.id.result_name);
        result_sex = (TextView) findViewById(R.id.result_sex);
        result_class = (TextView) findViewById(R.id.result_class);
        result_code = (TextView) findViewById(R.id.result_code);
        test_delete = findViewById(R.id.test_delete);

//        score_count = (TextView) findViewById(R.id.score_count);
        score_label = (TextView) findViewById(R.id.score_label);

        menu_test_1 = findViewById(R.id.menu_test_1);
        menu_test_2 = findViewById(R.id.menu_test_2);
        menu_test_3 = findViewById(R.id.menu_test_3);
        result_operation = findViewById(R.id.result_operation);
//        result_cancel = findViewById(R.id.result_cancel);
        result_test_again = findViewById(R.id.result_test_again);
        result_best_save = findViewById(R.id.result_best_save);
        animate_view = (ImageView) findViewById(R.id.animate_view);
        animate_view.setBackgroundResource(R.drawable.feihuoliang_anim);
        animationDrawable = (AnimationDrawable) animate_view.getBackground();
    }

    public byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    @Override
    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                String result = AppUtils.bytesToHexString(buffer);
                String code = result.replace("\n", "").replace("\r", "");
                Log.d("hailong30", "BB code is " + code);
                if (!StringUtils.isEmpty(code)) {
                    //如果是C5开头，则取后五位数字
                    //回0XC5+data1+data2+data3+data4+data5
                    //data1~data5，每个1个byte，对应一个数, ASCII码形式
                    String data = "";
                    if (code.startsWith(RC_Success)) {
                        if (testCount < 3) {
                            String data1 = code.substring(4, 14);
                            //讲ASCII转换成十进制再转换成对应的字符
                            for (int i = 0; i < data1.length(); i += 2) {
                                String convert = data1.substring(i, i + 2);
                                data += AppUtils.asciiToStr(convert);
                            }

                            if (data.startsWith("0000000000")) {
                                //正在测试
                                menu_start_icon.setVisibility(View.GONE);
                                menu_start_label.setVisibility(View.VISIBLE);
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
                            dismissStatus();
                            toast("测试完毕");

                            if (countDownTimer1 != null) {
                                countDownTimer1.cancel();
                                countDownTimer1 = null;
                            }
                            if (resultCountDownTimer != null) {
                                resultCountDownTimer.cancel();
                                resultCountDownTimer = null;
                            }

                            playTime = 0;
                            resultThread = new ResultThread(score);
                            resultThread.start();
                        }
                    } else if (!code.startsWith(RC_Success)) {
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

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer1 != null) {
            countDownTimer1.cancel();
            countDownTimer1 = null;
        }
    }

    CountDownTimer countDownTimer1;
    int playTime = 0;

    private void displayStatus(String msg) {
        menu_start_icon.setVisibility(View.GONE);
        menu_start_label.setVisibility(View.VISIBLE);
        menu_start_label.setText(msg);
    }

    private void dismissStatus() {
        menu_start_icon.setVisibility(View.VISIBLE);
        menu_start_label.setVisibility(View.GONE);
    }

    CountDownTimer displayStatusTimer1 = new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            dismissStatus();
        }
    };

    CountDownTimer devIdleTimer1 = new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            displayStatus("请打开外设");
            displayStatusTimer1.cancel();
            displayStatusTimer1.start();
        }
    };
    ResultThread resultThread;

    class ResultThread extends Thread {
        String data;

        public ResultThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            super.run();
            getResultSuccess(data);
        }

        private void getResultSuccess(final String data) {
            if (data == null) {
                return;
            }
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
            }
            ensureMp("test_score.ogg");
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
                        countDownTimer1 = new CountDownTimer((duration) * (size + 1), duration) {
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
                        countDownTimer1.start();
                    }
                });

            }

            synchronized (resultList) {
                resultList.add(data);
            }
            Log.d("hailong60", "FHL result is " + data);
            testCount++;
            isTesting = false;
            testOk = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    selectPos = testCount - 1;
                    try {
                        score_label.setText(String.valueOf(Float.parseFloat(data)));
                    } catch (Exception e) {

                    }
                    handleTestCount();
                }
            });

        }

    }

    byte[] pinBytes = new byte[]{
            0X09, 0X06, 0X60, 0X00,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF,
            (byte) 0XFF, (byte) 0XFF, (byte) 0XFF//(06 为命令字, 60 为密码 A(61 为密码 B), 00为扇区号, 12 个 F 为密码)

    };

    //下载密码
    public void loadPin() {
        try {
            isPinLoad = true;
            if (mOutputStream != null) {
                mOutputStream.write(pinBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startICSeriaport() {
        try {
            // if (mSerialPort == null) {
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
//            mReadThread_ic = new ReadThread_IC();
//                mReadThread_ic.setDaemon(true);
//            mReadThread_ic.start();
            // }
            ICHandler.post(mRunnable);
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.result_ic:
//                pause();
                ensureMp("ic_put_correct.ogg", tip);
                isCodeOk = isDataOk = isPinLoad = false;
//                closeSerialPort();
                startICSeriaport();
                loadPin();
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
//            case R.id.result_cancel:
//                handleResultCancel();
//                break;
            case R.id.result_best_save:
                //保存数据
                handleResultSave();
                break;
            case R.id.device_reconnect:
                onTestConnect();
                device_status.setText("正在连接设备...");
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
                    toast("正在测试，请稍候");
                    return;
                }
                //如果测试次数大于三次，不允许
                if (testCount >= 3) {
                    toast("最多只能测试三次");
                    return;
                }

                ensureMp("start_test.ogg", tip);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ensureMp("feihuoliang_chuiqi.ogg", tip);
                    }
                }, 1500);
                animationDrawable.start();
                //开始测试
                write(Send_Start_Test);
                devIdleTimer1.cancel();
                devIdleTimer1.start();
                break;
            case R.id.menu_test_1:
                if (resultList != null) {
                    if (resultList.size() > 0) {
                        score_label.setText(resultList.get(0));
                    }
                    selectPos = 0;
                }
                break;
            case R.id.menu_test_2:
                if (resultList != null) {
                    if (resultList.size() > 1) {
                        score_label.setText(resultList.get(1));
                    }
                    selectPos = 1;
                }
                break;
            case R.id.menu_test_3:
                if (resultList != null) {
                    if (resultList.size() > 2) {
                        score_label.setText(resultList.get(2));
                    }
                    selectPos = 2;
                }
                break;
            case R.id.result_test_again:
//                menu_start.performClick();
                handleResultCancel();
                break;


        }
    }

    int selectPos = 0;

    //模拟连接成功
    public void lianjie(View view) {
//        Message message = new Message();
//        message.obj = RC_Connect_Success;
//        mHandler.sendMessage(message);
    }

    //模拟第一次数据
    public void aaa(View view) {
        onPause();
        playTime = 0;
        resultThread = new ResultThread("1534.6");
        resultThread.start();
    }

    //模拟第二次数据
    public void bbb(View view) {
        onPause();
        playTime = 0;
        resultThread = new ResultThread("34.6");
        resultThread.start();
    }

    //模拟第三次数据
    public void ccc(View view) {
        onPause();
        playTime = 0;
        resultThread = new ResultThread("6738.6");
        resultThread.start();
    }

    protected void onTestConnect() {
        //1，发送0113，hold 300ms
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
//                }
//            }
//
//        };
//        connectCountDownTimer.start();
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
                userInfos = UserModel.queryUserInfoByCode(FeihuoliangSingleTestActivity.this, search);
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            dataImportDialog.dismiss();
            if (userInfos.isEmpty()) {
                ensureMp("invalid_code.ogg", tip);
            } else {
                showUserInfo(userInfos.get(0));
            }
        }
    }

    private void handleTestCount() {
        ShutDownTimer_1.cancel();
        if (testCount == 0) {
            menu_test_1.setVisibility(View.INVISIBLE);
            menu_test_2.setVisibility(View.INVISIBLE);
            menu_test_3.setVisibility(View.INVISIBLE);
        } else if (testCount == 1) {
            menu_test_1.setVisibility(View.VISIBLE);
            menu_test_2.setVisibility(View.INVISIBLE);
            menu_test_3.setVisibility(View.INVISIBLE);
        } else if (testCount == 2) {
            menu_test_1.setVisibility(View.VISIBLE);
            menu_test_2.setVisibility(View.VISIBLE);
            menu_test_3.setVisibility(View.INVISIBLE);
        } else if (testCount == 3) {
            menu_test_1.setVisibility(View.VISIBLE);
            menu_test_2.setVisibility(View.VISIBLE);
            menu_test_3.setVisibility(View.VISIBLE);
        }
        if (testCount > 0) {
            device_status.setText("测试完毕");
            result_operation.setVisibility(View.VISIBLE);
            if (resultCountDownTimer != null) {
                resultCountDownTimer.cancel();
            }
        } else {
//            device_status.setText("测试完毕");
            device_status.setText("连接成功");
            dec_status.setText("连接成功");
            result_operation.setVisibility(View.GONE);
        }

    }

    //数据取消
    void handleResultCancel() {
        testCount--;
        if (testCount < 0) {
            testCount = 0;
        }
        synchronized (resultList) {
            if (resultList != null) {
                if (resultList.size() > 0) {
                    if (selectPos >= 0 && selectPos < resultList.size()) {
                        resultList.remove(selectPos);
                    }
                }
            }
        }
        if (selectPos == resultList.size()) {
            selectPos -= 1;
        }
        if (selectPos < 0) {
            selectPos = 0;
        }
        if (resultList.size() == 0) {
            score_label.setText("0");
            testOk = testComplete = true;
            isTesting = false;
        }
        //自动跳到后一次
        try {
            if (selectPos < resultList.size()) {
                score_label.setText(String.valueOf(Integer.parseInt(resultList.get(selectPos))));
            }
        } catch (Exception e) {

        }
        // test_score_des.setText("第" + (selectPos + 1) + "次成绩:");
        handleTestCount();
    }

    //保存成绩到数据库
    void handleResultSave() {
        float max = 0;
        synchronized (resultList) {
            try {
                for (String str : resultList) {
                    float data = Float.parseFloat(str);
                    if (max < data) {
                        max = data;
                    }
                }
            } catch (Exception e) {

            }
        }
        //保存最好的成绩
        if (userInfo != null) {
            UserModel.updateUserInfoInFeihuoliang(FeihuoliangSingleTestActivity.this, userInfo, String.valueOf(max));
            toast("最好成绩" + String.valueOf(max) + "保存成功");
            reset();
        }
    }

    private void reset() {
        resultList.clear();
        result_settings.setVisibility(View.VISIBLE);
        result_container.setVisibility(View.GONE);
        testCount = 0;
        isTesting = testComplete = testOk = false;
        selectPos = 0;
//        test_score_des.setText("第" + (selectPos + 1) + "次成绩:");
        score_label.setText("0");
        menu_start_icon.setVisibility(View.VISIBLE);
        menu_start_label.setVisibility(View.GONE);
//        ((TextView)result_ic).setText("读卡");
        handleTestCount();
    }

    void handleSearchResult() {
        String str = code_et.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(FeihuoliangSingleTestActivity.this, "正在查询...");
        dataImportDialog.setCancelable(false);

        loadTask = new LocalLoadTask();
        loadTask.execute(str);
    }
}
