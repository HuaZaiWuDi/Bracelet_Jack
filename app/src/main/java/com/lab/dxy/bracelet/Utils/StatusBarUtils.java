package com.lab.dxy.bracelet.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 项目名称：
 * 类描述：沉浸式状态栏,新增更改状态栏颜色，去掉状态栏等
 * 创建人：Jack
 * 创建时间：2017/6/5
 * 使用方法：StatusBarUtils.from(Context).setTransparentStatusbar(true).process();
 */
public final class StatusBarUtils {

    boolean lightStatusBar;
    //透明且背景不占用控件的statusbar，这里估且叫做沉浸
    boolean transparentStatusBar;
    Window window;
    View actionBarView;
    boolean hindStatusBar;
    int statusBarColor;

    private StatusBarUtils(Window window, boolean lightStatusBar, boolean transparentStatusBar,
                           View actionBarView, boolean hindStatusBar, int statusBarColor) {
        this.lightStatusBar = lightStatusBar;
        this.transparentStatusBar = transparentStatusBar;
        this.window = window;
        this.actionBarView = actionBarView;
        this.hindStatusBar = hindStatusBar;
        this.statusBarColor = statusBarColor;
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLessKitkat() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    public static boolean isMoreLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static Builder from(Activity activity) {
        return new StatusBarUtils.Builder().setWindow(activity);
    }

    public static Builder from(Dialog dialog) {
        return new StatusBarUtils.Builder().setWindow(dialog);
    }

    public static Builder from(Window window) {
        return new StatusBarUtils.Builder().setWindow(window);
    }

    public static Builder from(FragmentActivity window) {
        return new StatusBarUtils.Builder().setWindow(window);
    }

    public static Builder from(PreferenceActivity window) {
        return new StatusBarUtils.Builder().setWindow(window);
    }

    public static Builder from(Fragment window) {
        return new StatusBarUtils.Builder().setWindow(window);
    }

    public static Builder from(android.support.v4.app.Fragment window) {
        return new StatusBarUtils.Builder().setWindow(window);
    }

    /**
     * Default status dp = 24 or 25
     * mhdpi = dp * 1
     * hdpi = dp * 1.5
     * xhdpi = dp * 2
     * xxhdpi = dp * 3
     * eg : 1920x1080, xxhdpi, => status/all = 25/640(dp) = 75/1080(px)
     * <p>
     * don't forget toolbar's dp = 48
     *
     * @return px
     */
    @IntRange(from = 0, to = 75)
    public static int getStatusBarOffsetPx(Context context) {
        if (isLessKitkat()) {
            return 0;
        }
        Context appContext = context.getApplicationContext();
        int result = 0;
        int resourceId =
                appContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = appContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void processActionBar(final View v) {
        if (v == null || !transparentStatusBar || isLessKitkat()) {
            return;
        }
        v.post(new Runnable() {
            @Override
            public void run() {
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + getStatusBarOffsetPx(v.getContext()),
                        v.getPaddingRight(),
                        v.getPaddingBottom());
                v.getLayoutParams().height += getStatusBarOffsetPx(v.getContext());
            }
        });
    }

    /**
     * 调用私有API处理颜色
     */
    public void processPrivateAPI() {
        processFlyMe(lightStatusBar);
        processMIUI(lightStatusBar);
    }

    public void process() {
        //处理4.4沉浸
        if (isKitkat()) {
            processKitkat();
        }
        //6.0处理沉浸与颜色，5.0只可以处理沉浸(不建议用白色背景)
        if (isMoreLollipop()) {
            processLollipopAbove();
        }
        //调用私有API处理颜色
        processPrivateAPI();
        processActionBar(actionBarView);

        if (hindStatusBar) {
//          去掉Activity上面的状态栏
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        /**
         * 渲染式状态栏颜色（只改变状态栏颜色）
         */
        if (statusBarColor != -1) {
            if (isMoreLollipop()) {
                //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                //设置状态栏颜色

                window.setStatusBarColor(statusBarColor);

                ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
                View mChildView = mContentView.getChildAt(0);
                if (mChildView != null) {
                    //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                    ViewCompat.setFitsSystemWindows(mChildView, true);
                }
            }
        }
    }

    /**
     * 处理4.4沉浸
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    void processKitkat() {
        WindowManager.LayoutParams winParams = window.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (transparentStatusBar) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        window.setAttributes(winParams);
    }

    /**
     * 改变小米的状态栏字体颜色为黑色, 要求MIUI6以上
     * Tested on: MIUIV7 5.0 Redmi-Note3
     */
    void processMIUI(boolean lightStatusBar) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, lightStatusBar ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception ignored) {

        }
    }

    /**
     * 改变魅族的状态栏字体为黑色，要求FlyMe4以上
     */
    private void processFlyMe(boolean isLightStatusBar) {
        WindowManager.LayoutParams lp = window.getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (isLightStatusBar) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception ignored) {
            //
        }
    }

    /**
     * 处理Lollipop以上
     * Lollipop可以设置为沉浸，不能设置字体颜色(所以白色背景会很丑)
     * M(API23)可以设定
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void processLollipopAbove() {
        int flag = window.getDecorView().getSystemUiVisibility();
        if (lightStatusBar) {
            /**
             * see {@link <a href="https://developer.android.com/reference/android/R.attr.html#windowLightStatusBar"></a>}
             */
            flag |= (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (transparentStatusBar) {
            //改变字体颜色
            flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        window.getDecorView().setSystemUiVisibility(flag);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    final public static class Builder {
        private Window window;
        private boolean lightStatusBar = false;
        private boolean transparentStatusbar = false;
        private View actionBarView;
        private boolean hindStatusBar = false;
        private int statusBarColor = -1;

        public Builder setActionbarView(@Nullable View actionbarView) {
            this.actionBarView = actionbarView;
            return this;
        }

        private Builder setWindow(@NonNull Window Window) {
            this.window = Window;
            return this;
        }

        private Builder setWindow(@NonNull Activity activity) {
            this.window = activity.getWindow();
            return this;
        }

        private Builder setWindow(@NonNull Dialog dialog) {
            this.window = dialog.getWindow();
            return this;
        }

        private Builder setWindow(@NonNull Fragment fragment) {
            this.window = fragment.getActivity().getWindow();
            return this;
        }

        private Builder setWindow(@NonNull android.support.v4.app.Fragment fragment) {
            this.window = fragment.getActivity().getWindow();
            return this;
        }

        private Builder setWindow(@NonNull FragmentActivity fragment) {
            this.window = fragment.getWindow();
            return this;
        }

        private Builder setWindow(@NonNull PreferenceActivity activity) {
            this.window = activity.getWindow();
            return this;
        }

        public Builder setLightStatusBar(boolean lightStatusBar) {
            this.lightStatusBar = lightStatusBar;
            return this;
        }

        public Builder setTransparentStatusbar(boolean transparentStatusbar) {
            this.transparentStatusbar = transparentStatusbar;
            return this;
        }

        public Builder setHindStatusBar(boolean hindStatusBar) {
            this.hindStatusBar = hindStatusBar;
            return this;
        }

        public Builder setStatusBarColor(@ColorInt int color) {
            statusBarColor = color;
            return this;
        }

        public void process() {
            new StatusBarUtils(window, lightStatusBar, transparentStatusbar, actionBarView, hindStatusBar, statusBarColor).process();
        }
    }

}