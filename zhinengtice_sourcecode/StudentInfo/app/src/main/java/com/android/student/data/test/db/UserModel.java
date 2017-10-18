package com.android.student.data.test.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.project.Item;
import com.android.student.data.test.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import static com.android.student.data.test.rwusb.pos.Cmd.ESCCmd.LF;

public class UserModel {
    private static final HandlerThread sWorkerThread = new HandlerThread("user-loader");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private static UserModel userModel;

    public static UserModel getInstance() {
        if (userModel == null) {
            synchronized (UserModel.class) {
                if (userModel == null) {
                    userModel = new UserModel();
                }
            }
        }
        return userModel;
    }

    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }


    static int count;

    public static void updateContentValue(ContentValues values, UserInfo item) {
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Weight:
                values.put(UserSettings.Favorites.Weight, item.weight);
                values.put(UserSettings.Favorites.Height, item.height);
                break;
            case MainApplication.Project_Feihuoliang:
                values.put(UserSettings.Favorites.Feihuoliang, item.feihuoliang);
                break;
            case MainApplication.Project_Tiqianqu:
                values.put(UserSettings.Favorites.Tiqianqu, item.tiqianqu);
                break;
            case MainApplication.Project_Yangwoqizuo:
                values.put(UserSettings.Favorites.Yangwoqizuo, item.yangwoqizuo);
                break;
            case MainApplication.Project_Yintixiangshang:
                values.put(UserSettings.Favorites.Yintixiangshang, item.yintixiangshang);
                break;
        }
    }

    //批量插入
    public static void addItemToDatabaseAsBulk(final Context context, final List<UserInfo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();

        MainApplication app = (MainApplication) context.getApplicationContext();
        UserProvider provider = app.getUserProvider();
        ContentValues[] valueArray = new ContentValues[items.size()];
        for (UserInfo item : items) {
            final ContentValues values = new ContentValues();
//            item.addBitmap(values, item.headImage);

            item.id = app.getUserProvider().generateNewId();
            values.put(UserSettings.Favorites._ID, item.id);
            if (StringUtils.isEmpty(item.avater_label)) {
                Log.d("hailong18", " avater label " + item.avater_label);
            }
            values.put(UserSettings.Favorites.AVATAR_PATH, item.avater_label);
            values.put(UserSettings.Favorites.NAME, item.name);
            values.put(UserSettings.Favorites.CODE, item.code);
            values.put(UserSettings.Favorites.SEX, item.sex);
            values.put(UserSettings.Favorites.CLASS, item.classes);
            values.put(UserSettings.Favorites.GRADE, item.grade);
            updateContentValue(values, item);
            values.put(UserSettings.Favorites.LEVEL, item.level);
            values.put(UserSettings.Favorites.ID_CARD, item.id_card);
            values.put(UserSettings.Favorites.RACE, item.race);
            values.put(UserSettings.Favorites.JIGUAN, item.jiguan);
            values.put(UserSettings.Favorites.ADDRESS, item.address);
            values.put(UserSettings.Favorites.JOB, item.job);
            valueArray[items.indexOf(item)] = values;
        }
        long middle = System.currentTimeMillis();
        provider.bulkInsert(UserSettings.Favorites.CONTENT_URI, valueArray);
        long end = System.currentTimeMillis();
        count += items.size();
        Log.d("hailong17", " count is " + count);
        Log.d("hailong20", " AA Time is " + (middle - start) + " BB Time is " + (end - start));
    }

    //批量更新
    public static void updateItemToDatabaseAsBulk(final Context context, final List<UserInfo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();
        MainApplication app = (MainApplication) context.getApplicationContext();
        UserProvider provider = app.getUserProvider();
        Uri[] uris = new Uri[items.size()];
        ContentValues[] valueArray = new ContentValues[items.size()];
        for (UserInfo item : items) {
            final ContentValues values = new ContentValues();

            values.put(UserSettings.Favorites._ID, item.id);
            values.put(UserSettings.Favorites.AVATAR_PATH, item.avater_label);
            values.put(UserSettings.Favorites.NAME, item.name);
            values.put(UserSettings.Favorites.CODE, item.code);
            values.put(UserSettings.Favorites.SEX, item.sex);
            values.put(UserSettings.Favorites.CLASS, item.classes);
            values.put(UserSettings.Favorites.GRADE, item.grade);
            values.put(UserSettings.Favorites.LEVEL, item.level);
            values.put(UserSettings.Favorites.ID_CARD, item.id_card);
            values.put(UserSettings.Favorites.RACE, item.race);
            values.put(UserSettings.Favorites.JIGUAN, item.jiguan);
            values.put(UserSettings.Favorites.ADDRESS, item.address);
            values.put(UserSettings.Favorites.JOB, item.job);
            int index = items.indexOf(item);
            uris[index] = UserSettings.Favorites.getContentUri(item.id, false);
            valueArray[index] = values;
        }
        long middle = System.currentTimeMillis();
        Log.d("hailong20", " CC Time is " + (middle - start));
        provider.bulkUpdate(uris, valueArray);
        long end = System.currentTimeMillis();
        Log.d("hailong20", " AA Time is " + (middle - start) + " BB Time is " + (end - start));
    }

    /**
     * Add an item to the database
     */
    public static void addItemToDatabase(final Context context, final UserInfo item) {
        Runnable r = new Runnable() {
            public void run() {
                final ContentValues values = new ContentValues();
                final ContentResolver cr = context.getContentResolver();
//                item.addBitmap(values, item.headImage);

                MainApplication app = (MainApplication) context.getApplicationContext();
                item.id = app.getUserProvider().generateNewId();
                values.put(UserSettings.Favorites._ID, item.id);
                values.put(UserSettings.Favorites.NAME, item.name);
                values.put(UserSettings.Favorites.CODE, item.code);
                values.put(UserSettings.Favorites.SEX, item.sex);
                values.put(UserSettings.Favorites.CLASS, item.classes);
                values.put(UserSettings.Favorites.GRADE, item.grade);
                values.put(UserSettings.Favorites.LEVEL, item.level);
                values.put(UserSettings.Favorites.ID_CARD, item.id_card);
                values.put(UserSettings.Favorites.RACE, item.race);
                values.put(UserSettings.Favorites.JIGUAN, item.jiguan);
                values.put(UserSettings.Favorites.ADDRESS, item.address);
                values.put(UserSettings.Favorites.JOB, item.job);
                cr.insert(UserSettings.Favorites.CONTENT_URI, values);

            }
        };
        runOnWorkerThread(r);
    }

    /**
     * Add an item to the database
     */
    public static void addItemToDatabaseSync(final Context context, final UserInfo item) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();
