package com.robot.et.core.hardware.light;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.EarsLightConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/9/18.
 */
public class EarsLightManager {
    private static final String TAG = "light";
    private static Timer timer;
    private static int lightState;

    static {
        int lightFd = EarsLight.initEarsLight();
        Log.i(TAG, "lightFd==" + lightFd);
    }

    // 设置耳朵灯的状态
    public static void setLight(int lightState) {
        Log.i(TAG, "lightState==" + lightState);
        EarsLightManager.lightState = lightState;
        int lightResult = 0;
        switch (lightState) {
            case EarsLightConfig.EARS_CLOSE:
                stopTime();
                lightResult = EarsLight.setLightStatus(lightState);
                break;
            case EarsLightConfig.EARS_BRIGHT:
                stopTime();
                lightResult = EarsLight.setLightStatus(lightState);
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
        Log.i(TAG, "lightResult1==" + lightResult);
    }

    private static void startTimer() {
        stopTime();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 5 * 1000);// 5s执行一次（时间太短的话可能会造成影响）
    }

    private static void stopTime() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int lightResult = EarsLight.setLightStatus(lightState);
            Log.i(TAG, "lightResult2==" + lightResult);
        }
    };
}
