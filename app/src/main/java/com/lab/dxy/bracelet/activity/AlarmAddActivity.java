package com.lab.dxy.bracelet.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.PickerView;
import com.lab.dxy.bracelet.ui.RxToast;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：jack
 * 创建时间：2017/5/9
 */

@EActivity(R.layout.actiivity_addalarm)
public class AlarmAddActivity extends BaseActivity {
    private byte min;
    private byte hour;
    private byte repeatData = 0x02;
    private byte dayOfWeek;
    private byte type = 0x02;
    private boolean isOpen = false;
    private CircleProgressDialog circleProgressDialog;
    private String weekStr;
    String[] week;

    @ViewById
    PickerView hourPicker;
    @ViewById
    PickerView minPicker;
    @ViewById
    ImageView img_delete;
    @ViewById
    TextView Title;
    @ViewById
    RelativeLayout rl_delete;
    @ViewById
    TextView weekHit;
    @ViewById
    TextView DistanceTimeText;

    @ViewById
    TextView Mon;
    @ViewById
    TextView Tue;
    @ViewById
    TextView Wed;
    @ViewById
    TextView Thu;
    @ViewById
    TextView Fri;
    @ViewById
    TextView Sat;
    @ViewById
    TextView Sun;


    @Pref
    MyPrefs_ myPrefs;

    @Extra
    boolean isAdd;

    @Extra
    long addTime;

    @Extra
    int position;


    @Click
    void rl_delete() {
        L.d("hour:" + this.hour + "---min:" + min + "----repeatData:" + repeatData + "---mode:" + 0 + "----position:" + position + "----type:" + type);
        if (isAdd) {
            isOpen = true;
            //TODO 写入闹钟信息
            addTime = System.currentTimeMillis();
            L.d("userAlarmTab:");
            onOpenAlarm();
        } else {
            UserAlarmTab tab = UserAlarmTab.getaddTime(addTime);
            if (tab != null) {
                isOpen = tab.isEnable();
            }
            UserAlarmTab.deletByAddedTime(addTime);
            onOpenAlarm();
        }
    }


    public void onOpenAlarm() {

//        MyToast.showShort(this, "正在设置闹钟");
        L.d("添加时间:" + Utils.setFormat(addTime, "yyyy-MM-dd HH mm ss", Utils.DATE));

        if (repeatData == 0x00) {
            L.d("仅响应一次");
            String s = Utils.setFormat(addTime, "HH-mm", Utils.DATE);
            String[] split = s.split("-");
            int nowHour = Integer.parseInt(split[0]);
            int nowMin = Integer.parseInt(split[1]);
            if (hour < nowHour) {
                addTime = addTime + 24 * 60 * 60 * 1000;
                L.d("向后推一天:" + Utils.setFormat(addTime, "yyyy-MM-dd HH mm ss", Utils.DATE));
            } else if (hour == nowHour && min < nowMin) {
                addTime = addTime + 24 * 60 * 60 * 1000;
                L.d("向后推一天:" + Utils.setFormat(addTime, "yyyy-MM-dd HH mm ss", Utils.DATE));
            }
        }
        if (BleService.isConnected) {
            circleProgressDialog.showDialog();
            rl_delete.setFocusable(false);
            rl_delete.setEnabled(false);
            L.d("添加时间:" + addTime);
            final UserAlarmTab tab = new UserAlarmTab(addTime, (int) hour, (int) min, 0, (int) repeatData, (int) type, isOpen);
            L.d("tab:---:" + tab);
            MyBle.getInstance().sendAlarmPush(true, tab, isSuccess -> {
                if (isSuccess) {
                    tab.save();
                    RxToast.success(getString(R.string.settingAlarmSuccess));
                    onBackPressed();
                } else {
                    if (UserAlarmTab.getAll().size() == 3)
                        RxToast.warning(getString(R.string.alarm_set_up, 3));
                    else
                        RxToast.warning(getString(R.string.settingAlarmFail2));
                }
                rl_delete.setFocusable(true);
                rl_delete.setEnabled(true);
                circleProgressDialog.dismiss();
            });
        } else {
            RxToast.warning(getString(R.string.barDisConnect));
        }
    }

