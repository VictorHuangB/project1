package com.android.student.data.test;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.db.UserInfo;
import com.android.student.data.test.db.UserModel;
import com.android.student.data.test.dialog.DataImportDialog;
import com.android.student.data.test.pattern.PatternUtils;
import com.android.student.data.test.result.SingleSearchResultActivity;

import java.util.ArrayList;

import static com.android.student.data.test.R.id.search_single_container;

/**
 * 水平分析
 * Created by hailong on 2016/10/19 0019.
 */

public class LevelAnalysisActivity extends Activity {
    View search_complex_bubble;
    AutoCompleteTextView search_et;
    View search_complex_container;
    //模糊的输入框
    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private Bitmap mDragOutline = null;

    private Paint mRegionDrawPaint;
    LocalLoadTask loadTask;
    DataImportDialog dataImportDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_analysis_layout);
        initViews();
//        silentLoadTask = new SilentLoadTask();
//        silentLoadTask.execute();

    }

    private void initViews() {
        ((TextView) findViewById(R.id.title)).setText("水平分析");
        View right_container = findViewById(R.id.right_container);
        right_container.setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.right_icon)).setImageResource(R.drawable.edit);
        ((TextView) findViewById(R.id.right_label)).setText("评分标准");
        right_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchActivity(ScoreRuleActivity.class);
            }
        });
        search_complex_bubble = findViewById(R.id.search_complex_bubble);
        search_et = (AutoCompleteTextView) findViewById(R.id.search_et);
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search_complex_container.performClick();
                return false;
            }
        });
        mDragOutline = createDragOutline();
        search_et.setBackgroundDrawable(new FastBitmapDrawable(mDragOutline));
        search_complex_container = findViewById(R.id.search_complex_container);
        search_complex_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_et.getText().toString().isEmpty()) {
                    Toast.makeText(LevelAnalysisActivity.this, "请输入学号查询", Toast.LENGTH_SHORT).show();
                    return;
                }
                dataImportDialog = new DataImportDialog(LevelAnalysisActivity.this, "正在查询...");
                dataImportDialog.setCancelable(false);
                loadTask = new LocalLoadTask();
                String str = search_et.getText().toString().replace(" ", "");
                if (str.contains("(")) {
                    str = str.substring(str.indexOf("(") + 1, str.length() - 1);
                }
                if (str.length() < 2) {
                    Toast.makeText(LevelAnalysisActivity.this, "至少输入两个字符，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                loadTask.execute(str);
//                Toast.makeText(LevelAnalysisActivity.this, "查询", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.back_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(0, 1);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setDuration(5000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                search_complex_bubble.setScaleX(1.0f + value * value / 8f);
                search_complex_bubble.setScaleY(1.0f + value * value / 8f);
            }
        });
        valueAnimator.start();
    }


    void launchActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
        overridePendingTransition(
                R.anim.right_in, R.anim.left_out);
    }


    class LocalLoadTask extends AsyncTask<String, Void, ArrayList<UserInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dataImportDialog != null && !dataImportDialog.isShowing()) {
                Log.d("hailong13", " show ");
                dataImportDialog.show();
            }
        }

        @Override
        protected ArrayList<UserInfo> doInBackground(String... params) {
            //查询信息
            ArrayList<UserInfo> userInfos = new ArrayList<>();
            String search = params[0];
            if (search != null) {
                userInfos = UserModel.queryUserInfoByCode(LevelAnalysisActivity.this, search);
//                if (search.length() > 4) {
//                    userInfos = UserModel.queryUserInfoByCode(LevelAnalysisActivity.this, search);
//                    if (userInfos.isEmpty()) {
//                        userInfos = UserModel.queryUserInfoByName(LevelAnalysisActivity.this, search);
//                    }
//                } else {
//                    userInfos = UserModel.queryUserInfoByName(LevelAnalysisActivity.this, search);
//                    if (userInfos.isEmpty()) {
//                        userInfos = UserModel.queryUserInfoByCode(LevelAnalysisActivity.this, search);
//                    }
//                }
            }
            //默认按学号排序
            userInfos = PatternUtils.getUserInfosByCode(userInfos);
            return userInfos;
        }

        @Override
        protected void onPostExecute(ArrayList<UserInfo> userInfos) {
            if (userInfos.isEmpty()) {
                Toast.makeText(LevelAnalysisActivity.this, "没有匹配的学生信息!", Toast.LENGTH_SHORT).show();
            } else {
                MainApplication.searchInfos = userInfos;
                Intent intent = new Intent();
                intent.putExtra("title", "水平分析结果");
                intent.setClass(LevelAnalysisActivity.this, SingleSearchResultActivity.class);
                startActivity(intent);

                overridePendingTransition(
                        R.anim.right_in, R.anim.left_out);
            }
            if (dataImportDialog != null && dataImportDialog.isShowing()) {
                dataImportDialog.dismiss();
            }
        }
    }

    /**
     * Generate a roundrect shadow
     */
    private Bitmap createDragOutline() {
        final Canvas canvas = new Canvas();
        final int outlineColor = Color.WHITE;
        int[] size = new int[2];
        int measureSpec = View.MeasureSpec.UNSPECIFIED;
        View view = findViewById(R.id.search_et_container);
        view.measure(measureSpec, measureSpec);
        size[0] = getResources().getDisplayMetrics().widthPixels - 2 * getResources().getDimensionPixelSize(R.dimen.complex_et_margin);
        size[1] = /*view.getMeasuredHeight()*/getResources().getDimensionPixelSize(R.dimen.search_et_height);
        Log.d("hailong11", " widgth is " + size[0]);
        final Bitmap b = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
        canvas.setBitmap(b);
        RectF rf = new RectF(0, 0, size[0], size[1]);
        rf.inset(6, 6);
        if (mRegionDrawPaint == null) {
            mRegionDrawPaint = new Paint();
            mRegionDrawPaint.setFilterBitmap(true);
            mRegionDrawPaint.setAntiAlias(true);
        }
        final float radius = 8.0f * MainApplication.getScreenDensity();
        canvas.drawRoundRect(rf, radius, radius, mRegionDrawPaint);
        mOutlineHelper.applyMediumExpensiveOutlineWithBlur(b, canvas, outlineColor, outlineColor);
        canvas.setBitmap(null);

        return b;
    }


}
