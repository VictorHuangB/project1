package com.android.student.data.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

/**
 * 解决有水平滑动控件时滑动太灵敏
 * 用动画的思路去实现,顺便解决多指情形
 * Created by hailong on 2016/12/16
 */
public class HomeScrollView extends ScrollView {
    int mTouchSlop;
    private View contentView;
    private float downX, downY;
    private int xy[] = new int[2];
    private static final float OVERSCROLL_DAMP_FACTOR = 0.14f;
    protected int mOverScrollY;
    float transY;
    float value;
    float mTotalScrollY;
    float mLastMotionY;

    protected int mActivePointerId = 0;

    public HomeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
        if (contentView != null) {
            contentView.setLayerType(LAYER_TYPE_HARDWARE, new Paint());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    //MM getWidth()/5 是临界点
    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if ((y < 0 && contentView.getScrollY() >= -getWidth() / 5) || getScrollY() >= contentView.getHeight() - getHeight()) {
            overScroll(y - getScrollY());
        }
    }

    // This curve determines how the effect of scrolling over the limits of the page dimishes
    // as the user pulls further and further from the bounds
    private float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return f * f * f + 1.0f;
    }

    private void overScroll(float amount) {
        dampedOverScroll(amount);
    }

    protected void dampedOverScroll(float amount) {
        int screenSize = getWidth() / 5;

        float f = (amount / screenSize);
        if (f == 0)
            return;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }
        int overScrollAmount = Math.round(OVERSCROLL_DAMP_FACTOR * f * screenSize);
        mOverScrollY = overScrollAmount;
        mTotalScrollY += mOverScrollY;
        contentView.setY(-mTotalScrollY);
    }

    ValueAnimator valueAnimator = new ValueAnimator();
    ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            value = (float) animation.getAnimatedValue();
            contentView.setY((1 - value) * transY);
        }
    };
    Animator.AnimatorListener listener = new AnimatorListenerAdapter()

    {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {
            contentView.setY(transY * (1 - value));
        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX(0);
                downY = ev.getY(0);
                mLastMotionY = downY;
                valueAnimator.cancel();//停在当前位置，动画结束
                mTotalScrollY = -contentView.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastMotionY = ev.getY(0);
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    pointerIndex = 0;
                }
                final float y = ev.getY(pointerIndex);
                if (getScrollY() <= 0 || (getScrollY() >= contentView.getHeight() - getHeight())) {
                    final float deltaY = mLastMotionY - y;
                    scrollBy(0, (int) deltaY);
                }
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTotalScrollY = 0;
                transY = contentView.getTranslationY();
                if (transY != 0) {
                    valueAnimator.setFloatValues(0, 1);
                    valueAnimator.setDuration(400);
                    valueAnimator.addUpdateListener(updateListener);
                    valueAnimator.addListener(listener);
                    valueAnimator.start();
                }
                mActivePointerId = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //位置交给当前的手指
    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int newPointerIndex = pointerIndex == 0 ? 1 : mActivePointerId;
        mLastMotionY = ev.getY(newPointerIndex);
        mActivePointerId = ev.getPointerId(newPointerIndex);
    }


}