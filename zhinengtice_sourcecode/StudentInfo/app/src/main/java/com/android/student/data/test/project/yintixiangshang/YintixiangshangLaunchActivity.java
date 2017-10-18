package com.android.student.data.test.project.yintixiangshang;

import android.os.Bundle;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.project.LaunchActivity;

/**
 * 引体向上测试项目
 * Created by hailong on 2016/11/9 0009.
 */
public class YintixiangshangLaunchActivity extends LaunchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.per = "个";//单位
        MainApplication.currentProject = MainApplication.Project_Yintixiangshang;//引体向上
        clz = YintixiangshangSingleTestActivity.class;
        groupclz = YintixiangshangGroupTestActivity.class;
        setProjectToast("引体向上");
    }


}
