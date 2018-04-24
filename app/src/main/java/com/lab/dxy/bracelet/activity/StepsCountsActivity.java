package com.lab.dxy.bracelet.activity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.adapter.IntroductionsAdapter;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
import com.lab.dxy.bracelet.core.GetMarkClass;
import com.lab.dxy.bracelet.entity.spl.DayStepsTab;
import com.lab.dxy.bracelet.service.BleService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.Utils.Utils.getDayOfWeekEvery;
import static com.lab.dxy.bracelet.Utils.Utils.getEveryDayOfMonth;
import static com.lab.dxy.bracelet.Utils.Utils.getFirstDayOfWeek;
import static com.lab.dxy.bracelet.Utils.Utils.getLastDayOfWeek;
import static com.lab.dxy.bracelet.fragment.exercisefragment.fromatDouble;
import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;


/**
 * 项目名称： bracelet
 * 类描述： 计步数据详情
 * 创建时间： 2017/10/9 15:16
 * 创建人：Jack
 */

@EActivity(R.layout.activity_steps_counts)
public class StepsCountsActivity extends BaseActivity {
    private String[] titles;
    private String[] days = new String[30];
    private String[] weeks = new String[30];
    private String[] months = new String[30];

    public static final int TYPE_DAY = 0;
    public static final int TYPE_WEEK = 1;
    public static final int TYPE_MONTH = 2;
    public int type = 0;
    IntroductionsAdapter adapter;
    Calendar calendarDay;
    Calendar calendarWeek;
    Calendar calendarMonth;
    private String[] hours, week, day;
    List<DayStepsTab> steps;
    List<List<DayStepsTab>> allStepsDay = new ArrayList<>();
    List<List<DayStepsTab>> allStepsWeek = new ArrayList<>();
    List<List<DayStepsTab>> allStepsMonth = new ArrayList<>();
    private AnimationDrawable loading;

    @ViewById
    SegmentTabLayout mSlidingTabLayout;
    @ViewById
    SlidingTabLayout MySlidingTabLayout;
    @ViewById
    ViewPager mViewPager;
    @ViewById
    TextView Title;
    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    TextView daySteps;
    @ViewById
    TextView dayKm;
    @ViewById
    TextView dayKcal;
    @ViewById
    TextView dayAim;
    @ViewById
    ImageView img_right;


    @Receiver(actions = ACTION_BLE_DISCONNECT)
    protected void onActionDisconnected() {
        if (loading != null)
            loading.stop();
    }


    @Click
    void running() {
        onBackPressed();
    }

    @AfterViews
    void initView() {

        titles = new String[]{getString(R.string.day), getString(R.string.week), getString(R.string.month)};
        Title.setText(getString(R.string.stepsCount));
        chooseDate();
        chooseDay();

        img_right.setImageResource(R.drawable.loading);
        loading = (AnimationDrawable) img_right.getDrawable();


        if (BleService.isConnected) {
            requerySteps();
        }
    }

    private void requerySteps() {
        byte[] bytes;
        //删除当天的，重新请求

//        DayStepsTab.deleteforDate(fromatDate(new Date()));
        if (DayStepsTab.getAll().size() == 0) {
            bytes = new byte[]{0, 0, 0, 0, 0, 0};
        } else {
            return;
//            Calendar c = Calendar.getInstance();
//            byte year = (byte) c.get(Calendar.YEAR);
//            byte month = (byte) (c.get(Calendar.MONTH) + 1);
//            byte day = (byte) c.get(Calendar.DAY_OF_MONTH);
//            bytes = new byte[]{year, month, day, year, month, day};
        }
        img_right.setVisibility(View.VISIBLE);
        loading.start();
        MyBle.getInstance().synStepsData(bytes, new BleCommandListener() {
            @Override
            public void success() {
                img_right.setVisibility(View.VISIBLE);
                loading.stop();
                L.d("成功");
                chooseDay();
            }

            @Override
            public void fail() {
                img_right.setVisibility(View.VISIBLE);
                L.d("失败");
                loading.stop();
            }
        });
    }

