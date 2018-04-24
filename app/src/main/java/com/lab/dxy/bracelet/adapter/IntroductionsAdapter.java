package com.lab.dxy.bracelet.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lab.dxy.bracelet.entity.spl.DayStepsTab;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * 项目名称：MeshLed_dxy
 * 类描述：
 * 创建人：oden
 * 创建时间：2016/8/15 11:23
 */
public class IntroductionsAdapter extends PagerAdapter {

    //界面列表
    private List<List<DayStepsTab>> allSteps;
    private List<String[]> label;
    private String[] hours;


    private LineChartData lineData;
    private Context content;
    private int max;


    public IntroductionsAdapter(List<List<DayStepsTab>> allSteps, String[] hours) {
        this.allSteps = allSteps;
        this.hours = hours;
    }

    public IntroductionsAdapter(List<String[]> label, List<List<DayStepsTab>> allSteps) {
        this.label = label;
        this.allSteps = allSteps;
    }

    public void setAllSteps(List<List<DayStepsTab>> allSteps) {
        this.allSteps = allSteps;
    }

    public void setHours(String[] hours) {
        this.hours = hours;
    }

    public void setLabel(List<String[]> label) {
        this.label = label;
    }

    public interface OnValueTouchListener {
        void onValueSelected(int i, int i1, PointValue pointValue);
    }

    private OnValueTouchListener onValueTouchListener;

    public void setOnValueTouchListener(OnValueTouchListener onValueTouchListener) {
        this.onValueTouchListener = onValueTouchListener;
    }

    @Override
    public int getCount() {
        return 30;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        content = container.getContext();
        LineChartView chartView = new LineChartView(content);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        chartView.setLayoutParams(params);
        generateInitialLineData(chartView, 29 - position);
        if (allSteps != null)
            generateLineData(chartView, ChartUtils.pickColor(), allSteps.get(29 - position));
        container.addView(chartView);

        chartView.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                onValueTouchListener.onValueSelected(i, i1, pointValue);
            }

            @Override
            public void onValueDeselected() {

            }
        });

        return chartView;
    }


    private int mChildCount = 0;

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            return POSITION_NONE;
        }

        return super.getItemPosition(object);
    }


    //判断是否由对象生成界面
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }


    /**
     * Generates initial data for line chart. At the begining all Y values are equals 0. That will change when user
     * will select value on column chart.
     */
    private void generateInitialLineData(LineChartView chartView, int position) {
        int numValues;
        if (label != null) {
            hours = label.get(position);
        }
        numValues = hours.length;
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        for (int i = 0; i < numValues; ++i) {
            values.add(new PointValue(i, 0));
            axisValues.add(new AxisValue(i).setLabel(hours[i]));
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_GREEN).setCubic(true);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        line.setCubic(false);
        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        line.setHasLabels(true);
        line.setHasLabelsOnlyForSelected(true);
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(5));
        chartView.setLineChartData(lineData);

        chartView.setValueSelectionEnabled(true);
        chartView.setSelected(true);
        // For build-up animation you have to disable viewport recalculation.
        chartView.setViewportCalculationEnabled(false);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v = new Viewport(0, max, (float) numValues, 0);
        chartView.setMaximumViewport(v);
        chartView.setCurrentViewport(v);

        chartView.setZoomEnabled(false);

    }


    public void generateLineData(LineChartView chartView, int color, List<DayStepsTab> tabs) {
        // Cancel last animation if not finished.
        chartView.cancelDataAnimation();

        max = 500;
        // Modify data targets
        Line line = lineData.getLines().get(0);// For this example there is always only one line.
        line.setColor(color);
        if (tabs == null) return;
        List<PointValue> values = line.getValues();

        for (int i = 0; i < values.size(); i++) {
            PointValue value = values.get(i);

            if (tabs.size() == 0 || tabs.get(i) == null) {
                value.setTarget(value.getX(), 0);
            } else {
                value.setTarget(value.getX(), tabs.get(i).getSteps());
                value.setLabel(tabs.get(i).getSteps() + "");
                max = tabs.get(i).getSteps() > max ? tabs.get(i).getSteps() : max;
            }
        }
        Viewport v = new Viewport(0, max, (float) values.size(), 0);

        chartView.setMaximumViewport(v);
        chartView.setCurrentViewport(v);
        chartView.setCurrentViewportWithAnimation(v);
        // Start new data animation with 300ms duration;
        chartView.startDataAnimation(300);
    }

}