//        item.addBitmap(values, item.headImage);

        MainApplication app = (MainApplication) context.getApplicationContext();
        item.id = app.getUserProvider().generateNewId();
        values.put(UserSettings.Favorites._ID, item.id);
        values.put(UserSettings.Favorites.NAME, item.name);
        values.put(UserSettings.Favorites.CODE, item.code);
        values.put(UserSettings.Favorites.SEX, item.sex);
        values.put(UserSettings.Favorites.CLASS, item.classes);
        values.put(UserSettings.Favorites.GRADE, item.grade);
        values.put(UserSettings.Favorites.LEVEL, item.level);
        values.put(UserSettings.Favorites.ID_CARD, item.id_card);
        values.put(UserSettings.Favorites.RACE, item.race);
        values.put(UserSettings.Favorites.JIGUAN, item.jiguan);
        values.put(UserSettings.Favorites.ADDRESS, item.address);
        values.put(UserSettings.Favorites.JOB, item.job);
        cr.insert(UserSettings.Favorites.CONTENT_URI, values);
    }


    public static ArrayList<UserInfo> getUserInfos(Cursor c, String search, boolean byName) {
        ArrayList<UserInfo> userInfos = new ArrayList<>();
        final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);
        final int avaterpathIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.AVATAR_PATH);
        final int nameIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.NAME);
        final int codeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CODE);
        final int sexIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SEX);
        final int classIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CLASS);
        final int gradeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.GRADE);
        final int[] projectScoreIndexs = getProjectIndex(c);

        final int levelIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.LEVEL);
        final int idcardIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ID_CARD);
        final int raceIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.RACE);
        final int jiguanIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.JIGUAN);
        final int addressIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ADDRESS);
        final int jobIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.JOB);


        while (c.moveToNext()) {
            UserInfo userInfo = new UserInfo();
            Log.d("hailong12", " Name is " + c.getString(nameIndex) + " code is " + c.getString(codeIndex));
            userInfo.avater_label = c.getString(avaterpathIndex);
            Log.d("hailong13", " userInfo.avater_label is " + userInfo.avater_label);
            userInfo.name = c.getString(nameIndex);
            userInfo.code = c.getString(codeIndex);
            String name = PatternUtils.hanziToPinyin(userInfo.name);
            if (byName) {
                Log.d("hailong13", " name is " + name + " search " + search);
                if (!userInfo.name.contains(search) && !name.contains(search)) {
                    continue;
                }
            } else {
                if (!userInfo.code.contains(search)) {
                    continue;
                }
            }
            userInfo.id = c.getLong(idIndex);
            userInfo.sex = c.getString(sexIndex);
            userInfo.classes = c.getString(classIndex);
            userInfo.grade = c.getString(gradeIndex);
            getProjectScore(c, userInfo, projectScoreIndexs);
            userInfo.level = c.getString(levelIndex);
            userInfo.id_card = c.getString(idcardIndex);
            userInfo.race = c.getString(raceIndex);
            userInfo.jiguan = c.getString(jiguanIndex);
            userInfo.address = c.getString(addressIndex);
            userInfo.job = c.getString(jobIndex);

//            byte[] data = c.getBlob(headIconIndex);
//            if (data != null) {
//                userInfo.headImage = byteToBitmap(data);
////                userInfo.headImage = BitmapFactory.decodeByteArray(data, 0, data.length);
//            }
            userInfos.add(userInfo);
        }
        if (c != null) {
            c.close();
        }
        return userInfos;
    }

    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));
        bitmap = (Bitmap) softRef.get();
        if (imgByte != null) {
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    public static ArrayList<UserInfo> queryUserInfoByCode(Context context, String code) {
        //查询全部然后判断是否包含，支持模糊查询
        return queryUserInfoByEntireCode(context, code);
    }

    //整个code查找
    public static ArrayList<UserInfo> queryUserInfoByEntireCode(Context context, String code) {
        //查询全部然后判断是否包含，支持模糊查询
        Cursor c = null;
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    UserSettings.Favorites.CODE + "=?", new String[]{code}, null, null);
        } catch (Exception e) {
            // Ignore
        }
        return getUserInfos(c, code, false);

    }

    public static ArrayList<UserInfo> queryUserInfoByName(Context context, String name) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    /*UserSettings.Favorites.NAME + "=?", new String[]{name},*/null, null, null, null);
        } catch (Exception e) {
            // Ignore
        }

        return getUserInfos(c, name, true);
    }


    public static int[] getProjectIndex(Cursor c) {
        int[] projectIndex = new int[2];
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Weight:
                int weightIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Weight);
                int heightIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Height);
                projectIndex[0] = weightIndex;//weight
                projectIndex[1] = heightIndex;//height
                break;
            case MainApplication.Project_Feihuoliang:
                int feihuoliangIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Feihuoliang);
                projectIndex[0] = feihuoliangIndex;
                break;
            case MainApplication.Project_Tiqianqu:
                int tiqianquIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Tiqianqu);
                projectIndex[0] = tiqianquIndex;
                break;
            case MainApplication.Project_Yangwoqizuo:
                int yangwoqizuoIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Yangwoqizuo);
                projectIndex[0] = yangwoqizuoIndex;
                break;
            case MainApplication.Project_Yintixiangshang:
                int yintixiangshangIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.Yintixiangshang);
                projectIndex[0] = yintixiangshangIndex;
                break;
        }
        return projectIndex;
    }

    public static void getProjectScore(Cursor c, UserInfo userInfo, int[] indexs) {
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Weight:
                userInfo.weight = c.getString(indexs[0]);//weight
                userInfo.height = c.getString(indexs[1]);//height
                break;
            case MainApplication.Project_Feihuoliang:
                userInfo.feihuoliang = c.getString(indexs[0]);//weight
                if (StringUtils.isEmpty(userInfo.feihuoliang)) {
                    userInfo.feihuoliang = "0";
                }
                break;
            case MainApplication.Project_Tiqianqu:
                userInfo.tiqianqu = c.getString(indexs[0]);//体前屈
                if (StringUtils.isEmpty(userInfo.tiqianqu)) {
                    userInfo.tiqianqu = "0";
                }
                break;
            case MainApplication.Project_Yangwoqizuo:
                userInfo.yangwoqizuo = c.getString(indexs[0]);//仰卧起坐
                if (StringUtils.isEmpty(userInfo.yangwoqizuo)) {
                    userInfo.yangwoqizuo = "0";
                }
                break;
            case MainApplication.Project_Yintixiangshang:
                userInfo.yintixiangshang = c.getString(indexs[0]);//引体向上
                if (StringUtils.isEmpty(userInfo.yintixiangshang)) {
                    userInfo.yintixiangshang = "0";
                }
                break;
        }
    }


    public static ArrayList<UserInfo> queryUserInfoFilter(Context context, String classz, String grade) {
        //查询全部然后判断是否包含，支持模糊查询
        Cursor c = null;
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    UserSettings.Favorites.CLASS + "=? and " + UserSettings.Favorites.GRADE + "=? ",
                    new String[]{classz, grade/*, String.valueOf(AppUtils.getProjectType())*/}, null, null);
        } catch (Exception e) {
            // Ignore
        }
        ArrayList<UserInfo> userInfos = new ArrayList<>();
        final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);
        final int avaterPathIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.AVATAR_PATH);
        final int nameIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.NAME);
        final int codeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CODE);
        final int sexIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SEX);
        final int classIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CLASS);
        final int gradeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.GRADE);
        final int[] projectScoreIndexs = getProjectIndex(c);
        final int levelIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.LEVEL);
        final int idcardIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ID_CARD);
        final int raceIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.RACE);
        final int jiguanIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.JIGUAN);
        final int addressIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ADDRESS);
        final int jobIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.JOB);

        final int headIconIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ICON);

        while (c.moveToNext()) {
            UserInfo userInfo = new UserInfo();
            userInfo.avater_label = c.getString(avaterPathIndex);
            userInfo.name = c.getString(nameIndex);
            userInfo.code = c.getString(codeIndex);
            userInfo.id = c.getLong(idIndex);
            userInfo.sex = c.getString(sexIndex);
            userInfo.classes = c.getString(classIndex);
            userInfo.grade = c.getString(gradeIndex);
            getProjectScore(c, userInfo, projectScoreIndexs);
            userInfo.level = c.getString(levelIndex);
            userInfo.id_card = c.getString(idcardIndex);
            userInfo.race = c.getString(raceIndex);
            userInfo.jiguan = c.getString(jiguanIndex);
            userInfo.address = c.getString(addressIndex);
            userInfo.job = c.getString(jobIndex);

//            byte[] data = c.getBlob(headIconIndex);
//            if (data != null) {
////                userInfo.headImage = BitmapFactory.decodeByteArray(data, 0, data.length);
//                userInfo.headImage = byteToBitmap(data);
//            }
            userInfos.add(userInfo);
        }
        Log.d("hailong16", "AA userInfos size " + userInfos.size() + " count " + c.getCount() + " classz " + classz + " grade " + grade);
        if (c != null) {
            c.close();
        }

        return userInfos;

    }

    public static void updateUserInfo(Context context, UserInfo info, String code) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.CODE, code);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);
            }
        };
        runOnWorkerThread(r);
    }

    //IC卡交换信息
    public static void exchangeUserInfo(Context context, UserInfo oldUserInfo, UserInfo newUserInfo) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.NAME, newUserInfo.name);
        values.put(UserSettings.Favorites.CLASS, newUserInfo.classes);
        values.put(UserSettings.Favorites.GRADE, newUserInfo.grade);
        values.put(UserSettings.Favorites.SEX, newUserInfo.sex);
        final Uri uri = UserSettings.Favorites.getContentUri(oldUserInfo.id, false);
        final ContentResolver cr = context.getContentResolver();
        cr.update(uri, values, null, null);
    }

    //肺活量
    public static void updateUserInfoInFeihuoliang(Context context, UserInfo info, String feihuoliang) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.Feihuoliang, feihuoliang);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();
        cr.update(uri, values, null, null);
    }


    //身高体重
    public static void updateUserInfoInHeightAndWeight(Context context, UserInfo info, String height, String weight) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.Height, height);
        values.put(UserSettings.Favorites.Weight, weight);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();

        Runnable r = new Runnable() {
            public void run() {
                cr.update(uri, values, null, null);
            }
        };
        runOnWorkerThread(r);
    }

    //体前屈
    public static void updateUserInfoInTiqianqu(Context context, UserInfo info, String tiqianqu) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.Tiqianqu, tiqianqu);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();
        cr.update(uri, values, null, null);
    }

    //仰卧起坐
    public static void updateUserInfoInYangwoqizuo(Context context, UserInfo info, String yangwoqizuo) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.Yangwoqizuo, yangwoqizuo);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();
        cr.update(uri, values, null, null);
    }

    //引体向上
    public static void updateUserInfoInYintixiangshang(Context context, UserInfo info, String yintixiangshang) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.Yintixiangshang, yintixiangshang);
        final Uri uri = UserSettings.Favorites.getContentUri(info.id, false);
        final ContentResolver cr = context.getContentResolver();
        cr.update(uri, values, null, null);
    }

    public static String[] getScore(UserInfo info) {
        String[] scores = new String[2];
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Weight:
                scores[0] = info.weight;
                scores[1] = info.height;
                break;
            case MainApplication.Project_Feihuoliang:
                scores[0] = info.feihuoliang;
                break;
            case MainApplication.Project_Tiqianqu:
                scores[0] = info.tiqianqu;
                break;
            case MainApplication.Project_Yangwoqizuo:
                scores[0] = info.yangwoqizuo;
                break;
            case MainApplication.Project_Yintixiangshang:
                scores[0] = info.yintixiangshang;
                break;
        }
        return scores;
    }

    public static void setScore(UserInfo info, String[] scores) {
        if (scores == null) {
            return;
        }
        switch (MainApplication.currentProject) {
            case MainApplication.Project_Weight:
                info.weight = scores[0];
                info.height = scores[1];
                break;
            case MainApplication.Project_Feihuoliang:
                info.feihuoliang = scores[0];
                break;
            case MainApplication.Project_Tiqianqu:
                info.tiqianqu = scores[0];
                break;
            case MainApplication.Project_Yangwoqizuo:
                info.yangwoqizuo = scores[0];
                break;
            case MainApplication.Project_Yintixiangshang:
                info.yintixiangshang = scores[0];
                break;
        }
    }

    //覆盖导入,老数据level不覆盖
    public static void updateUserInfo(Context context, UserInfo oldUser, UserInfo newUser, boolean coverScore) {
        final ContentValues values = new ContentValues();
        values.put(UserSettings.Favorites.NAME, newUser.name);
        values.put(UserSettings.Favorites.CODE, newUser.code);
        values.put(UserSettings.Favorites.SEX, newUser.sex);
        values.put(UserSettings.Favorites.CLASS, newUser.classes);
        values.put(UserSettings.Favorites.GRADE, newUser.grade);
        updateContentValue(values, newUser);
//        if (coverScore) {
//            values.put(UserSettings.Favorites.SCORE, newUser.score);
//        }
        values.put(UserSettings.Favorites.ID_CARD, newUser.id_card);
        values.put(UserSettings.Favorites.RACE, newUser.race);
        values.put(UserSettings.Favorites.JIGUAN, newUser.jiguan);
        values.put(UserSettings.Favorites.ADDRESS, newUser.address);
        values.put(UserSettings.Favorites.JOB, newUser.job);
        final Uri uri = UserSettings.Favorites.getContentUri(oldUser.id, false);
        final ContentResolver cr = context.getContentResolver();

//        Runnable r = new Runnable() {
//            public void run() {
//                cr.update(uri, values, null, null);
//            }
//        };
//        runOnWorkerThread(r);
        Log.d("hailong19", " AAAA counnt " + (counnt++));
        cr.update(uri, values, null, null);
    }

    static int counnt;

    public static ArrayList<String> getClasses(Context context) {
        Cursor c = null;
        ArrayList<String> classList = new ArrayList<>();
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    null, null, null, null);
            final int classIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CLASS);
            while (c.moveToNext()) {
                classList.add(c.getString(classIndex));
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        ArrayList<String> targetList = new ArrayList<>();
        int size = classList.size();
        for (int i = 0; i < size; i++) {
            String a = classList.get(i);
            if (targetList.isEmpty()) {
                targetList.add(a);
            }
            ArrayList<String> clone = new ArrayList<>(targetList);
            boolean contain = false;
            for (String str : clone) {
                if (a.equals(str)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                targetList.add(a);
            }
        }
        return targetList;
    }

    public static ArrayList<String> getGrades(Context context) {
        Cursor c = null;
        ArrayList<String> gradeList = new ArrayList<>();
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    null, null, null, null);
            final int gradeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.GRADE);
            while (c.moveToNext()) {
                gradeList.add(c.getString(gradeIndex));
                Log.d("hailong12", " class " + c.getString(gradeIndex));
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        ArrayList<String> targetList = new ArrayList<>();
        int size = gradeList.size();
        for (int i = 0; i < size; i++) {
            String a = gradeList.get(i);
            if (targetList.isEmpty()) {
                targetList.add(a);
            }
            ArrayList<String> clone = new ArrayList<>(targetList);
            boolean contain = false;
            for (String str : clone) {
                if (a.equals(str)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                targetList.add(a);
            }
        }
        return targetList;
    }

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

    static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flattenBitmap(bitmap);
            values.put(UserSettings.Favorites.ICON, data);
        }
    }

    public static int deleteAll(Context context) {
        MainApplication app = (MainApplication) context.getApplicationContext();
        UserProvider provider = app.getUserProvider();
        return provider.deleteAll();
    }

    /**
     * Removes the specified item from the database
     *
     * @param context
     * @param item
     */
    public static void deleteItemFromDatabase(Context context, final UserInfo item) {
        final ContentResolver cr = context.getContentResolver();
        final Uri uriToDelete = UserSettings.Favorites.getContentUri(item.id, false);
        Runnable r = new Runnable() {
            public void run() {
                cr.delete(uriToDelete, null, null);
            }
        };
        r.run();
    }

    public static boolean checkSameUserAndUpdate(Context context, UserInfo newUser, String code, boolean coverScore) {
        boolean hasSame = false;
        Cursor c = null;
        UserInfo oldUser = new UserInfo();
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    UserSettings.Favorites.CODE + "=?", new String[]{code}, null, null);
            final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);

            if (c != null && c.getCount() > 0) {
                hasSame = true;
                c.moveToFirst();
                oldUser.id = c.getLong(idIndex);
                Log.d("hailong13", " olduser level " + oldUser.level);
            }

        } catch (Exception e) {
            // Ignore
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (hasSame) {
            //更新
            updateUserInfo(context, oldUser, newUser, coverScore);
        }
        return hasSame;
    }


    public static Item getCodeList(Context context) {
        Item item = new Item();
        long start = System.currentTimeMillis();
        ArrayList<Long> idlist = new ArrayList<>();
        ArrayList<String> codeList = new ArrayList<>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    null, null, null, null);

            if (c != null) {
                final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);
                final int codeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CODE);
                while (c.moveToNext()) {
                    Long id = c.getLong(idIndex);
                    String code = c.getString(codeIndex);
                    idlist.add(id);
                    codeList.add(code);

                }
            }

        } catch (Exception e) {
            // Ignore
        } finally {
            if (c != null) {
                c.close();
            }
        }
        item.codeList = codeList;
        item.idlist = idlist;
        long end = System.currentTimeMillis();
        Log.d("hailong20", " Time " + (end - start));
        return item;
    }

    public static boolean checkSameUser(Context context, String code) {
        boolean hasSame = false;
        Cursor c = null;
        try {
            c = context.getContentResolver().query(UserSettings.Favorites.CONTENT_URI, null,
                    UserSettings.Favorites.CODE + "=?", new String[]{code}, null, null);

            if (c != null && c.getCount() > 0) {
                hasSame = true;
            }

        } catch (Exception e) {
            // Ignore
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return hasSame;
    }
}