    private void synDayStepsData(int position) {

        L.d("allSteps:" + allStepsDay.size());
        List<DayStepsTab> tabs = allStepsDay.get(29 - position);

        L.d("tabs:" + tabs.size());

        synStepsData(tabs.size() > 0 ? tabs.get(tabs.size() - 1).getSteps() : 0);
    }


    private void synStepsData(int sumSteps) {

        int height = myPrefs.Height().get();
        int weight = myPrefs.Weight().get();
        int runAim = myPrefs.runAim().get();

        GetMarkClass markClass = new GetMarkClass(sumSteps, weight, height);
        double km = markClass.getKm();
        double mark = markClass.getMark();

        daySteps.setText(sumSteps + "");
        dayKm.setText(fromatDouble(km, 2) + "");
        dayKcal.setText(fromatDouble(mark, 2) + "");
        dayAim.setText(runAim + "");

    }


    private void synWeekStepsData(int position) {
        L.d("allSteps:" + allStepsWeek.size());
        List<DayStepsTab> tabs = allStepsWeek.get(29 - position);


        L.d("tabs:" + tabs.size());
        int sumSteps = 0;
        for (DayStepsTab tab : tabs) {
            if (tab != null)
                sumSteps = sumSteps + tab.getSteps();
        }

        int height = myPrefs.Height().get();
        int weight = myPrefs.Weight().get();
        int runAim = myPrefs.runAim().get();

        GetMarkClass markClass = new GetMarkClass(sumSteps, weight, height);
        double km = markClass.getKm();
        double mark = markClass.getMark();

        daySteps.setText(sumSteps + "");
        dayKm.setText(fromatDouble(km, 2) + "");
        dayKcal.setText(fromatDouble(mark, 2) + "");
        dayAim.setText(runAim * 7 + "");

    }


    private void synMonthStepsData(int position) {
        List<DayStepsTab> tabs = allStepsMonth.get(29 - position);

        int sumSteps = 0;
        for (DayStepsTab tab : tabs) {
            if (tab != null)
                sumSteps = sumSteps + tab.getSteps();
        }

        int height = myPrefs.Height().get();
        int weight = myPrefs.Weight().get();
        int runAim = myPrefs.runAim().get();

        GetMarkClass markClass = new GetMarkClass(sumSteps, weight, height);
        double km = markClass.getKm();
        double mark = markClass.getMark();

        daySteps.setText(sumSteps + "");
        dayKm.setText(fromatDouble(km, 2) + "");
        dayKcal.setText(fromatDouble(mark, 2) + "");
        dayAim.setText(runAim * 30 + "");

    }


    private void chooseWeek() {
        calendarWeek = Calendar.getInstance();
        week = getResources().getStringArray(R.array.week);
        if (allStepsWeek.size() == 0)
            for (int i = 29; i >= 0; i--) {

                if (i != 29) {
                    calendarWeek.add(Calendar.DATE, -7);
                }
                weeks[i] = fromatDate(calendarWeek.getTime()).substring(5);

                SimpleDateFormat sdf = new SimpleDateFormat(fromatDate, Locale.getDefault());
                String first = sdf.format(getFirstDayOfWeek(calendarWeek.getTime()));
                String last = sdf.format(getLastDayOfWeek(calendarWeek.getTime()));

                weeks[i] = first.substring(5) + "/" + last.substring(5);

                steps = new ArrayList<>();
                for (int j = 0; j < 7; j++) {
                    String day = Utils.setFormat(getDayOfWeekEvery(calendarWeek.getTime(), j).getTime(), fromatDate, Utils.DATE);
                    steps.add(j, null);
                    List<DayStepsTab> tabs = DayStepsTab.getByDate(day);
                    if (tabs.size() > 0)
                        steps.add(j, tabs.get(tabs.size() - 1));
                }
                allStepsWeek.add(steps);
            }

        adapter = new IntroductionsAdapter(allStepsWeek, week);

        mViewPager.setAdapter(adapter);

        MySlidingTabLayout.setViewPager(mViewPager, weeks);


        mViewPager.setCurrentItem(weeks.length - 1, false);

        adapter.setOnValueTouchListener((i, i1, pointValue) -> synStepsData((int) pointValue.getY()));
    }

