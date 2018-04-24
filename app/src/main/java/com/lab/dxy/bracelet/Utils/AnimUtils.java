package com.lab.dxy.bracelet.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import lecho.lib.hellocharts.util.ChartUtils;

/**
 * 项目名称：TextureViewDome
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/7/19
 */
public class AnimUtils {


    public AnimUtils() {
        throw new RuntimeException("cannot be Instantiated");
    }

    /**
     * 抖动动画
     *
     * @param CycleTimes 动画重复的次数
     *                   抖动方式：左上右下
     */
    public static Animation shakeAnimation(int CycleTimes) {
        Animation translateAnimation = new TranslateAnimation(0, 6, 0, 6);
        translateAnimation.setInterpolator(new CycleInterpolator(CycleTimes));
        translateAnimation.setDuration(1000);
        return translateAnimation;
    }


    /**
     * 点击反馈动画
     *
     * @param view     视图
     * @param duration 时间
     */
    public static void doHeartBeat(View view, final int duration) {

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.4f, 0.9f, 1.0f),
                        ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.4f, 0.9f, 1.0f)
                );
                set.setDuration(duration);
                set.start();
            }
        });
    }


    /**
     * @param triggerView 事件View
     *                    <p>
     *                    带水波动画的Activity跳转
     */

    @SuppressLint("NewApi")
    public static void navigateWithRippleCompat(final Activity activity, final Intent intent,
                                                final View triggerView) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat option = ActivityOptionsCompat.makeClipRevealAnimation(triggerView, 0, 0,
                    triggerView.getMeasuredWidth(), triggerView.getMeasuredHeight());
            ActivityCompat.startActivity(activity, intent, option.toBundle());

            return;
        }

        int[] location = new int[2];
        triggerView.getLocationInWindow(location);
        final int cx = location[0] + triggerView.getWidth() / 2;
        final int cy = location[1] + triggerView.getHeight() / 2;
        final ImageView view = new ImageView(activity);
        view.setBackgroundColor(ChartUtils.pickColor());
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int w = decorView.getWidth();
        int h = decorView.getHeight();
        decorView.addView(view, w, h);
        int finalRadius = (int) Math.sqrt(w * w + h * h) + 1;
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.setDuration(300);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                decorView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        decorView.removeView(view);
                    }
                }, 500);
            }
        });
        anim.start();
    }

    /**
     * 带水波动画
     */
    @SuppressLint("NewApi")
    public static void navigateWithRippleCompat(final Activity activity,
                                                final View triggerView, @ColorRes int color) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat option = ActivityOptionsCompat.makeClipRevealAnimation(triggerView, 0, 0,
                    triggerView.getMeasuredWidth(), triggerView.getMeasuredHeight());
            return;
        }

        int[] location = new int[2];
        triggerView.getLocationInWindow(location);
        final int cx = location[0] + triggerView.getWidth() / 2;
        final int cy = location[1] + triggerView.getHeight() / 2;
        final ImageView view = new ImageView(activity);
        view.setImageResource(color);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int w = decorView.getWidth();
        int h = decorView.getHeight();
        decorView.addView(view, w, h);
        int finalRadius = (int) Math.sqrt(w * w + h * h) + 1;
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        anim.setDuration(500);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                decorView.removeView(view);
            }
        });
        anim.start();
    }


    /**
     * 跳跃动画
     *
     * @param view       视图
     * @param jumpHeight 跳跃高度
     * @param duration   时间
     * @return Animator
     */
    public static Animator doHappyJump(View view, int jumpHeight, int duration) {
        Keyframe scaleXFrame1 = Keyframe.ofFloat(0f, 1.0f);
        Keyframe scaleXFrame2 = Keyframe.ofFloat(0.05f, 1.5f);
        Keyframe scaleXFrame3 = Keyframe.ofFloat(0.1f, 0.8f);
        Keyframe scaleXFrame4 = Keyframe.ofFloat(0.15f, 1.0f);
        Keyframe scaleXFrame5 = Keyframe.ofFloat(0.5f, 1.0f);
        Keyframe scaleXFrame6 = Keyframe.ofFloat(0.55f, 1.5f);
        Keyframe scaleXFrame7 = Keyframe.ofFloat(0.6f, 0.8f);
        Keyframe scaleXFrame8 = Keyframe.ofFloat(0.65f, 1.0f);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofKeyframe("scaleX",
                scaleXFrame1, scaleXFrame2, scaleXFrame3, scaleXFrame4,
                scaleXFrame5, scaleXFrame6, scaleXFrame7, scaleXFrame8);

        Keyframe scaleYFrame1 = Keyframe.ofFloat(0f, 1.0f);
        Keyframe scaleYFrame2 = Keyframe.ofFloat(0.05f, 0.5f);
        Keyframe scaleYFrame3 = Keyframe.ofFloat(0.1f, 1.15f);
        Keyframe scaleYFrame4 = Keyframe.ofFloat(0.15f, 1.0f);
        Keyframe scaleYFrame5 = Keyframe.ofFloat(0.5f, 1.0f);
        Keyframe scaleYFrame6 = Keyframe.ofFloat(0.55f, 0.5f);
        Keyframe scaleYFrame7 = Keyframe.ofFloat(0.6f, 1.15f);
        Keyframe scaleYFrame8 = Keyframe.ofFloat(0.65f, 1.0f);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofKeyframe("scaleY",
                scaleYFrame1, scaleYFrame2, scaleYFrame3, scaleYFrame4,
                scaleYFrame5, scaleYFrame6, scaleYFrame7, scaleYFrame8);

        Keyframe translationY1 = Keyframe.ofFloat(0f, 0f);
        Keyframe translationY2 = Keyframe.ofFloat(0.085f, 0f);
        Keyframe translationY3 = Keyframe.ofFloat(0.2f, -jumpHeight);
        Keyframe translationY4 = Keyframe.ofFloat(0.25f, -jumpHeight);
        Keyframe translationY5 = Keyframe.ofFloat(0.375f, 0f);
        Keyframe translationY6 = Keyframe.ofFloat(0.5f, 0f);
        Keyframe translationY7 = Keyframe.ofFloat(0.585f, 0f);
        Keyframe translationY8 = Keyframe.ofFloat(0.7f, -jumpHeight);
        Keyframe translationY9 = Keyframe.ofFloat(0.75f, -jumpHeight);
        Keyframe translationY10 = Keyframe.ofFloat(0.875f, 0f);
        PropertyValuesHolder translationYHolder = PropertyValuesHolder.ofKeyframe("translationY",
                translationY1, translationY2, translationY3, translationY4, translationY5,
                translationY6, translationY7, translationY8, translationY9, translationY10);

        Keyframe rotationY1 = Keyframe.ofFloat(0f, 0f);
        Keyframe rotationY2 = Keyframe.ofFloat(0.125f, 0f);
        Keyframe rotationY3 = Keyframe.ofFloat(0.3f, -360f * 3);
        PropertyValuesHolder rotationYHolder = PropertyValuesHolder.ofKeyframe("rotationY",
                rotationY1, rotationY2, rotationY3);

        Keyframe rotationX1 = Keyframe.ofFloat(0f, 0f);
        Keyframe rotationX2 = Keyframe.ofFloat(0.625f, 0f);
        Keyframe rotationX3 = Keyframe.ofFloat(0.8f, -360f * 3);
        PropertyValuesHolder rotationXHolder = PropertyValuesHolder.ofKeyframe("rotationX",
                rotationX1, rotationX2, rotationX3);

        ValueAnimator valueAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                scaleXHolder, scaleYHolder, translationYHolder, rotationYHolder, rotationXHolder);
        valueAnimator.setDuration(duration);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.start();
        return valueAnimator;
    }

    /**
     * 缩小
     *
     * @param view
     */
    public static void zoomIn(final View view, float scale, float dist) {
        view.setPivotY(view.getHeight());
        view.setPivotX(view.getWidth() / 2);
        AnimatorSet mAnimatorSet = new AnimatorSet();
        ObjectAnimator mAnimatorScaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, scale);
        ObjectAnimator mAnimatorScaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, scale);
        ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(view, "translationY", 0.0f, -dist);

        mAnimatorSet.play(mAnimatorTranslateY).with(mAnimatorScaleX);
        mAnimatorSet.play(mAnimatorScaleX).with(mAnimatorScaleY);
        mAnimatorSet.setDuration(300);
        mAnimatorSet.start();
    }


    /**
     * f放大
     *
     * @param view
     */
    public static void zoomOut(final View view, float scale) {
        view.setPivotY(view.getHeight());
        view.setPivotX(view.getWidth() / 2);
        AnimatorSet mAnimatorSet = new AnimatorSet();

        ObjectAnimator mAnimatorScaleX = ObjectAnimator.ofFloat(view, "scaleX", scale, 1.0f);
        ObjectAnimator mAnimatorScaleY = ObjectAnimator.ofFloat(view, "scaleY", scale, 1.0f);
        ObjectAnimator mAnimatorTranslateY = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), 0);

        mAnimatorSet.play(mAnimatorTranslateY).with(mAnimatorScaleX);
        mAnimatorSet.play(mAnimatorScaleX).with(mAnimatorScaleY);
        mAnimatorSet.setDuration(300);
        mAnimatorSet.start();
    }

    public static void ScaleUpDowm(View view) {
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1200);
        view.startAnimation(animation);
    }


    public static void animateHeight(int start, int end, final View view) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();//根据时间因子的变化系数进行设置高度
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);//设置高度
            }
        });
        valueAnimator.start();
    }

    /**
     * 颜色渐变动画
     *
     * @param beforeColor 变化之前的颜色
     * @param afterColor  变化之后的颜色
     * @param listener    变化事件
     */
    public static void animationColorGradient(int beforeColor, int afterColor, final onUpdateListener listener) {
        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), beforeColor, afterColor).setDuration(3000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                textView.setTextColor((Integer) animation.getAnimatedValue());
                listener.onUpdate((Integer) animation.getAnimatedValue());
            }
        });
        valueAnimator.start();
    }

    public interface onUpdateListener {
        void onUpdate(int intValue);
    }

    public interface onAnimUpdateListener {
        void onUpdate();
    }


    private void doCountDown(View view, final onAnimUpdateListener listener) {

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator mAnimatorScaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f);
        ObjectAnimator mAnimatorScaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f);
        ObjectAnimator mAnimatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        mAnimatorScaleX.setRepeatCount(3);
        mAnimatorScaleY.setRepeatCount(3);
        mAnimatorAlpha.setRepeatCount(3);
        //mAnimatorScaleX设置的监听只执行开始、结束、者重复
        mAnimatorScaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                L.d("重复");
                listener.onUpdate();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                listener.onUpdate();
            }
        });

        animatorSet.playTogether(mAnimatorScaleX, mAnimatorScaleY, mAnimatorAlpha);
        animatorSet.setDuration(1000);
        animatorSet.setStartDelay(1000);
        animatorSet.start();
        //animatorSet设置的监听只能运行start和end,不能监听到重复

    }


    public static void doAction(ViewGroup view, int duration, float scaleX, float scaleY, float times) {
        try {
            if (view.getScaleX() == 1) {
                ViewCompat.animate(view).setDuration(duration).scaleX(scaleX).scaleY(scaleY).setInterpolator(new CycleInterpolator(times))
                        .setListener(new ViewPropertyAnimatorListener() {

                            @Override
                            public void onAnimationStart(final View view) {
                            }

                            @Override
                            public void onAnimationEnd(final View v) {
                            }

                            @Override
                            public void onAnimationCancel(final View view) {
                            }
                        }).withLayer().start();

                for (int index = 0; index < ((ViewGroup) view).getChildCount(); ++index) {
                    View nextChild = ((ViewGroup) view).getChildAt(index);
                    ViewCompat.animate(nextChild).setDuration(duration).scaleX(scaleX).scaleY(scaleY).setInterpolator(new CycleInterpolator(times))
                            .setListener(new ViewPropertyAnimatorListener() {

                                @Override
                                public void onAnimationStart(final View view) {
                                }

                                @Override
                                public void onAnimationEnd(final View v) {
                                }

                                @Override
                                public void onAnimationCancel(final View view) {
                                }
                            }).withLayer().start();
                }
            }
        } catch (Exception e) {
            L.e("only ViewGroups : likes RelativeLayout, LinearLayout, etc could doAction");
        }
    }


    public static void doAction(View view) {

        try {
            if (view.getScaleX() == 1) {
                ViewCompat.animate(view).setDuration(300).scaleX(0.8f).scaleY(0.8f).setInterpolator(new CycleInterpolator(0.5f))
                        .setListener(new ViewPropertyAnimatorListener() {

                            @Override
                            public void onAnimationStart(final View view) {
                            }

                            @Override
                            public void onAnimationEnd(final View v) {
                            }

                            @Override
                            public void onAnimationCancel(final View view) {
                            }
                        }).withLayer().start();
            }


        } catch (Exception e) {
            L.e("only ViewGroups : likes RelativeLayout, LinearLayout, etc could doAction");
        }
    }


}
