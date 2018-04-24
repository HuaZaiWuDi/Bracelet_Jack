package com.lab.dxy.bracelet.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.AnimUtils;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.fragment.ANCSFragment;
import com.lab.dxy.bracelet.fragment.AboutAppFragment;
import com.lab.dxy.bracelet.fragment.AboutFragment;
import com.lab.dxy.bracelet.fragment.HelpFragment;
import com.lab.dxy.bracelet.fragment.SettingFragment;
import com.lab.dxy.bracelet.fragment.findFragment_;


public class BaseByFragmentActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
        setContentView(R.layout.activity_base_by_fragment);

        ImageView back = (ImageView) findViewById(R.id.running);
        final ImageView img_right = (ImageView) findViewById(R.id.img_right);
        TextView Title = (TextView) findViewById(R.id.Title);

        back.setOnClickListener(view -> onBackPressed());
        final int type = getIntent().getIntExtra("type", 0);

        img_right.setOnClickListener(view -> {
            if (type == 6) {
                Intent intent = new Intent(BaseByFragmentActivity.this, AllAppANCDActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                AnimUtils.navigateWithRippleCompat(BaseByFragmentActivity.this, intent, img_right);
            }
        });

        Fragment fragment = null;
        switch (type) {
            case 3:
                Title.setText(getString(R.string.action_settings));
                fragment = SettingFragment.getInstance();
                break;
            case 4:
                Title.setText(getString(R.string.aboutUs));
                fragment = AboutFragment.getInstance();
                break;
            case 5:
                Title.setText(getString(R.string.help));
                fragment = HelpFragment.getInstance();
                break;
            case 6:
                img_right.setVisibility(View.VISIBLE);
                Title.setText(getString(R.string.ANCS));
                fragment = ANCSFragment.getInstance();
                img_right.setImageResource(R.mipmap.add);
                break;
            case 0:
                Title.setText(getString(R.string.aboutUs));
                fragment = AboutAppFragment.getInstance();
                break;
            case 1:
                Title.setText(getString(R.string.sleepData));
                fragment = new findFragment_();
                break;
        }
        if (fragment != null)
            getSupportFragmentManager().beginTransaction().replace(R.id.mFrameLayout, fragment).commitAllowingStateLoss();
    }
}
