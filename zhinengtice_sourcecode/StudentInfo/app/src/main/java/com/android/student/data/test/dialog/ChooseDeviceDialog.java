package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;


/**
 * 设备选择框，本地和U盘
 */
public class ChooseDeviceDialog extends Dialog {

    private Context context;

    public ChooseDeviceDialog(Context context) {
        super(context, R.style.device_choose_dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.data_device_choose, null);// 得到加载view
        TextView device_local = (TextView) v.findViewById(R.id.device_local);
        TextView device_U = (TextView) v.findViewById(R.id.device_U);
        device_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.sendBroadcast(new Intent(MainApplication.Import_Task_Local_Action));
                dismiss();
            }
        });
        device_U.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.sendBroadcast(new Intent(MainApplication.Import_Task_Usb_Action));
                dismiss();

            }
        });
        // main.xml中的ImageView
        setCanceledOnTouchOutside(true);
        setContentView(v);// 设置布局

    }


}

