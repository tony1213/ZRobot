package com.robot.et.core.software.common.speech.voice.ifly;

import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;

/**
 * Created by houdeming on 2016/9/3.
 * 科大讯飞父类
 */
public class Voice {
    protected final String TAG = "voice";

    // 初始化监听
    protected InitListener initListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {// 初始化失败,错误码
                Log.i(TAG, "InitListener  初始化失败,错误码==" + code);
            } else {// 初始化成功，之后可以调用startSpeaking方法
                Log.i(TAG, "InitListener  初始化成功");
            }
        }
    };
}
