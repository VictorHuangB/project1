package com.android.student.data.test;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.dialog.LoadingDialog;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;

/**
 * 读取Ecel的内容
 * Created by hailong on 2016/10/17 0017.
 */
public class ExelImportSimpleActivity extends Activity {
    protected Dialog loadingDialog;
    protected Dialog searchDialog;
//    PullLayout pull_layout;
//    RecyclerView mRecycler;
//    StaggeredGridLayoutManager gridLayoutManager;
//    LinearLayoutManager mLinearLayoutManager;
    boolean hasMore = false;
    int currentMode = 0;//0 下拉，1上拉
    boolean loadComplete = false;

    View search;
    EditText search_et;//搜索框
    View search_clear;//删除搜索内容
    ListView data_list;//学生信息
    View search_result_container;//查询结果窗口
    View search_result_close;//关闭查询窗口
    ListView search_result;//搜索列表
    String searchStr;
    TextView search_result_label;

    DataAdapter dataAdapter;
    LoadTask loadTask;

    SearchTask searchTask;
    DataAdapter resultAdapter;
    String[] avatars;
    /**
     * xls保存位置
     */
    public static final String Datalist_path =
            Environment.getExternalStorageDirectory() + "/datamanager/datalist/";
    /**
     * 头像保存位置
     */
    public static final String Pictures_path =
            Environment.getExternalStorageDirectory() + "/datamanager/pictures/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excel_test);
        loadingDialog = createLoadingDialog(
                this, "正在获取学生信息...");
        loadingDialog.show();
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (!loadComplete) {
                    loadTask.cancel(true);
                    Toast.makeText(ExelImportSimpleActivity.this, "获取信息中断", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ExelImportSimpleActivity.this, "获取信息成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
        if (loadTask == null) {
            loadTask = new LoadTask();
            loadTask.execute();
        }
    }

    @Override
    public void onBackPressed() {
        if (search_result_container.getVisibility() == View.VISIBLE) {
            search_result_container.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();

    }

    class SearchTask extends AsyncTask<String, Void, ArrayList<StudentInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            searchDialog = createLoadingDialog(
                    ExelImportSimpleActivity.this, "正在查询，请稍候...");
            searchDialog.show();
            searchDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                }
            });
        }

        @Override
        protected ArrayList<StudentInfo> doInBackground(String... params) {
            ArrayList<StudentInfo> studentInfos = new ArrayList<>();
            if (params[0] == null) {
                return studentInfos;
            }
            String searchStr = params[0].toLowerCase();
            ArrayList<StudentInfo> tmpInfos = new ArrayList<>(allstudentInfos);
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.avater_label = "头像";
            studentInfo.name = "姓名";
            studentInfo.code = "学号";
            studentInfo.sex = "性别";
            studentInfo.classes = "年级";
            studentInfo.grade = "班级";
            studentInfos.add(studentInfo);
            for (StudentInfo info : tmpInfos) {
                String name = info.name.toString();
                String code = info.code.toString();
                if (name.toLowerCase().contains(searchStr)
                        || code.toLowerCase().contains(searchStr)) {
                    studentInfos.add(info);
                }
            }
            return studentInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<StudentInfo> studentInfos) {
            search_result_container.setVisibility(View.VISIBLE);
            resultAdapter = new DataAdapter(ExelImportSimpleActivity.this, studentInfos);
            search_result.setAdapter(resultAdapter);
            if (searchDialog != null && searchDialog.isShowing()) {
                searchDialog.dismiss();
            }
            search_result_label.setText("查询到" + (studentInfos.size() - 1) + "条结果");
            Toast.makeText(ExelImportSimpleActivity.this, "查询完毕!", Toast.LENGTH_SHORT).show();
        }
    }

    class LoadTask extends AsyncTask<Void, Void, ArrayList<StudentInfo>> {

        @Override
        protected ArrayList<StudentInfo> doInBackground(Void... params) {
            ArrayList<StudentInfo> studentInfos = new ArrayList<>();
            try {
                File baseFile = new File(Pictures_path);
                avatars = baseFile.list();
//                avatars = getAssets().list("pictures");
            } catch (Exception e) {

            }
            if (avatars == null) {
                return studentInfos;
            }
            try {
                File baseFile = new File(Datalist_path);
                FileInputStream fileInputStream = new FileInputStream(baseFile.listFiles()[0]);
//                InputStream inputStream = getAssets().open("namelist_demo.xls");
                Workbook book = Workbook.getWorkbook(fileInputStream);
                int num = book.getNumberOfSheets();
                Sheet sheet = book.getSheet(0);
                if (sheet != null) {
                    int Rows = sheet.getRows();
                    int Cols = sheet.getColumns();
                    for (int i = 0; i < Rows; ++i) {
                        StudentInfo info = new StudentInfo();
                        for (int j = 0; j < Cols; ++j) {
                            if (j == 0) {
                                info.name = sheet.getCell(j, i).getContents();
                            }
                            if (j == 1) {
                                info.code = sheet.getCell(j, i).getContents();
                            }
                            if (j == 2) {
                                info.sex = sheet.getCell(j, i).getContents();
                            }
                            if (j == 3) {
                                info.classes = sheet.getCell(j, i).getContents();
                            }
                            if (j == 4) {
                                info.grade = sheet.getCell(j, i).getContents();
                            }
                            if (avatars != null && i - 1 < avatars.length) {
                                try {
                                    info.avater_label = "pictures/" + avatars[i - 1];
                                } catch (Exception e) {

                                }
                            }
                        }
                        studentInfos.add(info);
                    }
                    book.close();
                }
            } catch (Exception e) {

            }
            //组合
            allstudentInfos = new ArrayList<>(studentInfos);
            return studentInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<StudentInfo> studentInfos) {
            if (loadingDialog != null) {
                if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
            TextView count = (TextView) findViewById(R.id.count);
            count.setText("共" + (studentInfos.size() - 1) + "条信息");
            if (dataAdapter == null) {
                dataAdapter = new DataAdapter(ExelImportSimpleActivity.this, studentInfos);
                data_list.setAdapter(dataAdapter);
            }
            if (studentInfos.size() <= 0) {
                Toast.makeText(ExelImportSimpleActivity.this, "学生信息为空!", Toast.LENGTH_SHORT).show();
            }
            loadComplete = true;
        }
    }

    private void initViews() {
        search = findViewById(R.id.search);
        search_et = (EditText) findViewById(R.id.search_et);
        search_clear = findViewById(R.id.search_clear);
        search_result_label = (TextView) findViewById(R.id.search_result_label);

        data_list = (ListView) findViewById(R.id.data_list);
        search_result_container = findViewById(R.id.search_result_container);
        search_result_close = findViewById(R.id.search_result_close);
        search_result = (ListView) findViewById(R.id.search_result);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchStr = search_et.getText().toString();
                if ("".equals(searchStr)) {
                    search_result_container.setVisibility(View.GONE);
                } else {
                    requestSearch();
                }
            }
        });
        search_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_result_container.setVisibility(View.GONE);
                search_et.setText("");
                if (searchTask != null) {
                    searchTask.cancel(true);
                }
            }
        });
        search_result_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_result_container.setVisibility(View.GONE);
            }
        });
