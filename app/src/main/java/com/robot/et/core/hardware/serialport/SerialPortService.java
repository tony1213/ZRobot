package com.robot.et.core.hardware.serialport;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.robot.et.common.BroadcastAction;
import com.robot.et.core.hardware.serialport.SerialPortUtil.OnDataReceiveListener;
import com.robot.et.entity.RadarInfo;

public class SerialPortService extends Service implements OnDataReceiveListener {

    private static SerialPortUtil instance;

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
            RadarInfo radarInfo = new Gson().fromJson(result, RadarInfo.class);
            int stopValue = 20;//距离多少时停止
            if (radarInfo.getLeft() < stopValue || radarInfo.getMiddle() < stopValue || radarInfo.getRight() < stopValue) {
                Intent intent = new Intent();
                intent.setAction(BroadcastAction.ACTION_ROBOT_RANDAR);
                sendBroadcast(intent);
            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
