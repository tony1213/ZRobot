package com.robot.et.core.software.common.push.ali;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.robot.et.common.UrlConfig;
import com.robot.et.core.software.common.network.HttpManager;
import com.robot.et.core.software.common.network.RobotInfoCallBack;
import com.robot.et.entity.RobotInfo;
import com.robot.et.util.DeviceUuidFactory;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;

/**
 * Created by houdeming on 2016/9/16.
 * 阿里推送
 */
public class ALiPush implements RobotInfoCallBack {
    private static final String TAG = "alipush";
    private String deviceId;
    private SharedPreferencesUtils share;
    private String robotNum;

    public ALiPush(Context context) {
        // 获取机器编号
        share = SharedPreferencesUtils.getInstance();
        robotNum = share.getString(SharedPreferencesKeys.ROBOT_NUM, "");
        if (TextUtils.isEmpty(robotNum)) {
            // 获取机器设备码
            deviceId = new DeviceUuidFactory(context).getDeviceUuid();
            Log.i(TAG, "deviceId===" + deviceId);
            // 获取机器信息
            HttpManager.getRobotInfo(UrlConfig.GET_ROBOT_INFO_BY_DEVICEID, deviceId, this);
        }
    }

    // 设置别名
    private void setAlia(String robotNum) {
        Log.i(TAG, "robotNum===" + robotNum);
        PushServiceFactory.getCloudPushService().addAlias(robotNum, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "添加别名成功");
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.i(TAG, "添加别名失败，errorCode: " + errorCode + ", errorMessage：" + errorMessage);
            }
        });
    }

    @Override
    public void onSuccess(RobotInfo info) {
        Log.i(TAG, "NettyService RobotInfoImpl  onSuccess");
        if (info != null) {//当前设备不存在机器编号，第一次获取
            robotNum = info.getRobotNum();
            if (!TextUtils.isEmpty(robotNum)) {
                share.putString(SharedPreferencesKeys.ROBOT_NUM, robotNum);
                share.commitValue();
                setAlia(robotNum);
            }
        } else {//当前设备已经存在机器编号，开始初始化
            HttpManager.getRobotInfo(UrlConfig.GET_ROBOT_INFO_START, deviceId, this);
        }
    }

    @Override
    public void onFail(String errorMsg) {
        Log.i(TAG, "NettyService RobotInfoImpl  onFail");
        HttpManager.getRobotInfo(UrlConfig.GET_ROBOT_INFO_BY_DEVICEID, deviceId, this);
    }
}
