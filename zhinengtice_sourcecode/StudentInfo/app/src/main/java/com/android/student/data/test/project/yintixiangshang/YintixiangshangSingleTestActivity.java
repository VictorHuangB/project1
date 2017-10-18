package com.android.student.data.test.project.yintixiangshang;

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
 * 引体向上单人测试页面
 * Created by hailong on 2016/11/9 0009.
 */
public class YintixiangshangSingleTestActivity extends SerialPortActivity implements View.OnClickListener {
    boolean isTesting;
    boolean testOk;
    TextView start_label;
    View result_settings;
    View result_ic, result_ok;
    EditText code_et;
    View code_delete;

    View start_test;
    TextView test_score_des;
    TextView score_label;

    View result_container;
    ImageView test_avatar;
    TextView result_name;
    TextView result_sex;
    TextView result_class;
    TextView result_code;
    View test_delete;

    View menu_start;//开始测试
    ImageView menu_start_icon;
    TextView menu_start_label;
    View result_test_cancel;
    View result_best_save;

    ImageView animate_view;
    AnimationDrawable animationDrawable;

    TextView time_counter;//倒计时时间
    View time_modify;//倒计时修改

    LocalLoadTask loadTask;
    DataImportDialog dataImportDialog;

    UserInfo userInfo;

    int slideSeconds;
    CountDownTimer resultCountDownTimer;
    CountDownTimer timerCounter;//倒计时
    FHLHandler mHandler = new FHLHandler();
    boolean deviceReady = true;

    private static final int Max_Result_Time = 3 * 80 * 1000;//等待结果的时间可能会有点长，这个时间不好控制,暂定3分钟
    private static final int Hold_Time = 800;//800ms发送一次

    private static final String Send_Start_Test = "0116";//开始测试
    private static final String Send_Get_Result = "0114";//获取测试数据

    private static final String RC_Testing = "01C4";//测试中
    private static final String RC_Problem = "01C1";//外设故障
    private static final String RC_Success = "01C5";//测试完毕


    boolean isPinLoad = false;//密码下载成功
    boolean isCodeOk = false;//IC卡学号读取完毕
    boolean isDataOk = false;//IC卡数据读取完毕
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    private SerialPort mSerialPort = null;
    //模拟0扇区02块的基本信息（除了姓名和学号）

    //UTF
    //Write UTF-16
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
//                YangwoqizuoSingleTestActivity.this.finish();
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
                IcSameUserDialog selectDialog = new IcSameUserDialog(YintixiangshangSingleTestActivity.this, oldUserInfo, newUserInfo);
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
                    if (UserModel.checkSameUser(this, icCode)) {
                        //拿到本地相同code的信息
                        ArrayList<UserInfo> userInfos = UserModel.queryUserInfoByEntireCode(this, icCode);
                        if (userInfos != null && userInfos.size() > 0) {
                            toast("学号重复，请选择");
                            showSameUserDialog(userInfos.get(0), info);
                        }
                        return;
                    }
                    UserModel.addItemToDatabase(this, info);
                    showUserInfo(info);
                    toast("读卡成功");
                }
                isDataOk = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yintixiangshang_test_layout);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initView();
        initData();
    }

    long lastRCTime;//上次接收数据时间
    long currentRCTime;//当前接收数据时间

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
            final AppDialog appDialog = new AppDialog(this, "取消", "确定", true);

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
            final AppDialog appDialog = new AppDialog(this, "取消", "确定", true);

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
        closeIc();
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        if (timerCounter != null) {
            timerCounter.cancel();
        }
        super.onDestroy();
    }

    class FHLHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((String) msg.obj) {
                case RC_Testing:
                    isTesting = true;
                    break;
                case RC_Problem:
                    toast("设备出故障了");
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
                toast("测试超时");
                Log.d("hailong12", "Result onFinish ");
            }

        };
        resultCountDownTimer.start();
    }

    void initData() {
        result_ic.setOnClickListener(this);
        result_ok.setOnClickListener(this);
        menu_start.setOnClickListener(this);
        test_delete.setOnClickListener(this);
        result_test_cancel.setOnClickListener(this);
        result_best_save.setOnClickListener(this);
        time_modify.setOnClickListener(this);
    }

    void initView() {
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
                playTime = 0;
                currentResult = "12";
                mHandler.removeCallbacks(resultSuccessRunnable);
                mHandler.postDelayed(resultSuccessRunnable, 2 * 1000);
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

        score_label = (TextView) findViewById(R.id.score_label);

        result_test_cancel = findViewById(R.id.result_test_cancel);
        result_best_save = findViewById(R.id.result_best_save);

        time_counter = (TextView) findViewById(R.id.time_counter);
        time_modify = findViewById(R.id.time_modify);

        animate_view = (ImageView) findViewById(R.id.animate_view);
        animate_view.setBackgroundResource(R.drawable.yintixiaingshang_anim);
        animationDrawable = (AnimationDrawable) animate_view.getBackground();
    }

    Runnable resultSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            //测试完毕
            isTesting = false;
            testOk = true;
            animationDrawable.stop();
            if (resultCountDownTimer != null) {
                resultCountDownTimer.cancel();
            }
            menu_start_icon.setVisibility(View.VISIBLE);
            menu_start_label.setVisibility(View.GONE);
            resultThread = new ResultThread(currentResult);
            resultThread.start();
        }
    };

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
                        mHandler.removeCallbacks(resultSuccessRunnable);
                        mHandler.postDelayed(resultSuccessRunnable, 10 * 1000);
                        int pos = data.indexOf(".");
                        if (pos >= 0) {
                            data = data.substring(0, pos);
                        }
                        if (countDownTimer1 != null) {
                            countDownTimer1.cancel();
                            countDownTimer1 = null;
                        }
                        if (resultCountDownTimer != null) {
                            resultCountDownTimer.cancel();
                            resultCountDownTimer = null;
                        }

                        playTime = 0;
                        currentResult = data;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    score_label.setText(String.valueOf(Integer.parseInt(currentResult)));
                                } catch (Exception e) {

                                }
                            }
                        });

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
    String currentResult;
    int currentSeconds = 60;
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
            ensureMp("test_over.ogg", tip);
            synchronized (resultThread) {
                try {
                    wait(1800);
                } catch (Exception e) {

                }
            }
            ensureMp("test_score.ogg");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        score_label.setText(String.valueOf(Integer.parseInt(data)));
                    } catch (Exception e) {

                    }
                }
            });
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
            mSerialPort = getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

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
                ensureMp("ic_put_correct.ogg", tip);
                isCodeOk = isDataOk = isPinLoad = false;
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
            case R.id.result_best_save:
                if (isTesting) {
                    toast("正在测试，请稍候");
                    return;
                }
                //保存数据
                handleResultSave();
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
                isTesting = true;
                ensureMp("start_test.ogg", tip);
