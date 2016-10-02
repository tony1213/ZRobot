package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.software.ros.BodyPositionSubscriber;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/9/30.
 * 跟随
 */
public class FollowBody {
    private static final String TAG = "follow";
    private static Timer timer;
    private static BodyPositionSubscriber bodyPositionSubscriber;
    private static Context context;

    // 处理跟随
    public static void handFollow(Context context, BodyPositionSubscriber bodyPositionSubscriber) {
        FollowBody.bodyPositionSubscriber = bodyPositionSubscriber;
        FollowBody.context = context;
        if (DataConfig.isFollow) {
            timer = TimerManager.createTimer();
            // 500ms读一次
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0);
                }
            }, 0, 500);
        }

    }

    // 最后一次转身的值
    private static int lastTurn;

    // 左转右转 中间是160.在150-170之间默认走直线, <150左转  >170右转
    private static boolean followTurn(int posX) {
        // 防止不停的转
        if (posX < 150 && Math.abs(posX - lastTurn) > 6) {
            Log.i(TAG, "左转");
            lastTurn = posX;
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), 45, 1000, 0);
            return true;
        }

        if (posX > 170 && Math.abs(posX - lastTurn) > 6) {
            Log.i(TAG, "右转");
            lastTurn = posX;
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), 45, 1000, 0);
            return true;
        }
        return false;
    }

    // 停止的距离
    private static final int STOP_VALUE = 70;

    // 前进还是停止
    private static void followMove(int posZ) {
        if (posZ < STOP_VALUE) {
            Log.i(TAG, "后退");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.BACKWARD.getMoveKey(), 500, 1000, 0);
        } else if (posZ >= STOP_VALUE && posZ <= STOP_VALUE + 10) {// 在停止的范围内10个差距内就停止
            Log.i(TAG, "停止");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 500, 1000, 0);
        } else {
            Log.i(TAG, "前进");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 500, 1000, 0);
        }
    }

    private static Handler handler = new Handler() {
        @Override

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (bodyPositionSubscriber != null) {
                double x = bodyPositionSubscriber.getX();
                double y = bodyPositionSubscriber.getY();
                double z = bodyPositionSubscriber.getZ();
                int posX = (int) x;
                int posY = (int) y;
                int posZ = (int) z;
                Log.i(TAG, "获取到人体的位置：X＝" + x + ",Y=" + y + ",Z=" + z);
                // x 代表左右位置【0-320】  y 代表上下位置【0-240】   z 代表距离人的距离
                if ((posX != 0) && (posZ != 0)) {
                    boolean ret = followTurn(posX);
                    if (false == ret) {
                        followMove(posZ);
                    }
                }

            }

        }
    };

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
