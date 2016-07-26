package com.robot.et.service.software.iflytek;

import org.json.JSONObject;

/**
 * Created by houdeming on 2016/7/25.
 * 解析科大讯飞获取当前的service
 */
public interface ParseIflyService {
    //解析结果回调
    public void getResult (String question, String service, JSONObject jObject);
    //解析异常
    public void onError (String errorMsg);

}
