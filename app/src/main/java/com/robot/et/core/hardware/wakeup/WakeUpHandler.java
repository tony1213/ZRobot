package com.robot.et.core.hardware.wakeup;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;

/**
 * Created by houdeming on 2016/9/16.
 * 与唤醒相关的处理
 */
public class WakeUpHandler {
    private final int VOICE_WAKEUP = 1;
    private final int BODY_DETECTION = 2;
    private final int ROBOT_TOUCH = 3;
    private IWakeUp iWakeUp;
    private int voiceFd;
    private int faceFd;

    public WakeUpHandler(IWakeUp iWakeUp) {
        this.iWakeUp = iWakeUp;
        // 获取人体检测的串口id
        faceFd = WakeUp.faceWakeUpInit();
        Log.i("wakeup", "face faceFd==" + faceFd);
        // 获取语音唤醒的串口id
        voiceFd = WakeUp.wakeUpInit();
        Log.i("wakeup", "voice voiceFd==" + voiceFd);
        // 去检测是否有语音唤醒
        voiceWakeUp();
    }

    //语言唤醒
    private void voiceWakeUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (voiceFd > 0) {
                        // 获取语音唤醒的状态值，当为1的时候代表唤醒，0的时候没有人唤醒
                        int wakeUpState = WakeUp.getWakeUpState();
                        if (wakeUpState == 1) {
                            // 获取唤醒的角度
                            int degree = WakeUp.getWakeUpDegree();
                            Log.i("wakeup", "degree==" + degree);
                            WakeUp.setGainDirection(0);// 设置麦克0为主麦
                            Message msg = handler.obtainMessage();
                            msg.what = VOICE_WAKEUP;
                            msg.arg1 = degree;
                            handler.sendMessage(msg);
                        }
                        // 每20ms读一次
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Log.i("wakeup", "voiceWakeUp InterruptedException=" + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }

    //人脸唤醒
    public void faceWakeUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 只有沉睡的时候再去检测人体
                while (DataConfig.isSleep) {
                    if (faceFd > 0) {
                        // 获取人体检测的状态值，1代表检测到人体，0代表没有检测到人体
                        int faceWakeUpState = WakeUp.getFaceWakeUpState();
                        if (faceWakeUpState == 1) {
                            //有人影进入范围
                            Log.i("wakeup", "检测到人影");
                            // 发送人体感应
                            handler.sendEmptyMessage(BODY_DETECTION);
                        }
                    }
                    // 每1s读一次
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.i("wakeup", "faceWakeUp InterruptedException=" + e.getMessage());
                    }
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case VOICE_WAKEUP:// 语音唤醒
                    iWakeUp.getVoiceWakeUpDegree(msg.arg1);
                    break;
                case BODY_DETECTION:// 检测到人影
                    iWakeUp.bodyDetection();
                    break;
                case ROBOT_TOUCH:// 机器触摸
                    break;
            }
        }
    };
}