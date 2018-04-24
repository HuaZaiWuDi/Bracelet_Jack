package com.lab.dxy.bracelet.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.MyApplication;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ByteUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxTextUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimer;
import com.lab.dxy.bracelet.activity.StepsCountsActivity_;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
import com.lab.dxy.bracelet.core.GetMarkClass;
import com.lab.dxy.bracelet.entity.spl.DayStepsTab;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.entity.spl.UserStepsTab;
import com.lab.dxy.bracelet.ui.RoundIndicatorView;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.myGridView;
import com.syd.oden.odenble.Utils.HexUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import me.dkzwm.smoothrefreshlayout.RefreshingListenerAdapter;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;

import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;
import static com.lab.dxy.bracelet.service.BleService.isBg;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * Created by 华 on 2017/5/4.
 */
@EFragment(R.layout.fragment_execisestate)
public class exercisefragment extends BaseFragment {
    private double distanceAll = 0;
    private double calorie = 0;
    private int heigeht;
    private int weigeht;
    private int runAim;
    ColumnChartData data;
    public static boolean DATA_INIT = false;

    public static boolean isFirstSyn = true;

    private Handler handler = new Handler();

    @Pref
    MyPrefs_ myPrefs;
    @ViewById
    RoundIndicatorView mRoundIndicatorView;
    @ViewById
    myGridView mGridView;
    @ViewById
    SmoothRefreshLayout refresh;
    @ViewById
    ColumnChartView mColumnChartView;
    @ViewById
    ImageView img_1;
    @ViewById
    ImageView img_2;
    @ViewById
    ImageView img_3;
    @ViewById
    TextView textData_1;
    @ViewById
    TextView textData_2;
    @ViewById
    TextView textData_3;

    @Receiver(actions = ACTION_BLE_CONNECTED)
    protected void onActionConnected() {
        if (refresh != null)
            if (!refresh.isRefreshing())
                refresh.autoRefresh(false);
    }


    @Receiver(actions = ACTION_BLE_DISCONNECT)
    protected void onActionDisconnected() {
        refreshComplete();
    }


    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime() {
        L.d("显示状态isResumed()：" + isResumed());
        L.d("显示状态isVisible()：" + isVisible());
        if (isVisible())
            timer.startTimer();

        setmColumnChartView();
    }


    @Click
    void img_statistics() {
        startActivity(new Intent(getActivity(), StepsCountsActivity_.class));
    }

    @AfterViews
    void init() {
        initRefresh();
        setmColumnChartView();
        doIconColor();
    }

    private void doIconColor() {
        img_1.setImageDrawable(Utils.tintDrawableWithColor(MyApplication.getmContext(), R.mipmap.stepstime, R.color.default_color));
        img_2.setImageDrawable(Utils.tintDrawableWithColor(MyApplication.getmContext(), R.mipmap.mileage, R.color.default_color));
        img_3.setImageDrawable(Utils.tintDrawableWithColor(MyApplication.getmContext(), R.mipmap.fire, R.color.default_color));
    }

