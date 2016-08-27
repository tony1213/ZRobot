package com.robot.et.core.software.common.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.face.iflytek.FaceDistinguishActivity;
import com.robot.et.db.RobotDB;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.FaceManager;

import java.util.Random;

/**
 * Created by houdeming on 2016/7/27.
 * 接受硬件消息的service
 */
public class MsgReceiverService extends Service {
    private Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("accept", "MsgReceiverService  onCreate()");
        intent = new Intent();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
        filter.addAction(BroadcastAction.ACTION_PLAY_MUSIC_END);
        filter.addAction(BroadcastAction.ACTION_SPEAK);
        filter.addAction(BroadcastAction.ACTION_FACE_DISTINGUISH);
        filter.addAction(BroadcastAction.ACTION_PHONE_HANGUP);
        filter.addAction(BroadcastAction.ACTION_OPEN_FACE_DISTINGUISH);
        filter.addAction(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION);
        filter.addAction(BroadcastAction.ACTION_TAKE_PHOTO_COMPLECTED);

        registerReceiver(receiver, filter);

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT)) {//唤醒中断
                Log.i("accept", "MsgReceiverService 接受到唤醒中断的广播");
                DataConfig.isSleep = false;
                responseAwaken();

            } else if (intent.getAction().equals(BroadcastAction.ACTION_SPEAK)) {//说话
                Log.i("accept", "MsgReceiverService  说话");
                int currentType = intent.getIntExtra("type", 0);
                String content = intent.getStringExtra("content");
                if (!TextUtils.isEmpty(content)) {
                    SpeechImpl.getInstance().startSpeak(currentType, content);
                } else {
                    SpeechImpl.getInstance().startListen();
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_PLAY_MUSIC_END)) {//音乐播放完成
                Log.i("accept", "MsgReceiverService  音乐播放完成");
                SpeechImpl.getInstance().startListen();
            } else if (intent.getAction().equals(BroadcastAction.ACTION_FACE_DISTINGUISH)) {//脸部识别之后要说的话
                Log.i("accept", "MsgReceiverService  脸部识别之后要说的话");
                String contetn = intent.getStringExtra("content");
                boolean isVerifySuccess = intent.getBooleanExtra("isVerifySuccess", false);
                if (DataConfig.isSleep) {//处于沉睡状态
                    DataConfig.isSleep = false;
                    if (isVerifySuccess) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, contetn);
                    } else {
                        SpeechImpl.getInstance().startListen();
                    }
                } else {//处于唤醒状态
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, contetn);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_OPEN_FACE_DISTINGUISH)) {//打开脸部识别
                Log.i("accept", "MsgReceiverService  打开脸部识别");
                boolean isVoiceFaceRecognise = intent.getBooleanExtra("isVoiceFaceRecognise", false);
                //硬件打开人脸识别
                if (!isVoiceFaceRecognise) {
                    //唤醒状态不去人脸识别
                    if (!DataConfig.isSleep) {
                        return;
                    }
                }

                //正在脸部识别不检测
                if (DataConfig.isFaceRecogniseIng) {
                    Log.i("accept", "MsgReceiverService  正在脸部识别");
                    return;
                }

                SpeechImpl.getInstance().cancelListen();

                intent.setClass(MsgReceiverService.this, FaceDistinguishActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!DataConfig.isTakePicture) {
                    intent.putParcelableArrayListExtra("faceInfo", RobotDB.getInstance().getFaceInfos());
                }
                startActivity(intent);

            } else if (intent.getAction().equals(BroadcastAction.ACTION_PHONE_HANGUP)) {//查看时电话挂断
                Log.i("accept", "MsgReceiverService  查看时电话挂断");
                // do nothing
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION)) {//机器人表情
                Log.i("accept", "MsgReceiverService  机器人表情");
                int emotionKey = intent.getIntExtra("emotion" , 0);
                if (emotionKey != 0) {
                    ViewCommon.initView();
                    EmotionManager.showEmotionAnim(emotionKey);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_TAKE_PHOTO_COMPLECTED)) {//自动拍照完成
                Log.i("accept", "MsgReceiverService  自动拍照完成");
                Bitmap bitmap = FaceManager.getBitmap();
                if (bitmap != null) {
                    ViewCommon.initView();
                    OneImgManager.showImg(bitmap);
                }
                SpeechImpl.getInstance().cancelListen();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "看我拍的怎么样呢，嘿嘿");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void responseAwaken() {
        //停止说
        SpeechImpl.getInstance().cancelSpeak();
        //停止听
        SpeechImpl.getInstance().cancelListen();
        //停止唱歌
        BroadcastEnclosure.stopMusic(this);

        DataConfig.isScriptQA = false;
        DataConfig.isAppPushRemind = false;
        DataConfig.isStartTime = false;
        DataConfig.isControlToyCar = false;

        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, getAwakenContent());

    }

    //获取唤醒时要说的内容
    private String getAwakenContent() {
        String content = "";
        String[] wakeUpSpeakContent = getResources().getStringArray(R.array.wake_up_speak_content);
        int size = wakeUpSpeakContent.length;
        if (wakeUpSpeakContent != null && size > 0) {
            int i = new Random().nextInt(size);
            content = wakeUpSpeakContent[i];
        }
        return content;
    }

}
