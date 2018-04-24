package com.lab.dxy.bracelet.core;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.rcc.ble.controller.Device;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ByteUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.RxAppUpdateUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.ble.BleManagerKit;
import com.lab.dxy.bracelet.core.net.RetrofitService;
import com.lab.dxy.bracelet.core.net.UpdateURL;
import com.lab.dxy.bracelet.core.rx.NetManager;
import com.lab.dxy.bracelet.core.rx.RxManager;
import com.lab.dxy.bracelet.core.rx.RxSubscriber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/24
 */
public class UpdateManager {
    public static final int TYPE_APP = 1;
    public static final int TYPE_BIN = 2;
    Context mContext;

    public onAppUpgradeListener mOnAppUpgradeListener;
    public onOtaUpgradeListener mOnOtaUpgradeListener;

    public interface onAppUpgradeListener {
        void onFindNewVersion(String downloadUrl, String newVersionInfo);

        void onIsNewestVersion();
    }

    public interface onOtaUpgradeListener {
        void onFindNewVersion(String downloadUrl, String newVersionInfo);

        void onIsNewestVersion();
    }

    public void setOnAppUpgradeListener(onAppUpgradeListener mOnAppUpgradeListener) {
        this.mOnAppUpgradeListener = mOnAppUpgradeListener;
    }

    public void setOnOtaUpgradeListener(onOtaUpgradeListener mOnOtaUpgradeListener) {
        this.mOnOtaUpgradeListener = mOnOtaUpgradeListener;
    }


    public UpdateManager(Context mContext) {
        this.mContext = mContext;
    }

