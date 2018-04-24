package com.lab.dxy.bracelet.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 项目名称：Ali_Sophix
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/25
 */
public class HeartRateLineView extends View {

    private int mWidth;
    private int mHeight;

    public HeartRateLineView(Context context) {
        this(context, null);
    }

    public HeartRateLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartRateLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wMode == MeasureSpec.EXACTLY) {
            mWidth = wSize;
        } else {
            mWidth = dp2px(400);
        }
        if (hMode == MeasureSpec.EXACTLY) {
            mHeight = hSize;
        } else {
            mHeight = dp2px(200);
        }

    }

    int startLine;
    private volatile int pathLength;
    /**
     * 偏移量
     **/
    private int cfOffset;
    private int count = 5;
    private int minWidth = 6;//心跳最小间隔
    private int maxWidth = 100;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        startLine = maxWidth;
        int left = getPaddingLeft();
        canvas.translate(left, mHeight / 2);
        Path path = new Path();

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp2px(2));
        paint.setColor(Color.parseColor("#FF7269"));


        count = (mWidth - left - maxWidth) / (maxWidth + minWidth * 14);

        path.lineTo(startLine, 0);
        for (int i = 0; i < count; i++) {
            //+10，-50
            startLine = startLine + minWidth;
            path.lineTo(startLine, -50);
            //+30,50
            startLine = startLine + minWidth * 3;
            path.lineTo(startLine, 50);
            //+40，-100
            startLine = startLine + minWidth * 4;
            path.lineTo(startLine, -100);
            //40,100
            startLine = startLine + minWidth * 4;
            path.lineTo(startLine, 100);
            //20,0
            startLine = startLine + minWidth * 2;
            path.lineTo(startLine, 0);
            startLine = startLine + maxWidth;
            path.lineTo(startLine, 0);
        }

        path.lineTo(mWidth, 0);

        //路径动画 https://www.jianshu.com/p/3efa5341abcc
        //分割路径
        Path cutPath = new Path();
        //第二个参数表示是否闭合
        PathMeasure pathMeasure = new PathMeasure();
        pathMeasure.setPath(path, false);
        pathLength = (int) pathMeasure.getLength();
        //api表示截取整个path片段   第一个参数截取的开始长度，第二个白叟接收长度，
        pathMeasure.getSegment(-pathLength + cfOffset, cfOffset, cutPath, true);


        canvas.drawPath(cutPath, paint);
//        canvas.drawPath(path, paint);
    }

    private ValueAnimator cgAnima;

    /**
     * 打开动画
     */
    public void startAnimation() {

        cgAnima = ValueAnimator.ofInt(0, pathLength * 2);
        cgAnima.setInterpolator(new LinearInterpolator());
        cgAnima.setDuration(2000);
        cgAnima.setRepeatCount(ValueAnimator.INFINITE);
        cgAnima.addUpdateListener(animation -> {
            cfOffset = (int) cgAnima.getAnimatedValue();
            postInvalidate();
        });
        cgAnima.start();
    }

    public void stopAnimation() {
        if (cgAnima != null)
            cgAnima.end();
    }


    @Override
    protected void onDetachedFromWindow() {
        if (cgAnima != null)
            cgAnima.cancel();
        super.onDetachedFromWindow();
    }

    public static int dp2px(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
