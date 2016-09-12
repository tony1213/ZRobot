package com.robot.et.service.software.iflytek;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.EnumManager;
import com.robot.et.common.enums.SceneServiceEnum;
import com.robot.et.service.software.TextUnderstander;
import com.robot.et.service.software.iflytek.util.ResultParse;
import com.robot.et.service.software.impl.SpeechlHandle;
import com.robot.et.service.software.system.music.PlayerControl;

import org.json.JSONObject;

//科大讯飞文本理解
public class IflyTextUnderstanderService extends Service implements TextUnderstander {

	private com.iflytek.cloud.TextUnderstander mTextUnderstander;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("ifly", "IflyTextUnderstanderService onCreate()");
		mTextUnderstander = com.iflytek.cloud.TextUnderstander.createTextUnderstander(this,textUnderstanderListener);
		SpeechlHandle.setTextUnderstander(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}


	//文本理解
	private void textUnderstander(String content) {
		if (mTextUnderstander.isUnderstanding()) {
			Log.i("ifly", "文本理取消");
			mTextUnderstander.cancel();
		}
		// 函数调用返回值
		int ret = mTextUnderstander.understandText(content, textListener);
		if (ret != 0) {
			Log.i("ifly", "文本理解错误码ret==" + ret);
			SpeechlHandle.startListen();
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
				if (!TextUtils.isEmpty(text)) {
					resultHandle(text);
				} else {
					SpeechlHandle.startListen();
				}
			} else {
				Log.i("ifly", "文本理解不正确");
				SpeechlHandle.startListen();
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

	private void speakContent (String question, String answer) {
		Log.i("ifly", "IflyTextUnderstanderService question===" + question);
		Log.i("ifly", "IflyTextUnderstanderService answer===" + answer);

		if (!TextUtils.isEmpty(answer)) {
			SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, answer);
		} else {
			SpeechlHandle.turingUnderstander(question);
		}

	}

	private void resultHandle (String result) {
		ResultParse.parseAnswerResult(result, new ParseResultCallBack() {
			@Override
			public void getResult(String question, String service, JSONObject jObject) {
				if (!TextUtils.isEmpty(question)) {
					if (!TextUtils.isEmpty(service)) {
						SceneServiceEnum serviceEnum = EnumManager.getIflyScene(service);
						Log.i("ifly", "IflyTextUnderstanderService serviceEnum===" + serviceEnum);
						if (serviceEnum != null) {
							String answer = "";
							switch (serviceEnum) {
								case BAIKE://百科
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case CALC://计算器
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case COOKBOOK://菜谱
									answer = ResultParse.getCookBookData(jObject);
									speakContent(question, answer);

									break;
								case DATETIME://日期
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case FAQ://社区问答
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case FLIGHT://航班查询
									// do nothing
									speakContent(question, answer);

									break;
								case HOTEL://酒店查询
									// do nothing
									speakContent(question, answer);

									break;
								case MAP://地图查询
									// do nothing
									speakContent(question, answer);

									break;
								case MUSIC://音乐
									answer = ResultParse.getMusicData(jObject, DataConfig.MUSIC_SPLITE);
									String content = PlayerControl.getMusicSpeakContent(answer, DataConfig.MUSIC_SPLITE);
									SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_MUSIC_START, content);

									break;
								case RESTAURANT://餐馆
									// do nothing
									speakContent(question, answer);

									break;
								case SCHEDULE://提醒
									answer = ResultParse.getRemindData(jObject, DataConfig.SCHEDULE_SPLITE);
									speakContent(question, answer);

									break;
								case STOCK://股票查询
									// do nothing
									speakContent(question, answer);

									break;
								case TRAIN://火车查询
									// do nothing
									speakContent(question, answer);

									break;
								case TRANSLATION://翻译
									// do nothing
									speakContent(question, answer);

									break;
								case WEATHER://天气查询
									answer = ResultParse.getWeatherData(jObject, "上海市", "浦东新区");
									if (!TextUtils.isEmpty(answer)) {
										if (answer.contains("空气质量")) {
											speakContent(question, answer);

										} else {
											String weatherContent = answer + "上海市" + "的天气";
											textUnderstander(weatherContent);
										}
									} else {
										speakContent(question, answer);
									}

									break;
								case OPENQA://褒贬&问候&情绪
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case TELEPHONE://打电话
									answer = ResultParse.getPhoneData(jObject);
									speakContent(question, answer);

									break;
								case MESSAGE://发短信
									// do nothing
									speakContent(question, answer);

									break;
								case CHAT://闲聊
									answer = ResultParse.getAnswerData(jObject);
									speakContent(question, answer);

									break;
								case PM25://空气质量
									answer = ResultParse.getPm25Data(jObject, "上海市", "浦东新区");
									speakContent(question, answer);

									break;

								default:
									speakContent(question, answer);
									break;
							}

						} else {
							speakContent(question, "");
						}

					} else {
						speakContent(question, "");
					}
				} else {
					SpeechlHandle.startListen();
				}
			}

			@Override
			public void onError(String errorMsg) {
				SpeechlHandle.startListen();
			}
		});

	}

	@Override
	public void understanderText(String content) {
		if (!TextUtils.isEmpty(content)) {
			textUnderstander(content);
		} else {
			SpeechlHandle.startListen();
		}
	}

}