    boolean[] weekIsSelect = {false, false, false, false, false, false, false};

    public void week(View view) {
        weekStr = "";
        switch (view.getId()) {
            case R.id.Mon:
                weekIsSelect[0] = !weekIsSelect[0];
                break;
            case R.id.Tue:
                weekIsSelect[1] = !weekIsSelect[1];
                break;
            case R.id.Wed:
                weekIsSelect[2] = !weekIsSelect[2];
                break;
            case R.id.Thu:
                weekIsSelect[3] = !weekIsSelect[3];
                break;
            case R.id.Fri:
                weekIsSelect[4] = !weekIsSelect[4];
                break;
            case R.id.Sat:
                weekIsSelect[5] = !weekIsSelect[5];
                break;
            case R.id.Sun:
                weekIsSelect[6] = !weekIsSelect[6];
                break;
        }

        int i = 0;
        for (int j = 0; j < 7; j++) {
            if (weekIsSelect[j]) i++;
            int position = j + 1;
            weekStr = weekIsSelect[j] ? weekStr + " " + week[position > 6 ? 0 : position] : weekStr.replace(week[position > 6 ? 0 : position], "");
        }
        if (i == 0) {
            weekHit.setText(getString(R.string.Never));
        } else if (i == 7) {
            weekHit.setText(getString(R.string.Every));
        } else {
            weekHit.setText(weekStr);
        }
        getRepeatDataResult();
        setSelect();
    }

    private void getRepeatDataResult() {
        repeatData = 0x00;

        if (weekIsSelect[0]) {
            repeatData = (byte) (repeatData | (0x01 << 1));
        }
        if (weekIsSelect[1]) {
            repeatData = (byte) (repeatData | (0x01 << 2));
        }
        if (weekIsSelect[2]) {
            repeatData = (byte) (repeatData | (0x01 << 3));
        }
        if (weekIsSelect[3]) {
            repeatData = (byte) (repeatData | (0x01 << 4));
        }
        if (weekIsSelect[4]) {
            repeatData = (byte) (repeatData | (0x01 << 5));
        }
        if (weekIsSelect[5]) {
            repeatData = (byte) (repeatData | (0x01 << 6));
        }
        if (weekIsSelect[6]) {
            repeatData = (byte) (repeatData | 0x01);
        }
    }


    @Click
    void running() {
        onBackPressed();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }


    @AfterViews
    void initView() {
        week = getResources().getStringArray(R.array.week);
        initPicker();
        circleProgressDialog = new CircleProgressDialog(this);
        circleProgressDialog.setText("");
        Title.setText(getString(R.string.settingAlarm));
        img_delete.setBackground(getResources().getDrawable(R.mipmap.icosuccessfulx));
        if (isAdd) {
            Calendar calendar = Calendar.getInstance();
            dayOfWeek = (byte) calendar.get(Calendar.DAY_OF_WEEK);

            hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
            min = (byte) calendar.get(Calendar.MINUTE);
            L.d("闹钟：" + dayOfWeek);
            setRepeatData(dayOfWeek);
            dayOfWeek = (byte) (dayOfWeek - 1);
            weekHit.setText(week[dayOfWeek]);
            weekStr = week[dayOfWeek];
            weekIsSelect[dayOfWeek - 1 < 0 ? 6 : dayOfWeek - 1] = true;
            hourPicker.setSelected(hour);
            minPicker.setSelected(min);
            setSelect();
            distanceTime();
        } else {
            UserAlarmTab userAlarmTab = UserAlarmTab.getaddTime(addTime);
            repeatData = (byte) userAlarmTab.getWeek();
            weekStr = setRepeatString(repeatData);
            weekHit.setText(setRepeatString(repeatData));
            setSelect();
            hour = (byte) userAlarmTab.getHour();
            min = (byte) userAlarmTab.getMin();
            hourPicker.setSelected(hour);
            minPicker.setSelected(min);

            distanceTime();
            MyBle.getInstance().sendAlarmPush(false, userAlarmTab, null);
        }
    }

