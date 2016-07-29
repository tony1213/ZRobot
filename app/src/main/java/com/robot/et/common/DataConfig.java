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
	//开始播放音乐前的提示
	public final static int SPEAK_TYPE_DO_NOTHINF = 3;


	//歌曲信息连接符
	public static String MUSIC_SPLITE = "&";
	// 提醒内容中间的分隔符
	public static String SCHEDULE_SPLITE = ",";
	//音乐文件不存在了
	public static String MUSIC_NOT_EXIT = "抱歉，主人，音乐文件已经不存在了，再换一首别的歌吧";

}
