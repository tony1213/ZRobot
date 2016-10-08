package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EarsLightConfig;
import com.robot.et.common.RosConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.enums.MatchSceneEnum;
import com.robot.et.core.software.camera.TakePhotoActivity;
import com.robot.et.core.software.common.move.Dance;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.ros.client.VisualClient;
import com.robot.et.core.software.system.media.MediaManager;
import com.robot.et.entity.LearnAnswerInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.EnumManager;
import com.robot.et.util.FaceManager;
import com.robot.et.util.MatchStringUtil;
import com.robot.et.util.RobotLearnManager;

/**
 * Created by houdeming on 2016/9/9.
 * 场景匹配处理
 */
public class MatchSceneHandler {
    private Context context;

    public MatchSceneHandler(Context context) {
        this.context = context;
    }

    // 匹配的场景
    public boolean isMatchScene(final String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        }
        // 获取场景的Enum
        MatchSceneEnum sceneEnum = EnumManager.getScene(result);
        Log.i("ifly", "sceneEnum=====" + sceneEnum);
        // 如果场景为null的话，获取机器人的学习内容
        if (sceneEnum == null) {
            DataConfig.isFaceDetector = false;
            LearnAnswerInfo info = RobotLearnManager.getRobotLearnInfo(result);
            String content = info.getAnswer();//回答的话语
            boolean flag = false;
            if (!TextUtils.isEmpty(content)) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, content);
                flag = true;
            }
            return flag;
        }

        boolean flag = false;
        switch (sceneEnum) {
            case VOICE_BIGGEST_SCENE:// 声音最大
                flag = true;
                MediaManager.getInstance().setMaxVolume();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量已经最大");

                break;
            case VOICE_LITTEST_SCENE:// 声音最小
                flag = true;
                MediaManager.getInstance().setCurrentVolume(6);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量已经最小");

                break;
            case VOICE_BIGGER_INDIRECT_SCENE:// 间接增加声音
                flag = true;
                MediaManager.getInstance().increaseVolume();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量增加");

                break;
            case VOICE_LITTER_INDIRECT_SCENE://间接降低声音
                flag = true;
                MediaManager.getInstance().reduceVolume();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量减小");

                break;
            case VOICE_BIGGER_SCENE:// 直接增加声音
                flag = true;
                MediaManager.getInstance().increaseVolume();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量增加");

                break;
            case VOICE_LITTER_SCENE://直接降低声音
                flag = true;
                MediaManager.getInstance().reduceVolume();
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量减小");

                break;
            case QUESTION_ANSWER_SCENE:// 智能学习回答话语
                flag = true;
                String content = RobotLearnManager.learnBySpeak(DataConfig.LEARN_BY_ROBOT, result);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, content);

                break;
            case DISTURB_OPEN_SCENE:// 免打扰开
                flag = true;
                HttpManager.changeRobotCallStatus(DataConfig.ROBOT_STATUS_DISYURB_NOT);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，进入免打扰模式");

                break;
            case DISTURB_CLOSE_SCENE:// 免打扰关
                flag = true;
                HttpManager.changeRobotCallStatus(DataConfig.ROBOT_STATUS_NORMAL);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，免打扰模式已关闭");

                break;
            case SHUT_UP_SCENE:// 闭嘴
                flag = true;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SLEEP, "那我休息了，白白");
                sleep(context);

                break;
            case DO_ACTION_SCENE:// 智能学习做动作

                break;
            case CONTROL_TOYCAR_SCENE:// 控制玩具车
                // 控制小车的不处理
