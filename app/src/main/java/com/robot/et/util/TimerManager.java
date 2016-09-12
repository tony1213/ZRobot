package com.robot.et.util;

import java.util.Timer;

/**
 * Created by houdeming on 2016/9/7.
 * Timer计时器管理
 */
public class TimerManager {

    // 创建计时器
    public static Timer createTimer() {
        return new Timer();
    }

    // 取消计时
    public static void cancelTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
}
