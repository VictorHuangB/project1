package com.android.student.data.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by hailong on 2016/11/10 0010.
 */
public class DataEditActivity extends Activity implements View.OnClickListener {
    EditText name_et;
    EditText code_et;
    EditText class_et;
    EditText grade_et;
    EditText score_et;
    EditText weight_et;
    EditText height_et;
    EditText idcard_et;
    EditText race_et;
    EditText jiguan_et;
    EditText address_et;
    EditText job_et;

    View name;
    View codeView;
    View claz;
    View grade;
    View score;
    View weight;
    View height;
    View idcard;
    View race;
    View jiguan;
    View address;
    View job;

    View second_area;
    View radio_boy;
    RadioButton radio_boy_radio;
    View radio_girl;
    RadioButton radio_girl_radio;
    boolean selectBoy = true;
    View confirm_add;
    boolean addUser;
    String title;
    String confirm;
    String code;
    UserInfo userInfo;
    DataImportDialog groupDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_add_layout);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            title = bundle.getString("title");
            confirm = bundle.getString("confirm");
            addUser = bundle.getBoolean("adduser", addUser);
            ((TextView) findViewById(R.id.title)).setText(title);
            ((TextView) findViewById(R.id.confirm_add_tv)).setText(confirm);
            if (!addUser) {
                userInfo = (UserInfo) bundle.getSerializable("user");
                if (userInfo != null) {
                    code = userInfo.code;
                }
            }
        }
        initViews();
        initData();
    }

    void initViews() {
        View back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        name_et = (EditText) findViewById(R.id.name_et);
        code_et = (EditText) findViewById(R.id.code_et);
        class_et = (EditText) findViewById(R.id.class_et);
        grade_et = (EditText) findViewById(R.id.grade_et);
        score_et = (EditText) findViewById(R.id.score_et);
        weight_et = (EditText) findViewById(R.id.weight_et);
        height_et = (EditText) findViewById(R.id.height_et);
        idcard_et = (EditText) findViewById(R.id.idcard_et);
        race_et = (EditText) findViewById(R.id.race_et);
        jiguan_et = (EditText) findViewById(R.id.jiguan_et);
        address_et = (EditText) findViewById(R.id.address_et);
        job_et = (EditText) findViewById(R.id.job_et);

        name = findViewById(R.id.name);
        codeView = findViewById(R.id.code);
        claz = findViewById(R.id.clasz);
        grade = findViewById(R.id.grade);
        score = findViewById(R.id.score);
        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        idcard = findViewById(R.id.idcard);
        race = findViewById(R.id.race);
        jiguan = findViewById(R.id.jiguan);
        address = findViewById(R.id.address);
        job = findViewById(R.id.job);


        radio_boy = findViewById(R.id.radio_boy);
        radio_boy_radio = (RadioButton) findViewById(R.id.radio_boy_radio);
        radio_girl = findViewById(R.id.radio_girl);
        radio_girl_radio = (RadioButton) findViewById(R.id.radio_girl_radio);

        confirm_add = findViewById(R.id.confirm_add);

        second_area = findViewById(R.id.second_area);

        radio_boy.setOnClickListener(this);
        radio_boy_radio.setOnClickListener(this);
        radio_girl.setOnClickListener(this);
        radio_girl_radio.setOnClickListener(this);
        confirm_add.setOnClickListener(this);
    }

    void initData() {
        if (code != null) {
//            ArrayList<UserInfo> infos = UserModel.queryUserInfoByCode(this, code);
//            if (infos != null && infos.size() > 0) {
//                userInfo = infos.get(0);
//            }
            if (MainApplication.currentProject == MainApplication.Project_Weight) {
                weight_et.setVisibility(View.VISIBLE);
                weight.setVisibility(View.VISIBLE);
                height_et.setVisibility(View.VISIBLE);
                height.setVisibility(View.VISIBLE);
                score_et.setVisibility(View.GONE);
                score.setVisibility(View.GONE);
                String scores[] = UserModel.getScore(userInfo);
                if (scores != null && scores.length == 2) {
                    weight_et.setText(scores[0]);
                    height_et.setText(scores[1]);
                }
            } else {
                weight_et.setVisibility(View.GONE);
                weight.setVisibility(View.GONE);
                height_et.setVisibility(View.GONE);
                height.setVisibility(View.GONE);
                score_et.setVisibility(View.VISIBLE);
                score.setVisibility(View.VISIBLE);
                String scores[] = UserModel.getScore(userInfo);
                if (scores != null && scores.length == 2) {
                    score_et.setText(scores[0]);
                }
            }
            if (userInfo != null) {
                if (userInfo.sex.contains("男")) {
                    selectBoy = true;
                    radio_boy_radio.performClick();
                } else {
                    selectBoy = false;
                    radio_girl_radio.performClick();
                }
                if (!StringUtils.isEmpty(userInfo.name)) {
                    name_et.setText(userInfo.name);
                    name.setVisibility(View.VISIBLE);
                    name_et.setVisibility(View.VISIBLE);
                } else {
                    name.setVisibility(View.GONE);
                    name_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.code)) {
                    code_et.setText(userInfo.code);
                    codeView.setVisibility(View.VISIBLE);
                    code_et.setVisibility(View.VISIBLE);
                } else {
                    codeView.setVisibility(View.GONE);
                    code_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.classes)) {
                    class_et.setText(userInfo.classes);
                    claz.setVisibility(View.VISIBLE);
                    class_et.setVisibility(View.VISIBLE);
                } else {
                    claz.setVisibility(View.GONE);
                    class_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.grade)) {
                    grade_et.setText(userInfo.grade);
                    grade.setVisibility(View.VISIBLE);
                    grade_et.setVisibility(View.VISIBLE);
                } else {
                    grade.setVisibility(View.GONE);
                    grade_et.setVisibility(View.GONE);
                }

                if (!StringUtils.isEmpty(userInfo.id_card)) {
                    idcard_et.setText(userInfo.id_card);
                    idcard.setVisibility(View.VISIBLE);
                    idcard_et.setVisibility(View.VISIBLE);
                } else {
                    idcard.setVisibility(View.GONE);
                    idcard_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.race)) {
                    race_et.setText(userInfo.race);
                    race.setVisibility(View.VISIBLE);
                    race_et.setVisibility(View.VISIBLE);
                } else {
                    race.setVisibility(View.GONE);
                    race_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.jiguan)) {
                    jiguan_et.setText(userInfo.jiguan);
                    jiguan.setVisibility(View.VISIBLE);
                    jiguan_et.setVisibility(View.VISIBLE);
                } else {
                    jiguan.setVisibility(View.GONE);
                    jiguan_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.address)) {
                    address_et.setText(userInfo.address);
                    address.setVisibility(View.VISIBLE);
                    address_et.setVisibility(View.VISIBLE);
                } else {
                    address.setVisibility(View.GONE);
                    address_et.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(userInfo.job)) {
                    job_et.setText(userInfo.job);
                    job.setVisibility(View.VISIBLE);
                    job_et.setVisibility(View.VISIBLE);
                } else {
                    job.setVisibility(View.GONE);
                    job_et.setVisibility(View.GONE);
                }

            }
        }
        if (jiguan_et.getVisibility() == View.GONE &&
                address_et.getVisibility() == View.GONE &&
                job_et.getVisibility() == View.GONE) {
            second_area.setVisibility(View.GONE);
        } else {
            second_area.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radio_boy:
            case R.id.radio_boy_radio:
                radio_girl_radio.setSelected(false);
                radio_boy_radio.setBackgroundResource(R.drawable.radio_select);
                radio_girl_radio.setBackgroundResource(R.drawable.radio_deselect);
                selectBoy = true;
                break;
            case R.id.radio_girl:
            case R.id.radio_girl_radio:
                radio_boy_radio.setSelected(false);
                radio_girl_radio.setBackgroundResource(R.drawable.radio_select);
                radio_boy_radio.setBackgroundResource(R.drawable.radio_deselect);
                selectBoy = false;
                break;
            case R.id.confirm_add:
                confirm();
                break;
        }
    }

    void confirm() {
        //判断基本信息
        //判断名字
        String name = name_et.getText().toString();
        if (StringUtils.isEmpty(name)) {
            Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断学号
        String code = code_et.getText().toString();
        if (StringUtils.isEmpty(code)) {
            Toast.makeText(this, "学号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断年级
        String claz = class_et.getText().toString();
        if (StringUtils.isEmpty(claz)) {
            Toast.makeText(this, "年级不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //判断班级
        String grade = grade_et.getText().toString();
        if (StringUtils.isEmpty(grade)) {
            Toast.makeText(this, "班级不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String score = score_et.getText().toString();
        String weight = weight_et.getText().toString();
        String height = height_et.getText().toString();
        String idcard = idcard_et.getText().toString();
        String race = race_et.getText().toString();
        String jiguan = jiguan_et.getText().toString();
        String address = address_et.getText().toString();
        String job = job_et.getText().toString();

        if (addUser) {
            groupDialog = new DataImportDialog(DataEditActivity.this, "正在更新");
            groupDialog.setCancelable(false);
            groupDialog.show();
            //校验是否存在同user，不存在就插入DB
            ArrayList<UserInfo> infos = UserModel.queryUserInfoByEntireCode(this, code);
            if (infos != null && infos.size() > 0) {
                Toast.makeText(this, "学号重复", Toast.LENGTH_SHORT).show();
                return;
            } else {
                UserInfo info = new UserInfo();
                info.name = name;
                info.code = code;
                info.sex = selectBoy ? "男" : "女";
                info.classes = PatternUtils.getMathClassOrGrade(claz) + "年级";
                info.grade = PatternUtils.getMathClassOrGrade(grade) + "班";
//                info.score = score;
                if (MainApplication.currentProject == MainApplication.Project_Weight) {
                    UserModel.setScore(info, new String[]{weight, height});
                } else {
                    UserModel.setScore(info, new String[]{score});
                }
                info.id_card = idcard;
                info.race = race;
                info.jiguan = jiguan;
                info.address = address;
                info.job = job;
                Log.d("hailong13", " info.clas " + info.classes + " info.grade " + info.grade);
                UserModel.addItemToDatabase(this, info);
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("com.data.edit.update"));
                finish();
            }

            groupDialog.dismiss();
        } else {
            //只是更新的操作
            UserInfo info = new UserInfo();
            info.name = name;
            info.code = code;
            info.sex = selectBoy ? "男" : "女";
            info.classes = PatternUtils.getMathClassOrGrade(claz) + "年级";
            info.grade = PatternUtils.getMathClassOrGrade(grade) + "班";
//            info.score = score;
            if (MainApplication.currentProject == MainApplication.Project_Weight) {
                UserModel.setScore(info, new String[]{weight, height});
            } else {
                UserModel.setScore(info, new String[]{score});
            }
            info.id_card = idcard;
            info.race = race;
            info.jiguan = jiguan;
            info.address = address;
            info.job = job;
            Log.d("hailong13", " info.clas " + info.classes + " info.grade " + info.grade);
            if (!this.code.equals(code)) {
                if (UserModel.checkSameUser(this, code)) {
                    Toast.makeText(this, "学号重复", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    UserModel.checkSameUserAndUpdate(this, info, this.code, true);
                    sendBroadcast(new Intent("com.data.edit.update"));
                    finish();
                }
            } else {
                UserModel.checkSameUserAndUpdate(this, info, this.code, true);
                sendBroadcast(new Intent("com.data.edit.update"));
                finish();
            }
        }

    }

}
