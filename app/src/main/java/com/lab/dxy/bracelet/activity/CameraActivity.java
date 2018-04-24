package com.lab.dxy.bracelet.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lab.dxy.bracelet.Contents;
import com.lab.dxy.bracelet.R;
import com.lab.dxy.bracelet.Utils.L;
import com.lab.dxy.bracelet.Utils.MyPrefs_;
import com.lab.dxy.bracelet.Utils.StatusBarUtils;
import com.lab.dxy.bracelet.Utils.Utils;
import com.lab.dxy.bracelet.base.BaseActivity;
import com.lab.dxy.bracelet.ble.MyBle;
import com.lab.dxy.bracelet.ui.FoucsView;
import com.lab.dxy.bracelet.ui.RecordView;
import com.lab.dxy.bracelet.ui.RxToast;
import com.syd.oden.odenble.Utils.HexUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.attr.layout_width;
import static android.view.View.VISIBLE;
import static com.inuker.bluetooth.library.Constants.ACTION_CHARACTER_CHANGED;
import static com.inuker.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.lab.dxy.bracelet.service.BleService.isConnected;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/22
 */

@EActivity(R.layout.activity_camera)
public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PictureCallback {
    public static File cacheDir;//照片路径

    private SurfaceHolder holder;
    private Camera camera;
    public int cameraPosition = 1;//0代表前置摄像头,1代表后置摄像头,默认打开前置摄像头
    private int cameraCount = 0;//获得相机的摄像头数量
    private Display display;
    private boolean isOpen = true;
    private FoucsView mFoucsView;
    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private boolean isTakePhoto = false;
    String path = "1";

    @ViewById
    SurfaceView mSurfaceView;
    @ViewById
    ImageView bright;
    @ViewById
    ImageView img;
    @ViewById
    FrameLayout parent;
    @ViewById
    RecordView mRecordView;
    @ViewById
    ImageView img_album;
    @ViewById
    ImageView camera_flip;

    @Pref
    MyPrefs_ myPrefs;


    @Extra
    int type;

    //5a0d009001000000000000000000000000000000
    @Receiver(actions = ACTION_CHARACTER_CHANGED)
    protected void onActionCharacterChanged(@Receiver.Extra(EXTRA_BYTE_VALUE) byte[] data) {
        L.d("readShake:" + HexUtil.encodeHexStr(data));
        if (data[1] == 0x16 && data[3] == 0x11) {
            L.d("手环动作指令：拍照");
            doShake();

            MyBle.getInstance().contorlApp(data[3], true);
        }
    }

    @Click
    void img_album() {
//        openAlbum(CameraActivity.this);
        Intent intent = new Intent(this, ImageActivity_.class);
        intent.putExtra("path", path);
        startActivity(intent);
    }


    @Click
    void back() {
        onBackPressed();
    }

    @Click
    void camera_flip() {//前后摄像头
        //交换摄像头
        changeCamera();
        myPrefs.edit().camera().put(cameraPosition).apply();
    }

    @Click
    void bright() {
        if (cameraPosition == 1) {
            Camera.Parameters p = camera.getParameters();
            if (isOpen) {
                isOpen = false;
                Glide.with(this)
                        .load(R.mipmap.camera_flash_on)
                        .into(bright);

                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);
            } else {
                isOpen = true;
                Glide.with(this)
                        .load(R.mipmap.camera_flash_off)
                        .into(bright);

                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(p);
            }
        }
    }


    @Click
    void takePhoto() {//拍照
        doShake();
    }


    @UiThread
    public void doShake() {
        try {
            mRecordView.setVisibility(VISIBLE);
            mRecordView.startCountDownView(3);
            mRecordView.setOnCountDownListener(() -> {
                mRecordView.setVisibility(View.GONE);
                L.e("CameraActivity", "开始时间" + new Date());
                if (camera != null && !isTakePhoto) {
                    isTakePhoto = true;
                    camera.takePicture(null, null, CameraActivity.this);
                }
            });

        } catch (Exception e) {
            L.e(e.getMessage());
        }
    }


    @AfterViews
    void initView() {
        initCamera();
        L.d("type:" + type);
        if (isConnected)
            if (type == 0) {
                MyBle.getInstance().keyOrder(true, 60 * 5);
            } else {
                MyBle.getInstance().shakeTakePhoto(true);
            }
    }

    private void initCamera() {
        //初始化surfaceview预览效果,照相机预览的空间
        holder = mSurfaceView.getHolder();//获得句柄
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前
        holder.setKeepScreenOn(true);// 屏幕常亮
        holder.addCallback(this);// 为SurfaceView的句柄添加一个回调函数

        //mFoucsView
        mFoucsView = new FoucsView(this, 200);
        FrameLayout.LayoutParams foucs_param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        foucs_param.gravity = Gravity.CENTER;
        mFoucsView.setLayoutParams(foucs_param);
        mFoucsView.setVisibility(View.INVISIBLE);
        parent.addView(mFoucsView);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.from(this).setHindStatusBar(true).process();

        try {
            cacheDir = new File(this.getExternalCacheDir().getAbsolutePath());
            cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

            if (cameraCount < 2) {
                camera_flip.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            RxToast.warning(getString(R.string.SDNoUse));
        }

        // 获取屏幕信息
        WindowManager mManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        display = mManger.getDefaultDisplay();

    }


    /**
     * handler touch focus
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if ((int) (result - firstTouchLength) / 50 != 0) {
                        firstTouch = true;
//                        CameraInterface.getInstance().setZoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }


    /**
     * focusview animation
     */
    private void setFocusViewWidthAnimation(float x, float y) {
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }

        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();

        handleFocus(this, x, y);

        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFoucsView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFoucsView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mFoucsView.setVisibility(View.VISIBLE);
            }
        });
    }


    private void reStartCamera() {
        Utils.mSleep(300);
        camera.stopPreview();//停掉原来摄像头的预览
        camera.release();//释放资源
        camera = null;//取消原来摄像头
        camera = Camera.open(cameraPosition == 1 ? 0 : 1);//打开当前选中的摄像头
        try {
            camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
            camera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setParameter();
        camera.startPreview();//开始预览
        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦
    }


    @UiThread
    public void notifyAlbum(String path) {
        if (isDestroyed()) return;
        Glide.with(CameraActivity.this)
                .load(path)
                .asBitmap()
                .into(img_album);

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        intent.setData(uri);
        sendBroadcast(intent);
        //// TODO: 2017/11/3 延迟拍照
        isTakePhoto = false;
    }


    /**
     * 拍照完成的回调
     *
     * @param data
     * @param Mycamera
     */
    @Override
    public void onPictureTaken(final byte[] data, Camera Mycamera) {

        reStartCamera();

        new Thread(() -> {
            try {

                Bitmap bitmap = byte2Bitmap(data);

                path = saveToSDCard(bitmap); // 保存图片到sd卡中

                notifyAlbum(path);

                L.e("结束时间" + new Date());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void transImage(String fromFile, String toFile, int width, int height, int quality) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(fromFile);
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            // 缩放图片的尺寸
            float scaleWidth = (float) width / bitmapWidth;
            float scaleHeight = (float) height / bitmapHeight;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 产生缩放后的Bitmap对象
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
            // save file
            File myCaptureFile = new File(toFile);
            FileOutputStream out = new FileOutputStream(myCaptureFile);
            if (resizeBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();//记得释放资源，否则会内存溢出
            }
            if (!resizeBitmap.isRecycled()) {
                resizeBitmap.recycle();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 设置照片格式
     */
    private void setParameter() {
        Camera.Parameters parameters = camera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setJpegQuality(50); // 设置照片质量
        //获得相机支持的照片尺寸,选择合适的尺寸,这个会影响图片大小。压缩图片尺寸，降低照片质量
        List<Camera.Size> sizes = null;
        List<Camera.Size> ShowSizes = null;
        try {
            sizes = parameters.getSupportedPictureSizes();
            ShowSizes = parameters.getSupportedPreviewSizes();

            int maxSize = Math.max(display.getWidth(), display.getHeight());
            int length = sizes.size();
            int showLength = ShowSizes.size();
            if (maxSize > 0) {
                for (int i = 0; i < length; i++) {
                    if (maxSize <= Math.min(sizes.get(i).width, sizes.get(i).height)) {
                        L.d("图片分辨率：" + sizes.get(i).width + "---" + sizes.get(i).height);
                        parameters.setPictureSize(sizes.get(i).width, sizes.get(i).height);
                        break;
                    }
                }
            }
            // 设置预览照片的大小
            if (maxSize > 0) {
                for (int i = 0; i < showLength; i++) {
                    if (maxSize <= Math.min(ShowSizes.get(i).width, ShowSizes.get(i).height)) {
                        parameters.setPreviewSize(ShowSizes.get(i).width, ShowSizes.get(i).height);
                        break;
                    }
                }
            }
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            L.d("图片尺寸异常");
        }
    }

    /**
     * 改变摄像头
     */
    private void changeCamera() {
        //切换前后摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        camera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setParameter();
                    camera.startPreview();//开始预览
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        camera.setDisplayOrientation(getPreviewDegree(CameraActivity.this));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setParameter();
                    camera.startPreview();//开始预览
                    camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦
                    cameraPosition = 1;
                    break;
                }
            }
        }
    }

    /**
     * 查找前置摄像头
     *
     * @return
     */
    private int findFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

    /**
     * 查找后置摄像头
     *
     * @return
     */
    private int findBackCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

    //将照片改为竖直方向
    private Bitmap byte2Bitmap(byte[] data) {
        //将照片改为竖直方向
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        switch (cameraPosition) {
            case 0://前
                matrix.preRotate(270);
                break;
            case 1:
                matrix.preRotate(90);
                break;
        }
//                    matrix.postScale(0.7f, 0.7f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    /**
     * 将拍下来的照片存放到指定缓存文件中
     *
     * @param
     * @throws IOException
     */
    public static String saveToSDCard(Bitmap bitmap) throws IOException {
        String dir = Environment.getExternalStorageDirectory() + Contents.DOWNLOAD_FOLDER + Contents.CAMERA_FOLDER;
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

    /**
     * 用于根据手机方向获得相机预览画面旋转的角度
     *
     * @param activity
     * @return
     */
    public int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            int CammeraIndex = findBackCamera();
            if (CammeraIndex == -1) {
                CammeraIndex = findFrontCamera();
            }
            camera = Camera.open(CammeraIndex);// 打开摄像头
            camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
            camera.setDisplayOrientation(90);
            setParameter();
            camera.startPreview(); // 开始预览
            camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.release(); // 释放照相机
            camera = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isConnected) {
            if (type == 0)
                MyBle.getInstance().keyOrder(false, 60 * 5);
            else
                MyBle.getInstance().shakeTakePhoto(false);
        }
    }

    public boolean handleFocus(final Context context, final float x, final float y) {
        if (camera == null) {
            return false;
        }
        final Camera.Parameters params = camera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f, context);
        camera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            return true;
        }

        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);
            camera.autoFocus((success, camera) -> {
                if (success) {
                    Camera.Parameters params1 = camera.getParameters();
                    params1.setFocusMode(currentFocusMode);
                    camera.setParameters(params1);
                } else {
                    handleFocus(context, x, y);
                }
            });
        } catch (Exception e) {
            L.d("对焦异常");
        }
        return false;
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Context context) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / getScreenHeight(context) * 2000 - 1000);
        int centerY = (int) (y / getScreenWidth(context) * 2000 - 1000);
//        Log.i("CJT", "FocusArea centerX = " + centerX + " , centerY = " + centerY);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
                .bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }


    //之后直接读取图片进行压缩：
    public static Bitmap scalePicture(String filename, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            BitmapFactory.decodeFile(filename, opts);
            int srcWidth = opts.outWidth;
            int srcHeight = opts.outHeight;
            int desWidth = 0;
            int desHeight = 0;
            // 缩放比例
            double ratio = 0.0;
            if (srcWidth > srcHeight) {
                ratio = srcWidth / maxWidth;
                desWidth = maxWidth;
                desHeight = (int) (srcHeight / ratio);
            } else {
                ratio = srcHeight / maxHeight;
                desHeight = maxHeight;
                desWidth = (int) (srcWidth / ratio);
            }
            // 设置输出宽度、高度
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inSampleSize = (int) (ratio) + 1;
            newOpts.inJustDecodeBounds = false;
            newOpts.outWidth = desWidth;
            newOpts.outHeight = desHeight;
            bitmap = BitmapFactory.decodeFile(filename, newOpts);

        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }

}
