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
    private static int arrayIndex;
    private static int[] arrays;

    public static void dance(Context context, String musicName) {
        Dance.context = context;
        isForward = false;
        // 唱歌
        String musicSrc = MusicManager.getMusicSrcByName(RequestConfig.JPUSH_MUSIC, musicName, "");
        BroadcastEnclosure.startPlayMusic(context, musicSrc, DataConfig.PLAY_MUSIC);
        // 获取雷达数据
        DataConfig.isOpenRadar = true;
        BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_RADAR);
        // 手臂摆动
        Waving.waving(context);

        arrayIndex = 0;
        arrays = getRandomArray();

        DataConfig.isDance = true;
        timer = TimerManager.createTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1800);
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            arrayIndex++;
            if (arrayIndex == 17) {
                arrayIndex = 1;
                // 重新获取数据
                arrays = getRandomArray();
            }

            move(arrays[arrayIndex - 1]);
        }
    };

    // 获取指定的特定数组
    private static int[] getRandomArray() {
        int length = 16;
        int m = 12;
        int n = 12;
        int i, j = 0;
        Random random = new Random();
        int[] array = new int[length];
        while (j < m) {
            array[j] = random.nextInt(n);
            for (i = 0; i < j; i++) {
                if (array[i] == array[j])
                    break;
            }
            if (i < j)
                continue;
            j++;
        }
        for (i = 0; i < 15; i++) {
            if (array[i] <= 3) {
                for (j = 14; j > i; j--) {
                    array[j + 1] = array[j];
                }
                array[i + 1] = array[i] + 12;
            }
        }
        return array;
    }

    // [0,1,2,3] 直走
    // [12,13,14,15] 转向
    private static void move(int key) {
        switch (key) {
            case 0:
            case 1:
            case 2:
            case 3:
                // 直走
                goForward(MOVE_GO);
                break;
            case 12:
            case 13:
            case 14:
            case 15:
                // 左转
                turnLeft(0, false, 90);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                // 顺时针转
                goForward(MOVE_TURN_LEFT);
                break;
            case 8:
            case 9:
            case 10:
            case 11:
                // 逆时针转
                goForward(MOVE_TURN_RIGHT);
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

    private static final int MOVE_GO = 1;
    private static final int MOVE_TURN_LEFT = 2;
    private static final int MOVE_TURN_RIGHT = 3;

    // 是否前进
    private static boolean isForward;

    // 前进
    private static void goForward(int type) {
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
                        doMove(type);
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
            doMove(type);
        }
    }

    private static void doMove(int type) {
        switch (type) {
            case MOVE_GO:
                // 直走
                isForward = true;
                Log.i(TAG, "直走");
                BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 550, 1000, 0);
                break;
            case MOVE_TURN_LEFT:
                // 顺时针转
                turnLeft(600, false, 90);
                break;
            case MOVE_TURN_RIGHT:
                // 逆时针转
                turnRight(-600, false, 90);
                break;
            default:
                break;
        }
    }

    // 左转
    private static void turnLeft(int radio, boolean turnHead, int angle) {
        if (radio == 0) {
            // 转的时候退一下
            Log.i(TAG, "左转");
            moveBack();
        } else {
            Log.i(TAG, "顺时针转");
        }

        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), angle, 1000, radio);
    }

    // 右转
    private static void turnRight(int radio, boolean turnHead, int angle) {
        if (radio == 0) {
            // 转的时候退一下
            Log.i(TAG, "右转");
            moveBack();
        } else {
            Log.i(TAG, "逆时针转");
        }

        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), angle, 1000, radio);
    }

    // 停止
    private static void stop() {
        Log.i(TAG, "停止");
        BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
    }

    // 后退
    private static void moveBack() {
        if (isForward) {
            isForward = false;
            stop();
            Log.i(TAG, "后退");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.BACKWARD.getMoveKey(), 150, 1000, 0);
        }
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
