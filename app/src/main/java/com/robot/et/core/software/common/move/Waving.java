package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/10/4.
 * 摆手
 */
public class Waving {
    private static final String TAG = "waving";
    private static Context context;
    private static final String ANGLE_FORWARD = "80";// 向前摆的角度
    private static final String ANGLE_BACK = "-40";// 向后摆的角度
    private static final int WAVING_TIME = 1000;// 摆的时间
    private static Timer timer;

    // 摆手
    public static void waving(Context context) {
        Waving.context = context;
        DataConfig.isWaving = true;
        wavingCount = 0;
        isFirst = false;
        // 摆前先归位
        wavingStop();
        timer = TimerManager.createTimer();
        // 1000ms执行一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    private static int wavingCount; // 摆动的次数
    private static boolean isFirst;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 2次一个循环
            wavingCount++;
            Log.i(TAG, "wavingCount==" + wavingCount);
            int moveTime;
            if (!isFirst) {
                isFirst = true;
                moveTime = WAVING_TIME;
            } else {
                moveTime = 2 * WAVING_TIME;
            }
            switch (wavingCount) {
                case 1:
                    wavingForward(moveTime);
                    break;
                case 2:
                    wavingCount = 0;
                    wavingBack(moveTime);
                    break;
                default:
                    break;
            }
        }
    };

    // 前摆
    private static void wavingForward(int moveTime) {
        Log.i(TAG, "前摆");
        controlArm(ScriptConfig.HAND_LEFT, ANGLE_FORWARD, moveTime);
        controlArm(ScriptConfig.HAND_RIGHT, ANGLE_BACK, moveTime);
    }

    // 后摆
    private static void wavingBack(int moveTime) {
        Log.i(TAG, "后摆");
        controlArm(ScriptConfig.HAND_LEFT, ANGLE_BACK, moveTime);
        controlArm(ScriptConfig.HAND_RIGHT, ANGLE_FORWARD, moveTime);
    }

    // 归位
    private static void wavingStop() {
        Log.i(TAG, "归位");
        controlArm(ScriptConfig.HAND_LEFT, "0", WAVING_TIME);
        controlArm(ScriptConfig.HAND_RIGHT, "0", WAVING_TIME);
    }

    private static void controlArm(String handCategory, String angle, int moveTime) {
        BroadcastEnclosure.controlArm(context, handCategory, angle, moveTime);
    }

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
