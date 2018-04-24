package com.lab.dxy.bracelet.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.BraPrefs_;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ble.listener.BleCommandListener;
import com.lab.dxy.bracelet.core.MySqlManager;
import com.lab.dxy.bracelet.service.BleService;
import com.lab.dxy.bracelet.ui.BasePopupWindow;
import com.lab.dxy.bracelet.ui.CircleImageView;
import com.lab.dxy.bracelet.ui.PickerView;
import com.lab.dxy.bracelet.ui.RxToast;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.lab.dxy.bracelet.Contents.ACTION_BLE_DISCONNECT;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.REQUEST_ALBUM;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.REQUEST_CAMERA;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.openAlbum;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.openCamera;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.readBitmapFromAlbumResult;
import static com.lab.dxy.bracelet.Utils.ImageTakerHelper.readBitmapFromCameraResult;
import static com.lab.dxy.bracelet.ble.BleManagerKit.MAC;

/**
 * Created by 华 on 2017/5/5.
 */
@EActivity(R.layout.activity_user)
public class UserData extends BaseActivity {


    private PopupWindow popupWindow;
    private LinearLayout ll_popup;
    private LinearLayout parentName;
    private View parentView;
    private String[] userSex;
    private int userSexProsition = 0;
    private int sex = 0;
    private PopupWindow UserNamePopW;
    private String heightText = "";
    private String weightText = "";


    @ViewById
    ImageView running;

    @ViewById
    CircleImageView userImg;

    @Pref
    BraPrefs_ braPrefs;
    @Pref
    MyPrefs_ myPrefs;

    @ViewById
    TextView Title;
    @ViewById
    TextView userNameText;
    @ViewById
    TextView userSexText;
    @ViewById
    TextView userDateText;
    @ViewById
    TextView userHeightText;
    @ViewById
    TextView userWeightText;
    @ViewById
    TextView BraName;
    @ViewById
    TextView text_StepsArm;
    @ViewById
    ImageView bg_userInfo;


    @Click
    void name() {
//        setUserNamePopW("修改昵称");
//        parentName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activity_translate_in));
//        setBackgroundAlpha(0.5f);
//        UserNamePopW.showAtLocation(parentView, Gravity.BOTTOM, 0, -90);
//        openKey();
    }

    @Click
    void sex() {
        sex = myPrefs.userSex().get();
        setUserSex(sex, userSex);
    }

    @Click
    void date() {
        setUserNamePopW(getString(R.string.settingAge), 0);
//        parentName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activity_translate_in));
        setBackgroundAlpha(0.5f);
        UserNamePopW.showAtLocation(parentView, Gravity.BOTTOM, 0, -90);
//        openKey();
    }

    @Click
    void height() {
        setUserNamePopW(getString(R.string.settingHeight), 1);
        setBackgroundAlpha(0.5f);
        UserNamePopW.showAtLocation(parentView, Gravity.BOTTOM, 0, -90);
//        openKey();
    }

    @Click
    void weight() {
        setUserNamePopW(getString(R.string.settingWeight), 2);
        setBackgroundAlpha(0.5f);
        UserNamePopW.showAtLocation(parentView, Gravity.BOTTOM, 0, -90);
//        openKey();
    }


    @Click
    void userImg() {
        window.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

    }


    @Click
    void BraName() {
//        setUserNamePopW("修改手环名字");
//        parentName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.activity_translate_in));
//        setBackgroundAlpha(0.5f);
//        UserNamePopW.showAtLocation(parentView, Gravity.CENTER, 0, -90);
//        MyToast.showShort(UserData.this, "暂时未开放");
    }

