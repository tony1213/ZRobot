package com.robot.et.core.software.common.speech.voice.ifly;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by houdeming on 2016/9/3.
 * 科大讯飞语音合成二次封装
 */
public class Speak extends Voice {
    private SpeechSynthesizer mTts;
    private ISpeak iSpeak;

    public Speak(Context context, ISpeak iSpeak) {
        this.iSpeak = iSpeak;
        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, initListener);
    }

    // 销毁当前类对象
    public void destroy() {
        if (mTts != null) {
            stopSpeak();
            mTts.destroy();
        }
    }

    /**
     * 科大讯飞语音合成 说话
     *
     * @param isFirstSetParam 是否是第一次合成
     * @param speakContent    要说的话
     * @param speakMen        发音人
     * @param speed           语速
     * @param pitch           语调
     * @param volume          音量
     * @return
     */
    public boolean speak(boolean isFirstSetParam, String speakContent, String speakMen, String speed, String pitch,
                         String volume) {
        if (mTts == null) {
            return false;
        }

        if (isFirstSetParam) {
            setTextToVoiceParam(speakMen, speed, pitch, volume);
        }
        // 调用sdk提供的语音合成方法
        int code = mTts.startSpeaking(speakContent, mTtsListener);
        // * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
        // * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
        // String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
        // int code = mTts.synthesizeToUri(text, path, mTtsListener);

        // 语音合成失败
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
                // 未安装则跳转到提示安装页面
            } else {
                Log.i(TAG, "语音合成失败,错误码=== " + code);
            }
            return false;
        }
        return true;
    }

    // 停止说话
    public void stopSpeak() {
        if (mTts != null) {
            if (mTts.isSpeaking()) {
                mTts.stopSpeaking();
            }
        }
    }

    // 语音合成监听器
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        // 开始播放
        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "  onSpeakBegin()");
            iSpeak.onSpeakBegin();
        }

        // 暂停播放
        @Override
        public void onSpeakPaused() {

        }

        // 继续播放
        @Override
        public void onSpeakResumed() {

        }

        // 合成进度
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {

        }

        // 播放进度
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {

        }

        // 合成完成
        @Override
        public void onCompleted(SpeechError error) {
            Log.i(TAG, "  onCompleted()");
            iSpeak.onCompleted(error);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    /**
     * 科大讯飞语音合成参数设置
     *
     * @param speakMen 发音人
     * @param speed    语速
     * @param pitch    语调
     * @param volume   音量
     */
    private void setTextToVoiceParam(String speakMen, String speed, String pitch, String volume) {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置合成发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, speakMen);
        // 设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, speed);
        // 设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, pitch);
        // 设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, volume);
        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }
}
