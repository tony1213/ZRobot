package com.robot.et.voice.ifly;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by houdeming on 2016/9/3.
 * 科大讯飞语音听写二次封装
 */
public class VoiceDictate extends Voice {
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private IVoiceDictate iVoiceDictate;
    private Context context;

    public VoiceDictate(Context context, IVoiceDictate iVoiceDictate) {
        this.iVoiceDictate = iVoiceDictate;
        this.context = context;
        // 初始化语音听写对象
        mIat = SpeechRecognizer.createRecognizer(context, initListener);
    }

    // 销毁当前类对象
    public void destroy() {
        if (mIat != null) {
            stopListen();
            mIat.destroy();
        }
    }

    /**
     * 开始听
     *
     * @param isFirstSetParam 是否是第一次听
     * @param language        听写的语言
     * @return
     */
    public boolean listen(boolean isFirstSetParam, String language) {
        if (mIat == null) {
            return false;
        }
        // 每次听之前要把上一次听的结果清除掉
        mIatResults.clear();
        // 只在第一次听的时候设置参数
        if (isFirstSetParam) {
            setVoiceToTextParam(language);
        }
        // 调用sdk提供的语音听写方法
        int ret = mIat.startListening(mRecognizerListener);
        // 语音听写返回的值
        if (ret != ErrorCode.SUCCESS) {
            Log.i(TAG, "listen  听写失败 ret===" + ret);
            return false;
        }
        return true;
    }

    // 停止听
    public void stopListen() {
        if (mIat != null) {
            if (mIat.isListening()) {
                mIat.cancel();
            }
        }
    }

    //听写监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.i(TAG, "onBeginOfSpeech()");
            iVoiceDictate.onBeginOfSpeech();
        }

        @Override
        public void onError(SpeechError error) {
            Log.i(TAG, "onError ");
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            iVoiceDictate.onError(error);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.i(TAG, "结束说话 ");
            iVoiceDictate.onEndOfSpeech();
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            // 获取语音听写返回的内容
            Log.i(TAG, "onResult ");
            String result = ResultParse.printResult(results, mIatResults);
            Log.i(TAG, "问题原版result====" + result);
            if (isLast) {
                Log.i(TAG, "onResult  isLast");
                iVoiceDictate.onResult(result);
            }
        }

        // 获取语音听写时说话音量的变化值
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            Log.i(TAG, "当前正在说话，音量大小： volume==" + volume);
            iVoiceDictate.onVolumeChanged(volume, data);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            Log.i(TAG, "onEvent");
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            // if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            // String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            // Log.d(TAG, "session id =" + sid);
            // }
        }
    };

    //上传词表监听器
    private LexiconListener mLexiconListener = new LexiconListener() {

        @Override
        public void onLexiconUpdated(String lexiconId, SpeechError error) {
            if (error != null) {
                Log.i(TAG, "上传联系人词表error===" + error.toString());
            } else {
                Log.i(TAG, "上传联系人词表成功");
            }
        }
    };

    //上传词表
    public boolean uploadUserThesaurus(String thesaurusName, String thesaurusSign) {
        String contents = readFile(thesaurusName, "utf-8");
        // 指定引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 置编码类型
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        int code = mIat.updateLexicon(thesaurusSign, contents, mLexiconListener);
        if (code != ErrorCode.SUCCESS) {
            return false;
        }
        return true;
    }

    /**
     * 科大讯飞语音听写
     *
     * @param language 听的语言
     */
    private void setVoiceToTextParam(String language) {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        if (language.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, language);
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");
    }

    //读取asset目录下文件
    private String readFile(String file, String code) {
        if (null == context) {
            Log.i(TAG, "context is null");
            return "";
        } else {
            AssetManager am = context.getAssets();
            int len = 0;
            byte[] buf = null;
            String result = "";
            try {
                InputStream in = am.open(file);
                len = in.available();
                buf = new byte[len];
                in.read(buf, 0, len);
                result = new String(buf, code);
                in.close();
            } catch (Exception e) {
                Log.e(TAG, "readFile Exception==" + e.getMessage());
            }
            return result;
        }
    }
}
