package com.robot.et.common.enums;

// 科大讯飞文本理解场景
public enum SceneServiceEnum {

    BAIKE("baike", "百科"), CALC("calc", "计算器"), COOKBOOK("cookbook", "菜谱"), DATETIME("datetime", "日期"),
    FAQ("faq", "社区问答"), FLIGHT("flight", "航班查询"), HOTEL("hotel", "酒店查询"), MAP("map", "地图查询"),
    MUSIC("music", "音乐"), RESTAURANT("restaurant", "餐馆"), SCHEDULE("schedule", "提醒"), STOCK("stock", "股票查询"),
    TRAIN("train", "火车查询"), TRANSLATION("translation", " 翻译"), WEATHER("weather", "天气查询"), OPENQA("openQA", "褒贬&问候&情绪"),
    TELEPHONE("telephone", "打电话"), MESSAGE("message", "发短信"), CHAT("chat", "闲聊"), PM25("pm25", "空气质量");

    // 对话场景的service值
    private String serviceKey;
    // 对话场景的名字
    private String serviceName;

    SceneServiceEnum(String serviceKey, String serviceName) {
        this.serviceKey = serviceKey;
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceKey() {
        return serviceKey;
    }

}
