package com.lab.dxy.bracelet.fragment;

import android.widget.ImageView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.AnimUtils;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;


/**
 * A placeholder fragment containing a simple view.
 */

@EFragment(R.layout.fragment_guide)
public class UserSexFragment extends BaseFragment {

    private boolean isMan = true;

    @ViewById
    ImageView bg_img;
    @ViewById
    ImageView man;
    @ViewById
    ImageView woman;
    @Pref
    MyPrefs_ myPrefs;

    @Click
    void man() {
        AnimUtils.navigateWithRippleCompat(getActivity(), man, R.color.textBlue);
        isMan = true;
        sexSelect();
    }

    @Click
    void woman() {
        AnimUtils.navigateWithRippleCompat(getActivity(), man, R.color.mediumvioletred);
        isMan = false;
        sexSelect();
    }


    @AfterViews
    void initData() {
//        Glide.with(this)
//                .load(R.drawable.bg_star)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)//缓存机制是只保存在缓存里面
//                .bitmapTransform(new BlurTransformation(getActivity(), 5, 1))//第二哥参数是高斯模糊程度（0-25），第三个是缩放比例不缩放未1
//                .into(bg_img);

        sexSelect();

    }


    public void sexSelect() {
        if (isMan) {
            myPrefs.userSex().put(0);
            man.setImageResource(R.mipmap.man_select);
            woman.setImageResource(R.mipmap.woman_unselect);
        } else {
            myPrefs.userSex().put(1);
            man.setImageResource(R.mipmap.man_unselect);
            woman.setImageResource(R.mipmap.woman_select);
        }
    }

}