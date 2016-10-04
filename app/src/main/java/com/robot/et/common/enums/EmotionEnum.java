package com.robot.et.common.enums;

import com.robot.et.R;

//控制表情的枚举
public enum EmotionEnum {

    EMOTION_SMILE(R.drawable.emotion_smile, "微笑", "嘻嘻"),
    EMOTION_SMILE2(R.drawable.emotion_smile, "笑一个", "嘻嘻"),
    EMOTION_SMILE3(R.drawable.emotion_smile, "笑一笑", "嘻嘻"),
    EMOTION_SMILE4(R.drawable.emotion_smile, "高兴", "哈哈"),
    EMOTION_SMILE5(R.drawable.emotion_smile, "傻笑", "哈哈"),
    EMOTION_SMILE6(R.drawable.emotion_smile, "兴奋", "哈哈"),

    EMOTION_ANGER(R.drawable.emotion_sad, "愤怒", "啊"),
    EMOTION_ANGER2(R.drawable.emotion_sad, "怒一个", "啊"),

    EMOTION_SAD(R.drawable.emotion_sad, "悲伤", "呜呜呜"),
    EMOTION_SAD2(R.drawable.emotion_sad, "哀愁", "欸"),
    EMOTION_SAD3(R.drawable.emotion_sad, "哀伤", "欸"),
    EMOTION_SAD4(R.drawable.emotion_sad, "忧愁", "欸"),
    EMOTION_SAD5(R.drawable.emotion_cry, "伤心", "呜呜呜"),
    EMOTION_SAD6(R.drawable.emotion_cry, "哭泣", "呜呜呜"),

    EMOTION_HAPPY(R.drawable.emotion_smile, "快乐", "嘿嘿"),
    EMOTION_HAPPY2(R.drawable.emotion_smile, "乐一个", "嘿嘿"),
    EMOTION_LOVELY(R.drawable.emotion_smile, "可爱", "嘻嘻"),

    EMOTION_TEAR(R.drawable.emotion_cry, "流泪", ""),

    EMOTION_SLEEP(R.drawable.emotion_rest, "睡觉", ""),
    EMOTION_SLEEP2(R.drawable.emotion_rest, "睡觉表情", ""),

    EMOTION_SURPRISED(R.drawable.emotion_sad, "惊讶", ""),

    EMOTION_BLINK_LEFT(R.drawable.emotion_blink, "眨左眼", ""),
    EMOTION_BLINK_RIGHT(R.drawable.emotion_blink, "眨右眼", ""),
    EMOTION_BLINK_TWO(R.drawable.emotion_blink, "眨眼", ""),
    EMOTION_BLINK_TWO2(R.drawable.emotion_blink, "眨双眼", ""),

    EMOTION_SWEAT(R.drawable.emotion_cry, "流汗", ""),

    EMOTION_EYES_WHITE(R.drawable.emotion_blink, "白眼", ""),

    EMOTION_DOUBT(R.drawable.emotion_blink, "疑问", "");

    // 表情的资源ID，当要显示表情时，所需要的值
    private int emotionKey;
    // 表情的名字
    private String emotionName;
    // 当前表情需要回答的内容（当指令控制表情时使用）
    private String requireAnswer;

    EmotionEnum(int emotionKey, String emotionName, String requireAnswer) {
        this.emotionKey = emotionKey;
        this.emotionName = emotionName;
        this.requireAnswer = requireAnswer;
    }

    public int getEmotionKey() {
        return emotionKey;
    }

    public String getEmotionName() {
        return emotionName;
    }

    public String getRequireAnswer() {
        return requireAnswer;
    }

}
