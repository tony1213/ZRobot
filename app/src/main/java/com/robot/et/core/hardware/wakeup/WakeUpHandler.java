package com.robot.et.core.hardware.wakeup;

import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.hardware.sleepawake.SleepAwake;

/**
 * Created by houdeming on 2016/9/16.
 * 与唤醒相关的处理
 */
public class WakeUpHandler {
    private final String TAG = "wakeup";
    private final int AWAKEN_VOICE = 1;// 语音唤醒
    private final int AWAKEN_PRESS = 2;// 短按唤醒
    private final int AWAKEN_TOUCH = 3;// 触摸唤醒
    private int sleepAwakenFd;
    private int allAwakeFd;
    private IWakeUp iWakeUp;

    public WakeUpHandler(IWakeUp iWakeUp) {
        this.iWakeUp = iWakeUp;
        // 初始化什么情况下都唤醒
        allAwakeFd = AllAwake.initAllAwake();
        Log.i(TAG, "allAwakeFd==" + allAwakeFd);
        // 初始化只有沉睡的时候再唤醒
        sleepAwakenFd = SleepAwake.initSleepAwake();
        Log.i(TAG, "sleepAwakenFd==" + sleepAwakenFd);

        allAwaken();
    }

    // 一直唤醒
    private void allAwaken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int allValue = AllAwake.getAllAwakeValue();
                    if (allValue != 65535) {
                        int intSource = allValue & 0xFFFF;
                        Log.i(TAG, "intSource==" + intSource);
                        switch (intSource) {
                            case AWAKEN_VOICE:// 语音唤醒
                                int degree = ((allValue & 0xFFFF0000) >> 16);
                                Log.i(TAG, "degree==" + degree);
                                // 语音板置0
                                AllAwake.setAllAwakePara(AWAKEN_VOICE, 0);
                                iWakeUp.getVoiceWakeUpDegree(degree);

                                break;
                            case AWAKEN_PRESS:// 短按唤醒
                                iWakeUp.shortPress();

                                break;
                            case AWAKEN_TOUCH:// 触摸唤醒
                                int touchId = ((allValue & 0xFFFF0000) >> 16);
                                Log.i(TAG, "touchId==" + touchId);
                                iWakeUp.bodyTouch(touchId);

                                break;

                        }
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "allAwaken InterruptedException");
                    }
                }
            }
        }).start();
    }

    //人脸唤醒
    public void sleepAwaken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 只有沉睡的时候再去检测人体
                while (DataConfig.isSleep) {
                    if (sleepAwakenFd >= 0) {
                        // 获取人体检测的状态值，1代表检测到人体，0代表没有检测到人体
                        int sleepValue = SleepAwake.getSleepAwakeValue();
                        Log.i(TAG, "sleepValue==" + sleepValue);
                        if (sleepValue == 1) {
                            //有人影进入范围
                            Log.i(TAG, "检测到人影");
                            // 发送人体感应
                            iWakeUp.bodyDetection();
                        }
                    }
                    // 每1s读一次
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "sleepAwaken InterruptedException=" + e.getMessage());
                    }
                }
            }
        }).start();
    }
}
