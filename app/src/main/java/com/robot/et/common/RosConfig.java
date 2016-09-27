package com.robot.et.common;

/**
 * Created by houdeming on 2016/9/22.
 */
public class RosConfig {
    // 进入物体识别
    public static final String START_DISTINGUISH = "Rai Learning";
    // 关闭视觉学习
    public static final String CLOSE_DISTINGUISH = "DeepLearnClose";
    // 初始化视觉
    public static final String INIT_VISION = "DeepLearnInit";
    // 退出
    public static final String CLOSE = "Stop";
    // 学习物体（这是什么）
    public static final String LEARN_OBJECT_WHAT = "DeepLearnRec";
    // 学习物体（这是手机）
    public static final String LEARN_OBJECT_KNOWN = "DeepLearn";
    //开启人体检测
    public static final String OPEN_VISUAL_BODY_TRK = "OpenBodyTRK";
    //识别视觉人体位置
    public static final String VISUAL_BODY_TRK = "BodyTRK";
    //关闭人体检测
    public static final String CLOSE_VISUAL_BODY_TRK = "CloseBodyTRK";
    // 创建地图
    public static final String CREATE_MAP = "MakeAMap";
    // 保存地图
    public static final String SAVE_MAP = "SaveAMap";
    // 加载地图
    public static final String LOAD_MAP = "World Navigation";
    // 忘记学习内容
    public static final String FORGET_LEARN_CONTENT = "DeleteAllVisual";
    // 漫游,随便走走
    public static final String RANDOM_MOVE = "Roaming";
    // 跟随
    public static final String FOLLOW = "Follower";
    // 这是是厨房（位置）
    public static final String POSITION = "PositionName";
    // 导航
    public static final String NAVIGATION = "DestinationName";
    // 转圈
    public static final String TURN = "Turn";
}
