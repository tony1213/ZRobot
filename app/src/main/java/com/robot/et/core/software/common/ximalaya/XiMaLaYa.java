package com.robot.et.core.software.common.ximalaya;

import android.content.Context;
import android.util.Log;

import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.PlayableModel;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;
import com.ximalaya.ting.android.opensdk.player.service.IXmPlayerStatusListener;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerConfig;
import com.ximalaya.ting.android.opensdk.player.service.XmPlayerException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by houdeming on 2016/9/18.
 */
public class XiMaLaYa {
    private static final String TAG = "ximalaya";
    private XmPlayerManager mPlayerManager;
    private CommonRequest mXimalaya;
    private IXiMaLaYa iXiMaLaYa;

    public XiMaLaYa(Context context, String appSecret, IXiMaLaYa iXiMaLaYa) {
        this.iXiMaLaYa = iXiMaLaYa;
        // 不希望SDK处理AudioFocus
        // 设置是否处理一般的audioFocus
        XmPlayerConfig.getInstance(context).setSDKHandleAudioFocus(false);
        // 设置是否处理电话拨出或进来时的AudioFocus
        XmPlayerConfig.getInstance(context).setSDKHandlePhoneComeAudioFocus(false);
        // 设置耳机线的插拔是的AudioFocus
        XmPlayerConfig.getInstance(context).setSDKHandleHeadsetPlugAudioFocus(false);

        // 初始化SDK
        mXimalaya = CommonRequest.getInstanse();
        mXimalaya.init(context, appSecret);

        // 初始化播放器
        mPlayerManager = XmPlayerManager.getInstance(context);
        // init()只需要调用一次，多次调用仅第一次有效
        mPlayerManager.init();
        // 监听播放状态
        mPlayerManager.addPlayerStatusListener(mPlayerStatusListener);
        mPlayerManager.setOnConnectedListerner(new XmPlayerManager.IConnectListener() {
            @Override
            public void onConnected() {
                mXimalaya.setDefaultPagesize(50);
                Log.i(TAG, "播放器初始化成功");
            }
        });
    }

    // 如果不接入播放器逻辑，则必须接入播放数据回传接口，否则喜马拉雅有权 直接封停接口
    private IXmPlayerStatusListener mPlayerStatusListener = new IXmPlayerStatusListener() {

        /** 切歌
         *
         * @param laModel 上一首model,可能为空 
         * @param curModel 下一首model 
         */
        @Override
        public void onSoundSwitch(PlayableModel laModel, PlayableModel curModel) {
            Log.i(TAG, "onSoundSwitch");
        }

        @Override
        public void onSoundPrepared() {
            // 播放器准备完毕
            Log.i(TAG, "onSoundPrepared");
        }

        @Override
        public void onSoundPlayComplete() {
            // 播放完成
            Log.i(TAG, "onSoundPlayComplete");
            if (iXiMaLaYa != null) {
                iXiMaLaYa.onSoundPlayComplete();
            }
        }

        @Override
        public void onPlayStop() {
            // 停止播放
            Log.i(TAG, "onPlayStop");
        }

        @Override
        public void onPlayStart() {
            // 开始播放
            Log.i(TAG, "onPlayStart");
            if (iXiMaLaYa != null) {
                iXiMaLaYa.onPlayStart();
            }
        }

        @Override
        public void onPlayProgress(int currPos, int duration) {
            // 播放进度回调
        }

        @Override
        public void onPlayPause() {
            // 暂停播放
            Log.i(TAG, "onPlayPause");
        }

        @Override
        public boolean onError(XmPlayerException exception) {
            // 播放器错误
            Log.i(TAG, "onError");
            if (iXiMaLaYa != null) {
                iXiMaLaYa.onPlayError();
            }
            return false;
        }

        @Override
        public void onBufferingStop() {
            // 结束缓冲
            Log.i(TAG, "onBufferingStop");
        }

        @Override
        public void onBufferingStart() {
            // 开始缓冲
            Log.i(TAG, "onBufferingStart");
        }

        @Override
        public void onBufferProgress(int percent) {
            // 缓冲进度回调
        }

    };

    // 停止播放
    public void stopPlay() {
        if (mPlayerManager != null) {
            if (mPlayerManager.isPlaying()) {
                mPlayerManager.stop();
            }
        }
    }

    // 释放播放器
    public void destroyPlayer() {
        if (mPlayerManager != null) {
            mPlayerManager.removePlayerStatusListener(mPlayerStatusListener);
            mPlayerManager.release();
        }
    }

    // 播放音乐
    public void playMusic(String musicName, final IPlayer iPlayer) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, musicName);
        CommonRequest.getSearchedTracks(map, new IDataCallBack<SearchTrackList>() {
            @Override
            public void onSuccess(SearchTrackList searchTrackList) {
                Log.e(TAG, "searchVoice onSuccess");
                List<Track> tracks = searchTrackList.getTracks();
                Log.e(TAG, "tracks.size()===" + tracks.size());
                if (tracks != null && tracks.size() > 0) {
                    // 播放的音乐src
                    Log.e(TAG, "track.getPlayUrl32()===" + tracks.get(0).getPlayUrl32());
                    // 播放的名字
                    Log.e(TAG, "track.getTrackTitle()===" + tracks.get(0).getTrackTitle());
                    // 默认播放第一个
                    mPlayerManager.playList(tracks, 0);
                    if (iPlayer != null) {
                        iPlayer.playSuccess();
                    }
                } else {
                    if (iPlayer != null) {
                        iPlayer.playFail();
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "searchVoice onError");
                if (iPlayer != null) {
                    iPlayer.playFail();
                }
            }
        });

    }

    // 播放电台
    public void playRadio(String radioName, final IPlayer iPlayer) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.SEARCH_KEY, radioName);
        CommonRequest.getSearchedRadios(map, new IDataCallBack<RadioList>() {
            @Override
            public void onSuccess(RadioList radioList) {
                Log.e(TAG, "searchRadio onSuccess");
                List<Radio> radios = radioList.getRadios();
                Log.e(TAG, "radios.size()===" + radios.size());
                if (radios != null && radios.size() > 0) {
                    // 默认播放第一个
                    Radio radio = radios.get(0);
                    // 电台的名字
                    Log.i(TAG, "radio.getRadioName()==" + radio.getRadioName());
                    mPlayerManager.playRadio(radio);
                    if (iPlayer != null) {
                        iPlayer.playSuccess();
                    }
                } else {
                    if (iPlayer != null) {
                        iPlayer.playFail();
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "searchRadio onError");
                if (iPlayer != null) {
                    iPlayer.playFail();
                }
            }
        });
    }
}
