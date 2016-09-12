package com.robot.et.core.hardware.serialport;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.core.hardware.serialport.SerialPortUtil.OnDataReceiveListener;
import com.robot.et.util.BroadcastEnclosure;

import java.util.Timer;
import java.util.TimerTask;

public class SerialPortService extends Service implements OnDataReceiveListener {

    private static SerialPortUtil instance;
    private final int STOP_VALUE = 20;//距离多少时停止
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("SerialPort", "串口实例化");
        instance = SerialPortUtil.getInstance();
        instance.setOnDataReceiveListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_MOVE_TO_SERIALPORT);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_MOVE_TO_SERIALPORT)) {
                Log.i("SerialPort", "接收串口Filter");
                byte[] content = intent.getByteArrayExtra("actioncontent");
                instance.sendBuffer(content);
            }
        }
    };


    @Override
    public void onDataReceive(byte[] buffer, int size) {
        Log.i("SerialPort", "onDataReceive()");
        if (buffer != null && buffer.length > 0 && size > 0) {
            String result = new String(buffer, 0, size);
            Log.i("SerialPort", "result==" + result);
            //result=={"category":"radar","left":54,"middle":8,"right":47}
            if (!TextUtils.isEmpty(result)) {
                if (!isJsonString(result)) return;

                String[] datas = result.split(",");
                int leftValue = getData(datas[1]);
                int middleValue = getData(datas[2]);
                int rightValue = getData(datas[3]);
                Log.i("SerialPort", "leftValue==" + leftValue + "---middleValue===" + middleValue + "---rightValue===" + rightValue);
                if (DataConfig.isControlRobotMove) {
                    if (leftValue < STOP_VALUE || middleValue < STOP_VALUE || rightValue < STOP_VALUE) {
                        DataConfig.isControlRobotMove = false;
                        BroadcastEnclosure.sendRadar(SerialPortService.this);

                        //向后退
                        int moveKey = ControlMoveEnum.BACKWARD.getMoveKey();
                        BroadcastEnclosure.controlRobotMove(SerialPortService.this, moveKey);

                        if (timer == null) {
                            timer = new Timer();
                        }
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(1);
                            }
                        }, 1 * 1000);//向后走1秒后停止

                    }
                }
            }
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                BroadcastEnclosure.sendRadar(SerialPortService.this);
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    };

    //获取距离的数据
    private int getData(String result) {
        int value = 0;
        String splitSign = ":";
        if (!TextUtils.isEmpty(result)) {
            if (result.contains(splitSign)) {
                String[] datas = result.split(splitSign);
                String content = datas[1];
                if (!TextUtils.isEmpty(content)) {
                    if (TextUtils.isDigitsOnly(content)) {
                        value = Integer.parseInt(content);
                    } else {//content=47}
                        if (content.contains("}")) {
                            String temp = content.substring(0, content.length() - 1);
                            if (TextUtils.isDigitsOnly(temp)) {
                                value = Integer.parseInt(temp);
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    //是否是json字符串
    private boolean isJsonString(String result) {
        if (!TextUtils.isEmpty(result)) {
            if (result.contains("{") && result.contains("}")) {
                int begin = result.lastIndexOf("{");
                int end = result.lastIndexOf("}");
                if (begin == 0 && end == result.length() - 1) {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
