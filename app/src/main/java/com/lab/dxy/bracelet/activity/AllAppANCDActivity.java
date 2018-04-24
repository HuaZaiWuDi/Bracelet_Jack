package com.lab.dxy.bracelet.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lab.dxy.bracelet.MyApplication;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ApplicationInfoUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.entity.AppInfo;
import com.lab.dxy.bracelet.entity.AppInfoItem;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import rx.Emitter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.lab.dxy.bracelet.MyApplication.aCache;


@EActivity(R.layout.activity_all_app_ancd)
public class AllAppANCDActivity extends BaseActivity {
    CircleProgressDialog circleProgressDialog;
    CommonAdapter adapter;
    AppInfoItem item;

    List<AppInfo> AppInfos;
    List<AppInfo> addApp;
    boolean isScroll = false;

    @ViewById
    RecyclerView mRecyclerView;
    @ViewById
    TextView Title;
    @ViewById
    ImageView img_right;
    @ViewById
    ImageView img_fab;


    @Click
    void running() {
        onBackPressed();
    }

    @Click
    void img_fab() {
        mRecyclerView.scrollToPosition(0);
    }

    @Click
    void img_right() {
        updateOpen();
        RxToast.success(getString(R.string.settingAlarmSuccess));
    }

    @AfterViews
    void init() {
        img_right.setImageResource(R.mipmap.success);
        img_fab.setVisibility(View.GONE);
        img_right.setVisibility(View.VISIBLE);
        Title.setText("社交提醒");
        circleProgressDialog = new CircleProgressDialog(this);
        circleProgressDialog.setText("正在加载...");
        circleProgressDialog.setTextColor(getResources().getColor(R.color.white));

//        aCache.clear();

        item = (AppInfoItem) aCache.getAsObject("AppInfo");
        AppInfoItem addAppItem = (AppInfoItem) aCache.getAsObject("addApp");

        if (addAppItem != null)
            addApp = addAppItem.getAppInfos();
        if (addApp == null)
            addApp = new ArrayList<>();

        if (item != null) {
            AppInfos = item.getAppInfos();
            initRecyclerView();
        } else {
            doRx();
        }
    }


    private void doRx() {
        circleProgressDialog.showDialog();

        Observable.create((Action1<Emitter<List<AppInfo>>>) listEmitter -> {
            List<AppInfo> allNonsystemProgramInfo = ApplicationInfoUtil.getAllNonsystemProgramInfo(MyApplication.getmContext());
            listEmitter.onNext(allNonsystemProgramInfo);
        }, Emitter.BackpressureMode.DROP)
                .subscribeOn(Schedulers.io())
                .doOnNext(appInfos -> {
                    AppInfos = appInfos;
                    item = new AppInfoItem(appInfos);
                    aCache.put("AppInfo", item);
                })
                .retry(1)//出现异常重新发送一次
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<AppInfo>>() {
                    @Override
                    public void onCompleted() {
                        circleProgressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        L.d("异常：" + e.fillInStackTrace());
                        RxToast.error(getString(R.string.settingFail));
                        circleProgressDialog.dismiss();
                    }

                    @Override
                    public void onNext(List<AppInfo> appInfos) {
                        initRecyclerView();
                    }

                });

    }


    private void initRecyclerView() {
//        AppInfos = sortItem(AppInfos);

        for (AppInfo infoApp : addApp) {
            //防止ConcurrentModificationException异常（并发异常）
            ListIterator<AppInfo> iterator = AppInfos.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().packageName.equals(infoApp.packageName)) {
                    iterator.remove();
                }
            }
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        adapter = new CommonAdapter<AppInfo>(this, R.layout.item_paly_text, AppInfos) {
            @Override
            public void convert(ViewHolder holder, AppInfo appInfo) {
                holder.setText(R.id.text_name, appInfo.appName);

                Glide.with(AllAppANCDActivity.this)
                        .load(appInfo.appIcon)
                        .error(R.mipmap.ic_launcher)
                        .crossFade(1000)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into((ImageView) holder.getView(R.id.img_icon));

                holder.setImageResource(R.id.go, appInfo.isOpen ? R.mipmap.slip_switch_on : R.mipmap.slip_switch_off);

                this.setOnItemClickListener((viewHolder, appInfo1, position) -> {

                    appInfo1.isOpen = !appInfo1.isOpen;
                    adapter.notifyItemChanged(position);
                });
            }
        };
        mRecyclerView.setAdapter(adapter);

        Animation animationIn = AnimationUtils.loadAnimation(this, R.anim.layout_in);
        Animation animationOut = AnimationUtils.loadAnimation(this, R.anim.layout_out);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) < 10) return;
                if (dy < 0 && !isScroll) {
                    isScroll = true;
                    img_fab.startAnimation(animationIn);
                    img_fab.setVisibility(View.VISIBLE);
                } else if (dy > 0 && isScroll) {
                    isScroll = false;
                    img_fab.startAnimation(animationOut);
                    img_fab.setVisibility(View.GONE);
                }
            }
        });

    }


    private List<AppInfo> sortItem(List<AppInfo> all) {
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                if (!all.get(i).isOpen) {
                    AppInfo temp = all.get(i);
                    all.set(i, all.get(j));
                    all.set(j, temp);
                }

            }
        }
        return all;
    }


    public void updateOpen() {
        boolean isChanged = false;

        if (AppInfos == null || addApp == null) {
            RxToast.error(getString(R.string.settingFail));
            return;
        }

        for (AppInfo info : AppInfos) {
            if (info.isOpen) {
                isChanged = true;
                addApp.add(info);
            }
        }

        //覆盖保存当前缓存
        item.setAppInfos(AppInfos);
        aCache.put("AppInfo", item);


        //添加到新的缓存
        AppInfoItem addAppItem = new AppInfoItem(addApp);
        aCache.put("addApp", addAppItem);
        RxToast.success(getString(isChanged ? R.string.settingAlarmSuccess : R.string.noChange));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (circleProgressDialog != null) {
            circleProgressDialog.dismiss();
        }
        super.onDestroy();
    }
}
