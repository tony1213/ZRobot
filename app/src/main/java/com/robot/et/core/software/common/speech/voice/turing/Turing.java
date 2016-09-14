package com.robot.et.core.software.common.speech.voice.turing;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.turing.androidsdk.InitListener;
import com.turing.androidsdk.SDKInit;
import com.turing.androidsdk.SDKInitBuilder;
import com.turing.androidsdk.TuringApiManager;

import org.json.JSONException;
import org.json.JSONObject;

import turing.os.http.core.ErrorMessage;
import turing.os.http.core.HttpConnectionListener;
import turing.os.http.core.RequestResult;

/**
 * Created by houdeming on 2016/9/3.
 * 图灵文本理解的二次封装
 */
public class Turing {
    private TuringApiManager mTuringApiManager;
    private Context context;
    private final String TAG = "turing";
    private ITuring iTuring;

    public Turing(Context context, ITuring iTuring, String secret, String appId, String uniqueId) {
        this.context = context;
        this.iTuring = iTuring;
        initTuringSDK(secret, appId, uniqueId);
    }

    // turingSDK初始化
    private void initTuringSDK(final String secret, final String appId, final String uniqueId) {
        SDKInitBuilder builder = new SDKInitBuilder(context).setSecret(secret)
                .setTuringKey(appId).setUniqueId(uniqueId);

        SDKInit.init(builder, new InitListener() {
            @Override
            public void onFail(String error) {
                Log.i(TAG, "图灵error===" + error);
                //异常处理 异常后重新去初始化
                initTuringSDK(secret, appId, uniqueId);
            }

            @Override
            public void onComplete() {
                // 获取userid成功后，才可以请求Turing服务器，需要请求必须在此回调成功，才可正确请求
                mTuringApiManager = new TuringApiManager(context);
                mTuringApiManager.setHttpListener(myHttpConnectionListener);
            }
        });
    }

    // 理解文本内容
    public boolean understandText(String content) {
        if (!TextUtils.isEmpty(content)) {
            //发出请求  以防mTuringApiManager为null
            if (mTuringApiManager != null) {
                mTuringApiManager.requestTuringAPI(content);
                return true;
            }
        }
        return false;
    }

    // 图灵文本理解监听器
    private HttpConnectionListener myHttpConnectionListener = new HttpConnectionListener() {

        @Override
        public void onSuccess(RequestResult result) {// 理解成功
            String content = "";
            if (result != null) {
                try {
                    JSONObject result_obj = new JSONObject(result.getContent().toString());
                    if (result_obj.has("text")) {
                        content = (String) result_obj.get("text");
                        Log.i(TAG, "图灵content====" + content);
                    }
                } catch (JSONException e) {
                    Log.i(TAG, "图灵JSONException====" + e.getMessage());
                }
            }
            iTuring.onResult(content);
        }

        @Override
        public void onError(ErrorMessage errorMessage) {// 理解失败
            Log.i(TAG, "图灵errorMessage.getMessage()====" + errorMessage.getMessage());
            iTuring.onError(errorMessage);
        }
    };
}
