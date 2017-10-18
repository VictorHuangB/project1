package com.android.student.data.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.AppDialog;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.dialog.DataItemDisplayDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.studentinfo.StudentColomn;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.util.ArrayList;

/**
 * 数据管理界面
 * Created by hailong on 2016/11/9.
 */
public class DataManagerActivity extends Activity {
    View back;
    Spinner search_group_grade_spnner;//集体查询选择年级下拉框
    Spinner search_group_class_spinner;//集体查询选择班级下拉框
    View search_group_container;//集体查询搜索按钮

    View search_grade_label;
    View search_class_label;
    View add;
    boolean gradeSelect = false;
    boolean classSelect = false;
    int classPos = 0;
    int gradePos = 0;
    DataImportDialog groupDialog;
    GroupLoadTask groupLoadTask;
    final ArrayList<String> classlist = new ArrayList<>();
    final ArrayList<String> gradelist = new ArrayList<>();
    ArrayList<String> columns = new ArrayList<>();
    ListView data_list;
    DataAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_manager);
        columns = AppUtils.getColumns(this);
        initViews();
        initData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (search_group_container != null) {
            search_group_container.performClick();
        }
    }

    void initViews() {
        back = findViewById(R.id.back);
        data_list = (ListView) findViewById(R.id.data_list);
        search_group_grade_spnner = (Spinner) findViewById(R.id.search_group_grade_spinner);
        search_group_class_spinner = (Spinner) findViewById(R.id.search_group_class_spinner);
        search_group_container = findViewById(R.id.search_group_container);

        search_grade_label = findViewById(R.id.search_grade_label);
        search_class_label = findViewById(R.id.search_class_label);

        add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DataManagerActivity.this, DataEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title", "添加名单");
                bundle.putString("confirm", "确认添加");
                bundle.putBoolean("adduser", true);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("hailong13", " classlist size " + classlist.size() + " getSelectedItemPosition() " + search_group_class_spinner.getSelectedItemPosition());
            String lastClass = classlist.get(search_group_class_spinner.getSelectedItemPosition());
            String lastGrade = gradelist.get(search_group_grade_spnner.getSelectedItemPosition());
            initSpinner();
            if (groupDialog != null) {
                groupDialog.dismiss();
            }
            groupDialog = new DataImportDialog(DataManagerActivity.this, "正在更新");
            groupDialog.setCancelable(false);
            groupLoadTask = new GroupLoadTask();
            groupLoadTask.execute(lastClass, lastGrade);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    void initData() {
        IntentFilter filter = new IntentFilter("com.data.edit.update");
        registerReceiver(receiver, filter);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        search_group_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupDialog = new DataImportDialog(DataManagerActivity.this, "正在查询");
                groupDialog.setCancelable(false);
                groupLoadTask = new GroupLoadTask();
                groupLoadTask.execute(classlist.get(search_group_class_spinner.getSelectedItemPosition()), gradelist.get(search_group_grade_spnner.getSelectedItemPosition()));
            }
        });
        initSpinner();
    }

    void initSpinner() {
        gradelist.clear();
        gradelist.add("请选择班级");
        gradelist.addAll(PatternUtils.sortGrades(/*UserModel.getGrades(DataManagerActivity.this)*/MainApplication.gradeInfos));
        gradelist.add("");
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                gradelist);
        search_group_grade_spnner.setAdapter(adapter);

        search_group_grade_spnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 && gradePos < gradelist.size()) {
                    search_group_grade_spnner.setSelection(gradePos);
                }
                if (!gradeSelect) {
                    gradeSelect = true;
                    search_grade_label.setVisibility(View.VISIBLE);
                } else {
                    search_grade_label.setVisibility(View.GONE);
                }
                gradePos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                search_grade_label.setVisibility(View.VISIBLE);
            }

        });
        classlist.clear();
        classlist.add("请选择年级");
        classlist.addAll(PatternUtils.sortClasses(/*UserModel.getClasses(DataManagerActivity.this)*/MainApplication.classInfos));
        classlist.add("");
        adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                classlist);
        search_group_class_spinner.setAdapter(adapter);
        search_group_class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 && classPos < classlist.size()) {
                    search_group_class_spinner.setSelection(classPos);
                }
                if (!classSelect) {
                    classSelect = true;
                    search_class_label.setVisibility(View.VISIBLE);
                } else {
                    search_class_label.setVisibility(View.GONE);
                }
                classPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    class GroupLoadTask extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (groupDialog != null && !groupDialog.isShowing()) {
                groupDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            String classes = params[0];
            String grade = params[1];
            ArrayList<UserInfo> userInfos = PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(DataManagerActivity.this, classes, grade));
            return userInfos;
        }

        @Override
        protected void onPostExecute(final ArrayList<UserInfo> userInfos) {
//            MainApplication.searchInfos = userInfos;
//            Intent intent = new Intent();
//            intent.putExtra("title", "集体查询结果");
//            intent.setClass(DataManagerActivity.this, DataManagerActivity.class);
//            startActivity(intent);
//          
//            overridePendingTransition(
//                    R.anim.right_in, R.anim.left_out);
            final ArrayList<UserInfo> infos = new ArrayList<>(userInfos);
            dataAdapter = new DataAdapter(DataManagerActivity.this, userInfos);
            setItemListener(new ItemListener() {
                @Override
                public void delete(final UserInfo info, final int position) {
                    final AppDialog appDialog = new AppDialog(DataManagerActivity.this, "取消", "确定", true);
                    appDialog.setupContentView("确定删除" + info.name + "?");
                    appDialog.setDialogClickListener(new AppDialog.DialogClickListener() {
                        @Override
                        public void operationLeft() {
                            appDialog.dismiss();
                        }

                        @Override
                        public void operationRight() {
                            //delete in DB
                            UserModel.deleteItemFromDatabase(DataManagerActivity.this, info);
                            infos.remove(position);
                            dataAdapter.setInfos(infos);
                            if (infos.size() > 0) {
                                data_list.setVisibility(View.VISIBLE);
                            } else {
                                data_list.setVisibility(View.GONE);
                            }
                            appDialog.dismiss();
                        }

                    });
                    appDialog.show();

                }

                @Override
                public void edit(UserInfo info, int position) {
                    //跳转到编辑页面
                    Intent intent = new Intent(DataManagerActivity.this, DataEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", info);
                    bundle.putString("title", "名单编辑");
                    bundle.putString("confirm", "确认修改");
                    bundle.putBoolean("adduser", false);
                    intent.putExtras(bundle);
//                    intent.putExtra("code", info.code);

                    startActivity(intent);
                }
            });
            data_list.setAdapter(dataAdapter);
            if (infos.size() > 0) {
                data_list.setVisibility(View.VISIBLE);
            } else {
                data_list.setVisibility(View.GONE);
            }
            if (groupDialog != null) {
                groupDialog.dismiss();
            }
        }
    }

    private void checkIfExist(DataViewHolder viewHolder) {
        if (!AppUtils.isWeight()) {
            //viewHolder.analysis.setVisibility(View.VISIBLE);
            viewHolder.score.setVisibility(View.VISIBLE);
        } else {
            viewHolder.weight.setVisibility(View.VISIBLE);
            viewHolder.height.setVisibility(View.VISIBLE);
        }
        for (String col : columns) {
            if (StudentColomn.NAME.equals(col)) {
                viewHolder.name.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CODE.equals(col)) {
                viewHolder.code.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.SEX.equals(col)) {
                viewHolder.sex.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CLASSES.equals(col)) {
                viewHolder.classes.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.GRADE.equals(col)) {
                viewHolder.grade.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ID_CARD.equals(col)) {
                viewHolder.idcard.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.RACE.equals(col)) {
                viewHolder.race.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JIGUAN.equals(col)) {
                viewHolder.jiguan.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ADDRESS.equals(col)) {
                viewHolder.adress.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JOB.equals(col)) {
                viewHolder.job.setVisibility(View.VISIBLE);
            }
        }


    }

    class DataAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;
        ArrayList<UserInfo> infos = new ArrayList<>();

        public DataAdapter(Context mContext, ArrayList<UserInfo> infos) {
            this.mContext = mContext;
            inflater = LayoutInflater.from(mContext);
            this.infos = infos;
        }

        public void setInfos(ArrayList<UserInfo> infos) {
            this.infos = infos;
            notifyDataSetChanged();
            notifyDataSetInvalidated();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            DataViewHolder viewHolder = new DataViewHolder();
            final UserInfo studentInfo = infos.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.data_manager_item, null);
                viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                viewHolder.avatar_label = (TextView) convertView.findViewById(R.id.avatar_label);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.code = (TextView) convertView.findViewById(R.id.code);
                viewHolder.sex = (TextView) convertView.findViewById(R.id.sex);
                viewHolder.classes = (TextView) convertView.findViewById(R.id.classes);
                viewHolder.grade = (TextView) convertView.findViewById(R.id.grade);
                viewHolder.score = (TextView) convertView.findViewById(R.id.score);
                viewHolder.analysis = (TextView) convertView.findViewById(R.id.analysis);
                viewHolder.weight = (TextView) convertView.findViewById(R.id.weight);
                viewHolder.height = (TextView) convertView.findViewById(R.id.height);
                viewHolder.idcard = (TextView) convertView.findViewById(R.id.idcard);
                viewHolder.race = (TextView) convertView.findViewById(R.id.race);
                viewHolder.jiguan = (TextView) convertView.findViewById(R.id.jiguan);
                viewHolder.adress = (TextView) convertView.findViewById(R.id.address);
                viewHolder.job = (TextView) convertView.findViewById(R.id.job);
                viewHolder.delete = convertView.findViewById(R.id.delete);
                viewHolder.edit = convertView.findViewById(R.id.edit);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DataViewHolder) convertView.getTag();
            }
            checkIfExist(viewHolder);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    AvatarDisplayDialog displayDialog = new AvatarDisplayDialog(DataManagerActivity.this);
