package com.robot.et.core.software.netty;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.AlarmRemindManager;
import com.robot.et.common.BroadcastCommon;
import com.robot.et.common.DataConfig;
import com.robot.et.common.PlayerControl;
import com.robot.et.common.RequestType;
import com.robot.et.common.RobotLearnManager;
import com.robot.et.core.software.impl.SpeechlHandle;
import com.robot.et.core.software.system.media.MediaManager;
import com.robot.et.core.software.window.network.HttpManager;
import com.robot.et.core.software.window.network.NetResultParse;
import com.robot.et.core.software.window.network.NettyClient;
import com.robot.et.entity.JpushInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<Object> implements NettyClient {
    private Context context;

    public NettyClientHandler(Context context) {
        this.context = context;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.i("netty", "接受到netty服务器发来的消息msg===" + msg);
        String message = (String) msg;
        if (!TextUtils.isEmpty(message)) {
            setPushResult(message);
        }

    }

    //推送来的结果
    private void setPushResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            JpushInfo info = NetResultParse.getJpushInfo(result);
            if (info != null) {
                String direction = info.getDirection();
                if (TextUtils.isEmpty(direction)) {
                    doPushResult(info);
                    return;
                }

                Log.i("netty", "direction===" + direction);
                if (TextUtils.isDigitsOnly(direction)) {
                    BroadcastCommon.controlMoveByApp(context, direction);
                } else {
                    String splite = "__";
                    if (direction.contains(splite)) {//1_1(小车编号__方向指令)
                        String[] datas = direction.split(splite);
                        Log.i("netty", "datas[0]===" + datas[0]);
                        Log.i("netty", "datas[1]===" + datas[1]);
                        String carNum = datas[0];
                        int toyCarNum = 0;
                        if (!TextUtils.isEmpty(carNum)) {
                            if (TextUtils.isDigitsOnly(carNum)) {
                                toyCarNum = Integer.parseInt(carNum);
                            }
                        }
                        BroadcastCommon.controlToyCarMove(context, datas[1], toyCarNum);
                    }
                }
            }
        }
    }

    // 处理推送的结果
    private void doPushResult(JpushInfo info) {
        if (info != null) {
            int extra = info.getExtra();
            String musicContent = info.getMusicContent();
            Log.i("netty", "pushCode===" + extra);
            Log.i("netty", "musicContent===" + musicContent);

            DataConfig.isJpushStop = false;

            switch (extra) {
                case RequestType.JPUSH_MUSIC:// 音乐
                    Log.i("netty", "音乐");
                    playMp3(RequestType.JPUSH_MUSIC, "MUSIC", musicContent);

                    break;
                case RequestType.JPUSH_STORY:// 故事
                    Log.i("netty", "故事");
                    playMp3(RequestType.JPUSH_STORY, "STORY", musicContent);

                    break;
                case RequestType.JPUSH_SYNCHRONOUS_CLASSROOM:// 同步课堂
                    Log.i("netty", "同步课堂");
                    playMp3(RequestType.JPUSH_SYNCHRONOUS_CLASSROOM, "SYNCHRONOUS_CLASSROOM", musicContent);

                    break;
                case RequestType.JPUSH_THOUSANDS_WHY:// 十万个为什么
                    Log.i("netty", "十万个为什么");
                    playMp3(RequestType.JPUSH_THOUSANDS_WHY, "THOUSANDS_WHY", musicContent);

                    break;
                case RequestType.JPUSH_ENCYCLOPEDIAS:// 百科
                    Log.i("netty", "百科");
                    playMp3(RequestType.JPUSH_ENCYCLOPEDIAS, "ENCYCLOPEDIAS", musicContent);

                    break;
                case RequestType.JPUSH_VOLUME_ADJUST:// 播放器音量控制
                    Log.i("netty", "播放器音量控制");
                    if (!TextUtils.isEmpty(musicContent)) {
                        MediaManager media = MediaManager.getInstance(context);
                        int currentValue = (int) (media.getMaxVolume() * Double.parseDouble(musicContent));
                        media.setCurrentVolume(currentValue);
                    }

                    break;
                case RequestType.JPUSH_UPPER:// 上一首
                    Log.i("netty", "上一首 currentMediaType===" + PlayerControl.getCurrentMediaType());
                    playMp3(PlayerControl.getCurrentMediaType(), PlayerControl.getCurrentMediaName(), musicContent);

                    break;
                case RequestType.JPUSH_LOWER:// 下一首
                    Log.i("netty", "下一首 currentMediaType===" + PlayerControl.getCurrentMediaType());
                    playMp3(PlayerControl.getCurrentMediaType(), PlayerControl.getCurrentMediaName(), musicContent);

                    break;
                case RequestType.JPUSH_PAUSE:// 音乐暂停
                    Log.i("netty", "音乐暂停");
                    DataConfig.isJpushStop = true;
                    SpeechlHandle.cancelSpeak();
                    BroadcastCommon.stopMusic(context);

                    break;
                case RequestType.JPUSH_GET_MEDIASTATE:// 获取媒体当前状态
                    Log.i("netty", "获取媒体当前状态");
                    if (DataConfig.isPlayMusic) {//正在播放
                        HttpManager.pushMediaState(PlayerControl.getCurrentMediaName(), "open", PlayerControl.getCurrentPlayName(), this);
                    } else {
                        HttpManager.pushMediaState("", "close", "", this);
                    }

                    break;
                case RequestType.JPUSH_ALARM:// 闹铃
                    Log.i("netty", "闹铃");
                    AlarmRemindManager.setAppAlarm(context, info);

                    break;
                case RequestType.JPUSH_REMIND:// APP提醒
                    Log.i("netty", "APP提醒");
                    AlarmRemindManager.setAppAlarmRemind(context, musicContent);

                    break;
                case RequestType.JPUSH_ROBOT_LEARN:// 机器人问答库
                    Log.i("netty", "机器人问答库");
                    RobotLearnManager.insertLeanInfo(context, info.getQuestion(), info.getAnswer(), "", DataConfig.LEARN_BY_ROBOT);

                    break;
                case RequestType.JPUSH_ROBOT_SPEAK:// 机器人学习库，通过说话学习
                    Log.i("netty", "机器人问答库通过说话学习");
                    RobotLearnManager.learnByAppSpeak(context, DataConfig.LEARN_BY_ROBOT, musicContent);

                    break;
                case RequestType.JPUSH_PERSON_LEARN:// 个人问答库
                    Log.i("netty", "个人问答库");
                    RobotLearnManager.insertLeanInfo(context, info.getQuestion(), info.getAnswer(), "", DataConfig.LEARN_BY_PERSON);

                    break;
                case RequestType.JPUSH_UPDATE_USERPHONE_INFO:// 更新用户联系方式
                    Log.i("netty", "更新用户联系方式");
                    //do  nothing
                    break;
                case RequestType.JPUSH_PLAY_SCRIPT:// 表演剧本
                    Log.i("netty", "表演剧本");

                    break;
                case RequestType.JPUSH_PATROL_MOVING_TRACK:// 本体巡逻移动轨迹
                    Log.i("netty", "本体巡逻移动轨迹");

                    break;
                case RequestType.JPUSH_RECORDING_ACTION:// 录制动作
                    Log.i("netty", "录制动作");

                    break;
                case RequestType.JPUSH_DELETE_A_MESSAGE:// 删除留言
                    Log.i("netty", "删除留言");
                    AlarmRemindManager.deleteAppRemindTips(context, musicContent);

                    break;
                case RequestType.JPUSH_CHOREOGRAPHY_DANCE:// 为某首歌曲编排舞蹈
                    Log.i("netty", "为某首歌曲编排舞蹈");

                    break;
                case RequestType.JPUSH_SCENE_INTERACTION:// 场景互动
                    Log.i("netty", "场景互动");

                    break;
                case RequestType.JPUSH_GRAPHIC_EDITOR:// 图形编辑
                    Log.i("netty", "图形编辑");

                    break;
                case RequestType.JPUSH_FROLIC:// 嬉闹
                    Log.i("netty", "嬉闹");

                    break;

                default:// agora音视频
                    break;
            }

        }
    }

    //播放.mp3文件
    private void playMp3(int mediaType, String mediaName, String musicName) {
        PlayerControl.setCurrentMediaType(mediaType);
        PlayerControl.setCurrentMediaName(mediaName);
        PlayerControl.setCurrentPlayName(musicName);

        HttpManager.pushMediaState(mediaName, "open", musicName, this);
        SpeechlHandle.cancelSpeak();
        BroadcastCommon.stopMusic(context);
        SpeechlHandle.cancelListen();
        DataConfig.isJpushPlayMusic = true;
        String speakContent = PlayerControl.getMusicSpeakContent(DataConfig.MUSIC_SRC_FROM_JPUSH, mediaType, musicName);
        SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_MUSIC_START, speakContent);
    }

    @Override
    public void connect(String result) {
        BroadcastCommon.connectNetty(context);
    }

}
