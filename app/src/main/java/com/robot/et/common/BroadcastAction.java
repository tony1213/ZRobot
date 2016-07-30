package com.robot.et.common;

/**
 * Created by Tony on 2016/7/25.
 */
public class BroadcastAction {
    //串口数据接收
    public static String ACTION_MOVE_TO_SERIALPORT ="action.control.robot.serialport";
    //唤醒,打断或者转向的广播
    public static String ACTION_WAKE_UP_OR_INTERRUPT = "action.wake.up.or.interrupt";
    //重置语音板的广播
    public static String ACTION_WAKE_UP_RESET = "action.wake.up.reset";
    //开始播放音乐
    public static String ACTION_PLAY_MUSIC_START = "action.play.music.start";
    //音乐播放完成
    public static String ACTION_PLAY_MUSIC_END = "action.play.music.end";
    //停止音乐播放
    public static String ACTION_STOP_MUSIC = "action.stop.music";
    //说话
    public static String ACTION_SPEAK = "action.speak";
    //脸部识别
    public static String ACTION_FACE_DISTINGUISH = "action.face.distinguish";
    //控制机器人移动(Netty指令)
    public static String ACTION_CONTROL_ROBOT_MOVE_WITH_NETTY = "action.control.robot.move.with.netty";
    //控制机器人移动（语音控制）
    public static String ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE = "action.control.robot.move.with.voice";

}
