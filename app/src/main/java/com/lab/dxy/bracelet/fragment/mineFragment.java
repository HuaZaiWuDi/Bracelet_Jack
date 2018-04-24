package com.lab.dxy.bracelet.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxBus;
import com.lab.dxy.bracelet.activity.AddDervice_;
import com.lab.dxy.bracelet.activity.BaseByFragmentActivity;
import com.lab.dxy.bracelet.activity.CameraActivity_;
import com.lab.dxy.bracelet.activity.MainActivity;
import com.lab.dxy.bracelet.activity.UserData_;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.core.GetMarkClass;
import com.lab.dxy.bracelet.entity.SettingPlayItem;
import com.lab.dxy.bracelet.entity.StepsItem;
import com.lab.dxy.bracelet.entity.spl.UserStepsTab;
import com.lab.dxy.bracelet.ui.BasePopupWindow;
import com.lab.dxy.bracelet.ui.CircleImageView;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import rx.Subscription;

import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.service.BleService.isConnected;


/**
 * Created by 华 on 2017/5/4.
 */
@EFragment(R.layout.fragment_mine)
public class mineFragment extends BaseFragment {

    private CircleProgressDialog circleProgressDialog;
    BasePopupWindow window;
    private Subscription subscription;

    @ViewById
    TextView userName;
    @ViewById
    CircleImageView userImg;
    @ViewById
    TextView aimRunText;
    @ViewById
    TextView aimWeightText;
    @ViewById
    TextView averageSteps;
    @ViewById
    TextView sumStepsToKm;
    @ViewById
    TextView daysCount;
    @ViewById
    TextView text_myDevice;


    @ViewById
    RelativeLayout aimWeight;
    @ViewById
    RelativeLayout aimRun;
    @ViewById
    ImageView bg_userInfo;

    @ViewById
    RecyclerView mRecyclerView;
    @ViewById
    LinearLayout prent;
    @ViewById
    ScrollView scrollView;


    @Pref
    MyPrefs_ MyPrefs;
    @Pref
    BraPrefs_ braPrefs;

    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime2() {
        init();
    }


