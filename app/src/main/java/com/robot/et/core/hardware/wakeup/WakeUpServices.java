package com.robot.et.core.hardware.wakeup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.ScriptConfig;
import com.robot.et.util.BroadcastEnclosure;
import com.robot.et.util.ShellUtils;

import java.util.ArrayList;
import java.util.List;

public class WakeUpServices extends Service {

    private int voiceFd;
    private int faceFd;
    private Intent interruptIntent, turnIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_RESET);
        registerReceiver(receiver, filter);

        interruptIntent = new Intent();
        turnIntent = new Intent();

        // 获取人体检测的串口id
        faceFd = WakeUp.faceWakeUpInit();
        Log.i("wakeup", "face faceFd==" + faceFd);
        // 没隔3秒去检测一次人体感应
        faceWakeUp();

        // 打开串口
        openI2C();
        // 获取语音唤醒的串口id
        voiceFd = WakeUp.wakeUpInit();
        Log.i("wakeup", "voice voiceFd==" + voiceFd);
        // 去检测是否有语音唤醒
        voiceWakeUp();

    }

    //打开I2C，一般的手机上面会出现错误
    private void openI2C() {
        List<String> commnandList = new ArrayList<String>();
        commnandList.add("su");
        commnandList.add("setenforce 0");
        commnandList.add("chmod 777 /sys/class/gpio");
        commnandList.add("chmod 777 /sys/class/gpio/export");
        commnandList.add("echo 13 > /sys/class/gpio/export");
        commnandList.add("chmod 777 /sys/class/gpio/gpio13");
        commnandList.add("chmod 777 /sys/class/gpio/gpio13/direction");
        commnandList.add("chmod 777 /sys/class/gpio/gpio13/edge");
        commnandList.add("chmod 777 /sys/class/gpio/gpio13/value");
        commnandList.add("echo 225 > /sys/class/gpio/export");
        commnandList.add("chmod 777 /sys/class/gpio/gpio225");
        commnandList.add("chmod 777 /sys/class/gpio/gpio225/direction");
        commnandList.add("chmod 777 /sys/class/gpio/gpio225/edge");
        commnandList.add("chmod 777 /sys/class/gpio/gpio225/value");
        ShellUtils.execCommand(commnandList, true);
    }

    //语言唤醒
    private void voiceWakeUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (voiceFd > 0) {
                        // 获取语音唤醒的状态值，当为1的时候代表唤醒，0的时候没有人唤醒
                        int wakeUpState = WakeUp.getWakeUpState();
//					Log.i("wakeup", "wakeUpState:" + wakeUpState);
                        if (wakeUpState == 1) {
                            // 获取唤醒的角度
                            int degree = WakeUp.getWakeUpDegree();
                            Log.i("wakeup", "degree:" + degree);
                            WakeUp.setGainDirection(0);// 设置麦克0为主麦

                            //当在人脸检测的时候不发送广播
                            if (!DataConfig.isFaceRecogniseIng) {
                                //软件做业务
                                interruptIntent.setAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
                                sendBroadcast(interruptIntent);
                                // 小于30度只头转
                                if (degree >= 330 && degree <= 360) {
                                    // 330-360  头向左转, 向左10度即+10
                                    int digit = 360 - degree;// 要转的角度
                                    BroadcastEnclosure.controlHead(WakeUpServices.this, DataConfig.TURN_HEAD_ABOUT, String.valueOf(digit));

                                } else if (degree >= 0 && degree <= 30) {
                                    // 0-30  头向右转, 向右10度即-10，
                                    String digit = "-" + degree;// 要转的角度
                                    BroadcastEnclosure.controlHead(WakeUpServices.this, DataConfig.TURN_HEAD_ABOUT, digit);

                                } else {// 大于30度时身体转过去，手同时摆动
                                    //硬件去转身
                                    turnIntent.setAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
                                    turnIntent.putExtra("degree", degree);
                                    sendBroadcast(turnIntent);
                                    // 摆手
                                    BroadcastEnclosure.controlWaving(WakeUpServices.this, ScriptConfig.HAND_UP, ScriptConfig.HAND_TWO, "0");
                                }
                            }
                        } else {
//						 Log.i("wakeup", "no wakeUp");
                        }
                    } else {
//					 Log.i("wakeup", "未打开I2C");
                    }
                }
            }
        }).start();
    }

    //人脸唤醒
    private void faceWakeUp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (faceFd > 0) {
                        // 获取人体检测的状态值，1代表检测到人体，0代表没有检测到人体
                        int faceWakeUpState = WakeUp.getFaceWakeUpState();
                        if (faceWakeUpState == 1) {
                            //有人影进入范围
                            Log.i("wakeup", "检测到人影");
                            // 发送人体感应的广播
                            BroadcastEnclosure.bodyDetection(WakeUpServices.this);
                        } else {
                            //没有人影进入范围
//                            Log.i("wakeup", "未检测到人影");
                        }
                    } else {
//                        Log.e("wakeup", "faceWakeUp初始化失败");
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.i("wakeup", "faceWakeUp InterruptedException=" + e.getMessage());
                    }
                }
            }
        }).start();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_RESET)) {// 唤醒重置
                Log.i("wakeup", "唤醒重置");
                int i = WakeUp.wakeUpReset();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 此处不要忘记关闭线程，暂时放在这里.2016-06-08
        Log.i("wakeup", "WakeUpServices onDestroy()");
        unregisterReceiver(receiver);
    }
}
