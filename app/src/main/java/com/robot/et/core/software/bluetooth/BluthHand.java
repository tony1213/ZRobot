package com.robot.et.core.software.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.common.enums.ControlMoveEnum;
import com.robot.et.entity.BluthReceiverInfo;
import com.robot.et.util.BroadcastEnclosure;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by houdeming on 2016/8/28.
 */
public class BluthHand {

    //对json数据结果处理
    public static void handleJsonResult(final Context context, String result) {
        Log.i("bluthresult", "handleJsonResult: result===" + result);
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
                        Intent interruptIntent = new Intent();
                        interruptIntent.setAction(BroadcastAction.ACTION_WAKE_UP_OR_INTERRUPT);
                        context.sendBroadcast(interruptIntent);

                        //身体去转
                        Intent turnIntent = new Intent();
                        turnIntent.setAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
                        turnIntent.putExtra("degree", xFAngle);
                        context.sendBroadcast(turnIntent);
                    }
                }

                //红外
                int hW = info.getHw();
                if (hW == 1) {//有人影进入范围
                    Log.i("bluthresult", "检测到人影");
                    BroadcastEnclosure.openFaceRecognise(context, false);
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
                        BroadcastEnclosure.sendRadar(context);

                        //向后退
                        int moveKey = ControlMoveEnum.BACKWARD.getMoveKey();
                        Log.i("bluthresult", "发送后退");
                        BroadcastEnclosure.controlRobotMove(context, moveKey);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("bluthresult", "雷达发送停止2");
                                BroadcastEnclosure.sendRadar(context);
                            }
                        }, 1000);
                    }
                }

            }
        }
    }

    //{"rdl":0,"rdm":0,"rdr":0,"xf":1,"xag":20,"hw":1}
    private static BluthReceiverInfo getBluthReceiverInfo(String result) {
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

}
