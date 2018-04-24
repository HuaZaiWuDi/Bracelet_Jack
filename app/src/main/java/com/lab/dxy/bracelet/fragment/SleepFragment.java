package com.lab.dxy.bracelet.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxTextUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.activity.BaseByFragmentActivity;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.entity.SleepItem;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.HistogramView;
import com.lab.dxy.bracelet.ui.RoundDisPlayView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;

/**
 * 项目名称：BSDKracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/26
 */

@EFragment(R.layout.fragment_sleep)
public class SleepFragment extends BaseFragment {

    int TotalTime = 0;

    public static synchronized Fragment getInstance() {
        return new SleepFragment_();
    }


    @ViewById
    RoundDisPlayView mRoundDisPlayView;
    @ViewById
    TextView textData_1;
    @ViewById
    TextView textData_2;
    @ViewById
    TextView textData_3;
    @ViewById
    HistogramView mHistogramView;
    @Pref
    MyPrefs_ myPrefs;

    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime() {
        synStepsData();
    }


    @Click
    void img_statistics() {
        Intent intent = new Intent(getActivity(), BaseByFragmentActivity.class);
        intent.putExtra("type", 1);
        startActivity(intent);
    }

    @AfterViews
    void initView() {

        mRoundDisPlayView.startAnimation();
        mRoundDisPlayView.setCentreText(0 / 60 + "", "h", getString(BleService.isConnected ? R.string.barConnect : R.string.barDisConnect))
                .setBackground(Color.parseColor("#25b4b4")).submit();
        synStepsData();

    }

    private int int2Angle(int i) {
        int d = i * 100 / TotalTime;
        L.d("d:" + d);
        int angle = d * 360 / 100;
        L.d("开始角度：" + angle);
        return angle;
    }


    List<HistogramView.Bar> mBarLists = new ArrayList<>();
    List<SleepItem> sleepItems = new ArrayList<>();

    private void synStepsData() {


        String date = Utils.setFormat(System.currentTimeMillis(), fromatDate, Utils.DATE);
        List<SleepDataTab2> sleepWeek = SleepDataTab2.getSleepDate(date);
        mBarLists.clear();
        sleepItems.clear();
        TotalTime = 0;

        int awake = 0;
        int restless = 0;
        int asleep = 0;


        if (sleepWeek.size() > 1) {

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
                    L.d("睡眠时间：" + time + "----type:" + type);
                    if (time >= checkNoWear || time >= 5 * 60) {
                        L.d("相同状态下判断为未佩戴：" + checkNoWear / 60);

                        SleepDataTab2.deleteByDate(date);
                        SleepDataTab2 sleepDataTab = new SleepDataTab2(date, 0, 0, getString(R.string.NoWearBra));
                        sleepDataTab.save();
                        mBarLists.clear();
                        mHistogramView.setBarLists(mBarLists);

                        mRoundDisPlayView.setCentreText(0 + "", "h", getString(R.string.NoWearBra))
                                .submit();

                        RxTextUtils.setTextView(getActivity(), textData_1, 0, getResources().getColor(R.color.flyBlue));
                        RxTextUtils.setTextView(getActivity(), textData_2, 0, getResources().getColor(R.color.flyBlue));
                        RxTextUtils.setTextView(getActivity(), textData_3, 0, getResources().getColor(R.color.flyBlue));

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

                mRoundDisPlayView.setCentreText((asleep + restless) / 60 + "", "h", getString(R.string.startSleep) + "：" + StartTime + getString(R.string.endSleep) + "：" + EndTime)
                        .setSweepAngle(new int[]{int2Angle(asleep), int2Angle(restless), int2Angle(awake)}).submit();
                mHistogramView.setText(strings);

            } catch (Exception e) {
                e.printStackTrace();
                L.d("运算异常");
            }
        } else {
            mRoundDisPlayView.setCentreText(0 + "", "h", sleepWeek.size() == 1 ? sleepWeek.get(0).getTime() : getString(R.string.noSleepData))
                    .submit();
        }

        RxTextUtils.setTextView(getActivity(), textData_1, asleep, getResources().getColor(R.color.flyBlue));
        RxTextUtils.setTextView(getActivity(), textData_2, restless, getResources().getColor(R.color.flyBlue));
        RxTextUtils.setTextView(getActivity(), textData_3, awake, getResources().getColor(R.color.flyBlue));

        mHistogramView.setBarLists(mBarLists);
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

}
