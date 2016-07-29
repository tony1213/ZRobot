package com.robot.et.core.software.custorm;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.EnumManager;
import com.robot.et.common.enums.MatchSceneEnum;
import com.robot.et.core.software.face.detector.FaceDataFactory;
import com.robot.et.core.software.face.detector.FaceDetectorActivity;
import com.robot.et.core.software.impl.SpeechlHandle;
import com.robot.et.core.software.system.media.MediaManager;
import com.robot.et.db.RobotDB;
import com.robot.et.util.MatchStringUtil;

/**
 * Created by houdeming on 2016/7/28.
 */
public class commandImpl implements command {

    private Context context;

    public commandImpl(Context context) {
        this.context = context;
    }

    public boolean isCustorm(String result) {
        if (!TextUtils.isEmpty(result)) {
            return isMatchScene(result);
        }
        return false;
    }

    @Override
    public boolean isMatchScene(String result) {
        MatchSceneEnum sceneEnum = EnumManager.getScene(result);
        Log.i("ifly", "sceneEnum=====" + sceneEnum);
        if (sceneEnum == null) {
            DataConfig.isFaceDetector = false;
            return false;
        }

        boolean flag = false;
        switch (sceneEnum) {
            case VOICE_BIGGEST_SCENE:// 声音最大
                flag = true;
                MediaManager.getInstance(context).setMaxVolume();
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量已经最大");

                break;
            case VOICE_LITTEST_SCENE:// 声音最小
                flag = true;
                MediaManager.getInstance(context).setCurrentVolume(6);
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量已经最小");

                break;
            case VOICE_BIGGER_INDIRECT_SCENE:// 间接增加声音
                flag = true;
                MediaManager.getInstance(context).increaseVolume();
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量增加");

                break;
            case VOICE_LITTER_INDIRECT_SCENE://间接降低声音
                flag = true;
                MediaManager.getInstance(context).reduceVolume();
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量减小");

                break;
            case VOICE_BIGGER_SCENE:// 直接增加声音
                flag = true;
                MediaManager.getInstance(context).increaseVolume();
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量增加");

                break;
            case VOICE_LITTER_SCENE://直接降低声音
                flag = true;
                MediaManager.getInstance(context).reduceVolume();
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "音量减小");

                break;
            case QUESTION_ANSWER_SCENE:// 智能学习回答话语
                flag = false;

                break;
            case DISTURB_OPEN_SCENE:// 免打扰开
                flag = false;

                break;
            case DISTURB_CLOSE_SCENE:// 免打扰关
                flag = false;

                break;
            case SHUT_UP_SCENE:// 闭嘴
                flag = true;
                SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "好的,我去玩去了");
                Intent intent = new Intent();
                intent.setAction(BroadcastAction.ACTION_WAKE_UP_RESET);
                context.sendBroadcast(intent);

                break;
            case DO_ACTION_SCENE:// 智能学习做动作
                flag = false;

                break;
            case CONTROL_TOYCAR_SCENE:// 控制玩具车
                flag = false;

                break;
            case RAISE_HAND_SCENE:// 抬手
                flag = false;

                break;
            case WAVING_SCENE:// 摆手
                flag = false;

                break;
            case OPEN_HOUSEHOLD_SCENE:// 打开家电
                flag = false;

                break;
            case CLOSE_HOUSEHOLD_SCENE:// 关闭家电
                flag = false;

                break;
            case FACE_NAME_SCENE:// 脸部名称
                if (DataConfig.isFaceDetector) {
                    DataConfig.isFaceDetector = false;
                    String faceName = MatchStringUtil.getFaceName(result);
                    Log.i("ifly", "faceName=====" + faceName);
                    if (!TextUtils.isEmpty(faceName)) {
                        flag = true;
                        FaceDataFactory.addFaceInfo(context, faceName);
                        SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, "我记住了，嘿嘿");

                    } else {
                        flag = false;
                    }
                } else {
                    flag = false;
                }

                break;
            case FACE_TEST_SCENE:// 脸部识别
                flag = true;
                Intent faceIntent = new Intent();
                faceIntent.setClass(context, FaceDetectorActivity.class);
                faceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                faceIntent.putParcelableArrayListExtra("faceInfo", RobotDB.getInstance(context).getFaceInfos());
                context.startActivity(faceIntent);

                break;

            default:
                break;
        }
        DataConfig.isFaceDetector = false;
        return flag;
    }

}
