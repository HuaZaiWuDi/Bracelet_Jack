package com.lab.dxy.bracelet.Utils.timer;

import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Oden on 2016/6/16.
 */
public class MyPeriodTimer {
    private Timer timer = null;
    private TimerTask task = null;
    private MyPeriodTimerListener myTimerListener;
    private long period;
    private long delay;
    private android.os.Handler sHandler = new android.os.Handler(Looper.getMainLooper());


    public MyPeriodTimer(long delay, long period, MyPeriodTimerListener l) {
        this.period = period;
        this.delay = delay;
        this.myTimerListener = l;
    }

    public void startTimer() {
        if (timer == null) {
            Log.d("[MyPeriodTimer]", "startTimer");
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    //切换到主线程
                    sHandler.post(() -> myTimerListener.enterTimer());
                }
            };
            timer.schedule(task, delay, period);
        }
    }

    public void stopTimer() {
        if (timer != null) {
            Log.d("[MyPeriodTimer]", "stopTimer");
            task.cancel();
            timer.cancel();

            task = null;
            timer = null;
        }
    }

}
