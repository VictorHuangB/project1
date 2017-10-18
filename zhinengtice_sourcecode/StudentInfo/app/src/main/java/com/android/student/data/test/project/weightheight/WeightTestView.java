package com.android.student.data.test.project.weightheight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.student.data.test.R;

/**
 * 测量体重 公斤数0->200kg
 * Created by hailong on 2016/11/17 0017.
 */
public class WeightTestView extends View {
    public int Max = 200;//最大200Kg
    float weight;
    int verticalLineHeight;
    double indicateGap;//刻度之间的间距
    Paint mPaint = new Paint();
    int longWidth, middleWidth, shortWidth;//长刻度，中长刻度,短刻度

    public WeightTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_HARDWARE, new Paint());
        init();
    }

    void init() {
        //计算
        //h1 表示刻度高度，h2 表示gap
        //h1 = 1px,h2 = ?,已知 (h1+h2)*200 = Math.PI*width,算出h2
        int width = dip2px(getContext(), getResources().getDimensionPixelSize(R.dimen.weight_size));
        verticalLineHeight = getResources().getDimensionPixelSize(R.dimen.weight_indicate_height);
        indicateGap = width * Math.PI / 200;
        mPaint.setStrokeWidth(verticalLineHeight);
        mPaint.setColor(getResources().getColor(R.color.indicateColor));
        mPaint.setAntiAlias(true);
        mPaint.setDither(false);
        mPaint.setFilterBitmap(true);
    }

    /**
     * dip转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getResources();
        longWidth = resources.getDimensionPixelSize(R.dimen.weight_long_width);
        middleWidth = resources.getDimensionPixelSize(R.dimen.weight_middle_width);
        shortWidth = resources.getDimensionPixelSize(R.dimen.weight_short_width);
    }

    private Bitmap createLabel(String label) {
        TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.weight_label, null);
        view.setText(label);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = getWidth();
        int height = getHeight();
        //画刻度
        //angle*R = arcWidth
        for (int i = 0; i < Max; i++) {
            double angle = 360.0f / Max;
            if (i % 5 == 0) {
                if (i % 10 == 0) {
                    //长刻度
                    canvas.save();
                    canvas.rotate((float) (i * angle), width / 2, height / 2);
                    canvas.translate(0, height / 2);
                    canvas.drawLine(0, 0, longWidth, 0, mPaint);
                    Bitmap label = createLabel(String.valueOf(i));
                    canvas.translate(longWidth + 5, -(label.getHeight() - verticalLineHeight) / 2);
                    canvas.drawBitmap(label, 0, 0, mPaint);
                    canvas.restore();
                    //画label

                } else {
                    //中长刻度
                    canvas.save();
                    canvas.rotate((float) (i * angle), width / 2, height / 2);
                    canvas.translate(0, height / 2);
                    canvas.drawLine(0, 0, middleWidth, 0, mPaint);
                    canvas.restore();
                }
            } else {
                //短刻度
                canvas.save();
                canvas.rotate((float) (i * angle), width / 2, height / 2);
                canvas.translate(0, height / 2);
                canvas.drawLine(0, 0, shortWidth, 0, mPaint);
                canvas.restore();
            }
        }

    }
}
