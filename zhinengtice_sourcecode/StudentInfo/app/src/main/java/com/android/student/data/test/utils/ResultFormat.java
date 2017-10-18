package com.android.student.data.test.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ResultFormat {
    private static final String BigFormatDecimal = "###,##0.00";
    private static final String NormalFormat = "###.##";
    private static final String NormalFormatDecimal = "##0.00";

    public static String getDecimal(double num, int len) {

        NumberFormat format = null;
        if (len == 0) {
            format = new DecimalFormat("#");
        } else {
            StringBuffer buff = new StringBuffer();
            buff.append("#.");
            for (int i = 0; i < len; i++) {
                buff.append("0");
            }
            format = new DecimalFormat(buff.toString());
        }
        return format.format(num);
    }

    //结果显示,以逗号隔开
    public static String getBigDecimal(String result) {
        return getDecimal(result, BigFormatDecimal);
    }

    //结果显示,以逗号隔开,强制保留两位小数
    public static String getBigDecimalDecimal(String result) {
        return getDecimal(result, BigFormatDecimal);
    }

    //结果正常显示
    public static String getDecimal(String result) {
        return getDecimal(result, NormalFormat);
    }

    public static String getDecimal(String result, String format) {
        if (!StringUtils.isEmpty(result)) {
            result.trim();
            DecimalFormat df = new DecimalFormat(format);
            try {
                return df.format(Double.parseDouble(result));
            } catch (Exception e) {

            }
        }
        return "0.00";
    }
}
