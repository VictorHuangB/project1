package com.android.student.data.test.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.student.data.test.MainApplication;
import com.android.student.data.test.R;
import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.studentinfo.StudentColomn;
import com.android.student.data.test.utils.AppUtils;
import com.android.student.data.test.utils.StringUtils;

import java.util.ArrayList;

/**
 * 导入类型 一种是导入新的数据(清除原有数据),一种是重新导入，成绩不改变
 * Created by hailong on 2016/11/9 0009.
 */

public class DataItemDisplayDialog extends Dialog {
    private Context context;
    ArrayList<String> columns = new ArrayList<>();
    UserInfo userInfo;

    public DataItemDisplayDialog(Context context, UserInfo userInfo) {
        super(context, R.style.StyleLicencePicTheme);
        this.context = context;
        columns = AppUtils.getColumns(context);
        this.userInfo = userInfo;
    }

    private void checkIfExist(DataViewHolder viewHolder) {
        if(!AppUtils.isWeight()){
            viewHolder.analysis.setVisibility(View.VISIBLE);
            viewHolder.score.setVisibility(View.VISIBLE);
        }else {
            viewHolder.weight.setVisibility(View.VISIBLE);
            viewHolder.height.setVisibility(View.VISIBLE);
        }
        for (String col : columns) {
            if (StudentColomn.NAME.equals(col)) {
                viewHolder.name.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CODE.equals(col)) {
                viewHolder.code.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.SEX.equals(col)) {
                viewHolder.sex.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.CLASSES.equals(col)) {
                viewHolder.classes.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.GRADE.equals(col)) {
                viewHolder.grade.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ID_CARD.equals(col)) {
                viewHolder.idcard.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.RACE.equals(col)) {
                viewHolder.race.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JIGUAN.equals(col)) {
                viewHolder.jiguan.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.ADDRESS.equals(col)) {
                viewHolder.adress.setVisibility(View.VISIBLE);
            }
            if (StudentColomn.JOB.equals(col)) {
                viewHolder.job.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DataViewHolder viewHolder = new DataViewHolder();
        View v = inflater.inflate(R.layout.data_item_display_dialog, null);// 得到加载view
        viewHolder.avatar = (ImageView) v.findViewById(R.id.avatar);
        viewHolder.avatar_label = (TextView) v.findViewById(R.id.avatar_label);
        viewHolder.name = (TextView) v.findViewById(R.id.name);
        viewHolder.code = (TextView) v.findViewById(R.id.code);
        viewHolder.sex = (TextView) v.findViewById(R.id.sex);
        viewHolder.classes = (TextView) v.findViewById(R.id.classes);
        viewHolder.grade = (TextView) v.findViewById(R.id.grade);
        viewHolder.score = (TextView) v.findViewById(R.id.score);
        viewHolder.analysis = (TextView) v.findViewById(R.id.analysis);
        viewHolder.weight = (TextView) findViewById(R.id.weight);
        viewHolder.height = (TextView) findViewById(R.id.height);
        viewHolder.idcard = (TextView) v.findViewById(R.id.idcard);
        viewHolder.race = (TextView) v.findViewById(R.id.race);
        viewHolder.jiguan = (TextView) v.findViewById(R.id.jiguan);
        viewHolder.adress = (TextView) v.findViewById(R.id.address);
        viewHolder.job = (TextView) v.findViewById(R.id.job);
        checkIfExist(viewHolder);

        final UserInfo studentInfo = userInfo;
        viewHolder.avatar.setVisibility(View.VISIBLE);
        viewHolder.avatar_label.setVisibility(View.GONE);
        try {
            if (StringUtils.isEmpty(studentInfo.avater_label)) {
                viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
            } else {
                Log.d("hailong18", " path is " + studentInfo.avater_label);
                viewHolder.avatar.setImageBitmap(BitmapFactory.decodeFile(studentInfo.avater_label));
            }
        } catch (Exception e) {
            viewHolder.avatar.setImageBitmap(MainApplication.defaultBitmap);
        }

        Resources resources = getContext().getResources();
        viewHolder.name.setTextColor(resources.getColor(R.color.screen_settings));
        viewHolder.code.setTextColor(resources.getColor(R.color.screen_settings));
        viewHolder.sex.setTextColor(resources.getColor(R.color.screen_settings));
        viewHolder.classes.setTextColor(resources.getColor(R.color.screen_settings));
        viewHolder.grade.setTextColor(resources.getColor(R.color.screen_settings));
        viewHolder.name.setText(studentInfo.name);
        viewHolder.code.setText(studentInfo.code);
        viewHolder.sex.setText(studentInfo.sex);
        viewHolder.classes.setText(studentInfo.classes);
        viewHolder.grade.setText(studentInfo.grade);

        String[] scores = UserModel.getScore(studentInfo);
        if(AppUtils.isWeight()){
            viewHolder.weight.setText(scores[0]+"kg");
            viewHolder.height.setText(scores[1]+"cm");
        }else {
            viewHolder.score.setText(AppUtils.getResult(scores[0]));
            viewHolder.analysis.setText(AppUtils.getRule(context, scores[0], "男".equals(studentInfo.sex)));
        }
//        viewHolder.score.setText(AppUtils.getResult(studentInfo.score));
//        viewHolder.analysis.setText(AppUtils.getRule(getContext(), studentInfo.score, "男".equals(studentInfo.sex)));
        viewHolder.idcard.setText(studentInfo.id_card);
        viewHolder.race.setText(studentInfo.race);
        viewHolder.jiguan.setText(studentInfo.jiguan);
        viewHolder.adress.setText(studentInfo.address);
        viewHolder.job.setText(studentInfo.job);
        setContentView(v);// 设置布局
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        lp.width = d.widthPixels;
        lp.height = d.heightPixels;
        dialogWindow.setAttributes(lp);
    }

    class DataViewHolder {
        ImageView avatar;
        TextView avatar_label;//头像
        TextView name;//姓名
        TextView code;//学号
        TextView sex;//性别
        TextView classes;//年级
        TextView grade;//班级
        TextView score;//成绩
        TextView analysis;//评级
        TextView weight;
        TextView height;
        TextView idcard;//身份证
        TextView race;//民族
        TextView jiguan;//籍贯
        TextView adress;//居住地址
        TextView job;//职务
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
