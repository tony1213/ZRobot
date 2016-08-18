package com.robot.et.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.core.hardware.move.ControlMoveService;
import com.robot.et.core.hardware.wakeup.WakeUpServices;
import com.robot.et.core.software.common.push.netty.NettyService;
import com.robot.et.core.software.common.receiver.MsgReceiverService;
import com.robot.et.core.software.common.view.CustomTextView;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.TextManager;
import com.robot.et.core.software.ros.MoveControler;
import com.robot.et.core.software.system.media.MusicPlayerService;
import com.robot.et.core.software.video.agora.AgoraService;
import com.robot.et.core.software.voice.iflytek.IflySpeakService;
import com.robot.et.core.software.voice.iflytek.IflyTextUnderstanderService;
import com.robot.et.core.software.voice.iflytek.IflyVoiceToTextService;
import com.robot.et.core.software.voice.turing.TuRingService;
import com.robot.et.util.SharedPreferencesKeys;
import com.robot.et.util.SharedPreferencesUtils;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

public class MainActivity extends RosActivity {


    private MoveControler mover;//ROS运动控制
    private NodeConfiguration nodeConfiguration;//ROS节点

    public MainActivity() {
        super("XRobot", "Xrobot", URI.create("http://192.168.3.1:11311"));//本体的ROS IP和端口
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //记录城市、区域位置
        SharedPreferencesUtils share = SharedPreferencesUtils.getInstance();
        share.putString(SharedPreferencesKeys.CITY_KEY, "上海市");
        share.putString(SharedPreferencesKeys.AREA_KEY, "浦东新区");
        share.commitValue();

        initView();

    }

    private void initView() {
        LinearLayout showText = (LinearLayout) findViewById(R.id.ll_show_text);
        LinearLayout showEmotion = (LinearLayout) findViewById(R.id.ll_show_emotion);
        CustomTextView tvText = (CustomTextView) findViewById(R.id.tv_text);
        ImageView imgLeft = (ImageView) findViewById(R.id.img_left);
        ImageView imgRight = (ImageView) findViewById(R.id.img_right);
        TextManager.setTextView(tvText);
        TextManager.setShowTextLl(showText);
        EmotionManager.setImg(imgLeft, imgRight);
        EmotionManager.setShowLinearLayout(showEmotion);

        EmotionManager.showEmotion(R.mipmap.emotion_normal);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("main", "onResume()");
        initService();
    }

    private void initService() {
        //netty
        startService(new Intent(this, NettyService.class));
        //语音听写
        startService(new Intent(this, IflyVoiceToTextService.class));
        //文本理解
        startService(new Intent(this, IflyTextUnderstanderService.class));
        //图灵
        startService(new Intent(this, TuRingService.class));
        //音乐
        startService(new Intent(this, MusicPlayerService.class));
        //唤醒
        startService(new Intent(this, WakeUpServices.class));
        //接受发来的消息
        startService(new Intent(this, MsgReceiverService.class));
        //语音合成
        startService(new Intent(this, IflySpeakService.class));
        //控制动
        startService(new Intent(this, ControlMoveService.class));
        //agora
        startService(new Intent(this, AgoraService.class));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE)){
                //此部分代码暂时这样修改，待完善。（时间太赶）2016-07-16
                String direction=intent.getStringExtra("direction");
                Log.i("ROS_MOVE","语音控制时，得到的direction参数："+direction);
                String digit=intent.getStringExtra("digit");
                Log.i("ROS_MOVE","语音控制时，得到的digit参数："+digit);
                if (null==direction|| TextUtils.equals("", direction)) {
                    return;
                }
                if (TextUtils.equals("1",direction)||TextUtils.equals("2",direction)){
                    doMoveAction(direction);
                    try {
                        Thread.sleep(1500);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }finally {
                        doMoveAction("5");
                    }
                }else if (TextUtils.equals("3",direction)){
                    doTrunAction(mover.getCurrentDegree(),270);
                }else if (TextUtils.equals("4",direction)){
                    doTrunAction(mover.getCurrentDegree(),90);
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE)){
                Log.i("ROS_WAKE_UP","语音唤醒时，当前机器人的角度："+mover.getCurrentDegree());
                int data=intent.getIntExtra("degree",0);//获取的Brocast传递的角度
                Log.i("ROS_WAKE_UP_DEGREE","语音唤醒时，获取的角度："+data);
                if (data == 0 || data == 360){
                    //原地不动
                    return;
                }
                doTrunAction(mover.getCurrentDegree(),Double.valueOf(data));
            }
        }
    };

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        mover = new MoveControler();
        mover.isPublishVelocity(false);
        nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(mover, nodeConfiguration);
    }


    private void doMoveAction(String message) {
        mover.isPublishVelocity(true);
        if (TextUtils.equals("1", message)) {
            Log.i("ROS_MOVE", "机器人移动方向:向前");
            mover.execMoveForword();
        } else if (TextUtils.equals("2", message)) {
            Log.i("ROS_MOVE", "机器人移动方向:向后");
            mover.execMoveBackForward();
        } else if (TextUtils.equals("3", message)) {
            Log.i("ROS_MOVE", "机器人移动方向:向左");
            mover.execTurnLeft();
        } else if (TextUtils.equals("4", message)) {
            Log.i("ROS_MOVE", "机器人移动方向:向右");
            mover.execTurnRight();
        } else if (TextUtils.equals("5", message)) {
            Log.i("ROS_MOVE", "机器人移动方向:停止");
            mover.execStop();
        }
    }
    public void doTrunAction(double currentDegree,double degree){
        mover.isPublishVelocity(true);
        double temp;
        if (currentDegree+degree<=180){
            temp=currentDegree+degree;
        }else {
            temp=currentDegree+degree-360;
        }
        if ((degree > 0 && degree < 180)){
            mover.execTurnRight();
            mover.setDegree(temp);
        }else{
            mover.execTurnLeft();
            mover.setDegree(temp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryService();
    }

    private void destoryService() {
        stopService(new Intent(this, IflyVoiceToTextService.class));
        stopService(new Intent(this, IflySpeakService.class));
        stopService(new Intent(this, IflyTextUnderstanderService.class));
        stopService(new Intent(this, TuRingService.class));
        stopService(new Intent(this, MusicPlayerService.class));
        stopService(new Intent(this, WakeUpServices.class));
        stopService(new Intent(this, MsgReceiverService.class));
        stopService(new Intent(this, NettyService.class));
        stopService(new Intent(this, ControlMoveService.class));
        stopService(new Intent(this, AgoraService.class));
    }
}
