package com.lab.dxy.bracelet.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.GridView;

public class myGridView extends GridView {
    int colorLine = Color.parseColor("#F3F3F3");


    public myGridView(Context context) {
        super(context);
    }

    public myGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public myGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);//自定义设置高度,第一个是值，后面是最大的状态
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
//        try {
//            View localView1 = getChildAt(0);
//            if (localView1 == null) return;
//            int column = getWidth() / localView1.getWidth();
//            int childCount = getChildCount();
//            Paint localPaint;
//            localPaint = new Paint();
//            localPaint.setStyle(Paint.Style.STROKE);
//            localPaint.setColor(colorLine);
//            localPaint.setStrokeMiter((float) 1.0);
//            for (int i = 0; i < childCount; i++) {
//                View cellView = getChildAt(i);
//                if ((i + 1) % column == 0) {
//                    canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
//                } else if ((i + 1) > (childCount - (childCount % column))) {
//                    canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
//                } else {
//                    canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint);
//                    canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
//                }
//            }
//            if (childCount % column != 0) {
//                for (int j = 0; j < (column - childCount % column); j++) {
//                    View lastView = getChildAt(childCount - 1);
//                    canvas.drawLine(lastView.getRight() + lastView.getWidth() * j, lastView.getTop(), lastView.getRight() + lastView.getWidth() * j, lastView.getBottom(), localPaint);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void setColorLine(int colorLine) {
        this.colorLine = colorLine;
        invalidate();
    }
}