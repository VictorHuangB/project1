/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.student.data.test.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Settings related utilities.
 */
public class UserSettings {
    /**
     * Favorites.
     */
    public static final class Favorites implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + UserProvider.AUTHORITY + "/"
                + UserProvider.TABLE_FAVORITES + "?" + UserProvider.PARAMETER_NOTIFY + "=true");

        /**
         * The content:// style URL for this table. When this Uri is used, no notification is sent if the content changes.
         */
        public static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" + UserProvider.AUTHORITY + "/"
                + UserProvider.TABLE_FAVORITES + "?" + UserProvider.PARAMETER_NOTIFY + "=false");

        /**
         * The content:// style URL for a given row, identified by its id.
         * 
         * @param id The row id.
         * @param notify True to send a notification is the content changes.
         * 
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + UserProvider.AUTHORITY + "/" + UserProvider.TABLE_FAVORITES + "/" + id + "?"
                    + UserProvider.PARAMETER_NOTIFY + "=" + notify);
        }

        /**
         * <P>
         * Type: TEXT 头像
         * </P>
         */
        public static final String ICON = "icon";
        /**
         * <P>
         * Type: TEXT 头像路径
         * </P>
         */
        public static final String AVATAR_PATH = "avaterpath";
        /**
         * <P>
         * Type: TEXT 名字
         * </P>
         */
        public static final String NAME = "name";
        /**
         * <P>
         * Type: TEXT 学号
         * </P>
         */
        public static final String CODE = "code";
        /**
         * <P>
         * Type: TEXT 性别
         * </P>
         */
        public static final String SEX = "sex";
        /**
         * <P>
         * Type: TEXT 年级
         * </P>
         */
        public static final String CLASS = "class";
        /**
         * <P>
         * Type: TEXT 班级
         * </P>
         */
        public static final String GRADE = "grade";
        /**
         * <P>
         * Type: TEXT 成绩
         * </P>
         */
        public static final String SCORE = "score";
        /**
         * <P>
         * Type: TEXT 评价
         * </P>
         */
        public static final String LEVEL = "level";

        /**
         * <P>
         * Type: TEXT 身份证
         * </P>
         */
        public static final String ID_CARD = "idcard";

        /**
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String RACE = "race";

        /**
         * <P>
         * Type: TEXT 籍贯
         * </P>
         */
        public static final String JIGUAN = "jiguan";

        /**
         * <P>
         * Type: TEXT 地址
         * </P>
         */
        public static final String ADDRESS = "address";

        /**
         * <P>
         * Type: TEXT 职务
         * </P>
         */
        public static final String JOB = "job";

        /*******   体测项目   ********/

        /**
         * <P>
         * Type: TEXT 身高
         * </P>
         */
        public static final String Height = "Height";

//        /**
//         * <P>
//         * Type: TEXT 身高水平
//         * </P>
//         */
      //  public static final String Height_Level = "Height_level";

        /**
         * <P>
         * Type: TEXT 体重
         * </P>
         */
        public static final String Weight = "Weight";

//        /**
//         * <P>
//         * Type: TEXT 体重水平
//         * </P>
//         */
//        public static final String Weight_Level = "Weight_level";

        /**
         * <P>
         * Type: TEXT 肺活量
         * </P>
         */
        public static final String Feihuoliang = "Feihuoliang";

        /**
         * <P>
         * Type: TEXT 体前屈
         * </P>
         */
        public static final String Tiqianqu = "Tiqianqu";

        /**
         * <P>
         * Type: TEXT 立定跳远
         * </P>
         */
        public static final String Lidingtiaoyuan = "Lidingtiaoyuan";

        /**
         * <P>
         * Type: TEXT 引体向上
         * </P>
         */
        public static final String Yintixiangshang = "Yintixiangshang";

        /**
         * <P>
         * Type: TEXT 跳绳
         * </P>
         */
        public static final String Tiaosheng = "Tiaosheng";

        /**
         * <P>
         * Type: TEXT 仰卧起坐
         * </P>
         */
        public static final String Yangwoqizuo = "Yangwoqizuo";

        /**
         * <P>
         * Type: TEXT 长跑1500米
         * </P>
         */
        public static final String Changpao_1500 = "Changpaoyiqianwu";

        /**
         * <P>
         * Type: TEXT 长跑1000米
         * </P>
         */
        public static final String Changpao_1000 = "Changpaoyiqian";

        /**
         * <P>
         * Type: TEXT 长跑800米
         * </P>
         */
        public static final String Changpao_800 = "Changpaobabai";

        /**
         * <P>
         * Type: TEXT 短跑100米
         * </P>
         */
        public static final String Duanpao_100 = "Duanpaoyibai";

        /**
         * <P>
         * Type: TEXT 短跑50米
         * </P>
         */
        public static final String Duanpao_50 = "Duanpaowushi";

    }
}
