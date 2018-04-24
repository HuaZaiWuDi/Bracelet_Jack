package com.lab.dxy.bracelet.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxTextUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
import com.lab.dxy.bracelet.entity.SleepItem;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.HistogramView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.view.ColumnChartView;

import static android.view.View.VISIBLE;
import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.Utils.Utils.getDayOfWeekEvery;
import static com.lab.dxy.bracelet.Utils.Utils.getEveryDayOfMonth;
import static com.lab.dxy.bracelet.Utils.Utils.getFirstDayOfWeek;
import static com.lab.dxy.bracelet.Utils.Utils.getLastDayOfWeek;
import static com.lab.dxy.bracelet.activity.StepsCountsActivity.TYPE_DAY;
import static com.lab.dxy.bracelet.activity.StepsCountsActivity.TYPE_MONTH;
import static com.lab.dxy.bracelet.activity.StepsCountsActivity.TYPE_WEEK;
import static com.lab.dxy.bracelet.fragment.exercisefragment.DATA_INIT;
import static com.lab.dxy.bracelet.ui.LinCharview.bigDecimal;


/**
 * Created by 华 on 2017/5/4.
 */
@EFragment(R.layout.fragment_find)
public class findFragment extends BaseFragment {
    String[] titles;
    String[] title;

    private static int DATETYPE = 0;
    public static String fromatDate = "yyyy-MM-dd";

    ColumnChartData data;
    List<Column> columns = new ArrayList<>();
    List<HistogramView.Bar> mBarLists = new ArrayList<>();
    List<SleepItem> sleepItems = new ArrayList<>();
    Calendar calendar;
    String Today = Utils.setFormat(System.currentTimeMillis(), fromatDate, Utils.DATE);

    Handler handler = new Handler();


    @ViewById
    TextView text_bom;

    @ViewById
    HistogramView mHistogramView;
    @ViewById
    ColumnChartView mColumnChartView;
    @ViewById
    TextView Title;
    @ViewById
    ImageView bg_img;
    @ViewById
    ImageView Right;
    @ViewById
    LinearLayout loading;
    @ViewById
    LinearLayout noData;
    @ViewById
    ImageView Left;
    @ViewById
    ScrollView mScrollView;
    @ViewById
    TextView text_data;
    @ViewById
    SegmentTabLayout mSlidingTabLayout;
    @ViewById
    TextView text_1;
    @ViewById
    TextView text_2;
    @ViewById
    TextView text_3;
    @ViewById
    TextView textData_1;
    @ViewById
    TextView textData_2;
    @ViewById
    TextView textData_3;


