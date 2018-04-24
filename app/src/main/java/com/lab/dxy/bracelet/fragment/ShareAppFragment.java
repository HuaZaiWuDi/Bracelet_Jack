package com.lab.dxy.bracelet.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.ApplicationInfoUtil;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.RxTextUtils;
import com.lab.dxy.bracelet.Utils.ScreenUtil;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseFragment;
import com.lab.dxy.bracelet.core.GetMarkClass;
import com.lab.dxy.bracelet.entity.spl.SleepDataTab2;
import com.lab.dxy.bracelet.ui.RxToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import me.shaohui.shareutil.ShareUtil;
import me.shaohui.shareutil.share.ShareListener;
import me.shaohui.shareutil.share.SharePlatform;

import static com.lab.dxy.bracelet.MyApplication.mContext;
import static com.lab.dxy.bracelet.fragment.findFragment.fromatDate;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：oden
 * 创建时间：2017/11/6
 */

@EFragment(R.layout.fragment_share_app)
public class ShareAppFragment extends BaseFragment {

    private int type;
    View parentLayout;

    public static Fragment getIntance(int type) {
        ShareAppFragment fragment = new ShareAppFragment_();
        fragment.type = type;
        return fragment;
    }

    @Pref
    MyPrefs_ myPrefs;


    @ViewById
    RelativeLayout shareData;

    @ViewById
    LinearLayout share;
    @ViewById
    LinearLayout showApp;


    @ViewById
    ImageView img_bg;
    @ViewById
    ImageView img_heard;
    @ViewById
    TextView dataType;
    @ViewById
    TextView data;
    @ViewById
    TextView unit;
    @ViewById
    TextView date;
    @ViewById
    ImageView img_1;
    @ViewById
    ImageView img_2;
    @ViewById
    ImageView img_3;
    @ViewById
    TextView text_1;
    @ViewById
    TextView text_2;
    @ViewById
    TextView text_3;
    @ViewById
    TextView textData_1;
    @ViewById
    TextView textData_2;
    @ViewById
    TextView textData_3;
    @ViewById
    TextView childUnit_1;
    @ViewById
    TextView childUnit_2;
    @ViewById
    TextView childUnit_3;


    @Click
    void back() {
        getActivity().onBackPressed();
        getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
    }

    @Click
    void img_qq() {
        if (checkApp(Contents.QQ)) {
            Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
            ShareUtil.shareImage(mContext, SharePlatform.QQ, bitmap, shareListener);
        }
    }

    @Click
    void img_QQ_zone() {
        if (checkApp(Contents.QQ)) {
            Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
            ShareUtil.shareImage(mContext, SharePlatform.QZONE, bitmap, shareListener);
        }
    }

    @Click
    void img_WX() {
        if (checkApp(Contents.WECHAR)) {
            Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
            ShareUtil.shareImage(mContext, SharePlatform.WX, bitmap, shareListener);
        }
    }

    @Click
    void img_friends() {
        if (checkApp(Contents.WECHAR)) {
            Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
            ShareUtil.shareImage(mContext, SharePlatform.WX_TIMELINE, bitmap, shareListener);
        }
    }

    @Click
    void img_weibo() {
        if (checkApp(Contents.WEIBO)) {
            Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
            ShareUtil.shareImage(mContext, SharePlatform.WEIBO, bitmap, shareListener);
        }
    }

