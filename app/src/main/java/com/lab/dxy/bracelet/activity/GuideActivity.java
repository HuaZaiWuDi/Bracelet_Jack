package com.lab.dxy.bracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.fragment.UserInfoFragment;
import com.lab.dxy.bracelet.fragment.UserSexFragment_;


public class GuideActivity extends BaseActivity {
    public static final int USER_INFO = 0;
    public static final int USER_HEIGHT = 1;
    public static final int USER_WEIGHT = 2;
    public static final int USER_AGE = 3;
    public static final int USER_ARM_STEPS = 4;
    ImageView bg_img;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        StatusBarUtils.from(this).setTransparentStatusbar(true).process();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        bg_img = (ImageView) findViewById(R.id.bg_img);
//        Glide.with(this)
//                .load(R.drawable.bg_star)
//                .error(R.drawable.gradient_blue)
//                .thumbnail(0.1f)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)//缓存机制是只保存在缓存里面
//                .bitmapTransform(new BlurTransformation(this, 5, 1))//第二哥参数是高斯模糊程度（0-25），第三个是缩放比例不缩放未1
//                .dontAnimate()
//                .into(bg_img);


        ImageView fab = (ImageView) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

            final Intent intent = new Intent(GuideActivity.this, MainActivity_.class);
            intent.putExtra("isFirst", "isFirst");
            startActivity(intent);
            finish();
        });

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new UserSexFragment_();
                case 1:
                    return UserInfoFragment.getIntance(USER_HEIGHT);
                case 2:
                    return UserInfoFragment.getIntance(USER_WEIGHT);
                case 3:
                    return UserInfoFragment.getIntance(USER_AGE);
                case 4:
                    return UserInfoFragment.getIntance(USER_ARM_STEPS);

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 5;
        }

    }


}
