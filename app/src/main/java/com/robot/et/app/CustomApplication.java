package com.robot.et.app;

import android.app.Application;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.mapapi.SDKInitializer;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.video.agora.BaseEngineEventHandlerActivity;
import com.robot.et.core.software.video.agora.MessageHandler;
import com.squareup.leakcanary.LeakCanary;

import io.agora.rtc.RtcEngine;

public class CustomApplication extends Application {

    private static CustomApplication instance;
    private RtcEngine rtcEngine;
    private MessageHandler messageHandler;
    public static CustomApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 初始化监听内存泄漏
        LeakCanary.install(this);
        // 初始化科大讯飞
        initVoice();
        // 初始化云推送
        initCloudChannel();
        // 初始化百度地图
        initBaiDuMap();
        // 初始化agora视频
        initAgora();
    }

    // 初始化科大讯飞
    private void initVoice() {
        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        StringBuffer param = new StringBuffer();
        param.append("appid=" + DataConfig.SPEECH_APPID);
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(this, param.toString());
    }

    /**
     * 初始化阿里云推送通道
     * 一定要放在这里初始化云推送，不然清掉apk再打开，接受不到推送的消息
     */
    private void initCloudChannel() {
        PushServiceFactory.init(this);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(this, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("alipush", "init cloudchannel success");
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.i("alipush", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    // 初始化百度地图
    private void initBaiDuMap() {
        // 初始化sdk，上下文必须要是application的，最好放在这里初始化sdk
        SDKInitializer.initialize(this);
    }

    // 初始化视频（注释：agora视频一定要在这里初始化，否则多次打开视频的时候不能保证拿到对方视频流）
    private void initAgora() {
        messageHandler = new MessageHandler();
    }

    public void setRtcEngine(String vendorKey){
        if(rtcEngine==null) {
            rtcEngine = RtcEngine.create(getApplicationContext(), vendorKey, messageHandler);
        }
    }

    public RtcEngine getRtcEngine(){
        return rtcEngine;
    }

    public void setEngineEventHandlerActivity(BaseEngineEventHandlerActivity engineEventHandlerActivity){
        messageHandler.setActivity(engineEventHandlerActivity);
    }
}
