package com.lab.dxy.bracelet.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.lab.dxy.bracelet.Utils.L;

/**
 * 项目名称：TestProject
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/11/27
 */
public class HeartRateRound extends ImageView {
    private int mWidth;
    private int mHeight;
    private Context mContext;

    public HeartRateRound(Context context) {
        this(context, null);
    }

    public HeartRateRound(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartRateRound(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
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

        int left = getPaddingLeft();

        int w = mWidth - left - getPaddingRight();
        maxLines = w / (lineWidth + lineGap);
        L.d("maxLines:" + maxLines);
        setMeasuredDimension(mWidth, mHeight);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawMyLines(canvas);

    }

    int progress = 0;
    int lineWidth = dp2px(4);
    int lineHeight = dp2px(25);
    int lineGap = dp2px(5);

    public void setProgress(final int Myprogress) {
        this.progress = Myprogress;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, maxLines);
        valueAnimator.setDuration(3000);
        valueAnimator.setRepeatCount(0);
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            int i = (int) valueAnimator1.getAnimatedValue();
            progress = i;
            invalidate();
        });
        valueAnimator.start();

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                progress = Myprogress;
                invalidate();
            }
        });
    }

    int maxLines;

    private void drawMyLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(lineWidth);

        /* 设置渐变色 这个正方形的颜色是改变的 */
        Shader mShader = new LinearGradient(0, 0, mWidth, mHeight,
                new int[]{Color.YELLOW, Color.RED}, null, Shader.TileMode.REPEAT); // 一个材质,打造出一个线性梯度沿著一条线。
        paint.setShader(mShader);

        for (int i = 0; i < maxLines; i++) {
            int x = (lineWidth + lineGap) * i;
            canvas.drawLine(x, mHeight - lineHeight, x, mHeight, paint);
        }
        int X = (lineWidth + lineGap) * progress;
        canvas.drawLine(X, mHeight - lineHeight - dp2px(10), X, mHeight, paint);

    }

    public static int dp2px(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

}