    @Click
    void addDervice() {
        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), AddDervice_.class));
            getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
    }

    @Click
    void userData() {
        startActivity(new Intent(getActivity(), UserData_.class));
        getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Click
    void userImg() {
        startActivity(new Intent(getActivity(), UserData_.class));
        getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }


    @AfterViews
    void initView() {
        aimWeight.setVisibility(View.GONE);
        aimRun.setVisibility(View.GONE);

        photoMode = getResources().getStringArray(R.array.photoMode);
        functionTitle = getResources().getStringArray(R.array.setting);

        circleProgressDialog = new CircleProgressDialog(getActivity());
        circleProgressDialog.setText("");
        initRecyclerView();

        popInit();

        subscription = RxBus.getInstance().doSubscribe(StepsItem.class, stepsItem -> {
            settingPalys.get(3).setOpen(true);
            adapter.notifyItemChanged(3);
        });

    }

    private String[] functionTitle;
    private int[] settingIcon = {R.mipmap.ico_idbroadcast_x, R.mipmap.icon_camera_green_482x, R.mipmap.icon_setting_482x, R.mipmap.about, R.mipmap.help};
    private String[] photoMode;

    private List<SettingPlayItem> settingPalys = new ArrayList<>();
    private CommonAdapter<SettingPlayItem> adapter;


    private void initData() {
        /**
         *  <string-array name="setting">
         <item>查找手环</item>
         <item>智能拍照</item>
         <item>设置</item>
         <item>关于我们</item>
         <item>帮助</item>
         </string-array>
         * */
        settingPalys.clear();
        for (int i = 0; i < functionTitle.length; i++) {
            settingPalys.add(new SettingPlayItem(functionTitle[i], false, settingIcon[i]));
        }
        settingPalys.get(3).setOpen(MainActivity.appUpdate || MainActivity.otaUpdate);
    }


    private void initRecyclerView() {
        initData();
        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        adapter = new CommonAdapter<SettingPlayItem>(getActivity(), R.layout.item_paly_text, settingPalys) {
            @Override
            public void convert(ViewHolder holder, SettingPlayItem settingPlayItem) {
                holder.setText(R.id.text_name, settingPlayItem.getTitle());
                holder.setImageResource(R.id.img_icon, settingPlayItem.getIcon());
                holder.setImageResource(R.id.go, settingPlayItem.isOpen() ? R.mipmap.news : R.mipmap.btn_arrow1_w36_n2x);
                this.setOnItemClickListener((viewHolder, settingPlayItem1, position) -> switchPosition(position));
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    private void switchPosition(int position) {
        Intent intent = new Intent(getActivity(), BaseByFragmentActivity.class);
        switch (position) {
            case 0:
                if (isConnected)
                    MyBle.getInstance().find();
                else
                    RxToast.warning(getString(R.string.barDisConnect));
                break;
            case 1:
                window.showAtLocation(prent, Gravity.BOTTOM, 0, 0);
                break;
            case 2:
                //跳转设置界面

                intent.putExtra("type", 3);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);

                break;
            case 3:
                intent.putExtra("type", 4);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);

                //跳转关于我们，以及应用升级
                break;
            case 4:
                //跳转帮助界面
                intent.putExtra("type", 5);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);

                break;
        }
    }


    //摇一摇拍照
    void shake(final int type) {
        if (Build.VERSION.SDK_INT >= 23) {
            new RxPermissions(getActivity())
                    .request(Manifest.permission.CAMERA)
                    .subscribe(aBoolean -> {
                        if (aBoolean) {
                            L.d("权限请求成功");
                            Intent intent = new Intent(getActivity(), CameraActivity_.class);
                            intent.putExtra("type", type);
                            startActivity(intent);
                        } else {
                            L.d("权限请求失败");
                            RxToast.error(getString(R.string.NoPermission));
                        }
                    });
        } else {
            Intent intent = new Intent(getActivity(), CameraActivity_.class);
            intent.putExtra("type", type);
            startActivity(intent);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    int scrollX, scrollY;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            L.d("隐藏：minefragment");
            scrollX = scrollView.getScrollX();
            scrollY = scrollView.getScrollY();
        } else {
            L.d("显示：minefragment");
            scrollView.scrollTo(scrollX, scrollY);
            init();
            initData();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }


    private void init() {
        String path = MyPrefs.userImg_Url().get();
        /**
         * 圆形：CropCircleTransformation
         方形：CropSquareTransformation
         圆角：RoundedCornersTransformation
         颜色覆盖：ColorFilterTransformation
         置灰：GrayscaleTransformation
         毛玻璃：BlurTransformation
         *
         * **/

        Glide.clear(userImg);
        Glide.with(this)
                .load(path)
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(getActivity()))//圆角图片
                .crossFade(1000)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(userImg, 10));

        if (TextUtils.isEmpty(braPrefs.bleName().get())) {
            text_myDevice.setText(R.string.AddDevice);
        } else {
            text_myDevice.setText(braPrefs.bleName().get());
        }

        int steps = 0;
        int standard = 0;
        List<UserStepsTab> all = UserStepsTab.getAll();
        if (all.size() != 0) {
            for (int i = 0; i < all.size(); i++) {
                steps += all.get(i).getSteps();
                if (all.get(i).getSteps() >= MyPrefs.runAim().get()) {
                    standard++;
                }
            }

            GetMarkClass markClass = new GetMarkClass(steps, MyPrefs.Weight().get(), MyPrefs.Height().get());
            int km = (int) markClass.getKm();

            L.d("总步数:" + steps + "---distanceAll:" + km);
            averageSteps.setText(steps / all.size() + "");
            sumStepsToKm.setText(km + "");
            daysCount.setText(standard + "");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (circleProgressDialog != null)
            circleProgressDialog.dismiss();
        if (window != null)
            window.dismissPop();
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // 智能拍照
    ///////////////////////////////////////////////////////////////////////////
    private void popInit() {
        window = new BasePopupWindow(getActivity());
        window.initPop(getString(R.string.Photograph), getString(R.string.ChoosePhotographMode), photoMode);
        window.setOnItemClickLisetener((position, text) -> shake(position));
    }


}
