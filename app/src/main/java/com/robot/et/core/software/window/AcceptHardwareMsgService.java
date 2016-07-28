package com.robot.et.core.software.window;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.impl.SpeechlHandle;

import java.util.Random;

/**
 * Created by houdeming on 2016/7/27.
 * 接受硬件消息的service
 */
public class AcceptHardwareMsgService extends Service {
    private Intent intent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("accept", "AcceptHardwareMsgService  onCreate()");
        intent = new Intent();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
        registerReceiver(receiver, filter);

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT)) {
                Log.i("accept", "AcceptHardwareMsgService 接受到唤醒中断的广播");
                responseAwaken();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void responseAwaken() {
        //停止说
        SpeechlHandle.cancelSpeak();
        //停止听
        SpeechlHandle.cancelListen();
        //停止唱歌
        intent.setAction(BroadcastAction.ACTION_STOP_MUSIC);
        sendBroadcast(intent);

        SpeechlHandle.startSpeak(DataConfig.SPEAK_TYPE_CHAT, getAwakenContent());

    }

    //获取唤醒时要说的内容
    private String getAwakenContent() {
        String content = "";
        String[] wakeUpSpeakContent = getResources().getStringArray(R.array.wake_up_speak_content);
        int size = wakeUpSpeakContent.length;
        if (wakeUpSpeakContent != null && size > 0) {
            int i = new Random().nextInt(size);
            content = wakeUpSpeakContent[i];
        }
        return content;
    }

}
