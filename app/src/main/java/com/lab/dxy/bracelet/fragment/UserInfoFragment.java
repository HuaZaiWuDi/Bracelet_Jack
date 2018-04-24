package com.lab.dxy.bracelet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.activity.GuideActivity;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ui.PickerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/8/11
 */

@EFragment(R.layout.fragment_height)
public class UserInfoFragment extends BaseFragment {
    private int y = 0;
    private List<String> data = new ArrayList<>();
    @ViewById
    ImageView bg_img;
    @ViewById
    PickerView userHright;
    @ViewById
    TextView Title;
    @ViewById
    TextView unit;
    @Pref
    MyPrefs_ myprefs;


    public static Fragment getIntance(int flag) {
        UserInfoFragment_ fragment = new UserInfoFragment_();
        Bundle args = new Bundle();
        args.putInt("Flag", flag);
        fragment.setArguments(args);
        return fragment;
    }


    @AfterViews
    void initData() {

        userHright.setmColorText(0xffffff);

        Bundle bundle = getArguments();
        int flag = bundle.getInt("Flag", 0);


        switch (flag) {
            case GuideActivity.USER_HEIGHT:
                height();
                break;
            case GuideActivity.USER_WEIGHT:
                weight();
                break;
            case GuideActivity.USER_AGE:
                age();
                break;
            case GuideActivity.USER_ARM_STEPS:
                steps();
                break;
        }
    }

    private void steps() {

        Title.setText(getString(R.string.settingSteps));
//        unit.setText(getString(R.string.steps));
        unit.setVisibility(View.GONE);

        data.clear();
        for (int i = 0; i < 30000; i = i + 1000) {
            data.add(i + "");
        }

        userHright.setData(data);
        userHright.setSelected(Integer.parseInt("10"));

        userHright.setOnSelectListener(text -> {
            try {
                if (!TextUtils.isEmpty(text)) {
                    int steps = Integer.parseInt(text);
                    myprefs.runAim().put(steps);
                }

            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });
    }

    private void age() {
        Title.setText(getString(R.string.settingAge));
        unit.setText("Year");

        data.clear();

        String yyyy = Utils.setFormat(System.currentTimeMillis(), "yyyy", Utils.DATE);
        y = Integer.parseInt(yyyy);

        for (int i = 1950; i < y; i++) {
            data.add(i + "");
        }

        userHright.setData(data);
        userHright.setSelected(Integer.parseInt("45"));

        userHright.setOnSelectListener(text -> {
            try {
                if (!TextUtils.isEmpty(text)) {
                    int year = Integer.parseInt(text);
                    myprefs.userAge().put((y - year));
                }

            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });
    }

    private void weight() {
        Title.setText(getString(R.string.settingWeight));
        unit.setText("KG");

        data.clear();

        for (int i = 10; i < 200; i++) {
            data.add(i + "");
        }

        userHright.setData(data);
        userHright.setSelected(Integer.parseInt("50"));

        userHright.setOnSelectListener(text -> {
            try {
                if (!TextUtils.isEmpty(text))
                    myprefs.Weight().put(Integer.parseInt(text));
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });
    }

    private void height() {
        Title.setText(getString(R.string.settingHeight));
        unit.setText("CM");

        data.clear();

        for (int i = 50; i < 250; i++) {
            data.add(i + "");
        }

        userHright.setData(data);
        userHright.setSelected(Integer.parseInt("125"));

        userHright.setOnSelectListener(text -> {
            try {
                if (!TextUtils.isEmpty(text))
                    myprefs.Height().put(Integer.parseInt(text));
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });
    }

}
