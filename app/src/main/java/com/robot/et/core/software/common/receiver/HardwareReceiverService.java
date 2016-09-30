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
import com.robot.et.common.EarsLightConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.TouchConfig;
import com.robot.et.common.UrlConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.hardware.light.LightHandler;
import com.robot.et.core.hardware.serialport.SerialPortHandler;
import com.robot.et.core.hardware.wakeup.IWakeUp;
import com.robot.et.core.hardware.wakeup.WakeUpHandler;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.RobotInfoCallBack;
import com.robot.et.core.software.common.network.VoicePhoneCallBack;
import com.robot.et.core.software.common.receiver.util.MoveFormat;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.script.TouchHandler;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.voice.util.PhoneManager;
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
 * 接受与硬件相关消息
 */
public class HardwareReceiverService extends Service implements IWakeUp {
    private final String TAG = "Receiver";
    private Timer timer;
    private WakeUpHandler wakeUpHandler;
    private final int CALL_PHONE = 1;
    private final int UPDATE_VIEW = 2;
    private final int SHORT_PRESS = 3;
    private final int TOUCH = 4;
    private final int LISTENER = 5;
    private LightHandler lightHandler;
    private SerialPortHandler serialPortHandler;
    private boolean isFirstPress;// 短按
    private static int mTouchId;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化唤醒
        wakeUpHandler = new WakeUpHandler(this);
        // 初始化灯
        lightHandler = new LightHandler();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_ROBOT_SLEEP);
        filter.addAction(BroadcastAction.ACTION_CONTROL_EARS_LED);
        filter.addAction(BroadcastAction.ACTION_CONTROL_WAVING);
        filter.addAction(BroadcastAction.ACTION_CONTROL_MOUTH_LED);
        filter.addAction(BroadcastAction.ACTION_ROBOT_TURN_HEAD);
        filter.addAction(BroadcastAction.ACTION_CONTROL_MOVE_BY_SERIALPORT);
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_ROBOT_SLEEP)) {// 机器人沉睡
                Log.i(TAG, "HardwareReceiverService 机器人沉睡");
                wakeUpHandler.sleepAwaken();
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_EARS_LED)) {// 耳朵灯
                int LEDState = intent.getIntExtra("LEDState", 0);
                Log.i(TAG, "HardwareReceiverService 耳朵灯LEDState==" + LEDState);
                lightHandler.setEarsLight(LEDState);
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_WAVING)) {//举手摆手
                Log.i(TAG, "举手摆手");
                String handCategory = intent.getStringExtra("handCategory");// 代表左手、右手还是双手
                String angleValue = intent.getStringExtra("angle");
                int moveTime = intent.getIntExtra("moveTime", 0);
                Log.i(TAG, "handCategory===" + handCategory);
                if (TextUtils.equals(handCategory, ScriptConfig.HAND_TWO)) {
                    // 当角度为0代表停止摆动，否则就是摆手
                    if (TextUtils.equals(angleValue, "0")) {// 停止摆动
                        // 停止左手
                        setHand(ScriptConfig.HAND_LEFT, 0, 1000);
                        // 停止右手
                        setHand(ScriptConfig.HAND_RIGHT, 0, 1000);
                    } else {// 摆手
                        handWaving();
                    }
                } else {
                    if (angleValue.contains("-") || TextUtils.isDigitsOnly(angleValue)) {
                        setHand(handCategory, Integer.parseInt(angleValue), moveTime);
                    }
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_MOUTH_LED)) {//胸口的LED灯
                Log.i(TAG, "胸口的LED灯");
                String LEDState = intent.getStringExtra("LEDState");// 代表灯的状态，（开、关、闪烁）
                if (!TextUtils.isEmpty(LEDState)) {
                    String json = MoveFormat.controlMouthLED(LEDState);
                    sendMoveAction(json);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_ROBOT_TURN_HEAD)) {//控制头转
                Log.i(TAG, "控制头转");
                int directionValue = intent.getIntExtra("direction", 0);
                int moveTime = intent.getIntExtra("moveTime", 0);
                String angleValue = intent.getStringExtra("angle");// 代表头转动的度数
                Log.i(TAG, "控制头转angleValue==" + angleValue);//-30
                if (!TextUtils.isEmpty(angleValue)) {
                    if (angleValue.contains("-") || TextUtils.isDigitsOnly(angleValue)) {
                        String json = MoveFormat.controlHead(directionValue, Integer.parseInt(angleValue), moveTime);
                        sendMoveAction(json);
                    }
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_MOVE_BY_SERIALPORT)) {// 控制运动
                int direction = intent.getIntExtra("direction", 0);
                int distance = intent.getIntExtra("distance", 0);
                int moveTime = intent.getIntExtra("moveTime", 0);
                int moveRadius = intent.getIntExtra("moveRadius", 0);
                Log.i(TAG, "distance==" + distance);
                int speed;
                if (direction == ControlMoveEnum.LEFT.getMoveKey() || direction == ControlMoveEnum.RIGHT.getMoveKey()
                        || direction == ControlMoveEnum.TURN_AFTER.getMoveKey()) {
                    // 左转右转后转是度数
                    speed = distance;
                    moveTime = distance * 1000 / 30;// 默认速度30度/s
                } else {// 前进后退是距离
                    speed = 300;// 默认速度300mm/s
                    moveTime = (distance * 10) / 3;
                }
                String json = MoveFormat.controlMove(direction, speed, moveTime, moveRadius);
                sendMoveAction(json);
            }
        }
    };

    // 摆手
    private void handWaving() {
        setHand(ScriptConfig.HAND_LEFT, 60, 1000);
        setHand(ScriptConfig.HAND_RIGHT, -60, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setHand(ScriptConfig.HAND_LEFT, 0, 1000);
                setHand(ScriptConfig.HAND_RIGHT, 0, 1000);
            }
        }, 1500);
    }

    // 设置数据
    private void setHand(String handCategory, int angleValue, int moveTime) {
        String json = MoveFormat.controlHand(handCategory, angleValue, moveTime);
        sendMoveAction(json);
    }

    // 响应之前的处理
    private boolean isHandle() {
        // 如果正在音视频的话什么也不处理
        if (DataConfig.isVideoOrVoice) {
            return true;
        }
        //唤醒状态不处理
        if (!DataConfig.isSleep) {
            return true;
        }
        // 可能在听,唱歌或者在说话，要停止掉，避免影响其它功能
        SpeechImpl.getInstance().cancelSpeak();
        SpeechImpl.getInstance().cancelListen();
        BroadcastEnclosure.stopMusic(HardwareReceiverService.this);
        return false;
    }

    // 接受到唤醒后的处理
    private void responseAwaken(boolean isSpeak) {
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
//        DataConfig.isControlToyCar = false;
        DataConfig.isLookPhoto = false;
        DataConfig.isShowLoadPicQRCode = false;
        DataConfig.isShowChatQRCode = false;

        // 正在表演剧本
        if (DataConfig.isPlayScript) {
            ScriptHandler.playScriptEnd(this);
        }
        // 停止运动
        BroadcastEnclosure.controlMoveBySerialPort(this, ControlMoveEnum.STOP.getMoveKey(), 1000, 1000, 0);

        if (isSpeak) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, getAwakenContent());
        } else {// 短按说你好
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "你好");
        }
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

    // 被唤醒
    private void awaken() {
        // 胸口灯常亮
        BroadcastEnclosure.controlChestLED(this, ScriptConfig.LED_ON);
    }

    @Override
    public void getVoiceWakeUpDegree(int degree) {
        Log.i(TAG, "HardwareReceiverService 接受到唤醒中断的广播");
        // 如果正在音视频的话关掉
        if (DataConfig.isVideoOrVoice) {
            return;
        }
        // 如果正在人脸识别的话关掉
        if (DataConfig.isFaceRecogniseIng) {
            BroadcastEnclosure.closeFaceDistinguish(HardwareReceiverService.this);
            handler.sendEmptyMessage(LISTENER);
            return;
        }
        // 相应唤醒后要做的事
        responseAwaken(true);
        awaken();
        Log.i(TAG, "degree==" + degree);
        handleAngle(degree);
        Log.i(TAG, "headAngle==" + headAngle);
        // 头转
        BroadcastEnclosure.controlHead(HardwareReceiverService.this, DataConfig.TURN_HEAD_ABOUT, String.valueOf(headAngle), 1000);
        Log.i(TAG, "bodyAngle==" + bodyAngle);
        if (bodyAngle > 0) {// 身体向右转
            BroadcastEnclosure.controlMoveBySerialPort(HardwareReceiverService.this, ControlMoveEnum.RIGHT.getMoveKey(), bodyAngle, 1000, 0);
        } else {// 身体向左转
            BroadcastEnclosure.controlMoveBySerialPort(HardwareReceiverService.this, ControlMoveEnum.LEFT.getMoveKey(), Math.abs(bodyAngle), 1000, 0);
        }
        // 只有身体转的时候处理
        if (degree > 30 && degree < 330) {
            // 耳朵的灯光在运动的时候进行闪烁
            BroadcastEnclosure.controlEarsLED(HardwareReceiverService.this, EarsLightConfig.EARS_BLINK);
            // 摆手
            BroadcastEnclosure.controlArm(HardwareReceiverService.this, ScriptConfig.HAND_TWO, "30", 1000);
        }
    }

    private int headAngle;//头部角度
    private int bodyAngle;//身体角度
    private static int lastAngle = 0;// 最后一次的角度，跟转身相关

    // 处理头与身体转的问题
    //0-30  头向右转  ： 330-360  头向左转
    //左右横向运动以正中为0度，向左10度即-10，向右10度即+10
    private void handleAngle(int angle) {
        if (angle < 0 || angle > 360) {
            return;
        }
        if (angle > 180 && angle <= 360) {
            angle = angle - 360;//-180-0
        }
        lastAngle += angle;
        if (lastAngle > 30) {
            bodyAngle = lastAngle;
            headAngle = 0;
            lastAngle = headAngle;
        } else if (lastAngle < -30) {
            bodyAngle = lastAngle;
            headAngle = 0;
            lastAngle = headAngle;
        } else {// 只需转头，不需要转身体
            headAngle = lastAngle;
            bodyAngle = 0;
        }
        // < 0 向左   > 0 向右
        if (bodyAngle > 180) {
            bodyAngle = bodyAngle - 360;
        } else if (bodyAngle < -180) {
            bodyAngle = 360 + bodyAngle;
        }
    }

    @Override
    public void bodyDetection() {
        Log.i(TAG, "HardwareReceiverService 人体检测");
        // 响应之前的处理
        if (isHandle()) {
            return;
        }

        // 为防止不停的人体检测触发，设置为已唤醒状态
        DataConfig.isSleep = false;
        // 显示正常表情
        handler.sendEmptyMessage(UPDATE_VIEW);
        awaken();

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
                controlLightLED(LIGHT_ON);
                // 耳朵灯旋转
                BroadcastEnclosure.controlEarsLED(HardwareReceiverService.this, EarsLightConfig.EARS_CLOCKWISE_TURN);
                // 防止在拨打电话前检测到多次，每次计时之前，先停止掉前面的计时器，保证最新的计时时间
                TimerManager.cancelTimer(timer);
                timer = null;
                // 开始计时
                timer = TimerManager.createTimer();
                // 如果没人摸机器人头部，则30s后，拨打用户手机视频
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(CALL_PHONE);
                    }
                }, 30 * 1000);

                return;
            }

            // 开启脸部识别
            BroadcastEnclosure.openFaceRecognise(HardwareReceiverService.this);
        }
    }

    @Override
    public void bodyTouch(int touchId) {
        Log.i(TAG, "HardwareReceiverService 硬件的触摸");
        // 获取触摸的位置
        if (touchId != 0) {
            // 响应之前的处理
            if (isHandle()) {
                return;
            }
            awaken();
            mTouchId = touchId;
            handler.sendEmptyMessage(TOUCH);

        }
    }

    @Override
    public void shortPress() {
        Log.i(TAG, "HardwareReceiverService 短按");
        // 当一直按的时候会一直触发，防止一直按着不放
        if (!isFirstPress) {
            isFirstPress = true;
            handler.sendEmptyMessage(SHORT_PRESS);
        }
    }

    // 触摸的处理
    private void touch(int touchId) {
        switch (touchId) {
            case TouchConfig.TOUCH_HEAD_TOP:// 头顶
                // 如果是安保模式的话，解除安保模式
                if (DataConfig.isSecuritySign) {// 安保模式
                    // 停止计时
                    TimerManager.cancelTimer(timer);
                    timer = null;
                    // 解除预警，耳朵灯变常亮，照明灯30s后灭
                    BroadcastEnclosure.controlEarsLED(HardwareReceiverService.this, EarsLightConfig.EARS_BRIGHT);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 照明灯灭
                            controlLightLED(LIGHT_OFF);
                        }
                    }, 30 * 1000);
                } else {// 不是安保模式
                    TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_HEAD_TOP));
                }
                break;
            case TouchConfig.TOUCH_HEAD_BACK:// 后脑勺
                TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_HEAD_BACK));
                break;
            case TouchConfig.TOUCH_EARS_LEFT:// 左耳
                TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_EARS_LEFT));
                break;
            case TouchConfig.TOUCH_EARS_RIGHT:// 右耳
                TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_EARS_RIGHT));
                break;
            case TouchConfig.TOUCH_ABDOMEN:// 腹部
                TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_BELLY));
                break;
            case TouchConfig.TOUCH_BACK:// 背部
                TouchHandler.responseTouch(this, String.valueOf(TouchHandler.TOUCH_BACK));
                break;
            default:
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CALL_PHONE:// 呼叫电话
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

                    break;
                case UPDATE_VIEW:// 更新view
                    // 显示正常表情
                    ViewCommon.initView();
                    EmotionManager.showEmotion(R.mipmap.emotion_normal);

                    break;
                case SHORT_PRESS:// 短按
                    // 如果正在音视频的话关掉
                    if (DataConfig.isVideoOrVoice) {
                        BroadcastEnclosure.closeAgora(HardwareReceiverService.this, false);
                        isFirstPress = false;
                        return;
                    }
                    // 如果正在人脸识别的话关掉
                    if (DataConfig.isFaceRecogniseIng) {
                        BroadcastEnclosure.closeFaceDistinguish(HardwareReceiverService.this);
                        isFirstPress = false;
                        awaken();
                        SpeechImpl.getInstance().startListen();
                        return;
                    }

                    responseAwaken(false);
                    awaken();
                    isFirstPress = false;

                    break;
                case TOUCH:// 触摸
                    touch(mTouchId);
                    break;
                case LISTENER:// 听，防止线程操作view
                    SpeechImpl.getInstance().startListen();
                    break;
                default:
                    break;
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

    // 照明灯 1：开  0：关
    private final int LIGHT_ON = 1;
    private final int LIGHT_OFF = 0;

    // 控制照明灯
    private void controlLightLED(int lightState) {
        lightHandler.setFloodLight(lightState);
    }

    // 发送与运动相关的json消息
    private void sendMoveAction(String result) {
        Log.i(TAG, "json===" + result);
        if (!TextUtils.isEmpty(result)) {
            byte[] content = result.getBytes();
            byte[] end = new byte[]{0x0a};//结束符
            byte[] realContent = byteMerger(content, end);
            if (serialPortHandler == null) {
                // 初始化串口
                serialPortHandler = new SerialPortHandler(this);
            }
            serialPortHandler.sendData(realContent);
        }
    }

    // 通过arraycopy获取byte数组
    private byte[] byteMerger(byte[] first, byte[] second) {
        byte[] content = new byte[first.length + second.length];
        System.arraycopy(first, 0, content, 0, first.length);
        System.arraycopy(second, 0, content, first.length, second.length);
        return content;
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
