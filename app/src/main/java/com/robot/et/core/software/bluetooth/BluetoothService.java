package com.robot.et.core.software.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.entity.BluthReceiverInfo;
import com.robot.et.util.BroadcastEnclosure;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by houdeming on 2016/8/25.
 */
public class BluetoothService extends Service {
    //演示robot2  address：20:16:06:20:65:84   密码：0000
    //备用AIRobot address：20:16:06:20:70:69   密码：0000
    private final String BLUE_ADDRESS = "20:16:06:20:65:84";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mChatService;
    private boolean isBreak;
    private Intent interruptIntent, turnIntent;
    private static int lastAngle = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("bluth", "BluetoothService  onCreate()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            mChatService = new BluetoothChatService(this, mHandler);
            startChatService();
            connectBluth();
        }

        interruptIntent = new Intent();
        turnIntent = new Intent();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_MOVE_TO_BLUTH);
        registerReceiver(receiver, filter);
    }

    private void startChatService() {
        if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
            mChatService.start();
        }
    }

    private void stopChatService() {
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_MOVE_TO_BLUTH)) {
                Log.i("bluth", "发送蓝牙数据");
                byte[] content = intent.getByteArrayExtra("actioncontent");
                if (content != null && content.length > 0) {
                    if (mChatService != null) {
                        mChatService.write(content);
                    }
                }
            }
        }
    };

    //缓存数据
    private static StringBuffer buffer = new StringBuffer(1024);

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConfig.MESSAGE_STATE_CHANGE:// 吐司
                    Log.i("bluth", "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:// 连接蓝牙
                            Log.i("bluth", "STATE_CONNECTED");
                            if (isBreak) {
                                isBreak = false;
                                SpeechImpl.getInstance().cancelListen();
                                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "蓝牙已连接");
                            }
                            //头部复位
                            BroadcastEnclosure.controlHead(BluetoothService.this, DataConfig.TURN_HEAD_ABOUT, "0");
                            break;
                        case BluetoothChatService.STATE_CONNECTING:// 正在连接
                            Log.i("bluth", "STATE_CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:// 蓝牙列表
                        case BluetoothChatService.STATE_NONE:// 没有蓝牙数据
                            Log.i("bluth", "STATE_NONE");
                            break;
                    }
                    break;
                case BluetoothConfig.MESSAGE_WRITE:// 写数据
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.i("bluth", "MESSAGE_WRITE writeMessage===" + writeMessage);
                    break;
                case BluetoothConfig.MESSAGE_READ:// 读数据
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    buffer.append(readMessage);
                    String buf = buffer.toString();
                    Log.i("bluth", "MESSAGE_READ buf.length()===" + buf.length());
                    Log.i("bluth", "MESSAGE_READ buf===" + buf);

                    String result = getJsonString(buf);
                    Log.i("bluthresult", "MESSAGE_READ result===" + result);
                    if (!TextUtils.isEmpty(result)) {
                        handleJsonResult(result);
                    }

                    break;
                case BluetoothConfig.MESSAGE_DEVICE_NAME:// 设备的名字
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(BluetoothConfig.DEVICE_NAME);
                    Log.i("bluth", "MESSAGE_DEVICE_NAME mConnectedDeviceName===" + mConnectedDeviceName);

                    break;
                case BluetoothConfig.MESSAGE_TOAST:// 蓝牙断开要重新连接
                    Log.i("bluth", "蓝牙断开要重新连接===" + msg.getData().getString(BluetoothConfig.TOAST));
                    if (!isBreak) {
                        isBreak = true;
                        stopChatService();
                        startChatService();
                        SpeechImpl.getInstance().cancelListen();
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "蓝牙断开");
                    }
                    connectBluth();
                    break;
            }
        }
    };

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
                        Log.i("bluth", "MESSAGE_READ start===" + start);
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
            BluthReceiverInfo info = getBluthReceiverInfo(result);
            if (info != null) {
                //唤醒
                int xFState = info.getxF();
                if (xFState == 1) {//有唤醒
                    int xFAngle = info.getxAg();
                    Log.i("wakeup", "xFAngle===" + xFAngle);
                    //当在人脸检测的时候不发送广播
                    if (!DataConfig.isFaceRecogniseIng) {
                        //软件做业务
                        interruptIntent.setAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
                        sendBroadcast(interruptIntent);

//                        handleAngle(xFAngle);
//
//                        Log.i("wakeup", "headAngle===" + headAngle);
//                        Log.i("wakeup", "bodyAngle===" + bodyAngle);
//                        //头部去转
//                        BroadcastEnclosure.controlHead(this, DataConfig.TURN_HEAD_ABOUT, String.valueOf(headAngle));
                        //身体去转
                        turnIntent.setAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
                        turnIntent.putExtra("degree", xFAngle);
                        sendBroadcast(turnIntent);
                    }
                }

                //红外
                int hW = info.getHw();
                if (hW == 1) {//有人影进入范围
                    Log.i("bluthresult", "检测到人影");
                    BroadcastEnclosure.openFaceRecognise(this, false);
                }

                //雷达数据
                int leftValue = info.getRdL();
                int middleValue = info.getRdM();
                int rightValue = info.getRdR();
                Log.i("bluthresult", "leftValue==" + leftValue + "---middleValue===" + middleValue + "---rightValue===" + rightValue);
                if (DataConfig.isControlRobotMove) {
                    int stopValue = 20;
                    if (leftValue < stopValue || middleValue < stopValue || rightValue < stopValue) {
                        DataConfig.isControlRobotMove = false;
                        Log.i("bluthresult", "雷达发送停止1");
                        BroadcastEnclosure.sendRadar(this);

                        //向后退
                        int moveKey = ControlMoveEnum.BACKWARD.getMoveKey();
                        Log.i("bluthresult", "发送后退");
                        BroadcastEnclosure.controlRobotMove(this, moveKey);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("bluthresult", "雷达发送停止2");
                                BroadcastEnclosure.sendRadar(BluetoothService.this);
                            }
                        }, 1000);
                    }
                }

            }
        }
    }

    private int headAngle;//头部角度
    private int bodyAngle;//身体角度

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
    private BluthReceiverInfo getBluthReceiverInfo(String result) {
        BluthReceiverInfo info = null;
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject object = new JSONObject(tokener);
                info = new BluthReceiverInfo();
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
                Log.i("bluthresult", "JSONException");
            }
        }
        return info;
    }

    //连接蓝牙
    private void connectBluth() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(BLUE_ADDRESS);
        mChatService.connect(device, true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopChatService();
    }

}
