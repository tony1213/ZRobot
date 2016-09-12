package com.robot.et.core.software.system.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.robot.et.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by houdeming on 2016/9/6.
 * 播放短暂声音的声音池
 */
public class Sound {
    private SoundPool soundPool; //声音池
    private Map<Integer, Integer> mapSRC;
    public static final int SOUND_CAMERA = 1;// 拍照时的声音
    public static final int SOUND_CALL_PHONE = 2;// 电话来了的声音
    public static final int SOUND_SPEAK_OVER = 3;// 话说完了的声音
    private static int streamId;// 播放返回的资源id

    public Sound(Context context) {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mapSRC = new HashMap<Integer, Integer>();
        mapSRC.put(SOUND_CAMERA, soundPool.load(context, R.raw.camera, 0));
        mapSRC.put(SOUND_CALL_PHONE, soundPool.load(context, R.raw.phone, 0));
        mapSRC.put(SOUND_SPEAK_OVER, soundPool.load(context, R.raw.speakover, 0));
    }

    /**
     * 播放声音
     *
     * @param soundId 播放声音的id
     */
    public void play(int soundId) {
        if (soundPool != null) {
            streamId = soundPool.play(mapSRC.get(soundId),//播放的声音资源
                    1.0f,//左声道，范围为0--1.0
                    1.0f,//右声道，范围为0--1.0
                    0, //优先级，0为最低优先级
                    0,//循环次数,0为不循环,-1为循环
                    1);//回放速度，0.5-2.0之间，1为正常速度
        }
    }

    // 停止掉声音提示
    // 程序中不用考虑播放流的生命周期，无效的soundID/streamID不会导致程序错误
    public void stopSound() {
        if (soundPool != null) {
            soundPool.stop(streamId);
        }
    }

    // 销毁声音池对象
    public void destroy() {
        if (soundPool != null) {
            soundPool.release();
        }
    }
}
