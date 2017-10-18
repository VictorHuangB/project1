package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.student.data.test.R;

/**
 * 自定义加载旋转框
 */
public class LoadingDialog extends Dialog {


    private Context context;
    private String msg;
    private TextView tipTextView;

    public LoadingDialog(Context context, String msg) {
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

        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
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
//        setCancelable(false);// 不可以用“返回键”取消
        setContentView(v);// 设置布局

    }

    public void setMessages(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);
    }
}
