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

import java.util.Random;
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
        forwardCount = 0;
        headAngle = 0;
        isRandomTurn = false;
        isForward = false;
        if (DataConfig.isRoam) {
            timer = TimerManager.createTimer();
            // 500ms执行一次
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
    // 前进的次数
    private static int forwardCount;
    // 随机转的
    private static boolean isRandomTurn;
    // 头转的角度
    private static int headAngle;
    // 是否前进
    private static boolean isForward;

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
            isRandomTurn = false;
            if (left >= STOP_LEFT_RIGHT) {
                if (middle >= STOP_MIDDLE) {
                    if (right >= STOP_LEFT_RIGHT) {
                        // 前进
                        handForward();
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
            handForward();
        }
    }

    // 停止
    private static void stop() {
        Log.i(TAG, "停止");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
    }

    // 处理左转
    private static void handTurnLeft() {
        turn();
        // 身体左转的时候头左转
        if (headAngle > -60) {
            headAngle -= 5;
            Log.i(TAG, "头左转");
            head(headAngle, 500);
        }
        Log.i(TAG, "左转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), DEFAULT_ANGLE, 1000, 0);
    }

    // 处理右转
    private static void handTurnRight() {
        turn();
        // 身体右转的时候头右转
        if (headAngle < 60) {
            headAngle += 5;
            Log.i(TAG, "头右转");
            head(headAngle, 500);
        }
        Log.i(TAG, "右转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), DEFAULT_ANGLE, 1000, 0);
    }

    private static void turn() {
        forwardCount = 0;
        // 当前进去后退时只停一次
        if (isForward) {
            isForward = false;
            stop();
        }
        // 转之前后退一点
        if (!isRandomTurn) {
            moveBack();
        }
    }

    // 前进
    private static void handForward() {
        forwardCount++;
        // 每前进4次就随机转
        if (forwardCount > 3) {
            isRandomTurn = true;
            Random random = new Random();
            int ranInt = random.nextInt(2);
            if (ranInt == 0) {// 0左转，1右转
                handTurnLeft();
            } else {
                handTurnRight();
            }
        } else {
            isForward = true;
            // 前进的时候头归位
            headAngle = 0;
            Log.i(TAG, "头归位");
            head(headAngle, 1000);
            Log.i(TAG, "前进");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 300, 1000, 0);
        }
    }

    // 后退
    private static void moveBack() {
        Log.i(TAG, "后退");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.BACKWARD.getMoveKey(), 150, 1000, 0);
    }

    // 头
    private static void head(int value, int moveTime) {
        BroadcastEnclosure.controlHead(context, DataConfig.TURN_HEAD_ABOUT, String.valueOf(value), moveTime);
    }

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
