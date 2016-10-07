package com.robot.et.core.hardware.serialport;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.hardware.serialport.SerialPortUtil.OnDataReceiveListener;
import com.robot.et.core.software.common.move.Come;
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
    private int stopCount;

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

    // 获取雷达数据
    public void getRadarData() {
        // 每次获取雷达数据时，一定要清空缓存的buffer
        if (buffer != null && buffer.length() > 0) {
            Log.i(TAG, "buffer 清空");
            buffer.setLength(0);
        }
        stopCount = 0;
        instance.getData();
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
                info = getReceiverInfo(result);
                // 只有在语音控制机器人运动的时候才会发送雷达广播
                if (DataConfig.isControlRobotMove) {
                    // 如果在蔽障范围内，发送停止的雷达广播
                    if (info != null) {
                        if (info.getDataM() <= 60 || info.getDataL() <= 60 || info.getDataR() <= 60) {
                            stopCount++;
                            // 防止缓存数据影响，连续3次的话，再发送雷达停止广播
                            if (stopCount == 3) {
                                stopCount = 0;
                                info = null;
                                DataConfig.isOpenRadar = false;
                                if (DataConfig.isComeIng) {
                                    DataConfig.isComeIng = false;
                                    Come.stopTimer();
                                }
                                DataConfig.isControlRobotMove = false;
                                // 发送雷达停止的广播
                                Log.i(TAG, "发送雷达停止的广播");
                                BroadcastEnclosure.sendRadar(context);
                            }
                        }
                    }
                }
            } else {
                info = null;
            }
        } else {
            info = null;
        }
    }

    private static SerialPortReceiverInfo info;

    public static SerialPortReceiverInfo getRadarInfo() {
        return info;
    }

    public static void setRadarInfo(SerialPortReceiverInfo info) {
        SerialPortHandler.info = info;
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

    //缓存数据
    private static StringBuffer buffer = new StringBuffer(1024);

    //{"act":"ult","datam":48,"datal":86,"datar":125}
    // 解析传来的数据
    private SerialPortReceiverInfo getReceiverInfo(String result) {
        SerialPortReceiverInfo info = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject object = new JSONObject(tokener);
                info = new SerialPortReceiverInfo();
                Log.i(TAG, "getReceiverInfo 获取数据");
                if (object.has("datal")) {
                    info.setDataL(object.getInt("datal"));
                }
                if (object.has("datam")) {
                    info.setDataM(object.getInt("datam"));
                }
                if (object.has("datar")) {
                    info.setDataR(object.getInt("datar"));
                }
            } catch (JSONException e) {
                Log.i(TAG, "JSONException==" + e.getMessage());
            }
        }
        return info;
    }
}
