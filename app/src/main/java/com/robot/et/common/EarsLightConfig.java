package com.robot.et.common;

/**
 * Created by houdeming on 2016/9/13.
 * 耳朵灯的状态（控制灯时，发送给硬件的信息）
 */
public class EarsLightConfig {
    // 灭
    public static final int EARS_CLOSE = 0;
    // 亮
    public static final int EARS_BRIGHT = 1;
    // 闪烁
    public static final int EARS_BLINK = 2;
    // 顺时针旋转
    public static final int EARS_CLOCKWISE_TURN = 3;
    // 逆时针旋转
    public static final int EARS_ANTI_CLOCKWISE_TURN = 4;
    // 跑马灯
    public static final int EARS_HORSE_RACE_LAMP = 5;
}
