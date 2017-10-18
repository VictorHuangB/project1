package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;


/**
 * 数据导入
 * 自定义加载旋转框
 */
public class DataImportDialog extends Dialog {

    private Context context;
    private String msg;
    private TextView tipTextView;
    private int type;
    private boolean showCancel = false;

    public DataImportDialog(Context context, String msg) {
        super(context, R.style.loading_dialog);
        // TODO Auto-generated constructor stub
        this.context = context;
        this.msg = msg;
    }

    public void setShowCancel(boolean showCancel) {
        this.showCancel = showCancel;
    }

    public void setType(int type) {
        this.type = type;
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
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息
        setCanceledOnTouchOutside(false);
        if (showCancel) {
            v.findViewById(R.id.dialog_cancel).setVisibility(View.VISIBLE);
            v.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dataCancelListener != null) {
                        dataCancelListener.cancelData();
                    }
                }
            });
        }
//        setCancelable(false);// 不可以用“返回键”取消
        setContentView(v);// 设置布局
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (type == 1) {
            context.sendBroadcast(new Intent(MainApplication.Import_Task_Cancel_Action));
        }
        Log.d("hailong15", " DataDialog onBackPress ");
    }

    public void setMessages(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);
    }

    DataCancelListener dataCancelListener;

    public void setDataCancelListener(DataCancelListener dataCancelListener) {
        this.dataCancelListener = dataCancelListener;
    }

    public interface DataCancelListener {
        void cancelData();
    }
}
