package com.robot.et.core.software.iflytek;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.iflytek.util.IflyUtils;
import com.robot.et.core.software.iflytek.util.ResultParse;
import com.robot.et.impl.SpeechRecognizerImpl;
import com.robot.et.util.SpeechImplHandle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class IflyVoiceToTextService extends Service implements SpeechRecognizerImpl {
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	private int ret = 0; // 函数调用返回值

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("ifly", "IflyVoiceToTextService  onCreate()");
		// 初始化SpeechRecognizer对象
		mIat = SpeechRecognizer.createRecognizer(this, mTtsInitListener);
		SpeechImplHandle.setSpeechRecognizer(this);

		uploadUserThesaurus();//上传词表

		beginListen();
	}

	private void beginListen() {
		listen(DataConfig.DEFAULT_SPEAK_MEN);
	}
	
	private void listen (String language) {
		mIatResults.clear();
		// 设置参数
		IflyUtils.setVoiceToTextParam(mIat, language);
		// 不显示听写对话框
		ret = mIat.startListening(mRecognizerListener);

		if (ret != ErrorCode.SUCCESS) {
			Log.i("ifly", "听写失败,错误码：" + ret);
		} else {
			Log.i("ifly", "开始听写");
		}
	}
	
	 //听写监听器
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			Log.i("ifly", "onBeginOfSpeech()" );
		}

		@Override
		public void onError(SpeechError error) {
			Log.i("ifly", "onError " );
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			stopListen();
			beginListen();
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			Log.i("ifly", "结束说话 " );
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.i("ifly", "onResult " );

			// 有人说话 
			String result = ResultParse.printResult(results,mIatResults);
			Log.i("ifly", "问题原版result====" + result);
			Toast.makeText(IflyVoiceToTextService.this, "问题原版result====" + result, Toast.LENGTH_SHORT).show();
			if(isLast){
				Log.i("ifly", "onResult  isLast" );
				stopListen();
				if (!TextUtils.isEmpty(result)) {
					SpeechImplHandle.startSpeak(DataConfig.SPEAK_TYPE_WELCOME, result);

				} else {
					beginListen();
				}
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			Log.i("ifly", "当前正在说话，音量大小： volume==" + volume );
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			Log.i("ifly", "onEvent" );
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}
	};

	//初始化监听
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
        		//初始化失败,错误码
				Log.i("ifly", "初始化失败" );
        	} else {
				// 初始化成功，之后可以调用startSpeaking方法
        		// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
        		// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}		
		}
	};
	
	//上传词表监听器
	private LexiconListener mLexiconListener = new LexiconListener() {

		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if (error != null) {
				Log.i("ifly", "上传联系人词表error===" + error.toString() );
				uploadUserThesaurus();
			} else {
				Log.i("ifly", "上传联系人词表成功" );
			}
		}
	};
	
	//上传词表
	private void uploadUserThesaurus(){
		String contents = readFile("userwords","utf-8");
		// 指定引擎类型
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 置编码类型
		mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		ret = mIat.updateLexicon("userword", contents, mLexiconListener);
		if(ret != ErrorCode.SUCCESS){
			Log.i("voiceresult", "上传热词失败,错误码==" + ret );
			uploadUserThesaurus();
		}
	}

	/**
	 * 读取asset目录下文件。
	 * @return content
	 */
	public String readFile(String file, String code) {
		int len = 0;
		byte[] buf = null;
		String result = "";
		try {
			InputStream in = getResources().getAssets().open(file);
			len = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			result = new String(buf, code);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopListen();
		mIat.destroy();
	}

	private void stopListen () {
		if(mIat.isListening()){
			mIat.cancel();
		}
	}

	@Override
	public void startListen() {
		beginListen();
	}

	@Override
	public void cancelListen() {
		stopListen();
	}

}
