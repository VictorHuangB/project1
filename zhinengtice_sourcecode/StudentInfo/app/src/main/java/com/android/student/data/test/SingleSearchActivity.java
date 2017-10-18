package com.android.student.data.test;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.result.SingleSearchResultActivity;

import java.util.ArrayList;

/**
 * 单项查询
 * Created by hailong on 2016/10/18 0018.
 */
public class SingleSearchActivity extends Activity {
    int project = -1;

    View back_container;//返回按钮

    AutoCompleteTextView search_et;//单人查询输入框
    View search_single_container;//单人查询搜索按钮

    Spinner search_group_grade_spnner;//集体查询选择年级下拉框
    Spinner search_group_class_spinner;//集体查询选择班级下拉框
    View search_group_container;//集体查询搜索按钮

    View search_grade_label;
    View search_class_label;
    boolean gradeSelect = false;
    boolean classSelect = false;
    int classPos = 0;
    int gradePos = 0;
    DataImportDialog dataImportDialog;
    DataImportDialog groupDialog;
    View search_clear;
    LocalLoadTask loadTask;
    GroupLoadTask groupLoadTask;
    final ArrayList<String> classlist = new ArrayList<>();
    final ArrayList<String> gradelist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_search_layout);
        Intent intent = getIntent();
        if (intent != null) {
            project = intent.getIntExtra("project", project);
        }
        initViews();
        initData();
    }

    private void initViews() {
        back_container = findViewById(R.id.back_container);
        ((TextView) findViewById(R.id.title)).setText("单项查询");
        search_et = (AutoCompleteTextView) findViewById(R.id.search_et);
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search_single_container.performClick();
                return false;
            }
        });
        search_clear = findViewById(R.id.search_clear);
        search_single_container = findViewById(R.id.search_single_container);
        search_group_grade_spnner = (Spinner) findViewById(R.id.search_group_grade_spinner);
        search_group_class_spinner = (Spinner) findViewById(R.id.search_group_class_spinner);
        search_group_container = findViewById(R.id.search_group_container);

        search_grade_label = findViewById(R.id.search_grade_label);
        search_class_label = findViewById(R.id.search_class_label);
    }


    class GroupLoadTask extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (groupDialog != null && !groupDialog.isShowing()) {
                groupDialog.setCancelable(false);
                groupDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            String classes = params[0];
            String grade = params[1];
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            if ("全部".equals(classes)) {
                if (!"全部".equals(grade)) {
                    for (String clz : MainApplication.classInfos) {
                        userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(SingleSearchActivity.this, clz, grade)));
                    }
                } else {
                    for (String clz : MainApplication.classInfos) {
                        for (String grad : MainApplication.gradeInfos) {
                            userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(SingleSearchActivity.this, clz, grad)));
                        }
                    }
                }
            } else if ("全部".equals(grade)) {
                if (!"全部".equals(classes)) {
                    for (String grad : MainApplication.gradeInfos) {
                        userInfos.addAll(PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(SingleSearchActivity.this, classes, grad)));
                    }
                }
            } else {
                userInfos = PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(SingleSearchActivity.this, classes, grade));
            }
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            MainApplication.searchInfos = userInfos;
            Intent intent = new Intent();
            intent.putExtra("title", "集体查询结果");
            intent.setClass(SingleSearchActivity.this, SingleSearchResultActivity.class);
            startActivity(intent);
            if (groupDialog != null) {
                groupDialog.dismiss();
            }
            overridePendingTransition(
                    R.anim.right_in, R.anim.left_out);
        }
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
                userInfos = UserModel.queryUserInfoByCode(SingleSearchActivity.this, search);
//                if (search.length() > 4) {
//                    userInfos = UserModel.queryUserInfoByCode(SingleSearchActivity.this, search);
//                    if (userInfos.isEmpty()) {
//                        userInfos = UserModel.queryUserInfoByName(SingleSearchActivity.this, search);
//                    }
//                } else {
//                    userInfos = UserModel.queryUserInfoByName(SingleSearchActivity.this, search);
//                    if (userInfos.isEmpty()) {
//                        userInfos = UserModel.queryUserInfoByCode(SingleSearchActivity.this, search);
//                    }
//                }
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                Toast.makeText(SingleSearchActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                MainApplication.searchInfos = userInfos;
                Intent intent = new Intent();
                intent.putExtra("title", "单人查询结果");
                intent.setClass(SingleSearchActivity.this, SingleSearchResultActivity.class);
                startActivity(intent);

                overridePendingTransition(
                        R.anim.right_in, R.anim.left_out);
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    private void initData() {
        search_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_et.setText("");
            }
        });
//        silentLoadTask = new SilentLoadTask();
//        silentLoadTask.execute();
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        search_single_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_et.getText().toString().isEmpty()) {
                    Toast.makeText(SingleSearchActivity.this, "请输入学号查询", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataImportDialog = new DataImportDialog(SingleSearchActivity.this, "正在查询...");
                dataImportDialog.setCancelable(false);
                String str = search_et.getText().toString().replace(" ", "");
                if (str.contains("(")) {
                    str = str.substring(str.indexOf("(") + 1, str.length() - 1);
                }
                if (str.length() < 2) {
                    Toast.makeText(SingleSearchActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadTask = new LocalLoadTask();
                loadTask.execute(str);

            }
        });
        search_group_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_group_class_spinner.getSelectedItemPosition() <= 0 ||
                        search_group_class_spinner.getSelectedItemPosition() == search_group_class_spinner.getCount() - 1) {
                    Toast.makeText(SingleSearchActivity.this, "请选择年级", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (search_group_grade_spnner.getSelectedItemPosition() <= 0 ||
                        search_group_grade_spnner.getSelectedItemPosition() == search_group_grade_spnner.getCount() - 1) {
                    Toast.makeText(SingleSearchActivity.this, "请选择班级", Toast.LENGTH_SHORT).show();
                    return;
                }
                groupDialog = new DataImportDialog(SingleSearchActivity.this, "正在查询");
                groupDialog.setCancelable(false);
                groupLoadTask = new GroupLoadTask();
                groupLoadTask.execute(classlist.get(search_group_class_spinner.getSelectedItemPosition()), gradelist.get(search_group_grade_spnner.getSelectedItemPosition()));
            }
        });

        gradelist.add("请选择班级");
        gradelist.add("全部");
        gradelist.addAll(PatternUtils.sortGrades(/*UserModel.getGrades(SingleSearchActivity.this)*/MainApplication.gradeInfos));
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
        classlist.addAll(PatternUtils.sortClasses(/*UserModel.getClasses(SingleSearchActivity.this)*/MainApplication.classInfos));
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
}
