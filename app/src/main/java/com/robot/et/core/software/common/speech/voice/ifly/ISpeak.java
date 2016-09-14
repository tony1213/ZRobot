package com.robot.et.core.software.common.speech.voice.ifly;

import com.iflytek.cloud.SpeechError;

/**
 * Created by houdeming on 2016/9/5.
 * 语音合成的接口
 */
public interface ISpeak {
    // 开始说
    void onSpeakBegin();

    // 合成完成
    void onCompleted(SpeechError error);
}
