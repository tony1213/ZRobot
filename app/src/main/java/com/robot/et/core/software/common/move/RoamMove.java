package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.hardware.serialport.SerialPortHandler;
import com.robot.et.entity.SerialPortReceiverInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/10/1.
 * 漫游
 */
public class RoamMove {
    private static final String TAG = "roam";
    private static Timer timer;
    private static Context context;

    public static void roam(Context context) {
        RoamMove.context = context;
        if (DataConfig.isRoam) {
            timer = TimerManager.createTimer();
            // 1000ms执行一次
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0);
                }
            }, 0, 500);
        }
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            roamMove();
        }
    };

    // 转默认角度
    private static final int DEFAULT_ANGLE = 15;
    // 中间停止的距离
    private static final int STOP_MIDDLE = 80;
    // 左右停止的距离
    private static final int STOP_LEFT_RIGHT = 100;

    private static void roamMove() {
        SerialPortReceiverInfo info = SerialPortHandler.getRadarInfo();
        if (info != null) {// 代表雷达数据已上传
            // 一定要清空数据
            SerialPortHandler.setRadarInfo(null);
            // 当有一个值 < 停止时就会有雷达数据上传
            int left = info.getDataL();
            int middle = info.getDataM();
            int right = info.getDataR();
            Log.i(TAG, "left=" + left + ",middle=" + middle + ",right=" + right);
            if (left >= STOP_LEFT_RIGHT) {
                if (middle >= STOP_MIDDLE) {
                    if (right >= STOP_LEFT_RIGHT) {
                        // 前进
                        moveForward();
                    } else {// 左转
                        handTurnLeft();
                    }
                } else {
                    if (right >= STOP_LEFT_RIGHT) {// 左转
                        handTurnLeft();
                    } else {// 左转
                        handTurnLeft();
                    }
                }
            } else {
                if (middle >= STOP_MIDDLE) {
                    if (right >= STOP_LEFT_RIGHT) {// 右转
                        handTurnRight();
                    } else {// 右转
                        handTurnRight();
                    }
                } else {
                    if (right >= STOP_LEFT_RIGHT) {// 右转
                        handTurnRight();
                    } else {// 右转
                        handTurnRight();
                    }
                }
            }
        } else {// 代表没上传数据
            // 前进
            moveForward();
        }
    }

    private static void stop() {
        Log.i(TAG, "停止");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
    }

    // 每次左转之前先停止
    private static void handTurnLeft() {
        stop();
        Log.i(TAG, "左转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), DEFAULT_ANGLE, 1000, 0);
    }

    // 每次右转之前先停止
    private static void handTurnRight() {
        stop();
        Log.i(TAG, "右转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), DEFAULT_ANGLE, 1000, 0);
    }

    private static void moveForward() {
        Log.i(TAG, "前进");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 500, 1000, 0);
    }

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
