package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.student.data.test.R;


/**
 * 系统设置对话框
 * Created by hailong on 2016/6/23
 */
public class AdminDialog extends Dialog {

    private Context mContext;
    //    private TextView dialog_message;
    private int type_phone = 1;
    private int type_normal = 2;
    private int type = type_normal;
    private View layout_single;
    private View layout_buttom;
    private TextView dialog_operation_left;
    private TextView dialog_operation_right;
    private EditText admin_et;
    private String leftOperationMsg;
    private String rightOperationMsg;
    private boolean emphasis = false;
    private boolean leftEmphasis = false;
    private boolean rightEmphasis = false;
    private boolean single = false;

    public AdminDialog(Context context, String leftOperationMsg, String rightOperationMsg) {
        this(context, R.style.StyleDialogNoTitle, leftOperationMsg, rightOperationMsg);
    }

    public AdminDialog(Context context, String leftOperationMsg, String rightOperationMsg, boolean emphasis) {
        this(context, R.style.StyleDialogNoTitle, leftOperationMsg, rightOperationMsg, emphasis);
    }

    public AdminDialog(Context context, String leftOperationMsg, String rightOperationMsg, boolean leftEmphasis, boolean rightEmphasis) {
        this(context, R.style.StyleDialogNoTitle, leftOperationMsg, rightOperationMsg, leftEmphasis, rightEmphasis);
    }

    public AdminDialog(Context context, int theme, String leftOperationMsg, String rightOperationMsg) {
        this(context, theme, leftOperationMsg, rightOperationMsg, false);
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * @param emphasis 操作颜色是否是蓝色
     */
    public AdminDialog(Context context, int theme, String leftOperationMsg, String rightOperationMsg, boolean emphasis) {
        super(context, theme);
        mContext = context;
        this.leftOperationMsg = leftOperationMsg;
        this.rightOperationMsg = rightOperationMsg;
        this.emphasis = emphasis;
    }

    /**
     * @param leftEmphasis,rightEmphasis 操作颜色是否是蓝色
     */
    public AdminDialog(Context context, int theme, String leftOperationMsg, String rightOperationMsg, boolean leftEmphasis, boolean rightEmphasis) {
        super(context, theme);
        mContext = context;
        this.leftOperationMsg = leftOperationMsg;
        this.rightOperationMsg = rightOperationMsg;
        this.leftEmphasis = leftEmphasis;
        this.rightEmphasis = rightEmphasis;
    }

    public void setupContentView() {
        View view = View.inflate(mContext, R.layout.admin_dialog, null);
        admin_et = (EditText) view.findViewById(R.id.admin_et);

        setContentView(view);
        applyCommon(view);
    }

    public void applyCommon(View view) {
        layout_buttom = view.findViewById(R.id.layout_buttom);
        layout_single = view.findViewById(R.id.layout_single);
        if (single) {
            layout_single.setVisibility(View.VISIBLE);
            layout_buttom.setVisibility(View.GONE);
        } else {
            layout_buttom.setVisibility(View.VISIBLE);
            layout_single.setVisibility(View.GONE);
        }
        if (single) {
            layout_single.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.operationLeft();
                    }
                    dismiss();
                }
            });
        } else {
            dialog_operation_left = (TextView) view.findViewById(R.id.dialog_operation_left);
            dialog_operation_right = (TextView) view.findViewById(R.id.dialog_operation_right);
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
                    if (clickListener != null) {
                        String adminCode = admin_et.getText().toString();
                        clickListener.operationRight(adminCode);
                    }
//                    dismiss();
                }
            });
            dialog_operation_left.setText(leftOperationMsg);
            dialog_operation_right.setText(rightOperationMsg);
            if (emphasis) {
                dialog_operation_left.setTextColor(mContext.getResources().getColor(R.color.avater_dialog_text_color));
                dialog_operation_right.setTextColor(mContext.getResources().getColor(R.color.avater_dialog_text_color));
            } else if (leftEmphasis) {
                dialog_operation_left.setTextColor(mContext.getResources().getColor(R.color.avater_dialog_text_color));
            } else if (rightEmphasis) {
                dialog_operation_right.setTextColor(mContext.getResources().getColor(R.color.avater_dialog_text_color));
            }
        }
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.75f);
        getWindow().setAttributes(lp);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private DialogClickListener clickListener = null;

    public void setDialogClickListener(DialogClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface DialogClickListener {
        void operationLeft();

        void operationRight(String adminCode);
    }

}

