package com.robot.et.core.software.voice.turing;

import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.voice.SpeechService;
import com.robot.et.core.software.common.speech.voice.turing.ITuring;
import com.robot.et.core.software.common.speech.voice.turing.Turing;

import turing.os.http.core.ErrorMessage;

// 图灵文本理解
public class TuRingService extends SpeechService implements ITuring {
    private Turing turing;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("turing", "TuRingService  onCreate()");
        SpeechImpl.setService(this);
        // 初始化对象
        turing = new Turing(this, this, DataConfig.TURING_SECRET, DataConfig.TURING_APPID, DataConfig.TURING_UNIQUEID);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 继承父类方法
     * 理解文本内容（外部调用）
     *
     * @param content 要理解的内容
     */
    @Override
    public void understanderTextByTuring(String content) {
        super.understanderTextByTuring(content);
        if (!TextUtils.isEmpty(content)) {
            boolean isSuccess = turing.understandText(content);
            if (!isSuccess) {
                SpeechImpl.getInstance().startListen();
            }
        } else {
            SpeechImpl.getInstance().startListen();
        }
    }

    /**
     * 实现ITuring接口方法
     * 理解成功（调用sdk内部方法）
     *
     * @param result 返回理解的结果
     */
    @Override
    public void onResult(String result) {
        Log.i("turing", "图灵result====" + result);
        if (!TextUtils.isEmpty(result)) {
            // 对天气的结果特殊处理
            if (result.contains(":") && result.contains("周") && result.contains("风") && result.contains(";")) {
                Log.i("turing", "从科大讯飞没有获取到天气问图灵");
                result = getWeatherContent(result);
            }
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, result);
        } else {
            SpeechImpl.getInstance().startListen();
        }
    }

    /**
     * 实现ITuring接口方法
     * 理解出现异常（调用sdk内部方法）
     *
     * @param errorMessage 返回理解的错误信息
     */
    @Override
    public void onError(ErrorMessage errorMessage) {
        Log.i("turing", "图灵errorMessage.getMessage()====" + errorMessage.getMessage());
        SpeechImpl.getInstance().startListen();
    }

    /*
       * 对图灵返回的天气进行处理
       * content weather格式 上海:05/16 周一,15-24° 23° 晴 北风微风; 05/17 周二,16-26° 晴 东南风微风;
       * 05/18 周三,17-26° 多云 东风微风; 05/19 周四,19-26° 多云 东风微风;
       */
    private String getWeatherContent(String content) {
        String result = "";
        if (!TextUtils.isEmpty(content)) {
            String[] datas = content.split(";");
            result = datas[0];
            String[] tempDatas = result.split(",");
            result = tempDatas[1];
            String cityData = tempDatas[0];
            String[] citys = cityData.split("\\:");
            String city = citys[0];

            String[] weathers = result.split(" ");
            // weathers[0]15-24° weathers[1]23° weathers[2]晴 weathers[3]北风微风
            if (weathers != null && weathers.length > 0) {
                StringBuffer buffer = new StringBuffer(1024);
                buffer.append(city).append("市").append("天气：").append(weathers[2]).append(",气温：").append(weathers[0]).append(",风力：").append(weathers[3]);
                result = buffer.toString();
            }
        }
        return result;
    }
}