    @Pref
    MyPrefs_ myPrefs;


    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime() {
        if (isVisible()) {
            String date = Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE);
            showLoading();
            setSleepData(date);
        }
    }


    @Click
    void Right() {

        if (calendar.getTime().getTime() > (System.currentTimeMillis() - 24 * 60 * 60 * 1000)) {
            L.d("超过今天");
            Right.setFocusable(false);
        } else if (DATETYPE == 0) {
            calendar.add(Calendar.DATE, +1);
            String date = Utils.setFormat(calendar.getTime().getTime(), fromatDate, Utils.DATE);
            Right.setFocusable(true);

            if (date.equals(Today)) {
                Title.setText(getString(R.string.Today));
            } else {
                Title.setText(date.substring(5));
            }

            setSleepData(date);

        } else if (DATETYPE == 1) {
            loading.setVisibility(View.INVISIBLE);
            Right.setFocusable(true);
            calendar.add(Calendar.DATE, +7);
            SimpleDateFormat sdf = new SimpleDateFormat(fromatDate, Locale.getDefault());
            String first = sdf.format(getFirstDayOfWeek(calendar.getTime()));
            String last = sdf.format(getLastDayOfWeek(calendar.getTime()));

            Title.setText(first.substring(5) + "/" + last.substring(5));
            setSleepWeek(calendar.getTime());
        } else if (DATETYPE == 2) {
            loading.setVisibility(View.INVISIBLE);
            Right.setFocusable(true);
            calendar.add(Calendar.MONTH, +1);
            int y = calendar.get(Calendar.YEAR);
            int w = calendar.get(Calendar.MONTH) + 1;
            if ((w) < 10) {
                Title.setText(y + "-0" + w);
            } else {
                Title.setText(y + "-" + w);
            }
            setSleepMonth(calendar.getTime());
        }
    }


    @Click
    void Left() {
        if (DATETYPE == 0) {
            calendar.add(Calendar.DATE, -1);
            String date = Utils.setFormat(calendar.getTime().getTime(), fromatDate, Utils.DATE);
            if (date.equals(Today)) {
                Title.setText(getString(R.string.Today));
            } else {
                Title.setText(date.substring(5));
            }
            setSleepData(date);
        } else if (DATETYPE == 1) {
            loading.setVisibility(View.INVISIBLE);
            calendar.add(Calendar.DATE, -7);
            SimpleDateFormat sdf = new SimpleDateFormat(fromatDate, Locale.getDefault());
            String first = sdf.format(getFirstDayOfWeek(calendar.getTime()));
            String last = sdf.format(getLastDayOfWeek(calendar.getTime()));

            Title.setText(first.substring(5) + "/" + last.substring(5));
            setSleepWeek(calendar.getTime());
        } else if (DATETYPE == 2) {
            loading.setVisibility(View.INVISIBLE);
            calendar.add(Calendar.MONTH, -1);
            int y = calendar.get(Calendar.YEAR);
            int w = calendar.get(Calendar.MONTH) + 1;
            if ((w) < 10) {
                Title.setText(y + "-0" + w);
            } else {
                Title.setText(y + "-" + w);
            }
            setSleepMonth(calendar.getTime());
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            L.d("隐藏：findfragment");
        } else {
            L.d("显示：findfragment");
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @AfterViews
    void initView() {
        titles = new String[]{getString(R.string.day), getString(R.string.week), getString(R.string.month)};
        title = getResources().getStringArray(R.array.sleep);
        serDefault();
        text_bom.setText(getString(R.string.sleepMessage));

//        SleepDataTab2.deleteAll();

        mColumnChartView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //允许ScrollView截断点击事件，ScrollView可滑动
                mScrollView.requestDisallowInterceptTouchEvent(false);
            } else {
                //不允许ScrollView截断点击事件，点击事件由子View处理
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
            return false;
        });
        String date = Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE);
        setSleepData(date);

    }


    @UiThread
    public void showLoading() {
        if (loading != null) {
            setFouce(false);
            if (DATETYPE == 0)
                loading.setVisibility(View.VISIBLE);
            noData.setVisibility(View.GONE);
            handler.removeCallbacks(loadingRunnable);
            handler.postDelayed(loadingRunnable, 15 * 1000);
        }
    }

    //加载超时
    Runnable loadingRunnable = new Runnable() {
        @Override
        public void run() {
            setFouce(true);
            loading.setVisibility(View.INVISIBLE);
            if (DATETYPE == 0)
                noData.setVisibility(VISIBLE);
        }
    };

    @UiThread
    public void dismissLoading(boolean hasData) {
        if (hasData) {
            setFouce(true);
            loading.setVisibility(View.INVISIBLE);
            handler.removeCallbacks(loadingRunnable);
            noData.setVisibility(View.GONE);
        } else {
            setFouce(true);
            loading.setVisibility(View.INVISIBLE);
            handler.removeCallbacks(loadingRunnable);
            if (DATETYPE == 0)
                noData.setVisibility(VISIBLE);
        }
    }


    private void setSleepData(String date) {
        L.d("date:" + date);
        List<SleepDataTab2> sleepWeek = SleepDataTab2.getSleepDate(date);


        if (sheckTime(date, myPrefs.sleepData().get())) {
            text_data.setText(getString(R.string.sleepDataWait));
            dismissLoading(false);
            synStepsData(date);
            L.d("时间未到");
            return;
        } else {
            L.d("时间已到");
            text_data.setText(getString(R.string.noSleepData));
        }


        L.d("睡眠数据：" + date + "---" + sleepWeek.size());
        if (BleService.isConnected) {
            if (sleepWeek.size() == 0) {
                L.d("同步指定日期的睡眠数据");
                showLoading();
                readSleepData(date);
            } else if (sleepWeek.size() == 1) {
                text_data.setText(sleepWeek.get(0).getTime());
                dismissLoading(false);
            } else {
                dismissLoading(true);
            }
        } else {
            if (sleepWeek.size() < 2) {
                text_data.setText(getString(R.string.barDisConnect));
                dismissLoading(false);
            } else {
                text_data.setText(getString(R.string.noSleepData));
                dismissLoading(true);
            }
        }
        synStepsData(date);
    }


    public static boolean sheckTime(String date, String dfDate) {
        if (date.equals(Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE))) {//今天

            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);

            String[] split = dfDate.split("-");
            L.d("现在时间：" + hour + "-----:" + split[1]);
            if (hour < Integer.parseInt(split[1].trim())) {
                return true;
            }
        }
        return false;
    }


    private void synStepsData(String date) {
        List<SleepDataTab2> sleepWeek = SleepDataTab2.getSleepDate(date);
        mBarLists.clear();
        sleepItems.clear();


        int TotalTime = 0;
        int awake = 0;
        int restless = 0;
        int asleep = 0;

        for (int i = 0; i < sleepWeek.size(); i++) {
            TotalTime += sleepWeek.get(i).getDuration();

            if (sleepWeek.get(i).getType() == 0) {
                awake += sleepWeek.get(i).getDuration();
            }
            if (sleepWeek.get(i).getType() == 1) {
                restless += sleepWeek.get(i).getDuration();
            }
            if (sleepWeek.get(i).getType() == 2) {
                asleep += sleepWeek.get(i).getDuration();
            }
        }


        String sleepQ = checkSleepQ(asleep, awake);
        sleepItems.add(new SleepItem(awake, restless, asleep, sleepQ));


        mHistogramView.setSleep(getString(R.string.sleepQuality, sleepQ));
        if (sleepWeek.size() > 1) {
            try {
                int type = sleepWeek.get(0).getType();
                int time = sleepWeek.get(0).getDuration();
                String startTime = sleepWeek.get(0).getTime().split("-")[0];
                String endTime = sleepWeek.get(0).getTime().split("-")[1];
                for (int i = 1; i < sleepWeek.size(); i++) {
                    if (type == sleepWeek.get(i).getType()) {//类型相同
                        time = time + sleepWeek.get(i).getDuration();
                        endTime = sleepWeek.get(i).getTime().split("-")[1];
                    } else {
                        double Proportion = ((double) (time * 100)) / (double) TotalTime;
                        HistogramView.Bar bar = new HistogramView.Bar(Proportion, type, startTime + "-" + endTime);
                        mBarLists.add(bar);
                        time = sleepWeek.get(i).getDuration();
                        type = sleepWeek.get(i).getType();
                        startTime = sleepWeek.get(i).getTime().split("-")[0];
                        endTime = sleepWeek.get(i).getTime().split("-")[1];
                    }

                    int checkNoWear = (int) ((float) TotalTime * 0.8f);
                    if (time >= checkNoWear || time >= 5 * 60) {
                        L.d("相同状态下判断为未佩戴：" + checkNoWear / 60);
                        text_data.setText(getString(R.string.NoWearBra));
                        dismissLoading(false);

                        SleepDataTab2.deleteByDate(date);
                        SleepDataTab2 sleepDataTab = new SleepDataTab2(date, 0, 0, getString(R.string.NoWearBra));
                        sleepDataTab.save();
                        mBarLists.clear();
                        mHistogramView.setBarLists(mBarLists);
                        onClickItem(-1);
                        return;
                    }
                }
                //最后一包
                double Proportion = ((double) (time * 100)) / (double) TotalTime;
                HistogramView.Bar bar = new HistogramView.Bar(Proportion, type, startTime + "-" + endTime);
                mBarLists.add(bar);

                String StartTime = sleepWeek.get(0).getTime().split("-")[0];
                String EndTime = sleepWeek.get(sleepWeek.size() - 1).getTime().split("-")[1];

                int start = Integer.parseInt(StartTime.split(":")[0].trim());
                int end = Integer.parseInt(EndTime.split(":")[0].trim());

                String[] strings = new String[5];
                strings[0] = StartTime;
                strings[4] = EndTime;

                L.d("时间：" + start + "----:" + end);
                double sum;
                if (start > 8) {
                    sum = 24 - start + end;
                } else {
                    sum = end - start;
                }

                for (int j = 1; j < 4; j++) {
                    int i1 = start + exercisefragment.fromatDouble(sum / 4) * j;
                    if (i1 >= 24) i1 = i1 - 24;
                    strings[j] = i1 + " : 00";
                }

                mHistogramView.setText(strings);

            } catch (Exception e) {
                e.printStackTrace();
                L.d("运算异常");
            }
        }

        mHistogramView.setBarLists(mBarLists);
        onClickItem(0);
    }


    public String checkSleepQ(int asleep, int awake) {
        String sleepStr = getString(R.string.sleepStatusBad);

        if (asleep == 0 && awake == 0) {
            return sleepStr;
        }
        if (asleep < 60 && awake >= 60) {
            sleepStr = getString(R.string.sleepStatusBad);
        } else if (asleep < 60 && awake < 60) {
            sleepStr = getString(R.string.sleepStatusFine);
        } else if (60 <= asleep && asleep < 120 && awake >= 120) {
            sleepStr = getString(R.string.sleepStatusBad);
        } else if (60 <= asleep && asleep <= 120 && awake < 120 && awake >= 60) {
            sleepStr = getString(R.string.sleepStatusFine);
        } else if (asleep > 120) {
            sleepStr = getString(R.string.sleepStatusGood);
        } else if (60 <= asleep && asleep < 120 && awake < 60) {
            sleepStr = getString(R.string.sleepStatusGood);
        }

        return sleepStr;
    }


    private void setSleepWeek(Date date) {
        mColumnChartView.cancelDataAnimation();
        columns.clear();
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        sleepItems.clear();
        for (int i = 0; i < 7; i++) {
            String day = Utils.setFormat(getDayOfWeekEvery(date, i).getTime(), fromatDate, Utils.DATE);

            onSaveSleepData(day);
            axisValues.add(new AxisValue(i).setLabel(day.substring(5)));

        }
        data = new ColumnChartData(columns);
        // Set stacked flag.叠加
        data.setStacked(true);
        data.setAxisXBottom(new Axis(axisValues).setHasLines(true).setTextColor(Color.GRAY));
        mColumnChartView.setColumnChartData(data);
        mColumnChartView.startDataAnimation(500);
        mColumnChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                onClickItem(i);
            }

            @Override
            public void onValueDeselected() {

            }
        });
    }

    private void onClickItem(int i) {
        L.d("点击的下标：" + i);
        RxTextUtils.setTextView(getActivity(), textData_1, i < 0 ? 0 : sleepItems.get(i).getAsleep(), getResources().getColor(R.color.flyBlue));
        RxTextUtils.setTextView(getActivity(), textData_2, i < 0 ? 0 : sleepItems.get(i).getRestless(), getResources().getColor(R.color.flyBlue));
        RxTextUtils.setTextView(getActivity(), textData_3, i < 0 ? 0 : sleepItems.get(i).getAwake(), getResources().getColor(R.color.flyBlue));
    }

    private void onSaveSleepData(String day) {
        List<SleepDataTab2> sleepWeek = SleepDataTab2.getSleepDate(day);

        int restless = 0;
        int asleep = 0;
        int awake = 0;
        for (int j = 0; j < sleepWeek.size(); j++) {
            if (sleepWeek.get(j).getType() == 1) {
                restless += sleepWeek.get(j).getDuration();
            }
            if (sleepWeek.get(j).getType() == 2) {
                asleep += sleepWeek.get(j).getDuration();
            }
            if (sleepWeek.get(j).getType() == 0) {
                awake += sleepWeek.get(j).getDuration();
            }
        }

        float[] ratio = new float[2];
        int sum = restless + asleep;
        if (sum != 0) {
            ratio[0] = bigDecimal((float) restless / (float) sum);
            ratio[1] = bigDecimal((float) asleep / (float) sum);
        } else {
            ratio[0] = 0;
            ratio[1] = 0;
        }
        SleepItem item1 = new SleepItem(awake, restless, asleep, day);
        sleepItems.add(item1);
        List<SubcolumnValue> values = new ArrayList<>();
        values.add(new SubcolumnValue(ratio[0], Color.parseColor("#777CC7")).setLabel(restless + ""));
        values.add(new SubcolumnValue(ratio[1], Color.parseColor("#3F468D")).setLabel(asleep + ""));

        Column column = new Column(values);
        column.setHasLabels(true);//标签
        column.setHasLabelsOnlyForSelected(true);
        columns.add(column);
    }


    private void setSleepMonth(Date date) {
        columns.clear();
        sleepItems.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int daysCountOfMonth = calendar.getActualMaximum(Calendar.DATE);//获取指定年份中指定月份有几天
        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        for (int i = 0; i < daysCountOfMonth; i++) {
            String day = Utils.setFormat(getEveryDayOfMonth(date, i).getTime() + 24 * 3600 * 1000, fromatDate, Utils.DATE);
            onSaveSleepData(day);
            axisValues.add(new AxisValue(i).setLabel(day.substring(8)));
        }
        data = new ColumnChartData(columns);
        // Set stacked flag.叠加
        data.setStacked(true);
        data.setAxisXBottom(new Axis(axisValues).setHasLines(true).setTextColor(Color.GRAY));
        mColumnChartView.setColumnChartData(data);
        mColumnChartView.setValueSelectionEnabled(true);//选中突出
        mColumnChartView.setZoomType(ZoomType.HORIZONTAL);//缩放类型
        mColumnChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {
                onClickItem(i);
            }

            @Override
            public void onValueDeselected() {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void readSleepData(final String date) {
        if (DATA_INIT) {
            byte[] dateByte;
            //"yyyy-MM-dd";
            String[] split = date.split("-");
            if (split.length != 3) return;
            int year = Integer.parseInt(split[0]) - 2000;
            int month = Integer.parseInt(split[1]);
            int day = Integer.parseInt(split[2]);

            dateByte = new byte[]{(byte) year, (byte) month, (byte) day, (byte) year, (byte) month, (byte) day};

            MyBle.getInstance().synSleepData(dateByte, new BleCommandListener() {
                @Override
                public void success() {
                    dismissLoading(true);
                    synStepsData(date);
                }

                @Override
                public void fail() {
                    dismissLoading(false);
                    SleepDataTab2 sleepDataTab = new SleepDataTab2(date, 0, 0, "没有睡眠数据");
                    sleepDataTab.save();
                }
            });
        }
    }


    public void setFouce(boolean b) {
        if (b) {
            Right.setVisibility(View.VISIBLE);
            Left.setVisibility(VISIBLE);
        } else {
            Right.setVisibility(View.INVISIBLE);
            Left.setVisibility(View.INVISIBLE);
        }
    }


    private void serDefault() {
        calendar = Calendar.getInstance();
        text_1.setText(title[0]);
        text_2.setText(title[1]);
        text_3.setText(title[2]);
        //选择日月周
        mSlidingTabLayout.setTabData(titles);

        mSlidingTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                calendar = Calendar.getInstance();
                noData.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                DATETYPE = position;
                switch (position) {
                    case TYPE_DAY:
                        Title.setText(getString(R.string.Today));
                        mHistogramView.setVisibility(View.VISIBLE);
                        mColumnChartView.setVisibility(View.GONE);
                        String date = Utils.setFormat(calendar.getTime().getTime(), fromatDate, Utils.DATE);
                        setSleepData(date);

                        break;
                    case TYPE_WEEK:
                        mHistogramView.setVisibility(View.GONE);
                        mColumnChartView.setVisibility(View.VISIBLE);

                        SimpleDateFormat sdf = new SimpleDateFormat(fromatDate, Locale.getDefault());
                        String first = sdf.format(getFirstDayOfWeek(new Date()));
                        String last = sdf.format(getLastDayOfWeek(new Date()));

                        Title.setText(first.substring(5) + "/" + last.substring(5));
                        setSleepWeek(new Date());
                        break;
                    case TYPE_MONTH:
                        mHistogramView.setVisibility(View.GONE);
                        mColumnChartView.setVisibility(View.VISIBLE);
                        int w = calendar.get(Calendar.MONTH) + 1;
                        int y = calendar.get(Calendar.YEAR);
                        if ((w) < 10) {
                            Title.setText(y + "-0" + w);
                        } else {
                            Title.setText(y + "-" + w);
                        }
                        setSleepMonth(new Date());
                        break;
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

    }

    public String format(int value) {
        return Utils.setFormat(value / 60, "00", Utils.NUMBER) + ":" + Utils.setFormat(value % 60, "00", Utils.NUMBER);
    }

}
