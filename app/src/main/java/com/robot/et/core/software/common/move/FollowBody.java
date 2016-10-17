package com.robot.et.core.software.common.move;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.software.common.view.TextManager;
import com.robot.et.core.software.common.view.ViewCommon;
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
    private static int[] arrays = new int[]{160, 160, 160, 160, 160, 160};

    // 处理跟随
    public static void handFollow(Context context, BodyPositionSubscriber bodyPositionSubscriber) {
        FollowBody.bodyPositionSubscriber = bodyPositionSubscriber;
        FollowBody.context = context;
        isGo = false;
        DataConfig.isFollow = true;
        timer = TimerManager.createTimer();
        // 500ms读一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 100);

    }

    // 获取平均值
    private static int getArrayInt(int posX) {
        int length = arrays.length;
        for (int i = 1; i < length; i++) {
            arrays[i - 1] = arrays[i];
        }
        arrays[length - 1] = posX;
        int sum = 0;
        for (int j = 0; j < length; j++) {
            sum += arrays[j];
        }
        return sum / length;
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
                String result = "X＝" + posX + "\n" + "Z=" + posZ;
                ViewCommon.initView();
                TextManager.showText(result);
                // 现在只做前后走，不转
                if ((posX != 0) && (posZ != 0)) {
//                    int newX = getArrayInt(posX);
//                    Log.i(TAG, "获取到人体的位置：newX＝" + newX);
//                    // 转
//                    boolean ret = followTurn(newX);
//                    if (!ret) {
//                        // 直走还是停
//                    }
                    followMove(posZ);
                } else {// 都是0的话，直接就停止
                    Log.i(TAG, "停止");
                    stop();
                }

            }
        }
    };

    // 是否是直走
    private static boolean isGo;
    // 停止的距离
    private static final int STOP_VALUE = 115;

    // 左转右转 中间是160.在150-170之间默认走直线, <150左转  >170右转
    private static boolean followTurn(int posX) {
        // 防止不停的转，要与上一次的位置比较，超过一定范围再转
        if (posX < 145) {
            Log.i(TAG, "左转");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), 10, 1000, 0);
            return true;
        }

        if (posX > 175) {
            Log.i(TAG, "右转");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), 10, 1000, 0);
            return true;
        }
        return false;
    }

    // 走还是停
    private static void followMove(int posZ) {
        isGo = true;
        if (posZ < STOP_VALUE) {
            Log.i(TAG, "后退");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.BACKWARD.getMoveKey(), 150, 1000, 0);
        } else if (posZ >= STOP_VALUE && posZ <= STOP_VALUE + 40) {// 在停止的范围内30个差距内就停止
            Log.i(TAG, "停止");
            stop();
        } else {
            Log.i(TAG, "前进");
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.FORWARD.getMoveKey(), 300, 1000, 0);
        }
    }

    private static void stop() {
        // 当直走时要转的时候，只停一次
        if (isGo) {
            isGo = false;
            BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);
        }
    }

    public static void stopTimer() {
        if (timer != null) {
            TimerManager.cancelTimer(timer);
            timer = null;
        }
    }
}
