package com.android.student.data.test.project;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.student.data.test.R;


/**
 * 连接外设Dialog
 * 自定义加载旋转框
 */
public class DeviceConnectDialog extends Dialog {

    private Context context;
    private String msg;
    private TextView tipTextView;

    public DeviceConnectDialog(Context context, String msg) {
        super(context, R.style.loading_dialog);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.data_import, null);// 得到加载view
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);
        spaceshipImage.setImageResource(R.drawable.connect_icon);
        tipTextView.setText(msg);// 设置加载信息
        setCanceledOnTouchOutside(false);
//        setCancelable(false);// 不可以用“返回键”取消
        setContentView(v);// 设置布局
    }

    public void setMsg(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);// 设置加载信息
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setMessages(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);
    }
}
