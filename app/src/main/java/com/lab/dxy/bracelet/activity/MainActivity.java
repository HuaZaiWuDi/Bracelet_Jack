package com.lab.dxy.bracelet.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.lab.dxy.bracelet.BuildConfig;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxBus;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimer;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.core.UpdateManager;
import com.lab.dxy.bracelet.entity.StepsItem;
import com.lab.dxy.bracelet.entity.TabEntity;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.fragment.HeartRateFragment;
import com.lab.dxy.bracelet.fragment.SleepFragment;
import com.lab.dxy.bracelet.fragment.exercisefragment_;
import com.lab.dxy.bracelet.fragment.mineFragment_;
import com.lab.dxy.bracelet.fragment.playFrament_;
import com.lab.dxy.bracelet.service.BleService_;
import com.lab.dxy.bracelet.ui.PowerIconView;
import com.lab.dxy.bracelet.ui.RxToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.ListIterator;

import me.shaohui.shareutil.LoginUtil;
import me.shaohui.shareutil.login.LoginListener;
import me.shaohui.shareutil.login.LoginPlatform;
import me.shaohui.shareutil.login.LoginResult;
import rx.Subscription;

import static com.inuker.bluetooth.library.Constants.ACTION_CHARACTER_CHANGED;
import static com.inuker.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.MyApplication.isFirstIn;
import static com.lab.dxy.bracelet.fragment.exercisefragment.DATA_INIT;
import static com.lab.dxy.bracelet.fragment.exercisefragment.isFirstSyn;
import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;
import static com.lab.dxy.bracelet.service.BleService.isBg;
import static com.lab.dxy.bracelet.service.BleService.isConnected;


