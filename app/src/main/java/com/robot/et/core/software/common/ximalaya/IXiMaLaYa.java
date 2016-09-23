package com.robot.et.core.software.common.ximalaya;

/**
 * Created by houdeming on 2016/9/18.
 * 喜马拉雅
 */
public interface IXiMaLaYa {
    // 开始播放
    void onPlayStart();

    // 播放完成
    void onSoundPlayComplete();

    // 播放器错误
    void onPlayError();
}
