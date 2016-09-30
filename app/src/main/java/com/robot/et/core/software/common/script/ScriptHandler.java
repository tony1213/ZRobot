package com.robot.et.core.software.common.script;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.RosConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.db.RobotDB;
import com.robot.et.entity.ScriptActionInfo;
import com.robot.et.entity.ScriptInfo;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.EnumManager;
import com.robot.et.util.FileUtils;
import com.robot.et.util.MatchStringUtil;
import com.robot.et.util.MusicManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by houdeming on 2016/7/6.
 */
public class ScriptHandler implements Script {

    //表演剧本
    public static void playScript(Context context, String content) {
        if (!TextUtils.isEmpty(content)) {
            BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_TWO, "0", 1000);
            // 根据剧本名字获取具体的剧本信息
            List<ScriptActionInfo> infos = getScriptActions(content);
            Log.i("netty", "playScript() infos.size()====" + infos.size());
            if (infos != null && infos.size() > 0) {
                DataConfig.isPlayScript = true;
                DataConfig.isSleep = false;
                doScriptAction(context, infos);
            }
        }
    }

    //执行剧本剧本动作
    public static void doScriptAction(Context context, List<ScriptActionInfo> infos) {
        if (infos != null && infos.size() > 0) {
            Log.i("netty", "doScriptAction()   infos.size()====" + infos.size());
            ScriptActionInfo info = infos.get(0);
            String content = info.getContent();
            Log.i("netty", "doScriptAction() info.getContent()====" + content);
            switch (info.getActionType()) {
                case ScriptConfig.SCRIPT_EXPRESSION://表情
                    Log.i("netty", "doScriptAction() 表情");
                    int emotionKey = EnumManager.getEmotionKey(content);
                    Log.i("netty", "doScriptAction() emotionKey====" + emotionKey);
                    BroadcastEnclosure.controlRobotEmotion(context, emotionKey);
                    handleNewScriptInfos(context, infos, true, getDealyTime(1500));

                    break;
                case ScriptConfig.SCRIPT_MUSIC://音乐
                    Log.i("netty", "doScriptAction() 音乐");
                    ScriptManager.setScriptActionInfos(infos);
                    DataConfig.isScriptPlayMusic = true;
                    DataConfig.isJpushPlayMusic = true;
                    playScriptMusic(context, RequestConfig.JPUSH_MUSIC, content, info.getSpareContent());

                    break;
                case ScriptConfig.SCRIPT_FOLLOW://跟随
                    Log.i("netty", "doScriptAction() 跟随");
                    String robotNum = "";
                    int toyCarNum = 0;
                    if (content.contains("机器人")) {
                        robotNum = info.getSpareContent();
                    } else if (content.contains("小车")) {
                        toyCarNum = MatchStringUtil.getToyCarNum(content);
                    }
                    Log.i("netty", "doScriptAction() robotNum====" + robotNum);
                    Log.i("netty", "doScriptAction() toyCarNum====" + toyCarNum);
                    // 跟随
                    BroadcastEnclosure.sendRos(context, RosConfig.FOLLOW, "");
                    handleNewScriptInfos(context, infos, true, getDealyTime(1500));

                    break;
                case ScriptConfig.SCRIPT_TURN_AROUND://转圈
                    Log.i("netty", "doScriptAction() 转圈");
                    int direction = ScriptManager.getTurnDirection(content);
                    int spareType = info.getSpareType();
                    Log.i("netty", "doScriptAction() direction====" + direction);
                    Log.i("netty", "doScriptAction() num====" + info.getSpareContent());
                    // 转圈
                    BroadcastEnclosure.controlMoveBySerialPort(context, direction, 500, 1000, spareType);
                    handleNewScriptInfos(context, infos, true, getDealyTime(1000));

                    break;
                case ScriptConfig.SCRIPT_QUESTION_ANSWER://问答
                    Log.i("netty", "doScriptAction() 问答");
                    ScriptManager.setScriptActionInfos(infos);
                    ScriptManager.setScriptAnswer("");
                    String requireAnswer = info.getSpareContent();
                    Log.i("netty", "doScriptAction() answer===" + requireAnswer);
                    String speakContent = "";
                    if (!TextUtils.isEmpty(requireAnswer)) {
                        DataConfig.isScriptQA = true;
                        ScriptManager.setScriptAnswer(requireAnswer);
                        speakContent = new StringBuffer(1024).append(content).append("，请回答：").append(requireAnswer).toString();
                    } else {
                        speakContent = content;
                    }
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SCRIPT, speakContent);

                    break;
                case ScriptConfig.SCRIPT_SPEAK://说话
                    Log.i("netty", "doScriptAction() 说话");
                    ScriptManager.setScriptActionInfos(infos);
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_SCRIPT, content);

                    break;
                case ScriptConfig.SCRIPT_HAND://手
                    Log.i("netty", "doScriptAction() 手");
                    // 举手摆手
                    String handDirection = ScriptManager.getHandDirection(info.getSpareContent());
                    // 左手右手
                    String handCategory = ScriptManager.getHandCategory(content);
                    Log.i("netty", "doScriptAction() handDirection===" + handDirection);
                    BroadcastEnclosure.controlArm(context, handCategory, handDirection, 1000);
                    handleNewScriptInfos(context, infos, true, getDealyTime(1000));

                    break;
                case ScriptConfig.SCRIPT_MOVE://走
                    Log.i("netty", "doScriptAction() 走");
                    int moveDirection = EnumManager.getMoveKey(content);
                    String spareContent = info.getSpareContent();
                    String distance = info.getSpareContent2();
                    int distanceInt = 500;
                    if (!TextUtils.isEmpty(distance)) {
                        if (TextUtils.isDigitsOnly(distance)) {
                            distanceInt = Integer.parseInt(distance);
                        }
                    }
                    Log.i("netty", "spareContent==" + spareContent);
                    int num = 0;
                    if (!TextUtils.isEmpty(spareContent)) {
                        if (spareContent.contains("小车")) {
                            num = MatchStringUtil.getToyCarNum(spareContent);
                            BroadcastEnclosure.controlToyCarMove(context, moveDirection, num);
                        } else {
                            BroadcastEnclosure.controlMoveBySerialPort(context, moveDirection, distanceInt, 1000, 0);
                        }
                    }

                    handleNewScriptInfos(context, infos, true, getDealyTime(500));

                    break;
                case ScriptConfig.SCRIPT_TURN://左转右转
                    Log.i("netty", "doScriptAction() 左转右转");
                    int turnDirection = EnumManager.getMoveKey(content);
                    String spareContent2 = info.getSpareContent2();
                    int moveRadius = info.getSpareType();
                    int turnInt = 60;
                    if (!TextUtils.isEmpty(spareContent2)) {
                        if (TextUtils.isDigitsOnly(spareContent2)) {
                            turnInt = Integer.parseInt(spareContent2);
                        }
                    }
                    BroadcastEnclosure.controlMoveBySerialPort(context, turnDirection, turnInt, 1000, moveRadius);
                    handleNewScriptInfos(context, infos, true, getDealyTime(500));

                    break;
                case ScriptConfig.SCRIPT_STOP://停止
                    Log.i("netty", "doScriptAction() 停止");
                    // 停止摆手
                    BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_TWO, "0", 1000);
                    // 停止走
                    BroadcastEnclosure.controlMoveBySerialPort(context, ControlMoveEnum.STOP.getMoveKey(), 1000, 1000, 0);
                    handleNewScriptInfos(context, infos, true, getDealyTime(500));

                    break;
                case ScriptConfig.SCRIPT_HEAD://头
                    Log.i("netty", "doScriptAction() 头");
                    String angle = info.getSpareContent();
                    // 如果是点头的话，时间3s，否则2s
                    boolean flag = turnHead(context, content, angle);
                    long time = 1000;
                    if (flag) {
                        time = 1500;
                    }
                    handleNewScriptInfos(context, infos, true, getDealyTime(time));

                    break;
                default:
                    break;
            }

        } else {
            Log.i("netty", "doScriptAction()剧本执行完毕");
            playScriptEnd(context);
        }
    }

    //头部转向
    //上下以垂直方向为0度，向前10度即-10，向后10度即+10  左右横向运动以正中为0度，向右10度即-10，向左10度即+10
    private static boolean turnHead(final Context context, String content, String angle) {
        boolean isHeadUp = false;
        String turnAngle = "";
        int headDirection = DataConfig.TURN_HEAD_ABOUT;
        boolean isSingle = true;
        if (TextUtils.equals(content, "抬头")) {
            Log.i("netty", "doScriptAction() 抬头");
            headDirection = DataConfig.TURN_HEAD_AROUND;
            turnAngle = "15";
            isSingle = true;
        } else if (TextUtils.equals(content, "点头")) {
            Log.i("netty", "doScriptAction() 点头");
            headDirection = DataConfig.TURN_HEAD_AROUND;
            turnAngle = "-10";
            isSingle = false;
            isHeadUp = true;
        } else if (TextUtils.equals(content, "左转")) {
            Log.i("netty", "doScriptAction() 头左转");
            headDirection = DataConfig.TURN_HEAD_ABOUT;
            turnAngle = getTurnAngle(true, angle);
            isSingle = true;
        } else if (TextUtils.equals(content, "右转")) {
            Log.i("netty", "doScriptAction() 头右转");
            headDirection = DataConfig.TURN_HEAD_ABOUT;
            turnAngle = getTurnAngle(false, angle);
            isSingle = true;
        }
        Log.i("netty", "doScriptAction() 头turnAngle====" + turnAngle);
        BroadcastEnclosure.controlHead(context, headDirection, turnAngle, 1000);
        if (!isSingle) {//点头
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BroadcastEnclosure.controlHead(context, DataConfig.TURN_HEAD_AROUND, "0", 1000);
                }
            }, 1000);
        }
        return isHeadUp;
    }

    //获取头部左转右转角度
    private static String getTurnAngle(boolean isLeft, String angleContent) {
        String angle = "";
        if (!TextUtils.isEmpty(angleContent)) {
            if (isLeft) {
                angle = angleContent;
            } else {
                angle = "-" + angleContent;
            }
        } else {
            if (isLeft) {
                angle = "-30";
            } else {
                angle = "30";
            }
        }
        return angle;
    }

    //更新剧本的内容
    public static void handleNewScriptInfos(final Context context, final List<ScriptActionInfo> infos, boolean isResume, final long delayTime) {
        if (DataConfig.isAppPushRemind) {
            DataConfig.isAppPushRemind = false;
            return;
        }

        if (infos != null && infos.size() > 0) {
            infos.remove(0);
            ScriptManager.setScriptActionInfos(infos);

            if (isResume) {
                if (DataConfig.isPlayScript) {
                    Log.i("netty", "delayTime111==" + delayTime);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doScriptAction(context, infos);
                        }
                    }, delayTime);

                } else {
                    Log.i("netty", "DataConfig.isPlayScript===false 剧本执行完毕");
                    playScriptEnd(context);
                }
            }
        } else {
            Log.i("netty", "setNewScriptInfos()剧本执行完毕");
            playScriptEnd(context);
        }
    }

    //表演剧本之前
    public static void playScriptStart(Context context) {
        SpeechImpl.getInstance().cancelSpeak();
        SpeechImpl.getInstance().cancelListen();
        BroadcastEnclosure.stopMusic(context);
    }

    //剧本执行完毕
    public static void playScriptEnd(Context context) {
        Log.i("netty", "playScriptEnd()");
        DataConfig.isPlayScript = false;
        if (!DataConfig.isPlayMusic) {
            BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_TWO, "0", 1000);
        }
        //重连netty
        BroadcastEnclosure.connectNetty(context);

    }

    private static long getDealyTime(long custormTime) {
        return custormTime;
    }

    //播放剧本音乐
    private static void playScriptMusic(Context context, int mediaType, String content, String url) {
        String musicSrc = "";
        if (!TextUtils.isEmpty(content)) {
            musicSrc = MusicManager.getDetailMusicSrc(mediaType, content);
        } else {
            musicSrc = url;
        }
        BroadcastEnclosure.startPlayMusic(context, musicSrc, DataConfig.PLAY_MUSIC);
    }

    //插入本地剧本
    public static void addLocalScript(Context context, String scriptName) {
        String content = FileUtils.readFile(context, scriptName, "utf-8");
        Log.i("netty", "addScript  content==" + content);
        ScriptParse.parseScript(content, new ScriptInfoCallBack() {
            @Override
            public void getScribt(ScriptInfo info, List<ScriptActionInfo> infos) {
                if (info != null) {
                    Log.i("netty", "addScript  size==" + infos.size());
                    if (infos != null && infos.size() > 0) {
                        addScript(info, infos);
                    }
                }
            }
        });

    }

    //增加APP发来的图形编辑
    public static void addAppGraphicEdit(String content) {
        if (!TextUtils.isEmpty(content)) {
            ScriptParse.parseScript(content, new ScriptInfoCallBack() {
                @Override
                public void getScribt(ScriptInfo info, List<ScriptActionInfo> infos) {
                    if (info != null) {
                        Log.i("netty", "addScript  size==" + infos.size());
                        if (infos != null && infos.size() > 0) {
                            addScript(info, infos);
                        }
                    }
                }
            });
        }
    }

    //增加APP发过来的录制动作
    public static void addAppRecordAction(String content) {
        if (!TextUtils.isEmpty(content)) {
            ScriptParse.parseAppRecordAction(content, new ScriptInfoCallBack() {
                @Override
                public void getScribt(ScriptInfo info, List<ScriptActionInfo> infos) {
                    if (info != null) {
                        Log.i("netty", "addScript  size==" + infos.size());
                        if (infos != null && infos.size() > 0) {
                            addScript(info, infos);
                        }
                    }
                }
            });
        }
    }

    //增加APP发过来的音乐编舞
    public static void addAppRecordMusic(String content) {
        if (!TextUtils.isEmpty(content)) {
            ScriptParse.parseAppRecordMusic(content, new ScriptInfoCallBack() {
                @Override
                public void getScribt(ScriptInfo info, List<ScriptActionInfo> infos) {
                    if (info != null) {
                        Log.i("netty", "addScript  size==" + infos.size());
                        if (infos != null && infos.size() > 0) {
                            addScript(info, infos);
                        }
                    }
                }
            });
        }
    }

    //增加剧本
    private static void addScript(ScriptInfo info, List<ScriptActionInfo> infos) {
        RobotDB mDao = RobotDB.getInstance();
        String scriptName = info.getScriptContent();
        int scriptId = mDao.getScriptId(scriptName);
        Log.i("netty", "addScript temId===" + scriptId);
        if (scriptId != -1) {//已经存在
            Log.i("netty", "addScript 数据内容已存在");
            mDao.deleteScriptAction(scriptId);
        } else {//没有存在
            Log.i("netty", "addScript 无数据内容");
            mDao.addScript(info);
            scriptId = mDao.getScriptId(scriptName);
            Log.i("netty", "addScript scriptId===" + scriptId);
        }

        if (infos != null && infos.size() > 0) {
            for (ScriptActionInfo actionInfo : infos) {
                actionInfo.setScriptId(scriptId);
                mDao.addScriptAction(actionInfo);
            }
            Log.i("netty", "addScript 加入数据库成功");
        }
    }

    //获取剧本执行的动作
    private static List<ScriptActionInfo> getScriptActions(String scriptContent) {
        List<ScriptActionInfo> infos = new ArrayList<ScriptActionInfo>();
        if (!TextUtils.isEmpty(scriptContent)) {
            RobotDB mDao = RobotDB.getInstance();
            int scriptId = mDao.getScriptId(scriptContent);
            Log.i("netty", "getScriptActions()  scriptId====" + scriptId);
            if (scriptId != -1) {
                infos = mDao.getScriptActionInfos(scriptId);
            }
        }
        return infos;
    }

    @Override
    public void scriptPlayMusic(Context context, boolean isStart) {
        if (isStart) {
            if (DataConfig.isScriptPlayMusic) {
                DataConfig.isScriptPlayMusic = false;
                handleNewScriptInfos(context, ScriptManager.getScriptActionInfos(), true, getDealyTime(0));
            } else {
                playScript(context, MusicManager.getCurrentPlayName());
            }
        }
    }

    @Override
    public void scriptSpeak(Context context) {
        handleNewScriptInfos(context, ScriptManager.getScriptActionInfos(), true, getDealyTime(0));
    }

    @Override
    public void scriptAction(Context context) {
    }

    @Override
    public void appScriptQA(Context context, String result) {
        if (!TextUtils.isEmpty(result)) {
            String answer = ScriptManager.getScriptAnswer();
            if (!TextUtils.isEmpty(answer)) {
                if (result.contains(answer)) {//回答正确
                    DataConfig.isScriptQA = false;
                    scriptSpeak(context);
                } else {//回答错误
                    SpeechImpl.getInstance().startListen();
                }
            }
        }
    }

}
