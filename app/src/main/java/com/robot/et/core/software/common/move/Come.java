package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/10/7.
 * 过来
 */
public class Come {
    private static final String TAG = "come";
    private static Timer timer;
    private static Context context;

    public static void come(Context context, long delay) {
        Come.context = context;
        DataConfig.isComeIng = true;
        timer = TimerManager.createTimer();
        // 1000ms读一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, delay, 1000);
    }

    private static Handler handler = new Handler() {
        @Override

        public void handleMessage(Message msg) {
            Log.i(TAG, "一直走");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 500, 1000, 0);
        }
    };

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
