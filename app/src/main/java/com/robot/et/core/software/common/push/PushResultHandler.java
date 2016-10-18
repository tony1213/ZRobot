package com.robot.et.core.software.common.push;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.EarsLightConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.RosConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.NetResultParse;
import com.robot.et.core.software.common.network.NettyClientCallBack;
import com.robot.et.core.software.common.script.ScriptHandler;
import com.robot.et.core.software.common.script.TouchHandler;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.system.media.MediaManager;
import com.robot.et.entity.JpushInfo;
import com.robot.et.entity.LocationInfo;
import com.robot.et.util.AlarmRemindManager;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.LocationManager;
import com.robot.et.util.MatchStringUtil;
import com.robot.et.util.MusicManager;
import com.robot.et.util.NetworkUtil;
import com.robot.et.util.RobotLearnManager;

/**
 * Created by houdeming on 2016/9/16.
 * 推送结果的处理
 */
public class PushResultHandler implements NettyClientCallBack {
    private static Context context;
    private static final String TAG = "netty";

    public PushResultHandler(Context context) {
        this.context = context;
    }

    //推送来的结果
    public void setPushResult(String result) {
        Log.i(TAG, "result==" + result);

        if (!TextUtils.isEmpty(result)) {
            JpushInfo info = new JpushInfo();
            if (result.contains("msg")) {
                info = NetResultParse.getJpushInfo(result);
            } else {
                // 格式：result==="1"
                int length = result.length();
                Log.i(TAG, "result.length==" + length);
                if (length > 2) {
                    result = result.substring(1, length - 1);
                }
                info.setDirection(result);
            }

            if (info != null) {
                String direction = info.getDirection();
                Log.i(TAG, "direction===" + direction);
                if (TextUtils.isEmpty(direction)) {
                    doPushResult(info);
                    return;
                }
                DataConfig.isHeadStop = false;
                if (TextUtils.isDigitsOnly(direction)) {
                    int moveKey = Integer.parseInt(direction);
                    if (moveKey < 10) {
                        if (moveKey == 5) {
                            if (isControlHead) {
                                DataConfig.isHeadStop = true;
                            }
                        }
                        isControlHead = false;
                        Log.i(TAG, "控制脚走");
                        BroadcastEnclosure.controlMoveBySerialPort(context, moveKey, 1 * 1000, 1000, 0);
                    } else {
                        //上下以垂直方向为0度，向前10度即-10，向后10度即+10  左右横向运动以正中为0度，向右10度即-10，向左10度即+10
                        isControlHead = true;
                        int directionTurn = DataConfig.TURN_HEAD_ABOUT;
                        int lastAboutHead = DataConfig.LAST_HEAD_ANGLE_ABOUT;//最后一次左右
                        int lastAroundHead = DataConfig.LAST_HEAD_ANGLE_AROUND;//最后一次前后
                        String angle = "0";
                        switch (moveKey) {//左右 +60 ---- -60  前后 -18 ----- +18
                            case 11://头向前
                                Log.i(TAG, "头向前");
                                DataConfig.isHeadFront = true;
                                directionTurn = DataConfig.TURN_HEAD_AROUND;
                                angle = String.valueOf(lastAroundHead);
                                break;
                            case 12://头向后
                                Log.i(TAG, "头向后");
                                DataConfig.isHeadFront = false;
                                directionTurn = DataConfig.TURN_HEAD_AROUND;
                                angle = String.valueOf(lastAroundHead);
                                break;
                            case 13://头向左
                                Log.i(TAG, "头向左");
                                DataConfig.isHeadLeft = true;
                                directionTurn = DataConfig.TURN_HEAD_ABOUT;
                                angle = String.valueOf(lastAboutHead);
                                break;
                            case 14://头向右
                                Log.i(TAG, "头向右");
                                DataConfig.isHeadLeft = false;
                                directionTurn = DataConfig.TURN_HEAD_ABOUT;
                                angle = String.valueOf(lastAboutHead);
                                break;
                            default:
                                break;
                        }
                        handleHead(directionTurn, angle);

                    }
                } else {
                    String splite = "__";
                    if (direction.contains(splite)) {//1_1(小车编号__方向指令)
                        String[] datas = direction.split(splite);
                        Log.i(TAG, "datas[0]===" + datas[0]);
                        Log.i(TAG, "datas[1]===" + datas[1]);
                        BroadcastEnclosure.controlToyCarMove(context, getIntNum(datas[1]), getIntNum(datas[0]));
                    }
                }
            }
        }
    }

