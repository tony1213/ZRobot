package com.robot.et.core.hardware.light;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.EarsLightConfig;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/9/18.
 */
public class EarsLightHandler {
    private static final String TAG = "light";
    private Timer timer;
    private int lightState;

    static {
        int lightFd = EarsLight.initEarsLight();
        Log.i(TAG, "lightFd==" + lightFd);
    }

    // 设置耳朵灯的状态
    public void setLight(int lightState) {
        Log.i(TAG, "lightState==" + lightState);
        this.lightState = lightState;
        switch (lightState) {
            case EarsLightConfig.EARS_CLOSE:
                stopTime();
                EarsLight.setLightStatus(lightState);
                break;
            case EarsLightConfig.EARS_BRIGHT:
                stopTime();
                EarsLight.setLightStatus(lightState);
                break;
            case EarsLightConfig.EARS_BLINK:
                startTimer();
                break;
            case EarsLightConfig.EARS_CLOCKWISE_TURN:
                startTimer();
                break;
            case EarsLightConfig.EARS_ANTI_CLOCKWISE_TURN:
                startTimer();
                break;
            case EarsLightConfig.EARS_HORSE_RACE_LAMP:
                startTimer();
                break;
            default:
                break;
        }
    }

    private void startTimer() {
        stopTime();
        timer = TimerManager.createTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 5 * 1000);// 5s执行一次（时间太短的话可能会造成影响）
    }

    private void stopTime() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            EarsLight.setLightStatus(lightState);
        }
    };
}