    public void getOtaUpgradeInfo() {

        RetrofitService dxyService = NetManager.getInstance().createString(RetrofitService.class);
        RxManager.getInstance().doSubscribe(dxyService.getUpgradeVersion(Contents.postInfo), new RxSubscriber<String>() {
            @Override
            protected void _onError(Throwable e) {
                L.e(e.toString());
            }

            @Override
            protected void _onNext(String s) {
                L.d("otaInfo:" + s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String i = jsonObject.getString("i");
                    JSONObject jsonObject1 = new JSONObject(i);
                    String version = jsonObject1.getString("bin_build");
                    String fileName = jsonObject1.getString("bin_filename");
                    String newOTtaInfo = jsonObject1.getString("description");
                    String newOTtaENInfo = jsonObject1.getString("description_en");
                    L.d("versionota: " + version);
                    L.d("fileName: " + fileName);
                    L.d("description: " + newOTtaInfo);
                    L.d("description_en: " + newOTtaENInfo);
                    if (Integer.parseInt(version) > Contents.otaVersion) {
                        L.d("otaVersion:" + Contents.otaVersion + "----Integer.parseInt(version):" + Integer.parseInt(version));
                        if (Utils.isZh(mContext))
                            mOnOtaUpgradeListener.onFindNewVersion(UpdateURL.UPDATE_LODERA_OTA_URL + fileName, newOTtaInfo);
                        else
                            mOnOtaUpgradeListener.onFindNewVersion(UpdateURL.UPDATE_LODERA_OTA_URL + fileName, newOTtaENInfo);
                        L.d("发现新版本!");
                    } else {
                        mOnOtaUpgradeListener.onIsNewestVersion();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getAppUpgradeInfo() {
        RetrofitService dxyService = NetManager.getInstance().createString(RetrofitService.class);
        RxManager.getInstance().doSubscribe(dxyService.getUpgradeAppVersion(Contents.postAppInfo), new RxSubscriber<String>() {
            @Override
            protected void _onError(Throwable e) {
                L.e(e.toString());
            }

            @Override
            protected void _onNext(String s) {
                L.d("appInfo:" + s);
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    String i = jsonObject.getString("i");
                    JSONObject jsonObject1 = new JSONObject(i);
                    String versionCode = jsonObject1.getString("versionCode");
                    String version = jsonObject1.getString("android_build");
                    String fileName = jsonObject1.getString("android_filename");
                    String newAPPInfo = jsonObject1.getString("description");
                    String newAPPENInfo = jsonObject1.getString("description_en");
                    String android_file_size = jsonObject1.getString("android_file_size");
                    L.d("appVersion: " + version);
                    L.d("fileName: " + fileName);
                    L.d("description: " + newAPPInfo);
                    L.d("description_en: " + newAPPENInfo);
                    L.d("appVersion:" + Contents.versionCode + "----Integer.parseInt(versionCode):" + Integer.parseInt(version));

//                    String AppInFo = "版本信息" + newAPPInfo + "\n版本号/：" + versionCode + "\n新版本大小：" + android_file_size + "M";
                    String AppInFo = mContext.getString(R.string.applicationInfo, newAPPInfo, versionCode, android_file_size);

                    if (Integer.parseInt(version) > Contents.versionCode) {
                        if (Utils.isZh(mContext))
                            mOnAppUpgradeListener.onFindNewVersion(UpdateURL.UPDATE_LODER_APP_URL + fileName, AppInFo);
                        else
                            mOnAppUpgradeListener.onFindNewVersion(UpdateURL.UPDATE_LODER_APP_URL + fileName, AppInFo);
                        L.d("发现新版本!");
                    } else {
                        mOnAppUpgradeListener.onIsNewestVersion();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void downLoadAppOrBin(String appUrl, final ProgressDialog progressDialog, final int type) {
        final String dirName = Environment.getExternalStorageDirectory() + Contents.DOWNLOAD_FOLDER;
        final String TAG = "Jack";
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (type == TYPE_APP) {
            progressDialog.setMessage(mContext.getString(R.string.downloadapp));
        } else if (type == TYPE_BIN) {
            progressDialog.setMessage(mContext.getString(R.string.downloadbin));
        }
        progressDialog.setTitle(mContext.getString(R.string.wait_download));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }

        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Void, Void> execute = new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                try {
                    File dirFile = new File(dirName);
                    if (!dirFile.exists()) {
                        L.d("文件不存在");
                        if (dirFile.mkdir()) {
                            L.d("创建文件成功！");
                        }
                    } else {
                        L.d("文件存在");
                    }

                    URL url = new URL(params[0]);
                    URLConnection connection = url.openConnection();

                    int contentLength = connection.getContentLength();
                    Log.d(TAG, "长度：" + contentLength);

                    String newFilename = params[0].substring(params[0].lastIndexOf("=") + 1);
                    newFilename = dirName + newFilename;
                    Log.d(TAG, "newFilename：" + newFilename);
                    File file = new File(newFilename);

                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d(TAG, "newFilename：" + "已存在，删除!");
                        }
                    }
                    byte[] bs = new byte[128];
                    int len;
                    int hasRead = 0;
                    int progress;
                    int progressBefore = 0;

                    InputStream is = connection.getInputStream();
                    OutputStream os = new FileOutputStream(newFilename);
                    while ((len = is.read(bs)) != -1) {
                        os.write(bs, 0, len);
                        hasRead += len;
                        progress = (int) ((double) hasRead / (double) contentLength * 100);
                        if (progressBefore != progress) {
                            progressBefore = progress;
                            progressDialog.setProgress(progress);
                            Log.d(TAG, "progress：" + progress + "%");
                            if (progress == 100) {
                                progressDialog.dismiss();
                                File apkfile = new File(dirName, params[0].substring(params[0].lastIndexOf("=") + 1));
                                if (type == TYPE_APP) {
                                    RxAppUpdateUtils.installApp(mContext, apkfile, Contents.appPackageName);

                                } else if (type == TYPE_BIN) {
                                    L.d("ota广播次数");
                                    updateOTA(newFilename);
                                }
                            }
                        }
                    }
                    is.close();
                    os.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(appUrl);
    }

    boolean isOTAsuccess = false;
    private static final int MESSAGE_UPDATE_TIP = 1;
    private static final int MESSAGE_UPDATE_PROGRESS = 2;
    private byte[] bytes;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;


    public void updateOTA(String path) {
        bytes = ByteUtil.readFirmware(path);
        isOTAsuccess = false;
        // For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                L.e("Unable to initialize BluetoothManager!");
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            L.e("Unable to obtain a BluetoothAdapte!");
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(BleManagerKit.MAC);

        if (device == null) {
            L.d("device is null");
            return;
        } else {
            Device mdevice = new Device(device, null, 0);
            mdevice.setCallback(mDeviceCallback);
            mdevice.connect(mContext);
            mdevice.setSDK3D2(Contents.SDKVersion == Contents.SDK_3D2);
            mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "send connect request to " + device.getAddress()).sendToTarget();
            isSuccess = false;
        }
    }

    private boolean isSuccess = false;

    @SuppressLint("HandlerLeak")
    private Handler mTipsHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_UPDATE_PROGRESS:  //展示进度
                    L.d("progress:" + msg.obj);
                    if (bytes == null) return;
                    int len = bytes.length / 16;
                    if (len * (int) msg.obj / 100 == 100) {
                        isSuccess = true;
                    }
                    onOTAUpdateListener.progressChanged(len, len * (int) msg.obj / 100);
                    break;
                case MESSAGE_UPDATE_TIP:
                    if (msg.obj.toString().equals("ota success")) {
                        onOTAUpdateListener.finishOTA(true);
                        isOTAsuccess = true;

                    } else if (msg.obj.equals("ota failure")) {
                        onOTAUpdateListener.finishOTA(false);

                    } else if (msg.obj.equals("start ota")) {

                    } else if (msg.obj.equals("disconnected")) {
                        if (!isOTAsuccess)
                            onOTAUpdateListener.finishOTA(false);
                    } else if (((String) msg.obj).contains("send connect request to ")) {
                        onOTAUpdateListener.startOTA(bytes.length / 16);
                    }
                    break;
            }
        }
    };

    private Device.Callback mDeviceCallback = new Device.Callback() {
        @Override
        public void onConnected(Device device) {
            mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "connected").sendToTarget();
            L.d("连接成功");
        }

        @Override
        public void onDisconnected(Device device) {
            L.d("断开连接");
            mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "disconnected").sendToTarget();
        }

        @Override
        public void onServicesDiscovered(Device device) {
            mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "start ota").sendToTarget();
            L.d("开始ota");
            device.startOta(bytes);
        }

        @Override
        public void onOtaStateChanged(Device device, int state) {
            switch (state) {
                case Device.STATE_PROGRESS:
                    mTipsHandler.obtainMessage(MESSAGE_UPDATE_PROGRESS, device.getOtaProgress()).sendToTarget();
                    break;
                case Device.STATE_SUCCESS:
                    L.d("升级成功");
                    mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "ota success").sendToTarget();
                    break;
                case Device.STATE_FAILURE:
                    L.d("升级失败");
                    if (isSuccess) {
                        mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "ota success").sendToTarget();
                    } else
                        mTipsHandler.obtainMessage(MESSAGE_UPDATE_TIP, "ota failure").sendToTarget();
                    break;
            }
        }
    };

    public interface OnOTAUpdateListener {
        void progressChanged(int len, int current);

        void finishOTA(boolean success);

        void startOTA(int len);
    }

    private OnOTAUpdateListener onOTAUpdateListener;

    public void setOnOTAUpdateListener(OnOTAUpdateListener onOTAUpdateListener) {
        this.onOTAUpdateListener = onOTAUpdateListener;
    }
}
