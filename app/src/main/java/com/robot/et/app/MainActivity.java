package com.robot.et.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.core.hardware.move.ControlMoveService;
import com.robot.et.core.software.common.baidumap.IMap;
import com.robot.et.core.software.common.baidumap.Map;
import com.robot.et.core.software.common.receiver.HardwareReceiverService;
import com.robot.et.core.software.common.receiver.MsgReceiverService;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.view.CustomTextView;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.SpectrumManager;
import com.robot.et.core.software.common.view.TextManager;
import com.robot.et.core.software.common.view.VisualizerView;
import com.robot.et.core.software.ros.MasterChooserService;
import com.robot.et.core.software.ros.MoveControler;
import com.robot.et.core.software.video.agora.AgoraService;
import com.robot.et.core.software.voice.TextToVoiceService;
import com.robot.et.core.software.voice.TextUnderstanderService;
import com.robot.et.core.software.voice.VoiceToTextService;
import com.robot.et.entity.LocationInfo;
import com.robot.et.util.LocationManager;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;

public class MainActivity extends RosActivity {

    private MoveControler mover;//ROS运动控制
    private NodeConfiguration nodeConfiguration;//ROS节点

    private final float VISUALIZER_HEIGHT_DIP = 150f;//频谱View高度
    private Map map;
    private String city;


    public MainActivity() {
        super("XRobot", "Xrobot", URI.create("http://192.168.2.158:11311"));//本体的ROS IP和端口
//		super("XRobot","Xrobot",URI.create("http://192.168.2.164:11311"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initView();
        initService();
        initBaiDuMap();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.robot.et.rocon");
        filter.addAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE);
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
        filter.addAction(BroadcastAction.ACTION_ROBOT_RADAR);
        registerReceiver(receiver, filter);
    }

    // 初始化UI
    private void initView() {
        LinearLayout showText = (LinearLayout) findViewById(R.id.ll_show_text);
        LinearLayout showEmotion = (LinearLayout) findViewById(R.id.ll_show_emotion);
        CustomTextView tvText = (CustomTextView) findViewById(R.id.tv_text);
        ImageView imgEmotion = (ImageView) findViewById(R.id.img_emotion);
        LinearLayout showMusicView = (LinearLayout) findViewById(R.id.ll_show_music);
        LinearLayout showOneImg = (LinearLayout) findViewById(R.id.ll_show_one_img);
        ImageView imageView = (ImageView) findViewById(R.id.img_one);
        ImageView imageBitmap = (ImageView) findViewById(R.id.img_one_bitmap);
        ImageView imagePhoto = (ImageView) findViewById(R.id.img_photo);

        TextManager.setView(showText, tvText);
        EmotionManager.setView(showEmotion, imgEmotion);

        EmotionManager.showEmotion(R.mipmap.emotion_normal);

        //对播放音乐频谱的初始设置
        setVolumeControlStream(AudioManager.STREAM_MUSIC);//设置音频流 - STREAM_MUSIC：音乐回放即媒体音量
        VisualizerView visualizerView = new VisualizerView(this);
        visualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,//宽度
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)//高度
        ));
        showMusicView.addView(visualizerView);
        SpectrumManager.setView(showMusicView, visualizerView);
        OneImgManager.setView(showOneImg, imageView, imageBitmap, imagePhoto);
    }

    // 初始化百度地图
    private void initBaiDuMap() {
        map = new Map(this, new IMap() {
            @Override
            public void getLocationInfo(BDLocation location) {
                if (location != null) {
                    city = location.getCity();
                    String area = location.getDistrict();
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    // city=上海市,area=浦东新区,latitude=31.217769,longitude=121.603934
                    Log.i("map", "city=" + city + ",area=" + area + ",longitude=" + longitude + ",latitude=" + latitude);
                    LocationInfo info = new LocationInfo();
                    info.setCity(city);
                    info.setArea(area);
                    info.setLongitude(String.valueOf(longitude));
                    info.setLatitude(String.valueOf(latitude));
                    LocationManager.setInfo(info);
                }
            }
        });
    }

    private void initService() {
        //netty
//        startService(new Intent(this, NettyService.class));
        //语音听写
        startService(new Intent(this, VoiceToTextService.class));
        //文本理解
        startService(new Intent(this, TextUnderstanderService.class));
        //接受发来的消息
        startService(new Intent(this, MsgReceiverService.class));
        //语音合成
        startService(new Intent(this, TextToVoiceService.class));
        //控制动
        startService(new Intent(this, ControlMoveService.class));
        //agora
        startService(new Intent(this, AgoraService.class));
        //接受与硬件相关消息
        startService(new Intent(this, HardwareReceiverService.class));
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        mover = new MoveControler();
        mover.isPublishVelocity(false);
        nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
        nodeConfiguration.setMasterUri(getMasterUri());
        nodeMainExecutor.execute(mover, nodeConfiguration);
        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "初始化");
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE)) {
                //语音控制运动
                String direction = String.valueOf(intent.getIntExtra("direction", 5));
                Log.i("ControlMove", "MainActivity语音控制时，得到的direction参数：" + direction);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取的运动方向是：" + direction);
                if (null == direction || TextUtils.equals("", direction)) {
                    return;
                }
                if (TextUtils.equals("1", direction) || TextUtils.equals("2", direction)) {
                    doMoveAction(direction);
//                    try {
//                        Thread.sleep(1500);
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    doMoveAction("5");
                } else if (TextUtils.equals("3", direction)) {
                    doTrunAction(mover.getCurrentDegree(), 270);
                } else if (TextUtils.equals("4", direction)) {
                    doTrunAction(mover.getCurrentDegree(), 90);
                } else if (TextUtils.equals("5", direction)) {
                    doMoveAction(direction);
                }

            } else if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE)) {
                //中断运动控制
                doMoveAction("5");
                //唤醒控制运动
                double d = (double) intent.getIntExtra("degree", 0);
                Log.i("ControlMove", "MainActivity语音控制时，得到的唤醒角度" + d);
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取的唤醒角度是：" + d);
                doTrunAction(mover.getCurrentDegree(), d);
            }
        }
    };


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

    public void doTrunAction(double currentDegree, double degree) {
        mover.isPublishVelocity(true);
        double temp;
        if (currentDegree + degree <= 180) {
            temp = currentDegree + degree;
        } else {
            temp = currentDegree + degree - 360;
        }
        if ((degree > 0 && degree < 180)) {
            mover.execTurnRight();
            mover.setDegree(temp);
        } else {
            mover.execTurnLeft();
            mover.setDegree(temp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果city为空，代表没有定位成功，要把定位关掉
        if (TextUtils.isEmpty(city)) {
            map.destroyMap();
        }
        unregisterReceiver(receiver);
        stopService(new Intent(this, VoiceToTextService.class));
        stopService(new Intent(this, TextToVoiceService.class));
        stopService(new Intent(this, TextUnderstanderService.class));
        stopService(new Intent(this, MsgReceiverService.class));
//        stopService(new Intent(this, NettyService.class));
        stopService(new Intent(this, ControlMoveService.class));
        stopService(new Intent(this, AgoraService.class));
        stopService(new Intent(this, HardwareReceiverService.class));
        stopService(new Intent(this, MasterChooserService.class));
    }
}
