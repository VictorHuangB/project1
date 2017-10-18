package com.android.student.data.test.project;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.DataExportActivity;
import com.android.student.data.test.DataManagerActivity;
import com.android.student.data.test.LevelAnalysisActivity;
import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.ScoreSortActivity;
import com.android.student.data.test.SingleSearchActivity;
import com.android.student.data.test.SystemSettingsActivity;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.db.UserSettings;
import com.android.student.data.test.dialog.AdminDialog;
import com.android.student.data.test.dialog.ChooseDeviceDialog;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.dialog.ImportTypeSelectDialog;
import com.android.student.data.test.studentinfo.StudentColomn;
import com.android.student.data.test.utils.CopyDirectoryUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Sheet;
import jxl.Workbook;

import static com.android.student.data.test.R.id.project_title;

/**
 * 入口页面
 * Created by hailong on 2016/10/18 0018.
 */
public class LaunchActivity extends Activity implements View.OnClickListener {
    View menu_single;//单项查询
    //    View menu_complex;//综合查询
    View menu_score;//成绩查询

    View menu_level;//水平分析
    View menu_download;//名单下载
    TextView menu_download_label;
    View menu_upload;//导出数据
    View menu_settings;
    View menu_start_single;
    View menu_start_group;
    //    View menu_connect;//连接外设
    View menu_datamanager;//数据管理
    boolean hasImport = false;

    protected Class<?> clz;//启动哪个单人测试页面
    protected Class<?> groupclz;//启动哪个多人测试页面
    LocalLoadTask loadTask;
    String[] avatars;
    boolean loadComplete = false;
    boolean override = false;
//    protected DeviceConnectDialog deviceConnectDialog;
    /**
     * xls保存位置在根目录/学生名单/学生信息.xls
     */
    public static final String Datalist_path =
            Environment.getExternalStorageDirectory() + "/学生名单/";

    public static final String Pictures_path =
            Environment.getExternalStorageDirectory() + "/学生名单/学生头像/";

    public static final String Pictures_path_New =
            Environment.getExternalStorageDirectory() + "/学生名单/学生头像1/";

    public static final String Pictures_Store_path =
            Environment.getExternalStorageDirectory() + "/学生名单/学生头像/";

    /**
     * U盘位置
     */
    public static final String USB_Datalist_path = "/mnt/usbhost1/";
    /**
     * U盘新位置
     */
    public static final String USB_Datalist_Extra_path0 = "/mnt/usbhost0/";

    /**
     * U盘新位置
     */
    public static final String USB_Datalist_Extra_path2 = "/mnt/usbhost2/";

    /**
     * U盘新位置
     */
    public static final String USB_Datalist_Extra_path3 = "/mnt/usbhost3/";

