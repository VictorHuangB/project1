package com.android.student.data.test.project.yangwoqizuo;

import android.os.Bundle;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.project.LaunchActivity;

/**
 * 仰卧起坐测试项目
 * Created by hailong on 2016/11/9 0009.
 */
public class YangwoqizuoLaunchActivity extends LaunchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.per = "个";//单位
        MainApplication.currentProject = MainApplication.Project_Yangwoqizuo;//肺活量
        clz = YangwoqizuoSingleTestActivity.class;
        groupclz = YangwoqiuzoGroupTestActivity.class;
        setProjectToast("仰卧起坐");
    }


}