    private boolean isControlHead = false;

    //控制头
    private void handleHead(int directionTurn, String angle) {
        Intent intent = new Intent();
        intent.setAction(BroadcastAction.ACTION_CONTROL_HEAD_BY_APP);
        intent.putExtra("directionTurn", directionTurn);
        intent.putExtra("angle", angle);
        context.sendBroadcast(intent);
    }

    // 获取小车、方向的int值
    private int getIntNum(String result) {
        int num = 0;
        if (!TextUtils.isEmpty(result)) {
            if (TextUtils.isDigitsOnly(result)) {
                num = Integer.parseInt(result);
            }
        }
        return num;
    }

    // 处理推送的结果
    private void doPushResult(JpushInfo info) {
        if (info != null) {
            int extra = info.getExtra();
            String musicContent = info.getMusicContent();
            Log.i(TAG, "pushCode===" + extra);
            Log.i(TAG, "musicContent===" + musicContent);

            DataConfig.isJpushStop = false;

            switch (extra) {
                case RequestConfig.JPUSH_MUSIC:// 音乐
                    Log.i(TAG, "音乐");
                    playMp3(RequestConfig.JPUSH_MUSIC, "MUSIC", musicContent);

                    break;
                case RequestConfig.JPUSH_STORY:// 故事
                    Log.i(TAG, "故事");
                    playMp3(RequestConfig.JPUSH_STORY, "STORY", musicContent);

                    break;
                case RequestConfig.JPUSH_SYNCHRONOUS_CLASSROOM:// 同步课堂
                    Log.i(TAG, "同步课堂");
                    playMp3(RequestConfig.JPUSH_SYNCHRONOUS_CLASSROOM, "SYNCHRONOUS_CLASSROOM", musicContent);

                    break;
                case RequestConfig.JPUSH_THOUSANDS_WHY:// 十万个为什么
                    Log.i(TAG, "十万个为什么");
                    playMp3(RequestConfig.JPUSH_THOUSANDS_WHY, "THOUSANDS_WHY", musicContent);

                    break;
                case RequestConfig.JPUSH_ENCYCLOPEDIAS:// 百科
                    Log.i(TAG, "百科");
                    playMp3(RequestConfig.JPUSH_ENCYCLOPEDIAS, "ENCYCLOPEDIAS", musicContent);

                    break;
                case RequestConfig.JPUSH_VOLUME_ADJUST:// 播放器音量控制
                    Log.i(TAG, "播放器音量控制");
                    if (!TextUtils.isEmpty(musicContent)) {
                        MediaManager media = MediaManager.getInstance();
                        int currentValue = (int) (media.getMaxVolume() * Double.parseDouble(musicContent));
                        media.setCurrentVolume(currentValue);
                    }

                    break;
                case RequestConfig.JPUSH_UPPER:// 上一首
                    Log.i(TAG, "上一首 currentMediaType===" + MusicManager.getCurrentMediaType());
                    playMp3(MusicManager.getCurrentMediaType(), MusicManager.getCurrentMediaName(), musicContent);

                    break;
                case RequestConfig.JPUSH_LOWER:// 下一首
                    Log.i(TAG, "下一首 currentMediaType===" + MusicManager.getCurrentMediaType());
                    playMp3(MusicManager.getCurrentMediaType(), MusicManager.getCurrentMediaName(), musicContent);

                    break;
                case RequestConfig.JPUSH_PAUSE:// 音乐暂停
                    Log.i(TAG, "音乐暂停");
                    DataConfig.isJpushStop = true;
                    SpeechImpl.getInstance().cancelSpeak();
                    BroadcastEnclosure.stopMusic(context);

                    break;
                case RequestConfig.JPUSH_GET_MEDIASTATE:// 获取媒体当前状态
                    Log.i(TAG, "获取媒体当前状态");
                    if (DataConfig.isPlayMusic) {//正在播放
                        HttpManager.pushMediaState(MusicManager.getCurrentMediaName(), "open", MusicManager.getCurrentPlayName(), this);
                    } else {
                        HttpManager.pushMediaState("", "close", "", this);
                    }

                    break;
                case RequestConfig.JPUSH_ALARM:// 闹铃
                    Log.i(TAG, "闹铃");
                    AlarmRemindManager.addAppAlarmClock(info);

                    break;
                case RequestConfig.JPUSH_REMIND:// APP提醒
                    Log.i(TAG, "APP提醒");
                    AlarmRemindManager.addAppAlarmRemind(NetResultParse.parseAppRemind(musicContent));

                    break;
                case RequestConfig.JPUSH_ROBOT_LEARN:// 机器人问答库
                    Log.i(TAG, "机器人问答库");
                    RobotLearnManager.insertLeanInfo(info.getQuestion(), info.getAnswer(), "", DataConfig.LEARN_BY_ROBOT);

                    break;
                case RequestConfig.JPUSH_ROBOT_SPEAK:// 机器人学习库，通过说话学习
                    Log.i(TAG, "机器人问答库通过说话学习");
                    RobotLearnManager.learnByAppSpeak(DataConfig.LEARN_BY_ROBOT, musicContent);

                    break;
                case RequestConfig.JPUSH_PERSON_LEARN:// 个人问答库
                    Log.i(TAG, "个人问答库");
                    RobotLearnManager.insertLeanInfo(info.getQuestion(), info.getAnswer(), "", DataConfig.LEARN_BY_PERSON);

                    break;
                case RequestConfig.JPUSH_UPDATE_USERPHONE_INFO:// 更新用户联系方式
                    Log.i(TAG, "更新用户联系方式");
                    //do  nothing
                    break;
                case RequestConfig.JPUSH_PLAY_SCRIPT:// 表演剧本
                    Log.i(TAG, "表演剧本");
                    if (DataConfig.isVideoOrVoice) {
                        return;
                    }
                    ScriptHandler.playScriptStart(context);
                    ScriptHandler.playScript(context, musicContent);

                    break;
                case RequestConfig.JPUSH_PATROL_MOVING_TRACK:// 本体巡逻移动轨迹
                    Log.i(TAG, "本体巡逻移动轨迹");
                    //do nothing
                    break;
                case RequestConfig.JPUSH_RECORDING_ACTION:// 录制动作
                    Log.i(TAG, "录制动作");
                    ScriptHandler.addAppRecordAction(musicContent);

                    break;
                case RequestConfig.JPUSH_DELETE_A_MESSAGE:// 删除留言
                    Log.i(TAG, "删除留言");
                    AlarmRemindManager.deleteAppRemindTips(musicContent);

                    break;
                case RequestConfig.JPUSH_CHOREOGRAPHY_DANCE:// 为某首歌曲编排舞蹈
                    Log.i(TAG, "为某首歌曲编排舞蹈");
                    ScriptHandler.addAppRecordMusic(musicContent);

                    break;
                case RequestConfig.JPUSH_SCENE_INTERACTION:// 场景互动
                    Log.i(TAG, "场景互动");
                    if (DataConfig.isVideoOrVoice) {
                        return;
                    }
                    ScriptHandler.playScriptStart(context);
//                    ScriptHandler.playScript(context, musicContent);
                    // 表演本地剧本
                    ScriptHandler.playLocalScript(context);

                    break;
                case RequestConfig.JPUSH_GRAPHIC_EDITOR:// 图形编辑
                    Log.i(TAG, "图形编辑");
                    ScriptHandler.addAppGraphicEdit(musicContent);

                    break;
                case RequestConfig.JPUSH_FROLIC:// 嬉闹
                    Log.i(TAG, "嬉闹");
                    if (DataConfig.isVideoOrVoice) {
                        return;
                    }
                    ScriptHandler.playScriptStart(context);
                    TouchHandler.responseTouch(context, musicContent);

                    break;
                case RequestConfig.JPUSH_ROBOT_HOME_COORDINATE:// 机器人坐标信息告知app
                    Log.i(TAG, "机器人坐标信息告知app");
                    LocationInfo locationInfo = LocationManager.getInfo();
                    // 上传机器人信息
                    if (locationInfo != null) {
                        HttpManager.pushMsgToBindApp(JSON.toJSONString(locationInfo), RequestConfig.ROBOT_HOME_COORDINATE);
                    }

                    break;
                case RequestConfig.JPUSH_WIFI_INFO:// app推送来的WiFi信息
                    Log.i(TAG, "app推送来的WiFi信息");
                    NetResultParse.getCommandStr(musicContent, new IWiFiInfo() {
                        @Override
                        public void getWiFi(String WIFiName, String userName) {
                            if (TextUtils.equals(WIFiName, NetworkUtil.getConnectWifiName(context))) {
                                homeMeet(userName);
                            }
                        }
                    });

                    break;
                case RequestConfig.JPUSH_APP_CONTROL_ROBOT_WHERE:// 远程监护去哪里指令，控制机器人运动
                    Log.i(TAG, "远程监护去哪里指令，控制机器人运动");
                    if (!TextUtils.isEmpty(musicContent)) {
                        if (MatchStringUtil.matchString(musicContent, MatchStringUtil.navigationRegex)) {
                            String area = MatchStringUtil.getNavigationArea(musicContent);
                            Log.i(TAG, "远程监护去哪里指令area==" + area);
                            if (!TextUtils.isEmpty(area)) {
                                // 导航到
                                BroadcastEnclosure.sendRos(context, RosConfig.NAVIGATION, area);
                            }
                        }
                    }

                    break;
                default:// agora音视频
                    Log.i(TAG, "agora音视频");
                    Intent intent = new Intent();
                    intent.setAction(BroadcastAction.ACTION_JOIN_AGORA_ROOM);
                    intent.putExtra("JpushInfo", info);
                    context.sendBroadcast(intent);

                    break;
            }

        }
    }

