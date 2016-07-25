package com.robot.et.impl;

/**
 * Created by houdeming on 2016/7/25.
 * 语音合成接口
 */
public interface SpeechSynthesizerImpl {
    //开始语音合成
    public void startSpeak(int speakType, String speakContent);
    //取消语音合成
    public void cancelSpeak();

}
