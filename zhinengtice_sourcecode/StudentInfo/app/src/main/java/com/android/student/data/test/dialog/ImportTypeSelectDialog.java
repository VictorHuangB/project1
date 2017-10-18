package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.android.student.data.test.R;

/**
 * 导入类型 一种是导入新的数据(清除原有数据),一种是重新导入，成绩不改变
 * Created by hailong on 2016/11/9 0009.
 */

public class ImportTypeSelectDialog extends Dialog {

    private Context context;

    public ImportTypeSelectDialog(Context context) {
        super(context, R.style.device_choose_dialog);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.import_type_dialog, null);// 得到加载view
        View import_new = v.findViewById(R.id.import_new);
        View import_override = v.findViewById(R.id.import_override);
        import_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null){
                    clickListener.operationLeft();
                }
                dismiss();
            }
        });
        import_override.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener!=null){
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
