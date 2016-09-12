package com.robot.et.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.ScriptConfig;

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

    //通过App控制机器人走
    public static void controlMoveByApp(Context context, int direction) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_NETTY);
        intent.putExtra("direction", direction);
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
    public static void startPlayMusic(Context context, String musicUrl) {
        intent.setAction(BroadcastAction.ACTION_PLAY_MUSIC_START);
        intent.putExtra("musicUrl", musicUrl);
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

    //摆手
    public static void controlWaving(Context context, String handDirection, String handCategory, String num) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_WAVING);
        intent.putExtra("handDirection", handDirection);
        if (TextUtils.isEmpty(handCategory)) {
            handCategory = ScriptConfig.HAND_TWO;
        }
        intent.putExtra("handCategory", handCategory);
        intent.putExtra("num", num);
        context.sendBroadcast(intent);
    }

    //嘴巴的LED灯
    public static void controlMouthLED(Context context, String LEDState) {
        if (!TextUtils.isEmpty(LEDState)) {
            intent.setAction(BroadcastAction.ACTION_CONTROL_MOUTH_LED);
            intent.putExtra("LEDState", LEDState);
            context.sendBroadcast(intent);
        }
    }

    //跟随
    public static void controlFollow(Context context, String robotNum, int toyCarNum) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_FOLLOW);
        intent.putExtra("robotNum", robotNum);
        intent.putExtra("toyCarNum", toyCarNum);
        context.sendBroadcast(intent);
    }

    //控制小车转圈
    public static void controlTurnAround(Context context, int turnDirection, String turnNum) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_TURN);
        intent.putExtra("turnDirection", turnDirection);
        intent.putExtra("turnNum", turnNum);
        context.sendBroadcast(intent);
    }

    //连接agora
    public static void connectAgora(Context context, int type){
        intent.setAction(BroadcastAction.ACTION_CONNECT_AGORA);
        intent.putExtra("type", type);
        context.sendBroadcast(intent);
    }

    //打开人脸识别
    public static void openFaceRecognise(Context context, boolean isVoiceFaceRecognise){
        intent.setAction(BroadcastAction.ACTION_OPEN_FACE_DISTINGUISH);
        intent.putExtra("isVoiceFaceRecognise", isVoiceFaceRecognise);
        context.sendBroadcast(intent);
    }

    //语音控制走小车的广播
    public static void controlRobotMove(Context context, int direction) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE);
        intent.putExtra("direction", direction);
        context.sendBroadcast(intent);
    }

    //发送雷达的广播
    public static void sendRadar(Context context) {
        intent.setAction(BroadcastAction.ACTION_ROBOT_RADAR);
        context.sendBroadcast(intent);
    }

    //语音控制走小车的广播
    public static void controlRobotMoveRos(Context context, int direction, String digit) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE_ROS);
        intent.putExtra("direction", direction);
        intent.putExtra("digit", digit);
        context.sendBroadcast(intent);
    }

    //控制头的广播
    public static void controlHead(Context context, int direction, String angle) {
        intent.putExtra("direction", direction);
        intent.putExtra("angle", angle);
        intent.setAction(BroadcastAction.ACTION_ROBOT_TURN_HEAD);
        context.sendBroadcast(intent);
    }

}
