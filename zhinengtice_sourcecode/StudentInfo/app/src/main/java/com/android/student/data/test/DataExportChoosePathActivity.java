package com.android.student.data.test;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 数据导出，选择路径导出
 * Created by hailong on 2016/12/5 0005.
 */

public class DataExportChoosePathActivity extends Activity {
    String baseRoot = "";
    String currentPath = "";
    boolean isUsb;
    LinearLayout file_index_list;
    ListView file_list;
    TextView file_select_name;
    View file_confirm;
    ArrayList<String> files = new ArrayList<>();
    FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_choose_dialog);
        baseRoot = getIntent().getStringExtra("baseRoot");
        isUsb = getIntent().getBooleanExtra("isUsb", false);
        initViews();
        initData();
    }

    private void initViews() {
        View back_container = findViewById(R.id.back_container);
        back_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.title)).setText("请选择路径");

        file_index_list = (LinearLayout) findViewById(R.id.file_index_list);
        file_list = (ListView) findViewById(R.id.file_list);
        file_select_name = (TextView) findViewById(R.id.file_select_name);
        file_confirm = findViewById(R.id.file_confirm);
    }

    private void initData() {
        LinearLayout file_base_index_list = (LinearLayout) findViewById(R.id.file_base_index_list);
        if (!isUsb) {
            View v = createIndexView("本地存储");
            file_base_index_list.addView(v);
        } else if (!StringUtils.isEmpty(baseRoot)) {
//            if (baseRoot.contains("/")) {
//                String[] paths = baseRoot.split("[/]");
//                if (paths != null) {
//                    for (int i = 0; i < paths.length; i++) {
//                        String path = paths[i];
//                        View v = createIndexView(path);
//                        file_base_index_list.addView(v);
//                    }
//                }
//            } else {
//                View v = createIndexView(baseRoot);
//                file_base_index_list.addView(v);
//            }
            View v = createIndexView("外部存储");
            file_base_index_list.addView(v);
        }
        file_base_index_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (files != null) {
                    files.clear();
                }
                file_select_name.setText("");
                file_index_list.removeAllViews();
                file_list.setVisibility(View.VISIBLE);
                initFileList();
            }
        });
        initFileList();
        file_list.setOnItemClickListener(onItemClickListener);
        file_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择确认，拿到当前的
                Intent intent = new Intent("com.student.choose.root");
                intent.putExtra("root", currentPath);
                sendBroadcast(intent);
                finish();
            }
        });
    }

    private void initFileList() {
        currentPath = baseRoot;
        LocalLoadTask localLoadTask = new LocalLoadTask();
        localLoadTask.execute();
    }

    class LocalLoadTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            files = getFileList(baseRoot);
            return files;
        }

        @Override
        protected void onPostExecute(ArrayList<String> files) {
            if (files == null || files.size() == 0) {
                Toast.makeText(DataExportChoosePathActivity.this, "没有找到存储信息", Toast.LENGTH_SHORT).show();
                finish();
            }
            fileAdapter = new FileAdapter();
            file_list.setAdapter(fileAdapter);
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= files.size()) {
                return;
            }
            String path = files.get(position);
            updateFileList(path, false);
        }
    };

    private View createIndexView(String path) {
        View v = LayoutInflater.from(this).inflate(R.layout.file_index_item, null);
        TextView file_index_name = (TextView) v.findViewById(R.id.file_index_name);
        file_index_name.setText(path);
        return v;
    }

    private void addIndexHeader(String path) {
        View v = createIndexView(path);
        v.setTag(path);
        file_index_list.addView(v);
        file_select_name.setText(path);
        currentPath = getCurrentPath(file_index_list.getChildCount() - 1);
        Log.d("hailong", "Add currentPAth " + currentPath);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = file_index_list.indexOfChild(v);
                if (position >= 0) {
                    removeIndexHeader(position);
                }
            }
        });
    }

    private void removeIndexHeader(int position) {
        int count = file_index_list.getChildCount();
        if (files.size() == 0 || position >= files.size()) {
            file_select_name.setText("unknown");
        } else {
            file_select_name.setText(files.get(position));
        }
        currentPath = getCurrentPath(file_index_list.getChildCount() - 1);
        Log.d("hailong", "remove currentPAth " + currentPath);
        String path;
        if (count > position + 1) {
            path = getCurrentPath(position);
            Log.d("hailong", " path " + path);
            for (int i = count - 1; i > position; i--) {
                file_index_list.removeViewAt(i);
            }
            updateFileList(path, true);
        }

    }

    private String getCurrentPath(int position) {
        int count = file_index_list.getChildCount();
        String path = new String(baseRoot + File.separator);
        if (count > position) {
            for (int i = 0; i <= position; i++) {
                View child = file_index_list.getChildAt(i);
                path += (child.getTag() + File.separator);
            }
        }
        return path;
    }

    private void updateFileList(String path, boolean withRoot) {
        //判断
        ArrayList<String> tmpList;
        String curPath = "";
        if (withRoot) {
            curPath = currentPath = path;
        } else {
            if (currentPath.endsWith(File.separator)) {
                curPath = currentPath + path;
            } else {
                curPath = currentPath + File.separator + path;
            }
        }
        if (!withRoot) {
            addIndexHeader(path);
        }
        ArrayList<String> list = getFileList(curPath);
        if (list != null) {
            if (list.size() > 0) {
                files.clear();
                files.addAll(list);
                if (fileAdapter != null) {
                    file_list.setVisibility(View.VISIBLE);
                    fileAdapter.notifyDataSetChanged();
                }
            } else {
                if (fileAdapter != null) {
                    fileAdapter.notifyDataSetChanged();
                    file_list.setVisibility(View.GONE);
                }
            }
        } else {
            //Toast.makeText(DataExportChoosePathActivity.this, "文件夹为空", Toast.LENGTH_LONG).show();
            if (fileAdapter != null) {
                fileAdapter.notifyDataSetChanged();
                file_list.setVisibility(View.GONE);
            }
        }
    }

    public ArrayList<String> getFileList(String strPath) {
        ArrayList<String> list = new ArrayList<>();
        File dir = new File(strPath);
        File[] files = dir.listFiles();

        if (files == null)
            return null;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                list.add(file.getName());
            }
        }
        return list;
    }

    class FileAdapter extends BaseAdapter {
        @Override
        public Object getItem(int position) {
            return files == null ? null : files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return files == null ? 0 : files.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FileHolder fileHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(DataExportChoosePathActivity.this).inflate(R.layout.file_list_item, null);
                fileHolder = new FileHolder();
                fileHolder.file_name = (TextView) convertView.findViewById(R.id.file_name);
                convertView.setTag(fileHolder);
            } else {
                fileHolder = (FileHolder) convertView.getTag();
            }
            if (files != null) {
                fileHolder.file_name.setText(files.get(position));
            }
            return convertView;
        }
    }

    class FileHolder {
        TextView file_name;
    }
}

