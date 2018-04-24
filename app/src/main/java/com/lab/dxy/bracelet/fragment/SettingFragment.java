package com.lab.dxy.bracelet.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.activity.AlarmSet_;
import com.lab.dxy.bracelet.activity.BaseByFragmentActivity;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.SettingPlayItem;
import com.lab.dxy.bracelet.ui.PickerView;
import com.lab.dxy.bracelet.ui.RxSeekBar;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.OnItemClicksListener;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.location.b.g.s;
import static com.lab.dxy.bracelet.R.id.min;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/1
 */
@EFragment(R.layout.activity_setting_acivity)
public class SettingFragment extends BaseFragment {

    private List<SettingPlayItem> settingPalys = new ArrayList<>();
    private String[] takePhoto;
    private CircleProgressDialog circleProgressDialog;
    private int selectPosition = 0;

    public static synchronized SettingFragment getInstance() {
        return new SettingFragment_();
    }

    @ViewById
    RecyclerView mRecyclerView;
    @ViewById
    RelativeLayout activity_setting_acivity;

    @Pref
    MyPrefs_ myPrefs;

    @AfterViews
    void initView() {

        circleProgressDialog = new CircleProgressDialog(getActivity());
        circleProgressDialog.setText("");
        functions = getActivity().getResources().getStringArray(R.array.function);
        takePhoto = getResources().getStringArray(R.array.takePhoto);
        initRecycler();
    }

    @Override
    public void onDestroy() {
//        aCache.remove("AppInfo");
        super.onDestroy();
    }


    ///////////////////////////////////////////////////////////////////////////
    // 久坐时间设置
    ///////////////////////////////////////////////////////////////////////////

    private int SedentaryMin;

    private void showSedMin() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_sed_time, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        PickerView minPicker = (PickerView) v.findViewById(min);

        List<String> min = new ArrayList<String>();


        for (int i = 30; i < 121; i = i + 10) {
            min.add("" + i);
        }
        SedentaryMin = myPrefs.SedTime().get() < 30 ? 30 : myPrefs.SedTime().get();
        minPicker.setData(min);
        minPicker.setSelected((myPrefs.SedTime().get() - 30) / 10);
        minPicker.setOnSelectListener(text -> SedentaryMin = Integer.parseInt(text));


        final AlertDialog dialog = builder.setView(v).show();

