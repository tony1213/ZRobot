package com.robot.et.main;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.mapapi.SDKInitializer;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.push.ali.ALiPush;
import com.squareup.leakcanary.LeakCanary;

public class CustomApplication extends Application {

    private static CustomApplication instance;

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
        initCloudChannel(this);
        // 初始化百度地图
        initBaiDuMap();
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
     *
     * @param applicationContext
     */
    private void initCloudChannel(final Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        pushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("alipush", "init cloudchannel success");
                new ALiPush(applicationContext);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e("alipush", "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    // 初始化百度地图
    private void initBaiDuMap() {
        // 初始化sdk，上下文必须要是application的，最好放在这里初始化sdk
        SDKInitializer.initialize(this);
    }
}
