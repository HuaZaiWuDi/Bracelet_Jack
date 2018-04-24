package com.lab.dxy.bracelet.fragment;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.MyApplication;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ApplicationInfoUtil;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.entity.AppInfo;
import com.lab.dxy.bracelet.entity.AppInfoItem;
import com.lab.dxy.bracelet.service.MyNotificationListenerService;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.recyclerview.CommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.OnItemClicksListener;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static android.service.notification.NotificationListenerService.requestRebind;
import static com.lab.dxy.bracelet.MyApplication.aCache;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/12/1
 */
@EFragment(R.layout.activity_setting_acivity)
public class ANCSFragment extends BaseFragment {
    private CommonAdapter adapter;
    private List<AppInfo> addApp;
    private int selectPosition;
    private String[] text;
    private AppInfoItem item;
    private int[] img = {R.mipmap.icon_phone, R.mipmap.ico_remind_app_c1002x, R.mipmap.ico_zplay_sms2x
            , R.mipmap.ico_wechatsign_2x, R.mipmap.ico_qqsign_482x};
    private String[] app = {"", Contents.MSM, Contents.WHATSAPP, Contents.WECHAR, Contents.QQ};

    public static synchronized ANCSFragment getInstance() {
        return new ANCSFragment_();
    }

    @ViewById
    RecyclerView mRecyclerView;
    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;


    @AfterViews
    void initView() {
        text = getResources().getStringArray(R.array.ANCS);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (isEnabled()) {
            toggleNotificationListenerService();
        }

        item = (AppInfoItem) aCache.getAsObject("addApp");
        if (item == null) {
            item = new AppInfoItem();
            addApp = new ArrayList<>();
            for (int j = 0; j < text.length; j++) {
                addApp.add(new AppInfo(text[j], app[j], img[j], false));
            }
            item.setAppInfos(addApp);
            aCache.put("addApp", item);
        } else {
            addApp = item.getAppInfos();
        }
        if (!braPrefs.isOpenANCS().get()) {
            if (isEnabled())
                braPrefs.isOpenANCS().put(true);

            addApp.get(0).isOpen = isEnabled() && Utils.isCanUseSim(MyApplication.getmContext());
            addApp.get(1).isOpen = isEnabled() && Utils.isCanUseSim(MyApplication.getmContext());
            addApp.get(2).isOpen = isEnabled() && !TextUtils.isEmpty(ApplicationInfoUtil.getProgramNameByPackageName(MyApplication.getmContext(), app[2]));
            addApp.get(3).isOpen = isEnabled() && !TextUtils.isEmpty(ApplicationInfoUtil.getProgramNameByPackageName(MyApplication.getmContext(), app[3]));
            addApp.get(4).isOpen = isEnabled() && !TextUtils.isEmpty(ApplicationInfoUtil.getProgramNameByPackageName(MyApplication.getmContext(), app[4]));

        }
        initRecycler();
    }

    @Override
    public void onStop() {
        super.onStop();
        item.setAppInfos(addApp);
        aCache.put("addApp", item);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void initRecycler() {
        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        adapter = new CommonAdapter<AppInfo>(getActivity(), R.layout.item_paly_text, addApp) {
            @Override
            public void convert(ViewHolder holder, AppInfo appInfo) {
                holder.setText(R.id.text_name, appInfo.appName);
                Glide.with(getActivity())
                        .load(appInfo.appIcon)
                        .asBitmap()
                        .error(appInfo.versionCode)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into((ImageView) holder.getView(R.id.img_icon));

                holder.setImageResource(R.id.go, appInfo.isOpen ? R.mipmap.slip_switch_on : R.mipmap.slip_switch_off);

                this.setOnItemClicksListener(new OnItemClicksListener() {
                    @Override
                    public void onItemClick(ViewGroup parent, View view, Object o, int position) {
                        selectPosition = position;
                        L.d("点击");
                        switchPosition();
                    }

                    @Override
                    public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position) {
                        selectPosition = position;
                        L.d("长按");
                        if (position > 4)
                            showMyDialog();
                        return false;
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    private void showMyDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(this.getString(R.string.hint)).setMessage(this.getString(R.string.isDelete));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addApp.remove(selectPosition);
                adapter.notifyDataSetChanged();
            }
        }).setNegativeButton(R.string.cancel, null).show();
    }


    private void switchPosition() {
        if (!isEnabled()) {
            openSetting();
            return;
        }
        AppInfo info = addApp.get(selectPosition);
        if (selectPosition == 0 || selectPosition == 1) {
            if (!Utils.isCanUseSim(getActivity())) {
                RxToast.warning(getString(R.string.SimNoUse));
                return;
            }
        } else {
            if (!TextUtils.isEmpty(info.packageName)) {
                String name = ApplicationInfoUtil.getProgramNameByPackageName(getActivity(), info.packageName);
                if (TextUtils.isEmpty(name)) {
                    RxToast.warning(getString(R.string.uninstall));
                    return;
                }
            }
        }


        info.isOpen = !info.isOpen;
        L.d(info.toString());
        adapter.notifyItemChanged(selectPosition);

    }


    public void openSetting() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.hint)).setMessage(getString(R.string.ANCS_Message));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(
                        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        }).setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private boolean isEnabled() {
        String pkgName = getActivity().getPackageName();
        final String flat = Settings.Secure.getString(getActivity().getContentResolver(),
                MyNotificationListenerService.ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(getActivity(), MyNotificationListenerService.class);
        PackageManager pm = getActivity().getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        L.d("重新开启Notification");
        if (Build.VERSION.SDK_INT >= 24) {
            requestRebind(thisComponent);
        }
    }

}