    @Click
    void running() {
        onBackPressed();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Click
    void stepsArm() {
        setUserNamePopW(getString(R.string.settingSteps), 3);
        setBackgroundAlpha(0.5f);
        UserNamePopW.showAtLocation(parentView, Gravity.BOTTOM, 0, -90);
//        openKey();
    }

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

    @Click
    void UnBind() {
        braPrefs.bleAddr().put("");
        braPrefs.bleName().put("");
        myPrefs.clear();
        MySqlManager.DeleteAllInfo();
        myPrefs.isAutoConnect().put(false);
        MyBle.getInstance().getBleManagerKit().disConnect();
        MAC = "";
        Utils.broadUpdate(ACTION_BLE_DISCONNECT);
        RxToast.info(getString(R.string.unBind));
        onBackPressed();
        overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }


    @AfterViews
    void initView() {
        setData();
        popInit();

    }


    private void setData() {

        Title.setText(getString(R.string.UserInfo));

        userSex = this.getResources().getStringArray(R.array.user_sex);

        L.d("手环名字：" + myPrefs.bleName().get());

        BraName.setText(myPrefs.bleName().get().equals("DXY") ? "DXY" : myPrefs.bleName().get().replace(Contents.barFilterName, "").trim());
        String path = myPrefs.userImg_Url().get();


        Glide.with(this)
                .load(path)
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(this))//圆角图片
                .crossFade(1000)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(userImg, 10));

        userNameText.setText(myPrefs.userName().get());

        sex = myPrefs.userSex().get();
        userSexText.setText(userSex[sex]);

        userDateText.setText(myPrefs.userAge().get() + "");

        heightText = myPrefs.Height().get() + "";
        userHeightText.setText(heightText + "cm");


        weightText = myPrefs.Weight().get() + "";
        userWeightText.setText(weightText + "Kg");

        text_StepsArm.setText(myPrefs.runAim().get() + getString(R.string.steps));

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        StatusBarUtils.from(this).setStatusBarColor(getResources().getColor(R.color.flyBlue)).process();
        parentView = LayoutInflater.from(this).inflate(R.layout.activity_user, null, false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//弹出输入法时布局上移
    }


