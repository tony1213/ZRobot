package com.robot.et.core.software.common.speech.voice.ifly;

import com.iflytek.cloud.SpeechError;

/**
 * Created by houdeming on 2016/9/5.
 * 文本理解接口
 */
public interface ITextUnderstand {
    // 文本理解结果
    void onResult(String result);

    // 文本理解错误
    void onError(SpeechError error);
}
