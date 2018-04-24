package com.lab.dxy.bracelet.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimer;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimerListener;
import com.lab.dxy.bracelet.adapter.AlarmAdapter;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/9
 */

@EActivity(R.layout.activity_setalarm)
public class AlarmSet extends BaseActivity {
    private AlarmAdapter adapter;
    private List<UserAlarmTab> all;
    private CircleProgressDialog circleProgressDialog;

    @ViewById
    RecyclerView alarmRecyclerView;
    @ViewById
    TextView Title;

    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;


    @Click
    void running() {
        onBackPressed();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Click
    void img_add() {
        if (all.size() >= 3) {
            RxToast.warning(getString(R.string.alarm_set_up, 3));
        } else {
            Intent intent = new Intent(AlarmSet.this, AlarmAddActivity_.class);
            intent.putExtra("isAdd", true);
            startActivity(intent);
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }

    @AfterViews
    void initView() {
        Title.setText(getString(R.string.Intelligence));
        circleProgressDialog = new CircleProgressDialog(this);
        circleProgressDialog.setText("");
        setAdapter();
        timer.startTimer();
    }


    MyPeriodTimer timer = new MyPeriodTimer(1000, 10000, new MyPeriodTimerListener() {
        @Override
        public void enterTimer() {
            adapter.notifyDataSetChanged();
        }
    });


    private void setAdapter() {
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        all = UserAlarmTab.getAll();
        L.d("闹钟个数：" + all.size());
        adapter = new AlarmAdapter(this, all);
        alarmRecyclerView.setAdapter(adapter);

        adapter.setRecyclerOnClickLisener(new AlarmAdapter.RecyclerOnClickLisener() {
            @Override
            public void OnClickitemLisener(int i) {
                Intent intent = new Intent(AlarmSet.this, AlarmAddActivity_.class);
                intent.putExtra("isAdd", false);
                intent.putExtra("addTime", all.get(i).getAddTime());
                intent.putExtra("position", i);
                startActivity(intent);
            }

            @Override
            public void OnLongClickItemLisener(int i) {
                showMyDialog(i);
            }
        });
    }


    private void showMyDialog(final int i) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.hint)).setMessage(this.getString(R.string.isDelete));
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (BleService.isConnected) {
                L.d("点击下标：" + i);
                if (i < all.size()) {
                    circleProgressDialog.showDialog();
                    MyBle.getInstance().sendAlarmPush(false, all.get(i), isSuccess -> {
                        RxToast.success(getString(R.string.settingAlarmSuccess));
                        circleProgressDialog.dismiss();
                        UserAlarmTab.deletByAddedTime(all.get(i).getAddTime());
                        adapter.removeData(i);
                    });
                }
            } else {
                RxToast.warning(getString(R.string.barDisConnect));
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }


    private List<UserAlarmTab> sortItem(List<UserAlarmTab> all) {
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                if (all.get(i).getAddTime() < all.get(j).getAddTime()) {
                    UserAlarmTab temp = all.get(i);
                    all.set(i, all.get(j));
                    all.set(j, temp);
                }
            }
        }
        return all;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
    }


    @Override
    protected void onStart() {
        super.onStart();
        setAdapter();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (circleProgressDialog != null)
            circleProgressDialog.dismiss();
        timer.stopTimer();
    }
}
