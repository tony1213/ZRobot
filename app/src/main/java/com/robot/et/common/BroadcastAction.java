package com.robot.et.common;

/**
 * Created by Tony on 2016/7/25.
 */
public class BroadcastAction {
    //串口数据接收
    public static String ACTION_MOVE_TO_SERIALPORT ="action.control.robot.serialport";
    //唤醒或打断并且转向的广播
    public static String ACTION_WAKE_UP_AND_MOVE = "action.wake.up.and.move";
    //重置语音板的广播
    public static String ACTION_WAKE_UP_RESET = "action.wake.up.reset";
}
