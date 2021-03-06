package com.robot.et.core.software.common.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.UrlConfig;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.RobotInfoCallBack;
import com.robot.et.core.software.common.network.VoicePhoneCallBack;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.voice.iflytek.util.PhoneManager;
import com.robot.et.entity.RobotInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.DateTools;
import com.robot.et.util.DeviceUuidFactory;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;
import com.robot.et.util.TimerManager;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/9/10.
 * 接受硬件消息
 */
public class HardwareReceiverService extends Service {
    private final String TAG = "Receiver";
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
        filter.addAction(BroadcastAction.ACTION_HARDWARE_TOUCH);
        filter.addAction(BroadcastAction.ACTION_BODY_DETECTION);
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT)) {// 唤醒中断
                Log.i(TAG, "HardwareReceiverService 接受到唤醒中断的广播");
                // 相应唤醒后要做的事
                responseAwaken();

            } else if (intent.getAction().equals(BroadcastAction.ACTION_HARDWARE_TOUCH)) {// 硬件的触摸
                Log.i(TAG, "HardwareReceiverService 硬件的触摸");
                // 如果是安保模式的话，解除安保模式
                if (DataConfig.isSecuritySign) {// 安保模式
                    // 停止计时
                    TimerManager.cancelTimer(timer);
                    timer = null;
                    // 解除预警，耳朵灯变常亮，照明灯30s后灭
                    BroadcastEnclosure.controlEarsLED(HardwareReceiverService.this, "");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 照明灯灭
                            BroadcastEnclosure.controlLightLED(HardwareReceiverService.this, "");
                        }
                    }, 30 * 1000);
                } else {// 不是安保模式

                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_BODY_DETECTION)) {// 人体检测
                Log.i(TAG, "HardwareReceiverService 人体检测");
                // 如果正在音视频的话什么也不处理
                if (DataConfig.isVideoOrVoice) {
                    return;
                }
                // 检测到人时，可能在听,唱歌或者在说话，要停止掉，避免影响其它功能
                SpeechImpl.getInstance().cancelSpeak();
                SpeechImpl.getInstance().cancelListen();
                BroadcastEnclosure.stopMusic(HardwareReceiverService.this);
                // 获取当前的时间
                int currentHour = DateTools.getCurrentHour(System.currentTimeMillis());
                // 如果早上6点-9点，问早安,不识别人
                if (currentHour >= 6 && currentHour <= 9) {
                    // 通知视觉寻找人体
                    // do thing
                    // 说欢迎语并报天气
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_WEATHER, "早上好");
                    return;
                } else {
                    if (DataConfig.isSecuritySign) {// 安保模式
                        // 自身照明灯亮起
                        BroadcastEnclosure.controlLightLED(HardwareReceiverService.this, "");
                        // 耳朵灯旋转
                        BroadcastEnclosure.controlEarsLED(HardwareReceiverService.this, "");
                        // 防止在拨打电话前检测到多次，每次计时之前，先停止掉前面的计时器，保证最新的计时时间
                        TimerManager.cancelTimer(timer);
                        timer = null;
                        // 开始计时
                        timer = TimerManager.createTimer();
                        // 如果没人摸机器人头部，则30s后，拨打用户手机视频
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(1);
                            }
                        }, 30 * 1000);

                        return;
                    }
                    //唤醒状态不去人脸识别
                    if (!DataConfig.isSleep) {
                        return;
                    }
                    // 开启脸部识别
                    BroadcastEnclosure.openFaceRecognise(HardwareReceiverService.this);
                }
            }

        }
    };

    // 接受到唤醒后的处理
    private void responseAwaken() {
        //停止说
        SpeechImpl.getInstance().cancelSpeak();
        //停止听
        SpeechImpl.getInstance().cancelListen();
        //停止唱歌
        BroadcastEnclosure.stopMusic(this);

        DataConfig.isSleep = false;
        DataConfig.isScriptQA = false;
        DataConfig.isAppPushRemind = false;
        DataConfig.isStartTime = false;
        DataConfig.isControlToyCar = false;
        DataConfig.isLookPhoto = false;

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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 拨打用户手机视频
            if (DataConfig.isSecuritySign) {// 安保模式
                TimerManager.cancelTimer(timer);
                timer = null;
                // 获取管理员手机号
                final SharedPreferencesUtils share = SharedPreferencesUtils.getInstance();
                String adminPhone = share.getString(SharedPreferencesKeys.ADMINISTRATORS_PHONENUM, "");
                if (TextUtils.isEmpty(adminPhone)) {
                    HttpManager.getRobotInfo(UrlConfig.GET_ROBOT_INFO_BY_DEVICEID, new DeviceUuidFactory(HardwareReceiverService.this).getDeviceUuid(), new RobotInfoCallBack() {
                        @Override
                        public void onSuccess(RobotInfo info) {
                            if (info != null) {
                                String phone = info.getAdminPhone();
                                if (!TextUtils.isEmpty(phone)) {
                                    share.putString(SharedPreferencesKeys.ADMINISTRATORS_PHONENUM, phone);
                                    share.commitValue();
                                    callPhone(phone);
                                }
                            }
                        }

                        @Override
                        public void onFail(String errorMsg) {

                        }
                    });

                } else {
                    callPhone(adminPhone);
                }
            }
        }
    };

    // 呼叫电话
    private void callPhone(String adminPhone) {
        Log.i(TAG, "HardwareReceiverService adminPhone==" + adminPhone);
        HttpManager.getRoomNum(adminPhone, new VoicePhoneCallBack() {
            @Override
            public void getPhoneInfo(String userName, String result) {
                String content = PhoneManager.getCallContent(userName, result);
                if (!TextUtils.isEmpty(content)) {
                    // 是从安保模式打过去的电话
                    DataConfig.isSecurityCall = true;
                    // 默认开始视频通话
                    DataConfig.isAgoraVideo = true;
                    BroadcastEnclosure.connectAgora(HardwareReceiverService.this, RequestConfig.JPUSH_CALL_VIDEO);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        TimerManager.cancelTimer(timer);
        timer = null;
        DataConfig.isSecurityCall = false;
    }
}
