package com.robot.et.core.software.common.speech.voice.turing;

import turing.os.http.core.ErrorMessage;

/**
 * Created by houdeming on 2016/9/5.
 * 图灵接口
 */
public interface ITuring {
    // 图灵文本理解结果
    void onResult(String result);

    // 图灵文本理解错误
    void onError(ErrorMessage errorMessage);
}