//                    displayDialog.setImageBitmap(studentInfo.headImage);
//                    displayDialog.show();
                    DataItemDisplayDialog displayDialog = new DataItemDisplayDialog(DataManagerActivity.this, studentInfo);
                    displayDialog.show();
                }
            });
            viewHolder.avatar.setVisibility(View.VISIBLE);
            viewHolder.avatar_label.setVisibility(View.GONE);
            try {
                if (StringUtils.isEmpty(studentInfo.avater_label)) {
                    viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
                } else {
                    Log.d("hailong18", " path is " + studentInfo.avater_label);
                    viewHolder.avatar.setImageBitmap(BitmapFactory.decodeFile(studentInfo.avater_label));
                }
            } catch (Exception e) {
                viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
            }
            viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.sex.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.classes.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.grade.setTextColor(mContext.getResources().getColor(R.color.screen_settings));
            viewHolder.name.setText(studentInfo.name);
            viewHolder.code.setText(studentInfo.code);
            viewHolder.sex.setText(studentInfo.sex);
            viewHolder.classes.setText(studentInfo.classes);
            viewHolder.grade.setText(studentInfo.grade);
            String[] scores = UserModel.getScore(studentInfo);
            if (AppUtils.isWeight()) {
                if (!StringUtils.isEmpty(scores[0])) {
                    viewHolder.weight.setText(scores[0] + "kg");
                } else {
                    viewHolder.weight.setText("---kg");
                }
                if (!StringUtils.isEmpty(scores[1])) {
                    viewHolder.height.setText(scores[1] + "cm");
                } else {
                    viewHolder.height.setText("---cm");
                }
            } else {
                viewHolder.score.setText(AppUtils.getResult(scores[0]));
                viewHolder.analysis.setText(AppUtils.getRule(DataManagerActivity.this, scores[0], "男".equals(studentInfo.sex)));
            }

            viewHolder.idcard.setText(studentInfo.id_card);
            viewHolder.race.setText(studentInfo.race);
            viewHolder.jiguan.setText(studentInfo.jiguan);
            viewHolder.adress.setText(studentInfo.address);
            viewHolder.job.setText(studentInfo.job);
            viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemListener != null) {
                        itemListener.delete(studentInfo, position);
                    }
                }
            });
            viewHolder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (itemListener != null) {
                        itemListener.edit(studentInfo, position);
                    }
                }
            });
            return convertView;
        }


    }

    public void setItemListener(ItemListener itemListener) {
        this.itemListener = itemListener;
    }

    ItemListener itemListener;

    interface ItemListener {
        void delete(UserInfo info, int position);

        void edit(UserInfo info, int position);
    }

    class DataViewHolder {
        ImageView avatar;
        TextView avatar_label;//头像
        TextView name;//姓名
        TextView code;//学号
        TextView sex;//性别
        TextView classes;//年级
        TextView grade;//班级
        TextView score;//成绩
        TextView analysis;//评级
        TextView weight;//重量
        TextView height;//高度
        TextView idcard;//身份证
        TextView race;//民族
        TextView jiguan;//籍贯
        TextView adress;//居住地址
        TextView job;//职务
        View delete;//删除
        View edit;//编辑
    }

}
