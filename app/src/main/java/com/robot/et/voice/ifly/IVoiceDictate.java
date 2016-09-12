package com.robot.et.voice.ifly;

import com.iflytek.cloud.SpeechError;

/**
 * Created by houdeming on 2016/9/5.
 * 语音听写接口
 */
public interface IVoiceDictate {
    // 开始语音听写
    void onBeginOfSpeech();

    // 语音听写结束
    void onEndOfSpeech();

    // 语音听写时音量返回值
    void onVolumeChanged(int volume, byte[] data);

    // 语音听写错误
    void onError(SpeechError error);

    // 返回语音听写结果
    void onResult(String result);
}
