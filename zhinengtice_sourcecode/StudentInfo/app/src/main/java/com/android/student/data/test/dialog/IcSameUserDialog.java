package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.student.data.test.R;
import com.android.student.data.test.db.UserInfo;

/**
 * 读IC卡的时候两个相同的学号，用户手动选择
 * Created by hailong on 2016/12/21
 */

public class IcSameUserDialog extends Dialog {

    private Context context;
    private UserInfo oldUserInfo;
    private UserInfo newUserInfo;

    public IcSameUserDialog(Context context, UserInfo oldUserInfo, UserInfo newUserInfo) {
        super(context, R.style.device_choose_dialog);
        this.context = context;
        this.oldUserInfo = oldUserInfo;
        this.newUserInfo = newUserInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.ic_sameuser_dialog, null);// 得到加载view
        TextView old_user = (TextView) v.findViewById(R.id.old_user);
        TextView new_user = (TextView) v.findViewById(R.id.new_user);

        old_user.setText("姓名：" + oldUserInfo.name + "   学号：" + oldUserInfo.code +
                "   年级：" + oldUserInfo.classes + "   班级：" + oldUserInfo.grade + "   性别：" + oldUserInfo.sex);
        new_user.setText("姓名：" + newUserInfo.name + "   学号：" + newUserInfo.code +
                "   年级：" + newUserInfo.classes + "   班级：" + newUserInfo.grade + "   性别：" + newUserInfo.sex);
        old_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.operationLeft();
                }
                dismiss();
            }
        });
        new_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.operationRight();
                }
                dismiss();
            }
        });
        setContentView(v);// 设置布局
    }

    private DialogClickListener clickListener = null;

    public void setDialogClickListener(DialogClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface DialogClickListener {
        void operationLeft();

        void operationRight();
    }

}
