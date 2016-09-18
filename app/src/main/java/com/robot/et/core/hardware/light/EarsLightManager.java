package com.robot.et.core.hardware.light;

import android.util.Log;

/**
 * Created by houdeming on 2016/9/18.
 */
public class EarsLightManager {
    private final String TAG = "light";

    static {
        int lightFd = EarsLight.initEarsLight();
        Log.i(TAG, "lightFd==" + lightFd);
    }

    // 设置耳朵灯的状态
    public static void setLight(int lightState) {
        int lightResult = EarsLight.setLightStatus(lightState);
        Log.i(TAG, "lightResult==" + lightResult);
    }
}
