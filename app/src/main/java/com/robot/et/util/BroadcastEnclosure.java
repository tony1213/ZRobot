package com.robot.et.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.robot.et.common.BroadcastAction;

/**
 * Created by houdeming on 2016/8/1.
 */
public class BroadcastEnclosure {
    private static Intent intent;

    static {
        intent = new Intent();
    }

    //连接netty
    public static void connectNetty(Context context) {
        intent.setAction(BroadcastAction.ACTION_OPEN_NETTY);
        context.sendBroadcast(intent);
    }

    //控制机器人周围小车走的广播
    public static void controlToyCarMove(Context context, int direction, int toyCarNum) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_AROUND_TOYCAR);
        intent.putExtra("direction", direction);
        intent.putExtra("toyCarNum", toyCarNum);
        context.sendBroadcast(intent);
    }

    //停止音乐
    public static void stopMusic(Context context) {
        intent.setAction(BroadcastAction.ACTION_STOP_MUSIC);
        context.sendBroadcast(intent);
    }

    //开始播放音乐
    public static void startPlayMusic(Context context, String musicUrl, int playType) {
        intent.setAction(BroadcastAction.ACTION_PLAY_MUSIC_START);
        intent.putExtra("musicUrl", musicUrl);
        intent.putExtra("playType", playType);
        context.sendBroadcast(intent);
    }

    //机器人表情
    public static void controlRobotEmotion(Context context, int emotionKey) {
        if (emotionKey != 0) {
            intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION);
            intent.putExtra("emotion", emotionKey);
            context.sendBroadcast(intent);
        }
    }

    //胸口的LED灯
    public static void controlChestLED(Context context, String LEDState) {
        if (!TextUtils.isEmpty(LEDState)) {
            intent.setAction(BroadcastAction.ACTION_CONTROL_MOUTH_LED);
            intent.putExtra("LEDState", LEDState);
            context.sendBroadcast(intent);
        }
    }

    //耳朵灯
    public static void controlEarsLED(Context context, int LEDState) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_EARS_LED);
        intent.putExtra("LEDState", LEDState);
        context.sendBroadcast(intent);
    }

    //连接agora
    public static void connectAgora(Context context, int type) {
        intent.setAction(BroadcastAction.ACTION_CONNECT_AGORA);
        intent.putExtra("type", type);
        context.sendBroadcast(intent);
    }

    //关闭agora
    public static void closeAgora(Context context, boolean isNeedVoiceType) {
        intent.setAction(BroadcastAction.ACTION_CLOSE_AGORA);
        intent.putExtra("isNeedVoiceType", isNeedVoiceType);
        context.sendBroadcast(intent);
    }

    //关闭人脸检测
    public static void closeFaceDistinguish(Context context) {
        intent.setAction(BroadcastAction.ACTION_CLOSE_FACE_DISTINGUISH);
        context.sendBroadcast(intent);
    }

    //打开人脸识别
    public static void openFaceRecognise(Context context) {
        intent.setAction(BroadcastAction.ACTION_OPEN_FACE_DISTINGUISH);
        context.sendBroadcast(intent);
    }

    //发送雷达的广播
    public static void sendRadar(Context context) {
        intent.setAction(BroadcastAction.ACTION_ROBOT_RADAR);
        context.sendBroadcast(intent);
    }

    //控制头的广播
    public static void controlHead(Context context, int direction, String angle, int moveTime) {
        intent.putExtra("direction", direction);
        intent.putExtra("angle", angle);
        intent.putExtra("moveTime", moveTime);
        intent.setAction(BroadcastAction.ACTION_ROBOT_TURN_HEAD);
        context.sendBroadcast(intent);
    }

    //控制手臂的广播
    public static void controlArm(Context context, String handCategory, String angle, int moveTime) {
        intent.putExtra("handCategory", handCategory);
        intent.putExtra("angle", angle);
        intent.putExtra("moveTime", moveTime);
        intent.setAction(BroadcastAction.ACTION_CONTROL_WAVING);
        context.sendBroadcast(intent);
    }

    // 开启与硬件相关的广播
    public static void openHardware(Context context, int type) {
        intent.putExtra("type", type);
        intent.setAction(BroadcastAction.ACTION_OPEN_HARDWARE);
        context.sendBroadcast(intent);
    }

    //播放声音提示的广播
    public static void playSoundTips(Context context, int soundId, int playType) {
        intent.putExtra("playType", playType);
        intent.putExtra("soundId", soundId);
        intent.setAction(BroadcastAction.ACTION_PLAY_SOUND_TIPS);
        context.sendBroadcast(intent);
    }

    //人体感应的广播
    public static void bodyDetection(Context context) {
        intent.setAction(BroadcastAction.ACTION_BODY_DETECTION);
        context.sendBroadcast(intent);
    }

    // 唤醒身体去转角度
    public static void wakeUpTurnBody(Context context, int degree) {
        intent.setAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
        intent.putExtra("degree", degree);
        context.sendBroadcast(intent);
    }

    //语音控制机器人走的广播
    public static void controlRobotMoveRos(Context context, int direction, String digit) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE);
        intent.putExtra("direction", direction);
        intent.putExtra("digit", digit);
        context.sendBroadcast(intent);
    }

    // 通过串口控制运动
    public static void controlMoveBySerialPort(Context context, int direction, int distance, int moveTime, int moveRadius) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_MOVE_BY_SERIALPORT);
        intent.putExtra("direction", direction);
        intent.putExtra("distance", distance);
        intent.putExtra("moveTime", moveTime);
        intent.putExtra("moveRadius", moveRadius);
        context.sendBroadcast(intent);
    }

    //ros的广播
    public static void sendRos(Context context, String rosKey, String name) {
        intent.setAction(BroadcastAction.ACTION_ROS_SERVICE);
        intent.putExtra("rosKey", rosKey);
        intent.putExtra("name", name);
        context.sendBroadcast(intent);
    }

    //ros移动的广播
    private void sendRosMove(Context context, String rosKey, String x, String y, String angle) {
        intent.setAction(BroadcastAction.ACTION_ROS_SERVICE);
        intent.putExtra("rosKey", rosKey);
        intent.putExtra("dotX", x);
        intent.putExtra("dotY", y);
        intent.putExtra("angle", angle);
        context.sendBroadcast(intent);
    }
}
