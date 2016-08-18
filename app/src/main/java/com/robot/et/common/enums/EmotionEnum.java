package com.robot.et.common.enums;

import com.robot.et.R;

//控制表情的指令
public enum EmotionEnum {

	EMOTION_NORMAL(R.mipmap.emotion_normal, "正常", ""),

	EMOTION_SMILE(R.drawable.emotion_smile, "微笑", "嘻嘻"),
	EMOTION_SMILE2(R.drawable.emotion_smile, "笑一个", "嘻嘻"),
	EMOTION_SMILE3(R.drawable.emotion_smile, "笑一笑", "嘻嘻"),
	EMOTION_SMILE4(R.drawable.emotion_smile, "高兴", "哈哈"),
	EMOTION_SMILE5(R.drawable.emotion_smile, "傻笑", "哈哈"),
	EMOTION_SMILE6(R.drawable.emotion_smile, "兴奋", "哈哈"),

	EMOTION_ANGER(R.drawable.emotion_anger, "愤怒", "啊"),
	EMOTION_ANGER2(R.drawable.emotion_anger, "怒一个", "啊"),

	EMOTION_SAD(R.drawable.emotion_sorry, "悲伤", "呜呜呜"),
	EMOTION_SAD2(R.drawable.emotion_sorry, "哀愁", "欸"),
	EMOTION_SAD3(R.drawable.emotion_sorry, "哀伤", "欸"),
	EMOTION_SAD4(R.drawable.emotion_sorry, "忧愁", "欸"),
	EMOTION_SAD5(R.drawable.emotion_sorry, "伤心", "呜呜呜"),
	EMOTION_SAD6(R.drawable.emotion_sorry, "哭泣", "呜呜呜"),

	EMOTION_HAPPY(R.drawable.emotion_smile, "快乐", "嘿嘿"),
	EMOTION_HAPPY2(R.drawable.emotion_smile, "乐一个", "嘿嘿"),
	EMOTION_LOVELY(R.drawable.emotion_smile, "可爱", "嘻嘻"),

	EMOTION_LOOK_LEFT(R.drawable.emotion_look_left, "左看", ""),
	EMOTION_LOOK_RIGHT(R.drawable.emotion_look_right, "右看", ""),

	EMOTION_TEAR(R.drawable.emotion_sorry, "流泪", ""),

	EMOTION_SLEEP(R.drawable.emotion_dead_eye, "睡觉", ""),
	EMOTION_SLEEP2(R.drawable.emotion_dead_eye, "睡觉表情", ""),

	EMOTION_SURPRISED(R.drawable.emotion_dead_eye, "惊讶", ""),

	EMOTION_BLINK_LEFT(R.drawable.emotion_blink, "眨左眼", ""),
	EMOTION_BLINK_RIGHT(R.drawable.emotion_blink, "眨右眼", ""),
	EMOTION_BLINK_TWO(R.drawable.emotion_blink, "眨眼", ""),
	EMOTION_BLINK_TWO2(R.drawable.emotion_blink, "眨双眼", ""),

	EMOTION_SWEAT(R.drawable.emotion_dead_eye, "流汗", ""),

	EMOTION_EYES_WHITE(R.drawable.emotion_dead_eye, "白眼", ""),

	EMOTION_DOUBT(R.drawable.emotion_turn, "疑问", "");

	private int emotionKey;//表情的资源ID
	private String emotionName;//表情的名字
	private String requireAnswer;//需要回答的内容

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
