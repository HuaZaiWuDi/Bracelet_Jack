package com.lab.dxy.bracelet.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.adapter.BleItemAdapter;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.core.MySqlManager;
import com.lab.dxy.bracelet.ui.RxToast;
import com.skyfishjy.library.RippleBackground;
import com.syd.oden.odenble.Utils.HexUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.MyApplication.bluetoothClient;
import static com.lab.dxy.bracelet.MyApplication.isFirstIn;
import static com.lab.dxy.bracelet.ble.BleManagerKit.MAC;
import static com.lab.dxy.bracelet.fragment.exercisefragment.isFirstSyn;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * Created by 华 on 2017/5/4.
 */

@EActivity(R.layout.activity_adddervice)
public class AddDervice extends BaseActivity {
    public static List<SearchResult> bleScanItemList = new ArrayList<>();
    BleItemAdapter adapter;
    boolean isSynData = false;

    @Pref
    MyPrefs_ myPrefs;

    @Pref
    BraPrefs_ braPrefs;

    @ViewById
    TextView title;
    @ViewById
    TextView connectDevice;

    @ViewById
    ListView derviceList;
    @ViewById
    ProgressBar pro_search;
    @ViewById
    RippleBackground mRippleBackground;
    @ViewById
    ImageView centerImage;
    @ViewById
    ImageView switchAutoConnect;
    @ViewById
    TextView isAutoConnect;

    @Click
    void back() {
        onBackPressed();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Click
    void switchAutoConnect() {
        Boolean isAutoConnect = myPrefs.isAutoConnect().get();
        switchAutoConnect.setImageResource(isAutoConnect ? R.mipmap.switch_off : R.mipmap.switch_on);
        this.isAutoConnect.setText(getString(isAutoConnect ? R.string.connectOnce : R.string.autoConnect));
        myPrefs.isAutoConnect().put(!isAutoConnect);
        if (!myPrefs.isAutoConnect().get())
            braPrefs.bleAddr().put("");
        else
            braPrefs.bleAddr().put(MAC);
    }

    @Click
    void connectDevice() {
        if (!isConnected && !TextUtils.isEmpty(braPrefs.bleAddr().get())) {
            MyBle.getInstance().getBleManagerKit().connectDevice();
            mRippleBackground.startRippleAnimation();
        }
        derviceList.setVisibility(View.GONE);
        mRippleBackground.setVisibility(View.VISIBLE);
    }


    //监听系统蓝牙开启
    @Receiver(actions = BluetoothAdapter.ACTION_STATE_CHANGED)
    void blueToothisOpen(@Receiver.Extra(BluetoothAdapter.EXTRA_STATE) int state) {
        if (state == BluetoothAdapter.STATE_OFF) {
            pro_search.setVisibility(View.GONE);
            RxToast.warning(getString(R.string.bleCloseAndScan));
        } else if (state == BluetoothAdapter.STATE_ON) {
            RxToast.info(getString(R.string.startScan));
            pro_search.setVisibility(View.VISIBLE);
            sheckPromission();
        }
    }


    @Receiver(actions = ACTION_BLE_CONNECTED)
    protected void onActionConnected() {
        title.setText(R.string.barConnect);
        pro_search.setVisibility(View.GONE);
        RxToast.info(getString(R.string.barConnect));
        mRippleBackground.stopRippleAnimation();
        if (!TextUtils.isEmpty(braPrefs.bleAddr().get()))
            connectDevice.setText(braPrefs.bleName().get());

//        title.setText(R.string.startSynData);
//        timer.startTimer();
    }

//    @Receiver(actions = ACTION_DATA_INIT)
//    protected void setSynPickerTime() {
//        if (!isSynData)
//            return;
//
//        RxToast.info("第一次同步可能需要2到3分钟，请耐心等候", Toast.LENGTH_LONG);
//
//        Utils.mSleep(500);
//        if (DayStepsTab.getAll().size() == 0 || SleepDataTab.getAll().size() == 0)
//            MyBle.getInstance().synSleepData(new byte[]{0, 0, 0, 0, 0, 0}, new BleCommandListener() {
//                @Override
//                public void success() {
//                    Max = 100;
//                    Utils.mSleep(2000);
//                    MyBle.getInstance().synStepsData(new byte[]{0, 0, 0, 0, 0, 0}, new BleCommandListener() {
//                        @Override
//                        public void success() {
//                            timer.stopTimer();
//                        }
//
//                        @Override
//                        public void fail() {
//                            RxToast.error(getString(R.string.settingFail));
//                            timer.stopTimer();
//                        }
//                    });
//                }
//
//                @Override
//                public void fail() {
//                    RxToast.error(getString(R.string.settingFail));
//                    timer.stopTimer();
//                }
//            });
//    }
//
//    MyPeriodTimer timer = new MyPeriodTimer(0, 1000, () -> synTextView());
//
//    @UiThread
//    public void synTextView() {
//        if (startTime <= Max) {
//            if (startTime == 100) {
//                title.setText(R.string.synDataSuccess);
//                centerImage.setText(startTime + "%");
//            }
//            centerImage.setText(startTime + "%");
//            startTime++;
//        }
//    }
//
//    int Max = 50;
//    int startTime = 0;


    @Click
    void addDervice() {
        title.setText(getString(R.string.startScan));
        bleScanItemList.clear();
        addrList.clear();
        sheckPromission();
        adapter.notifyDataSetChanged();
        derviceList.setVisibility(View.VISIBLE);
        mRippleBackground.setVisibility(View.GONE);
        mRippleBackground.stopRippleAnimation();
    }


    private void setAdapter() {

        bleScanItemList.clear();
        addrList.clear();
        L.d("bleScanItemList:" + bleScanItemList.size());
        adapter = new BleItemAdapter(this, bleScanItemList);
        derviceList.setAdapter(adapter);

        derviceList.setOnItemClickListener((parent, view, position, id) -> {
            title.setText(R.string.connecting);
            if (!braPrefs.bleAddr().equals(bleScanItemList.get(position).getAddress())) {

                braPrefs.bleName().put(bleScanItemList.get(position).getName());
                braPrefs.bleAddr().put(bleScanItemList.get(position).getAddress());
                myPrefs.clear();
                MySqlManager.DeleteAllInfo();
                braPrefs.isOpenANCS().put(false);
                Utils.broadUpdate(ACTION_BLE_DISCONNECT);
                isFirstSyn = true;
                isFirstIn = true;
                isConnected = false;
                MAC = bleScanItemList.get(position).getAddress();

            }
            bluetoothClient.stopSearch();
            MyBle.getInstance().getBleManagerKit().connectDevice();

            derviceList.setVisibility(View.GONE);
            mRippleBackground.setVisibility(View.VISIBLE);
            mRippleBackground.startRippleAnimation();
        });
    }

    @AfterViews
    void initView() {
        if (!TextUtils.isEmpty(braPrefs.bleAddr().get()))
            connectDevice.setText(braPrefs.bleAddr().get());

        Glide.clear(centerImage);
        Glide.with(this)
                .load(R.mipmap.appicon)
                .bitmapTransform(new CropCircleTransformation(this))//圆角图片
                .crossFade(1000)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(centerImage, 10));


        setAdapter();
        switchAutoConnect.setImageResource(!myPrefs.isAutoConnect().get() ? R.mipmap.switch_off : R.mipmap.switch_on);
        this.isAutoConnect.setText(getString(!myPrefs.isAutoConnect().get() ? R.string.connectOnce : R.string.autoConnect));

    }

