package com.robot.et.core.software.system.media;

import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by houdeming on 2016/9/7.
 * 音乐播放工具
 */
public class Music {
    private final String TAG = "Music";
    private MediaPlayer mediaPlayer;
    private IMusic iMusic;// 音乐播放接口
//    private Visualizer mVisualizer;// 频谱器

    public Music(final IMusic iMusic) {
        this.iMusic = iMusic;
        // 媒体播放器对象
        mediaPlayer = new MediaPlayer();
        //实例化Visualizer，参数SessionId可以通过MediaPlayer的对象获得
//        mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
//        //采样 - 参数内必须是2的位数 - 如64,128,256,512,1024
//        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//        SpectrumManager.setVisualizer(mVisualizer);
        //设置音乐播放完成时的监听器
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                Log.i(TAG, "音乐播放完成");
                iMusic.musicPlayComplected();
            }
        });

    }

    // 播放音乐
    public boolean play(String musicSrc) {
        boolean flag = false;
        if (!TextUtils.isEmpty(musicSrc)) {
            try {
                // 如果mediaPlayer为空直接返回
                if (mediaPlayer == null) {
                    return flag;
                }
                flag = true;
                mediaPlayer.reset();// 把各项参数恢复到初始状态
                mediaPlayer.setDataSource(musicSrc);
                // 进行缓冲
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new PreparedListener());
            } catch (IllegalStateException e) {
                Log.i(TAG, "play IllegalStateException==" + e.getMessage());
                flag = false;
            } catch (IOException e) {
                Log.i(TAG, "play IOException==" + e.getMessage());
                flag = false;
            }
        }
        return flag;
    }

    // 停止播放
    public void stopPlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    // 销毁当前对象
    public void destroy() {
//        mVisualizer.release();
        if (mediaPlayer != null) {
            // 停止音乐播放
            stopPlay();
            // 释放对象
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, "音乐开始播放");
            mediaPlayer.start(); // 开始播放
            iMusic.startPlayMusic();
        }
    }
}
