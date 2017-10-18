package com.android.student.data.test.studentinfo;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.student.data.test.R;

public class AvatarDisplayDialog extends Dialog implements GestureImageView.TapupListaner {

    private Context context;
    GestureImageView licencePic;

    public AvatarDisplayDialog(Context context) {
        super(context, R.style.StyleLicencePicTheme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    public void setImageBitmap(Bitmap bm) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_delivery_needs_pic_dialog, null);
        licencePic = (GestureImageView) view.findViewById(R.id.activity_delivery_needs_gestuerimg);
        licencePic.setTapupListaner(this);
        licencePic.setImageBitmap(bm);
        setContentView(view);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = d.widthPixels;
        lp.height = d.heightPixels;
        dialogWindow.setAttributes(lp);

    }

    @Override
    public void onTapUp() {
        cancel();
    }
}