    private void sheckPromission() {
        //定位权限
        if (Build.VERSION.SDK_INT >= 23)
            new RxPermissions(this)
                    .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .subscribe(aBoolean -> {
                        if (aBoolean) {
                            L.d("权限请求成功");
                            scan();
                        } else {
                            L.d("权限请求失败");
                            RxToast.error(getString(R.string.openLoactionpermission));
                        }
                    });
        else scan();
    }


    @Override
    protected void onStop() {
        super.onStop();
        bluetoothClient.stopSearch();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setTransparentStatusbar(true).process();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothClient.stopSearch();

    }

    String TAG = getClass().getName();

    public void scan() {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.searchBluetoothLeDevice(2 * 60 * 1000, 3);

        bluetoothClient.search(builder.build(), new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.d(TAG, "onSearchStarted");
                title.setText(getString(R.string.startScan));
                pro_search.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDeviceFounded(final SearchResult device) {
                Log.d(TAG, "device:" + device.device);
                Log.d(TAG, "rssi:" + device.rssi);
                Log.d(TAG, "deviceName:" + device.getName());
                Log.d(TAG, "deviceAddr:" + device.getAddress());
                Log.d(TAG, "scanRecord:" + HexUtil.encodeHexStr(device.scanRecord));

                if (doDeviceFilter(device))
                    if (addrList.contains(device.getAddress())) {
                        for (int i = 0; i < bleScanItemList.size(); i++) {
                            if (bleScanItemList.get(i).getAddress().equals(device.getAddress())) {
                                bleScanItemList.get(i).scanRecord = device.scanRecord;
                            }
                        }
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    } else {
                        addrList.add(device.getAddress());
                        bleScanItemList.add(device);
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    }
            }

            @Override
            public void onSearchStopped() {
                Log.d(TAG, "onSearchStopped");
                addrList.clear();
                title.setText(getString(R.string.stopScan));
                pro_search.setVisibility(View.GONE);
            }

            @Override
            public void onSearchCanceled() {
                Log.d(TAG, "onSearchCanceled");
                title.setText(getString(R.string.stopScan));
                pro_search.setVisibility(View.GONE);
            }
        });

    }

    private boolean doDeviceFilter(SearchResult device) {

        String s = HexUtil.encodeHexStr(device.scanRecord);
        String[] split = s.split("0bff0201");

        if (split.length == 2) {
            String deviceFilter = split[1].substring(14, 16);
            //新版过滤逻辑：0x21:DXY-手环，0x22:美盛通，0x23优蓝，0x24佳琪
            if (deviceFilter.equals(Contents.BRA_DXY) || deviceFilter.equals(Contents.BRA_MST) || deviceFilter.equals(Contents.BRA_YL) || deviceFilter.equals(Contents.BRA_JQ)) {
                return true;
            }
        } else if (device.getName().contains(Contents.barFilterName)) {
            return true;
        } else if (device.getAddress().equals(braPrefs.bleAddr().get())) {
            return false;
        }
        return false;
    }

    private ArrayList<String> addrList = new ArrayList<>();
}
