package com.robot.et.core.hardware.move;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.core.hardware.serialport.SerialPortHandler;
import com.robot.et.entity.RobotAction;
import com.robot.et.entity.SerialPortSendInfo;
import com.robot.et.util.BroadcastEnclosure;

// 串口控制动作
public class ControlMoveService extends Service {
    private int controlNumAways;// 控制小车一直走的次数
    private final String TAG = "move";
    private SerialPortHandler serialPortHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        serialPortHandler = new SerialPortHandler(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_CONTROL_AROUND_TOYCAR);
        filter.addAction(BroadcastAction.ACTION_CONTROL_WAVING);
        filter.addAction(BroadcastAction.ACTION_CONTROL_MOUTH_LED);
        filter.addAction(BroadcastAction.ACTION_CONTROL_TOYCAR_AWAYS);
        filter.addAction(BroadcastAction.ACTION_ROBOT_TURN_HEAD);
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_AROUND_TOYCAR)) {//控制周围小车
                Log.i(TAG, "控制周围小车");
                int direction = intent.getIntExtra("direction", 0);// 运动的方向
                int toyCarNum = intent.getIntExtra("toyCarNum", 0);// 控制小车的号码
                Log.i(TAG, "控制周围小车direction===" + direction);
                Log.i(TAG, "toyCarNum===" + toyCarNum);
                contrlToyCarMove(direction, toyCarNum);

                // 当控制小车的时候，一直走，直到符合下面的条件后再停下来
                if (DataConfig.isControlToyCar) {
                    if (direction == 1 || direction == 2) {// 前进后退的时候为10次
                        controlNumAways = 10;
                    } else if (direction == 3 || direction == 4) {// 左转右转的时候为100次
                        controlNumAways = 100;
                    }

                    // 当为5的时候代表停下来
                    if (direction != 5) {
                        SystemClock.sleep(200);
                        intent.setAction(BroadcastAction.ACTION_CONTROL_TOYCAR_AWAYS);
                        intent.putExtra("directionType", direction);
                        intent.putExtra("toyCarNum", toyCarNum);
                        sendBroadcast(intent);
                    } else {
                        Log.i(TAG, "directionType 停止===" + direction);
                        DataConfig.controlNum = controlNumAways;
                        contrlToyCarMove(5, toyCarNum);
                    }
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_WAVING)) {//举手摆手
                Log.i(TAG, "举手摆手");
                String handDirection = intent.getStringExtra("handDirection");// 代表手向上向下还是摆手
                String handCategory = intent.getStringExtra("handCategory");// 代表左手、右手还是双手
                String num = intent.getStringExtra("num");// 代表执行的次数
                Log.i(TAG, "handCategory===" + handCategory);
                if (!TextUtils.isEmpty(handDirection) && !TextUtils.isEmpty(handCategory)) {
                    handAction(handDirection, handCategory);
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_MOUTH_LED)) {//嘴的LED灯
                Log.i(TAG, "嘴的LED灯");
                String LEDState = intent.getStringExtra("LEDState");// 代表灯的状态，（开、关、闪烁）
                if (!TextUtils.isEmpty(LEDState)) {
                    controlMouthLED(LEDState);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_TOYCAR_AWAYS)) {//语音不停的控制小车
                Log.i(TAG, "语音不停的控制小车");
                int direction = intent.getIntExtra("directionType", 0);// 小车运动的方向
                int toyCarNum = intent.getIntExtra("toyCarNum", 0);// 小车的编码
                DataConfig.controlNum++;
                if (DataConfig.controlNum < controlNumAways) {
                    BroadcastEnclosure.controlToyCarMove(ControlMoveService.this, direction, toyCarNum);
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_ROBOT_TURN_HEAD)) {//控制头转
                Log.i(TAG, "控制头转");
                int directionValue = intent.getIntExtra("direction", DataConfig.TURN_HEAD_ABOUT);// 代表是点头，抬头还是转头等
                String angleValue = intent.getStringExtra("angle");// 代表头转动的度数
                Log.i(TAG, "控制头转angleValue==" + angleValue);//-30
                if (!TextUtils.isEmpty(angleValue)) {
                    if (angleValue.contains("-") || TextUtils.isDigitsOnly(angleValue)) {
                        controlHeadTurn(directionValue, Integer.parseInt(angleValue));
                    }
                }
            }
        }
    };

    // 控制小车
    private void contrlToyCarMove(int directionType, int toyCarNum) {
        // 转换为硬件所需要的json格式字符串
        if (directionType != 0) {
            Log.i(TAG, "控制机器人周围玩具toyCarNum===" + toyCarNum);
            RobotAction action = new RobotAction();
            action.setCategory("go");
            switch (directionType) {
                case 1:
                    Log.i(TAG, "玩具控制 向前");
                    action.setAction("forward");
                    break;
                case 2:
                    Log.i(TAG, "玩具控制 向后");
                    action.setAction("backward");
                    break;
                case 3:
                    Log.i(TAG, "玩具控制 向左");
                    action.setAction("turnLeft");
                    break;
                case 4:
                    Log.i(TAG, "玩具控制 向右");
                    action.setAction("turnRight");
                    break;
                case 5:
                    Log.i(TAG, "玩具控制 停止");
                    action.setAction("stop");
                    break;
                default:
                    break;
            }
            action.setCarNum(toyCarNum);
            String json = JSON.toJSONString(action);

            sendMoveAction(json);
        }
    }

    //控制摆臂
    private void handAction(String handDirection, String handCategory) {
        // 转换为硬件所需要的json格式字符串
        SerialPortSendInfo info = new SerialPortSendInfo();
        info.setcG("Hand");
        if (TextUtils.equals(handDirection, ScriptConfig.HAND_UP)) {
            info.setaT("up");
        } else if (TextUtils.equals(handDirection, ScriptConfig.HAND_DOWN)) {
            info.setaT("down");
        } else if (TextUtils.equals(handDirection, ScriptConfig.HAND_WAVING)) {
            info.setaT("waving");
        } else if (TextUtils.equals(handDirection, ScriptConfig.HAND_STOP)) {
            info.setaT("stop");
        }

        if (TextUtils.equals(handCategory, ScriptConfig.HAND_LEFT)) {
            info.setSide("L");
        } else if (TextUtils.equals(handCategory, ScriptConfig.HAND_RIGHT)) {
            info.setSide("R");
        } else if (TextUtils.equals(handCategory, ScriptConfig.HAND_TWO)) {
            info.setSide("LR");
        }
        String json = JSON.toJSONString(info);
        sendMoveAction(json);
    }

    //控制嘴的LED
    private void controlMouthLED(String LEDState) {
        // 转换为硬件所需要的json格式字符串
        SerialPortSendInfo info = new SerialPortSendInfo();
        info.setcG("DP");
        if (TextUtils.equals(LEDState, ScriptConfig.LED_ON)) {
            info.setaT("ON");
        } else if (TextUtils.equals(LEDState, ScriptConfig.LED_OFF)) {
            info.setaT("OFF");
        } else if (TextUtils.equals(LEDState, ScriptConfig.LED_BLINK)) {
            info.setaT("blink");
        }
        String json = JSON.toJSONString(info);
        sendMoveAction(json);
    }

    //控制头转向
    private void controlHeadTurn(int directionValue, int angleValue) {
        // 转换为硬件所需要的json格式字符串
        SerialPortSendInfo info = new SerialPortSendInfo();
        info.setcG("DIS");
        if (directionValue == DataConfig.TURN_HEAD_ABOUT) {
            info.setaT("HZ");
        } else if (directionValue == DataConfig.TURN_HEAD_AROUND) {
            info.setaT("VT");
        }
        info.setaG(angleValue);
        String json = JSON.toJSONString(info);
        sendMoveAction(json);
    }

    // 发送与运动相关的json消息
    private void sendMoveAction(String result) {
        Log.i(TAG, "json===" + result);
        if (!TextUtils.isEmpty(result)) {
            byte[] content = result.getBytes();
            byte[] end = new byte[]{0x0a};//结束符
            byte[] realContent = byteMerger(content, end);
            serialPortHandler.sendData(realContent);
        }
    }

    // 通过arraycopy获取byte数组
    private byte[] byteMerger(byte[] first, byte[] second) {
        byte[] content = new byte[first.length + second.length];
        System.arraycopy(first, 0, content, 0, first.length);
        System.arraycopy(second, 0, content, first.length, second.length);
        return content;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
