package com.robot.et.core.hardware.light;

import android.util.Log;

import com.robot.et.common.EarsLightConfig;

/**
 * Created by houdeming on 2016/9/18.
 */
public class LightHandler {
    private static final String TAG = "light";
    private static int lightState;
    private static boolean isStart = false;

    public LightHandler() {
        // 初始化耳朵灯
        int earsLightFd = EarsLight.initEarsLight();
        Log.i(TAG, "earsLightFd==" + earsLightFd);
        // 初始化照明灯
        int floodLightFd = FloodLight.initFloodLight();
        Log.i(TAG, "floodLightFd==" + floodLightFd);
    }

    // 设置耳朵灯的状态
    public void setEarsLight(int lightState) {
        Log.i(TAG, "lightState==" + lightState);
        this.lightState = lightState;
        isStart = false;

        switch (lightState) {
            case EarsLightConfig.EARS_CLOSE:
                EarsLight.setLightStatus(lightState);

                break;
            case EarsLightConfig.EARS_BRIGHT:
                EarsLight.setLightStatus(lightState);

                break;
            case EarsLightConfig.EARS_BLINK:
                isStart = true;
                controlEarsLight();

                break;
            case EarsLightConfig.EARS_CLOCKWISE_TURN:
                isStart = true;
                controlEarsLight();

                break;
            case EarsLightConfig.EARS_ANTI_CLOCKWISE_TURN:
                isStart = true;
                controlEarsLight();

                break;
            case EarsLightConfig.EARS_HORSE_RACE_LAMP:
                isStart = true;
                controlEarsLight();

                break;
            default:
                break;
        }
    }

    // 设置照明灯的状态
    public void setFloodLight(int lightState) {
        FloodLight.setLightStatus(lightState);
    }

    // 控制耳朵灯
    private void controlEarsLight() {
        // 不要用timer计时器来控制，如果时间较长的话，会对其他线程造成影响
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStart) {
                    Log.i(TAG, "controlEarsLight lightState==" + lightState);
                    EarsLight.setLightStatus(lightState);
                    try {
                        Thread.sleep(5000);// 5s执行一次（时间太短的话可能会造成影响）
                    } catch (InterruptedException e) {
                        Log.i(TAG, "controlEarsLight InterruptedException==" + e.getMessage());
                    }
                }
            }
        }).start();
    }
}
