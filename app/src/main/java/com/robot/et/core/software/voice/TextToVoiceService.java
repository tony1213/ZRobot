package com.robot.et.core.software.voice;

import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.robot.et.R;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EarsLightConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.speech.voice.ifly.ISpeak;
import com.robot.et.core.software.common.speech.voice.ifly.Speak;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.system.media.Sound;
import com.robot.et.util.AlarmRemindManager;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.LocationManager;
import com.robot.et.util.MusicManager;

// 语音合成
public class TextToVoiceService extends SpeechService implements ISpeak {
    private int currentType;
    private boolean isFirstSetParam = true;
    private Speak speak;
    private int speakCount;// 提醒说话的次数

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ifly", "TextToVoiceService  onCreate()");
        SpeechImpl.setService(this);
        // 初始化
        speak = new Speak(this, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        speak.destroy();
    }

    /**
     * 继承父类方法
     * 开始说（外部调用）
     *
     * @param speakType    说话的类型
     * @param speakContent 说话的内容
     */
    @Override
    public void startSpeak(int speakType, String speakContent) {
        super.startSpeak(speakType, speakContent);
        Log.i("ifly", "speakType===" + speakType);
        Log.i("ifly", "speakContent===" + speakContent);
        currentType = speakType;
        if (!TextUtils.isEmpty(speakContent)) {
            // 如果是提醒的话，停止掉别的，处理提醒内容
            if (currentType == DataConfig.SPEAK_TYPE_REMIND_TIPS) {//提醒
                cancelSpeak();
                SpeechImpl.getInstance().cancelListen();
                //停止唱歌
                BroadcastEnclosure.stopMusic(TextToVoiceService.this);
            }

            boolean isSuccess = speak.speak(isFirstSetParam, speakContent, DataConfig.DEFAULT_SPEAK_MEN, "60", "50", "100");
            if (isSuccess) {
                isFirstSetParam = false;
            } else {
                SpeechImpl.getInstance().startListen();
            }
        } else {
            SpeechImpl.getInstance().startListen();
        }
    }

    /**
     * 继承父类方法
     * 取消说（外部调用）
     */
    @Override
    public void cancelSpeak() {
        super.cancelSpeak();
        speak.stopSpeak();
    }

    /**
     * 实现ISpeak接口方法
     * 开始说话（调用sdk内部方法）
     */
    @Override
    public void onSpeakBegin() {
        Log.i("ifly", "onSpeakBegin()");
        // 回答的时候耳朵灯光闪烁
        BroadcastEnclosure.controlEarsLED(TextToVoiceService.this, EarsLightConfig.EARS_BLINK);
    }

    /**
     * 实现ISpeak接口方法
     * 说话完成（调用sdk内部方法）
     *
     * @param error 返回异常
     */
    @Override
    public void onCompleted(SpeechError error) {
        Log.i("ifly", "onCompleted()");
        // 回答完毕灯光灭
        BroadcastEnclosure.controlEarsLED(TextToVoiceService.this, EarsLightConfig.EARS_CLOSE);

        if (currentType != DataConfig.SPEAK_TYPE_NO_SOUND_TIPS) {
            // 说话结束播放声音提示
            BroadcastEnclosure.playSoundTips(TextToVoiceService.this, Sound.SOUND_SPEAK_OVER, DataConfig.PLAY);
        }

        if (error == null) {
            responseSpeakCompleted();
        } else {
            Log.i("ifly", "onCompleted  error=" + error.getPlainDescription(true));
            SpeechImpl.getInstance().startListen();
        }
    }

    // 合成完成后要做的处理
    private void responseSpeakCompleted() {
        switch (currentType) {
            case DataConfig.SPEAK_TYPE_CHAT://对话
                // 说提醒的时候，中间唤醒打断，造成下次提醒影响
                speakCount = 0;
                SpeechImpl.getInstance().startListen();
                break;
            case DataConfig.SPEAK_TYPE_MUSIC_START://音乐开始播放前的提示
                showNormalEmotion(true);
                if (DataConfig.isJpushPlayMusic) {// app推送
                    BroadcastEnclosure.startPlayMusic(this, MusicManager.getMusicSrc(), MusicManager.getMusicType());
                } else {// 第三方
                    BroadcastEnclosure.startPlayMusic(this, MusicManager.getMusicName(), MusicManager.getMusicType());
                }
                break;
            case DataConfig.SPEAK_TYPE_DO_NOTHINF://什么都不处理
                showNormalEmotion(true);
                //do nothing
                break;
            case DataConfig.SPEAK_TYPE_SHOW_QRCODE://显示二维码的图片
                //do nothing
                break;
            case DataConfig.SPEAK_TYPE_NO_SOUND_TIPS://没有提示音
                //do nothing
                break;
            case DataConfig.SPEAK_TYPE_SLEEP://睡觉
                //do nothing
                break;
            case DataConfig.SPEAK_TYPE_REMIND_TIPS://闹铃提醒
                showNormalEmotion(true);
                if (DataConfig.isAppPushRemind) {
                    SpeechImpl.getInstance().startListen();
                    return;
                }

                // 设置的闹铃说3遍
                if (speakCount < 2) {
                    // 单次闹铃
                    String alarmContent = AlarmRemindManager.getSpeakRemindContent();
                    if (!TextUtils.isEmpty(alarmContent)) {
                        speakCount++;
                        startSpeak(DataConfig.SPEAK_TYPE_REMIND_TIPS, alarmContent);
                    } else {
                        // 同一时间内的多个提醒
                        String alarmMoreContent = AlarmRemindManager.getMoreAlarmContent();
                        startSpeak(DataConfig.SPEAK_TYPE_REMIND_TIPS, alarmMoreContent);
                    }
                    return;
                } else {
                    speakCount = 0;
                    //  单次闹铃说完之后要设置空
                    AlarmRemindManager.setSpeakRemindContent("");
                    startSpeak(DataConfig.SPEAK_TYPE_REMIND_TIPS, "重要的事情说三遍");
                }

                break;
            case DataConfig.SPEAK_TYPE_WEATHER:// 天气
                String weatherContent = new StringBuffer(1024).append("今天").append(LocationManager.getInfo().getCity()).
                        append(LocationManager.getInfo().getArea()).append("的天气").toString();
                SpeechImpl.getInstance().understanderText(weatherContent);

                break;
            case DataConfig.SPEAK_TYPE_SCRIPT://剧本对话
                if (DataConfig.isScriptQA) {
                    SpeechImpl.getInstance().startListen();
                    return;
                }
                new ScriptHandler().scriptSpeak(this);

                break;
            case RequestConfig.JPUSH_CALL_CLOSE://视频或语音时电话挂断
                showNormalEmotion(true);
                // do nothing
                break;
            case RequestConfig.JPUSH_CALL_VIDEO://视频通话
                showNormalEmotion(true);
                // 不是从安保模式打过去的电话
                DataConfig.isSecurityCall = false;
                BroadcastEnclosure.connectAgora(this, RequestConfig.JPUSH_CALL_VIDEO);
                break;
            case RequestConfig.JPUSH_CALL_VOICE://语音通话
                showNormalEmotion(true);
                // 不是从安保模式打过去的电话
                DataConfig.isSecurityCall = false;
                BroadcastEnclosure.connectAgora(this, RequestConfig.JPUSH_CALL_VOICE);
                break;
            default:
                break;
        }
    }

    // 显示正常的表情界面
    private void showNormalEmotion(boolean isShow) {
        ViewCommon.initView();
        if (isShow) {
            EmotionManager.showEmotion(R.mipmap.emotion_normal);
        }
    }
}