/**
 * 项目名称： Bracelet
 * 类描述： 主界面
 * 创建时间：
 * 创建人：Jack
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    Intent intent;
    private Fragment mFragmentNow;
    private FragmentManager fm;

    public static boolean appUpdate = false;
    public static boolean otaUpdate = false;
    Fragment exercisefragment = new exercisefragment_();
    //    Fragment findFragment = new findFragment_();
    Fragment playFrament = new playFrament_();
    Fragment mineFragment = new mineFragment_();
    Fragment heartRateFragment = HeartRateFragment.getInstance();
    Fragment sleepFragment = SleepFragment.getInstance();
    UpdateManager upgradeManager;

    @Extra
    String isFirst;

    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;

    @ViewById
    ImageView img_state;
    @ViewById
    ImageView img_find;
    @ViewById
    ImageView img_play;
    @ViewById
    ImageView img_mine;
    @ViewById
    ImageView running;
    @ViewById
    PowerIconView power;

    @ViewById
    TextView text_state;
    @ViewById
    TextView text_find;
    @ViewById
    TextView text_play;
    @ViewById
    TextView Title;
    @ViewById
    TextView text_mine;
    @ViewById
    TextView connectedText;
    @ViewById
    ProgressBar progressBar_header;
    @ViewById
    CommonTabLayout mCommonTabLayout;

    //5a0d009001000000000000000000000000000000
    @Receiver(actions = ACTION_CHARACTER_CHANGED)
    protected void onActionCharacterChanged(@Receiver.Extra(EXTRA_BYTE_VALUE) byte[] value) {
        if (value[1] == 0x0d && value[3] == (byte) 0x90) {
            power.setCharge(value[4] == 0x01, myPrefs.powerValue().get());
        }
        if (value[1] == 0x0d && value[3] == (byte) 0x80) {
            myPrefs.edit().powerValue().put((int) value[4]).apply();
            power.setValue((float) value[4]);
        }
    }


    @Receiver(actions = ACTION_BLE_CONNECTED)
    protected void onActionConnected() {
        if (progressDialog != null)
            progressDialog.dismiss();

        if (!isBg)
            RxToast.info(getString(R.string.barConnect));
    }


    //监听系统蓝牙开启
    @Receiver(actions = BluetoothAdapter.ACTION_STATE_CHANGED)
    void blueToothisOpen(@Receiver.Extra(BluetoothAdapter.EXTRA_STATE) int state) {
        if (state == BluetoothAdapter.STATE_OFF) {
            RxToast.info(getString(R.string.bleClose));
            Utils.broadUpdate(ACTION_BLE_DISCONNECT);
        } else if (state == BluetoothAdapter.STATE_ON) {
            isConnected = false;
            MyBle.getInstance().getBleManagerKit().connectDevice();
        }
    }


    @Receiver(actions = ACTION_BLE_DISCONNECT)
    protected void onActionDisconnected() {
        DATA_INIT = false;
        power.setCharge(false, myPrefs.powerValue().get());
    }


    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime() {

        myPrefs.isFirstSynTime().put(System.currentTimeMillis());
        isFirstSyn = false;
        DATA_INIT = true;
        updateTab();

        if (isFirstIn) {
            isFirstIn = false;
            MyBle.getInstance().queryDeviceVersion(isSuccess -> {
                detectOtaUpgrade();
            });
        }
    }


    @Receiver(actions = Intent.ACTION_LOCALE_CHANGED)
    protected void onLocaleChanged() {
        finish();
    }


    @Click
    void Connected() {
        startActivity(new Intent(MainActivity.this, AddDervice_.class));
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }


    @Click
    void running() {

        Intent intent = new Intent(this, ShareActivity_.class);
        if (Title.getText().toString().equals(getString(R.string.state))) {
            intent.putExtra("type", 1);
        } else if (Title.getText().toString().equals(getString(R.string.find)))
            intent.putExtra("type", 2);
        else if (Title.getText().toString().equals(getString(R.string.heartRate))) {
            intent.putExtra("type", 0);
        } else {
            startActivity(new Intent(this, AddDervice_.class));
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            return;
        }

        startActivity(intent);
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }


    @Click
    void Title() {
        if (BuildConfig.LOG_DEBUG && isConnected) {
            upgradeManager.updateOTA(Environment.getExternalStorageDirectory() + Contents.DOWNLOAD_FOLDER + "LEGEND_Bracelet_TW64_V1302.bin");
        }
        LoginUtil.login(this, LoginPlatform.WX, new LoginListener() {
            @Override
            public void loginSuccess(LoginResult result) {
                L.d("登录成功");
            }

            @Override
            public void loginFailure(Exception e) {
                L.d("登录失败");
            }

            @Override
            public void loginCancel() {
                L.d("登录关闭");
            }
        });
    }


    String[] bottomTitle;
    int[] mIconUnselectIds = {R.mipmap.ico_tabbar_status_n2x, R.mipmap.ico_tabbar_find_n2x, R.mipmap.icon_unselect_heart, R.mipmap.ico_tabbar_play_n2x, R.mipmap.ico_tabbar_my_n2x};
    int[] mIconSelectIds = {R.mipmap.ico_tabbar_status_a2x, R.mipmap.ico_tabbar_find_a2x, R.mipmap.icon_select_heart, R.mipmap.ico_tabbar_play_a2x, R.mipmap.ico_tabbar_my_a2x};
    ArrayList<CustomTabEntity> tabs;

    @AfterViews
    protected void initView() {
        power.setVisibility(View.VISIBLE);
        running.setImageResource(R.mipmap.share);
        bottomTitle = getResources().getStringArray(R.array.bottomTitle);
        Title.setText(bottomTitle[0]);


        bottomTab();

        power.setValue((float) myPrefs.powerValue().get());

        Subscription subscription1 = RxBus.getInstance()
                .doSubscribe(StepsItem.class, stepsItem -> mCommonTabLayout.hideMsg(tabs.size() - 1));

        RxBus.getInstance().addSubscription(StepsItem.class, subscription1);


        //是否是今天第一次进入app
        Long aLong = myPrefs.isFirstSynTime().get();

        String oldTime = Utils.setFormat(aLong, fromatDate, Utils.DATE);
        String newTime = Utils.setFormat(System.currentTimeMillis(), fromatDate, Utils.DATE);

        if (oldTime.equals(newTime)) {
            isFirstSyn = false;
        }


    }

    private void bottomTab() {
        tabs = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            if (i != 3) {
                tabs.add(new TabEntity(bottomTitle[i], mIconSelectIds[i], mIconUnselectIds[i]));
            }
        }


        mCommonTabLayout.setTabData(tabs);
        updateTab();

        mCommonTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                switchTab(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

    }

    private void updateTab() {
        if (myPrefs.deviceVersion().get() == 0x03) {

            //防止ConcurrentModificationException异常（并发异常）
            ListIterator<CustomTabEntity> iterator = tabs.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().getTabSelectedIcon() == R.mipmap.icon_select_heart) {
                    iterator.remove();
                    mCommonTabLayout.setTabData(tabs);
                }
            }
            L.d("tabs.size():" + tabs.size());
        }
    }


    private void switchTab(int position) {
        int type = tabs.get(position).getTabSelectedIcon();
        Title.setText(tabs.get(position).getTabTitle());
        switch (type) {
            case R.mipmap.ico_tabbar_status_a2x:
                running.setImageResource(R.mipmap.share);
                switchFragment(mFragmentNow, exercisefragment);
                break;
            case R.mipmap.ico_tabbar_find_a2x:
                running.setImageResource(R.mipmap.share);
                switchFragment(mFragmentNow, sleepFragment);
                break;
            case R.mipmap.icon_select_heart:
                running.setImageResource(R.mipmap.share);
                switchFragment(mFragmentNow, heartRateFragment);
                break;
            case R.mipmap.ico_tabbar_play_a2x:
                running.setImageResource(R.mipmap.bracelet);
                switchFragment(mFragmentNow, playFrament);
                break;
            case R.mipmap.ico_tabbar_my_a2x:
                running.setImageResource(R.mipmap.bracelet);
                switchFragment(mFragmentNow, mineFragment);
                break;
        }

    }


    private void setDefaultFragment() {
        fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.framelayout, exercisefragment);
        transaction.commit();
        mFragmentNow = exercisefragment;
    }

    private void switchFragment(Fragment from, Fragment to) {
        if (from != to) {
            mFragmentNow = to;
            FragmentTransaction transaction = fm.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.framelayout, to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(from).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
        intent = new Intent(this, BleService_.class);
        startService(intent);

        L.d("onCreate");
        if (isFirst != null) {
            startActivity(new Intent(MainActivity.this, AddDervice_.class));
            overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }
        Utils.getVersionName(this);
        upgradeManager = new UpdateManager(this);
        detectUpgrade();

        setDefaultFragment();

    }

    /**
     * Check if service is running.
     *
     * @param serviceClass
     * @return
     */
    public static boolean isServiceRunning(@NonNull Class<?> serviceClass, @NonNull Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    ProgressDialog dialog;

    @Override
    protected void onStart() {
        super.onStart();
        L.d("onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        L.d("onStop");
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        L.d("onDestroy");
        try {
            if (dialog != null)
                dialog.dismiss();
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            RxBus.getInstance().unSubscribe(StepsItem.class);
        } catch (Exception e) {
            e.printStackTrace();
            L.d("关闭Service异常");
        }
    }

    private void detectUpgrade() {
        upgradeManager.setOnAppUpgradeListener(new UpdateManager.onAppUpgradeListener() {
            @Override
            public void onFindNewVersion(String downloadUrl, String newVersionInfo) {
                L.d("onFindNewVesion: " + downloadUrl);
                showUpgradeDialog(downloadUrl, newVersionInfo);
                appUpdate = true;
            }

            @Override
            public void onIsNewestVersion() {
                L.d("onIsNewestVersion");
                appUpdate = false;
            }
        });
        upgradeManager.getAppUpgradeInfo();
    }

    @UiThread
    void showUpgradeDialog(final String downloadUrl, String newVersionInfo) {
        mCommonTabLayout.showMsg(tabs.size() - 1, 1);
        RxBus.getInstance().post(StepsItem.class);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.findNewVersion)).setMessage(newVersionInfo);
        builder.setPositiveButton(getString(R.string.downloadapp), (dialog1, which) -> {
            dialog = new ProgressDialog(MainActivity.this);
            upgradeManager.downLoadAppOrBin(downloadUrl, dialog, UpdateManager.TYPE_APP);
        }).setNegativeButton(R.string.cancel, null).show();
    }

    private void detectOtaUpgrade() {

        upgradeManager.setOnOtaUpgradeListener(new UpdateManager.onOtaUpgradeListener() {

            @Override
            public void onFindNewVersion(String downloadUrl, String newVersionInfo) {
                L.d("onFindNewVesion: " + downloadUrl);

                showOtaUpgradeDialog(downloadUrl, newVersionInfo);
                otaUpdate = true;
            }

            @Override
            public void onIsNewestVersion() {
                L.d("onIsNewestVersion");
                otaUpdate = false;
            }
        });
        upgradeManager.getOtaUpgradeInfo();

        upgradeManager.setOnOTAUpdateListener(new UpdateManager.OnOTAUpdateListener() {
            @Override
            public void progressChanged(int len, int current) {
                progressDialog.setProgress(current);
                sendCountTmp2 = current;
            }

            @Override
            public void finishOTA(boolean success) {
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage(getString(success ? R.string.load_success : R.string.load_fail));
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(true);
                        timer.stopTimer();
                    }
                }
                if (success) {
                    mCommonTabLayout.hideMsg(tabs.size() - 1);
                    RxToast.success(getString(R.string.load_success));
                    UserAlarmTab.deleteAll();
                }
                timerCount = 0;
                sendCountTmp1 = 0;
                sendCountTmp2 = 0;
                sendRate = 0;
            }

            @Override
            public void startOTA(int len) {
                otaShowDialog(len);
                timer.startTimer();
            }
        });
    }


    @UiThread
    void showOtaUpgradeDialog(final String downloadUrl, String newVersionInfo) {
        mCommonTabLayout.showMsg(tabs.size() - 1, 1);

        RxBus.getInstance().post(StepsItem.class);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(myPrefs.bleName().get()).setMessage(getString(R.string.otaMessage) + newVersionInfo);
        builder.setPositiveButton(R.string.downloadbin, (dialog1, which) -> {
            dialog = new ProgressDialog(MainActivity.this);
            upgradeManager.downLoadAppOrBin(downloadUrl, dialog, UpdateManager.TYPE_BIN);
        }).setNegativeButton(R.string.nextUpdate, (dialogInterface, i) -> {
        }).show();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }


    //不退出app，而是隐藏当前的app
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }


    /*-------------------------------------升级OTA--------------------------------------------------------*/


    private ProgressDialog progressDialog;
    private int timerCount = 0;
    private int sendCountTmp1 = 0;
    private int sendCountTmp2 = 0;
    private int sendRate = 0;


    private void otaShowDialog(int len) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.downloadbin));
        progressDialog.setMessage(getString(R.string.wait_download));
        progressDialog.setMax(len);
        progressDialog.setProgress(0);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    @UiThread
    void setProgressDialogMessage(String str) {
        progressDialog.setMessage(str);
    }


    MyPeriodTimer timer = new MyPeriodTimer(200, 1000, () -> {
        L.d("startTimer: enter");
        sendRate = (sendCountTmp2 - sendCountTmp1) * 20;
        sendCountTmp1 = sendCountTmp2;
        timerCount++;
        MainActivity.this.setProgressDialogMessage(MainActivity.this.getString(R.string.wait_download) + "\n" + timerCount + "s\n" + sendRate + "Bps");
    });


}
