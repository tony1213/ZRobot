package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.EmotionEnum;
import com.robot.et.core.software.common.move.RoamMove;
import com.robot.et.core.software.common.move.Waving;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.MusicManager;

/**
 * Created by houdeming on 2016/10/4.
 * 漫游的业务处理（漫游的时候要头动、手动、唱歌，表情动）
 */
public class Roam {
    private static final String TAG = "roam";
    private static Context context;

    // 漫游
    public static void roam(Context context) {
        Roam.context = context;
        DataConfig.isRoam = true;
        // 唱歌
        String content = MusicManager.getRandomMusic();
        if (!TextUtils.isEmpty(content)) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_MUSIC_START, "好的");
        }
        // 获取雷达数据
        BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_RADAR);
        // 漫游
        RoamMove.roam(context);
        // 表情
        controlEmotion();
        // 手臂摆动
        Waving.waving(context);

    }

    // 控制表情
    private static void controlEmotion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (DataConfig.isRoam) {
                    handler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(6 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "表情");
            BroadcastEnclosure.controlRobotEmotion(context, EmotionEnum.EMOTION_SMILE.getEmotionKey());
        }
    };
}
