package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.common.enums.EmotionEnum;
import com.robot.et.core.software.common.move.Come;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.entity.ResponseAppRemindInfo;
import com.robot.et.entity.ScriptActionInfo;
import com.robot.et.util.AlarmRemindManager;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.EnumManager;
import com.robot.et.util.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by houdeming on 2016/7/28.
 */
public class CommandHandler {

    private Context context;

    public CommandHandler(Context context) {
        this.context = context;
    }

    public boolean isCustorm(String result) {
        if (!TextUtils.isEmpty(result)) {
            if (isAppPushRemind(result)) {
                return true;
            }

            if (isControlMove(result)) {
                return true;
            }

            if (isScriptQA(result)) {
                return true;
            }

            if (isCustomDialogue(result)) {
                return true;
            }

            if (isMatchEmotion(result)) {
                return true;
            }
        }

        return false;
    }

    //是否控制运动
    public boolean isControlMove(String result) {
        Log.i("ifly", "执行语音运动控制");
        if (!TextUtils.isEmpty(result)) {
            // 如果字数大于6个字数时，作为胡乱听得不处理
            if (result.length() > 6) {
                return false;
            }

            int moveKey = EnumManager.getMoveKey(result);
            Log.i("ifly", "moveKey===" + moveKey);
            if (moveKey != 0) {
                if (DataConfig.isControlMotion) {// 运动开关开启
//                if (DataConfig.isControlToyCar) {//控制小车
//                    DataConfig.controlNum = 0;
//                    BroadcastEnclosure.controlToyCarMove(context, moveKey, DataConfig.toyCarNum);
//                    SpeechImpl.getInstance().startListen();
//                } else {//控制机器人
//                }
                    //控制机器人
                    Log.i("ifly", "执行语音控制机器人");
                    DataConfig.isControlRobotMove = true;
                    // 防止运动的过程中，再执行指令，要先停止前面的指令，再执行
                    if (DataConfig.isComeIng) {
                        DataConfig.isComeIng = false;
                        Come.stopTimer();
                    }
                    BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 0, 1000, 0);

                    if (result.contains("过来")) {// 过来场景
                        String content = "好的，主人，我来啦";
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, content);
                        DataConfig.isOpenRadar = true;
                        BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_RADAR);
                        // 过来的时候当唤醒时只有头转的时候，此时身体也要转过来，头部归位
                        int degree = DataConfig.voiceDegree;
                        long delay = 0;
                        if (degree != 0) {
                            delay = 600;
                            if (degree <= 30) {// (0-30)身体向右转
                                BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.RIGHT.getMoveKey(), degree, 1000, 0);
                            } else {// (330-360)身体向左转
                                int angle = 360 - degree;
                                BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.LEFT.getMoveKey(), angle, 1000, 0);
                            }
                            // 头部归位
                            BroadcastEnclosure.controlHead(context, DataConfig.TURN_HEAD_ABOUT, "0", 1000);
                        }
                        // 过来
                        Come.come(context, delay);

                    } else {// 运动的语音指令
                        String content = getRandomAnswer();
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, content);

                        int digit = Utilities.chineseNumber2Int(result);
                        Log.i("ifly", "result===" + result);
                        Log.i("ifly", "digit===" + digit);
                        if (moveKey == ControlMoveEnum.LEFT.getMoveKey() || moveKey == ControlMoveEnum.RIGHT.getMoveKey()) {
                            // 左转右转
                            if (digit == 0) {
                                digit = 90;// 默认90度
                            }
                        } else if (moveKey == ControlMoveEnum.TURN_AFTER.getMoveKey()) {// 向后转
                            digit = 180;
                        } else {// 前进
                            if (digit == 0) {
                                digit = 1 * 1000;// 默认1米
                            } else {
                                digit *= 1000;// 单位是mm
                            }
                            // 只有前进的时候加雷达
                            if (moveKey == ControlMoveEnum.FORWARD.getMoveKey()) {
                                // 前进的时候开启雷达监测，用来蔽障
                                DataConfig.isOpenRadar = true;
                                BroadcastEnclosure.openHardware(context, DataConfig.HARDWARE_RADAR);
                            }
                        }
                        // 距离：毫米  时间：毫秒
                        BroadcastEnclosure.controlMoveBySerialPort(context, moveKey, digit, 1000, 0);
                    }

                } else {// 运动开关关闭
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "抱歉，运动控制已关闭，请说：打开运动开启运动控制");
                }
                return true;
            }
        }
        return false;
    }

    //是否是自定义问答
    public boolean isCustomDialogue(String result) {
        String[] questions = context.getResources().getStringArray(R.array.custom_question);
        int length = questions.length;
        if (questions != null && length > 0) {
            for (int i = 0; i < length; i++) {
                String question = questions[i];
                if (result.contains(question) || question.contains(result)) {
                    String[] answers = context.getResources().getStringArray(R.array.custom_answer);
                    if (answers != null && answers.length > 0) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, answers[i]);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //是否是APP提醒必须要说的话
    public boolean isAppPushRemind(String result) {
        if (DataConfig.isAppPushRemind) {
            handleAppRemind(result);
            return true;
        }
        return false;
    }

    //是否是表情
    public boolean isMatchEmotion(String result) {
        EmotionEnum emotionEnum = EnumManager.getEmotionEnum(result);
        if (emotionEnum != null) {
            DataConfig.isEmotionAnim = true;
            ViewCommon.initView();
            EmotionManager.showEmotionAnim(emotionEnum.getEmotionKey());
            String answer = emotionEnum.getRequireAnswer();
            if (!TextUtils.isEmpty(answer)) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, answer);
            } else {
                SpeechImpl.getInstance().startListen();
            }
            return true;
        }
        return false;
    }

    //是否是APP发来的是剧本的问答
    public boolean isScriptQA(String result) {
        if (DataConfig.isScriptQA) {
            new ScriptHandler().appScriptQA(context, result);
            return true;
        }
        return false;
    }

    //没有响应App的命令
    public void noResponseApp() {
        if (!DataConfig.isStartTime) {
            DataConfig.isStartTime = true;
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (DataConfig.isStartTime) {
//                        ResponseAppRemindInfo mInfo = new ResponseAppRemindInfo();
//                        mInfo.setAnswer("");
//                        mInfo.setOriginalTime(AlarmRemindManager.getOriginalAlarmTime());
//                        HttpManager.pushMsgToApp(JSON.toJSONString(mInfo), RequestConfig.TO_APP_REMIND, new PushResultHandler(context));
//
//                        doAppRemindNoResponse();
//                    }
//                }
//            }, 15 * 1000);
        }
    }

    //控制移动的时候，随机回答内容
    private String getRandomAnswer() {
        String[] randomDatas = new String[]{"好的", "收到"};
        int randNum = new Random().nextInt(randomDatas.length);
        return randomDatas[randNum];
    }

    //APP发来的提醒需求处理
    private void handleAppRemind(String result) {
        ResponseAppRemindInfo mInfo = new ResponseAppRemindInfo();
        mInfo.setAnswer(result);
        mInfo.setOriginalTime(AlarmRemindManager.getOriginalAlarmTime());
//        HttpManager.pushMsgToApp(JSON.toJSONString(mInfo), RequestConfig.TO_APP_REMIND, new PushResultHandler(context));

        if (!TextUtils.isEmpty(result)) {
            String answer = AlarmRemindManager.getRequireAnswer();
            if (!TextUtils.isEmpty(answer)) {
                if (result.contains(answer)) {//回答正确
                    DataConfig.isAppPushRemind = false;
                    DataConfig.isStartTime = false;
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "嘿嘿，我可以去玩喽");
                } else {//回答错误
                    doAppRemindNoResponse();
                }
            }
        }
    }

    //APP发来的提醒没有按照主人设置要求的话的处理
    private void doAppRemindNoResponse() {
        DataConfig.isAppPushRemind = false;
        DataConfig.isStartTime = false;
        SpeechImpl.getInstance().cancelSpeak();
        SpeechImpl.getInstance().cancelListen();
        int type = AlarmRemindManager.getSpareType();
        if (type != 0) {
            List<ScriptActionInfo> infos = new ArrayList<ScriptActionInfo>();
            ScriptActionInfo info = new ScriptActionInfo();
            info.setActionType(type);
            info.setContent(AlarmRemindManager.getSpareContent());
            infos.add(info);
            DataConfig.isPlayScript = false;
            ScriptHandler.doScriptAction(context, infos);
        } else {
            SpeechImpl.getInstance().startListen();
        }
    }
}
