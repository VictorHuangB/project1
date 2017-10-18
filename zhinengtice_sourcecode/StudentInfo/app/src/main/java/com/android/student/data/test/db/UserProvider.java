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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.student.data.test.MainApplication;

public class UserProvider extends ContentProvider {
    private static final String TAG = "UserProvider";
    private static final boolean DEBUG = false;

    private static final String DATABASE_NAME = "student.db";

    private static final int DATABASE_VERSION = 9;

    static final String AUTHORITY = "com.android.student.data.test.settings";

    static final String TABLE_FAVORITES = "favorites";
    static final String PARAMETER_NOTIFY = "notify";

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.d("hailong12", " onCreate is ");
        mOpenHelper = new DatabaseHelper(getContext());
        ((MainApplication) getContext()).setUserProvider(this);
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    private static long dbInsertAndCheck(DatabaseHelper helper, SQLiteDatabase db, String table, String nullColumnHack,
                                         ContentValues values) {
        if (!values.containsKey(UserSettings.Favorites._ID)) {
            throw new RuntimeException("Error: attempting to add item without specifying an id");
        }
        return db.insert(table, nullColumnHack, values);
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = dbInsertAndCheck(mOpenHelper, db, args.table, null, initialValues);
        if (rowId <= 0)
            return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (dbInsertAndCheck(mOpenHelper, db, args.table, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0)
            sendNotify(uri);

        return count;
    }

    public int bulkUpdate(Uri[] uri, ContentValues[] values) {
        if (uri.length != values.length) {
            return -1;
        }
        int numValues = values.length;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            for (int i = 0; i < numValues; i++) {
                SqlArguments args = new SqlArguments(uri[i],null,null);
                db.update(args.table, values[i], args.where, null);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        for (int i = 0; i < numValues; i++) {
            sendNotify(uri[i]);
        }
        return values.length;
    }


    public int deleteAll() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete("favorites", null, null);
        return count;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);

        if (count > 0)
            sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    public long generateNewId() {
        return mOpenHelper.generateNewId();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;
        private long mMaxId = -1;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;

            // In the case where neither onCreate nor onUpgrade gets called, we
            // read the maxId from
            // the DB here
            if (mMaxId == -1) {
                mMaxId = initializeMaxId(getWritableDatabase());
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (DEBUG)
                Log.d(TAG, "creating new launcher database");

            mMaxId = 1;
            //最多13项
            db.execSQL("CREATE TABLE favorites ("
                    + "_id INTEGER PRIMARY KEY,"
                    + "avaterpath TEXT,"
                    + "name TEXT,"
                    + "code TEXT,"
                    + "sex TEXT,"
                    + "class TEXT,"
                    + "grade TEXT,"
                    + "score TEXT,"
                    + "level TEXT,"
                    + "idcard TEXT,"
                    + "race TEXT,"
                    + "jiguan TEXT,"
                    + "address TEXT,"
                    + "job TEXT,"
                    //项目
                    + "projecttype INTEGER,"
                    + "Height TEXT,"//保存高度的成绩
                    + "Weight TEXT,"
                    + "Feihuoliang TEXT,"
                    + "Tiqianqu TEXT,"
                    + "Lidingtiaoyuan TEXT,"
                    + "Yintixiangshang TEXT,"
                    + "Tiaosheng TEXT,"
                    + "Yangwoqizuo TEXT,"
                    + "Changpaoyiqianwu TEXT,"
                    + "Changpaoyiqian TEXT,"
                    + "Changpaobabai TEXT,"
                    + "Duanpaoyibai TEXT,"
                    + "Duanpaowushi TEXT,"

                    + "icon BLOB"
                    + ");");
//            convertDatabase(db);
        }

        private boolean convertDatabase(SQLiteDatabase db) {
            if (DEBUG)
                Log.d(TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final Uri uri = Uri.parse("content://" + Settings.AUTHORITY + "/favorites?notify=true");
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
                // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    converted = copyFromCursor(db, cursor) > 0;
                } finally {
                    cursor.close();
                }

                if (converted) {
                    resolver.delete(uri, null, null);
                }
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            final int idIndex = c.getColumnIndexOrThrow(UserSettings.Favorites._ID);
            final int nameIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.NAME);
            final int codeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CODE);
            final int sexIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SEX);
            final int classIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.CLASS);
            final int gradeIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.GRADE);
            final int scoreIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.SCORE);
            final int levelIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.LEVEL);

            final int headIconIndex = c.getColumnIndexOrThrow(UserSettings.Favorites.ICON);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(UserSettings.Favorites._ID, c.getLong(idIndex));
                values.put(UserSettings.Favorites.NAME, c.getString(nameIndex));
                values.put(UserSettings.Favorites.CODE, c.getString(codeIndex));
                values.put(UserSettings.Favorites.SEX, c.getString(sexIndex));
                values.put(UserSettings.Favorites.CLASS, c.getString(classIndex));
                values.put(UserSettings.Favorites.GRADE, c.getString(gradeIndex));
                values.put(UserSettings.Favorites.SCORE, c.getString(scoreIndex));
                values.put(UserSettings.Favorites.LEVEL, c.getString(levelIndex));
                values.put(UserSettings.Favorites.ICON, c.getBlob(headIconIndex));
                Log.d("hailong12", " Name is " + c.getString(nameIndex));
                rows[i++] = values;
            }

            db.beginTransaction();
            int total = 0;
            try {
                int numValues = rows.length;
                for (i = 0; i < numValues; i++) {
                    if (dbInsertAndCheck(this, db, TABLE_FAVORITES, null, rows[i]) < 0) {
                        return 0;
                    } else {
                        total++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return total;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (DEBUG)
                Log.d(TAG, "onUpgrade triggered");

            int version = oldVersion;
            if (version != DATABASE_VERSION) {
                Log.w(TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                onCreate(db);
            }
        }

        // Generates a new ID to use for an object in your database. This method
        // should be only
        // called from the main UI thread. As an exception, we do call it when
        // we call the
        // constructor from the worker thread; however, this doesn't extend
        // until after the
        // constructor is called, and we only pass a reference to
        // LauncherProvider to LauncherApp
        // after that point
        public long generateNewId() {
            if (mMaxId < 0) {
                throw new RuntimeException("Error: max id was not initialized");
            }
            mMaxId += 1;
            return mMaxId;
        }

        private long initializeMaxId(SQLiteDatabase db) {
            Cursor c = db.rawQuery("SELECT MAX(_id) FROM favorites", null);

            // get the result
            final int maxIdIndex = 0;
            long id = -1;
            if (c != null && c.moveToNext()) {
                id = c.getLong(maxIdIndex);
            }
            if (c != null) {
                c.close();
            }

            if (id == -1) {
                throw new RuntimeException("Error: could not query max id");
            }

            return id;
        }
    }

    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
