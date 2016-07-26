package com.robot.et.core.software.system.music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.util.SpeechImplHandle;

import java.io.IOException;

public class MusicPlayerService extends Service implements PlayerImpl {

    // 媒体播放器对象
    private MediaPlayer mediaPlayer;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("music", "MusicPlayerService onCreate()");
        PlayerImplHandle.setPlayer(this);

        mediaPlayer = new MediaPlayer();

        //设置音乐播放完成时的监听器
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                Log.i("music", "音乐播放完成");
                SpeechImplHandle.startListen();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void musicSrcNotExit() {
        String tempContent = DataConfig.MUSIC_NOT_EXIT;
        SpeechImplHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, tempContent);
    }

    @Override
    public void stopPlay() {
        if (mediaPlayer.isPlaying()) {
            // 音乐停止播放
            mediaPlayer.stop();
        }
    }

    @Override
    public void startPlay(String musicSrc) {
        if (!TextUtils.isEmpty(musicSrc)) {
            try {
                mediaPlayer.reset();// 把各项参数恢复到初始状态
                mediaPlayer.setDataSource(musicSrc);
                // 进行缓冲
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new PreparedListener());
            } catch (IllegalStateException e) {
                //报异常提示文件不存在
                musicSrcNotExit();
            } catch (IOException e) {
                //报异常提示文件不存在
                musicSrcNotExit();
            }

        } else {
            //提示文件不存在
            musicSrcNotExit();
        }
    }

    //实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
    private final class PreparedListener implements OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i("music", "音乐开始播放");
            mediaPlayer.start(); // 开始播放
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
