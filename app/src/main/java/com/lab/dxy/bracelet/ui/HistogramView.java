package com.lab.dxy.bracelet.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;

import java.util.List;

public class HistogramView extends View {

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
    private int count;
    private boolean isDown = false;//是否点击
    private String sleep;//睡眠质量
    private String noData;

    private float mTouchSlop;//系统判定的最小

    private Context mContext;

    private List<Bar> mBarLists;
    private String[] text;//底部的文字

    public static int[] Colors = new int[]{Color.parseColor("#ff820e"), Color.parseColor("#777CC7"), Color.parseColor("#3F468D")};

    private OnClickItemListener onClickItemListener;

    private interface OnClickItemListener {
        void onClickItem(int i);
    }

    private void setonClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }


    public HistogramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData(context);
    }

    public HistogramView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initData(context);
    }

    public HistogramView(Context context) {
        this(context, null);
        initData(context);
    }

    private void initData(Context context) {

        //系统认为发生滑动的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRect = new Rect();

    }

    public void setBarLists(List<Bar> barLists) {
        mBarLists = barLists;
        if (mBarLists.size() > 0) {
            colcrDefault = mBarLists.get(0).color;
            position = 0;
            isDown = false;
        }
        count = mBarLists.size();
        noData = mContext.getString(R.string.noSleepData);
        postInvalidate();
    }

    public void setText(String[] text) {
        if (text.length == 0 || text == null) {
            this.text = new String[]{"00:00", "00:00"};
        } else {
            this.text = text;
        }
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
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
        int fontHeight = (int) Math.ceil(fontMetricsInt.bottom - fontMetricsInt.top);


        int maxWidth = mRight - mLeft;
        int maxHeight = mTop - mBottom;

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        double proportion = 0;
        int a = 0;

        if (count > 0) {
            // 逐个画bar
            for (int i = 0; i < count; i++) {
                Bar bar = mBarLists.get(i);

                a = (int) (maxWidth * proportion / 100);

                proportion = proportion + bar.Proportion;

                // 画 bar 图形
                left = mLeft + a;
                right = mLeft + (int) (maxWidth * proportion / 100);
                bottom = mBottom - fontHeight;
                top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * bar.ratio);
                mRect.set(left, top, right, bottom);
                mPaint.setColor(bar.color);
                canvas.drawRect(mRect, mPaint);

            }

            // 画线
            mPaint.setColor(Color.parseColor("#BAB7BF"));
            canvas.drawLine(mLeft, mBottom - fontHeight, mRight, mBottom - fontHeight, mPaint);

            //画底部的文字
            mPaint.setColor(Color.parseColor("#BAB7BF"));
            mPaint.setTextSize(sp2Px(mContext, 15));
            if (text != null) {
                for (int i = 0; i < text.length; i++) {
                    canvas.drawText(text[i], (mWidth / text.length) * (i + 0.5f), mBottom, mPaint);
                }
            }

            //画顶部的图例
            mPaint.setTextSize(sp2Px(mContext, 25));
            mPaint.setColor(Color.GRAY);
            top = bottom - (int) ((mBottom - mTop - fontHeight * 2) * 0.9f);
            if (isDown) {
                String text = mBarLists.get(position).typeStr + "\t" + mBarLists.get(position).time;
                canvas.drawText(text, maxWidth * 0.5f, top, mPaint);
            } else {
                canvas.drawText(sleep, maxWidth * 0.5f, top, mPaint);
            }
        }
        super.onDraw(canvas);
    }

    int colcrDefault = 0;
    int position = 0;
    int left = 0;
    int right = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mBarLists.size() == 0) return true;
        //获取屏幕上点击的坐标
        float x = event.getX();
        float y = event.getY();
        int maxWidth = mRight - mLeft;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                L.d("移动：" + position);

                mBarLists.get(position).color = colcrDefault;
                invalidate();

                int sum = 0;
                for (int i = 0; i < mBarLists.size(); i++) {
                    Bar bar = mBarLists.get(i);
                    sum += (int) (maxWidth * bar.Proportion / 100);
                    int a = (int) (maxWidth * bar.Proportion / 100);

                    left = mLeft + sum - a;
                    right = mLeft + sum;
                    if (x <= right && x >= left) {
                        mBarLists.get(position).color = colcrDefault;
                        colcrDefault = mBarLists.get(i).color;
                        position = i;
                        mBarLists.get(i).color = Color.LTGRAY;
                        invalidate();
                    }
                }

                break;
            case MotionEvent.ACTION_DOWN:
                isDown = true;

                int sum2 = 0;
                for (int i = 0; i < mBarLists.size(); i++) {
                    Bar bar = mBarLists.get(i);
                    sum2 += (int) (maxWidth * bar.Proportion / 100);
                    int a = (int) (maxWidth * bar.Proportion / 100);

                    left = mLeft + sum2 - a;
                    right = mLeft + sum2;
                    if (x <= right && x >= left) {
                        mBarLists.get(position).color = colcrDefault;
                        colcrDefault = mBarLists.get(i).color;
                        position = i;
                        mBarLists.get(i).color = Color.LTGRAY;
                        invalidate();
                    }
                }
                L.d("按下：" + position);
                break;
            case MotionEvent.ACTION_UP:
                L.d("抬起：" + position);
                mBarLists.get(position).color = colcrDefault;
                invalidate();
                isDown = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                L.d("关闭：" + position);
                mBarLists.get(position).color = colcrDefault;
                invalidate();
                isDown = false;
                break;
        }
        return true;
    }


    public static class Bar {
        public float ratio = 0.8f;
        public double Proportion;
        public int color;
        public String time;
        public String typeStr;

        public Bar(double Proportion, int type, String time) {
            this.Proportion = Proportion;
            if (type == 0) {
                this.color = Colors[0];
                typeStr = "清醒";
                ratio = 0.5f;
            } else if (type == 1) {
                this.color = Colors[1];
                typeStr = "浅睡眠";
                ratio = 0.7f;
            } else if (type == 2) {
                this.color = Colors[2];
                typeStr = "深睡眠";
            }
            this.time = time;
        }
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


    //一些工具方法
    protected int dp2px(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics());
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
}