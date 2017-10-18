package com.android.student.data.test.project.tiqianqu;

import android.os.Bundle;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.project.LaunchActivity;
import com.android.student.data.test.project.feihuoliang.FeihuoliangGroupTestActivity;
import com.android.student.data.test.project.feihuoliang.FeihuoliangSingleTestActivity;

/**
 * 体前屈
 * Created by hailong on 2016/11/21 0021.
 */
public class TiqianquLaunchActivity extends LaunchActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.per = "cm";//单位
        MainApplication.currentProject = MainApplication.Project_Tiqianqu;//体前屈
        clz = TiqianquSingleTestActivity.class;
        groupclz = TiqianquGroupTestActivity.class;
        setProjectToast("体前屈");
    }
}
