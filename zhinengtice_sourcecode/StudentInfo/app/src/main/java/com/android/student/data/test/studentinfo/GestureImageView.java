package com.android.student.data.test.studentinfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * 功能说明：手势缩放图片，拖动图片
 * 作者：hailong
 * 时间：15/9/21
 */
public class GestureImageView extends ImageView {
    //缩放或者拖动？
    int mode = NONE;
    private static final int NONE = 0;
    private static final int DRAG = 1;//拖动
    private static final int ZOOM = 2;//缩放

    float downX = 0;
    float downY = 0;
    PointF mid = new PointF();
    float oldDist = 1f;
    Matrix matrix = new Matrix();
    Matrix matrix1 = new Matrix();
    Matrix savedMatrix = new Matrix();

    int mTouchSlop;

    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        matrix = new Matrix();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.setMatrix(matrix);
        super.draw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                downX = event.getX();
                downY = event.getY();
                savedMatrix.set(matrix);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = ZOOM;
                oldDist = calculateDistance(event);
                savedMatrix.set(matrix);
                midPoint(mid, event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    matrix1.set(savedMatrix);
                    float newDist = calculateDistance(event);
                    float scale = newDist / oldDist;
                    matrix1.postScale(scale, scale, mid.x, mid.y);// scale
                    matrix.set(matrix1);
                    invalidate();
                } else if (mode == DRAG) {
                    matrix1.set(savedMatrix);
                    matrix1.postTranslate(event.getX() - downX, event.getY() - downY);// translate
                    matrix.set(matrix1);
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if ((Math.abs(event.getX() - downX) < mTouchSlop / 2 && Math.abs(event.getY() - downY) < mTouchSlop / 2)) {
                    mTapupListaner.onTapUp();
                }
                mode = NONE;
                break;
        }
        return true;
    }


    /**
     * @param event
     * @return 两指间距
     */
    private float calculateDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * @param point 两指中间位置
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    //单击后事件
    private TapupListaner mTapupListaner;

    public void setTapupListaner(TapupListaner mTapupListaner) {
        this.mTapupListaner = mTapupListaner;
    }

    public interface TapupListaner {
        void onTapUp();
    }
}
