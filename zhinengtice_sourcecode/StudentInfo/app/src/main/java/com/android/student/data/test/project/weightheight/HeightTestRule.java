package com.android.student.data.test.project.weightheight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.android.student.data.test.R;

/**
 * 实现垂直的标尺
 * 这样设计的原因是，总共能容纳40个刻度，就是40cf，保证 刻度线的高*刻度数+刻度间隔+（刻度数-1） = M，M就是要算出的刻度尺的高度
 * 目前刻度高为1dp，刻度间隔为3dp，这样可以计算40*1+（40-1）*3=M，M=157，看xml的配置也是157，只要符合要求都可以
 * Created by hailong on 2016/11/16 0016.
 */
public class HeightTestRule extends View {
    private static final int rulesInSight = 40;//一视野只能容纳40条刻度
    private static final int Limit_Y = 280;
    private static int Max = 0;//280-40/2 = 260；
    private static int scrollMaxPos = 0;//scrollMaxPos  + getHeight = 最多滑动刻度

    protected Scroller mScroller;//靠它控制滑动
    protected float mDownMotionY;
    protected float mLastMotionY;
    protected float mTotalMotionY;

    protected static final int INVALID_POINTER = -1;
    protected int mActivePointerId = INVALID_POINTER;

    int indicateHeight;//刻度的高度 一般1dp即可
    int indicateGap;//刻度之间的间距
    int verticalLineWidth;
    Paint mPaint = new Paint();
    private Bitmap longBitmap, shortBitmap;//长刻度，短刻度
    private RuleChangeListener ruleChangeListener;//scrollY改变会触发

    public HeightTestRule(Context context, AttributeSet attrs) {
        super(context, attrs);
        //流畅度提升，开启硬件加速
        setLayerType(View.LAYER_TYPE_HARDWARE, mPaint);
        init();
    }

    public interface RuleChangeListener {
        void onChange(int level);
    }

    public void setRuleChangeListener(RuleChangeListener ruleChangeListener) {
        this.ruleChangeListener = ruleChangeListener;
    }

    //滚动到指定刻度
    public void scroll(int height) {
        if (height <= Max) {
            if (!isMoving()) {
                int finalY = Max * (indicateHeight + indicateGap) - indicateHeight;
                int deltaY = finalY - getScrollY() - height * (indicateGap + indicateHeight);
                mScroller.startScroll(0, getScrollY(), 0, deltaY, 500);
                invalidate();
            }
        } else {
            Toast.makeText(getContext(), "身高不能大于" + Max + "cm", Toast.LENGTH_SHORT).show();
        }
    }

    //是否在滚动，在滚动就不处理，否则滚动到指定位置
    public boolean isMoving() {
        return mScroller.computeScrollOffset();
    }

    //回到原点
    public void reset() {
        int finalY = Max * (indicateHeight + indicateGap) - indicateHeight;
        mScroller.startScroll(0, getScrollY(), 0, finalY - getScrollY(), 500);
        invalidate();
    }

    void init() {
        Max = Limit_Y - rulesInSight / 2;
        scrollMaxPos = Limit_Y + rulesInSight / 2 - 1;
        indicateGap = getResources().getDimensionPixelSize(R.dimen.indicate_gap);
        indicateHeight = getResources().getDimensionPixelSize(R.dimen.indicate_height);
        verticalLineWidth = getResources().getDimensionPixelSize(R.dimen.vertical_line_width);
        mScroller = new Scroller(getContext(), new ScrollInterpolator());

        mPaint.setStrokeWidth(verticalLineWidth);
        mPaint.setColor(getResources().getColor(R.color.indicateColor));
        mPaint.setAntiAlias(true);
        mPaint.setDither(false);
        mPaint.setFilterBitmap(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        longBitmap = createLongLine();
        shortBitmap = createshortLine();
        //开始滚动到原点
        int finalY = Max * (indicateHeight + indicateGap) - indicateHeight;
        scrollTo(0, finalY);
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // Don't bother scrolling if the page does not need to be moved
            if (getScrollX() != mScroller.getCurrX() || getScrollY() != mScroller.getCurrY()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
        }
    }

    private Bitmap createLongLine() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.line_long, null);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    //根据布局直接生成bitmap，就是这么懒
    private Bitmap createshortLine() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.line_short, null);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    private Bitmap createLabel(int scrollY) {
        TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.indicate_label, null);
        int count = (int) (scrollY / indicateHeight);
        view.setText(String.valueOf(count));
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = Limit_Y * (indicateGap + indicateHeight);
        //画竖线
        canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        canvas.drawLine(0, 0, 0, height, mPaint);
        canvas.restore();
        int count = height / indicateGap;
        //画刻度
        for (int i = count - 1; i >= 0; i--) {
            canvas.save();
            canvas.translate(getScrollX(), 0);
            canvas.translate(0, (Limit_Y - i) * indicateHeight + (Limit_Y - i - 1) * indicateGap);
            if (i % 10 == 0) {
                canvas.drawBitmap(longBitmap, 0, 0, mPaint);
                //画label
                Bitmap label = createLabel(i * indicateHeight);
                canvas.save();
                canvas.translate(longBitmap.getWidth() * 2, -(label.getHeight() - indicateHeight) / 2);
                canvas.drawBitmap(label, 0, 0, mPaint);
                canvas.restore();
                //画label

            } else {
                canvas.drawBitmap(shortBitmap, 0, 0, mPaint);
            }
            canvas.restore();
        }


    }

    @Override
    public void scrollTo(int x, int y) {
        //最多不超过
        if (y >= 0 && y <= (indicateGap + indicateHeight) * scrollMaxPos) {
            super.scrollTo(x, y);
        }
        if (ruleChangeListener != null) {
            int scrollY = getScrollY();
            int finalY = Max * (indicateHeight + indicateGap) - indicateHeight;
            int level = (finalY - scrollY) / (indicateHeight + indicateGap);
            ruleChangeListener.onChange(level);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mDownMotionY = mLastMotionY = ev.getY();
                mTotalMotionY = 0;
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float y = ev.getY(pointerIndex);
                final float deltaY = mLastMotionY - y;

                mTotalMotionY += Math.abs(deltaY);

                if (Math.abs(deltaY) >= 1.0f) {
                    scrollBy(0, (int) deltaY);
                    mLastMotionY = y;
                } else {
                    awakenScrollBars();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //Do nothing
                break;
        }
        return true;
    }


}
