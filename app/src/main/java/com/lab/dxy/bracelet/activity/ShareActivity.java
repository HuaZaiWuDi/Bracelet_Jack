package com.lab.dxy.bracelet.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.fragment.ShareAppFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;


@EActivity(R.layout.activity_share)
public class ShareActivity extends BaseActivity {


    @Extra
    int type;

    @AfterViews
    void initView() {

        Fragment fragment = ShareAppFragment.getIntance(type);
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_share, fragment).commitAllowingStateLoss();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setHindStatusBar(true).process();
//        //滑动退出 1.extends SwipeBackActivity 2.主题添加 <item name="android:windowIsTranslucent">true</item>
//        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
//        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_RIGHT);
    }

}
