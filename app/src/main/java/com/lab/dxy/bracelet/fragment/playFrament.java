package com.lab.dxy.bracelet.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ApplicationInfoUtil;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.activity.AlarmSet_;
import com.lab.dxy.bracelet.activity.AllAppANCDActivity_;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.entity.SettingPlayItem;
import com.lab.dxy.bracelet.entity.StepsItem;
import com.lab.dxy.bracelet.entity.spl.UserAlarmTab;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.RxToast;
import com.lab.dxy.bracelet.ui.myGridView;
import com.lab.dxy.bracelet.ui.recyclerview.CommonGridAndListAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.DividerItemDecoration;
import com.lab.dxy.bracelet.ui.recyclerview.MultiItemCommonAdapter;
import com.lab.dxy.bracelet.ui.recyclerview.MultiItemTypeSupport;
import com.lab.dxy.bracelet.ui.recyclerview.ScrollLinearLayoutManager;
import com.lab.dxy.bracelet.ui.recyclerview.ViewHolder;
import com.syd.oden.circleprogressdialog.core.CircleProgressDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import static com.lab.dxy.bracelet.Contents.ACTION_DATA_INIT;
import static com.lab.dxy.bracelet.MyApplication.mContext;


/**
 * Created by 华 on 2017/5/4.
 */
@EFragment(R.layout.fragment_play)
public class playFrament extends BaseFragment {
    private List<StepsItem> stepsLists = new ArrayList<>();
    CircleProgressDialog circleProgressDialog;

    public String[] text;
    public int[] img = {R.mipmap.icon_phone, R.mipmap.ico_zplay_alarm2x, R.mipmap.ico_remind_app_c1002x, R.mipmap.ico_zplay_sms2x
            , R.mipmap.ico_wechatsign_2x, R.mipmap.ico_qqsign_482x};


    private String[] app = {"", "", Contents.WHATSAPP, "", Contents.WECHAR, Contents.QQ};
    CommonGridAndListAdapter<StepsItem> gridAdapter;

    @ViewById
    myGridView mGridView;

    @ViewById
    RecyclerView mRecyclerView;
    @ViewById
    Button more;

    @Pref
    MyPrefs_ myPrefs;
    @Pref
    BraPrefs_ braPrefs;

    @Receiver(actions = ACTION_DATA_INIT)
    protected void setSynPickerTime2() {
        initData();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }


    @Click
    void more() {
        startActivity(new Intent(getActivity(), AllAppANCDActivity_.class));
    }


    @AfterViews
    void initView() {
        more.setVisibility(View.GONE);
        text = getResources().getStringArray(R.array.ANCS);
        functionTitle = getResources().getStringArray(R.array.function);

        circleProgressDialog = new CircleProgressDialog(getActivity());
        circleProgressDialog.setText("");
        setGridViewAdapter();
        initRecyclerView();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            L.d("隐藏：playfragment");
        } else {
            L.d("显示：playfragment");
            initData();
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }


    private static final int TITLE = 0;
    private static final int TEXT = 1;
    private String[] functionTitle;
    private int[] functionIcon = {R.mipmap.icon_sportremind_2x, R.mipmap.icon_sportremind_2x, R.mipmap.icon_sportremind_2x, R.mipmap.anti_last,
            R.mipmap.bg_disconnect, R.mipmap.ico_loading_2x, R.mipmap.ico_idbroadcast_x, R.mipmap.icon_camera_green_482x, R.mipmap.icon_camera_green_482x};
    private boolean[] functionIsOpen = {false, false, false, false, false, false, false, false, false};

    private List<SettingPlayItem> settingPalys = new ArrayList<>();
    MultiItemCommonAdapter adapter;


    private void initData() {
        functionIsOpen[0] = myPrefs.BrightIsOpen().get();
        functionIsOpen[1] = myPrefs.stepsIsOpen().get();
        functionIsOpen[2] = myPrefs.SedentaryIsOpen().get();
        functionIsOpen[3] = myPrefs.isOpenAntiLast().get();
        functionIsOpen[4] = myPrefs.isOpenBGDisconnect().get();
        settingPalys.clear();
        settingPalys.add(new SettingPlayItem(getString(R.string.function), true));
        for (int i = 0; i < 4; i++) {
            SettingPlayItem playItem = new SettingPlayItem(false, true, functionTitle[i], "", functionIcon[i], functionIsOpen[i]);
            settingPalys.add(playItem);
        }
        settingPalys.add(new SettingPlayItem(getString(R.string.modular), true));
        for (int i = 6; i < 9; i++) {
            SettingPlayItem playItem = new SettingPlayItem(false, false, functionTitle[i], "", functionIcon[i], false);
            settingPalys.add(playItem);
        }
    }