//        search_et.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                searchStr = search_et.getText().toString();
//                if ("".equals(searchStr)) {
//                    search_result_container.setVisibility(View.GONE);
//                } else {
//                    requestSearch();
//                }
//            }
//        });

    }

    private void requestSearch() {
        if (allstudentInfos.size() <= 0) {
            Toast.makeText(ExelImportSimpleActivity.this, "学生信息为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        searchTask = new SearchTask();
        searchTask.execute(searchStr);
    }

    ArrayList<StudentInfo> allstudentInfos = new ArrayList<>();

    class StudentInfo {
        String avatar;//头像
        String avater_label;
        String name;//姓名
        String code;//学号
        String sex;//性别
        String classes;//年级
        String grade;//班级
    }

    class DataAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;
        ArrayList<StudentInfo> infos = new ArrayList<>();

        public DataAdapter(Context mContext, ArrayList<StudentInfo> infos) {
            this.mContext = mContext;
            inflater = LayoutInflater.from(mContext);
            this.infos = infos;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DataViewHolder viewHolder = new DataViewHolder();
            final StudentInfo studentInfo = infos.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.student_info_avatar, null);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.avatar_label = (TextView) convertView.findViewById(R.id.avatar_label);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.code = (TextView) convertView.findViewById(R.id.code);
                viewHolder.sex = (TextView) convertView.findViewById(R.id.sex);
                viewHolder.classes = (TextView) convertView.findViewById(R.id.classes);
                viewHolder.grade = (TextView) convertView.findViewById(R.id.grade);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DataViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.avatar.setImageBitmap(null);
                viewHolder.avatar.setVisibility(View.GONE);
                viewHolder.avatar_label.setVisibility(View.VISIBLE);
                viewHolder.avatar_label.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
                viewHolder.avatar_label.setText("头像");
                viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
                viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
                viewHolder.sex.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
                viewHolder.classes.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
                viewHolder.grade.setTextColor(mContext.getResources().getColor(R.color.excel_attribute_color));
            } else {
                viewHolder.avatar.setVisibility(View.VISIBLE);
                viewHolder.avatar_label.setVisibility(View.GONE);
                try {
                    viewHolder.avatar.setImageBitmap(BitmapFactory.decodeStream(getResources().getAssets().open(studentInfo.avater_label)));
                } catch (Exception e) {

                }
                viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
                viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
                viewHolder.sex.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
                viewHolder.classes.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
                viewHolder.grade.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            }

            viewHolder.name.setText(studentInfo.name);
            viewHolder.code.setText(studentInfo.code);
            viewHolder.sex.setText(studentInfo.sex);
            viewHolder.classes.setText(studentInfo.classes);
            viewHolder.grade.setText(studentInfo.grade);
            return convertView;
        }
    }

    class DataViewHolder {
        ImageView avatar;
        TextView avatar_label;//头像
        TextView name;//姓名
        TextView code;//学号
        TextView sex;//性别
        TextView classes;//年级
        TextView grade;//班级
    }

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public Dialog createLoadingDialog(Context context, String msg) {

        Dialog loadingDialog = new LoadingDialog(context, msg);// 创建自定义样式dialog

        return loadingDialog;

    }

}