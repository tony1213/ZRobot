package com.robot.et.core.hardware.serialport;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.hardware.serialport.SerialPortUtil.OnDataReceiveListener;
import com.robot.et.entity.SerialPortReceiverInfo;
import com.robot.et.util.BroadcastEnclosure;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

// 串口接受信息类
public class SerialPortHandler implements OnDataReceiveListener {
    private static SerialPortUtil instance;
    private final String TAG = "SerialPort";
    private Context context;

    public SerialPortHandler(Context context) {
        Log.i(TAG, "串口实例化");
        this.context = context;
        instance = SerialPortUtil.getInstance();
        instance.setOnDataReceiveListener(this);
    }

    // 发送数据到硬件
    public void sendData(byte[] content) {
        Log.i(TAG, "接收串口Filter");
        // 发送数据到硬件
        if (content != null && content.length > 0) {
            instance.sendBuffer(content);
        }
    }

    /**
     * 实现OnDataReceiveListener接口方法
     *
     * @param buf  字节数组
     * @param size 数组大小
     */
    @Override
    public void onDataReceive(byte[] buf, int size) {
        Log.i(TAG, "onDataReceive()");
        if (buf != null && buf.length > 0 && size > 0) {
            // 将buffer转String
            String readMessage = new String(buf, 0, size);
            buffer.append(readMessage);
            String tempData = buffer.toString();
            Log.i(TAG, "tempData===" + tempData);
            // 获取完整的json字符串
            String result = getJsonString(tempData);
            Log.i(TAG, "result===" + result);
            // 对json结果处理
            if (!TextUtils.isEmpty(result)) {
                handleJsonResult(result);
            }
        }
    }

    //获取完整的json格式数据，可能丢帧
    private String getJsonString(String str) {
        String begin = "{";
        String end = "}";
        String result = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains(begin) && str.contains(end)) {
                int start = str.indexOf(begin);
                int stop = str.lastIndexOf(end);
                if (stop > start) {
                    result = str.substring(start, stop + 1);
                    if (!TextUtils.isEmpty(str)) {
                        buffer.delete(start, stop + 1);
                        Log.i(TAG, "getJsonString start===" + start);
                        if (start != 0) {
                            buffer.delete(0, start);
                        }
                    }
                }
            }
        }
        return result;
    }

    //对json数据结果处理
    private void handleJsonResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            SerialPortReceiverInfo info = getBluthReceiverInfo(result);
            if (info != null) {
                // 获取语音唤醒的值，当为1时代表有人唤醒
                int xFState = info.getxF();
                if (xFState == 1) {//有唤醒
                    // 获取唤醒的角度
                    int xFAngle = info.getxAg();
                    Log.i("wakeup", "xFAngle===" + xFAngle);
                    //当在人脸检测的时候不发送广播
                    if (!DataConfig.isFaceRecogniseIng) {
                        //软件做业务
                        Intent interruptIntent = new Intent();
                        interruptIntent.setAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
                        context.sendBroadcast(interruptIntent);

//                        handleAngle(xFAngle);
//
//                        Log.i("wakeup", "headAngle===" + headAngle);
//                        Log.i("wakeup", "bodyAngle===" + bodyAngle);
//                        //头部去转
//                        BroadcastEnclosure.controlHead(this, DataConfig.TURN_HEAD_ABOUT, String.valueOf(headAngle));
                        //身体去转
                        Intent turnIntent = new Intent();
                        turnIntent.setAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
                        turnIntent.putExtra("degree", xFAngle);
                        context.sendBroadcast(turnIntent);
                    }
                }

                // 获取红外检测的值，当为1时代表检测到人
                int hW = info.getHw();
                if (hW == 1) {//有人影进入范围
                    Log.i("wakeup", "检测到人影");
                    // 发送检测到人影的广播
                    BroadcastEnclosure.bodyDetection(context);
                }

                // 获取雷达数据
                int leftValue = info.getRdL();// 左边的距离数据
                int middleValue = info.getRdM();// 中间的距离数据
                int rightValue = info.getRdR();// 右边的距离数据
                Log.i(TAG, "leftValue==" + leftValue + "---middleValue===" + middleValue + "---rightValue===" + rightValue);
                // 只有在语音控制走的时候，才会发送雷达停止的广播
                if (DataConfig.isControlRobotMove) {
                    int stopValue = 50;// 距离多少时发送雷达停止的广播
                    if (leftValue < stopValue || middleValue < stopValue || rightValue < stopValue) {
                        DataConfig.isControlRobotMove = false;
                        Log.i(TAG, "雷达发送停止1");
                        // 发送雷达停止的广播
                        BroadcastEnclosure.sendRadar(context);

                        //向后退
                        int moveKey = ControlMoveEnum.BACKWARD.getMoveKey();
                        Log.i(TAG, "发送后退");
                        BroadcastEnclosure.controlRobotMoveRos(context, moveKey, "0");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "雷达发送停止2");
                                BroadcastEnclosure.sendRadar(context);
                            }
                        }, 1000);
                    }
                }

            }
        }
    }

    //缓存数据
    private static StringBuffer buffer = new StringBuffer(1024);
    private int headAngle;//头部角度
    private int bodyAngle;//身体角度
    private static int lastAngle = 0;// 最后一次的角度，跟转身相关

    //对角度处理
    //0-60  头向右转  ： 300-360  头向左转
    //左右横向运动以正中为0度，向右10度即-10，向左10度即+10
    private void handleAngle(int angle) {
        lastAngle = (lastAngle + angle) % 360;

        if (lastAngle >= 300 && lastAngle <= 360) {
            headAngle = 360 - lastAngle;
            bodyAngle = 0;

        } else if (lastAngle >= 0 && lastAngle <= 60) {
            headAngle = lastAngle - 60;
            bodyAngle = 0;

        } else {
            headAngle = 0;
            bodyAngle = lastAngle;
            lastAngle = 0;

        }
    }

    //{"rdl":0,"rdm":0,"rdr":0,"xf":1,"xag":20,"hw":1}
    // 解析传来的数据
    private SerialPortReceiverInfo getBluthReceiverInfo(String result) {
        SerialPortReceiverInfo info = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject object = new JSONObject(tokener);
                info = new SerialPortReceiverInfo();
                if (object.has("rdl")) {
                    info.setRdL(object.getInt("rdl"));
                }
                if (object.has("rdm")) {
                    info.setRdM(object.getInt("rdm"));
                }
                if (object.has("rdr")) {
                    info.setRdR(object.getInt("rdr"));
                }
                if (object.has("xf")) {
                    info.setxF(object.getInt("xf"));
                }
                if (object.has("xag")) {
                    info.setxAg(object.getInt("xag"));
                }
                if (object.has("hw")) {
                    info.setHw(object.getInt("hw"));
                }
            } catch (JSONException e) {
                Log.i(TAG, "JSONException==" + e.getMessage());
            }
        }
        return info;
    }
}