    // 回家迎接
    private void homeMeet(String userName) {
        // 如果正在视频的话，不播放音乐
        if (DataConfig.isVideoOrVoice) {

        }
        // 表现出动态感、兴奋感
        // 灯开始闪烁，原地转一圈，
        beginHandler();
        BroadcastEnclosure.controlEarsLED(context, EarsLightConfig.EARS_BLINK);
        // 手上下摆动
        BroadcastEnclosure.controlArm(context, ScriptConfig.HAND_TWO, "60", 1000);
        // 说话
        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, userName + "回来了，我们去门口迎接他吧");
        // 走到门口

        // 2秒后把手放下来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BroadcastEnclosure.controlEarsLED(context, EarsLightConfig.EARS_CLOSE);
            }
        }, 2000);
    }

    //播放.mp3文件
    private void playMp3(int mediaType, String mediaName, String musicName) {
        // 保存当前播放类型
        MusicManager.setCurrentMediaType(mediaType);
        // 保存当前播放类型名字
        MusicManager.setCurrentMediaName(mediaName);
        // 保存当前播放歌曲
        MusicManager.setCurrentPlayName(musicName);

        // 告诉app当前播放的状态
        HttpManager.pushMediaState(mediaName, "open", musicName, this);

        // 如果正在视频的话，不播放音乐
        if (DataConfig.isVideoOrVoice) {
            return;
        }
        beginHandler();
        DataConfig.isJpushPlayMusic = true;
        // 获取播放音乐前要说的内容
        String speakContent = MusicManager.getMusicSpeakContent(DataConfig.MUSIC_SRC_FROM_JPUSH, mediaType, musicName);
        // 把内容说出来
        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_MUSIC_START, speakContent);
    }

    private void beginHandler() {
        // 每次播放心的音乐之前停止说、唱歌跟听
        SpeechImpl.getInstance().cancelSpeak();
        BroadcastEnclosure.stopMusic(context);
        SpeechImpl.getInstance().cancelListen();
    }

    @Override
    public void connect(String result) {
        // 重新连接netty
//        BroadcastEnclosure.connectNetty(context);
    }
}
