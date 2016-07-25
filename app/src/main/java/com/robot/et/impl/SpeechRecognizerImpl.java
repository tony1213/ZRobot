package com.robot.et.impl;

/**
 * Created by houdeming on 2016/7/25.
 * 语音听写的接口
 */
public interface SpeechRecognizerImpl {
    //开始语音听写
    public void startListen();
    //取消语音听写
    public void cancelListen();

}
