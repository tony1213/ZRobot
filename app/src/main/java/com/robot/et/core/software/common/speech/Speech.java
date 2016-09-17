package com.robot.et.core.software.common.speech;

/**
 * Created by houdeming on 2016/8/8.
 */
public interface Speech {
    // 开始语音合成
    void startSpeak(int speakType, String speakContent);

    // 取消语音合成
    void cancelSpeak();

    // 开始语音听写
    void startListen();

    // 取消语音听写
    void cancelListen();

    // 理解文本
    void understanderText(String content);
}
