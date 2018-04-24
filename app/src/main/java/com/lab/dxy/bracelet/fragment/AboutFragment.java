package com.lab.dxy.bracelet.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxBus;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimer;
import com.lab.dxy.bracelet.Utils.timer.MyPeriodTimerListener;
import com.lab.dxy.bracelet.activity.AddDervice_;
import com.lab.dxy.bracelet.activity.BaseByFragmentActivity;
import com.lab.dxy.bracelet.activity.MainActivity;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.core.MySqlManager;
import com.lab.dxy.bracelet.core.UpdateManager;
import com.lab.dxy.bracelet.entity.SettingPlayItem;
import com.lab.dxy.bracelet.entity.StepsItem;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.OnItemClickListener;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.lab.dxy.bracelet.Contents.ACTION_BLE_CONNECTED;
import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.MyApplication.aCache;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/1
 */
@EFragment(R.layout.activity_setting_acivity)
public class AboutFragment extends BaseFragment {
    private CommonAdapter adapter;
    private List<SettingPlayItem> settingPalys = new ArrayList<>();
    private String[] about;
    private int selectPosition;
    UpdateManager upgradeManager;

    public static synchronized AboutFragment getInstance() {
        return new AboutFragment_();
    }

    @ViewById
    RecyclerView mRecyclerView;
    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;
    @ViewById
    TextView Title;


    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime() {
        initData();
        adapter.notifyDataSetChanged();
    }


    @Receiver(actions = ACTION_BLE_CONNECTED)
    protected void onActionConnected() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }


    @AfterViews
    void initView() {
        upgradeManager = new UpdateManager(getActivity());
        about = getActivity().getResources().getStringArray(R.array.about);

        initRecycler();
    }


    private void initRecycler() {
        initData();
        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        adapter = new CommonAdapter<SettingPlayItem>(getActivity(), R.layout.item_paly_text, settingPalys) {
            @Override
            public void convert(ViewHolder holder, SettingPlayItem settingPlayItem) {
                holder.setText(R.id.text_name, settingPlayItem.getTitle());
                holder.setText(R.id.param, settingPlayItem.getText());
                holder.setImageResource(R.id.img_icon, settingPlayItem.getIcon());
                holder.setImageResource(R.id.go, settingPlayItem.isOpen() ? R.mipmap.news : R.mipmap.btn_arrow1_w36_n2x);
                this.setOnItemClickListener(new OnItemClickListener<SettingPlayItem>() {
                    @Override
                    public void onItemClick(ViewHolder viewHolder, SettingPlayItem settingPlayItem, final int position) {
                        selectPosition = position;
                        switchPosition();
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    private void initData() {
        settingPalys.clear();
        for (int j = 0; j < about.length; j++) {
            if (j == 0) {
                settingPalys.add(new SettingPlayItem(about[j], Utils.getVersionName(getActivity()), R.mipmap.icon_setting_482x, MainActivity.appUpdate));
            } else if (j == 1) {
                settingPalys.add(new SettingPlayItem(about[j], Contents.otaVersion + "", R.mipmap.icon_setting_482x, MainActivity.otaUpdate));
            } else
                settingPalys.add(new SettingPlayItem(about[j], false, R.mipmap.icon_setting_482x));
        }
    }

    private void switchPosition() {
        switch (selectPosition) {
            case 0:
                detectUpgrade();
                break;
            case 1:
                if (isConnected)
                    detectOtaUpgrade();
                else
                    RxToast.warning(getString(R.string.barDisConnect));
                break;
            case 2:
                if (isConnected)
                    showExitDialog();
                else
                    RxToast.warning(getString(R.string.barDisConnect));
                break;
            case 3:
                Intent intent = new Intent(getActivity(), BaseByFragmentActivity.class);
                intent.putExtra("type", 0);
                startActivity(intent);
                break;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // 应用升级
    ///////////////////////////////////////////////////////////////////////////
    private ProgressDialog progressDialog;
    private int timerCount = 0;
    private int sendCountTmp1 = 0;
    private int sendCountTmp2 = 0;
    private int sendRate = 0;


    private void otaShowDialog(int len) {
        progressDialog = new ProgressDialog(getActivity());
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


    MyPeriodTimer timer = new MyPeriodTimer(200, 1000, new MyPeriodTimerListener() {
        @Override
        public void enterTimer() {
            L.d("startTimer: enter");
            sendRate = (sendCountTmp2 - sendCountTmp1) * 20;
            sendCountTmp1 = sendCountTmp2;
            timerCount++;
            setProgressDialogMessage(getString(R.string.wait_download) + "\n" + timerCount + "s\n" + sendRate + "Bps");
        }
    });

    private void detectUpgrade() {
        upgradeManager.setOnAppUpgradeListener(new UpdateManager.onAppUpgradeListener() {
            @Override
            public void onFindNewVersion(String downloadUrl, String newVersionInfo) {
                L.d("onFindNewVesion: " + downloadUrl);
                showUpgradeDialog(downloadUrl, newVersionInfo);
            }

            @Override
            public void onIsNewestVersion() {
                L.d("onIsNewestVersion");
                RxToast.info(getString(R.string.news_version));
            }
        });
        upgradeManager.getAppUpgradeInfo();
    }

    @UiThread
    void showUpgradeDialog(final String downloadUrl, String newVersionInfo) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.findNewVersion)).setMessage(newVersionInfo);
        builder.setPositiveButton(getString(R.string.downloadapp), (dialog1, which) -> upgradeManager.downLoadAppOrBin(downloadUrl, new ProgressDialog(getActivity()), UpdateManager.TYPE_APP)).setNegativeButton(R.string.cancel, null).show();
    }

    private void detectOtaUpgrade() {
        upgradeManager.setOnOtaUpgradeListener(new UpdateManager.onOtaUpgradeListener() {

            @Override
            public void onFindNewVersion(String downloadUrl, String newVersionInfo) {
                L.d("onFindNewVesion: " + downloadUrl);
                showOtaUpgradeDialog(downloadUrl, newVersionInfo);
            }

            @Override
            public void onIsNewestVersion() {
                L.d("onIsNewestVersion");
                RxToast.info(getString(R.string.news_version));
            }
        });
        upgradeManager.getOtaUpgradeInfo();

        upgradeManager.setOnOTAUpdateListener(new UpdateManager.OnOTAUpdateListener() {
            @Override
            public void progressChanged(int len, int current) {
                progressDialog.setMax(len);
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
                    RxBus.getInstance().post(StepsItem.class);
                    MainActivity.otaUpdate = false;
                    RxToast.success(getString(R.string.load_success));
                }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(myPrefs.bleName().get()).setMessage(getString(R.string.otaMessage) + newVersionInfo);
        builder.setPositiveButton(R.string.downloadbin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                upgradeManager.downLoadAppOrBin(downloadUrl, new ProgressDialog(getActivity()), UpdateManager.TYPE_BIN);
            }
        }).setNegativeButton(R.string.nextUpdate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).show();
    }


    /*-----------------------------------恢复出厂设置--------------------------------------------------------------*/


    public void showExitDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.hint)).setMessage(getString(R.string.factoryAPP));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (BleService.isConnected) {
                    myPrefs.clear();
                    aCache.clear();
                    MySqlManager.DeleteAllInfo();
                    MyBle.getInstance().reStart(0);
                    getActivity().startActivity(new Intent(getActivity(), AddDervice_.class));
                    getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                }
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }

}
