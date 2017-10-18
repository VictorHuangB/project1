package com.android.student.data.test.project;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.student.data.test.R;

/**
 * 圆角背景，这样数字就不会跑出界外
 * @author hailong
 */
public class RoundCornerContainer extends FrameLayout {
    int radius = 0;

    public RoundCornerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        radius = context.getResources().getDimensionPixelSize(R.dimen.round_corner_radius);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //radius 的值等于宽高
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        Path path = new Path();
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        RectF rectF = new RectF(rect);
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
    }
}
