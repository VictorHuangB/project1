package com.android.student.data.test.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.studentinfo.StudentColomn;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.Sheet;
import jxl.Workbook;

/**
 * Created by hailong on 2016/11/11 0011.
 */
public class DataUploadModel {
    private final Object mLock = new Object();
    private DeferredHandler mHandler = new DeferredHandler();
    private UserLoaderTask mLoaderTask;
    private static final HandlerThread sWorkerThread = new HandlerThread("user-loader");

    static {
        sWorkerThread.start();
    }

    int ITEMS_CHUNK;
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private WeakReference<UserCallbacks> mCallbacks;
    private ArrayList<UserInfo> bundledusers;
    private final Context mContext;
    String[] params;
    String[] avatars;
    int dataCount;
//    int score = 49;
    boolean override;
    boolean flag;

    public DataUploadModel(Context context, String[] params) {
        mContext = context;
        this.params = params;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setuserCallbacks(UserCallbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<UserCallbacks>(callbacks);
        }
    }

    private boolean loadUsers() {
        MainApplication.classInfos.clear();
        MainApplication.gradeInfos.clear();
        final ArrayList<UserInfo> userInfos = new ArrayList<>();
        try {
            Log.d("hailong13", " param0 " + params[0]);
            File baseFile = new File(params[0]);
            avatars = baseFile.list();
        } catch (Exception e) {

        }
        HashMap<Integer, String> kvMap = new HashMap<>();
        try {
            File baseFile = new File(params[1]);
            Log.d("hailong13", " param1 " + params[1]);
            FileInputStream fileInputStream = null;
            if (baseFile == null) {
                return false;
            }
            for (File file : baseFile.listFiles()) {
                if (file.getName().contains("xls")) {
                    Log.d("hailong13", " xls find it ");
                    fileInputStream = new FileInputStream(file);
                    break;
                }
            }
            if (fileInputStream == null) {
                return false;
            }
            Workbook book = Workbook.getWorkbook(fileInputStream);
            Sheet sheet = book.getSheet(0);
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(MainApplication.Shared_Prf, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String columStr = "";
            int count = 0;
            long start1 = System.currentTimeMillis();
            if (sheet != null) {
                int Rows = /*sheet.getRows();*/dataCount;
                int Cols = sheet.getColumns();
                Log.d("hailong17", " Rows is " + Rows + " Cols " + Cols);
                keep:
                for (int i = 0; i < Rows; ++i) {
                    ArrayList<String> classInfos = new ArrayList<>(MainApplication.classInfos);
                    ArrayList<String> gradeInfos = new ArrayList<>(MainApplication.gradeInfos);
                    UserInfo info = new UserInfo();
                    for (int j = 0; j < Cols; ++j) {
                        if (flag) {
                            MainApplication.classInfos.clear();
                            MainApplication.gradeInfos.clear();
                            return false;
                        }
                        final String content = sheet.getCell(j, i).getContents();
                        //针对第一项，记录列名
                        if (i == 0) {
                            if (existColum(content)) {
                                if (j != Cols - 1) {
                                    columStr += (content + "=");
                                } else {
                                    columStr += content;
                                }
                                kvMap.put(j, content);
                            }
                        } else {
                            for (Integer integer : kvMap.keySet()) {
                                if (j == integer) {
                                    Log.d("hailong12", " value si " + kvMap.get(integer) + " content " + content);
                                    switch (kvMap.get(integer)) {
                                        case StudentColomn.NAME:
                                            info.name = content;
                                            break;
                                        case StudentColomn.CODE:
                                            info.code = content;
                                            break;
                                        case StudentColomn.SEX:
                                            info.sex = content;
                                            break;
                                        case StudentColomn.CLASSES:
                                            info.classes = content;
                                            break;
                                        case StudentColomn.GRADE:
                                            info.grade = content;
                                            break;
                                        case StudentColomn.ID_CARD:
                                            info.id_card = content;
                                            break;
                                        case StudentColomn.RACE:
                                            info.race = content;
                                            break;
                                        case StudentColomn.JIGUAN:
                                            info.jiguan = content;
                                            break;
                                        case StudentColomn.ADDRESS:
                                            info.address = content;
                                            break;
                                        case StudentColomn.JOB:
                                            info.job = content;
                                            break;
                                    }
                                }
                            }
                        }

                    }
                    Log.d("hailong16", " count " + (count++));
                    if (i != 0) {
                        if (avatars != null && i - 1 < avatars.length) {
                            try {
                                info.avater_label = params[0] + avatars[i - 1];
                                Log.d("hailong12", " label " + info.avater_label);
                            } catch (Exception e) {

                            }
                        }

                        //加入数据库
//                        if (!StringUtils.isEmpty(info.avater_label)) {
//                            info.headImage = BitmapFactory.decodeFile(info.avater_label);
//                        }
//                            if (info.headImage == null) {
//                                info.headImage = BitmapFactory.decodeResource(MainApplication.getInstance().getResources(), R.drawable.head);
//                            }
                        if (override && !UserModel.checkSameUserAndUpdate(mContext, info, info.code, false)) {
                            //判断是否存在同一个用户
                            //Test
//                            score += 4;
//                            info.score = String.valueOf(score);
                            // userInfos.add(info);
                            //Add to DB
                            UserModel.addItemToDatabaseSync(mContext, info);
                            for (String clz : classInfos) {
                                if (!clz.equals(info.classes)) {
                                    MainApplication.classInfos.add(info.classes);
                                }
                            }
                            for (String grade : gradeInfos) {
                                if (!grade.equals(info.grade)) {
                                    MainApplication.gradeInfos.add(info.grade);
                                }
                            }
                        } else if (!override) {
                            //Test
//                            score += 4;
//                            info.score = String.valueOf(score);
                            userInfos.add(info);
                            //Add to DB
//                                UserModel.addItemToDatabaseSync(mContext, info);

                            if (MainApplication.classInfos.isEmpty()) {
                                MainApplication.classInfos.add(info.classes);
                            } else {
                                boolean exist = false;
                                for (String clz : classInfos) {
                                    if (clz.equals(info.classes)) {
                                        exist = true;
                                        break;
                                    }
                                }
                                if (!exist) {
                                    MainApplication.classInfos.add(info.classes);
                                }
                            }
                            if (MainApplication.gradeInfos.isEmpty()) {
                                MainApplication.gradeInfos.add(info.grade);
                            } else {
                                boolean exist = false;
                                for (String grade : gradeInfos) {
                                    if (grade.equals(info.grade)) {
                                        exist = true;
                                        break;
                                    }
                                }
                                if (!exist) {
                                    MainApplication.gradeInfos.add(info.grade);
                                }
                            }
                        }
                    }
                }
                Log.d("hailong16", " size is " + userInfos.size());

                Log.d("hailong12", "columStr " + columStr);
                bundledusers = new ArrayList<>(userInfos);
                String classes = "";
                String grades = "";
                for (int i = 0; i < MainApplication.classInfos.size(); i++) {
                    String aa = MainApplication.classInfos.get(i);
                    if (i != MainApplication.classInfos.size() - 1) {
                        classes += aa + ";";
                    } else {
                        classes += aa;
                    }
                }
                for (int i = 0; i < MainApplication.gradeInfos.size(); i++) {
                    String bb = MainApplication.gradeInfos.get(i);
                    if (i != MainApplication.gradeInfos.size() - 1) {
                        grades += bb + ";";
                    } else {
                        grades += bb;
                    }
                }
                editor.putString("classes", classes);
                editor.putString("grades", grades);
                editor.putString("studentcolumns", columStr);
                editor.putBoolean("hasImport", true);
                editor.commit();
                book.close();

                long end = System.currentTimeMillis();
                Log.d("hailong17", " time is " + (end - start1));
            }
        } catch (Exception e) {
            Log.d("hailong14", " e message " + e.getMessage());
        }

        return !kvMap.isEmpty();
    }

    boolean existColum(String column) {
        for (String col : StudentColomn.studentColomn) {
            if (column.equals(col)) {
                return true;
            }
        }
        return false;
    }

    private class UserLoaderTask implements Runnable {
        private boolean mIsLaunching;
        private boolean mStopped;

        UserLoaderTask(boolean isLaunching) {
            mIsLaunching = isLaunching;
        }

        @Override
        public void run() {
            synchronized (mLock) {
                Process.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);

                loadUsers();
                bindusers();
            }

            synchronized (mLock) {
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
            }
        }


        public void stopLocked() {
            synchronized (UserLoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        public void bindusers() {
            final UserCallbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                return;
            }
            final ArrayList<UserInfo> normaluserItems = new ArrayList<>(bundledusers);
            final int N = normaluserItems.size();
            ITEMS_CHUNK = N / 20;
            if (ITEMS_CHUNK <= 0) {
                ITEMS_CHUNK = 1;
            }
            for (int i = 0; i < N; i += ITEMS_CHUNK) {
                final int start = i;
                final int chunkSize = (i + ITEMS_CHUNK <= N) ? ITEMS_CHUNK : (N - i);
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        UserCallbacks callbacks = tryGetCallbacks(oldCallbacks);
                        if (callbacks != null) {
                            callbacks.bindUserItems(normaluserItems, start, start + chunkSize);
                        }
                    }
                };
                mHandler.post(r);
            }

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    UserCallbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.finishBindingItems();
                    }
                }
            });
        }

        UserCallbacks tryGetCallbacks(UserCallbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }

                if (mCallbacks == null) {
                    return null;
                }

                final UserCallbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    return null;
                }

                return callbacks;
            }
        }

    }

    public void startLoader(boolean isLaunching) {
        synchronized (mLock) {
            // Don't bother to start the thread if we know it's not going to do
            // anything
            if (mCallbacks != null && mCallbacks.get() != null) {
                // If there is already one running, tell it to stop.
                // also, don't downgrade isLaunching if we're already running
                mLoaderTask = new UserLoaderTask(isLaunching);
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                sWorker.post(mLoaderTask);
                mCallbacks.get().startBinding();
            }
        }
    }

    public void stopLoader() {
        synchronized (mLock) {
            if (mLoaderTask != null) {
                mLoaderTask.stopLocked();
            }
        }
    }

    public interface UserCallbacks {
        public void startBinding();

        public void bindUserItems(ArrayList<UserInfo> userInfos, int start, int end);

        public void finishBindingItems();
    }
}