//                flag = true;
//                DataConfig.isControlToyCar = true;
//                // 获取小车编号
//                int toyCarNum = MatchStringUtil.getToyCarNum(result);
//                Log.i("ifly", "toyCarNum=====" + toyCarNum);
//                DataConfig.toyCarNum = toyCarNum;
//                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");

                break;
            case RAISE_LEFT_HAND_SCENE:// 抬左手
                flag = true;
                hand(ScriptConfig.HAND_LEFT, "70", 1000);

                break;
            case RAISE_RIGHT_HAND_SCENE:// 抬右手
                flag = true;
                hand(ScriptConfig.HAND_RIGHT, "70", 1000);

                break;
            case RAISE_HAND_SCENE:// 抬手
                flag = true;
                hand(ScriptConfig.HAND_LEFT, "70", 1000);
                hand(ScriptConfig.HAND_RIGHT, "70", 1000);

                break;
            case WAVING_SCENE:// 摆手
                flag = true;
                BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_TWO, "60", 1000);

                break;
            case HEAD_UP_SCENE:// 抬头
                flag = true;
                head(DataConfig.TURN_HEAD_AROUND, "20", 1000);

                break;
            case HEAD_DOWN_SCENE:// 低头
                flag = true;
                head(DataConfig.TURN_HEAD_AROUND, "-15", 1000);

                break;
            case PLAY_SCRIPT_SCENE:// 表演节目
                flag = true;
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_normal);
//                if (isPlayCo) {
//                    isPlayCo = false;
//                    ScriptHandler.playScript(context, "合唱共舞");
//                } else {
//                    isPlayCo = true;
//                    ScriptHandler.playScript(context, "大家一起喜羊羊");
//                }

                Dance.dance(context, "小跳蛙");

                break;
            case FACE_NAME_SCENE:// 脸部名称
                if (DataConfig.isFaceDetector) {
                    String faceName = MatchStringUtil.getFaceName(result);
                    Log.i("ifly", "faceName=====" + faceName);
                    if (!TextUtils.isEmpty(faceName)) {
                        flag = true;
                        FaceManager.addFaceInfo(faceName);
                        ViewCommon.initView();
                        OneImgManager.showImg(R.mipmap.robot_qr_code);
                        DataConfig.isShowChatQRCode = true;
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SHOW_QRCODE, "好的，我记住了，请扫描二维码和我聊天");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (DataConfig.isShowChatQRCode) {
                                    DataConfig.isShowChatQRCode = false;
                                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SLEEP, "我休息去了");
                                    // 沉睡
                                    sleep(context);
                                }
                            }
                        }, 15 * 1000);// 15s 后沉睡

                    }
                }

                break;
            case FACE_TEST_SCENE:// 开启脸部识别
                flag = true;
                DataConfig.isVoiceFaceRecognise = true;
                BroadcastEnclosure.openFaceRecognise(context);

                break;
            case LOOK_PHOTO_SCENE:// 看看照片的标志
                flag = true;
                DataConfig.isLookPhoto = true;
                Gallery.getShowPic(context);

                break;
            case OPEN_SECURITY_SCENE:// 进入安保场景
                flag = true;
                DataConfig.isSecuritySign = true;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，已开启安保模式");
                break;
            case CLOSE_SECURITY_SCENE:// 解除安保场景
                flag = true;
                DataConfig.isSecuritySign = false;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，已解除安保模式");
                break;
            case PHOTOGRAPH_SCENE:// 拍照
                flag = true;
                // 头抬到最高点(20)
                BroadcastEnclosure.controlHead(context, DataConfig.TURN_HEAD_AROUND, String.valueOf(20), 1000);
                // 拍照时手抬起来
                BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_LEFT, "100", 1000);
                // 说提示音
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_NO_SOUND_TIPS, "好的，三，二，一，茄子");
                // 开始自动拍照
                Intent intent = new Intent();
                intent.setClass(context, TakePhotoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                break;
            case ENVIRONMENT_LEARN_SCENE:// 环境认识学习
                //这里是xxx
//                String environmentContent = MatchStringUtil.getEnvironmentLearnAnswer(result);
//                Log.i("ifly", "environmentContent=====" + environmentContent);
//                if (!TextUtils.isEmpty(environmentContent)) {
//                    flag = true;
//                    BroadcastEnclosure.sendRos(context, RosConfig.POSITION, environmentContent);
//                }

                break;
            case VISION_LEARN_SCENE:// 视觉学习
                flag = true;
                // 显示正常表情
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_normal);
                tempResult = result;
                // 视觉学习的时候，先关闭视觉人体监测，再打开视觉学习
                if (!isFirstInitVision) {
                    Log.e("Visual","Is Second VisualREC");
//                    if (visualClient == null) {
//                        visualClient = new VisualClient(this);
//                    }
//                    // 关闭视觉人体监测
//                    BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_VISUAL_BODY_TRK, "");
//                    // 打开视觉学习
//                    BroadcastEnclosure.sendRos(context, RosConfig.INIT_VISION, "");

                    BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_VISUAL_BODY_TRK, "");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 打开视觉学习 
                            BroadcastEnclosure.sendRos(context, RosConfig.INIT_VISION, "");
                        }
                    },500);
                    learnThing(tempResult);
                } else {
                    Log.e("Visual","Is First VisualREC");
                    try {
                        Log.e("Visual","step rec one");
                        BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_VISUAL_BODY_TRK, "");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BroadcastEnclosure.sendRos(context, RosConfig.INIT_VISION, "");
                            }
                        },500);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        Log.e("Visual","step rec two");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 视觉学习 
                                learnThing(result);
                            }
                        },2000);
                    }
                }

                break;
            case GO_WHERE_SCENE:// 去哪里的指令
