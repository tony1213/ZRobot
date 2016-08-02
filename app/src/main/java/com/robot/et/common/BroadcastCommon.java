package com.robot.et.common;

import android.content.Context;
import android.content.Intent;

/**
 * Created by houdeming on 2016/8/1.
 */
public class BroadcastCommon {

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
    public static void controlMoveByApp(Context context, String direction) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_NETTY);
        intent.putExtra("direction", direction);
        context.sendBroadcast(intent);
    }

    //控制机器人周围小车走的广播
    public static void controlToyCarMove(Context context, String direction, int toyCarNum) {
        intent.setAction(BroadcastAction.ACTION_CONTROL_AROUND_TOYCAR);
        intent.putExtra("direction", direction);
        intent.putExtra("toyCarNum", toyCarNum);
        context.sendBroadcast(intent);
    }

    //停止音乐
    public static void stopMusic(Context context){
        intent.setAction(BroadcastAction.ACTION_STOP_MUSIC);
        context.sendBroadcast(intent);
    }

}
