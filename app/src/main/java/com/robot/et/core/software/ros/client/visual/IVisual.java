package com.robot.et.core.software.ros.client.visual;

/**
 * Created by houdeming on 2016/10/8.
 */
public interface IVisual {
    // 初始化视觉
    void initVisual(String msg);

    // 视觉学习初始化
    void initVisualLearn(boolean isSuccess);

    // 视觉人体检测初始化
    void initVisualBody(boolean isSuccess);
}
