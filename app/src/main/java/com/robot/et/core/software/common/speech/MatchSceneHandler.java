package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.enums.MatchSceneEnum;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.push.netty.NettyClientHandler;
import com.robot.et.core.software.camera.AutoPhotographActivity;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.core.software.system.media.MediaManager;
import com.robot.et.db.RobotDB;
import com.robot.et.entity.LearnAnswerInfo;
import com.robot.et.entity.VisionRecogniseEnvironmentInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.EnumManager;
import com.robot.et.util.FaceManager;
import com.robot.et.util.MatchStringUtil;
import com.robot.et.util.RobotLearnManager;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;

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
    public boolean isMatchScene(String result) {
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
                sleep();

                break;
            case DO_ACTION_SCENE:// 智能学习做动作
                flag = false;

                break;
            case CONTROL_TOYCAR_SCENE:// 控制玩具车
                flag = true;
                DataConfig.isControlToyCar = true;
                // 获取小车编号
                int toyCarNum = MatchStringUtil.getToyCarNum(result);
                Log.i("ifly", "toyCarNum=====" + toyCarNum);
                DataConfig.toyCarNum = toyCarNum;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");

                break;
            case RAISE_HAND_SCENE:// 抬手
                flag = true;
                hand(1, ScriptConfig.HAND_RIGHT);

                break;
            case WAVING_SCENE:// 摆手
                flag = true;
                hand(1, ScriptConfig.HAND_TWO);

                break;
            case OPEN_HOUSEHOLD_SCENE:// 打开家电
                flag = true;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");
                HttpManager.pushMsgToApp("开", RequestConfig.TO_APP_BLUETOOTH_CONTROLLER, new NettyClientHandler(context));

                break;
            case CLOSE_HOUSEHOLD_SCENE:// 关闭家电
                flag = true;
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");
                HttpManager.pushMsgToApp("关", RequestConfig.TO_APP_BLUETOOTH_CONTROLLER, new NettyClientHandler(context));

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
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SHOW_QRCODE, "好的，我记住了，请扫描二维码和我聊天");
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
//                Gallery.getShowPic(context);
                SpeechImpl.getInstance().startListen();

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
                // 说提示音
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "好的，3,2,1，茄子");
                // 拍照时手抬起来
                BroadcastEnclosure.controlWaving(context, ScriptConfig.HAND_UP, ScriptConfig.HAND_TWO, "0");
                // 开始自动拍照
                Intent intent = new Intent();
                intent.setClass(context, AutoPhotographActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                break;
            /*case VISION_LEARN_SCENE:// 视觉学习
                flag = true;
                String visionContent = MatchStringUtil.getVisionLearnAnswer(result);
                Log.i("ifly", "visionContent=====" + visionContent);
                String speakContent = "";
                if (TextUtils.isEmpty(visionContent)) {//问题：这是什么？
                    //从底层视觉学习库中查找答案
                    // do thing
                    String learnAnswer = "";


                    if (TextUtils.isEmpty(learnAnswer)) {
                        speakContent = "不知道，您能告诉我吗？";
                    } else {
                        speakContent = "这是" + learnAnswer;
                    }
                } else {//告诉答案：例如：这是玩具车
                    //告诉底层视觉学习的答案
                    // do thing


                    speakContent = "好的，我记住了";
                }
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, speakContent);

                break;
            case ENVIRONMENT_LEARN_SCENE:// 环境认识学习
                //这里是xxx
                String environmentContent = MatchStringUtil.getEnvironmentLearnAnswer(result);
                Log.i("ifly", "environmentContent=====" + environmentContent);
                if (!TextUtils.isEmpty(environmentContent)) {
                    flag = true;
                    //通知视觉学习记住内容 获取物体坐标
                    // do  thing
                    addVisionRecogniseInfo(environmentContent, "", "");

                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，我记住了");
                }

                break;*/
            case GO_WHERE_SCENE:// 去哪里的指令
                String whereContent = MatchStringUtil.getGoWhereAnswer(result);
                Log.i("ifly", "whereContent=====" + whereContent);
                if (!TextUtils.isEmpty(whereContent)) {
                    //通知机器人去哪里
                    // do  thing
                    flag = true;
                    VisionRecogniseEnvironmentInfo info = RobotDB.getInstance().getVisionRecogniseEnvironmentInfo(whereContent);
                    if (info != null) {

                    }
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");
                }

                break;
            case VISION_LEARN_SIGN_SCENE:// 进入视觉学习的标志
                flag = true;
                String knowContent = "";
                if (!DataConfig.isIntoKnowEnvironment) {
                    DataConfig.isIntoKnowEnvironment = true;
                    DataConfig.isStartRoam = true;
                    DataConfig.isRecogniseComplected = true;
                    knowContent = "我会的可多了，但是这是个新环境，我可以走一圈认识一下吗？";
                } else {
                    DataConfig.isIntoKnowEnvironment = false;
                    DataConfig.isStartRoam = false;
                    DataConfig.isRecogniseComplected = false;
                    knowContent = "我会记住我见过的所有东西，你想考考我吗？";
                }

                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, knowContent);

                break;
            case START_RECOGNISE_ENVIRONMENT_SCENE:// 进入识别环境的标志
                if (DataConfig.isStartRoam) {
                    flag = true;
                    DataConfig.isStartRoam = false;
                    SpeechImpl.getInstance().startListen();
                    Log.i("ifly", "通知本体开始漫游并识别物体");
                    //通知本体开始漫游并识别物体
                    //do thing
                }

                break;
            case RECOGNISE_COMPLECTED_SCENE:// 识别环境完成
                if (DataConfig.isRecogniseComplected) {
                    DataConfig.isRecogniseComplected = false;
                    flag = true;
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "我认识好了，你可以叫我去厨房、客厅，我就会去那儿");
                    //通知本体回到原位
                    //do thing


                }

                break;

            default:
                break;
        }
        DataConfig.isFaceDetector = false;
        return flag;
    }

    //手臂
    private void hand(int num, final String handCategory) {
        BroadcastEnclosure.controlWaving(context, ScriptConfig.HAND_UP, handCategory, "0");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BroadcastEnclosure.controlWaving(context, ScriptConfig.HAND_DOWN, handCategory, "0");
            }
        }, 1500);
        SpeechImpl.getInstance().startListen();
    }

    //让机器人睡觉
    public void sleep() {
        DataConfig.isSleep = true;
        // 唤醒后重置
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_WAKE_UP_RESET);
        context.sendBroadcast(intent);
        // 显示睡觉表情
        ViewCommon.initView();
        EmotionManager.showEmotion(R.mipmap.emotion_blink);
    }

    //增加视觉环境学习到数据库
    private void addVisionRecogniseInfo(String positionName, String positionX, String positionY) {
        RobotDB mDb = RobotDB.getInstance();
        VisionRecogniseEnvironmentInfo info = mDb.getVisionRecogniseEnvironmentInfo(positionName);
        if (info != null) {//已经存在
            mDb.updateVisionPositionXY(positionName, positionX, positionY);
        } else {//不存在
            VisionRecogniseEnvironmentInfo mInfo = new VisionRecogniseEnvironmentInfo();
            String robotNum = SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, "");
            mInfo.setRobotNum(robotNum);
            mInfo.setPositionName(positionName);
            mInfo.setPositionX(positionX);
            mInfo.setPositionY(positionY);
            mDb.addVisionRecogniseEnvironment(mInfo);
        }
    }
}
