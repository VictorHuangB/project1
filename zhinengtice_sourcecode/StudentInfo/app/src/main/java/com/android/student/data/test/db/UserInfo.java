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

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.utils.AppUtils;

import java.io.Serializable;

/**
 * Represents user information.
 */
public class UserInfo implements Serializable {
    public String avater_label;
    public String name;//姓名
    public String code;//学号
    public String sex;//性别
    public String classes;//年级
    public String grade;//班级

    public String weight = "0";
    public String height = "0";
    public String feihuoliang = "0";//肺活量
    public String tiqianqu = "0";//体前屈
    public String yangwoqizuo ="0";//仰卧起坐
    public String yintixiangshang ="0";//引体向上

    public String level;//优良及格或者不及格等
    public String id_card;//身份证
    public String race;//民族
    public String jiguan;//籍贯
    public String address;//住址
    public String job;//职务

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * The bitmap of the item for drag useage, added by hailong
     */
//    public Bitmap headImage = null;
//    public Bitmap defaultBitmap;
    public UserInfo() {
    }

    UserInfo(UserInfo info) {
        id = info.id;
        avater_label = info.avater_label;
        name = info.name;
        code = info.code;
        sex = info.sex;
        classes = info.classes;
        grade = info.grade;
        this.weight = info.weight;
        this.height = info.height;
        this.feihuoliang = info.feihuoliang;
        this.tiqianqu = info.tiqianqu;
        this.yangwoqizuo = info.yangwoqizuo;

        level = info.level;
        id_card = info.id_card;
        race = info.race;
        jiguan = info.jiguan;
        address = info.address;
        job = info.job;

//        headImage = info.headImage;
    }

    /**
     * Write the fields of this item to the DB
     *
     * @param values
     */
    void onAddToDatabase(ContentValues values, Bitmap bitmap) {
        values.put(UserSettings.Favorites.NAME, name);
        values.put(UserSettings.Favorites.CODE, code);
        values.put(UserSettings.Favorites.SEX, sex);
        values.put(UserSettings.Favorites.CLASS, classes);
        values.put(UserSettings.Favorites.GRADE, grade);

        values.put(UserSettings.Favorites.Weight, weight);
        values.put(UserSettings.Favorites.Height, height);
        values.put(UserSettings.Favorites.Feihuoliang, feihuoliang);
        values.put(UserSettings.Favorites.Tiqianqu, tiqianqu);

        values.put(UserSettings.Favorites.LEVEL, level);
        values.put(UserSettings.Favorites.ID_CARD, id_card);
        values.put(UserSettings.Favorites.RACE, race);
        values.put(UserSettings.Favorites.JIGUAN, jiguan);
        values.put(UserSettings.Favorites.ADDRESS, address);
        values.put(UserSettings.Favorites.JOB, job);
        if (bitmap == null) {
            //TODO
            //学生默认照片
            bitmap = BitmapFactory.decodeResource(MainApplication.getInstance().getResources(), R.drawable.head);
        }
        if (bitmap != null) {
            writeBitmap(values, bitmap);
//            headImage = bitmap;
        }
    }


    void addBitmap(ContentValues values, Bitmap bitmap) {
        boolean useDef = false;
        if (bitmap == null) {
            //TODO
            //学生默认照片
            useDef = true;
            bitmap = /*BitmapFactory.decodeResource(MainApplication.getInstance().getResources(), R.drawable.head);*/MainApplication.defaultBitmap;
        }
        if (bitmap != null) {
            if (useDef) {
//                writeBitmap(values, bitmap);
                byte[] data = /*flattenBitmap(bitmap);*/MainApplication.bytes;
                values.put(UserSettings.Favorites.ICON, data);
            } else {
                byte[] data = AppUtils.flattenBitmap(bitmap);
                values.put(UserSettings.Favorites.ICON, data);
            }
//            headImage = bitmap;
        }
    }


    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = /*flattenBitmap(bitmap);*/MainApplication.bytes;
            values.put(UserSettings.Favorites.ICON, data);
        }
    }

//    @Override
//    public String toString() {
//        return "User(id=" + this.id + " name=" + userName + " job=" + userJob + " phone number=" + userNumber + " email="
//                + userEmail + " fax=" + userFax + " company name=" + userCompanyName + " company address=" + userCompanyAddress
//                + ")";
//    }
}
