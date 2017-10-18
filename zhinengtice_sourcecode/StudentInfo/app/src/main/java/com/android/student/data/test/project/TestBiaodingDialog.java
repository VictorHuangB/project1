package com.android.student.data.test.project;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.student.data.test.R;


/**
 * 进入标定
 */
public class TestBiaodingDialog extends Dialog {

    private Context context;
    private String msg;
    private TextView tipTextView;
    TextView biaoding_tv;
    EditText input_et;
    TextView input_unit;
    View dialog_operation_left;
    View dialog_operation_right;

    public TestBiaodingDialog(Context context, String msg) {
        super(context, R.style.loading_dialog);
        this.context = context;
        this.msg = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.biaoding_dialog, null);// 得到加载view
        // main.xml中的ImageView
        tipTextView = (TextView) v.findViewById(R.id.tipTextView);
        tipTextView.setText(msg);// 设置加载信息
        biaoding_tv = (TextView) v.findViewById(R.id.biaoding_tv);
        input_et = (EditText) v.findViewById(R.id.input_et);
        input_unit = (TextView) v.findViewById(R.id.input_unit);
        dialog_operation_left = v.findViewById(R.id.dialog_operation_left);
        dialog_operation_right = v.findViewById(R.id.dialog_operation_right);

        dialog_operation_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.operationLeft();
                }
                dismiss();
            }
        });

        dialog_operation_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String biaoding = input_et.getText().toString();
                if (clickListener != null) {
                    clickListener.operationRight(biaoding);
                }
                dismiss();
            }
        });

        setCanceledOnTouchOutside(false);
//        setCancelable(false);// 不可以用“返回键”取消
        setContentView(v);// 设置布局
    }

    public void setMsg(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);// 设置加载信息
    }

    public void setUinit(String unit) {
        input_unit.setText(unit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setMessages(String msg) {
        this.msg = msg;
        tipTextView.setText(msg);
    }

    private BiaodingDialogClickListener clickListener = null;

    public void setDialogClickListener(BiaodingDialogClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface BiaodingDialogClickListener {
        void operationLeft();

        void operationRight(String biaoding);
    }
}
