package com.android.student.data.test.studentinfo;

/**
 * 学生的基本信息，所有Excel的列名必须是如下，顺序可不一致
 * Created by hailong on 2016/11/3 0003.
 */
public class StudentColomn {
    public static final String NAME = "姓名";
    public static final String CODE = "学号";
    public static final String SEX = "性别";
    public static final String CLASSES = "年级";
    public static final String GRADE = "班级";
    public static final String SCORE = "成绩";
    public static final String WEIGHT = "身高";
    public static final String HEIGHT = "体重";
    public static final String ANALYSIS = "水平";
    public static final String ID_CARD = "身份证";
    public static final String RACE = "民族";
    public static final String JIGUAN = "籍贯";
    public static final String ADDRESS = "家庭地址";
    public static final String JOB = "职务";
    public static final String[] studentColomn = new String[]{
            NAME,CODE,SEX,CLASSES,GRADE,ID_CARD,RACE,JIGUAN,ADDRESS,JOB
    };
}
