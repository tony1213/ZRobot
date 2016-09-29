package com.robot.et.core.software.common.receiver.util;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.robot.et.common.DataConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.entity.SerialPortFormatInfo;

/**
 * Created by houdeming on 2016/9/27.
 * 发送到串口数据格式的转化
 */
public class MoveFormat {
    //控制嘴的LED
    public static String controlMouthLED(String LEDState) {
        // 转换为硬件所需要的json格式字符串
        SerialPortFormatInfo info = new SerialPortFormatInfo();
        info.setAct("led");
        if (TextUtils.equals(LEDState, ScriptConfig.LED_ON)) {
            info.setCmd("on");
        } else if (TextUtils.equals(LEDState, ScriptConfig.LED_OFF)) {
            info.setCmd("off");
        } else if (TextUtils.equals(LEDState, ScriptConfig.LED_BLINK)) {
            info.setCmd("blink");
        }
        String json = JSON.toJSONString(info);
        return json;
    }

    // 控制头转向
    public static String controlHead(int directionValue, int angleValue, int moveTime) {
        SerialPortFormatInfo info = new SerialPortFormatInfo();
        info.setAct("dig");
        if (directionValue == DataConfig.TURN_HEAD_ABOUT) {
            info.setCmd("lrhead");
        } else if (directionValue == DataConfig.TURN_HEAD_AROUND) {
            info.setCmd("udhead");
        }
        info.setAim(angleValue);
        info.setTim(moveTime);
        String json = JSON.toJSONString(info);
        return json;
    }

    // 控制手臂
    public static String controlHand(String handCategory, int angleValue, int moveTime) {
        SerialPortFormatInfo info = new SerialPortFormatInfo();
        info.setAct("dig");
        if (TextUtils.equals(handCategory, ScriptConfig.HAND_LEFT)) {
            info.setCmd("larm");
        } else if (TextUtils.equals(handCategory, ScriptConfig.HAND_RIGHT)) {
            info.setCmd("rarm");
        }
        info.setAim(angleValue);
        info.setTim(moveTime);
        String json = JSON.toJSONString(info);
        return json;
    }

    // 控制运动
    public static String controlMove(int direction, int speed, int moveTime, int moveRadius) {
        SerialPortFormatInfo info = new SerialPortFormatInfo();
        info.setAct("move");
        switch (direction) {
            case 1:// 前进
                info.setCmd("fwd");
                break;
            case 2:// 后退
                info.setCmd("bwd");
                break;
            case 3:// 左转
                info.setCmd("tleft");
                break;
            case 4:// 右转
                info.setCmd("tright");
                break;
            case 5:// 停止
                info.setCmd("stop");
                break;
            case 6:// 后转
                info.setCmd("tleft");
                break;
            default:
                break;
        }
        info.setAim(speed);
        info.setTim(moveTime);
        info.setRad(moveRadius);
        String json = JSON.toJSONString(info);
        return json;
    }
}
