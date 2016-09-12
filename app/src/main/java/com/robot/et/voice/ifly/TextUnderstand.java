package com.robot.et.voice.ifly;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;

/**
 * Created by houdeming on 2016/9/3.
 * 科大讯飞文本理解二次封装
 */
public class TextUnderstand extends Voice {
    private TextUnderstander mTextUnderstander;
    private ITextUnderstand iTextUnderstand;

    public TextUnderstand(Context context, ITextUnderstand iTextUnderstand) {
        this.iTextUnderstand = iTextUnderstand;
        // 初始化文本理解对象
        mTextUnderstander = TextUnderstander.createTextUnderstander(context, initListener);
    }

    // 销毁当前对象
    public void destroy() {
        if (mTextUnderstander != null) {
            cancelUnderstand();
            mTextUnderstander.destroy();
        }
    }

    // 取消文本理解
    private void cancelUnderstand() {
        if (mTextUnderstander != null) {
            if (mTextUnderstander.isUnderstanding()) {
                Log.i(TAG, "文本理解取消");
                mTextUnderstander.cancel();
            }
        }
    }

    // 理解内容
    public boolean understandText(String content) {
        if (mTextUnderstander == null) {
            return false;
        }

        // 每次理解之前一定要把上一次的理解取消，避免造成影响
        cancelUnderstand();
        // 调用sdk提供的理解方法
        int ret = mTextUnderstander.understandText(content, textListener);
        // 理解失败
        if (ret != ErrorCode.SUCCESS) {
            Log.i(TAG, "文本理解错误码ret==" + ret);
            return false;
        }
        return true;
    }

    // 文本理解监听器
    private TextUnderstanderListener textListener = new TextUnderstanderListener() {

        @Override
        public void onResult(UnderstanderResult result) {// 理解成功
            Log.i(TAG, "文本理解onResult");
            Message message = handler.obtainMessage();
            message.obj = result;
            handler.sendMessage(message);
        }

        @Override
        public void onError(SpeechError error) {// 理解失败
            // 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
            Log.i(TAG, "文本理解onError Code==" + error.getErrorCode());
            iTextUnderstand.onError(error);
        }
    };

    // 理解返回的内容处于子线程中，通过handler，把结果放到主线程中处理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            UnderstanderResult result = (UnderstanderResult) msg.obj;
            Log.i(TAG, "文本理解onResult  result===" + result);
            String text = "";
            if (null != result) {
                text = result.getResultString();
                Log.i(TAG, "文本理解text===" + text);
            }
            iTextUnderstand.onResult(text);
        }
    };
}