    private List<String[]> label = new ArrayList<>();

    private void chooseMonth() {
        calendarMonth = Calendar.getInstance();

        if (allStepsMonth.size() == 0)
            for (int i = 29; i >= 0; i--) {
                if (i != 29)
                    calendarMonth.add(Calendar.MONTH, -1);

                months[i] = fromatDate(calendarMonth.getTime()).substring(0, 7);

                int daysCountOfMonth = calendarMonth.getActualMaximum(Calendar.DATE);//获取指定年份中指定月份有几天
                L.d("daysCountOfMonth:" + daysCountOfMonth);
                day = new String[daysCountOfMonth];

                steps = new ArrayList<>();
                for (int j = 0; j < daysCountOfMonth; j++) {
                    day[j] = (j + 1) + "";

                    String day = Utils.setFormat(getEveryDayOfMonth(calendarMonth.getTime(), j).getTime() + 24 * 60 * 60 * 1000, fromatDate, Utils.DATE);

                    steps.add(j, null);
                    List<DayStepsTab> tabs = DayStepsTab.getByDate(day);
                    if (tabs.size() > 0)
                        steps.add(j, tabs.get(tabs.size() - 1));
                }
                allStepsMonth.add(steps);
                label.add(day);
            }

        adapter = new IntroductionsAdapter(label, allStepsMonth);

        mViewPager.setAdapter(adapter);

        MySlidingTabLayout.setViewPager(mViewPager, months);

        mViewPager.setCurrentItem(months.length - 1, false);

        adapter.setOnValueTouchListener((i, i1, pointValue) -> synStepsData((int) pointValue.getY()));

    }


    private void chooseDay() {
        calendarDay = Calendar.getInstance();
        hours = new String[24];
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hours[i] = "0" + i;
            } else {
                hours[i] = i + "";
            }
        }
        if (allStepsDay.size() == 0)
            for (int i = 29; i >= 0; i--) {
                if (i != 29) {
                    calendarDay.add(Calendar.DATE, -1);
                }
                days[i] = fromatDate(calendarDay.getTime()).substring(5);


                steps = DayStepsTab.getByDate(fromatDate(calendarDay.getTime()));
                allStepsDay.add(steps);
            }

        adapter = new IntroductionsAdapter(allStepsDay, hours);
        mViewPager.setAdapter(adapter);

        MySlidingTabLayout.setViewPager(mViewPager, days);

        //选择单天
        MySlidingTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                L.d("type:" + type);
                switch (type) {
                    case TYPE_DAY:
                        synDayStepsData(position);
                        break;
                    case TYPE_WEEK:
                        synWeekStepsData(position);
                        break;
                    case TYPE_MONTH:
                        synMonthStepsData(position);
                        break;
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (type) {
                    case TYPE_DAY:
                        synDayStepsData(position);
                        break;
                    case TYPE_WEEK:
                        synWeekStepsData(position);
                        break;
                    case TYPE_MONTH:
                        synMonthStepsData(position);
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        adapter.setOnValueTouchListener((i, i1, pointValue) -> synStepsData((int) pointValue.getY()));

        mViewPager.setCurrentItem(days.length - 1, false);
    }


    private void chooseDate() {
        //选择日月周
        mSlidingTabLayout.setTabData(titles);

        mSlidingTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                type = position;
                L.d("type:" + type);
                switch (position) {
                    case TYPE_DAY:
                        chooseDay();
                        break;
                    case TYPE_WEEK:
                        chooseWeek();
                        break;
                    case TYPE_MONTH:
                        chooseMonth();
                        break;
                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
    }


    private String fromatDate(Date date) {
        return Utils.setFormat(date.getTime(), fromatDate, Utils.DATE);
    }


}
