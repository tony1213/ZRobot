package com.robot.et.core.software.common.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.enums.EmotionEnum;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.push.netty.NettyClientHandler;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.ViewCommon;
import com.robot.et.entity.ResponseAppRemindInfo;
import com.robot.et.entity.ScriptActionInfo;
import com.robot.et.util.AlarmRemindManager;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.EnumManager;

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
            if (isRosService(result)) {
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
        Log.e("ControlMove", "执行语音运动控制");
        if (!TextUtils.isEmpty(result)) {
            int moveKey = EnumManager.getMoveKey(result);
            Log.i("ifly", "moveKey===" + moveKey);
            if (moveKey != 0) {
                if (DataConfig.isControlToyCar) {//控制小车
                    DataConfig.controlNum = 0;
                    BroadcastEnclosure.controlToyCarMove(context, moveKey, DataConfig.toyCarNum);
                    SpeechImpl.getInstance().startListen();
                } else {//控制机器人
                    Log.e("ControlMove", "执行语音控制机器人");
                    DataConfig.isControlRobotMove = true;
                    String content = "";
                    if (result.contains("过来")) {
                        content = "好的，主人，我来啦";
                    } else {
                        content = getRandomAnswer();
                    }
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, content);

                    int digit = getIntNum(result);
                    Log.i("ControlMove", "result===" + result);
                    Log.i("ControlMove", "digit===" + digit);
                    if (digit != 0) {
                        Log.e("ControlMove", "数字不为空");
                        BroadcastEnclosure.controlRobotMoveRos(context, moveKey, String.valueOf(digit));
                    } else {
                        Log.e("ControlMove", "数字为空");
                        BroadcastEnclosure.controlRobotMove(context, moveKey);
                    }
                }
                return true;
            }
        }
        return false;
    }

    //获取要走的距离
    private int getIntNum(String result) {
        int data = 0;
        if (!TextUtils.isEmpty(result)) {
            char[] datas = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '两'};
            int resultLength = result.length();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < resultLength; i++) {
                char temp = result.charAt(i);
                if (Character.isDigit(temp)) {
                    buffer.append(temp);
                } else {
                    int datasLength = datas.length;
                    for (int j = 0; j < datasLength; j++) {
                        if (temp == datas[j]) {
                            data = j + 1;
                            if (j == datas.length - 1) {
                                data = 2;
                            }
                        }
                    }
                }
            }
            String tempDistance = buffer.toString();
            if (!TextUtils.isEmpty(tempDistance)) {
                if (TextUtils.isDigitsOnly(tempDistance)) {
                    data = Integer.parseInt(tempDistance);
                }
            }
        }
        return data;
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

    //是否ros服务
    public boolean isRosService(String result) {
        String content = "";
        if (!TextUtils.isEmpty(result)) {
            if (result.contains("导航到")) {
                //获取目的地
                int start = result.indexOf("到");
                content = result.substring(start + 1, result.length());
                sendRos("DestinationName", content);
                return true;
            } else if (result.contains("这个地方是") || result.contains("这里是")) {
                //获取地图上面的位置坐标
                int start = result.indexOf("是");
                content = result.substring(start + 1, result.length());
                sendRos("PositionName", content);
                return true;
            } else if (result.contains("记住这个是") || result.contains("记住这是")) {
                int start = result.indexOf("是");
                content = result.substring(start + 1, result.length());
                sendRos("DeepLearn", content);
                return true;
            } else if (TextUtils.equals("跟着我", result)) {
                sendRos("WORLDFOLLOWER", "1");
                SpeechImpl.getInstance().startListen();
            } else if (TextUtils.equals("不要跟着我", result)) {
                sendRos("WORLDFOLLOWER", "0");
                SpeechImpl.getInstance().startListen();
            } else {
                String rosKey = EnumManager.getRosServiceKey(result);
                Log.i("ros", "rosKey===" + rosKey);
                if (!TextUtils.isEmpty(rosKey)) {
                    sendRos(rosKey, "");
                    SpeechImpl.getInstance().startListen();
                    return true;
                }
            }
        }
        return false;
    }

    //是否是表情
    public boolean isMatchEmotion(String result) {
        EmotionEnum emotionEnum = EnumManager.getEmotionEnum(result);
        if (emotionEnum != null) {
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
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (DataConfig.isStartTime) {
                        ResponseAppRemindInfo mInfo = new ResponseAppRemindInfo();
                        mInfo.setAnswer("");
                        mInfo.setOriginalTime(AlarmRemindManager.getOriginalAlarmTime());
                        HttpManager.pushMsgToApp(JSON.toJSONString(mInfo), RequestConfig.TO_APP_REMIND, new NettyClientHandler(context));

                        doAppRemindNoResponse();
                    }
                }
            }, 15 * 1000);
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
        HttpManager.pushMsgToApp(JSON.toJSONString(mInfo), RequestConfig.TO_APP_REMIND, new NettyClientHandler(context));

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

    //ros的广播
    private void sendRos(String rosKey, String name) {
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_ROS_SERVICE);
        intent.putExtra("rosKey", rosKey);
        intent.putExtra("name", name);
        context.sendBroadcast(intent);
    }

    private void sendRosMove(String rosKey, String x, String y, String angle) {
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_ROS_SERVICE);
        intent.putExtra("rosKey", rosKey);
        intent.putExtra("dotX", x);
        intent.putExtra("dotY", y);
        intent.putExtra("angle", angle);
        context.sendBroadcast(intent);
    }
}