    @Click
    void img_download() {
        RxToast.info(getString(R.string.wait_download));
        final Bitmap bitmap = ScreenUtil.snapShotWithoutStatusBar(getActivity());
        new Thread(() -> {
            try {
                String s = saveToSDCard(bitmap);
                synAlbum(s);
                showToast(getString(R.string.downloadSuccess));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    @AfterViews
    void initView() {
        parentLayout = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_share_app, null, false);
        switch (type) {
            case 0:
                shareHeardRate();
                return;
            case 1:
                shareSteps();
                break;
            case 2:
                shareSleep();
                break;
        }

        dataType.setText(getString(type == 1 ? R.string.stepsData : R.string.sleepData));
        unit.setText(getString(type == 1 ? R.string.steps : R.string.score));
        img_1.setImageResource(type == 1 ? R.mipmap.stepstime : R.mipmap.sleep);
        img_2.setImageResource(type == 1 ? R.mipmap.mileage : R.mipmap.wake);
        img_3.setImageResource(type == 1 ? R.mipmap.fire : R.mipmap.sleeptime);
        text_1.setText(getString(type == 1 ? R.string.stepsTime : R.string.startSleep));
        text_2.setText(getString(type == 1 ? R.string.stepsKm : R.string.endSleep));
        text_3.setText(getString(type == 1 ? R.string.stepsKcal : R.string.aSleep));
        childUnit_1.setText(type == 1 ? "" : "");
        childUnit_2.setText(type == 1 ? "km" : "");
        childUnit_3.setText(type == 1 ? "kcal" : "");
        date.setText(Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE));
    }

    private void shareHeardRate() {
        Glide.with(getActivity())
                .load(myPrefs.userImg_Url().get())
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(img_heard);

        img_bg.setBackgroundColor(getResources().getColor(R.color.heartRate));

        dataType.setText(R.string.heartRateData);
        unit.setText("bpm");
        img_1.setImageResource(R.mipmap.dynamic_heart_rate);
        img_2.setImageResource(R.mipmap.static_heart_rate);
        img_3.setImageResource(R.mipmap.average_heard_rate);
        text_1.setText(R.string.dynamicHeart);
        text_2.setText(R.string.staticHeart);
        text_3.setText(R.string.averageHeart);
        childUnit_1.setText("bpm");
        childUnit_2.setText("bpm");
        childUnit_3.setText("bpm");

        textData_1.setText("120");
        textData_2.setText("86");
        textData_3.setText("88");
        data.setText(myPrefs.heartRate().get() + "");
        date.setText(Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE));
    }


    @Override
    public void onStart() {
        super.onStart();
        L.d("onStart");
    }


    @Override
    public void onStop() {
        super.onStop();
        L.d("onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        L.d("onPause");
    }


    @UiThread
    public void synAlbum(String path) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        getActivity().sendBroadcast(intent);
    }


    @UiThread
    public void showToast(String mag) {
        RxToast.success(mag);
    }


    private void setVisbility(boolean b) {

        showApp.setVisibility(b ? View.GONE : View.VISIBLE);
        share.setVisibility(b ? View.VISIBLE : View.GONE);
    }


    ShareListener shareListener = new ShareListener() {
        @Override
        public void shareSuccess() {
            L.d("分享成功");
            RxToast.success(getString(R.string.shareSuccess));
        }

        @Override
        public void shareFailure(Exception e) {
            L.d("分享失败:" + e.fillInStackTrace());
            RxToast.error(getString(R.string.shareFail));
        }

        @Override
        public void shareCancel() {
            L.d("分享关闭");
        }
    };


    private boolean checkApp(String appPackage) {

        String name = ApplicationInfoUtil.getProgramNameByPackageName(getActivity(), appPackage);
        if (TextUtils.isEmpty(name)) {
            RxToast.warning(getString(R.string.uninstall));
            return false;
        }
        return true;
    }


    private void shareSleep() {
        Glide.with(getActivity())
                .load(myPrefs.userImg_Url().get())
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(img_heard);
        img_bg.setImageResource(R.drawable.gradient_violet);

        List<SleepDataTab2> sleepWeek = SleepDataTab2.getSleepDate(Utils.setFormat(new Date().getTime(), fromatDate, Utils.DATE));

        if (sleepWeek.size() <= 1) {
            data.setText(getString(R.string.sleepStatusBad));
            textData_1.setText("00:00");
            textData_2.setText("00:00");
            RxTextUtils.setTextView(getContext(), textData_3, 0, Color.parseColor("#ffffff"));
            return;
        }

        int asleep = 0;
        int awake = 0;

        for (int i = 0; i < sleepWeek.size(); i++) {

            if (sleepWeek.get(i).getType() == 0) {
                awake += sleepWeek.get(i).getDuration();
            }
            if (sleepWeek.get(i).getType() == 2) {
                asleep += sleepWeek.get(i).getDuration();
            }
        }
        String sleepQ = checkSleepQ(asleep, awake);

        data.setText(sleepQ);
        String StartTime = sleepWeek.get(0).getTime().split("-")[0];
        String EndTime = sleepWeek.get(sleepWeek.size() - 1).getTime().split("-")[1];

        textData_1.setText(StartTime);
        textData_2.setText(EndTime);
//        textData_3.setText(intToStringMin(asleep));
        RxTextUtils.setTextView(getContext(), textData_3, asleep, Color.parseColor("#ffffff"));
    }

    private void shareSteps() {
        Glide.with(getActivity())
                .load(myPrefs.userImg_Url().get())
                .error(R.mipmap.img_heard)
                .bitmapTransform(new CropCircleTransformation(getActivity()))
                .into(img_heard);

        img_bg.setImageResource(R.drawable.gradient_flyblue);

        int steps = myPrefs.steps().get();
        int height = myPrefs.Height().get();
        int Weight = myPrefs.Weight().get();

        getMark(steps, height, Weight);
    }

    private void getMark(int steps, int height, int weight) {
        GetMarkClass markClass = new GetMarkClass(steps, weight, height);
        double km = markClass.getKm();
        double mark = markClass.getMark();

        data.setText(steps + "");

        RxTextUtils.setTextView(getContext(), textData_1, steps / 60, Color.parseColor("#ffffff"));
//        textData_1.setText(intToStringMin(steps / 2));
        textData_2.setText(fromatDouble(km, 2));
        textData_3.setText(fromatDouble(mark, 2));
    }

    private String fromatDouble(double i, int scale) {
        BigDecimal bigDecimal = new BigDecimal(i);
        BigDecimal decimal = bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);//保留小数点后2位，直接去掉值。
        return String.valueOf(decimal);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 将拍下来的照片存放到指定缓存文件中
     *
     * @param
     * @throws IOException
     */
    public static String saveToSDCard(Bitmap bitmap) throws IOException {
        String dir = Environment.getExternalStorageDirectory() + "/DCIM/Camera";
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()); // 格式化时间
        String filename = format.format(date) + ".jpg";
//        File fileFolder = new File(cacheDir + "/bracelet_image/");
        File fileFolder = new File(dir);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"braceletImage"的目录
            fileFolder.mkdir();
        }
        File jpgFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
//        outputStream.write(data); // 写入sd卡中
        outputStream.flush();
        outputStream.close(); // 关闭输出流
        return jpgFile.getPath();
    }


    public String checkSleepQ(int asleep, int awake) {
        String sleepStr = getString(R.string.sleepStatusBad);
        if (asleep == 0 && awake == 0) {
            return sleepStr;
        }
        if (asleep < 60 && awake >= 60) {
            sleepStr = getString(R.string.sleepStatusBad);
        } else if (asleep < 60 && awake < 60) {
            sleepStr = getString(R.string.sleepStatusFine);
        } else if (60 <= asleep && asleep < 120 && awake >= 120) {
            sleepStr = getString(R.string.sleepStatusBad);
        } else if (60 <= asleep && asleep < 120 && awake < 120 && awake >= 60) {
            sleepStr = getString(R.string.sleepStatusFine);
        } else if (asleep > 120) {
            sleepStr = getString(R.string.sleepStatusGood);
        } else if (60 <= asleep && asleep < 120 && awake < 60) {
            sleepStr = getString(R.string.sleepStatusGood);
        }

        return sleepStr;
    }


}
