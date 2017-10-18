package com.android.student.data.test.pattern;

import android.util.Log;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.db.UserInfo;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式,截取"年级","班级","高",然后取出"一",转成1，如果一年级转成1
 * Created by hailong on 2016/11/4 0004.
 */
public class PatternUtils {
    //学号a-z擦除
    private static final Pattern pinyinPattern = Pattern.compile("[0~9]");
    private static final Pattern codePattern = Pattern.compile("[a-zA-Z]");
    private static final String[][] customMather = {
            new String[]{"年级", ""},
            new String[]{"班", ""},
            new String[]{"初", ""},
            new String[]{"高", ""},
            new String[]{"大", ""}
    };
    private static final String[][] gradeMather = {
            new String[]{"一", "1"},
            new String[]{"二", "2"},
            new String[]{"三", "3"},
            new String[]{"四", "4"},
            new String[]{"五", "5"},
            new String[]{"六", "6"},
            new String[]{"七", "7"},
            new String[]{"八", "8"},
            new String[]{"九", "9"},
            new String[]{"十", "10"},
            new String[]{"十一", "11"},
            new String[]{"十二", "12"},
            new String[]{"十三", "13"},
            new String[]{"十四", "14"},
            new String[]{"十五", "15"},
            new String[]{"十六", "16"},
            new String[]{"十七", "17"},
            new String[]{"十八", "18"},
            new String[]{"十九", "19"},
            new String[]{"二十", "20"},
            new String[]{"二十一", "21"},
            new String[]{"二十二", "22"},
            new String[]{"二十三", "23"},
            new String[]{"二十四", "24"},
            new String[]{"二十五", "25"},
            new String[]{"二十六", "26"},
            new String[]{"二十七", "27"},
            new String[]{"二十八", "28"},
            new String[]{"二十九", "29"},
            new String[]{"三十", "30"},
            new String[]{"三十一", "31"},
            new String[]{"三十二", "32"},
            new String[]{"三十三", "33"},
            new String[]{"三十四", "34"},
            new String[]{"三十五", "35"},
            new String[]{"三十六", "36"},
            new String[]{"三十七", "37"},
            new String[]{"三十八", "38"},
            new String[]{"三十九", "39"},
            new String[]{"四十", "40"},
            new String[]{"年级", ""},
            new String[]{"班", ""},
            new String[]{"初", ""},
            new String[]{"高", ""},
            new String[]{"大", ""}
    };

    public static Integer replace(final String sourceString, Object[] object) {
        String temp = sourceString;
        for (int i = 0; i < object.length; i++) {
            String[] result = (String[]) object[i];
            Pattern pattern = Pattern.compile(result[0]);
            Matcher matcher = pattern.matcher(temp);
            temp = matcher.replaceAll(result[1]);
        }
        int pos = -1;
        try {
            pos = Integer.parseInt(temp);
        } catch (Exception e) {
        }
        return pos;
    }

    public static ArrayList<String> sortClasses(ArrayList<String> classesList) {
        if (classesList == null) {
            return null;
        }
        HashMap<Integer, String> map = new HashMap<>();
        ArrayList<Integer> integers = new ArrayList<>();
        ArrayList<String> clone = new ArrayList<>();
        try {
            for (String claz : classesList) {

                int pos = /*Integer.parseInt(getNumber(claz));*/replace(claz, gradeMather);
                Log.d("hailong14", " clz " + claz + " pos " + pos);
                integers.add(pos);
                map.put(pos, claz);
            }
        } catch (Exception e) {

        }
        Collections.sort(integers, getComporater());
        for (Integer inte : integers) {
            clone.add(map.get(inte));
        }
        if (clone.isEmpty()) {
            clone = new ArrayList<>(classesList);
        }
        return clone;
    }

    public static ArrayList<String> sortGrades(ArrayList<String> gradeList) {
        if (gradeList == null) {
            return null;
        }
        HashMap<Integer, String> map = new HashMap<>();
        ArrayList<Integer> integers = new ArrayList<>();
        ArrayList<String> clone = new ArrayList<>();
        try {
            for (String grade : gradeList) {
                int pos = /*Integer.parseInt(getNumber(grade));*/replace(grade, gradeMather);
                integers.add(pos);
                map.put(pos, grade);
            }
        } catch (Exception e) {

        }
        Collections.sort(integers, getComporater());
        for (Integer inte : integers) {
            clone.add(map.get(inte));
        }
        if (clone.isEmpty()) {
            clone = new ArrayList<>(gradeList);
        }
        return clone;
    }

    public static String getMathClassOrGrade(String src) {
        String target;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(src);
        m.find();
        try {
            target = replaceWithPinyin(m.group());
        } catch (Exception e) {
            target = src;
        }
        String temp = target;
        for (int i = 0; i < customMather.length; i++) {
            String[] result = customMather[i];
            Pattern pattern = Pattern.compile(result[0]);
            Matcher matcher = pattern.matcher(temp);
            temp = matcher.replaceAll(result[1]);
        }
        return temp;
    }

    public static String getNumber(String src) {
        String target;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(src);
        m.find();
        try {
            target = m.group();
        } catch (Exception e) {
            target = src;
        }
        return target;
    }

