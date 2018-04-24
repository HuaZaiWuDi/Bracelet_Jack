package com.lab.dxy.bracelet.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * 项目名称：Ali_Sophix
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/25
 */
public class RoundDisPlayView extends View {

    private int mWidth;
    private int mHeight;
    private Paint paint1;
    private Paint paint2;
    private Paint paint_4;
    private String unit = "";//单位
    private String hitText = "";//title下面的文字
    private int BackgroundColor = Color.parseColor("#9337E4");
    private int radius;//最近里面的圈的半径
    private int lineWidth;//线的宽度
    private int ProgressWidth;//进度的宽度
    private int w;//圈的间隔
    private int startAngle1 = 0;
    private int startAngle2 = 0;
    private int startAngle3 = 0;
    private int sweepAngle1 = 0;
    private int sweepAngle2 = 0;
    private int sweepAngle3 = 0;
    private String currentNum = 9527 + "";//中间的文字
    private int[] linesColor = {Color.parseColor("#F4479D"), Color.parseColor("#43B7FD"), Color.parseColor("#F5CC36")};

    public RoundDisPlayView(Context context) {
        this(context, null);
    }

    public RoundDisPlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundDisPlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RoundDisPlayView setCentreText(String currentNum, String unit, String hitText) {
        this.currentNum = currentNum;
        this.unit = unit;
        this.hitText = hitText;
        return this;
    }

    public RoundDisPlayView setLinesColor(@ColorInt int[] linesColor) {
        if (linesColor.length == 3)
            this.linesColor = linesColor;
        return this;
    }


    public RoundDisPlayView setSweepAngle(@IntRange(from = 0, to = 360) int[] sweepAngles) {
        sweepAngle1 = sweepAngles[0];
        sweepAngle2 = sweepAngles[1];
        sweepAngle3 = sweepAngles[2];
        return this;
    }

    public RoundDisPlayView setBackground(@ColorInt int BackgroundColor) {
        this.BackgroundColor = BackgroundColor;
        return this;
    }

    public void submit() {
        postInvalidate();
    }


    private void init() {

        sweepAngle1 = 270;
        sweepAngle2 = 270;
        sweepAngle3 = 270;

        lineWidth = dp2px(1);
        ProgressWidth = dp2px(7);
        w = dp2px(13);

        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setDither(true);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setColor(Color.parseColor("#17F085"));
        paint_4 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStrokeCap(Paint.Cap.ROUND);//设置为线条圆头
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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制背景
        canvas.drawColor(BackgroundColor);

        radius = getMeasuredWidth() / 4; //不要在构造方法里初始化，那时还没测量宽高
        canvas.save();
        canvas.translate(mWidth / 2, (mWidth) / 2);
        //画最里面的圈
        drawRound1(canvas);
        //画中间的圈
        drawRound2(canvas);
        //画最外的圈
        drawRound3(canvas);
        //画中间的文字
        drawCentreText(canvas);
        canvas.restore();
    }

    private void drawCentreText(Canvas canvas) {
        canvas.save();
        paint_4.setStyle(Paint.Style.FILL);
        paint_4.setTextSize(radius / 2);
        paint_4.setColor(0xffffffff);

        canvas.drawText(currentNum + "", -paint_4.measureText(currentNum + "") / 2, 0, paint_4);

        float v = paint_4.measureText(currentNum + "");
        paint_4.setTextSize(radius / 8);

        Rect r = new Rect();
        canvas.drawText(unit, v / 2, 0, paint_4);

        paint_4.getTextBounds(hitText, 0, hitText.length(), r);
        canvas.drawText(hitText, -r.width() / 2, r.height() + 50, paint_4);
        canvas.restore();
    }


    private void drawRound3(Canvas canvas) {
        canvas.save();
        //画最外层圈
        paint1.setStrokeWidth(lineWidth);
        RectF rectf3 = new RectF(-radius - w * 2, -radius - w * 2, radius + w * 2, radius + w * 2);
        canvas.drawArc(rectf3, 0, 360, false, paint1);

        //画最外的的进度
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(ProgressWidth);

        paint2.setColor(linesColor[0]);

        canvas.drawArc(rectf3, startAngle3, sweepAngle1, false, paint2);
        canvas.restore();
    }

    private void drawRound2(Canvas canvas) {
        canvas.save();
        //画中间的圈
        paint1.setStrokeWidth(lineWidth);
        RectF rectf2 = new RectF(-radius - w, -radius - w, radius + w, radius + w);
        canvas.drawArc(rectf2, 0, 360, false, paint1);

        //画中间圈的进度
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(ProgressWidth);

        paint2.setColor(linesColor[1]);

        canvas.drawArc(rectf2, startAngle2, sweepAngle2, false, paint2);
        canvas.restore();

    }

    private void drawRound1(Canvas canvas) {
        canvas.save();
        //最里面的圈
        paint1.setAlpha(0x40);
        paint1.setStrokeWidth(lineWidth);
        RectF rectf = new RectF(-radius, -radius, radius, radius);
        canvas.drawArc(rectf, 0, 360, false, paint1);

        //最里面的进度
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(ProgressWidth);

        paint2.setColor(linesColor[2]);

        canvas.drawArc(rectf, startAngle1, sweepAngle3, false, paint2);
        canvas.restore();
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus)
            startAnimation();
    }


    ValueAnimator cgAnima1;
    ValueAnimator cgAnima3;

    public void startAnimation() {
        if (cgAnima1 == null || cgAnima3 == null) {
            cgAnima1 = ValueAnimator.ofInt(0, 360);
            cgAnima1.setInterpolator(new AccelerateDecelerateInterpolator());
            cgAnima1.setDuration(2000);
            cgAnima1.setRepeatCount(ValueAnimator.INFINITE);
            cgAnima1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    startAngle1 = (int) cgAnima1.getAnimatedValue();
                    startAngle2 = 360 - (int) cgAnima1.getAnimatedValue();
                    RoundDisPlayView.this.postInvalidate();
                }
            });
            cgAnima3 = ValueAnimator.ofInt(180, 360, 180);
            cgAnima3.setInterpolator(new AccelerateDecelerateInterpolator());
            cgAnima3.setDuration(2000);
            cgAnima3.setRepeatCount(ValueAnimator.INFINITE);
            cgAnima3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    startAngle3 = (int) cgAnima3.getAnimatedValue();
                    RoundDisPlayView.this.postInvalidate();
                }
            });
        }
        cgAnima3.start();
        cgAnima1.start();

    }


    public void stopAnimation() {
        if (cgAnima1 != null && cgAnima3 != null) {
            cgAnima1.end();
            cgAnima3.end();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    public static int dp2px(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
}
