package com.robot.et.core.software.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by houdeming on 2016/8/8.
 */
public class BaseService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechImpl.setService(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startSpeak(int speakType, String speakContent) {
    }

    public void cancelSpeak() {
    }

    public void startListen() {
    }

    public void cancelListen() {
    }

    public void understanderTextByIfly(String content) {
    }

    public void understanderTextByTuring(String content) {
    }

}
