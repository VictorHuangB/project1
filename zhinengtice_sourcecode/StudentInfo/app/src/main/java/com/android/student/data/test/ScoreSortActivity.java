package com.android.student.data.test;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.rwusb.DataUtils;
import com.android.student.data.test.rwusb.Global;
import com.android.student.data.test.rwusb.PL2303Driver;
import com.android.student.data.test.rwusb.USBDriver;
import com.android.student.data.test.rwusb.WorkService;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 成绩排名
 * Created by hailong on 2016/10/19 0019.
 */
public class ScoreSortActivity extends Activity {
    View back_container;//返回按钮

    Spinner search_group_grade_spnner;//集体查询选择年级下拉框
    Spinner search_group_class_spinner;//集体查询选择班级下拉框
    View search_group_container;//集体查询搜索按钮

    View search_grade_label;
    View search_class_label;
    ListView data_list_boy;
    ListView data_list_girl;
    DataAdapter boyAdapter;
    DataAdapter girlAdapter;
    boolean gradeSelect = false;
    boolean classSelect = false;
    int classPos = 0;
    int gradePos = 0;
    GroupLoadTask groupLoadTask;
    final ArrayList<String> classlist = new ArrayList<>();
    final ArrayList<String> gradelist = new ArrayList<>();
    DataImportDialog groupDialog;
    private static Handler mHandler = null;
    private ArrayList<UserInfo> boyInfos = new ArrayList<>();
    private ArrayList<UserInfo> girlInfos = new ArrayList<>();
    private static boolean connectSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_sort_layout);
        initViews();
        initData();
        mHandler = new MHandler(this);
        WorkService.addHandler(mHandler);
    }

    public void probe() {
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceList.size() > 0) {
            final UsbDevice device = deviceIterator.next();

            PendingIntent mPermissionIntent = PendingIntent
                    .getBroadcast(
                            ScoreSortActivity.this,
                            0,
                            new Intent(
                                    ScoreSortActivity.this
                                            .getApplicationInfo().packageName),
                            0);
            if (!mUsbManager.hasPermission(device)) {
                mUsbManager.requestPermission(device,
                        mPermissionIntent);
                Toast.makeText(getApplicationContext(),
                        Global.toast_usbpermit, Toast.LENGTH_LONG)
                        .show();
            } else if (!connectSuccess) {
                USBDriver.USBPort port = new USBDriver.USBPort(mUsbManager, device);
                PL2303Driver.TTYTermios serial = new PL2303Driver.TTYTermios(9600,
                        PL2303Driver.TTYTermios.FlowControl.NONE, PL2303Driver.TTYTermios.Parity.NONE,
                        PL2303Driver.TTYTermios.StopBits.ONE, 8);
                WorkService.workThread.connectUsb(port, serial);
            } else {
                print();
            }
        } else {
            toastMsg("没有可用打印机");
        }
    }

    private void toastMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScoreSortActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }


    static class MHandler extends Handler {

        ScoreSortActivity mActivity;

        MHandler(ScoreSortActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (mActivity == null) {
                return;
            }
            switch (msg.what) {
                /**
                 * DrawerService 的 onStartCommand会发送这个消息
                 */
                case Global.CMD_POS_WRITERESULT: {
                    int result = msg.arg1;
//                    Toast.makeText(theActivity, (result == 1) ? Global.toast_success : Global.toast_fail,
//                            Toast.LENGTH_SHORT).show();
                    break;
                }
                case Global.MSG_WORKTHREAD_SEND_CONNECTUSBRESULT: {
                    int result = msg.arg1;
                    Toast.makeText(
                            MainApplication.getInstance(),
                            (result == 1) ? "连接成功"
                                    : "连接失败", Toast.LENGTH_SHORT).show();
                    if (1 == result) {
                        connectSuccess = true;
                    } else {
                        connectSuccess = false;
                    }
                    mActivity.probe();
                    break;
                }

            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (search_group_container != null) {
            search_group_container.performClick();
        }
    }

    void launchActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(
                R.anim.right_in, R.anim.left_out);
    }

    private byte[] convertBytes(byte[] buf1, byte[] buf2) {
        byte[] lastByte1 = new byte[buf1.length + buf2.length];
        DataUtils.copyBytes(buf2, 0, lastByte1, 0, buf2.length);
        DataUtils.copyBytes(buf1, 0, lastByte1, buf2.length, buf1.length);
        return lastByte1;
    }

    private byte[] getBytes(String text) {
        byte[] strbuf = new byte[0];
        try {
            strbuf = text.getBytes("gbk");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strbuf;
    }

    private String getRightResult(String result) {
        return result == null ? "0" : result;
    }

    private byte[] getSend(ArrayList<byte[][]> byteArray) {
        byte[] lastByte1 = new byte[0];
        byte[] buf;
        for (byte[][] item : byteArray) {
            buf = DataUtils.byteArraysToBytes(item);
            lastByte1 = convertBytes(buf, lastByte1);
        }
        return lastByte1;
    }

    public void print() {
        byte[] setHT = {0x1b, 0x44, 0x18, 0x00};
        byte[] HT = {0x09};
        byte[] LF = {0x0d, 0x0a};
        //TODO
        int count = boyInfos.size();
        //模拟打印前10位学生的成绩
        List<UserInfo> boys = new ArrayList<>(boyInfos);
        List<UserInfo> girls = new ArrayList<>(girlInfos);
//        if (boyInfos.size() >= count) {
//            boys = this.boyInfos.subList(0, count);
//        } else {
//            boys = new ArrayList<>(boyInfos);
//        }
//
//        if (girlInfos.size() >= count) {
//            girls = this.girlInfos.subList(0, count);
//        } else {
//            girls = new ArrayList<>(girlInfos);
//        }
        byte[][] head = new byte[][]{
                setHT, getBytes("男生成绩"), HT, getBytes(""), LF, LF
        };

        ArrayList<byte[][]> arrayList = new ArrayList<>();
        arrayList.add(head);

        for (UserInfo info : boys) {
            byte[][] name = new byte[][]{
                    setHT, getBytes("姓名"), HT, getBytes(info.name), LF
            };
            byte[][] code = new byte[][]{
                    setHT, getBytes("学号"), HT, getBytes(info.code), LF
            };
            byte[][] claz = new byte[][]{
                    setHT, getBytes("年级"), HT, getBytes(info.classes), LF
            };
            byte[][] grade = new byte[][]{
                    setHT, getBytes("班级"), HT, getBytes(info.grade), LF
            };

            arrayList.add(name);
            arrayList.add(code);
            arrayList.add(claz);
            arrayList.add(grade);
            switch (MainApplication.currentProject) {
                case MainApplication.Project_Feihuoliang:
                    byte[][] result = new byte[][]{
                            setHT, getBytes("肺活量"), HT, getBytes(info.feihuoliang + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Weight:
                    result = new byte[][]{
                            setHT, getBytes("身高"), HT, getBytes(getRightResult(info.height) + "cm"), LF
                    };
                    arrayList.add(result);
                    result = new byte[][]{
                            setHT, getBytes("体重"), HT, getBytes(getRightResult(info.weight) + "kg"), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Yangwoqizuo:
                     result = new byte[][]{
                            setHT, getBytes("仰卧起坐"), HT, getBytes(info.yangwoqizuo + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Yintixiangshang:
                    result = new byte[][]{
                            setHT, getBytes("引体向上"), HT, getBytes(info.yintixiangshang + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
            }


        }
        byte[][] end = new byte[][]{
                LF
        };

        arrayList.add(end);
        head = new byte[][]{
                setHT, getBytes("女生成绩"), HT, getBytes(""), LF, LF
        };

        arrayList.add(head);

        for (UserInfo info : girls) {
            byte[][] name = new byte[][]{
                    setHT, getBytes("姓名"), HT, getBytes(info.name), LF
            };
            byte[][] code = new byte[][]{
                    setHT, getBytes("学号"), HT, getBytes(info.code), LF
            };
            byte[][] claz = new byte[][]{
                    setHT, getBytes("年级"), HT, getBytes(info.classes), LF
            };
            byte[][] grade = new byte[][]{
                    setHT, getBytes("班级"), HT, getBytes(info.grade), LF
            };

            arrayList.add(name);
            arrayList.add(code);
            arrayList.add(claz);
            arrayList.add(grade);
            switch (MainApplication.currentProject) {
                case MainApplication.Project_Feihuoliang:
                    byte[][] result = new byte[][]{
                            setHT, getBytes("肺活量"), HT, getBytes(info.feihuoliang + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Weight:
                    result = new byte[][]{
                            setHT, getBytes("身高"), HT, getBytes(getRightResult(info.height) + "cm"), LF
                    };
                    arrayList.add(result);
                    result = new byte[][]{
                            setHT, getBytes("体重"), HT, getBytes(getRightResult(info.weight) + "kg"), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Yangwoqizuo:
                    result = new byte[][]{
                            setHT, getBytes("仰卧起坐"), HT, getBytes(info.yangwoqizuo + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
                case MainApplication.Project_Yintixiangshang:
                    result = new byte[][]{
                            setHT, getBytes("引体向上"), HT, getBytes(info.yintixiangshang + MainApplication.per), LF, LF
                    };
                    arrayList.add(result);
                    break;
            }


        }
        end = new byte[][]{
                LF, LF
        };

        arrayList.add(end);

        byte[] buf = getSend(arrayList);

        if (WorkService.workThread.isConnected()) {
            Bundle data = new Bundle();
            data.putByteArray(Global.BYTESPARA1, buf);
            data.putInt(Global.INTPARA1, 0);
            data.putInt(Global.INTPARA2, buf.length);
            WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);
        } else {
            Toast.makeText(this, Global.toast_notconnect, Toast.LENGTH_SHORT).show();
            connectSuccess = false;
            //连接打印机
            probe();
        }
    }

    private void initViews() {
        ((TextView) findViewById(R.id.title)).setText("成绩排名");
        View right_container = findViewById(R.id.right_container);
        right_container.setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.right_icon)).setImageResource(R.drawable.print);
        ((TextView) findViewById(R.id.right_label)).setText("打印成绩");
        right_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boyInfos.size() <= 0 && girlInfos.size() <= 0) {
                    Toast.makeText(ScoreSortActivity.this, "没有学生成绩", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    probe();
                }

            }
        });
        back_container = findViewById(R.id.back_container);
        data_list_boy = (ListView) findViewById(R.id.data_list_boy);
        data_list_girl = (ListView) findViewById(R.id.data_list_girl);

        search_group_grade_spnner = (Spinner) findViewById(R.id.search_group_grade_spinner);
        search_group_class_spinner = (Spinner) findViewById(R.id.search_group_class_spinner);
        search_group_container = findViewById(R.id.search_group_container);

        search_grade_label = findViewById(R.id.search_grade_label);
        search_class_label = findViewById(R.id.search_class_label);
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
            boyInfos.clear();
            girlInfos.clear();
            String classes = params[0];
            String grade = params[1];

            ArrayList<UserInfo> userInfos = null;
            if (AppUtils.isWeight()) {
                userInfos = PatternUtils.getUserInfosByCode(UserModel.queryUserInfoFilter(ScoreSortActivity.this, classes, grade));
            } else {
                userInfos = PatternUtils.getUserInfosByLevel(UserModel.queryUserInfoFilter(ScoreSortActivity.this, classes, grade));
            }

            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            for (UserInfo info : userInfos) {
                if (info.sex.equals("男")) {
                    boyInfos.add(info);
                } else if (info.sex.equals("女")) {
                    girlInfos.add(info);
                }
            }
            boyAdapter = new DataAdapter(ScoreSortActivity.this, boyInfos, true);
            data_list_boy.setAdapter(boyAdapter);
            girlAdapter = new DataAdapter(ScoreSortActivity.this, girlInfos, false);
            data_list_girl.setAdapter(girlAdapter);
            if (groupDialog != null) {
                groupDialog.dismiss();
            }
        }
    }

    private void initData() {
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        search_group_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupDialog = new DataImportDialog(ScoreSortActivity.this, "正在查询");
                groupDialog.setCancelable(false);
                groupLoadTask = new GroupLoadTask();
                groupLoadTask.execute(classlist.get(search_group_class_spinner.getSelectedItemPosition()), gradelist.get(search_group_grade_spnner.getSelectedItemPosition()));
            }
        });

        gradelist.add("请选择班级");
        gradelist.addAll(PatternUtils.sortGrades(/*UserModel.getGrades(ScoreSortActivity.this)*/MainApplication.gradeInfos));
        gradelist.add("");
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                gradelist);
        search_group_grade_spnner.setAdapter(adapter);

        search_group_grade_spnner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
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

        classlist.add("请选择年级");
        classlist.addAll(PatternUtils.sortClasses(/*UserModel.getClasses(ScoreSortActivity.this)*/MainApplication.classInfos));
        classlist.add("");
        adapter = new ArrayAdapter(this, R.layout.app_spinner_layout,
                classlist);
        search_group_class_spinner.setAdapter(adapter);
        search_group_class_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
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

    class DataAdapter extends BaseAdapter {
        Context mContext;
        LayoutInflater inflater;
        ArrayList<UserInfo> infos = new ArrayList<>();
        boolean isBoy;

        public DataAdapter(Context mContext, ArrayList<UserInfo> infos, boolean isBoy) {
            this.mContext = mContext;
            inflater = LayoutInflater.from(mContext);
            this.infos = infos;
            this.isBoy = isBoy;
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
            final UserInfo studentInfo = infos.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sort_result_layout, null);
                viewHolder.sort = (TextView) convertView.findViewById(R.id.sort);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.code = (TextView) convertView.findViewById(R.id.code);
                viewHolder.sex = (ImageView) convertView.findViewById(R.id.sex);
                viewHolder.score = (TextView) convertView.findViewById(R.id.score);
                viewHolder.weight = (TextView) convertView.findViewById(R.id.weight);
                viewHolder.height = (TextView) convertView.findViewById(R.id.height);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (DataViewHolder) convertView.getTag();
            }
            if (!AppUtils.isWeight()) {
                viewHolder.score.setVisibility(View.VISIBLE);
            } else {
                viewHolder.weight.setVisibility(View.VISIBLE);
                viewHolder.height.setVisibility(View.VISIBLE);
            }
            if (isBoy) {
                viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.sort.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.score.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.weight.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.height.setTextColor(mContext.getResources().getColor(R.color.boy_text_color));
                viewHolder.sex.setImageResource(R.drawable.boy);
            } else {
                viewHolder.name.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.code.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.sort.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.score.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.weight.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.height.setTextColor(mContext.getResources().getColor(R.color.app_white_color));
                viewHolder.sex.setImageResource(R.drawable.girl);
            }
            viewHolder.sort.setText(String.valueOf(position + 1));
            viewHolder.name.setText(studentInfo.name);
            viewHolder.code.setText(studentInfo.code);
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
            }
//            viewHolder.score.setText(AppUtils.getResult(studentInfo.score));

            return convertView;
        }
    }

    class DataViewHolder {
        TextView sort;//名次
        TextView name;//姓名
        TextView code;//学号
        ImageView sex;
        TextView score;//成绩
        TextView weight;
        TextView height;
    }
}
