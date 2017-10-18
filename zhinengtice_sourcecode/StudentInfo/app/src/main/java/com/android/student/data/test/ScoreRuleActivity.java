package com.android.student.data.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

/**
 * 评分标准
 * Created by hailong on 2016/10/19 0019.
 */
public class ScoreRuleActivity extends Activity {
    View back_container;//返回按钮
    EditText youxiu_et;
    EditText lianghao_min_et, lianghao_max_et;
    EditText jige_min_et, jige_max_et;
    EditText cha_min_et;
    View rule_boy;
    View rule_girl;
    TextView confirm;
    boolean selectBoy = true;
    String[] array = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_rule_layout);
        back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        initViews();
        array = AppUtils.getRuleArrays(ScoreRuleActivity.this, selectBoy);
        fillEts();
    }

    @Override
    public void onBackPressed() {
        String[] rules = AppUtils.getRuleKey();
        SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String ruleBoy = "";
        String ruleGirl = "";
        ruleBoy = sharedPreferences.getString(rules[0], "");
        ruleGirl = sharedPreferences.getString(rules[1], "");
        String msg ="";
        if (!StringUtils.isEmpty(ruleBoy)
                &&  !StringUtils.isEmpty(ruleGirl))  {
            super.onBackPressed();
            return;
        }
        if (StringUtils.isEmpty(ruleBoy)) {
            msg = "没有完善男生标准,确定退出？";
        } else if (StringUtils.isEmpty(ruleGirl)) {
            msg = "没有完善女生标准，确定退出？";
        }
        final AppDialog appDialog = new AppDialog(this, "取消", "确定", true);

        appDialog.setupContentView(msg);
        appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
            @Override
            public void operationLeft() {
                appDialog.dismiss();
            }

            @Override
            public void operationRight() {
                ScoreRuleActivity.super.onBackPressed();
            }

        });
        appDialog.show();


    }

    void initViews() {
        ((TextView) findViewById(R.id.title)).setText("评分标准");
        youxiu_et = (EditText) findViewById(R.id.youxiu_et);
        lianghao_min_et = (EditText) findViewById(R.id.lianghao_min_et);
        lianghao_max_et = (EditText) findViewById(R.id.lianghao_max_et);
        jige_min_et = (EditText) findViewById(R.id.jige_min_et);
        jige_max_et = (EditText) findViewById(R.id.jige_max_et);
        cha_min_et = (EditText) findViewById(R.id.cha_min_et);
        rule_boy = findViewById(R.id.rule_boy);
        rule_girl = findViewById(R.id.rule_girl);
        confirm = (TextView)findViewById(R.id.confirm);

        rule_boy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectBoy) {
                    selectBoy = true;
                    confirm.setText("确认男生");
                    rule_boy.setBackgroundResource(R.drawable.radio_select_plat);
                    rule_girl.setBackgroundColor(getResources().getColor(R.color.start_upload));
                    array = AppUtils.getRuleArrays(ScoreRuleActivity.this, selectBoy);
                    fillEts();
                }
            }
        });
        rule_girl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectBoy) {
                    selectBoy = false;
                    confirm.setText("确认女生");
                    rule_girl.setBackgroundResource(R.drawable.radio_select_plat);
                    rule_boy.setBackgroundColor(getResources().getColor(R.color.start_upload));
                    array = AppUtils.getRuleArrays(ScoreRuleActivity.this, selectBoy);
                    fillEts();
                }
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String upper, middleMin, middleMax, jigeMin, jigeMax, noJige;
                upper = youxiu_et.getText().toString();
                middleMin = lianghao_min_et.getText().toString();
                middleMax = lianghao_max_et.getText().toString();
                jigeMin = jige_min_et.getText().toString();
                jigeMax = jige_max_et.getText().toString();
                noJige = cha_min_et.getText().toString();

                if (AppUtils.checkRule(ScoreRuleActivity.this, upper, middleMin, middleMax, jigeMin, jigeMax, noJige, selectBoy)) {
                    SharedPreferences sharedPreferences = getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
                    String ruleBoy = "";
                    String ruleGirl = "";
                    String[] rules = AppUtils.getRuleKey();
                    ruleBoy = sharedPreferences.getString(rules[0], "");
                    ruleGirl = sharedPreferences.getString(rules[1], "");
                    if (StringUtils.isEmpty(ruleGirl) || StringUtils.isEmpty(ruleBoy)) {
                        if (selectBoy) {
                            if (StringUtils.isEmpty(ruleGirl)) {
                                Toast.makeText(ScoreRuleActivity.this, "请填写女生的评分标准", Toast.LENGTH_SHORT).show();
                                rule_girl.performClick();
                            }
                        } else {
                            if (StringUtils.isEmpty(ruleBoy)) {
                                Toast.makeText(ScoreRuleActivity.this, "请填写男生的评分标准", Toast.LENGTH_SHORT).show();
                                rule_boy.performClick();
                            }
                        }
                    } else {
                        Toast.makeText(ScoreRuleActivity.this, "评分标准已生效", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }

            }
        });
    }

    //从本地拿到数据
    void fillEts() {
        if (array != null) {
            if (array.length > 5) {
                youxiu_et.setText(array[0]);
                lianghao_min_et.setText(array[1]);
                lianghao_max_et.setText(array[2]);
                jige_min_et.setText(array[3]);
                jige_max_et.setText(array[4]);
                cha_min_et.setText(array[5]);
            }
        }
    }

}