    private void initRecyclerView() {
        initData();
        L.d("个数：" + settingPalys.size());
        mRecyclerView.setLayoutManager(new ScrollLinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        MultiItemTypeSupport<SettingPlayItem> multiItemTypeSupport = new MultiItemTypeSupport<SettingPlayItem>() {
            @Override
            public int getLayoutId(int itemType) {
                if (itemType == TITLE)
                    return R.layout.item_paly_title;
                else
                    return R.layout.item_paly_text;
            }

            @Override
            public int getItemViewType(int i, SettingPlayItem settingItem) {
                if (i == 0 || i == 5)
                    return TITLE;
                else
                    return TEXT;
            }


        };

        adapter = new MultiItemCommonAdapter<SettingPlayItem>(getActivity(), settingPalys, multiItemTypeSupport) {
            @Override
            public void convert(ViewHolder holder, SettingPlayItem s) {
                if (s.isTitle()) {
                    holder.setText(R.id.title, s.getTitle());
                } else {
                    holder.setText(R.id.text_name, s.getTitle());
                    holder.setImageResource(R.id.img_icon, s.getIcon());
                    if (s.isFunction()) {
                        if (s.isOpen()) {
                            holder.setImageResource(R.id.go, R.mipmap.slip_switch_on);
                        } else {
                            holder.setImageResource(R.id.go, R.mipmap.slip_switch_off);
                        }
                    }
                }

                this.setOnItemClickListener((viewHolder, settingItem, i) -> {
                    if (settingItem.isTitle()) return;
                    if (BleService.isConnected) {
                        if (i < 6 && i != 1) {
                            settingItem.setOpen(!settingItem.isOpen());
                            adapter.notifyItemChanged(i);
                        }
                        switchPosition(i, settingItem);

                    } else {
                        RxToast.warning(getString(R.string.barDisConnect));
                    }
                });
            }
        };
        mRecyclerView.setAdapter(adapter);
    }

    private void switchPosition(final int position, final SettingPlayItem item) {
        final boolean b = item.isOpen();
        switch (position) {
            case 1:
                circleProgressDialog.showDialog();
                MyBle.getInstance().setBright(!b, isSuccess -> {
                    circleProgressDialog.dismiss();
                    myPrefs.BrightIsOpen().put(!b);
                    item.setOpen(!b);
                    adapter.notifyItemChanged(position);
                });
                break;
            case 2:
                myPrefs.stepsIsOpen().put(b);
                break;
            case 3:
                setSedentary(b);
                break;
            case 4:
                myPrefs.isOpenAntiLast().put(b);
                break;

            case 6:
                MyBle.getInstance().find();
                break;
            case 7:

                break;
            case 8:
//                shake(2);
                break;

        }
    }


    private void setGridViewAdapter() {
        try {
            stepsLists.clear();
            for (int j = 0; j < text.length; j++) {
                stepsLists.add(new StepsItem(text[j], img[j], false));
            }

            setDefult();
            gridAdapter = new CommonGridAndListAdapter<StepsItem>(getActivity(), stepsLists, R.layout.item_gridview) {
                @Override
                public void convert(ViewHolder helper, StepsItem item) {
                    helper.setText(R.id.text, item.getText());
                    helper.setImageResource(R.id.img, item.getImg());
                    helper.setText(R.id.IsOpen, item.isOpen() ? getString(R.string.on) : getString(R.string.off));
                }
            };

            mGridView.setAdapter(gridAdapter);
            mGridView.setColorLine(R.color.list_gap_gray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGridView.setOnItemClickListener((parent, view, position, id) -> {

            if (!TextUtils.isEmpty(app[position])) {
                String name = ApplicationInfoUtil.getProgramNameByPackageName(getActivity(), app[position]);
                if (TextUtils.isEmpty(name)) {
                    RxToast.warning(mContext.getString(R.string.uninstall));
                    return;
                }
            }

            if (!Utils.isCanUseSim(getActivity())) {
                if (position == 0 || position == 3) {
                    RxToast.warning(mContext.getString(R.string.SimNoUse));
                    return;
                }
            }

            if (position == 1) {
                linear_clock();
            } else {
//                    if (!isEnabled()) {
//                        openSetting();
//                        return;
//                    }
                if (stepsLists.get(position).isOpen()) {
                    stepsLists.get(position).setOpen(false);
                } else {
                    stepsLists.get(position).setOpen(true);
                }
            }

            boolean open = stepsLists.get(position).isOpen();
            switch (position) {
                case 0:
                    L.d("open:" + open);
                    myPrefs.CallisOpen().put(open);
                    break;
                case 2:
                    myPrefs.whatAPPisOpen().put(open);
                    break;
                case 3:
                    myPrefs.MSMisOpen().put(open);
                    break;
                case 4:
                    myPrefs.WeCharisOpen().put(open);
                    break;
                case 5:
                    myPrefs.QQisOpen().put(open);
                    break;
            }
            gridAdapter.notifyDataSetChanged();
        });


    }


    void restart() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.hint)).setMessage("手环将会重启");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
//                myBle.braRetart();
            MyBle.getInstance().reStart(1);
        }).setNegativeButton(R.string.cancel, null).show();
    }

    void linear_clock() {
        startActivity(new Intent(getActivity(), AlarmSet_.class));
    }


    private void setDefult() {
        List<UserAlarmTab> all = UserAlarmTab.getAll();

        myPrefs.ClockisOpen().put(all.size() != 0);
        stepsLists.get(1).setOpen(all.size() != 0);


    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (isAdded()) {

        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    private void setSedentary(final boolean b) {
        int sedTime = myPrefs.SedTime().get();
        if (sedTime < 30) {
            sedTime = 30;
            myPrefs.SedTime().put(sedTime);
        }
        circleProgressDialog.showDialog();
        MyBle.getInstance().setSedPush(b, sedTime / 60, sedTime % 60, isSuccess -> {
            circleProgressDialog.dismiss();
            if (isSuccess) {
                myPrefs.edit().SedentaryIsOpen().put(b).apply();
                adapter.notifyItemChanged(2);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        try {
//            if (getActivity() != null && intent != null)
//                getActivity().stopService(intent);
//            intent = null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            LL.d("异常");
//        }
        if (circleProgressDialog != null)
            circleProgressDialog.dismiss();
    }


}