    boolean isUsbType = false;
    String targetPath = "学生名单";
    String[] usbPath = new String[2];
    DataImportDialog dataImportDialog;
    ArrayList<String> codeList = new ArrayList<>();
    ArrayList<Long> idList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_layout);

        Animation animation = (Animation) AnimationUtils.loadAnimation(this, R.anim.launch_anim);
        LayoutAnimationController lac = new LayoutAnimationController(animation);
        lac.setOrder(LayoutAnimationController.ORDER_RANDOM);
        lac.setDelay(0.5f);//注意这个地方是以秒为单位，是浮点型数据，所以要加f
        ((LinearLayout) findViewById(R.id.menu_container1)).setLayoutAnimation(lac);
        ((LinearLayout) findViewById(R.id.menu_container2)).setLayoutAnimation(lac);
        ((LinearLayout) findViewById(R.id.menu_container3)).setLayoutAnimation(lac);
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        hasImport = sharedPreferences.getBoolean("hasImport", hasImport);
        initViews();
        if (hasImport) {
            menu_download_label.setText("继续下载");
        }
        IntentFilter intentFilter = new IntentFilter(MainApplication.Import_Complete_Action);
        intentFilter.addAction(MainApplication.Import_Init_Action);
        intentFilter.addAction(MainApplication.Import_Task_Cancel_Action);
        intentFilter.addAction(MainApplication.Import_Task_Local_Action);
        intentFilter.addAction(MainApplication.Import_Task_Usb_Action);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void setProjectToast(String project) {
        ((TextView) findViewById(project_title)).setText(project);
    }

    public void cancelTask() {
        flag = true;
        hasImport = false;
        override = false;
        if (loadTask != null) {
            loadTask.cancel(true);
        }
        toast("下载取消");
        loadTask = null;
    }

    protected void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(LaunchActivity.this, msg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.Import_Complete_Action.equals(intent.getAction())) {
                hasImport = true;
                menu_download_label.setText("继续下载");
            } else if (MainApplication.Import_Init_Action.equals(intent.getAction())) {
                hasImport = false;
                menu_download_label.setText("名单下载");
            } else if (MainApplication.Import_Task_Cancel_Action.equals(intent.getAction())) {
                hasImport = false;
                menu_download_label.setText("名单下载");
                cancelTask();
            } else if (MainApplication.Import_Task_Local_Action.equals(intent.getAction())) {
                dataImportDialog = new DataImportDialog(context, "正在读取本地学生名单，请稍候...");
                dataImportDialog.setType(1);
                dataImportDialog.setCancelable(true);
                dataImportDialog.setShowCancel(true);
                dataImportDialog.setDataCancelListener(new DataImportDialog.DataCancelListener() {
                    @Override
                    public void cancelData() {
                        hasImport = false;
                        menu_download_label.setText("名单下载");
                        cancelTask();
                    }
                });
                dataImportDialog.show();
                isUsbType = false;
                flag = false;
                loadTask = new LocalLoadTask();
                loadTask.execute(Pictures_path, Datalist_path);
            } else if (MainApplication.Import_Task_Usb_Action.equals(intent.getAction())) {
                dataImportDialog = new DataImportDialog(context, "正在从U盘读取本地学生名单，如果名单较多可能花费几分钟时间，请耐心等候...");
                dataImportDialog.setType(1);
                dataImportDialog.setCancelable(true);
                dataImportDialog.setShowCancel(true);
                dataImportDialog.setDataCancelListener(new DataImportDialog.DataCancelListener() {
                    @Override
                    public void cancelData() {
                        hasImport = false;
                        menu_download_label.setText("名单下载");
                        cancelTask();
                    }
                });
                dataImportDialog.show();
                isUsbType = true;
                flag = false;
                loadTask = new LocalLoadTask();
                refreshUSBFileList(USB_Datalist_path);
                Log.d("hailong13", " paths " + usbPath[0]);
                boolean isEmpty = StringUtils.isEmpty(usbPath[1]);
                if (usbPath != null && !isEmpty) {
                    Log.d("hailong20", " paths " + usbPath[0]);
                    loadTask.execute(usbPath);
                } else {
                    refreshUSBFileList(USB_Datalist_Extra_path0);
                    if (usbPath != null && !isEmpty) {
                        loadTask.execute(usbPath);
                    } else {
                        refreshUSBFileList(USB_Datalist_Extra_path2);
                        if (usbPath != null && !isEmpty) {
                            loadTask.execute(usbPath);
                        } else {
                            refreshUSBFileList(USB_Datalist_Extra_path3);
                            if (usbPath != null && !isEmpty) {
                                loadTask.execute(usbPath);
                            } else {
                                dataImportDialog.dismiss();
                                toast("U盘没有找到学生名单路径");
                            }
                        }
                    }
                }
            }

        }
    };

    public void refreshUSBFileList(String strPath) {
        File dir = new File(strPath);
        File[] files = dir.listFiles();

        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory() && !targetPath.equals(file.getName())) {
                refreshUSBFileList(file.getAbsolutePath());
            } else {
                if (file.getAbsolutePath().contains(targetPath)) {
                    usbPath[0] = file.getAbsolutePath() + "/学生头像/";
                    usbPath[1] = file.getAbsolutePath() + "/";
                }
            }

        }
    }

    boolean existColum(String column) {
        for (String col : StudentColomn.studentColomn) {
            if (column.equals(col)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    class Task implements Runnable {
        ArrayList<UserInfo> userInfos = new ArrayList<>();

        public Task(ArrayList<UserInfo> userInfos) {
            this.userInfos = userInfos;
        }

        @Override
        public void run() {
            UserModel.addItemToDatabaseAsBulk(LaunchActivity.this, userInfos);
        }
    }

    //    int score = 56;
    boolean flag;

    class LocalLoadTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            //删除数据库
            int count = UserModel.deleteAll(LaunchActivity.this);
            Log.d("hailong15", " count is " + count);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //删除数据库
            int count = UserModel.deleteAll(LaunchActivity.this);
            Log.d("hailong15", "AA count is " + count);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            if (isCancelled()) {
                return;
            }
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            MainApplication.classInfos.clear();
            MainApplication.gradeInfos.clear();
            final ArrayList<UserInfo> userInfos = new ArrayList<>();
            final ArrayList<UserInfo> updateUserInfos = new ArrayList<>();
            if (isUsbType) {
                try {
                    CopyDirectoryUtils.copyDirectiory(params[0], Pictures_path);
                } catch (Exception e) {

                }
                params[0] = Pictures_path;
            }
            try {
                File baseFile = new File(params[0]);
                avatars = baseFile.list();
            } catch (Exception e) {

            }
            HashMap<Integer, String> kvMap = new HashMap<>();
            try {
                File baseFile = new File(params[1]);
                FileInputStream fileInputStream = null;
                if (baseFile == null) {
                    return false;
                }
                for (File file : baseFile.listFiles()) {
                    if (file.getName().contains("xls")) {
                        Log.d("hailong13", " xls find it ");
                        fileInputStream = new FileInputStream(file);
                        break;
                    }
                }
                if (fileInputStream == null) {
                    return false;
                }
                Workbook book = Workbook.getWorkbook(fileInputStream);
                Sheet sheet = book.getSheet(0);
                SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String columStr = "";
                if (sheet != null) {
                    int Rows = sheet.getRows();
                    int Cols = sheet.getColumns();
                    keep:
                    for (int i = 0; i < Rows; ++i) {
                        UserInfo info = new UserInfo();
                        for (int j = 0; j < Cols; ++j) {
                            if (flag) {
                                MainApplication.classInfos.clear();
                                MainApplication.gradeInfos.clear();
                                return false;
                            }
                            final String content = sheet.getCell(j, i).getContents();
                            //针对第一项，记录列名
                            if (i == 0) {
                                if (existColum(content)) {
                                    if (j != Cols - 1) {
                                        columStr += (content + "=");
                                    } else {
                                        columStr += content;
                                    }
                                    kvMap.put(j, content);
                                }
                            } else {
                                for (Integer integer : kvMap.keySet()) {
                                    if (j == integer) {
                                        switch (kvMap.get(integer)) {
                                            case StudentColomn.NAME:
                                                info.name = content;
                                                break;
                                            case StudentColomn.CODE:
                                                info.code = content;
                                                break;
                                            case StudentColomn.SEX:
                                                info.sex = content;
                                                break;
                                            case StudentColomn.CLASSES:
                                                info.classes = content;
                                                break;
                                            case StudentColomn.GRADE:
                                                info.grade = content;
                                                break;
                                            case StudentColomn.ID_CARD:
                                                info.id_card = content;
                                                break;
                                            case StudentColomn.RACE:
                                                info.race = content;
                                                break;
                                            case StudentColomn.JIGUAN:
                                                info.jiguan = content;
                                                break;
                                            case StudentColomn.ADDRESS:
                                                info.address = content;
                                                break;
                                            case StudentColomn.JOB:
                                                info.job = content;
                                                break;
                                        }
                                    }
                                }
                            }

                        }
                        if (i != 0) {
                            if (avatars != null && i - 1 < avatars.length) {
                                boolean find = false;
                                String avater = "";
                                int size = avatars.length;
                                for (int k = 0; k < size; k++) {
                                    String a = avatars[k];
                                    if (a.startsWith(info.code)) {
                                        find = true;
                                        avater = a;
                                        break;
                                    }
                                }

                                if (find) {
                                    try {
                                        info.avater_label = params[0] + avater;
                                    } catch (Exception e) {

                                    }
                                }
                            }

                            //加入数据库
                            if (override) {
                                if (codeList.contains(info.code.toString())) {
                                    int index = codeList.indexOf(info.code.toString());
                                    try {
                                        info.id = idList.get(index);
                                    } catch (Exception e) {

                                    }
                                    updateUserInfos.add(info);
                                } else {
                                    userInfos.add(info);
                                }

                                if (MainApplication.classInfos.isEmpty()) {
                                    MainApplication.classInfos.add(info.classes);
                                } else {
                                    if (!MainApplication.classInfos.contains(info.classes.toString())) {
                                        MainApplication.classInfos.add(info.classes);
                                    }
                                }
                                if (MainApplication.gradeInfos.isEmpty()) {
                                    MainApplication.gradeInfos.add(info.grade);
                                } else {
                                    if (!MainApplication.gradeInfos.contains(info.grade.toString())) {
                                        MainApplication.gradeInfos.add(info.grade);
                                    }
                                }
                            } else if (!override) {
                                userInfos.add(info);
                                if (MainApplication.classInfos.isEmpty()) {
                                    MainApplication.classInfos.add(info.classes);
                                } else {
                                    if (!MainApplication.classInfos.contains(info.classes.toString())) {
                                        MainApplication.classInfos.add(info.classes);
                                    }
                                }
                                if (MainApplication.gradeInfos.isEmpty()) {
                                    MainApplication.gradeInfos.add(info.grade);
                                } else {
                                    if (!MainApplication.gradeInfos.contains(info.grade.toString())) {
                                        MainApplication.gradeInfos.add(info.grade);
                                    }
                                }
                            }
                        }
                    }
                    if (userInfos.size() > 0) {
                        UserModel.addItemToDatabaseAsBulk(LaunchActivity.this, userInfos);
                    }
                    if (updateUserInfos.size() > 0) {
                        UserModel.updateItemToDatabaseAsBulk(LaunchActivity.this, updateUserInfos);
                    }
                    String classes = "";
                    String grades = "";
                    for (int i = 0; i < MainApplication.classInfos.size(); i++) {
                        String aa = MainApplication.classInfos.get(i);
                        if (i != MainApplication.classInfos.size() - 1) {
                            classes += aa + ";";
                        } else {
                            classes += aa;
                        }
                    }
                    for (int i = 0; i < MainApplication.gradeInfos.size(); i++) {
                        String bb = MainApplication.gradeInfos.get(i);
                        if (i != MainApplication.gradeInfos.size() - 1) {
                            grades += bb + ";";
                        } else {
                            grades += bb;
                        }
                    }
                    editor.putString("classes", classes);
                    editor.putString("grades", grades);
                    editor.putString("studentcolumns", columStr);
                    editor.putBoolean("hasImport", true);
                    editor.commit();
                    book.close();
                    sendBroadcast(new Intent(MainApplication.Import_Complete_Action));
                }
            } catch (Exception e) {
            }

            return !kvMap.isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean vo) {
            if (!flag) {
                if (!vo) {
                    toast("没有找到学生名单对应的Excel");
                } else {
                    toast("获取学生名单成功");
                }
            }
            loadComplete = true;
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
//        if (mSendingThread != null) {
//            mSendingThread.interrupt();
//        }
    }

    protected boolean isDeviceReady() {
        return false;
    }

//    protected void testDevice() {
//        onTestDeviceBefore();
//        deviceConnectDialog = new DeviceConnectDialog(LaunchActivity.this, "开始检测设备...");
//        deviceConnectDialog.setCancelable(false);
//        deviceConnectDialog.show();
//    }
//
//    protected void onTestDeviceBefore() {
//
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.menu_settings && v.getId() != R.id.menu_download) {
            if (!hasImport) {
                toast("尚未导入学生名单，请先导入名单");
                return;
            }
        }
        switch (v.getId()) {
            case R.id.menu_single://单项查询
                launchActivity(SingleSearchActivity.class);
                break;
            case R.id.menu_score://成绩查询
                launchActivity(ScoreSortActivity.class);
                break;
            case R.id.menu_level://水平分析
                launchActivity(LevelAnalysisActivity.class);
                break;
            case R.id.menu_download://下载名单
                if (hasImport) {
                    ImportTypeSelectDialog selectDialog = new ImportTypeSelectDialog(LaunchActivity.this);
                    selectDialog.setDialogClickListener(new ImportTypeSelectDialog.DialogClickListener() {
                        @Override
                        public void operationLeft() {
                            //数据库数据清除
//                            int count = UserModel.deleteAll(LaunchActivity.this);
                            MainApplication.classInfos.clear();
                            MainApplication.gradeInfos.clear();
                            getContentResolver().delete(UserSettings.Favorites.CONTENT_URI, null, null);
                            ChooseDeviceDialog dialog = new ChooseDeviceDialog(LaunchActivity.this);
                            //清除设置
                            SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.commit();
                            sendBroadcast(new Intent(MainApplication.Import_Init_Action));
                            dialog.show();
                        }

                        @Override
                        public void operationRight() {
                            ChooseDeviceDialog dialog = new ChooseDeviceDialog(LaunchActivity.this);
                            Item item = UserModel.getCodeList(LaunchActivity.this);
                            codeList = item.codeList;
                            idList = item.idlist;
                            override = true;
                            //sendBroadcast(new Intent(MainApplication.Import_Init_Action));
                            dialog.show();
                        }
                    });
                    selectDialog.show();

                } else {
                    ChooseDeviceDialog dialog = new ChooseDeviceDialog(LaunchActivity.this);
                    dialog.show();
                }
                break;
            case R.id.menu_upload://导出数据
                launchActivity(DataExportActivity.class);
                break;
            case R.id.menu_start_single:
                Intent intent = new Intent(this, clz);
                startActivity(intent);
                overridePendingTransition(
                        R.anim.right_in, R.anim.left_out);
                break;
            case R.id.menu_start_group:
                intent = new Intent(this, groupclz);
                startActivity(intent);
                overridePendingTransition(
                        R.anim.right_in, R.anim.left_out);

                break;
//            case R.id.menu_connect:
//                //测试前的通讯确认工作：
//                testDevice();
//                break;
            case R.id.menu_datamanager:
                Log.d("hailong13", " DataExportChoosePathActivity ");
                launchActivity(DataManagerActivity.class);
//                Cursor cursor = null;
//                try {
//                    cursor = getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
//                            null, null, null, null);
//                } catch (Exception e) {
//                    // Ignore
//                }
//                testDB(cursor);
                break;
            case R.id.menu_settings:
                final AdminDialog appDialog = new AdminDialog(LaunchActivity.this, "取消", "确定", true);
                appDialog.setupContentView();
                appDialog.setDialogClickListener(new AdminDialog.DialogClickListener() {
                    @Override
                    public void operationLeft() {
                        appDialog.dismiss();
                    }

                    @Override
                    public void operationRight(String adminCode) {
                        if (StringUtils.isEmpty(adminCode) || adminCode.length() < 6) {
                            toast("密码格式错误");
                            return;
                        }
                        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                        String pinCode = sharedPreferences.getString("adminCode", "123456");
                        if (adminCode.equals(pinCode)) {
                            appDialog.dismiss();
                            launchActivity(SystemSettingsActivity.class);
                        } else {
                            toast("密码输入错误");
                        }

                    }

                });
                appDialog.show();

                break;
        }
    }

    protected void launchActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(
                R.anim.right_in, R.anim.left_out);
    }

    void testDB(Cursor c) {
        final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);
        final int nameIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.NAME);
        final int codeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CODE);
        final int sexIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SEX);
        final int classIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CLASS);
        final int gradeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.GRADE);
        final int scoreIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SCORE);
        final int levelIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.LEVEL);

        final int headIconIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ICON);

        while (c.moveToNext()) {
            Log.d("hailong12", " Name is " + c.getString(nameIndex) + " code is " + c.getString(codeIndex));
            UserInfo userInfo = new UserInfo();
            userInfo.id = c.getLong(idIndex);
            userInfo.name = c.getString(nameIndex);
            userInfo.code = c.getString(codeIndex);
            userInfo.sex = c.getString(sexIndex);
            userInfo.classes = c.getString(classIndex);
            userInfo.grade = c.getString(gradeIndex);
//            userInfo.score = c.getString(scoreIndex);
            userInfo.level = c.getString(levelIndex);
//            byte[] data = c.getBlob(headIconIndex);
//            if (data != null) {
//                userInfo.headImage = BitmapFactory.decodeByteArray(data, 0, data.length);
//            }
        }
        if (c != null) {
            c.close();
        }

    }


    private void initViews() {
        menu_single = findViewById(R.id.menu_single);
//        menu_complex = findViewById(R.id.menu_complex);
        menu_score = findViewById(R.id.menu_score);
        menu_level = findViewById(R.id.menu_level);
        menu_download = findViewById(R.id.menu_download);
        menu_download_label = (TextView) findViewById(R.id.menu_download_label);

        menu_upload = findViewById(R.id.menu_upload);
        menu_settings = findViewById(R.id.menu_settings);
        menu_start_single = findViewById(R.id.menu_start_single);
        menu_start_group = findViewById(R.id.menu_start_group);
//        menu_connect = findViewById(R.id.menu_connect);
        menu_datamanager = findViewById(R.id.menu_datamanager);

        menu_single.setOnClickListener(this);
//        menu_complex.setOnClickListener(this);
        menu_score.setOnClickListener(this);
        menu_level.setOnClickListener(this);
        menu_download.setOnClickListener(this);
        menu_upload.setOnClickListener(this);
        menu_settings.setOnClickListener(this);
        menu_start_single.setOnClickListener(this);
        menu_start_group.setOnClickListener(this);
//        menu_connect.setOnClickListener(this);
        menu_datamanager.setOnClickListener(this);
    }
}