    private void initPicker() {
        List<String> hourData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourData.add(i < 10 ? "0" + i : i + "");
        }
        hourPicker.setData(hourData);
        List<String> minData = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minData.add(i < 10 ? "0" + i : i + "");
        }
        minPicker.setData(minData);

        hourPicker.setOnSelectListener(text -> {
            hour = Byte.parseByte(text);
            distanceTime();
        });
        minPicker.setOnSelectListener(text -> {
            min = Byte.parseByte(text);
            distanceTime();
        });

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (circleProgressDialog != null)
            circleProgressDialog.dismiss();
    }

    public String setRepeatString(byte data) {
        String repeat = "";
        L.d("setRepeat: " + data);

        if ((data & 0x02) == 0x02) {
            repeat = getString(R.string.Mon) + " ";
            weekIsSelect[0] = true;
        }
        if ((data & 0x04) == 0x04) {
            repeat = repeat + getString(R.string.Tue) + " ";
            weekIsSelect[1] = true;
        }
        if ((data & 0x08) == 0x08) {
            repeat = repeat + getString(R.string.Wed) + " ";
            weekIsSelect[2] = true;
        }
        if ((data & 0x10) == 0x10) {
            repeat = repeat + getString(R.string.Thu) + " ";
            weekIsSelect[3] = true;
        }
        if ((data & 0x20) == 0x20) {
            repeat = repeat + getString(R.string.Fri) + " ";
            weekIsSelect[4] = true;
        }
        if ((data & 0x40) == 0x40) {
            repeat = repeat + getString(R.string.Sat) + " ";
            weekIsSelect[5] = true;
        }
        if ((data & 0x01) == 0x01) {
            repeat = repeat + getString(R.string.Sun) + " ";
            weekIsSelect[6] = true;
        }
        if (data == 0x00) {
            repeat = getString(R.string.Never);
        }
        if (data == 0x7f) {
            repeat = getString(R.string.Every);
        }
        return repeat;
    }

    public void setRepeatData(byte dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                repeatData = 0x01;
                break;
            case 2:
                repeatData = 0x02;
                break;
            case 3:
                repeatData = 0x04;
                break;
            case 4:
                repeatData = 0x08;
                break;
            case 5:
                repeatData = 0x10;
                break;
            case 6:
                repeatData = 0x20;
                break;
            case 7:
                repeatData = 0x40;
                break;
        }
    }


    private void setSelect() {
        Mon.setBackgroundResource(weekIsSelect[0] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Tue.setBackgroundResource(weekIsSelect[1] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Wed.setBackgroundResource(weekIsSelect[2] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Thu.setBackgroundResource(weekIsSelect[3] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Fri.setBackgroundResource(weekIsSelect[4] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Sat.setBackgroundResource(weekIsSelect[5] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
        Sun.setBackgroundResource(weekIsSelect[6] ? R.drawable.shape_circle_flyblue : R.drawable.shape_circle_line);
    }


    private void distanceTime() {
        Calendar calendar = Calendar.getInstance();
        int newHour = hour - calendar.get(Calendar.HOUR_OF_DAY);
        int newMin = min - calendar.get(Calendar.MINUTE);
        if (newHour == 0 && newMin < 0)
            DistanceTimeText.setText(getString(R.string.DistanceTime, 23, newMin + 60));
        else if (newHour == 0 && newMin == 0)
            DistanceTimeText.setText(getString(R.string.DistanceTime, 24, 0));
        else
            DistanceTimeText.setText(getString(R.string.DistanceTime, newHour < 0 ? newHour + 24 : newHour, newMin < 0 ? newMin + 60 : newMin));
    }

}