    MyPeriodTimer timer = new MyPeriodTimer(5 * 1000, 10 * 1000, () -> {
        if (!refresh.isRefreshing() && isConnected && !isBg)
            onlySynTodaySteps();
    });


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            L.d("隐藏：exercisefragment");
            timer.stopTimer();
        } else {
            L.d("显示：exercisefragment");
            if (isVisible())
                timer.startTimer();
        }
    }


    private void initRefresh() {
        refresh.setDisableLoadMore(true);
        refresh.setEnableKeepRefreshView(true);
        ClassicHeader header = new ClassicHeader(getActivity());
        refresh.setHeaderView(header);

        refresh.setEnableOverScroll(false);
//        refresh.setEnabledInterceptEventWhileLoading(true);设置刷新工程中不响应触摸事件，和setEnableKeepRefreshView(true);一起用

        refresh.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                header.removeCallbacks(refreshFail);
                handler.postDelayed(refreshFail, 20 * 1000);
                if (isConnected) {
                    handler.postDelayed(() ->
                            initData(), 500);
                } else {
                    refreshComplete();
                    RxToast.warning(getString(R.string.barDisConnect));
                }
            }
        });
        if (!refresh.isRefreshing() && isConnected)
            refresh.autoRefresh(false);

    }


    Runnable refreshFail = new Runnable() {
        @Override
        public void run() {
            if (refresh.isRefreshing()) {
                refresh.refreshComplete(false);
            }
        }
    };


    private void queryPower() {
        MyBle.getInstance().queryPower(data -> {
            L.d("接收电量：" + HexUtil.encodeHexStr(data));
            bleCharge();
            if (data[1] == 0x0d) {
                myPrefs.edit().powerValue().put((int) data[4]).apply();
                L.d("当前电量：" + data[4]);
            }
        });
    }


    private void queryBright() {
        MyBle.getInstance().queryBright(data -> {
            L.d("接收抬手亮屏：" + HexUtil.encodeHexStr(data));
            querySed();

            if (data[1] == 0x0c && data[3] == (byte) 0xfd) {
                myPrefs.edit().BrightIsOpen().put(data[4] == 0x01).apply();
            }
        });
    }


    private void querySed() {
        MyBle.getInstance().queryAlarm(2, 0, data -> {
            L.d("接收久坐提醒：" + HexUtil.encodeHexStr(data));
            synSleepData();
            if (data[1] == 0x14 && data[4] == 0x02) {
                myPrefs.edit().SedTime().put((data[6] * 60 + data[7]) < 30 ? 30 : (data[6] * 60 + data[7])).apply();

                myPrefs.edit().SedentaryIsOpen().put(data[5] == 0x01).apply();

                L.d("久坐提醒：");
            }
        });
    }

    private void bleCharge() {
        MyBle.getInstance().queryCharge(isSuccess -> {
            L.d("接收充电状态：" + isSuccess);
            if (isFirstSyn) {
                queryBright();
            } else {
                synLastDayStepsData(true);
            }
        });
    }


    private void synSleepData() {
        MyBle.getInstance().querySleepDate(data -> {
            L.d("接收睡眠时间：" + HexUtil.encodeHexStr(data));
            if (data[1] == (byte) 0x0d && data[3] == (byte) 0xa1) {

                myPrefs.sleepData().put(data[4] + "-" + data[6]);
            }

            synLastDayStepsData(false);
        });
    }

    private void synLastDayStepsData(boolean isSynToday) {
        Calendar c = Calendar.getInstance();
        if (!isSynToday)
            c.add(Calendar.DATE, -1);
        byte year = (byte) (c.get(Calendar.YEAR) - 2000);
        byte month = (byte) (c.get(Calendar.MONTH) + 1);
        byte day = (byte) (c.get(Calendar.DAY_OF_MONTH));
        L.d("年：" + c.get(Calendar.YEAR));
        L.d("月：" + month);
        L.d("日：" + day);
        byte[] bytes = new byte[]{year, month, day, year, month, day};

        MyBle.getInstance().synStepsData(bytes, new BleCommandListener() {
            @Override
            public void success() {
                if (checkSleepTime())
                    mRoundIndicatorView.postDelayed(() -> readSleepData(), 3000);
            }

            @Override
            public void fail() {
                L.d("失败");
            }
        });
    }


    private boolean checkSleepTime() {
        String date = Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE);
        if (!findFragment.sheckTime(date, myPrefs.sleepData().get())) {
            L.d("时间已到");
            if (SleepDataTab2.getSleepDate(date).size() > 0) {
                refreshComplete();
                Utils.broadUpdate(ACTION_DATA_INIT);
                return false;
            }
        } else {
            refreshComplete();
            Utils.broadUpdate(ACTION_DATA_INIT);
            return false;
        }
        return true;
    }


    //读当天的睡眠数据
    public void readSleepData() {

        String date = Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE);
        String[] split = date.split("-");
        if (split.length != 3) return;
        int year = Integer.parseInt(split[0]) - 2000;
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[2]);

        byte[] dateByte = new byte[]{(byte) year, (byte) month, (byte) day, (byte) year, (byte) month, (byte) day};

        MyBle.getInstance().synSleepData(dateByte, new BleCommandListener() {
            @Override
            public void success() {
                refreshComplete();
                Utils.broadUpdate(ACTION_DATA_INIT);
                setLast();
            }

            @Override
            public void fail() {
                SleepDataTab2 sleepDataTab = new SleepDataTab2(date, 0, 0, "没有睡眠数据");
                sleepDataTab.save();
                setLast();
                refreshComplete();
                Utils.broadUpdate(ACTION_DATA_INIT);
            }
        });
    }

    private void setLast() {
//        MyBle.getInstance().last(myPrefs.isOpenAntiLast().get(), 0, null);
    }

    private void onlySynTodaySteps() {
        MyBle.getInstance().queryOneDaySteps(0, 0, 0, data -> {
            if (data[3] == (byte) 0x86) {
                L.d("接收当日计步：" + HexUtil.encodeHexStr(data));

                byte[] bytes = new byte[4];
                System.arraycopy(data, 7, bytes, 0, 4);

                int steps = ByteUtil.bytesToIntG4(bytes, 0);
                L.d("当天步数：" + steps);
                myPrefs.steps().put(steps);
                synStepsData(steps);
            }
        });
    }


    private void synTodaySteps() {
        final String date = Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE);
        MyBle.getInstance().queryOneDaySteps(0, 0, 0, data -> {
            queryPower();

            myPrefs.edit().otaVersion().put(Contents.otaVersion).apply();
            myPrefs.edit().deviceVersion().put(Contents.deviceVersion).apply();
            L.d("接收当日计步：" + HexUtil.encodeHexStr(data));
            if (data[3] == (byte) 0x86) {

                byte[] bytes = new byte[4];
                System.arraycopy(data, 7, bytes, 0, 4);

                int steps = ByteUtil.bytesToIntG4(bytes, 0);
                L.d("当天步数：" + steps);
                myPrefs.steps().put(steps);
                synStepsData(steps);

                UserStepsTab.DeleteAddTime(date);
                GetMarkClass markClass = new GetMarkClass(steps, weigeht, heigeht);
                double km = markClass.getKm();
                UserStepsTab stepsTab = new UserStepsTab(date, steps, (int) km, runAim);
                stepsTab.save();
                L.d("保存的步数：" + UserStepsTab.getAddTime(date).toString());
            }
        });
    }


    private void initData() {

        int stepsAim = myPrefs.runAim().get() / 100;
        int sex = myPrefs.userSex().get();
        boolean userSex = sex == 0;
        int age = myPrefs.userAge().get();

        final boolean[] function = {true, true, true, true, true, true, false, true};

        function[5] = myPrefs.isOpenAntiLast().get();
        function[4] = myPrefs.CallisOpen().get();
        function[3] = myPrefs.MSMisOpen().get();

        MyBle.getInstance().synPrams(stepsAim, 1, 1, true, userSex, age, weigeht, heigeht, function, new BleCommandListener() {
            @Override
            public void success() {
                synTodaySteps();
            }

            @Override
            public void fail() {
                L.d("同步失败");

            }
        });
    }


    @UiThread
    public void refreshComplete() {
        if (refresh != null)
            if (refresh.isRefreshing())
                refresh.refreshComplete();
    }


    ///////////////////////////////////////////////////////////////////////////
    // mColumnChartView
    ///////////////////////////////////////////////////////////////////////////

    private void setmColumnChartView() {
        mColumnChartView.cancelDataAnimation();
        List<Column> columns = new ArrayList<>();

        List<AxisValue> axisValues = new ArrayList<>();
        List<AxisValue> axisYValues = new ArrayList<>();
        for (int i = 0; i < 24; i++) {

            List<SubcolumnValue> values = new ArrayList<>();
//                values.add(new SubcolumnValue(tab.getSteps(), Color.parseColor("#F0CF17")).setLabel(tab.getSteps() + ""));
            values.add(new SubcolumnValue(0, Color.parseColor("#F0CF17")));

            Column column = new Column(values);
            column.setHasLabels(true);//标签
            column.setHasLabelsOnlyForSelected(true);
            columns.add(column);

            if (i % 6 == 0)
                axisValues.add(new AxisValue(i).setLabel(Utils.setFormat(i, "00", Utils.NUMBER) + ":00"));
        }
        axisValues.add(new AxisValue(23).setLabel(Utils.setFormat(23, "00", Utils.NUMBER) + ":00"));
        axisYValues.add(new AxisValue((float) 500).setLabel(500 + ""));
        data = new ColumnChartData(columns);
        // Set stacked flag.叠加
        data.setStacked(false);
        mColumnChartView.setValueSelectionEnabled(true);//选中突出
        data.setAxisXBottom(new Axis(axisValues).setTextColor(Color.GRAY));
        data.setAxisYRight(new Axis(axisYValues).setHasLines(true).setInside(true).setTextColor(Color.parseColor("#ffffff")));


        mColumnChartView.setZoomEnabled(false);
        mColumnChartView.setColumnChartData(data);

        mColumnChartView.setOnValueTouchListener(new ColumnChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, int i1, SubcolumnValue subcolumnValue) {

            }

            @Override
            public void onValueDeselected() {

            }
        });
    }

    private void setUpdateValue() {
        mColumnChartView.cancelDataAnimation();
        Calendar calendar = Calendar.getInstance();
        List<DayStepsTab> steps = DayStepsTab.getByDate(Utils.setFormat(calendar.getTime().getTime(), fromatDate, Utils.DATE));
        if (steps == null || steps.size() < 24 || steps.size() == 0)
            return;

        int max = 0;
        int right = 0;
        for (Column column : data.getColumns()) {
            right = column.getValues().size();
            for (int i = 0; i < right; i++) {
                int steps1 = steps.get(i).getSteps();
                column.getValues().get(i).setTarget(steps1).setLabel(steps1 + "");
                max = steps1 > max ? steps1 : max;
            }
        }

        data.getAxisYLeft().getValues().get(0).setValue(max / 2).setLabel(max / 2 + "");
        Viewport v = new Viewport(0, max, (float) right, 0);
        mColumnChartView.setMaximumViewport(v);
        mColumnChartView.setCurrentViewport(v);
        mColumnChartView.setCurrentViewportWithAnimation(v);

        mColumnChartView.startDataAnimation();
    }


    private void synStepsData(int steps) {
        steps = Math.abs(steps);
        heigeht = myPrefs.Height().get();
        weigeht = myPrefs.Weight().get();
        runAim = myPrefs.runAim().get();

        getMark(steps);
        RxTextUtils.setTextView(getActivity(), textData_1, steps / 60, getResources().getColor(R.color.flyBlue));
        textData_2.setText(fromatDouble(distanceAll, 2));
        textData_3.setText(fromatDouble(calorie, 2));
    }


    @Override
    public void onStart() {
        super.onStart();
        L.d("onStart");

        synStepsData(myPrefs.steps().get());
    }


    @Override
    public void onResume() {
        super.onResume();
        L.d("33333onResume:" + isResumed());
        L.d("33333isVisible:" + isVisible());
        if (isConnected && isVisible())
            timer.startTimer();

    }


    @Override
    public void onPause() {
        super.onPause();
        timer.stopTimer();
    }


    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    private void getMark(int steps) {
        GetMarkClass markClass = new GetMarkClass(steps, weigeht, heigeht);
        double km = markClass.getKm();
        double mark = markClass.getMark();
        distanceAll = km;
        calorie = mark;
        mRoundIndicatorView.setCurrentNumAnim(steps);
        mRoundIndicatorView.setMaxNum(runAim);
    }


    public static String fromatDouble(double i, int scale) {
        BigDecimal bigDecimal = new BigDecimal(i);
        BigDecimal decimal = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);//保留小数点后2位，直接去掉值。
        return String.valueOf(decimal);
    }

    public static int fromatDouble(double i) {
        BigDecimal bigDecimal = new BigDecimal(i);
        BigDecimal decimal = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP);//保留小数点后2位，直接去掉值。
        return decimal.intValue();
    }
}
