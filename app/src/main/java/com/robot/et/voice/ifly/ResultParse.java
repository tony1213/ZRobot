package com.robot.et.voice.ifly;

import android.util.Log;

import com.iflytek.cloud.RecognizerResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;

/**
 * Created by houdeming on 2016/7/25.
 * 科大讯飞结果json解析
 */
public class ResultParse {
    private static final String TAG = "json";

    // 科大讯飞语音听写的结果json解析
    public static String printResult(RecognizerResult results, HashMap<String, String> mIatResults) {
        String text = parseVoiceToTextResult(results.getResultString());
        String sn = "";
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            Log.i(TAG, "printResult  JSONException");
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String result = resultBuffer.toString();
        return result;
    }

    // 科大讯飞语音听写json解析
    private static String parseVoiceToTextResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            Log.i(TAG, "parseVoiceToTextResult  Exception");
        }
        return ret.toString();
    }
}
