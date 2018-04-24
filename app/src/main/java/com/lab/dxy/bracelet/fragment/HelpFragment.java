package com.lab.dxy.bracelet.fragment;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.adapter.HelpCardAdapter;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.entity.CardItem;
import com.lab.dxy.bracelet.ui.recyclerview.cardstack.RxCardStackView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.util.ChartUtils;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/1
 */
@EFragment(R.layout.fragment_help)
public class HelpFragment extends BaseFragment {

    private List<CardItem> data = new ArrayList<>();
    String[] title;
    String[] text;

    public static synchronized HelpFragment getInstance() {
        return new HelpFragment_();
    }

    @ViewById
    RxCardStackView mRxCardStackView;


    @AfterViews
    void init() {
        title = getActivity().getResources().getStringArray(R.array.help_title);
        text = getActivity().getResources().getStringArray(R.array.hlep_text);
        initRxCard();
    }

    private void initRxCard() {
        initData();
        HelpCardAdapter adapter = new HelpCardAdapter(getActivity());
        adapter.setData(data);
        mRxCardStackView.setAdapter(adapter);
        mRxCardStackView.setItemExpendListener(expend -> {

        });
    }

    private void initData() {
        data.clear();
        for (int i = 0; i < title.length; i++) {
            data.add(new CardItem(ChartUtils.nextColor(), title[i], text[i]));
        }
    }
}
