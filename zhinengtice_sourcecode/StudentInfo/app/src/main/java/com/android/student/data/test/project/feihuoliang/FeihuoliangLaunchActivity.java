package com.android.student.data.test.project.feihuoliang;

import android.os.Bundle;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.project.LaunchActivity;

/**
 * 肺活量测试项目
 * Created by hailong on 2016/11/9 0009.
 */
public class FeihuoliangLaunchActivity extends LaunchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.per = "ml";//单位
        MainApplication.currentProject = MainApplication.Project_Feihuoliang;//肺活量
        clz = FeihuoliangSingleTestActivity.class;
        groupclz = FeihuoliangGroupTestActivity.class;
        setProjectToast("肺活量");
    }


}
