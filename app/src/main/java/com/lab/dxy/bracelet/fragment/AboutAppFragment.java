package com.lab.dxy.bracelet.fragment;

import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/6
 */
@EFragment(R.layout.fragment_about_app)
public class AboutAppFragment extends BaseFragment {

    public static synchronized AboutAppFragment getInstance() {
        return new AboutAppFragment_();
    }

    @ViewById
    TextView aboutOurs;

    @AfterViews
    void init() {
        aboutOurs.setText(getString(R.string.callOurs, "0755-28168579", "dxylab@dxytech.com "));
    }

}
