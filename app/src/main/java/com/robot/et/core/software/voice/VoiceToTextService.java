package com.robot.et.core.software.voice;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.robot.et.R;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EarsLightConfig;
import com.robot.et.core.software.common.speech.CommandHandler;
import com.robot.et.core.software.common.speech.Gallery;
import com.robot.et.core.software.common.speech.MatchSceneHandler;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.speech.voice.ifly.IVoiceDictate;
import com.robot.et.core.software.common.speech.voice.ifly.VoiceDictate;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.TextManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.MatchStringUtil;
import com.robot.et.util.TimerManager;

import java.util.Timer;
import java.util.TimerTask;

// 语音听写
public class VoiceToTextService extends SpeechService implements IVoiceDictate {
    private boolean isFirstSetParam = true;
    private Timer timer;
    private boolean isFirstListen;
    private CommandHandler commandHandler;
    private VoiceDictate voiceDictate;
    private MatchSceneHandler matchSceneHandler;
    private final int UPDATE_VIEW = 1;
    private final int TIME_CONTROL = 2;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ifly", "VoiceToTextService  onCreate()");
        // 初始化语音听写对象
        SpeechImpl.setService(this);
        // 初始化
        voiceDictate = new VoiceDictate(this, this);
        // 初始化CommandHandler类
        commandHandler = new CommandHandler(this);
        // 初始化MatchSceneHandler类
        matchSceneHandler = new MatchSceneHandler(this);
        // 上传词表
        uploadThesaurus();
        // 语音控制运动默认打开
        DataConfig.isControlMotion = true;
        DataConfig.isComeIng = false;
        DataConfig.isOpenRadar = false;
    }

    // 上传词表
    private void uploadThesaurus() {
        boolean isSuccess = voiceDictate.uploadUserThesaurus("userwords", "userword");
        if (isSuccess) {
            Log.i("ifly", "上传词表成功");
        } else {
            Log.i("ifly", "上传词表失败");
            uploadThesaurus();
        }
    }

    // 开始听的处理
    private void beginListen() {
        // 是否是app的提醒
        if (DataConfig.isAppPushRemind) {
            commandHandler.noResponseApp();
        }
        // 开始计时听的时间
        if (!isFirstListen) {
            isFirstListen = true;
            startTimer();
        }
        // 表情动画或查看图片时，不显示眼睛
        if (!DataConfig.isLookPhoto) {
            if (DataConfig.isEmotionAnim) {
                DataConfig.isEmotionAnim = false;
            } else {
                handler.sendEmptyMessage(UPDATE_VIEW);
            }
        }
        // 调用听方法
        boolean isSuccess = voiceDictate.listen(isFirstSetParam, DataConfig.DEFAULT_SPEAK_MEN);
        if (isSuccess) {
            isFirstSetParam = false;
        } else {
            voiceDictate.stopListen();
            beginListen();
        }
    }

    //开始计时
    private void startTimer() {
        TimerManager.cancelTimer(timer);
        timer = null;
        timer = TimerManager.createTimer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(TIME_CONTROL);
            }
        }, 2 * 60 * 1000);//设置多少分钟沉睡（单位毫秒）
    }

    // 当在指定的时间内没有说话的话，停止听，进入睡眠状态
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_VIEW:
                    // 显示正常表情
                    ViewCommon.initView();
                    EmotionManager.showEmotion(R.mipmap.emotion_normal);

                    break;
                case TIME_CONTROL:
                    TimerManager.cancelTimer(timer);
                    timer = null;
                    voiceDictate.stopListen();
                    // 沉睡
                    MatchSceneHandler.sleep(VoiceToTextService.this);

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataConfig.isSleep = false;
        DataConfig.isLookPhoto = false;
        DataConfig.isSecuritySign = false;
        DataConfig.isEmotionAnim = false;
        DataConfig.isControlRobotMove = false;
        voiceDictate.destroy();
        TimerManager.cancelTimer(timer);
        timer = null;
    }

    /**
     * 继承父类方法
     * 开始听（外部调用）
     */
    @Override
    public void startListen() {
        super.startListen();
        // 要在开始听之前设置isFirstListen
        isFirstListen = false;
        beginListen();
    }

    /**
     * 继承父类方法
     * 取消听（外部调用）
     */
    @Override
    public void cancelListen() {
        super.cancelListen();
        voiceDictate.stopListen();
    }

    /**
     * 实现IVoiceDictate接口方法
     * 开始听（调用sdk内部方法）
     */
    @Override
    public void onBeginOfSpeech() {
        // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
        Log.i("ifly", "onBeginOfSpeech()");
        // 听的时候灯光常亮
        BroadcastEnclosure.controlEarsLED(VoiceToTextService.this, EarsLightConfig.EARS_BRIGHT);
    }

    /**
     * 实现IVoiceDictate接口方法
     * 完成听（调用sdk内部方法）
     */
    @Override
    public void onEndOfSpeech() {
        // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        Log.i("ifly", "结束说话 ");
        // 听结束灯光灭
        BroadcastEnclosure.controlEarsLED(VoiceToTextService.this, EarsLightConfig.EARS_CLOSE);
    }

    /**
     * 实现IVoiceDictate接口方法
     * 听写时说话音量的变化值（调用sdk内部方法）
     *
     * @param volume 音量值
     * @param data
     */
    @Override
    public void onVolumeChanged(int volume, byte[] data) {
        // 获取语音听写时说话音量的变化值
        Log.i("ifly", "当前正在说话，音量大小： volume==" + volume);
    }

    /**
     * 实现IVoiceDictate接口方法
     * 听写错误（调用sdk内部方法）
     *
     * @param error 错误信息
     */
    @Override
    public void onError(SpeechError error) {
        Log.i("ifly", "onError ");
        // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
        // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
        voiceDictate.stopListen();
        beginListen();
    }

    /**
     * 实现IVoiceDictate接口方法
     * 听写结果（调用sdk内部方法）
     *
     * @param result 听写结果
     */
    @Override
    public void onResult(String result) {
        Log.i("ifly", "result====" + result);
        // 停止听
        voiceDictate.stopListen();
        if (!TextUtils.isEmpty(result)) {
            // 结果只有一个字的时候过滤掉，继续听
            if (result.length() == 1) {
                beginListen();
                return;
            }

            TimerManager.cancelTimer(timer);
            timer = null;
            isFirstListen = false;

            if (DataConfig.isLookPhoto) {// 查看图片
                if (MatchStringUtil.matchString(result, MatchStringUtil.lastPhotoRegex)) {// 上一张照片
                    Gallery.showLastOnePic(VoiceToTextService.this);
                    return;
                } else if (MatchStringUtil.matchString(result, MatchStringUtil.nextPhotoRegex)) {// 下一张照片
                    Gallery.showNextPic(VoiceToTextService.this);
                    return;
                } else {
                    DataConfig.isLookPhoto = false;
                    // 显示语音听写转化的文字内容
                    ViewCommon.initView();
                    TextManager.showText(result);
                }
            } else {// 不是查看图片
                // 显示语音听写转化的文字内容
                ViewCommon.initView();
                TextManager.showText(result);
            }

            // 判断是否是自定义
            if (commandHandler.isCustorm(result)) {
                return;
            }
            // 是否是匹配的场景
            if (matchSceneHandler.isMatchScene(result)) {
                return;
            }
            // 科大讯飞文本理解
            SpeechImpl.getInstance().understanderText(result);

        } else {
            beginListen();
        }
    }
}
