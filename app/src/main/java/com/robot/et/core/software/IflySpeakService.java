package com.robot.et.core.software;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.robot.et.core.software.util.IflyUtils;

public class IflySpeakService extends Service {
	// 语音合成对象
	private SpeechSynthesizer mTts;
	private int currentType;
	private String city,area;
	private String alarmContent;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}


	private void speak(int speakType,String content,boolean isTypeCloud) {
		IflyUtils.setTextToVoiceParam(mTts,"nannan","60","50","50");

		int code = mTts.startSpeaking(content, mTtsListener);
		// * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
		// * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
		// String path =
		// Environment.getExternalStorageDirectory()+"/tts.pcm";
		// int code = mTts.synthesizeToUri(text, path, mTtsListener);

		if (code != ErrorCode.SUCCESS) {
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
				// 未安装则跳转到提示安装页面
			} else {

			}
		}
	}
	
	// 初始化监听
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				// 初始化失败,错误码
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}
		}
	};
	
	// 合成回调监听
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		// 开始播放
		@Override
		public void onSpeakBegin() {

		}
		// 暂停播放
		@Override
		public void onSpeakPaused() {
		}
		// 继续播放
		@Override
		public void onSpeakResumed() {
		}
		// 合成进度
		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
		}
		// 播放进度
		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		}
		@Override
		public void onCompleted(SpeechError error) {

		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mTts.isSpeaking()){
			mTts.stopSpeaking();
		}
		// 退出时释放连接
		mTts.destroy();
	}

}
