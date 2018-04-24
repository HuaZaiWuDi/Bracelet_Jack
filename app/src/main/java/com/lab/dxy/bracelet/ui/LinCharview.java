package com.lab.dxy.bracelet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/6/21
 */
public class LinCharview extends View {
    private Paint mPaint;
    private Rect mRect;

    private int mWidth;
    private int mHeight;

    private int mPaddingStart;
    private int mPaddingEnd;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    private int fontHeight;

    private Context mContext;

    private List<LinCharview.Item> mBarLists;

    private OnClickItemListener onClickItemListener;

    public interface OnClickItemListener {
        void onClickItem(int i);
    }

    public void setonClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }


    private void initData() {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRect = new Rect();

    }

    public void setBarLists(List<LinCharview.Item> barLists) {
        mBarLists = barLists;
        postInvalidate();
    }


    public LinCharview(Context context) {
        this(context, null);
        initData();
    }

    public LinCharview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initData();
    }

    public LinCharview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = getSizeFromMeasureSpec(widthMeasureSpec, 480);
        mHeight = getSizeFromMeasureSpec(heightMeasureSpec, 480);

        mPaddingStart = getPaddingStart();
        mPaddingEnd = getPaddingEnd();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();

        mLeft = mPaddingStart;
        mTop = mPaddingTop;
        mRight = mWidth - mPaddingEnd;
        mBottom = mHeight - mPaddingBottom;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 设置底部文字属性
        mPaint.setTextSize(sp2Px(mContext, 11));
        mPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        fontHeight = (int) Math.ceil(fontMetricsInt.bottom - fontMetricsInt.top);

        int N = mBarLists.size();
        int maxWidth = mRight - mLeft;
//        LL.d("maxWidth:" + maxWidth + "N:" + N);

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        int UNIT_WIDTH = (mRight - mLeft) / (2 * N + 1);

        // 逐个画bar
        for (int i = 0; i < N; i++) {
            Item item = mBarLists.get(i);

            // 画 bar 底部文字
            left = (int) (mLeft + (i * 2 + 0.5f) * UNIT_WIDTH);
            right = left + UNIT_WIDTH * 2;
            top = mBottom - fontHeight;
            bottom = mBottom;
            mRect.set(left, top, right, bottom);
            int baseLine = (mRect.top + mRect.bottom - fontMetricsInt.top - fontMetricsInt.bottom) / 2;
            mPaint.setColor(Color.BLACK);
            canvas.drawText(item.bootomText, mRect.centerX(), baseLine, mPaint);

            // 画 bar 下面图形
            left = mLeft + (i * 2 + 1) * UNIT_WIDTH;
            right = left + UNIT_WIDTH;
            bottom = mBottom - fontHeight;
            top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * item.ratio[0]);
            mRect.set(left, top, right, bottom);
            mPaint.setColor(item.color[0]);
            canvas.drawRect(mRect, mPaint);


            // 画 bar 上面图形
            left = mLeft + (i * 2 + 1) * UNIT_WIDTH;
            right = left + UNIT_WIDTH;
            bottom = mBottom - fontHeight - (int) ((mBottom - mTop - fontHeight * 2) * item.ratio[0]);
            top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * item.ratio[1]);
            mRect.set(left, top, right, bottom);
            mPaint.setColor(item.color[1]);
            canvas.drawRect(mRect, mPaint);

        }

//        //画顶部的图例
//        mPaint.setTextSize(sp2Px(mContext, 15));
//        String[] str = new String[]{"深睡", "浅睡"};
//        int[] strColor = new int[]{Color.GRAY, Color.WHITE, Color.YELLOW};
//        for (int i = 0; i < str.length; i++) {
//            mPaint.setColor(strColor[i]);
//            int legendLength = (int) mPaint.measureText(str[i]);
//            canvas.drawText(str[i], maxWidth * 0.8f + legendLength * (i + 0.5f), top - 20, mPaint);
//        }

        // 画线
        mPaint.setColor(Color.WHITE);
        canvas.drawLine(mLeft, mBottom - fontHeight, mRight, mBottom - fontHeight, mPaint);
        // canvas.drawLine(mLeft, mTop + fontHeight, mRight, mTop + fontHeight, mPaint);

        super.onDraw(canvas);
    }


    public static class Item {
        public float[] ratio = new float[2];
        public int[] color = new int[]{Color.WHITE, Color.GRAY};
        public String bootomText;


        public Item(float[] Duration, String bootomText) {
            ratio = Duration;
            this.bootomText = bootomText;
        }
    }

    private int position = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取屏幕上点击的坐标
        float x = event.getX();
        float y = event.getY();
        int UNIT_WIDTH = (mRight - mLeft) / (2 * mBarLists.size() + 1);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //如果坐标在柱状图区域内，则将点击的文字改颜色
                for (int i = 0; i < mBarLists.size(); i++) {
                    int left = mLeft + (i * 2 + 1) * UNIT_WIDTH;
                    int right = left + UNIT_WIDTH * 2;

                    int bottom = mBottom - fontHeight;
                    int top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * (mBarLists.get(i).ratio[0] + mBarLists.get(i).ratio[1]));
                    if (left > x || x > right) {
                        if (top > y || y > bottom) {
                            mBarLists.get(position).color = new int[]{Color.WHITE, Color.GRAY};
                            postInvalidate();
                        }
                    }
                }
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mBarLists.size(); i++) {
                    int left = mLeft + (i * 2 + 1) * UNIT_WIDTH;
                    int right = left + UNIT_WIDTH * 2;

                    int bottom = mBottom - fontHeight;
                    int top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * (mBarLists.get(i).ratio[0] + mBarLists.get(i).ratio[1]));
                    if (left <= x && x <= right) {
                        if (top <= y && y <= bottom)
                            if (onClickItemListener != null) {
                                position = i;
                                onClickItemListener.onClickItem(i);
                                mBarLists.get(i).color = new int[]{Color.LTGRAY, Color.LTGRAY};
                                postInvalidate();
                            }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //点击抬起后，回复初始位置。
                mBarLists.get(position).color = new int[]{Color.WHITE, Color.GRAY};
                postInvalidate();
        }
        return true;
    }

    // 工具类
    public static int getSizeFromMeasureSpec(int measureSpec, int defaultSize) {
        int result = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = defaultSize;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(defaultSize, size);
            }
        }
        return result;
    }

    public static float sp2Px(Context context, float sp) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        float px = metrics.scaledDensity;
        return sp * px;
    }

    public static float bigDecimal(float f) {
        BigDecimal b = new BigDecimal(f);
        float f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

}
