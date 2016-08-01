package com.robot.et.common;

public class DataConfig {
	//科大讯飞的appid
	public final static String SPEECH_APPID = "570e1085";
	//默认发音人
	public final static String DEFAULT_SPEAK_MEN = "nannan";

	//图灵的appid
	public static final String TURING_APPID = "8314e713b83b80dbe26264214907bce1";
	//图灵的secret
	public static final String TURING_SECRET = "b4e5061c950ea99a";
	//图灵的UNIQUEID   填写一个任意的标示，没有具体要求，但一定要写
	public static final String TURING_UNIQUEID = "131313131";


	//说话类型
	//对话
	public final static int SPEAK_TYPE_CHAT = 1;
	//开始播放音乐前的提示
	public final static int SPEAK_TYPE_MUSIC_START = 2;
	//什么都不做
	public final static int SPEAK_TYPE_DO_NOTHINF = 3;
	//打开脸部识别
	public final static int SPEAK_TYPE_FACE_DETECTOR = 4;
	//闹铃提醒
	public static final int SPEAK_TYPE_REMIND_TIPS = 5;


	//歌曲信息连接符
	public static String MUSIC_SPLITE = "&";
	// 提醒内容中间的分隔符
	public static String SCHEDULE_SPLITE = ",";
	//音乐文件不存在了
	public static String MUSIC_NOT_EXIT = "抱歉，主人，音乐文件已经不存在了，再换一首别的歌吧";

	//是否是脸部识别
	public static boolean isFaceDetector = false;

	//机器人问答
	public static final int LEARN_BY_ROBOT = 0;
	//个人问答
	public static final int LEARN_BY_PERSON = 1;

	//提醒广播的标志
	public static String ACTION_REMIND_SIGN = "&ALARM&";

	//已提醒
	public static final int REMIND_HAD_ID = 1;
	//未提醒
	public static final int REMIND_NO_ID = 0;

	//设置为每天的闹铃
	public static int alarmAllDay = 0;

}
