package com.lab.dxy.bracelet.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.RxToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;

import rx.Subscription;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/5/18
 */

@EActivity(R.layout.activity_image)
public class ImageActivity extends BaseActivity {

    public String[] Mypermission = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS
    };

    @Extra
    String path;

    @Extra
    int position;

    @ViewById
    ImageView image;
    @ViewById
    ImageView splashImg;
    @ViewById
    TextView splashText;

    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;

    @ViewById
    RelativeLayout title;
    @ViewById
    RelativeLayout parent;

    @Click
    void back() {
        onBackPressed();
    }


    @Click
    void delete() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            RxToast.success(getString(R.string.deleteSuccess));
            finish();
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }

    @AfterViews
    void initView() {
        setTheme(R.style.AppTheme);

        if (BleService.isConnected) {
            startActivity(new Intent(ImageActivity.this, MainActivity_.class));
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            finish();
            return;
        }

        if (path != null) {
            takePic();
        } else {
            setSplash();
        }
    }

    private void takePic() {
        splashImg.setVisibility(View.GONE);
        splashText.setVisibility(View.GONE);

        title.setAlpha(0.9f);
        L.d("ImageActivity_path:" + path);
        Glide.with(this)
                .load(new File(path))
                .skipMemoryCache(true)
                .placeholder(R.mipmap.no_photo)
                .into(image);
    }

    private void setSplash() {

        Glide.with(this).load(R.drawable.pic_bg_ocean).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(image);
        splashText.setText(Contents.barFilterName + getString(R.string.bleBra));

        splashImg.startAnimation(AnimationUtils.loadAnimation(this, R.anim.layout_up));

        splashImg.getAnimation().setFillAfter(true);

        splashText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.text_selce));

        title.setVisibility(View.GONE);
        setpPrmisstion();

        if (!checkNetwork()) {
            L.e("!checkNetwork()");
            toInternetActivity();
        }
    }

    private void showToMainActivity() {
        Handler hander = new Handler();
        hander.postDelayed(() -> {
            if (braPrefs.isFirstIn().get()) {
                braPrefs.edit().isFirstIn().put(false).apply();
                startActivity(new Intent(ImageActivity.this, GuideActivity.class));
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                finish();
            } else {
                startActivity(new Intent(ImageActivity.this, MainActivity_.class));
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                finish();
            }
        }, 2000);
    }

    Subscription subscribe;

    private void setpPrmisstion() {
        //定位权限
        if (Build.VERSION.SDK_INT >= 23) {
            subscribe = new RxPermissions(this)
                    .request(Mypermission)
                    .subscribe(aBoolean -> {
                        if (aBoolean) {
                            L.d("权限请求成功");
                            try {
//                                    UserInfoManager userInfoManager = new UserInfoManager(ImageActivity.this, UserInfoManager.BAIDU_MODE);
//                                    userInfoManager.getLocation();
                                showToMainActivity();
                            } catch (Exception e) {
                                e.fillInStackTrace();
                            }
                        } else {
                            L.d("权限请求失败");
                            RxToast.error(getString(R.string.openLoactionpermission));
                            showToMainActivity();
                        }
                    });
        } else {
            showToMainActivity();
        }

    }

    @Override
    protected void onDestroy() {
        if (subscribe != null && subscribe.isUnsubscribed())
            subscribe.unsubscribe();
        super.onDestroy();
    }

    ConnectivityManager conn;

    //检查网络连接
    private boolean checkNetwork() {
        conn = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = conn.getActiveNetworkInfo();
        if (net != null && net.isConnected()) {
            if (net.isAvailable()) {
                return true;
            }
        }
        RxToast.error(getString(R.string.network_fail));
        conn = null;
        return false;
    }


    //无网络连接时，跳转到网络链接
    private void toInternetActivity() {
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle(getString(R.string.NoNet)).setMessage(getString(R.string.settingNet));
        b.setPositiveButton(R.string.ok, (dialog, whichbutton) -> {
            // TODO Auto-generated method stub
            if (Build.VERSION.SDK_INT > 10) {
                //3.0以上需要打开的设置界面
                ImageActivity.this.startActivity(new Intent(Settings.ACTION_SETTINGS));
            } else {
                //3.0以下需要打开的设置页面
                ImageActivity.this.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
//                showToMainActivity();
        }).setNeutralButton(R.string.cancel, (dialog, whichButton) -> dialog.cancel()).create();
        b.show();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setTransparentStatusbar(true).process();
    }
}
