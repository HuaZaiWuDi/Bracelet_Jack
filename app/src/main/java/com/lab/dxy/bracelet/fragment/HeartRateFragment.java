package com.lab.dxy.bracelet.fragment;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.AnimUtils;
import com.lab.dxy.bracelet.Utils.ButtonUtils;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxTextUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.entity.SettingPlayItem;
import com.lab.dxy.bracelet.ui.HeartRateLineView;
import com.lab.dxy.bracelet.ui.HeartRateRound;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/11/24
 */
@EFragment(R.layout.fragment_hear_rate)
public class HeartRateFragment extends BaseFragment {

    public static synchronized Fragment getInstance() {
        return new HeartRateFragment_();
    }


    @ViewById
    TextView text_heartRate;
    @ViewById
    ImageView hearRate;
    @ViewById
    HeartRateRound hearRateRound;
    @ViewById
    RecyclerView mRecyclerView;
    @ViewById
    ImageView historyHeartRate;
    @ViewById
    HeartRateLineView mHeartRateLineView;
    @Pref
    MyPrefs_ myPrefs;
    @ViewById
    ScrollView scrollView;

    @Click
    void historyHeartRate() {
        AnimUtils.doAction(historyHeartRate);
    }


    @Click
    void btn_heartRate() {
        if (ButtonUtils.isFastDoubleClick(R.id.btn_heartRate, 8000)) {
            mHeartRateLineView.startAnimation();
            mHeartRateLineView.setVisibility(View.VISIBLE);
            ViewCompat.animate(hearRate).setDuration(8000).scaleX(0.8f).scaleY(0.8f).setInterpolator(new CycleInterpolator(10))
                    .setListener(new ViewPropertyAnimatorListener() {

                        @Override
                        public void onAnimationStart(final View view) {

                        }

                        @Override
                        public void onAnimationEnd(final View v) {
                            mHeartRateLineView.setVisibility(View.INVISIBLE);
                            Random random = new Random();
                            int nextInt = random.nextInt(42);
                            hearRateRound.setProgress(nextInt);
                            myPrefs.heartRate().put(nextInt + 80);
                            RxTextUtils.setHeartRate(getContext(), text_heartRate, nextInt + 80, Color.parseColor("#ffffff"));
                            settingPalys.add(new SettingPlayItem((nextInt + 80) + "", fromatDate(new Date())));
                            settingPalys = sortItem(settingPalys);     //实现list集合逆序排列
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationCancel(final View view) {
                        }
                    }).withLayer().start();
        }
    }

    int scrollX, scrollY;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            L.d("隐藏：HeartRateFragment");

            scrollX = scrollView.getScrollX();
            scrollY = scrollView.getScrollY();
        } else {
            L.d("显示：HeartRateFragment");
            scrollView.scrollTo(scrollX, scrollY);
        }
    }


    @AfterViews
    void initView() {
        hearRateRound.setProgress(0);

        RxTextUtils.setHeartRate(getContext(), text_heartRate, 0, Color.parseColor("#ffffff"));

        initRecycler();

    }

    private List<SettingPlayItem> settingPalys = new ArrayList<>();
    private CommonAdapter<SettingPlayItem> adapter;

    private void initRecycler() {
        initDate();

        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        adapter = new CommonAdapter<SettingPlayItem>(getActivity(), R.layout.item_paly_text, settingPalys) {
            @Override
            public void convert(ViewHolder holder, SettingPlayItem settingPlayItem) {
                holder.setVisible(R.id.img_icon, View.GONE);
                holder.setVisible(R.id.go, View.INVISIBLE);

                TextView textView = holder.getView(R.id.text_name);
                RxTextUtils.setHeartRate(textView, Integer.parseInt(settingPlayItem.getTitle()));
                holder.setText(R.id.param, settingPlayItem.getText());


                this.setOnItemClickListener((viewHolder, settingPlayItem1, position) -> {

                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }


    private List<SettingPlayItem> sortItem(List<SettingPlayItem> all) {
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                if (Utils.setParseDate(all.get(i).getText(), fromatDate + " HH:mm:ss").getTime() < Utils.setParseDate(all.get(j).getText(), fromatDate + " HH:mm:ss").getTime()) {
                    SettingPlayItem temp = all.get(i);
                    all.set(i, all.get(j));
                    all.set(j, temp);
                }
            }
        }
        return all;
    }


    private void initDate() {
        settingPalys.clear();
        settingPalys.add(new SettingPlayItem("97", fromatDate(new Date())));
        settingPalys.add(new SettingPlayItem("139", fromatDate(new Date())));
        settingPalys.add(new SettingPlayItem("80", fromatDate(new Date())));
        settingPalys.add(new SettingPlayItem("111", fromatDate(new Date())));
    }

    private String fromatDate(Date date) {
        return Utils.setFormat(date.getTime(), fromatDate + " HH:mm:ss", Utils.DATE);
    }

}