    public static String replaceWithPinyin(String src) {
        String target = "";
        if (src.startsWith("0")) {
            src = src.substring(1, src.length());
        }
        switch (src) {
            case "1":
                target = "一";
                break;
            case "2":
                target = "二";
                break;
            case "3":
                target = "三";
                break;
            case "4":
                target = "四";
                break;
            case "5":
                target = "五";
                break;
            case "6":
                target = "六";
                break;
            case "7":
                target = "七";
                break;
            case "8":
                target = "八";
                break;
            case "9":
                target = "九";
                break;
            case "10":
                target = "十";
                break;
            case "11":
                target = "十一";
                break;
            case "12":
                target = "十二";
                break;
            case "13":
                target = "十三";
                break;
            case "14":
                target = "十四";
                break;
            case "15":
                target = "十五";
                break;
            case "16":
                target = "十六";
                break;
            case "17":
                target = "十七";
                break;
            case "18":
                target = "十八";
                break;
            case "19":
                target = "十九";
                break;
            case "20":
                target = "二十";
                break;
            case "21":
                target = "二十一";
                break;
            case "22":
                target = "二十二";
                break;
            case "23":
                target = "二十三";
                break;
            case "24":
                target = "二十四";
                break;
            case "25":
                target = "二十五";
                break;
            case "26":
                target = "二十六";
                break;
            case "27":
                target = "二十七";
                break;
            case "28":
                target = "二十八";
                break;
            case "29":
                target = "二十九";
                break;
            case "30":
                target = "三十";
                break;

        }
        return target;
    }

    //根据学号来排列，由低到高
    public static ArrayList<UserInfo> getUserInfosByCode(ArrayList<UserInfo> infos) {
        HashMap<Integer, UserInfo> map = new HashMap<>();
        ArrayList<Integer> integers = new ArrayList<>();
        ArrayList<UserInfo> codeSortInfos = new ArrayList<>();
        for (UserInfo userInfo : infos) {
            String code = testCodePattern(userInfo.code);
            int max =/* code.length() >= 8 ? 8 : (code.length() >= 4 ? 4 : code.length());*/ code.length();
//            if (code.length() >= max) {
            //截出后四位
            code = code.substring(/*code.length() - max*/0, code.length());
            int pos = Integer.parseInt(getNumber(code));
            integers.add(pos);
            map.put(pos, userInfo);
//            }
        }
        Collections.sort(integers, getComporater());
        for (Integer inte : integers) {
            codeSortInfos.add(map.get(inte));
        }
        return codeSortInfos;
    }


    //根据成绩来排列，由高到低
    public static ArrayList<UserInfo> getUserInfosByLevel(ArrayList<UserInfo> infos) {
        Collections.sort(infos, getCommonComporater());
        return infos;
    }


    public static String testCodePattern(String code) {
        Matcher matcher = codePattern.matcher(code);
        return matcher.replaceAll("");
    }

    //汉字转拼音后去掉[ ] 然后把数字去掉声调
    public static String getPinyin(String src) {
        src = src.replace("[", "").replace("]", "");
        Matcher matcher = pinyinPattern.matcher(src);
        return matcher.replaceAll("");
    }

    public static String hanziToPinyin(String str) {
        String name = "";
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        boolean success = true;
        try {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                String vals = PinyinHelper.toHanyuPinyinStringArray(c, format)[0];
                name += PatternUtils.getPinyin(vals);
            }
        } catch (Exception e) {
            success = false;
        }
        if (success) {
            return name;
        }
        return str;
    }

    public static String getWriteICclass(String clas) {
        String replace = String.valueOf(replace(clas, gradeMather));
        if (replace.length() == 1) {
            replace = "0" + replace;
        }
        return replace;
    }

    public static final Comparator<UserInfo> getCommonComporater() {
        return new Comparator<UserInfo>() {
            public final int compare(UserInfo a, UserInfo b) {
                int result = 0;
                try {
                    switch (MainApplication.currentProject) {
                        case MainApplication.Project_Feihuoliang:
                            if (Float.parseFloat(a.feihuoliang) < Float.parseFloat(b.feihuoliang)) {
                                result = 1;
                            } else if (Float.parseFloat(a.feihuoliang) > Float.parseFloat(b.feihuoliang)) {
                                result = -1;
                            } else {
                                result = 0;
                            }
                            break;
                        case MainApplication.Project_Tiqianqu:
                            if (Float.parseFloat(a.tiqianqu) < Float.parseFloat(b.tiqianqu)) {
                                result = 1;
                            } else if (Float.parseFloat(a.tiqianqu) > Float.parseFloat(b.tiqianqu)) {
                                result = -1;
                            } else {
                                result = 0;
                            }
                            break;

                        case MainApplication.Project_Yangwoqizuo:
                            if (Float.parseFloat(a.yangwoqizuo) < Float.parseFloat(b.yangwoqizuo)) {
                                result = 1;
                            } else if (Float.parseFloat(a.yangwoqizuo) > Float.parseFloat(b.yangwoqizuo)) {
                                result = -1;
                            } else {
                                result = 0;
                            }
                            break;

                        case MainApplication.Project_Yintixiangshang:
                            if (Float.parseFloat(a.yintixiangshang) < Float.parseFloat(b.yintixiangshang)) {
                                result = 1;
                            } else if (Float.parseFloat(a.yintixiangshang) > Float.parseFloat(b.yintixiangshang)) {
                                result = -1;
                            } else {
                                result = 0;
                            }
                            break;
                    }
                } catch (Exception e) {
                    return 0;
                }
                return result;
            }
        };
    }

    public static final Comparator<Integer> getComporater() {
        return new Comparator<Integer>() {
            public final int compare(Integer a, Integer b) {
                if (a > b) {
                    return 1;
                } else if (a < b) {
                    return -1;
                } else return 0;
            }
        };
    }


}