//                String whereContent = MatchStringUtil.getGoWhereAnswer(result);
//                Log.i("ifly", "whereContent=====" + whereContent);
//                if (!TextUtils.isEmpty(whereContent)) {
//                    //通知机器人去哪里
//                    // do  thing
//                    flag = true;
//                    VisionRecogniseEnvironmentInfo info = RobotDB.getInstance().getVisionRecogniseEnvironmentInfo(whereContent);
//                    if (info != null) {
//
//                    }
//                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");
//                }

                break;
            case OPEN_MOTION_SCENE:// 打开运动
                flag = true;
                DataConfig.isControlMotion = true;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，运动控制已开启，如若关闭，请说：关闭运动");

                break;
            case CLOSE_MOTION_SCENE:// 关闭运动
                flag = true;
                DataConfig.isControlMotion = false;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，运动控制已关闭，如若开启，请说：打开运动");

                break;
            case FORGET_LEARN_SCENE:// 忘记学习内容
                flag = true;
                // 忘记学习内容
                BroadcastEnclosure.sendRos(context, RosConfig.FORGET_LEARN_CONTENT, "");

                break;

            case ROAM_SCENE:// 漫游
                flag = true;
                // 显示正常表情
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_normal);
                DataConfig.isControlRobotMove = false;

                Roam.roam(context);

                break;
            case FOLLOW_SCENE:// 跟着我
                flag = true;
                // 显示正常表情
                ViewCommon.initView();
                EmotionManager.showEmotion(R.mipmap.emotion_normal);

                // 跟着我的时候先关闭视觉学习，再打开视觉人体监测，需要500ms
                if (!isFirstInitFollow) {
                    Log.e("Visual","Is Second VisualBody TRK");
//                    if (visualClient == null) {
//                        visualClient = new VisualClient(this);
//                    }
//                    // 关闭视觉学习
//                    BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_DISTINGUISH, "");
//                    // 打开视觉人体检测
//                    BroadcastEnclosure.sendRos(context, RosConfig.OPEN_VISUAL_BODY_TRK, "");

                    BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_DISTINGUISH, "");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 打开人体跟随
                            BroadcastEnclosure.sendRos(context, RosConfig.OPEN_VISUAL_BODY_TRK, "");
                        }
                    },500);

                } else {
                    // 跟随
                    Log.e("Visual","Is First VisualBody TRK");
                    // 打开人体跟随
                    try{
                        Log.e("Visual","step trk 1");
                        BroadcastEnclosure.sendRos(context, RosConfig.CLOSE_DISTINGUISH, "");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BroadcastEnclosure.sendRos(context, RosConfig.OPEN_VISUAL_BODY_TRK, "");
                            }
                        },500);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        Log.e("Visual","step trk 2");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BroadcastEnclosure.sendRos(context, RosConfig.VISUAL_BODY_TRK, "");
                            }
                        },500);
                    }
                }
                break;

            case NAVIGATION_SCENE:// 导航到
//                String area = MatchStringUtil.getNavigationArea(result);
//                if (!TextUtils.isEmpty(area)) {
//                    flag = true;
//                    // 导航到
//                    BroadcastEnclosure.sendRos(context, RosConfig.NAVIGATION, area);
//                }

                break;

            default:
                break;
        }
        DataConfig.isFaceDetector = false;
        return flag;
    }

    //    private boolean isPlayCo;
    private boolean isFirstInitVision = true;// 初始化视觉学习
    private boolean isFirstInitFollow = true ;// 初始化跟随
    private String tempResult;
    private VisualClient visualClient;

    //手臂
    private void hand(final String handCategory, final String angle, final int moveTime) {
        BroadcastEnclosure.controlArm(context, handCategory, angle, moveTime);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BroadcastEnclosure.controlArm(context, handCategory, "0", moveTime);
            }
        }, 1400);
        SpeechImpl.getInstance().startListen();
    }

    // 头部
    private void head(final int direction, final String angle, final int moveTime) {
        BroadcastEnclosure.controlHead(context, direction, angle, moveTime);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                BroadcastEnclosure.controlHead(context, direction, "0", moveTime);
//            }
//        }, 1500);
        SpeechImpl.getInstance().startListen();
    }

    // 视觉学习东西
    private void learnThing(String result) {
        String visionContent = MatchStringUtil.getVisionLearnAnswer(result);
        if (TextUtils.isEmpty(visionContent)) {// 这是什么？
            BroadcastEnclosure.sendRos(context, RosConfig.LEARN_OBJECT_WHAT, "");
        } else {// 这是手机
            BroadcastEnclosure.sendRos(context, RosConfig.LEARN_OBJECT_KNOWN, visionContent);
        }
    }

    //让机器人睡觉
    public static void sleep(final Context context) {
        DataConfig.isSleep = true;
        // 耳朵灯灭
        BroadcastEnclosure.controlEarsLED(context, EarsLightConfig.EARS_CLOSE);
        // 胸口灯呼吸
        BroadcastEnclosure.controlChestLED(context, ScriptConfig.LED_BLINK);
        // 显示睡觉表情
        ViewCommon.initView();
        EmotionManager.showEmotionAnim(R.drawable.emotion_rest);
        // 防止人体检测立即开启，5s之后再开启人体检测
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 告诉机器人沉睡了
                if (DataConfig.isSleep) {
                    BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_SLEEP);
                }
            }
        }, 5 * 1000);
    }

    // 初始化视觉
    /*@Override
    public void initVisual(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, msg);
        }
    }*/

    // 视觉学习
    /*@Override
    public void initVisualLearn(boolean isSuccess) {
        if (isSuccess) {
            isFirstInitVision = true;
            isFirstInitFollow = false;
            learnThing(tempResult);
        } else {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉学习打开失败");
        }

    }*/

    // 视觉检测人体
   /* @Override
    public void initVisualBody(boolean isSuccess) {
        if (isSuccess) {
            isFirstInitFollow = true;
            isFirstInitVision = false;
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "好的");
            BroadcastEnclosure.sendRos(context, RosConfig.VISUAL_BODY_TRK, "");
        } else {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉人体检测打开失败");
        }
    }*/
}
