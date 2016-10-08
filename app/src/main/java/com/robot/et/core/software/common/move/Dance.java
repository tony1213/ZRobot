package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.hardware.serialport.SerialPortHandler;
import com.robot.et.entity.SerialPortReceiverInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.MusicManager;
import com.robot.et.util.TimerManager;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/10/1.
 * 跳舞
 */
public class Dance {
    private static final String TAG = "dance";
    private static Timer timer;
    private static Context context;

    public static void dance(Context context, String musicName) {
        Dance.context = context;
        // 唱歌
        String musicSrc = MusicManager.getMusicSrcByName(RequestConfig.JPUSH_MUSIC, musicName, "");
        BroadcastEnclosure.startPlayMusic(context, musicSrc, DataConfig.PLAY_MUSIC);
        // 获取雷达数据
        DataConfig.isOpenRadar = true;
        BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_RADAR);
        // 手臂摆动
        Waving.waving(context);

        arrayIndex = 0;
        DataConfig.isDance = true;
        timer = TimerManager.createTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1800);
    }

    private static int arrayIndex;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            arrayIndex++;
            if (arrayIndex == 13) {
                arrayIndex = 1;
            }
            int[] arrays = getRandomArray();
            move(arrays[arrayIndex - 1]);
        }
    };

    private static int[] getRandomArray() {
        Random random = new Random();
        int[] array = new int[12];
        for (int i = 0; i < 12; i++) {
            array[i] = random.nextInt(10);
        }
        return array;
    }

    private static void move(int key) {
        switch (key) {
            case 0:
                Log.i(TAG, "前进");
                goForward();
                break;
            case 1:
                turnLeft(0, false, 90);
                goForward();
                break;
            case 2:
                turnLeft(0, false, 90);
                goForward();
                break;
            case 3:
                turnLeft(0, false, 90);
                goForward();
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                turnLeft(600, false, 90);
                break;
            case 8:
            case 9:
            case 10:
            case 11:
                turnRight(-600, false, 90);
                break;
            default:
                break;
        }
    }

    // 中间停止的距离
    private static final int STOP_MIDDLE = 80;
    // 左右停止的距离
    private static final int STOP_LEFT_RIGHT = 100;
    // 转默认角度
    private static final int DEFAULT_ANGLE = 15;

    // 前进
    private static void goForward() {
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
                        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 550, 1000, 0);
                    } else {// 左转
                        turnLeft(0, false, DEFAULT_ANGLE);
                    }
                } else {
                    if (right >= STOP_LEFT_RIGHT) {// 左转
                        turnLeft(0, false, DEFAULT_ANGLE);
                    } else {// 左转
                        turnLeft(0, false, DEFAULT_ANGLE);
                    }
                }
            } else {
                if (middle >= STOP_MIDDLE) {
                    if (right >= STOP_LEFT_RIGHT) {// 右转
                        turnRight(0, false, DEFAULT_ANGLE);
                    } else {// 右转
                        turnRight(0, false, DEFAULT_ANGLE);
                    }
                } else {
                    if (right >= STOP_LEFT_RIGHT) {// 右转
                        turnRight(0, false, DEFAULT_ANGLE);
                    } else {// 右转
                        turnRight(0, false, DEFAULT_ANGLE);
                    }
                }
            }
        } else {// 代表没上传数据
            // 前进
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 550, 1000, 0);
        }
    }

    // 左转
    private static void turnLeft(int radio, boolean turnHead, int angle) {
        Log.i(TAG, "左转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), angle, 1000, radio);
    }

    // 右转
    private static void turnRight(int radio, boolean turnHead, int angle) {
        Log.i(TAG, "右转");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), angle, 1000, radio);
    }

    // 停止
    private static void stop() {
        Log.i(TAG, "停止");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
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
