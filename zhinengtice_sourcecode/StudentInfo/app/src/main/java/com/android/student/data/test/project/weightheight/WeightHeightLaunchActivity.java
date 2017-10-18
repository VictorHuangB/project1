package com.android.student.data.test.project.weightheight;

import android.os.Bundle;

import com.android.student.data.test.LevelAnalysisActivity;
import com.android.student.data.test.MainApplication;
import com.android.student.data.test.project.LaunchActivity;

/**
 * 身高体重测试项目
 * Created by hailong on 2016/11/9 0009.
 */
public class WeightHeightLaunchActivity extends LaunchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.per = "";//单位
        MainApplication.currentProject = MainApplication.Project_Weight;//身高体重
        clz = WeightHeightSingleActivity.class;
        groupclz = WeightHeightGroupActivity.class;
        setProjectToast("身高体重");
    }

    @Override
    protected void launchActivity(Class<?> cls) {
        if(cls.getName().equals(LevelAnalysisActivity.class.getName())){
            toast("身高体重不支持水平分析");
            return;
        }
        super.launchActivity(cls);
    }

}
