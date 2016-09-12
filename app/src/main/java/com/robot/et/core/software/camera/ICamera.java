package com.robot.et.core.software.camera;

/**
 * Created by houdeming on 2016/9/12.
 */
public interface ICamera {
    // 快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作
    void onShutter();

    // 聚焦成功
    void onAutoFocus();

    // 拍照结果的返回
    void onTakePictureInfo(byte[] data);
}