//                ensureMp("test_over.ogg", tip);
                animationDrawable.start();
                menu_start_icon.setVisibility(View.GONE);
                menu_start_label.setVisibility(View.VISIBLE);
                //开始测试
                write(Send_Start_Test);
                //开始倒计时
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        waitResult();
                    }
                }, 2000);
                break;
            case R.id.result_test_cancel:
//                if (isTesting) {
//                    toast("正在测试，请稍候");
//                    return;
//                }
                handleResultCancel();
                break;
            case R.id.time_modify:
                if (isTesting) {
                    toast("正在测试中，请稍候再试");
                    return;
                }
                //弹出框修改秒数
                final TimeCounterDialog appDialog = new TimeCounterDialog(YintixiangshangSingleTestActivity.this, "取消", "确定", true, time_counter.getText().toString());
                appDialog.setupContentView();
                appDialog.setDialogClickListener(new TimeCounterDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog.dismiss();
                    }

                    @Override
                    public void operationRight(String seconds) {
                        time_counter.setText(seconds);
                        currentSeconds = Integer.parseInt(seconds);
                    }

                });
                appDialog.show();
                break;

        }
    }

    //模拟第一次数据
    public void aaa(View view) {
        onPause();
        playTime = 0;
        currentResult = "50";
        menu_start_icon.setVisibility(View.VISIBLE);
        menu_start_label.setVisibility(View.GONE);
        resultThread = new ResultThread("50");
        resultThread.start();
    }

    class LocalLoadTask extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
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
                userInfos = UserModel.queryUserInfoByCode(YintixiangshangSingleTestActivity.this, search);
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

    //数据取消
    void handleResultCancel() {
        if (timerCounter != null) {
            timerCounter.cancel();
        }
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        currentResult = null;
        menu_start_icon.setVisibility(View.VISIBLE);
        menu_start_label.setVisibility(View.GONE);
        score_label.setText("0");
        isTesting = testOk = false;
        time_counter.setText(String.valueOf(currentSeconds));
    }

    //保存成绩到数据库
    void handleResultSave() {
        if (userInfo == null) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        if (isTesting) {
            toast("正在测试，请稍候");
            return;
        }
        if (StringUtils.isEmpty(currentResult) || Integer.parseInt(currentResult) <= 0) {
            toast("无效测试成绩");
            return;
        }
        //保存最好的成绩
        if (userInfo != null) {
            UserModel.updateUserInfoInYintixiangshang(this, userInfo, currentResult);
            toast("保存成功");
            reset();
        }
    }

    private void reset() {
        if (timerCounter != null) {
            timerCounter.cancel();
        }
        if (resultCountDownTimer != null) {
            resultCountDownTimer.cancel();
        }
        currentResult = null;
        mHandler.removeCallbacks(resultSuccessRunnable);
        result_settings.setVisibility(View.VISIBLE);
        result_container.setVisibility(View.GONE);
        isTesting = testOk = false;
        score_label.setText("0");
        menu_start_icon.setVisibility(View.VISIBLE);
        menu_start_label.setVisibility(View.GONE);
        time_counter.setText(String.valueOf(currentSeconds));
    }

    void handleSearchResult() {
        String str = code_et.getText().toString().replace(" ", "");
        if (str.isEmpty() || str.length() < 2) {
            ensureMp("invalid_code.ogg", tip);
            return;
        }
        dataImportDialog = new DataImportDialog(this, "正在查询...");
        dataImportDialog.setCancelable(false);

        loadTask = new LocalLoadTask();
        loadTask.execute(str);
    }
}