        v.findViewById(R.id.ok).setOnClickListener((View v1) -> {
            dialog.dismiss();
            circleProgressDialog.showDialog();
            MyBle.getInstance().setSedPush(true, SedentaryMin / 60, SedentaryMin % 60, isSuccess -> {
                circleProgressDialog.dismiss();
                if (isSuccess) {
                    myPrefs.SedTime().put(SedentaryMin);
                    settingPalys.get(selectPosition).setText(SedentaryMin + "min");
                    settingPalys.get(selectPosition).setOpen(true);
                    myPrefs.SedentaryIsOpen().put(true);
                    adapter.notifyItemChanged(selectPosition);
                    RxToast.success(getString(R.string.settingAlarmSuccess));
                } else {
                    RxToast.error(SettingFragment.this.getString(R.string.InputFail));
                }
            });
        });
        v.findViewById(R.id.cancel).setOnClickListener(v12 -> dialog.dismiss());
    }

    private void setSedentary(final boolean b) {
        SedentaryMin = myPrefs.SedTime().get();
        if (SedentaryMin < 30) {
            SedentaryMin = 30;
            myPrefs.SedTime().put(SedentaryMin);
        }
        circleProgressDialog.showDialog();
        MyBle.getInstance().setSedPush(b, SedentaryMin / 60, SedentaryMin % 60, isSuccess -> {
            circleProgressDialog.dismiss();
            if (isSuccess) {
                RxToast.success(getString(R.string.settingAlarmSuccess));
                myPrefs.edit().SedentaryIsOpen().put(b).apply();
                settingPalys.get(selectPosition).setText(SedentaryMin + "min");
                settingPalys.get(selectPosition).setOpen(b);
                adapter.notifyItemChanged(selectPosition);
            }
        });
    }


    CommonAdapter adapter;

    private void initRecycler() {
        initData();
        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        adapter = new CommonAdapter<SettingPlayItem>(getActivity(), R.layout.item_paly_text, settingPalys) {
            @Override
            public void convert(ViewHolder holder, SettingPlayItem settingPlayItem) {
                holder.setText(R.id.text_name, settingPlayItem.getTitle());
                holder.setText(R.id.param, settingPlayItem.getText());
                holder.setImageResource(R.id.img_icon, settingPlayItem.getIcon());
                if (settingPlayItem.isFunction())
                    holder.setImageResource(R.id.go, settingPlayItem.isOpen() ? R.mipmap.slip_switch_on : R.mipmap.slip_switch_off);
                else
                    holder.setImageResource(R.id.go, R.mipmap.btn_arrow1_w36_n2x);


                holder.setOnClickListener(R.id.param, view -> {
                    int indexOf = settingPalys.indexOf(settingPlayItem);
                    if (indexOf == 0) {
                        showSedMin();
//                        if (isConnected)
//                        else
//                            RxToast.warning(getString(R.string.barDisConnect));
                    }
                });


                setOnItemClicksListener(new OnItemClicksListener<SettingPlayItem>() {
                    @Override
                    public void onItemClick(ViewGroup parent, View view, SettingPlayItem settingPlayItem, int position) {
                        selectPosition = position;
                        if (isConnected || position == 4 || position == 6)
                            switchPosition();
                        else
                            RxToast.warning(getString(R.string.barDisConnect));
                    }

                    @Override
                    public boolean onItemLongClick(ViewGroup parent, View view, SettingPlayItem settingPlayItem, int position) {
                        if (position == 0) {
                            if (isConnected)
                                showSedMin();
                            else
                                RxToast.warning(getString(R.string.barDisConnect));
                        }
                        return false;
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);

    }

    private void switchPosition() {
        boolean b = settingPalys.get(selectPosition).isOpen();
        int icon = settingPalys.get(selectPosition).getIcon();
        switch (icon) {
            case R.mipmap.icon_sed:
                setSedentary(!b);
                break;
            case R.mipmap.icon_bright_bra:
                bright(selectPosition, b);
                break;
            case R.mipmap.icon_last:
                myPrefs.isOpenAntiLast().put(!b);
                MyBle.getInstance().last(!b, 5000,null);
                settingPalys.get(selectPosition).setOpen(!b);
                adapter.notifyItemChanged(selectPosition);
                break;
            case R.mipmap.push:
                //跳转消息提醒页面
                Intent intent = new Intent(getActivity(), BaseByFragmentActivity.class);
                intent.putExtra("type", 6);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                break;
            case R.mipmap.icon_sleep:
                showSleepDialog();
                break;
            case R.mipmap.ico_zplay_alarm2x:
                startActivity(new Intent(getActivity(), AlarmSet_.class));
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                break;
            case R.mipmap.icon_photo_mode:
                initShakeMode();
                break;
            case R.mipmap.bg_disconnect:
                myPrefs.stepsIsOpen().put(!b);
                settingPalys.get(selectPosition).setOpen(!b);
                adapter.notifyItemChanged(selectPosition);
                break;
        }
    }

    private void bright(final int position, final boolean b) {
        circleProgressDialog.showDialog();
        MyBle.getInstance().setBright(!b, isSuccess -> {
            circleProgressDialog.dismiss();
            myPrefs.BrightIsOpen().put(!b);
            settingPalys.get(position).setOpen(!b);
            adapter.notifyItemChanged(position);
        });
    }


    /**
     * <string-array name="function">
     * <item>久坐提醒</item>
     * <item>抬手亮屏</item>
     * <item>防丢提醒</item>
     * <item>熄屏提醒</item>
     * <item>消息提醒</item>
     * <item>睡眠检测</item>
     * <item>智能闹钟</item>
     * <item>摇一摇拍照模式</item>
     **/
    String[] functions;
    int[] icons = {R.mipmap.icon_sed, R.mipmap.icon_bright_bra, R.mipmap.icon_last, R.mipmap.bg_disconnect, R.mipmap.push, R.mipmap.icon_sleep, R.mipmap.ico_zplay_alarm2x, R.mipmap.icon_photo_mode};

    private void initData() {
        settingPalys.clear();
        for (int i = 0; i < functions.length; i++) {
            if (i < 4) {
                settingPalys.add(new SettingPlayItem(functions[i], true, false, icons[i]));
            } else
                settingPalys.add(new SettingPlayItem(functions[i], false, icons[i]));
        }
        settingPalys.get(0).setText(myPrefs.SedTime().get() + "min");
        settingPalys.get(0).setOpen(myPrefs.SedentaryIsOpen().get());
        settingPalys.get(1).setOpen(myPrefs.BrightIsOpen().get());
        settingPalys.get(2).setOpen(myPrefs.isOpenAntiLast().get());
        settingPalys.get(3).setOpen(myPrefs.stepsIsOpen().get());//息屏提醒

        settingPalys.get(5).setText(sleepTime(myPrefs.sleepData().get()));

        settingPalys.get(7).setText(takePhoto[myPrefs.SeekValue().get()]);

    }


    int startTime;
    int endTime;

    private void showSleepDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.dialog_timepicker, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        PickerView startHour = (PickerView) v.findViewById(R.id.startHour);
        PickerView endHour = (PickerView) v.findViewById(R.id.endHour);
        TextView ok = (TextView) v.findViewById(R.id.ok);
        TextView cancel = (TextView) v.findViewById(R.id.cancel);
        List<String> hour = new ArrayList<String>();

        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hour.add("0" + i);
            } else {
                hour.add("" + i);
            }
        }

        startHour.setData(hour);
        endHour.setData(hour);

        try {
            String sleep = myPrefs.sleepData().get().trim();
            String[] split = sleep.split("-");

            startTime = Integer.parseInt(split[0].trim());
            endTime = Integer.parseInt(split[1].trim());

            L.d("strings:" + sleep + "---" + s);
            startHour.setSelected(startTime);
            endHour.setSelected(endTime);
        } catch (Exception e) {
            e.printStackTrace();
            L.d("转换异常");
        }
        startHour.setOnSelectListener(text -> startTime = Integer.parseInt(text));
        endHour.setOnSelectListener(text -> endTime = Integer.parseInt(text));
        builder.setView(v);
        final AlertDialog dialog = builder.show();
        ok.setOnClickListener(v1 -> {

            if (startTime == endTime) {
                RxToast.warning(getString(R.string.sleepNoSame));
                return;
            }

            circleProgressDialog.showDialog();
            MyBle.getInstance().setSleepDate(new int[]{startTime, endTime}, isSuccess -> {
                circleProgressDialog.dismiss();
                if (isSuccess) {
                    RxToast.success(getString(R.string.settingAlarmSuccess));
                    myPrefs.sleepData().put(startTime + "-" + endTime);
                    settingPalys.get(selectPosition).setText(startTime + ":00 - " + endTime + ":00");
                    adapter.notifyItemChanged(selectPosition);
                }
            });
            dialog.dismiss();
        });

        cancel.setOnClickListener(v12 -> dialog.dismiss());
    }


    int shakeValue;

    private void initShakeMode() {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.pop_progress, null, false);
        final RxSeekBar seekBar = (RxSeekBar) layout.findViewById(R.id.seekbar);

        shakeValue = myPrefs.SeekValue().get();
        seekBar.setValue(shakeValue);
        seekBar.setOnRangeChangedListener((view, min, max, isFromUser) -> {
            if (!isFromUser) return;
            shakeValue = (int) min;
            seekBar.setProgressDescription(takePhoto[shakeValue]);
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(layout).setNegativeButton(R.string.ok, (dialogInterface, i) -> {
            circleProgressDialog.showDialog();
            MyBle.getInstance().shakeTakePhotoVluse(shakeValue, isSuccess -> {
                circleProgressDialog.dismiss();
                myPrefs.SeekValue().put(shakeValue);
                RxToast.success(getString(R.string.settingAlarmSuccess));
                settingPalys.get(selectPosition).setText(takePhoto[shakeValue]);
                adapter.notifyItemChanged(selectPosition);
            });
        }).setCancelable(true).show();

    }

    private String sleepTime(String time) {
        String[] split = time.split("-");
        String str = split[0] + ":00 - " + split[1] + ":00";
        return str;
    }


}
