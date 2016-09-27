package com.robot.et.core.hardware.wakeup;

/**
 * Created by houdeming on 2016/9/16.
 */
public interface IWakeUp {
    // 获取语音唤醒角度
    void getVoiceWakeUpDegree(int degree);

    // 人体检测
    void bodyDetection();

    // 触摸
    void bodyTouch(int touchId);

    // 短按
    void shortPress();
}
