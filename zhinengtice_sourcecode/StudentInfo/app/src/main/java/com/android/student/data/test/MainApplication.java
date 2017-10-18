package com.android.student.data.test;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserProvider;
import com.android.student.data.test.rwusb.Global;
import com.android.student.data.test.rwusb.WorkService;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hailong on 2016/10/19 0019.
 */
public class MainApplication extends Application {
    public static final int Project_Weight = 0x1;
    public static final int Project_Height = 0x2;
    public static final int Project_Feihuoliang = 0x3;
    public static final int Project_Tiqianqu = 0x4;
    public static final int Project_Lidingtiaoyuan = 0x5;
    public static final int Project_Yintixiangshang = 0x6;
    public static final int Project_Tiaosheng = 0x7;
    public static final int Project_Yangwoqizuo = 0x8;
    public static final int Project_Changpao_1500 = 0x9;
    public static final int Project_Changpao_1000 = 0xa;
    public static final int Project_Changpao_800 = 0xb;
    public static int Project_Duanpao_100 = 0xc;
    public static int Project_Duanpao_50 = 0xd;

    //身高和体重没有水平分析
    //肺活量
    public static String rule_Feihuoliang_boy = "fhl_boyrule";
    public static String rule_Feihuoliang_girl = "fhl_girlrule";
    //体前屈
    public static String rule_Tiqianqu_boy = "tqq_boyrule";
    public static String rule_Tiqianqu_girl = "tqq_girlrule";
    //仰卧起坐
    public static String rule_Yangwoqizuo_boy = "ywqz_boyrule";
    public static String rule_Yangwoqizuo_girl = "ywqz_girlrule";
    //引体向上
    public static String rule_Yintixiangshang_boy = "ytxs_boyrule";
    public static String rule_Yintixiangshang_girl = "ytxs_girlrule";


    public static int currentProject = -1;
    public static String per = "";
    private static MainApplication mInstance;

    private static float sScreenDensity;

    public static final String Shared_Prf = "student_sharepref";
    public static final String Import_Complete_Action = "com.student.import.complete";
    public static final String Import_Init_Action = "com.student.import.init";
    public static final String Import_Task_Local_Action = "com.student.import.start.local";
    public static final String Import_Task_Usb_Action = "com.student.import.start.usb";
    public static final String Import_Task_Cancel_Action = "com.student.import.cancel";
    public static final String Import_Task_Cancel_Single_Search_Action = "com.student.import.cancel.single";
    public static final String Import_Task_Cancel_Sort_Action = "com.student.import.cancel.sort";
    public static final String Import_Task_Cancel_Level_Analysis_Action = "com.student.import.cancel.level";
    public static final String Import_Task_Cancel_Data_Manager_Action = "com.student.import.cancel.datamanager";
    //    public static ArrayList<UserInfo> userInfos = new ArrayList<>();
    public static ArrayList<String> classInfos = new ArrayList<>();
    public static ArrayList<String> gradeInfos = new ArrayList<>();
    public static ArrayList<UserInfo> searchInfos = new ArrayList<>();

    public static ArrayList<UserInfo> boyInfos = new ArrayList<>();
    public static ArrayList<UserInfo> girlInfos = new ArrayList<>();

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;
    UserProvider userProvider;
    public static Bitmap defaultBitmap;
    public static byte[] bytes;

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//			SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
            String path = sp.getString("DEVICE", "");
            path = "/dev/ttyS4/";
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
            baudrate = 9600;
            Log.d("SerialPort", " baudrate " + baudrate + " path is " + path);
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


    public void setUserProvider(UserProvider provider) {
        userProvider = provider;
    }

    public UserProvider getUserProvider() {
        return userProvider;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InitGlobalString();


        if (null == WorkService.workThread) {
            Intent intent = new Intent(this, WorkService.class);
            startService(intent);
        }
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String classes = sharedPreferences.getString("classes", "");
        String grades = sharedPreferences.getString("grades", "");
        if (!StringUtils.isEmpty(classes) && !StringUtils.isEmpty(grades)) {
            classInfos.clear();
            gradeInfos.clear();

            String[] splitsClas = classes.split("[;]");
            String[] splitsGrad = grades.split("[;]");
            classInfos.addAll(Arrays.asList(splitsClas));
            gradeInfos.addAll(Arrays.asList(splitsGrad));
        }

        mInstance = this;
        sScreenDensity = getResources().getDisplayMetrics().density;
        defaultBitmap = BitmapFactory.decodeResource(MainApplication.getInstance().getResources(), R.drawable.head);
        bytes = AppUtils.flattenBitmap(defaultBitmap);

    }
    private void InitGlobalString() {
        Global.toast_success = getString(R.string.toast_success);
        Global.toast_fail = getString(R.string.toast_fail);
        Global.toast_notconnect = getString(R.string.toast_notconnect);
        Global.toast_usbpermit = getString(R.string.toast_usbpermit);
    }
    public static MainApplication getInstance() {
        return mInstance;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }

}
