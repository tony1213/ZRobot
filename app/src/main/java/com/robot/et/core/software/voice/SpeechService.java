package com.robot.et.core.software.voice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;

/**
 * Created by houdeming on 2016/8/8.
 */
public class SpeechService extends Service {
    public SharedPreferencesUtils share;
    public String city;// 当前城市
    public String area;// 当前区域

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        share = SharedPreferencesUtils.getInstance();
        city = share.getString(SharedPreferencesKeys.CITY_KEY, "");
        area = share.getString(SharedPreferencesKeys.AREA_KEY, "");

    }

    /*
    开始说话
    speakType  说话的类型
    speakContent  说话的内容
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

    /*
    科大讯飞文本理解
    content 要理解的内容
     */
    public void understanderTextByIfly(String content) {

    }

    /*
    图灵文本理解
    content 要理解的内容
     */
    public void understanderTextByTuring(String content) {

    }
}