    @Override
    protected void onDestroy() {
        popDismiss();

        if (BleService.isConnected) {
            int stepsAim = myPrefs.runAim().get() / 100;
            int sex = myPrefs.userSex().get();
            boolean userSex = sex == 0;
            int age = myPrefs.userAge().get();
            int weight = myPrefs.Weight().get();
            int height = myPrefs.Height().get();

            MyBle.getInstance().synPrams(stepsAim, 1, 1, false, userSex, age, weight, height, null, new BleCommandListener() {
                @Override
                public void success() {
                    L.d("成功");
                }

                @Override
                public void fail() {
                    L.d("失败");
                }
            });

        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imgPath = null;
        try {
            L.d("data:" + data);
            if (data != null) {
                if (requestCode == REQUEST_ALBUM) {
                    imgPath = readBitmapFromAlbumResult(this, data);
                }

            } else {
                if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
                    imgPath = readBitmapFromCameraResult(this);
                }
            }
            L.d("图片路径：" + imgPath);
            if (!TextUtils.isEmpty(imgPath))
                showUserImg(imgPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUserImg(String imgPath) {

        Glide.with(this)
                .load(imgPath)
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(this))//圆角图片
                .crossFade(1000)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new GlideDrawableImageViewTarget(userImg, 10));

        myPrefs.userImg_Url().put(imgPath);
    }

    BasePopupWindow window;

    private void popInit() {
        window = new BasePopupWindow(this);
        window.initPop(getString(R.string.choosePhoto), getString(R.string.choosePhotoMode), new String[]{getString(R.string.takePhoto), getString(R.string.changeByAlbum)});
        window.setOnItemClickLisetener((position, text) -> {
            if (Build.VERSION.SDK_INT >= 23) {
                new RxPermissions(UserData.this)
                        .request(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(aBoolean -> {
                            if (aBoolean) {
                                if (text.equals(getString(R.string.takePhoto))) {
                                    openCamera(UserData.this, getPackageName());
                                } else {
                                    openAlbum(UserData.this);
                                }
                            } else {
                                L.d("权限请求失败");
                                RxToast.error(getString(R.string.NoPermission));
                            }
                        });
            } else {
                if (text.equals(getString(R.string.takePhoto))) {
                    openCamera(UserData.this, getPackageName());
                } else {
                    openAlbum(UserData.this);
                }
            }
        });
    }

    private void popDismiss() {
        if (window != null && window.isShowing()) {
            window.dismiss();
        } else if (UserNamePopW != null && UserNamePopW.isShowing()) {
            UserNamePopW.dismiss();
            parentName.clearAnimation();
        }
    }

    //屏幕主题变暗
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }


    protected void setUserSex(int choice, final String[] strings) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.settingUserInfo));

        builder.setSingleChoiceItems(strings, choice, (dialog, which) -> {
            L.d("which: " + which);
            userSexProsition = which;
        });

        //  设置确定按钮
        builder.setPositiveButton(R.string.ok, (arg0, arg1) -> {
            // TODO Auto-generated method stub
            L.d("arg1: " + arg1);
            userSexText.setText(userSex[userSexProsition]);
            myPrefs.userSex().put(userSexProsition);
        });
        //  设置取消按钮
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }


    int dataInt = 0;

    private void setUserNamePopW(final String Title, final int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_username, null, false);
        UserNamePopW = new PopupWindow(this);
        UserNamePopW.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        UserNamePopW.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        UserNamePopW.setBackgroundDrawable(new ColorDrawable(0x00000000));
        UserNamePopW.setFocusable(true);
        UserNamePopW.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);//解决popup被输入法挡住的问题
        UserNamePopW.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        UserNamePopW.setOutsideTouchable(true);
        UserNamePopW.setContentView(view);
        UserNamePopW.setAnimationStyle(R.style.popAnim);


        UserNamePopW.setOnDismissListener(() -> setBackgroundAlpha(1.0f));

        final TextView title = (TextView) view.findViewById(R.id.title);
        TextView ok = (TextView) view.findViewById(R.id.ok);
        TextView cancel = (TextView) view.findViewById(R.id.cancel);

        PickerView pickerView = (PickerView) view.findViewById(R.id.mPickerView);
        parentName = (LinearLayout) view.findViewById(R.id.parent);


        title.setText(Title);

        List<String> data = new ArrayList<>();

        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);

        switch (position) {
            case 0:
                dataInt = myPrefs.userAge().get();
                data.clear();


                for (int j = 1950; j <= year; j++) {
                    data.add(j + "");
                }
                pickerView.setData(data);
                pickerView.setSelected(year - 1950 - dataInt);

                break;
            case 1:
                dataInt = myPrefs.Height().get();
                data.clear();
                for (int j = 50; j < 250; j++) {
                    data.add(j + "");
                }

                pickerView.setData(data);
                pickerView.setSelected(dataInt - 50);
                break;
            case 2:
                dataInt = myPrefs.Weight().get();
                data.clear();

                for (int i = 10; i < 200; i++) {
                    data.add(i + "");
                }

                pickerView.setData(data);
                pickerView.setSelected(dataInt - 10);
                break;
            case 3:
                dataInt = myPrefs.runAim().get();
                data.clear();
                for (int i = 1000; i < 30000; i = i + 1000) {
                    data.add(i + "");
                }
                pickerView.setData(data);
                pickerView.setSelected((dataInt - 1000) / 1000);
                break;
        }

        pickerView.setOnSelectListener(text -> {
            try {
                if (!TextUtils.isEmpty(text)) {
                    dataInt = Integer.parseInt(text);

                    //这里数据的监听会有
                    L.d("(year-dataInt):" + dataInt);
                    switch (position) {
                        case 0:
                            L.d("year:" + year);
                            L.d("year222:" + (year - dataInt));
                            userDateText.setText((year - dataInt) + "");
                            myPrefs.userAge().put((year - dataInt));
                            break;
                        case 1:
                            userHeightText.setText(dataInt + "cm");
                            myPrefs.Height().put(dataInt);
                            break;
                        case 2:
                            userWeightText.setText(dataInt + "kg");
                            myPrefs.Weight().put(dataInt);
                            break;
                        case 3:
                            text_StepsArm.setText(dataInt + getString(R.string.steps));
                            myPrefs.runAim().put(dataInt);
                            break;
                    }
                }
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        });

        ok.setOnClickListener(v -> popDismiss());

        cancel.setOnClickListener(v -> popDismiss());

    }
}
