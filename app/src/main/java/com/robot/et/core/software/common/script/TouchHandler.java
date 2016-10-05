package com.robot.et.core.software.common.script;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.common.enums.EmotionEnum;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.util.BroadcastEnclosure;

/**
 * Created by houdeming on 2016/7/16.
 * app触摸机器人的控制
 */
public class TouchHandler {
    public final static int TOUCH_HEAD_TOP = 0;//头顶
    public final static int TOUCH_EYE = 1;//眼睛
    public final static int TOUCH_HAND_LEFT = 2;//左手
    public final static int TOUCH_HAND_RIGHT = 3;//右手
    public final static int TOUCH_BELLY = 4;//肚子、腹部
    public final static int TOUCH_HAND_FOOT = 5;//脚
    public final static int TOUCH_HEAD_BACK = 6;//后脑勺
    public final static int TOUCH_EARS_LEFT = 7;//左耳
    public final static int TOUCH_EARS_RIGHT = 8;//右耳
    public final static int TOUCH_BACK = 9;//背部

    // 响应触摸机器人的处理
    public static void responseTouch(Context context, String touchKey) {
        if (!TextUtils.isEmpty(touchKey)) {
            if (TextUtils.isDigitsOnly(touchKey)) {
                int key = Integer.parseInt(touchKey);
                String content = "";
                switch (key) {
                    case TOUCH_HEAD_TOP://头顶
                        Log.i("netty", "头");
                        content = "你好，我叫小雪，可以叫小黄小黄，和我交流";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);

                        break;
                    case TOUCH_EYE://眼睛
                        Log.i("netty", "眼睛key===" + EmotionEnum.EMOTION_BLINK_TWO.getEmotionKey());
                        BroadcastEnclosure.controlRobotEmotion(context, EmotionEnum.EMOTION_BLINK_TWO.getEmotionKey());
                        content = "看我眼睛大，你是不是很羡慕呢，嘿嘿";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);

                        break;
                    case TOUCH_HAND_LEFT://左手
                        Log.i("netty", "左手");
                        waving(context, ScriptConfig.HAND_LEFT);

                        break;
                    case TOUCH_HAND_RIGHT://右手
                        Log.i("netty", "右手");
                        waving(context, ScriptConfig.HAND_RIGHT);

                        break;
                    case TOUCH_BELLY://肚子、腹部
                        Log.i("netty", "肚子");
                        content = "好痒";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);

                        break;
                    case TOUCH_HAND_FOOT://脚
                        Log.i("netty", "脚");
                        content = "好久没人给我挠痒痒了呢，嘿嘿";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);

                        break;
                    case TOUCH_HEAD_BACK://后脑勺
                        Log.i("netty", "后脑勺");
                        content = "不要挠我”，抬头";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);
                        // 抬头10度即+10
                        headTurn(context, DataConfig.TURN_HEAD_AROUND, "10");

                        break;
                    case TOUCH_EARS_LEFT://左耳
                        Log.i("netty", "左耳");
                        content = "嘻嘻";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);
                        // 头向右转, 向右10度即-10
                        headTurn(context, DataConfig.TURN_HEAD_ABOUT, "-10");

                        break;
                    case TOUCH_EARS_RIGHT://右耳
                        Log.i("netty", "右耳");
                        content = "嘻嘻";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);
                        // 头向左转, 向左10度即+10
                        headTurn(context, DataConfig.TURN_HEAD_ABOUT, "10");

                        break;
                    case TOUCH_BACK://背部
                        Log.i("netty", "背部");
                        content = "好舒服";
                        speakContent(DataConfig.SPEAK_TYPE_CHAT, content);

                        break;
                    default:
                        break;

                }
            }
        }
    }

    // 说内容
    private static void speakContent(int speakType, String content) {
        SpeechImpl.getInstance().startSpeak(speakType, content);
    }

    // 摆手
    private static void waving(final Context context, final String handCategory) {
        BroadcastEnclosure.controlArm(context, handCategory, "25", 1500);
        // 1.5秒后把手放下来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BroadcastEnclosure.controlArm(context, handCategory, "0", 1500);
            }
        }, 1500);
        SpeechImpl.getInstance().startListen();
    }

    // 转头   头向左转, 向左10度即+10,头向右转, 向右10度即-10
    private static void headTurn(final Context context, final int headDirection, final String digit) {
        BroadcastEnclosure.controlHead(context, headDirection, digit, 1000);
        // 1.5s 头归位
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BroadcastEnclosure.controlHead(context, headDirection, "0", 1000);
            }
        }, 1500);
    }
}
