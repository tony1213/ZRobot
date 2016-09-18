package com.robot.et.core.software.common.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EarsLightConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.UrlConfig;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.push.PushResultHandler;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.speech.Gallery;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.common.ximalaya.IPlayer;
import com.robot.et.core.software.common.ximalaya.IXiMaLaYa;
import com.robot.et.core.software.common.ximalaya.XiMaLaYa;
import com.robot.et.core.software.face.iflytek.FaceDistinguishActivity;
import com.robot.et.core.software.system.media.IMusic;
import com.robot.et.core.software.system.media.Music;
import com.robot.et.core.software.system.media.Sound;
import com.robot.et.db.RobotDB;
import com.robot.et.entity.PictureInfo;
import com.robot.et.util.BitmapUtil;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.FileUtils;
import com.robot.et.util.MusicManager;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;
import com.robot.et.util.TimerManager;
import com.robot.et.util.Utilities;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by houdeming on 2016/7/27.
 * 接受消息的service
 */
public class MsgReceiverService extends Service implements IMusic, IXiMaLaYa {
    private final String TAG = "Receiver";
    private Sound sound;
    private Music music;
    private XiMaLaYa xiMaLaYa;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("accept", "MsgReceiverService  onCreate()");
        // 初始化声音池
        sound = new Sound(this);
        // 初始化音乐媒体
        music = new Music(this);
        // 初始化喜马拉雅
        xiMaLaYa = new XiMaLaYa(this, DataConfig.XIMALAYA_APPSECRET, this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_PLAY_MUSIC_START);
        filter.addAction(BroadcastAction.ACTION_STOP_MUSIC);
        filter.addAction(BroadcastAction.ACTION_FACE_DISTINGUISH);
        filter.addAction(BroadcastAction.ACTION_PHONE_HANGUP);
        filter.addAction(BroadcastAction.ACTION_OPEN_FACE_DISTINGUISH);
        filter.addAction(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION);
        filter.addAction(BroadcastAction.ACTION_TAKE_PHOTO_COMPLECTED);
        filter.addAction(BroadcastAction.ACTION_CONTROL_HEAD_BY_APP);
        filter.addAction(BroadcastAction.ACTION_PLAY_SOUND_TIPS);
        registerReceiver(receiver, filter);

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_PLAY_SOUND_TIPS)) {//播放声音提示
                int playType = intent.getIntExtra("playType", 0);
                if (playType == DataConfig.PLAY) {// 播放
                    int soundId = intent.getIntExtra("soundId", 0);
                    Log.i(TAG, "MsgReceiverService  播放声音提示soundId==" + soundId);
                    // 播放声音提示
                    if (soundId != 0) {
                        sound.play(soundId);
                    }
                } else {// 停止播放
                    Log.i(TAG, "MsgReceiverService  停止播放声音提示");
                    // 停止播放声音提示
                    sound.stopSound();
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_PLAY_MUSIC_START)) {//音乐开始播放
                Log.i(TAG, "MsgReceiverService  开启音乐");
                // 播放类型
                int playType = intent.getIntExtra("playType", 0);
                // 获取播放的音乐路径
                String musicName = intent.getStringExtra("musicUrl");
                Log.i(TAG, "MsgReceiverService  playType==" + playType);
                Log.i(TAG, "MsgReceiverService  musicUrl==" + musicName);
                if (!TextUtils.isEmpty(musicName)) {
                    // 播放音乐
//                    boolean isSuccess = music.play(musicUrl);
//                    // 播放音乐失败
//                    if (!isSuccess) {
//                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, DataConfig.MUSIC_NOT_EXIT);
//                    }
                    if (playType == DataConfig.PLAY_MUSIC) {// 播放音乐
                        xiMaLaYa.playMusic(musicName, new IPlayer() {
                            @Override
                            public void playSuccess() {
                                Log.i(TAG, "MsgReceiverService  播放成功");
                            }

                            @Override
                            public void playFail() {
                                Log.i(TAG, "MsgReceiverService  播放失败");
                                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "抱歉，播放资源不存在");
                            }
                        });
                    } else {// 播放电台
                        xiMaLaYa.playRadio(musicName, new IPlayer() {
                            @Override
                            public void playSuccess() {
                                Log.i(TAG, "MsgReceiverService  播放成功");
                            }

                            @Override
                            public void playFail() {
                                Log.i(TAG, "MsgReceiverService  播放失败");
                                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "抱歉，播放资源不存在");
                            }
                        });
                    }
                    return;
                }

                SpeechImpl.getInstance().startListen();

            } else if (intent.getAction().equals(BroadcastAction.ACTION_STOP_MUSIC)) {//停止音乐播放
                Log.i(TAG, "MsgReceiverService  停止音乐播放");
                // 停止播放音乐
//                music.stopPlay();
                if (xiMaLaYa != null) {
                    xiMaLaYa.stopPlay();
                }
                // 耳朵灯光灭
                BroadcastEnclosure.controlEarsLED(MsgReceiverService.this, EarsLightConfig.EARS_CLOSE);

            } else if (intent.getAction().equals(BroadcastAction.ACTION_FACE_DISTINGUISH)) {//脸部识别之后要说的话
                Log.i(TAG, "MsgReceiverService  脸部识别之后要说的话");
                // 显示正常表情
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_normal);
                // 获取人脸识别后要说的话
                String contetn = intent.getStringExtra("content");
                // 脸部识别是否成功
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
                Log.i(TAG, "MsgReceiverService  打开脸部识别");
                //正在脸部识别不检测
                if (DataConfig.isFaceRecogniseIng) {
                    Log.i(TAG, "MsgReceiverService  正在脸部识别");
                    return;
                }

                SpeechImpl.getInstance().cancelListen();

                // 打开脸部识别
                intent.setClass(MsgReceiverService.this, FaceDistinguishActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putParcelableArrayListExtra("faceInfo", RobotDB.getInstance().getFaceInfos());
                startActivity(intent);

            } else if (intent.getAction().equals(BroadcastAction.ACTION_PHONE_HANGUP)) {//查看时电话挂断
                Log.i(TAG, "MsgReceiverService  查看时电话挂断");
                // 显示睡觉表情
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_rest_6);
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_EMOTION)) {//机器人表情
                Log.i(TAG, "MsgReceiverService  机器人表情");
                int emotionKey = intent.getIntExtra("emotion", 0);
                // 显示表情
                if (emotionKey != 0) {
                    ViewCommon.initView();
                    EmotionManager.showEmotionAnim(emotionKey);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_TAKE_PHOTO_COMPLECTED)) {//自动拍照完成
                Log.i(TAG, "MsgReceiverService  自动拍照完成");
                // 拍完照手放下来
                BroadcastEnclosure.controlWaving(context, ScriptConfig.HAND_DOWN, ScriptConfig.HAND_TWO, "0");
                // 获取拍照后传来的图片数据
                byte[] photoData = intent.getByteArrayExtra("photoData");
                if (photoData != null && photoData.length > 0) {
                    String robotNum = SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, "");
                    // 把byte数组转化为Bitmap
                    Bitmap bitmap = BitmapUtil.byte2Bitmap(photoData);
                    // 上传图片到服务器
                    upLoadFile(bitmap, robotNum);
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_HEAD_BY_APP)) {//app控制头
                directionTurn = intent.getIntExtra("directionTurn", 0);
                angle = intent.getStringExtra("angle");
                Log.i(TAG, "app控制头  directionTurn==" + directionTurn + ",angle===" + angle);
                timer = TimerManager.createTimer();
                // 每隔一秒去改变一下头部
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(1);
                    }
                }, 0, 1000);
            }
        }
    };

    private int directionTurn;// 头部移动的方向
    private String angle;// 头部移动的角度

    private Timer timer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                // 停止头部转动
                if (DataConfig.isHeadStop) {
                    TimerManager.cancelTimer(timer);
                    timer = null;
                } else {
                    //上下以垂直方向为0度，向前10度即-10，向后10度即+10  左右横向运动以正中为0度，向右10度即-10，向左10度即+10
                    int angleValue = getAngle();
                    angle = String.valueOf(angleValue);
                    Log.i(TAG, "app控制头  angleValue==" + angleValue + ",angle====" + angle);
                    // 发送控制头部的广播
                    BroadcastEnclosure.controlHead(MsgReceiverService.this, directionTurn, angle);

                    // 当头移动的方向满足一定的值后停止
                    if (directionTurn == DataConfig.TURN_HEAD_ABOUT) {//左右 +60 --- -60
                        DataConfig.LAST_HEAD_ANGLE_ABOUT = angleValue;
                        if (angleValue <= -60 || angleValue >= 60) {
                            DataConfig.isHeadStop = true;
                        }
                    } else if (directionTurn == DataConfig.TURN_HEAD_AROUND) {//前后 -18 ----- +18
                        DataConfig.LAST_HEAD_ANGLE_AROUND = angleValue;
                        if (angleValue <= -20 || angleValue >= 20) {
                            DataConfig.isHeadStop = true;
                        }
                    }
                }
            }
        }
    };

    //左右 +60 ---- -60    前后 -18 ----- +18   获取一直发的角度
    private int getAngle() {
        int data = 0;
        if (!TextUtils.isEmpty(angle)) {
            if (angle.contains("-") || TextUtils.isDigitsOnly(angle)) {
                int angleValue = Integer.parseInt(angle);
                int incrementValue = 5;//每次递增的值
                if (directionTurn == DataConfig.TURN_HEAD_AROUND) {//前后
                    if (DataConfig.isHeadFront) {//前
                        angleValue -= incrementValue;
                        if (angleValue <= -20) {
                            angleValue = -20;
                        }
                    } else {//后
                        angleValue += incrementValue;
                        if (angleValue >= 20) {
                            angleValue = 20;
                        }
                    }
                } else {//左右
                    if (DataConfig.isHeadLeft) {//左
                        angleValue += incrementValue;
                        if (angleValue >= 60) {
                            angleValue = 60;
                        }
                    } else {//右
                        angleValue -= incrementValue;
                        if (angleValue <= -60) {
                            angleValue = -60;
                        }
                    }
                }
                data = angleValue;
            }
        }
        return data;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        sound.destroy();
        if (music != null) {
            music.destroy();
        }
        TimerManager.cancelTimer(timer);
        timer = null;
        DataConfig.isShowLoadPicQRCode = false;
        if (xiMaLaYa != null) {
            xiMaLaYa.destroyPlayer();
        }
    }

    // 上传图片
    private void upLoadFile(Bitmap bitmap, String robotNum) {
        // 设置上传图片的key值
        String[] fileKeys = new String[]{"file"};
        // 设置图片路径名字
        final String fileName = robotNum + "_" + System.currentTimeMillis() + ".png";
        // 保存图片路径
        FileUtils.saveFilePath(bitmap, fileName);
        // 获取上传图片路径
        File[] files = FileUtils.getFiles(FileUtils.getFilePath(fileName));
        Log.i(TAG, "filePath====" + FileUtils.getFilePath(fileName));
        // 上传图片到服务器
        HttpManager.uploadFile(robotNum, fileKeys, files, new Gallery.IPicInfo() {
            @Override
            public void getPicInfo(PictureInfo info) {
                if (info != null) {
                    // 上传成功后把保存的图片删掉
                    FileUtils.deleteFile(fileName);
                    Message msg = handler.obtainMessage();
                    msg.obj = info;
                    picHandler.sendMessage(msg);
                }
            }
        });
    }

    // 主线程处理拍照的图片
    private Handler picHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "MsgReceiverService  显示下载图片二维码");
            PictureInfo info = (PictureInfo) msg.obj;
            if (info != null) {
                // 根据下载链接加图片名字生成二维码
                String url = UrlConfig.LOAD_PIC_PATH + "?fileName=" + info.getPicName();
                Bitmap qrCode = Utilities.createQRCode(url);
                // 显示二维码图片
                if (qrCode != null) {
                    ViewCommon.initView();
                    OneImgManager.showImg(qrCode, true);
                    DataConfig.isShowLoadPicQRCode = true;
                    SpeechImpl.getInstance().cancelListen();
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SHOW_QRCODE, "扫描二维码可以下载照片哦");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (DataConfig.isShowLoadPicQRCode) {
                                DataConfig.isShowLoadPicQRCode = false;
                                ViewCommon.initView();
                                EmotionManager.showEmotion(R.mipmap.emotion_blink);
                            }
                        }
                    }, 30 * 1000);// 30s 后待机
                }
            }
        }
    };

    //播放APP推送来的下一首
    private void playAppLower(String musicName) {
        int currentType = MusicManager.getCurrentMediaType();
        if (currentType != 0 && !TextUtils.isEmpty(musicName)) {
            // 获取下一首音乐名字
            String musicSrc = MusicManager.getLowerMusicSrc(currentType, musicName + ".mp3");
            Log.i(TAG, "MusicPlayerService musicSrc ===" + musicSrc);
            MusicManager.setCurrentPlayName(MusicManager.getMusicNameNoMp3(musicSrc));
            // 通知app当前播放状态
            HttpManager.pushMediaState(MusicManager.getCurrentMediaName(), "open", musicName, new PushResultHandler(this));
            // 开始播放音乐
            BroadcastEnclosure.startPlayMusic(this, musicSrc, DataConfig.PLAY_MUSIC);
        }
    }

    // 开始播放音乐
    private void playStart() {
        Log.i(TAG, "音乐开始播放");
        DataConfig.isPlayMusic = true;
        // 耳朵灯光闪烁
        BroadcastEnclosure.controlEarsLED(MsgReceiverService.this, EarsLightConfig.EARS_BLINK);
        // 来自app推送来的音乐，判断是否有动作
        if (DataConfig.isJpushPlayMusic) {
            new ScriptHandler().scriptPlayMusic(MsgReceiverService.this, true);
        }
    }

    // 音乐播放完成
    private void playEnd() {
        Log.i(TAG, "音乐播放完成");
        DataConfig.isPlayMusic = false;
//        SpectrumManager.hideSpectrum();
        // 耳朵灯光灭
        BroadcastEnclosure.controlEarsLED(MsgReceiverService.this, EarsLightConfig.EARS_CLOSE);

        //播放的是APP推送来的歌曲，继续播放下一首
        if (DataConfig.isJpushPlayMusic) {
            String musicName = MusicManager.getCurrentPlayName();
            if (!TextUtils.isEmpty(musicName)) {
                playAppLower(musicName);
            }
            return;
        }

        SpeechImpl.getInstance().startListen();
    }

    // 实现IMusic接口方法  开始播放音乐
    @Override
    public void startPlayMusic() {
        playStart();
    }

    // 实现IMusic接口方法  音乐播放完成
    @Override
    public void musicPlayComplected() {
        playEnd();
    }

    // 实现IXiMaLaYa接口方法  开始播放音乐
    @Override
    public void onPlayStart() {
        playStart();
    }

    // 实现IXiMaLaYa接口方法  音乐播放完成
    @Override
    public void onSoundPlayComplete() {
        playEnd();
    }

    // 实现IXiMaLaYa接口方法  播放异常
    @Override
    public void onPlayError() {
        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "抱歉，播放器异常");
    }
}
