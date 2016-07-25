package com.robot.et.core.software.iflytek;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;

//科大讯飞文本理解
public class IflyTextUnderstanderService extends Service {

	private TextUnderstander mTextUnderstander;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("ifly", "IflyTextUnderstanderService onCreate()");
		mTextUnderstander = TextUnderstander.createTextUnderstander(this,textUnderstanderListener);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}


	//文本理解
	private void textUnderstander(String content) {
		if (mTextUnderstander.isUnderstanding()) {
			mTextUnderstander.cancel();
			Log.i("ifly", "文本理取消");
		}
		// 函数调用返回值
		int ret = mTextUnderstander.understandText(content, textListener);
		if (ret != 0) {
			Log.i("ifly", "文本理解错误码ret==" + ret);
		}
	}

	private InitListener textUnderstanderListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				Log.i("ifly", "文本理解初始化失败,错误码code==" + code);
			}
		}
	};

	private TextUnderstanderListener textListener = new TextUnderstanderListener() {

		@Override
		public void onResult(UnderstanderResult result) {
			Log.i("ifly", "文本理解onResult");
			Message message = handler.obtainMessage();
			message.obj = result;
			handler.sendMessage(message);
		}

		@Override
		public void onError(SpeechError error) {
			// 文本语义不能使用回调错误码14002，请确认您下载sdk时是否勾选语义场景和私有语义的发布
			Log.i("ifly", "文本理解onError Code==" + error.getErrorCode());
		}
	};

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			UnderstanderResult result = (UnderstanderResult) msg.obj;
			Log.i("ifly", "文本理解onResult  result===" + result);
			if (null != result) {
				String text = result.getResultString();
				Log.i("ifly", "文本理解text===" + text);
			} else {
				Log.i("ifly", "文本理解不正确");
			}
		};
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTextUnderstander.isUnderstanding()) {
			mTextUnderstander.cancel();
		}
		mTextUnderstander.destroy();
	}

}
