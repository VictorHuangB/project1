package com.android.student.data.test.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.pattern.PatternUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hailong on 2016/11/4 0004.
 */
public class AppUtils {
    public static ArrayList<String> getColumns(Context context) {
        ArrayList<String> columns = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("studentcolumns", "");
        String[] splits = str.split("[=]");
        if (splits != null && splits.length > 0) {
            columns.addAll(Arrays.asList(splits));
        }
        return columns;
    }

    public static ArrayList<String> getSoundList(String data) {
        if (data == null) {
            return null;
        }

        final ArrayList<String> sounds = new ArrayList<>();
        float parseData = 0;
        try {
            parseData = Float.parseFloat(data);
        } catch (Exception e) {

        }
        int h5, h4, h3, h2, h1;
        h5 = (int) parseData / 10000;//万
        h4 = (int) (parseData - 10000 * h5) / 1000;//千
        h3 = (int) (parseData - 10000 * h5 - 1000 * h4) / 100;//百
        h2 = (int) (parseData - 10000 * h5 - 1000 * h4 - 100 * h3) / 10;//十
        h1 = ((int) (parseData)) % 10;//个位
        if (h5 != 0) {
            setSoundId(String.valueOf(h5), sounds);
            sounds.add("wan.ogg");
        }
        if (h4 != 0) {
            setSoundId(String.valueOf(h4), sounds);
            sounds.add("qian.ogg");
        }
        if (h3 != 0) {
            setSoundId(String.valueOf(h3), sounds);
            sounds.add("bai.ogg");
        }
        if (h2 != 0) {
            if (!(data.length() == 2 && data.startsWith("1"))) {
                setSoundId(String.valueOf(h2), sounds);
            }
            sounds.add("number_10.ogg");
        }
        if (h1 != 0) {
            setSoundId(String.valueOf(h1), sounds);
        }
        if (data.contains(".")) {
            sounds.add("dian.ogg");
            String[] split = data.split("[.]");
            if (split != null && split.length > 1) {
                String last = split[1];
                int size = last.length();
                for (int i = 0; i < size; i++) {
                    String var = String.valueOf(last.charAt(i));
                    setSoundId(var, sounds);
                }
            }
        }
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Feihuoliang:
                sounds.add("feihuoliang_haosheng.ogg");
                break;
            case MainApplication.Project_Yangwoqizuo:
            case MainApplication.Project_Yintixiangshang:
//                sounds.add("ge.ogg");
                break;
        }
        return sounds;

    }

    public static void setSoundId(String var, ArrayList<String> sounds) {
        switch (var) {
            case "0":
                sounds.add("number_0.ogg");
                break;
            case "1":
                sounds.add("number_1.ogg");
                break;
            case "2":
                sounds.add("number_2.ogg");
                break;
            case "3":
                sounds.add("number_3.ogg");
                break;
            case "4":
                sounds.add("number_4.ogg");
                break;
            case "5":
                sounds.add("number_5.ogg");
                break;
            case "6":
                sounds.add("number_6.ogg");
                break;
            case "7":
                sounds.add("number_7.ogg");
                break;
            case "8":
                sounds.add("number_8.ogg");
                break;
            case "9":
                sounds.add("number_9.ogg");
                break;
            case ".":
                sounds.add("dian.ogg");
                break;

        }
    }


    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    public static boolean isWeight() {
        return MainApplication.currentProject == MainApplication.Project_Weight;
    }

    public static String getResult(String result) {
        if (StringUtils.isEmpty(result)) {
            return "---" + MainApplication.per;
        }
        if (MainApplication.per.equals("ml")
                || MainApplication.per.equals("次")
                || MainApplication.per.equals("个")
                || MainApplication.per.equals("kg")
                || MainApplication.per.equals("cm")) {
            return result + MainApplication.per;
        } else if (MainApplication.per.equals("米")) {
            return ResultFormat.getDecimal(result) + MainApplication.per;
        }
        return result;
    }

    //评分标准，不同的项目的评分标准不同
    public static String[] getRuleKey() {
        String[] rule = new String[2];
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Feihuoliang:
                rule[0] = MainApplication.rule_Feihuoliang_boy;
                rule[1] = MainApplication.rule_Feihuoliang_girl;
                break;
            case MainApplication.Project_Tiqianqu:
                rule[0] = MainApplication.rule_Tiqianqu_boy;
                rule[1] = MainApplication.rule_Tiqianqu_girl;
                break;
            case MainApplication.Project_Yangwoqizuo:
                rule[0] = MainApplication.rule_Yangwoqizuo_boy;
                rule[1] = MainApplication.rule_Yangwoqizuo_girl;
                break;
            case MainApplication.Project_Yintixiangshang:
                rule[0] = MainApplication.rule_Yintixiangshang_boy;
                rule[1] = MainApplication.rule_Yintixiangshang_girl;
                break;
        }
        return rule;
    }

    public static String[] getRuleArrays(Context context, boolean isBoy) {
        String[] array = null;
        //格式为youxiu=a;lianghao=b+c;jige=d+e;bujige=f
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String rule = "";
        if (isBoy) {
            rule = sharedPreferences.getString(getRuleKey()[0], "");
        } else {
            rule = sharedPreferences.getString(getRuleKey()[1], "");
        }

        String[] result = rule.split("[;]");
        try {
            if (result != null && result.length > 3) {
                array = new String[result.length * 2];
                //优秀
                String[] upperArray = result[0].split("[=]");
                if (upperArray != null && upperArray.length > 1) {
                    array[0] = upperArray[1];
                }
                Log.d("hailong13", "upperFloat" + array[0]);
                //良好
                String[] middleArray = result[1].split("[=]");
                if (middleArray != null && middleArray.length > 1) {
                    String[] middleS = middleArray[1].split("[+]");
                    if (middleS != null && middleS.length > 1) {
                        array[1] = middleS[0];
                        array[2] = middleS[1];
                    }
                }
                Log.d("hailong13", "middleMinFloat " + array[1] + " middleMaxFloat " + array[2]);
                //及格
                String[] jigeArray = result[2].split("[=]");
                if (jigeArray != null && jigeArray.length > 1) {
                    String[] jigeS = jigeArray[1].split("[+]");
                    if (jigeS != null && jigeS.length > 1) {
                        array[3] = jigeS[0];
                        array[4] = jigeS[1];
                    }
                }
                Log.d("hailong13", "jigeMinFloat " + array[3] + " jigeMaxFloat " + array[4]);
                //优秀
                String[] noJigeArray = result[3].split("[=]");
                if (noJigeArray != null && noJigeArray.length > 1) {
                    array[5] = noJigeArray[1];
                }
                Log.d("hailong13", "noJigeFloat" + array[5]);
            }
        } catch (Exception e) {

        }
        return array;
    }

    public static String getRule(Context context, String score, boolean isBoy) {
        //拿到评分标准
        String upper;
        String middleMin;
        String middleMax;
        String jigeMin;
        String jigeMax;
        String noJige;

        float upperFloat = 0;
        float middleMinFloat = 0;
        float middleMaxFloat = 0;
        float jigeMinFloat = 0;
        float jigeMaxFloat = 0;
        float noJigeFloat = 0;
        float scoreFloat;
        //格式为youxiu=a;lianghao=b+c;jige=d+e;bujige=f
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        String rule = "";
        if (isBoy) {
            rule = sharedPreferences.getString(getRuleKey()[0], "");
        } else {
            rule = sharedPreferences.getString(getRuleKey()[1], "");
        }

        String[] result = rule.split("[;]");
        try {
            if (result != null && result.length > 3) {
                //优秀
                String[] upperArray = result[0].split("[=]");
                if (upperArray != null && upperArray.length > 1) {
                    upper = upperArray[1];
                    upperFloat = Float.parseFloat(upper);
                }
                Log.d("hailong13", "upperFloat" + upperFloat);
                //良好
                String[] middleArray = result[1].split("[=]");
                if (middleArray != null && middleArray.length > 1) {
                    String[] middleS = middleArray[1].split("[+]");
                    if (middleS != null && middleS.length > 1) {
                        middleMin = middleS[0];
                        middleMax = middleS[1];
                        middleMinFloat = Float.parseFloat(middleMin);
                        middleMaxFloat = Float.parseFloat(middleMax);
                    }
                }
                Log.d("hailong13", "middleMinFloat " + middleMinFloat + " middleMaxFloat " + middleMaxFloat);
                //及格
                String[] jigeArray = result[2].split("[=]");
                if (jigeArray != null && jigeArray.length > 1) {
                    String[] jigeS = jigeArray[1].split("[+]");
                    if (jigeS != null && jigeS.length > 1) {
                        jigeMin = jigeS[0];
                        jigeMax = jigeS[1];
                        jigeMinFloat = Float.parseFloat(jigeMin);
                        jigeMaxFloat = Float.parseFloat(jigeMax);
                    }
                }
                Log.d("hailong13", "jigeMinFloat " + jigeMinFloat + " jigeMaxFloat " + jigeMaxFloat);
                //优秀
                String[] noJigeArray = result[3].split("[=]");
                if (noJigeArray != null && noJigeArray.length > 1) {
                    noJige = noJigeArray[1];
                    noJigeFloat = Float.parseFloat(noJige);
                }
                Log.d("hailong13", "noJigeFloat" + noJigeFloat);
            } else {
                return "---";
            }
        } catch (Exception e) {
            return "---";
        }
        try {
            scoreFloat = Float.parseFloat(score);
        } catch (Exception e) {
            Toast.makeText(context, "解析错误", Toast.LENGTH_SHORT).show();
            return "";
        }
        Log.d("hailong13", " scoreFloat " + scoreFloat + " middleMinFloat " + middleMinFloat + " middleMaxFloat " + middleMaxFloat);
        if (scoreFloat >= upperFloat) {
            return "优秀";
        } else if (scoreFloat >= middleMinFloat && scoreFloat <= middleMaxFloat) {
            return "良好";
        } else if (scoreFloat >= jigeMinFloat && scoreFloat <= jigeMaxFloat) {
            return "及格";
        } else if (scoreFloat < noJigeFloat) {
            return "不及格";
        }
        return "---";
    }

    public static boolean checkRule(Context context, String upper, String middleMin, String middleMax, String jigeMin, String jigeMax, String noJige, boolean isBoy) {
        boolean success = false;
        float upperFloat = 0;
        float middleMinFloat = 0;
        float middleMaxFloat = 0;
        float jigeMinFloat = 0;
        float jigeMaxFloat = 0;
        float noJigeFloat = 0;
        if (StringUtils.isEmpty(upper)
                || StringUtils.isEmpty(middleMin)
                || StringUtils.isEmpty(middleMax)
                || StringUtils.isEmpty(jigeMin)
                || StringUtils.isEmpty(jigeMax)
                || StringUtils.isEmpty(noJige)) {
            Toast.makeText(context, "请完善评分标准", Toast.LENGTH_SHORT).show();
            return success;
        }
        try {
            upperFloat = Float.parseFloat(upper);

            middleMinFloat = Float.parseFloat(middleMin);
            middleMaxFloat = Float.parseFloat(middleMax);

            jigeMinFloat = Float.parseFloat(jigeMin);
            jigeMaxFloat = Float.parseFloat(jigeMax);

            noJigeFloat = Float.parseFloat(noJige);
        } catch (Exception e) {
            Toast.makeText(context, "数据格式有误，请检查", Toast.LENGTH_SHORT).show();
            return success;
        }
        if ("秒".equals(MainApplication.per)) {//跑步这个项目特殊，数值越小成绩越好
            if (upperFloat > middleMaxFloat || upperFloat > middleMinFloat) {
                Toast.makeText(context, "优秀的输入值应该比良好的值小，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (middleMinFloat > middleMaxFloat) {
                Toast.makeText(context, "良好的最小值应该小于最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMinFloat < middleMaxFloat) {
                Toast.makeText(context, "及格的最小值应该大于良好的最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMinFloat > jigeMaxFloat) {
                Toast.makeText(context, "及格的最小值应该小于及格的最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMinFloat < middleMaxFloat) {
                Toast.makeText(context, "及格的最小值应该大于良好的最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (noJigeFloat < jigeMaxFloat) {
                Toast.makeText(context, "不及格的值应该大于及格的最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
        } else {//其他项目比大小，越大越优秀
            if (upperFloat < middleMaxFloat || upperFloat < middleMinFloat) {
                Toast.makeText(context, "优秀的输入值应该比良好的值大，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (middleMinFloat > middleMaxFloat) {
                Toast.makeText(context, "良好的最小值应该小于最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }

            if (jigeMinFloat > middleMinFloat) {
                Toast.makeText(context, "及格的最小值应该小于良好的最小值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMinFloat > jigeMaxFloat) {
                Toast.makeText(context, "及格的最小值应该小于及格的最大值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMinFloat > middleMinFloat) {
                Toast.makeText(context, "及格的最小值应该小于良好的最小值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (jigeMaxFloat > middleMinFloat) {
                Toast.makeText(context, "及格的最大值应该小于良好最小值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
            if (noJigeFloat > jigeMinFloat) {
                Toast.makeText(context, "不及格的值应该小于及格的最小值，请重新输入", Toast.LENGTH_SHORT).show();
                return success;
            }
        }
        //保存数据 格式为youxiu=a;lianghao=b+c;jige=d+e;bujige=f
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String commitStr = "youxiu=" + upper + ";";
        commitStr += "lianghao=" + middleMin + " + " + middleMax + ";";
        commitStr += "jige=" + jigeMin + " + " + jigeMax + ";";
        commitStr += "bujige=" + noJige;
        if (isBoy) {
            editor.putString(getRuleKey()[0], commitStr);
        } else {
            editor.putString(getRuleKey()[1], commitStr);
        }
        Log.d("hailong13", "commitStr " + commitStr);
        editor.commit();
        return true;
    }

    //子串转ASCII
    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        Log.d("hailong15", "chars leghth is " + chars.length);
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        String target = sbu.toString();
        Log.d("hailong12", "NN target is " + target);
        return target;
    }

    //ASCII 转子串
    public static String asciiToString(String value) {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

    //发送 字符-》ASCII-》Hex
    public static String asciiToIntegerStr(String value) {
        String number = stringToAscii(value);//String到ASCII
        String target = Integer.toHexString(Integer.valueOf(number));//ASCII到hex子串
        return target;
    }

    //接收 Hex-》ASCII->字符
    public static String hexToStr(String value) {
        //Hex到ASCII
        String Ascii = toD(value, 2);
        String str = asciiToString(Ascii);//String到ASCII
        return str;
    }

    public static String asciiToStr(String value) {
        int oct = strToInteger(value, 16);
        char c = (char) oct;
        return String.valueOf(c);
    }

    private static String octToAscii(String value) {

        return String.valueOf(Integer.parseInt(value));
    }

    // 任意进制数转为十进制数
    public static String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (r + formatting(a.substring(i, i + 1))
                    * Math.pow(b, a.length() - i - 1));
        }
        return String.valueOf(r);
    }

    public static int strToInteger(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (r + formatting(a.substring(i, i + 1))
                    * Math.pow(b, a.length() - i - 1));
        }
        return r;
    }

    // 将十六进制中的字母转为对应的数字
    public static int formatting(String a) {
        int i = 0;
        for (int u = 0; u < 10; u++) {
            if (a.equals(String.valueOf(u))) {
                i = u;
            }
        }
        if (a.equalsIgnoreCase("a")) {
            i = 10;
        }
        if (a.equalsIgnoreCase("b")) {
            i = 11;
        }
        if (a.equalsIgnoreCase("c")) {
            i = 12;
        }
        if (a.equalsIgnoreCase("d")) {
            i = 13;
        }
        if (a.equalsIgnoreCase("e")) {
            i = 14;
        }
        if (a.equalsIgnoreCase("f")) {
            i = 15;
        }
        return i;
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }

    public static String getSex(UserInfo userInfo) {
        return StringUtils.isEmpty(userInfo.sex) ? "未知" : userInfo.sex;
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        if (begin + count > src.length) {
            count = src.length - begin;
        }
        for (int i = begin; i < begin + count; i++)
            bs[i - begin] = src[i];
        return bs;
    }

    //得到班级
    public static String getICGrade(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        byte[] tmpBytes = subBytes(bytes, 14, 1);
        return PatternUtils.getMathClassOrGrade(String.valueOf(Integer.parseInt(bytesToHexString(tmpBytes)))) + "班";
    }

    //写学号
    public static byte[] stringToBytes(String data) {
        //先拼凑
        if (data == null) {
            return null;
        }
        int size = data.length();
        //将code转成0X的形式
        byte[] bytes = new byte[size / 2];
        for (int i = 0; i < data.length(); i += 2) {
            bytes[i / 2] = Integer.decode("0X" + data.substring(i, i + 2)).byteValue();
        }
        return bytes;
    }

    public static String getCompactCode(String code) {
        int limit = 20;
        String targetCode = "";
        if (code.length() > limit) {
            targetCode = code;
        }

        int pos = code.length();
        int len = limit - pos;
        for (int i = 0; i < len; i++) {
            targetCode += "F";
        }
        targetCode += code;
        return targetCode;
    }

    private static final Pattern namePattern = Pattern.compile("[a-zA-Z]");

    public static boolean checkIsAlphabet(String str) {
        Matcher matcher = namePattern.matcher(String.valueOf(str.charAt(0)));
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static String getCompactName(String name) {
        boolean isAlphabet = checkIsAlphabet(name);
        String targetName = "";
        Log.d("hailong", " isAlphabet " + isAlphabet);
        if (!isAlphabet) {
            if (name.length() > 4) {
                name = name.substring(0, 4);
            }
        } else {
            if (name.length() > 8) {
                name = name.substring(0, 8);
            }
        }
        Log.d("hailong", " AAAA " + name);
        byte[] bytes = name.getBytes(Charset.forName("gbk"));
        targetName = bytesToHexString(bytes);
        if (!isAlphabet) {
            if (name.length() == 3) {
                targetName += "2020";
            } else if (name.length() == 2) {
                targetName += "20202020";
            }
        } else {
            if (name.length() < 8) {
                int left = 8 - name.length();
                for (int i = 0; i < left; i++) {
                    targetName += "20";
                }
            }
        }
        return targetName;
    }

    public static void deleteEnd(String str) {
        if (str.endsWith("20")) {
            str = str.substring(0, str.length() - 2);
            deleteEnd(str);
        } else {
            return;
        }
    }

    public static String bufferToStr(byte[] buffer) {
        return new String(buffer, Charset.forName("gbk"));
    }

    //写姓名年级,班级，
    //07 12 26 02 60 00  01  01 AA  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
    //00 为寻卡、防冲突、选择卡、校验密码、写卡命令
    public static byte[] getCommonBytes(String data) {

        return null;
    }


    //两个数组是否相等
    public static boolean isEqual(byte[] bt1, byte[] bt2) {
        if (bt1 == null || bt2 == null
                || bt1.length != bt2.length) {
            return false;
        }
        int length = bt1.length;
        for (int i = 0; i < length; i++) {
            if (bt1[i] != bt2[i]) {
                return false;
            }
        }
        return true;
    }

}
