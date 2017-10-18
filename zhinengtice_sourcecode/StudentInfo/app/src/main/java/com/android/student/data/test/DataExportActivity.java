package com.android.student.data.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.project.LaunchActivity;
import com.android.student.data.test.studentinfo.StudentColomn;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.CreateExcel;
import com.android.student.data.test.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 数据导出
 * Created by hailong
 */
public class DataExportActivity extends Activity {
    View back_container;//返回按钮
    Spinner search_group_grade_spnner;//集体查询选择年级下拉框
    Spinner search_group_class_spinner;//集体查询选择班级下拉框

    View search_grade_label;
    View search_class_label;
    View export_confirm;
    View export_U;
    boolean gradeSelect = false;
    boolean classSelect = false;
    int classPos = 0;
    int gradePos = 0;
    DataImportDialog groupDialog;
    GroupLoadTask groupLoadTask;
    final ArrayList<String> classlist = new ArrayList<>();
    final ArrayList<String> gradelist = new ArrayList<>();
    boolean isUsb = false;
    String currentPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_export);
        ((TextView) findViewById(R.id.title)).setText("导出数据");
        back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initViews();
        initData();
    }

    void initViews() {
        search_group_grade_spnner = (Spinner) findViewById(R.id.search_group_grade_spinner);
        search_group_class_spinner = (Spinner) findViewById(R.id.search_group_class_spinner);

        search_grade_label = findViewById(R.id.search_grade_label);
        search_class_label = findViewById(R.id.search_class_label);
        export_confirm = findViewById(R.id.export_confirm);
        export_U = findViewById(R.id.export_U);
    }

    BroadcastReceiver reciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.student.choose.root".equals(intent.getAction())) {
                isUsb = true;
                currentPath = intent.getStringExtra("root");
                groupDialog = new DataImportDialog(DataExportActivity.this, "正在导出数据，名单多可能要花费几分钟时间，请耐心等...");
                groupDialog.setShowCancel(true);
                groupDialog.setCancelable(false);
                groupDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Toast.makeText(DataExportActivity.this, "导出取消", Toast.LENGTH_SHORT).show();
                    }
                });
                if (groupDialog != null && groupDialog.isShowing()) {
                    groupDialog.dismiss();
                }
                groupDialog.show();
                groupLoadTask = new GroupLoadTask();
                groupLoadTask.execute(classlist.get(search_group_class_spinner.getSelectedItemPosition()), gradelist.get(search_group_grade_spnner.getSelectedItemPosition()));
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(reciver);
    }

    void initData() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.student.choose.root");
        registerReceiver(reciver, filter);
        export_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_group_class_spinner.getSelectedItemPosition() <= 0 ||
                        search_group_class_spinner.getSelectedItemPosition() == search_group_class_spinner.getCount() - 1) {
                    Toast.makeText(DataExportActivity.this, "请选择年级", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (search_group_grade_spnner.getSelectedItemPosition() <= 0 ||
                        search_group_grade_spnner.getSelectedItemPosition() == search_group_grade_spnner.getCount() - 1) {
                    Toast.makeText(DataExportActivity.this, "请选择班级", Toast.LENGTH_SHORT).show();
                    return;
                }
                String targetPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (!StringUtils.isEmpty(targetPath)) {
                    Intent intent = new Intent(DataExportActivity.this, DataExportChoosePathActivity.class);
                    intent.putExtra("baseRoot", targetPath);
                    intent.putExtra("isUsb", false);
                    startActivity(intent);
                } else {
                    Toast.makeText(DataExportActivity.this, "没有可用的外部存储设备", Toast.LENGTH_SHORT).show();
                }
            }
        });

        export_U.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_group_class_spinner.getSelectedItemPosition() <= 0 ||
                        search_group_class_spinner.getSelectedItemPosition() == search_group_class_spinner.getCount() - 1) {
                    Toast.makeText(DataExportActivity.this, "请选择年级", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (search_group_grade_spnner.getSelectedItemPosition() <= 0 ||
                        search_group_grade_spnner.getSelectedItemPosition() == search_group_grade_spnner.getCount() - 1) {
                    Toast.makeText(DataExportActivity.this, "请选择班级", Toast.LENGTH_SHORT).show();
                    return;
                }
                String targetPath = LaunchActivity.USB_Datalist_path;
                File tmp = new File(targetPath);
                if (!tmp.exists()) {
                    targetPath = LaunchActivity.USB_Datalist_Extra_path0;
                    tmp = new File(targetPath);
                    if (!tmp.exists()) {
                        targetPath = LaunchActivity.USB_Datalist_Extra_path2;
                        tmp = new File(targetPath);
                        if (!tmp.exists()) {
                            targetPath = LaunchActivity.USB_Datalist_Extra_path3;
                        }
                    }
                }
                tmp = new File(targetPath);
                if (!tmp.exists()) {
                    Toast.makeText(DataExportActivity.this, "没有可用的外部存储设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!StringUtils.isEmpty(targetPath)) {
                    Intent intent = new Intent(DataExportActivity.this, DataExportChoosePathActivity.class);
                    intent.putExtra("baseRoot", targetPath);
                    intent.putExtra("isUsb", true);
                    startActivity(intent);
                } else {
                    Toast.makeText(DataExportActivity.this, "没有可用的外部存储设备", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gradelist.add("请选择班级");
        gradelist.add("全部");
        gradelist.addAll(PatternUtils.sortGrades(/*UserModel.getGrades(DataExportActivity.this)*/MainApplication.gradeInfos));
        gradelist.add("");
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                gradelist);
        search_group_grade_spnner.setAdapter(adapter);

        search_group_grade_spnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    search_group_grade_spnner.setSelection(gradePos);
                }
                if (!gradeSelect) {
                    gradeSelect = true;
                    search_grade_label.setVisibility(View.VISIBLE);
                } else {
                    search_grade_label.setVisibility(View.GONE);
                }
                gradePos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                search_grade_label.setVisibility(View.VISIBLE);
            }

        });

        classlist.add("请选择年级");
        classlist.add("全部");
        classlist.addAll(PatternUtils.sortClasses(/*UserModel.getClasses(DataExportActivity.this)*/MainApplication.classInfos));
        classlist.add("");
        adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                classlist);
        search_group_class_spinner.setAdapter(adapter);
        search_group_class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    search_group_class_spinner.setSelection(classPos);
                }
                if (!classSelect) {
                    classSelect = true;
                    search_class_label.setVisibility(View.VISIBLE);
                } else {
                    search_class_label.setVisibility(View.GONE);
                }
                classPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    class GroupLoadTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            String classes = params[0];
            String grade = params[1];
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            if ("全部".equals(classes)) {
                if (!"全部".equals(grade)) {
                    for (String clz : MainApplication.classInfos) {
                        userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(DataExportActivity.this, clz, grade)));
                    }
                } else {
                    for (String clz : MainApplication.classInfos) {
                        for (String grad : MainApplication.gradeInfos) {
                            userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(DataExportActivity.this, clz, grad)));
                        }
                    }
                }
            } else if ("全部".equals(grade)) {
                if (!"全部".equals(classes)) {
                    for (String grad : MainApplication.gradeInfos) {
                        userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(DataExportActivity.this, classes, grad)));
                    }
                }
            } else {
                userInfos = PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(DataExportActivity.this, classes, grade));
            }

            Log.d("hailong16", " userInfos size " + userInfos.size());
            CreateExcel excel = CreateExcel.getInstance();
            ArrayList<String> columns = AppUtils.getColumns(DataExportActivity.this);
            //加入成绩一列
            if (!AppUtils.isWeight())

            {
                columns.add(StudentColomn.SCORE);
            } else

            {
                columns.add(StudentColomn.WEIGHT);
                columns.add(StudentColomn.HEIGHT);
            }

            columns.add(StudentColomn.ANALYSIS);
            String[] titles = new String[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                String sr = columns.get(i);
                titles[i] = sr;
            }

            String targetPath = currentPath;
            Log.d("hailong", " targetPath " + targetPath);
            String sheetName = "学生成绩名单";
            switch (MainApplication.currentProject)

            {
                case MainApplication.Project_Feihuoliang:
                    sheetName = "肺活量测试";
                    break;
                case MainApplication.Project_Weight:
                    sheetName = "身高体重测试";
                    break;
                case MainApplication.Project_Yangwoqizuo:
                    sheetName="仰卧起坐测试";
                    break;
                case MainApplication.Project_Yintixiangshang:
                    sheetName="引体向上测试";
                    break;
            }

            excel.excelCreate(titles, isUsb, targetPath, sheetName + classes + grade + ".xls", sheetName);
            String[][] array = new String[userInfos.size()][columns.size()];
            for (int i = 0; i < userInfos.size(); i++) {
                UserInfo userInfo = userInfos.get(i);
                String[] str = new String[columns.size()];
                for (int j = 0; j < columns.size(); j++) {
                    String co = columns.get(j);
                    if (StudentColomn.NAME.equals(co)) {
                        str[j] = userInfo.name;
                    }
                    if (StudentColomn.CODE.equals(co)) {
                        str[j] = userInfo.code;
                    }
                    if (StudentColomn.SEX.equals(co)) {
                        str[j] = userInfo.sex;
                    }
                    if (StudentColomn.CLASSES.equals(co)) {
                        str[j] = userInfo.classes;
                    }
                    if (StudentColomn.GRADE.equals(co)) {
                        str[j] = userInfo.grade;
                    }
                    if (StudentColomn.SCORE.equals(co)) {
                        String[] scores = UserModel.getScore(userInfo);
                        if (scores != null && scores.length > 0) {
                            str[j] = scores[0];
                        }
                    }
                    if (StudentColomn.WEIGHT.equals(co)) {
                        String[] scores = UserModel.getScore(userInfo);
                        if (scores != null && scores.length > 0) {
                            str[j] = scores[0];
                        }
                    }

                    if (StudentColomn.HEIGHT.equals(co)) {
                        String[] scores = UserModel.getScore(userInfo);
                        if (scores != null && scores.length > 1) {
                            str[j] = scores[1];
                        }
                    }
                    if (StudentColomn.ANALYSIS.equals(co)) {
                        String[] scores = UserModel.getScore(userInfo);
                        if (scores != null && scores.length > 1) {
                            str[j] = AppUtils.getRule(DataExportActivity.this, scores[0], "男".equals(userInfo.sex));
                        }
                    }
                    if (StudentColomn.ID_CARD.equals(co)) {
                        str[j] = userInfo.id_card;
                    }
                    if (StudentColomn.RACE.equals(co)) {
                        str[j] = userInfo.race;
                    }
                    if (StudentColomn.JIGUAN.equals(co)) {
                        str[j] = userInfo.jiguan;
                    }
                    if (StudentColomn.ADDRESS.equals(co)) {
                        str[j] = userInfo.address;
                    }
                    if (StudentColomn.JOB.equals(co)) {
                        str[j] = userInfo.job;
                    }

                }
                array[i] = str;
                try {
                    if (i < array.length) {
                        excel.saveDataToExcel(i + 1, array[i]);
                    }
                } catch (Exception e) {
                    Log.d("hailong", " what exception ");
                    return false;
                }
            }
            excel.close();

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean bol) {
            /*try {
                Thread.sleep(6000);
            } catch (Exception e) {

            }*/
            CountDownTimer downTimer = new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    groupDialog.dismiss();
                    if (bol.booleanValue()) {
                        String clas = classlist.get(search_group_class_spinner.getSelectedItemPosition());
                        String grade = gradelist.get(search_group_grade_spnner.getSelectedItemPosition());
                        Toast.makeText(DataExportActivity.this, "导出成功，请查看 /学生名单导出/肺活量测试_" + clas + "_" + grade + ".xls", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DataExportActivity.this, "导出失败", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            downTimer.start();

        }
    }

}