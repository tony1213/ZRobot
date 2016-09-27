package com.robot.et.core.hardware.serialport;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.core.hardware.serialport.SerialPortUtil.OnDataReceiveListener;
import com.robot.et.entity.SerialPortReceiverInfo;

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
