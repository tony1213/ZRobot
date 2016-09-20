package com.robot.et.core.software.voice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by houdeming on 2016/8/8.
 */
public class SpeechService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 开始说话
     *
     * @param speakType    说话的类型
     * @param speakContent 说话的内容
     */
    public void startSpeak(int speakType, String speakContent) {

    }

    // 取消说话
    public void cancelSpeak() {

    }

    // 开始听
    public void startListen() {

    }

    // 取消听
    public void cancelListen() {

    }

    /**
     * 文本理解
     *
     * @param content 要理解的内容
     */
    public void understanderText(String content) {

    }
}
