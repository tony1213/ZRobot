package com.robot.et.core.software.voice.iflytek;

import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.robot.et.common.DataConfig;
import com.robot.et.common.RequestConfig;
import com.robot.et.common.enums.SceneServiceEnum;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.VoicePhoneCallBack;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.voice.SpeechService;
import com.robot.et.core.software.voice.iflytek.util.PhoneManager;
import com.robot.et.core.software.voice.iflytek.util.ResultParse;
import com.robot.et.entity.RemindInfo;
import com.robot.et.util.AlarmRemindManager;
import com.robot.et.util.EnumManager;
import com.robot.et.util.MusicManager;
import com.robot.et.voice.ifly.ITextUnderstand;
import com.robot.et.voice.ifly.TextUnderstand;

import org.json.JSONObject;

// 科大讯飞文本理解
public class IflyTextUnderstanderService extends SpeechService implements ITextUnderstand {
    private String underStandContent;
    private TextUnderstand textUnderstand;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ifly", "IflyTextUnderstanderService onCreate()");
        SpeechImpl.setService(this);
        // 初始化
        textUnderstand = new TextUnderstand(this, this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        textUnderstand.destroy();
    }

    // 如果有答案，直接说答案，没答案的话，把问题传给图灵进行理解
    private void speakContent(String question, String answer) {
        Log.i("ifly", "IflyTextUnderstanderService question===" + question);
        Log.i("ifly", "IflyTextUnderstanderService answer===" + answer);
        if (!TextUtils.isEmpty(answer)) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, answer);
        } else {
            SpeechImpl.getInstance().understanderTextByTuring(question);
        }
    }

    // 具体处理文本理解的结果
    private void resultHandle(String result) {
        ResultParse.parseAnswerResult(result, new ParseResultCallBack() {
            @Override
            public void getResult(String question, String service, JSONObject jObject) {
                if (!TextUtils.isEmpty(question)) {
                    if (!TextUtils.isEmpty(service)) {
                        // 获取当前属于哪一个场景
                        SceneServiceEnum serviceEnum = EnumManager.getIflyScene(service);
                        Log.i("ifly", "IflyTextUnderstanderService serviceEnum===" + serviceEnum);
                        if (serviceEnum != null) {
                            String answer = "";
                            switch (serviceEnum) {
                                case BAIKE://百科
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case CALC://计算器
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case COOKBOOK://菜谱
                                    answer = ResultParse.getCookBookData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case DATETIME://日期
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case FAQ://社区问答
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case FLIGHT://航班查询
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case HOTEL://酒店查询
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case MAP://地图查询
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case MUSIC://音乐
                                    answer = ResultParse.getMusicData(jObject, DataConfig.MUSIC_SPLITE);
                                    DataConfig.isJpushPlayMusic = false;
                                    String content = MusicManager.getMusicSpeakContent(DataConfig.MUSIC_SRC_FROM_OTHER, 0, answer);
                                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_MUSIC_START, content);

                                    break;
                                case RESTAURANT://餐馆
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case SCHEDULE://提醒
                                    // 日期 + 时间 + 做什么事 + 说的日期 + 说的时间
                                    RemindInfo info = ResultParse.getRemindData(jObject);
                                    if (info != null) {
                                        if (!TextUtils.isEmpty(info.getDate())) {
                                            answer = AlarmRemindManager.getIflyRemindTips(info);
                                        }
                                    }
                                    speakContent(question, answer);

                                    break;
                                case STOCK://股票查询
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case TRAIN://火车查询
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case TRANSLATION://翻译
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case WEATHER://天气查询
                                    answer = ResultParse.getWeatherData(jObject, city, area);
                                    if (!TextUtils.isEmpty(answer)) {
                                        if (answer.contains("空气质量")) {
                                            speakContent(question, answer);

                                        } else {
                                            StringBuffer buffer = new StringBuffer(1024);
                                            buffer.append(answer).append(city).append(area).append("的天气");
                                            String weatherContent = buffer.toString();
                                            textUnderstand.understandText(weatherContent);
                                        }
                                    } else {
                                        speakContent(question, answer);
                                    }

                                    break;
                                case OPENQA://褒贬&问候&情绪
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case TELEPHONE://打电话
                                    answer = ResultParse.getPhoneData(jObject);
                                    if (!TextUtils.isEmpty(answer)) {
                                        HttpManager.getRoomNum(answer, new VoicePhoneCallBack() {
                                            @Override
                                            public void getPhoneInfo(String userName, String result) {
                                                String content = PhoneManager.getCallContent(userName, result);
                                                if (!TextUtils.isEmpty(content)) {
                                                    DataConfig.isAgoraVideo = true;
                                                    SpeechImpl.getInstance().startSpeak(RequestConfig.JPUSH_CALL_VIDEO, "正在给" + content + "打电话");
                                                } else {
                                                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "主人，还没有这个人的电话呢，换个试试吧");
                                                }
                                            }
                                        });
                                    } else {
                                        speakContent(question, answer);
                                    }

                                    break;
                                case MESSAGE://发短信
                                    // do nothing
                                    speakContent(question, answer);

                                    break;
                                case CHAT://闲聊
                                    answer = ResultParse.getAnswerData(jObject);
                                    speakContent(question, answer);

                                    break;
                                case PM25://空气质量
                                    answer = ResultParse.getPm25Data(jObject, city, area);
                                    speakContent(question, answer);

                                    break;

                                default:
                                    speakContent(question, answer);
                                    break;
                            }

                        } else {
                            speakContent(question, "");
                        }

                    } else {
                        speakContent(question, "");
                    }
                } else {
                    speakContent(underStandContent, "");
                }
            }

            @Override
            public void onError(String errorMsg) {
                speakContent(underStandContent, "");
            }
        });

    }

    /**
     * 继承父类方法
     * 文本理解（外部调用）
     *
     * @param content 要理解的内容
     */
    @Override
    public void understanderTextByIfly(String content) {
        super.understanderTextByIfly(content);
        Log.i("ifly", "IflyTextUnderstanderService understanderTextByIfly===" + content);
        if (!TextUtils.isEmpty(content)) {
            underStandContent = content;
            boolean isSuccess = textUnderstand.understandText(content);
            if (!isSuccess) {
                speakContent(content, "");
            }
        } else {
            SpeechImpl.getInstance().startListen();
        }
    }

    /**
     * 实现ITextUnderstand接口方法
     * 理解成功（调用sdk内部方法）
     *
     * @param result 返回理解的结果
     */
    @Override
    public void onResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            resultHandle(result);
        } else {
            speakContent(underStandContent, "");
        }
    }

    /**
     * 实现ITextUnderstand接口方法
     * 理解异常（调用sdk内部方法）
     *
     * @param error 返回错误信息
     */
    @Override
    public void onError(SpeechError error) {
        // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
        Log.i("ifly", "文本理解onError Code==" + error.getErrorCode());
        // 使用图灵理解
        speakContent(underStandContent, "");
    }
}
