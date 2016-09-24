package com.robot.et.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.location.BDLocation;
import com.github.rosjava.android_remocons.common_tools.master.ConcertChecker;
import com.github.rosjava.android_remocons.common_tools.master.MasterId;
import com.github.rosjava.android_remocons.common_tools.master.RoconDescription;
import com.github.rosjava.android_remocons.common_tools.rocon.AppLauncher;
import com.github.rosjava.android_remocons.common_tools.rocon.InteractionsManager;
import com.github.rosjava.android_remocons.common_tools.system.WifiChecker;
import com.google.common.base.Preconditions;
import com.robot.et.R;
import com.robot.et.common.BroadcastAction;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.baidumap.IMap;
import com.robot.et.core.software.common.baidumap.Map;
import com.robot.et.core.software.common.receiver.ControlMoveService;
import com.robot.et.core.software.common.receiver.HardwareReceiverService;
import com.robot.et.core.software.common.receiver.MsgReceiverService;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.common.view.CustomTextView;
import com.robot.et.core.software.common.view.EmotionManager;
import com.robot.et.core.software.common.view.OneImgManager;
import com.robot.et.core.software.common.view.SpectrumManager;
import com.robot.et.core.software.common.view.TextManager;
import com.robot.et.core.software.common.view.VisualizerView;
import com.robot.et.core.software.ros.PairSubscriber;
import com.robot.et.core.software.ros.StatusPublisher;
import com.robot.et.core.software.ros.client.FollowClient;
import com.robot.et.core.software.ros.client.MoveClient;
import com.robot.et.core.software.ros.client.RmapClient;
import com.robot.et.core.software.ros.client.VisualClient;
import com.robot.et.core.software.ros.connect.first.MasterChooserService;
import com.robot.et.core.software.ros.connect.second.MasterFactory;
import com.robot.et.core.software.ros.move.MoveControler;
import com.robot.et.core.software.ros.position.PositionControler;
import com.robot.et.core.software.video.agora.AgoraService;
import com.robot.et.core.software.voice.TextToVoiceService;
import com.robot.et.core.software.voice.TextUnderstanderService;
import com.robot.et.core.software.voice.VoiceToTextService;
import com.robot.et.db.RobotDB;
import com.robot.et.entity.LocationInfo;
import com.robot.et.entity.VisionRecogniseEnvironmentInfo;
import com.robot.et.util.LocationManager;

import org.ros.android.RosActivity;
import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.service.ServiceResponseListener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import rocon_interaction_msgs.GetInteractionsResponse;
import rocon_interaction_msgs.Interaction;

public class MainAppActivity extends RosActivity {
    /* startActivityForResult Request Codes */
    private static final int CONCERT_MASTER_CHOOSER_REQUEST_CODE = 1;

    private final static String TAG = "MasterChooserService";

    private final float VISUALIZER_HEIGHT_DIP = 150f;//频谱View高度

    private final float CIRCLE = 3.1415926535f; //圆周率

    private ArrayList<Interaction> availableAppsCache;
    private Interaction selectedInteraction;
    private RoconDescription roconDescription;
    private InteractionsManager interactionsManager;
    private StatusPublisher statusPublisher;
    private PairSubscriber pairSubscriber;
    private NodeConfiguration nodeConfiguration;
    private boolean validatedConcert;
    private Map map;
    private String city;

    private VisualClient visualClient;//ROS 视觉识别的Client（Service：learn_to_recognize_ros_server）
    private RmapClient rmapClient;   //ROS 地图保存的Client（Service：/turtlebot/save_only_map）
    private MoveClient moveClient;   //ROS 运动控制（Topic:/set_goal）
    private PositionControler positionControler; //ROS 获取当前位置的坐标（Topic:/amcl_pose）
    private FollowClient followClient; //ROS跟随服务Client：（Service：/turtlebot_follower/change_state）

    private MoveControler mover; //ROS 操作Twist控制Robot（Topic:/cmd_vel_mux/input/teleop && /odom）

    public MainAppActivity() {
        super("XRobot", "Xrobot");//本体的ROS IP和端口
        availableAppsCache = new ArrayList<Interaction>();
        statusPublisher = StatusPublisher.getInstance();
        pairSubscriber = PairSubscriber.getInstance();
        pairSubscriber.setAppHash(0);
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
        filter.addAction(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE);
        filter.addAction(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE);
        filter.addAction(BroadcastAction.ACTION_ROS_SERVICE);
        filter.addAction(BroadcastAction.ACTION_ROBOT_RADAR);
        registerReceiver(receiver, filter);
        prepareAppManager();
    }

    @Override
    public void startMasterChooser() {
        Log.e(TAG, "开始执行MasterChooserService");
//        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "小黄人我来了");

//        startService(new Intent(this, MasterChooserService.class));
//        super.startActivityForResult(new Intent(this, MasterChooserActivity.class), CONCERT_MASTER_CHOOSER_REQUEST_CODE);
        super.startActivityForResult(new Intent(this, MasterFactory.class), CONCERT_MASTER_CHOOSER_REQUEST_CODE);
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
        visualizerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,(int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));//宽度，高度
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

    private void init(Intent intent) {
        URI uri;
        try {
            roconDescription = (RoconDescription) intent.getSerializableExtra(RoconDescription.UNIQUE_KEY);
            validatedConcert = false;
            validateConcert(roconDescription.getMasterId());
            uri = new URI(roconDescription.getMasterId().getMasterUri());
            Log.i("Remocon", "init(Intent) - master uri is " + uri.toString());
        } catch (ClassCastException e) {
            Log.e("Remocon", "Cannot get concert description from intent. " + e.getMessage());
            throw new RosRuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RosRuntimeException(e);
        }
        nodeMainExecutorService.setMasterUri(uri);
        if (roconDescription.getCurrentRole() == null) {
            chooseRole();
        } else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    while (!validatedConcert) {
                        // should use a sleep here to avoid burnout
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("Remocon", "init(Intent) passing control back to init(nodeMainExecutorService)");
                    MainAppActivity.this.init(nodeMainExecutorService);
                    return null;
                }
            }.execute();
        }
    }

    private void prepareAppManager() {
        // Prepare the app manager; we do here instead of on init to keep using the same instance when switching roles
        interactionsManager = new InteractionsManager(
                new InteractionsManager.FailureHandler() {
                    public void handleFailure(String reason) {
                        Log.e(TAG, "Failure on interactions manager: " + reason);
                    }
                }
        );
        interactionsManager.setupGetInteractionsService(new ServiceResponseListener<GetInteractionsResponse>() {
            @Override
            public void onSuccess(GetInteractionsResponse response) {
                Log.e(TAG,"GetInteractions Success");
                List<Interaction> apps = response.getInteractions();
                if (apps.size() > 0) {
                    availableAppsCache = (ArrayList<Interaction>) apps;
                    Log.e(TAG, "Interaction Publication: " + availableAppsCache.size() + " apps");
                    for (int i = 0; i < availableAppsCache.size(); i++) {
                        Log.e(TAG, "DisplayName:" + availableAppsCache.get(i).getDisplayName());
                    }
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "小黄人我来啦");
                } else {
                    // TODO: maybe I should notify the user... he will think something is wrong!
                    Log.e(TAG, "No interactions available for the '" + roconDescription.getCurrentRole() + "' role.");
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                Log.e(TAG, "retrieve interactions for the role '" + roconDescription.getCurrentRole() + "' failed: " + e.getMessage());
            }
        });
        interactionsManager.setupRequestService(new ServiceResponseListener<rocon_interaction_msgs.RequestInteractionResponse>() {
            @Override
            public void onSuccess(rocon_interaction_msgs.RequestInteractionResponse response) {
                Log.e(TAG,"");
                Preconditions.checkNotNull(selectedInteraction);
                final boolean allowed = response.getResult();
                final String reason = response.getMessage();
                boolean ret_launcher_dialog = false;
                if (AppLauncher.checkAppType(selectedInteraction.getName()) == AppLauncher.AppType.NOTHING) {
                    pairSubscriber.setAppHash(selectedInteraction.getHash());
                    ret_launcher_dialog = true;
                } else {
                    if (allowed) {
                        pairSubscriber.setAppHash(selectedInteraction.getHash());
                    } else {
                        pairSubscriber.setAppHash(0);
                    }
                }
                if (ret_launcher_dialog) {
                    Log.i(TAG, "Selected Launch button");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLauncher.Result result = AppLauncher.launch(MainAppActivity.this, roconDescription, selectedInteraction);
                            if (result == AppLauncher.Result.SUCCESS) {
                                Log.e(TAG, "Android app launch success");
                                // App successfully launched! Notify the concert and finish this activity
                                //statusPublisher.update(true, selectedInteraction.getHash(), selectedInteraction.getName());
                                // TODO try to no finish so statusPublisher remains while on app;  risky, but seems to work!    finish();
                            } else if (result == AppLauncher.Result.NOTHING) {
                                Log.e(TAG,"Android app nothing");
                                //statusPublisher.update(false, selectedInteraction.getHash(), selectedInteraction.getName());
                            } else if (result == AppLauncher.Result.NOT_INSTALLED) {
                                // App not installed; ask for going to play store to download the missing app
                                Log.e(TAG, "Android app not installed.");
                                statusPublisher.update(false, 0, null);
                                selectedInteraction = null;
                            } else {
                                Log.e(TAG, "Cannot start Android app");
                            }
                        }
                    });
                } else {
                    Log.i(TAG, "User select cancel");
                    statusPublisher.update(false, 0, null);
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                Log.e(TAG, "Retrieve rapps for role " + roconDescription.getCurrentRole() + " failed: " + e.getMessage());
            }
        });
        pairSubscriber.setAppHash(0);
    }

    public void validateConcert(final MasterId id) {
        // Run a set of checkers in series. The last step must ensure the master is up.
        final ConcertChecker cc = new ConcertChecker(
                new ConcertChecker.ConcertDescriptionReceiver() {
                    public void receive(RoconDescription concertDescription) {
                        // Check that it's not busy
                        if (concertDescription.getConnectionStatus() == RoconDescription.UNAVAILABLE) {
                            Log.e(TAG, "Concert is unavailable : busy serving another remote controller.");
                            startMasterChooser();
                        } else {
                            validatedConcert = true;   // for us this is enough check!
                        }
                    }
                }, new ConcertChecker.FailureHandler() {
            public void handleFailure(String reason) {
                final String reason2 = reason;
                // Kill the connecting to ros master dialog.
                Log.e(TAG, "Cannot contact ROS master: " + reason2);
                // TODO : gracefully abort back to the concert master chooser instead.
                finish();
            }
        });

        // Ensure that the correct WiFi network is selected.
        final WifiChecker wc = new WifiChecker(
                new WifiChecker.SuccessHandler() {
                    public void handleSuccess() {
                        Log.e(TAG, "Starting connection process");
                        cc.beginChecking(id);
                    }
                }, new WifiChecker.FailureHandler() {
            public void handleFailure(String reason) {
                final String reason2 = reason;
                Log.e(TAG, "Cannot connect to concert WiFi: " + reason2);
                finish();
            }
        }, new WifiChecker.ReconnectionHandler() {
            public boolean doReconnection(String from, String to) {
                if (from == null) {
                    Log.e(TAG, "To interact with this master, you must connect to " + to + "\nDo you want to connect to " + to + "?");
                } else {
                    Log.e(TAG, "To interact with this master, you must switch wifi networks" + "\nDo you want to switch from " + from + " to " + to + "?");
                }
                return true;
            }
        });
        wc.beginChecking(id, (WifiManager) getSystemService(WIFI_SERVICE));
    }

    private void chooseRole() {
        Log.i(TAG, "concert chosen; show choose user role dialog");
        roconDescription.setCurrentRole(0);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (!validatedConcert) {
                    // should use a sleep here to avoid burnout
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                MainAppActivity.this.init(nodeMainExecutorService);
                return null;
            }
        }.execute();
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
        Log.e(TAG, "init(NodeMainExecutor nodeMainExecutor)");
        mover = new MoveControler();
        mover.isPublishVelocity(false);

        positionControler = new PositionControler();//获取当前的位置坐标
        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
            interactionsManager.init(roconDescription.getInteractionsNamespace());
            interactionsManager.getAppsForRole(roconDescription.getMasterId(), roconDescription.getCurrentRole());
            interactionsManager.setRemoconName(statusPublisher.REMOCON_FULL_NAME);
            //execution of publisher
            if (!statusPublisher.isInitialized()) {
                // If we come back from an app, it should be already initialized, so call execute again would crash
                nodeMainExecutorService.execute(statusPublisher, nodeConfiguration.setNodeName(StatusPublisher.NODE_NAME));
            }
            //execution of subscriber
            pairSubscriber.setAppHash(0);

            if (!pairSubscriber.isInitialized()) {
                // If we come back from an app, it should be already initialized, so call execute again would crash
                nodeMainExecutorService.execute(pairSubscriber, nodeConfiguration.setNodeName(pairSubscriber.NODE_NAME));
            }
            nodeMainExecutorService.execute(mover, nodeConfiguration.setNodeName("mover"));

            nodeMainExecutorService.execute(positionControler, nodeConfiguration.setNodeName("positionControler"));
//            nodeMainExecutor.execute(positionControler, nodeConfiguration);
        } catch (IOException e) {
            // Socket problem
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(BroadcastAction.ACTION_ROS_SERVICE)) {
                String flag = intent.getStringExtra("rosKey");
                String name = intent.getStringExtra("name");
                Log.e(TAG, "下发ROS服务：Key" + flag + ",name:" + name);
                if (TextUtils.equals("Roaming", flag)) {
                    //随便走走(Rapp)
                    startRosInteraction(availableAppsCache, roconDescription.getCurrentRole(), "Roaming");
                    SpeechImpl.getInstance().startListen();
                } else if (TextUtils.equals("WORLDFOLLOWER", flag)) {
                    //跟随和不跟随(service)
                    Log.e("ROS_Client", "Service:Start WORLDFOLLOWER");
                    if (TextUtils.equals("0", name)) {
                        followClient = new FollowClient((byte) 0);
                    } else {
                        followClient = new FollowClient((byte) 1);
                    }
                    nodeMainExecutorService.execute(followClient, nodeConfiguration.setNodeName("FollowClient"));
                } else if (TextUtils.equals("Deep Learning", flag)) {
                    //视觉深度学习(Rapp)
                    Log.e("ROS_Client", "Start Deep Learning");
                    startRosInteraction(availableAppsCache, roconDescription.getCurrentRole(), "Deep Learning");
                } else if (TextUtils.equals("DeepLearnInit", flag)) {
                    //视觉初始化(service)
                    Log.e("ROS_Client", "Service：Start DeepLearnInit");
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的");
                    visualClient = new VisualClient((short) 1, "");
                    nodeMainExecutorService.execute(visualClient, nodeConfiguration.setNodeName("deepLearnClient"));
                } else if (TextUtils.equals("DeepLearn", flag)) {
                    //视觉学习(service)
                    Log.e("ROS_Client", "Service：Start DeepLearn");
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "好的，正在学习中，请不同角度展示物体");
                    visualClient = new VisualClient((short) 2, name);
                    nodeMainExecutorService.execute(visualClient, nodeConfiguration.setNodeName("deepLearnClient"));
                } else if (TextUtils.equals("DeepLearnRec", flag)) {
                    //视觉识别(service)
                    Log.e("ROS_Client", "Service：Start DeepLearnRec");
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_DO_NOTHINF, "好的，正在识别中");
                    visualClient = new VisualClient((short) 3, "");
                    nodeMainExecutorService.execute(visualClient, nodeConfiguration.setNodeName("deepLearnClient"));
                } else if (TextUtils.equals("DeepLearnClose", flag)) {
                    //视觉关闭(service)
                    Log.e("ROS_Client", "Service：Start DeepLearnClose");
                    visualClient = new VisualClient((short) 4, "");
                    nodeMainExecutorService.execute(visualClient, nodeConfiguration.setNodeName("deepLearnClient"));
                } else if (TextUtils.equals("DeleteAllVisual", flag)) {
                    //视觉删除学习内容(service)
                    Log.e("ROS_Client", "Service：Start DeleteAllVisual");
                    visualClient = new VisualClient((short) 5, "");
                    nodeMainExecutorService.execute(visualClient, nodeConfiguration.setNodeName("deepLearnClient"));
                } else if (TextUtils.equals("SaveAMap", flag)) {
                    //保存地图(Service)
                    Log.e("ROS_Client", "Service:Start SaveAMap");
                    rmapClient = new RmapClient("robotai");
                    nodeMainExecutorService.execute(rmapClient, nodeConfiguration.setNodeName("RmapClient"));
                } else if (TextUtils.equals("World Navigation", flag)) {
                    //地图导航(Rapp)
                    Log.e("ROS_Client", "Rapp:Start World Navigation");
                    startRosInteraction(availableAppsCache, roconDescription.getCurrentRole(), "World Navigation");
                } else if (TextUtils.equals("PositionName", flag)) {
                    //语音获取位置坐标(Service)
                    Log.e("ROS_Client", "Service:Start PositionName");
                    if (TextUtils.equals("", name)) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "请告诉我这是哪里？");
                    }
                    double x = positionControler.getX();
                    double y = positionControler.getY();
                    positionControler.setName(name);
                    SpeechImpl.getInstance().cancelSpeak();
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取到" + name + "的当前X坐标为：" + x + ",Y坐标：" + y);
                    Log.e("ROS_Client", "获取到" + name + "的坐标为：" + "(" + x + "," + y + ")");
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，" + name + "记住了");
                    VisionRecogniseEnvironmentInfo info = new VisionRecogniseEnvironmentInfo();
                    info.setPositionName(name);
                    info.setPositionX(String.valueOf(x));
                    info.setPositionY(String.valueOf(y));
                    RobotDB.getInstance().addVisionRecogniseEnvironment(info);
                    Log.e("ROS_Client", "记录位置信息成功");
                } else if (TextUtils.equals("DestinationName", flag)) {
                    //导航到目标(Service)
                    Log.e("ROS_Client", "Service:Start DestinationName" + name);
                    if (TextUtils.equals("", name)) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "没有导航目标");
                        return;
                    }
                    VisionRecogniseEnvironmentInfo info = RobotDB.getInstance().getVisionRecogniseEnvironmentInfo(name);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (null == info) {
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "异常，没有导航目标");
                            return;
                        }
                        float x = Float.valueOf(info.getPositionX());
                        float y = Float.valueOf(info.getPositionY());
                        moveClient = new MoveClient("map", x, y, 0);
                        Log.e("ROS_Client", "导航目的地：" + name + "，坐标为：(" + x + "," + y + ")");
                        nodeMainExecutorService.execute(moveClient, nodeConfiguration.setNodeName("moveClient"));
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "导航开始,目标是：" + name);
                    }
                } else if (TextUtils.equals("Stop", flag)) {
                    Log.e("ROS_Client", "Stop Rapp");
                    doStopAction();
                }
            } else if (intent.getAction().equals(BroadcastAction.ACTION_CONTROL_ROBOT_MOVE_WITH_VOICE)) {
//                String direction=String.valueOf(intent.getIntExtra("direction",5));
//                Log.i("ROS_MOVE","语音控制时，得到的direction参数："+direction);
//                if (null==direction|| TextUtils.equals("", direction)) {
//                    return;
//                }
//                if (TextUtils.equals("1",direction)){
//                    moveClient=new MoveClient("base_link",1.0f,0.0f,0.0f);
//                }else if (TextUtils.equals("2",direction)){
//                    moveClient=new MoveClient("base_link",-1.0f,0.0f,0.0f);
//                }else if (TextUtils.equals("3",direction)){
//                    moveClient=new MoveClient("base_link",0.0f,0.0f,90.0f*2*CIRCLE/360.0f);
//                }else if (TextUtils.equals("4",direction)){
//                    moveClient=new MoveClient("base_link",0.0f,0.0f,-90.0f*2*CIRCLE/360.0f);
//                } else if (TextUtils.equals("5",direction)){
//                    moveClient=new MoveClient("base_link",0.0f,0.0f,0.0f);
//                }
//                nodeMainExecutorService.execute(moveClient,nodeConfiguration.setNodeName("moveClient"));

                String direction = String.valueOf(intent.getIntExtra("direction", 5));
                Log.i("ROS_MOVE", "ROS得到的direction参数：" + direction);
                if (TextUtils.equals("1", direction)) {
                    Log.e("ROS_MOVE","开始执行运动，方向：前进");
                    doMoveAction(direction);
                }else if (TextUtils.equals("2", direction)){
                    Log.e("ROS_MOVE","开始执行运动，方向：后退");
                    doMoveAction(direction);
                } else if (TextUtils.equals("3", direction)) {
                    Log.e("ROS_MOVE","开始执行运动，方向：向左转");
                    doTrunAction(mover.getCurrentDegree(), 270);
                } else if (TextUtils.equals("4", direction)) {
                    Log.e("ROS_MOVE","开始执行运动，方向：向右转");
                    doTrunAction(mover.getCurrentDegree(), 90);
                } else if (TextUtils.equals("5", direction)) {
                    Log.e("ROS_MOVE","开始执行运动，方向：停止");
                    doMoveAction(direction);
                }
            }else if (intent.getAction().equals(BroadcastAction.ACTION_WAKE_UP_TURN_BY_DEGREE)) {
                //方案一：（基于ROS地图服务）
//                Log.e("ROS_Client", "Service：Get WakeUp Degree");
//                float d = (float) intent.getIntExtra("degree", 0);
//                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取的唤醒角度是："+d);
//                moveClient=new MoveClient("base_link",0,0,2*CIRCLE-d*2*CIRCLE/360);
//                nodeMainExecutorService.execute(moveClient,nodeConfiguration.setNodeName("moveClient"));
                //方案二（直接控制Twist）
                double d = (double) intent.getIntExtra("degree", 0);
//                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取的唤醒角度是：" + d);
                doTrunAction(mover.getCurrentDegree(), d);
            } else if (intent.getAction().equals(BroadcastAction.ACTION_ROBOT_RADAR)) {
                //方案一：（基于ROS地图服务）
//                moveClient=new MoveClient("base_link",0,0,0);
//                nodeMainExecutorService.execute(moveClient,nodeConfiguration.setNodeName("moveClient"));
                //方案二：（直接控制Twist）
                Log.e("radar", "触发雷达安全距离，执行刹车指令");
                doMoveAction("5");
            }
        }
    };

    /*
    * 硬编码控制Robot前进
    * 描述：直接操作Twist
    */
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

    /*
    * 硬编码控制Robot转弯
    *
    */
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

    protected void startRosInteraction(final ArrayList<Interaction> apps, final String role, final String displayName) {
//        doStopAction();
        selectedInteraction = null;
        for (int i = 0; i < apps.size(); i++) {
            Log.e(TAG, "InteractionDisplayName:" + apps.get(i).getDisplayName());
            if (apps.get(i).getDisplayName().equals(displayName)) {
                selectedInteraction = apps.get(i);
                interactionsManager.requestAppUse(roconDescription.getMasterId(), role, selectedInteraction);
                statusPublisher.update(true, selectedInteraction.getHash(), selectedInteraction.getName());
                Log.e(TAG, "Current Start Rapp is：" + displayName);
            }
        }
    }

    protected void doStopAction() {
        pairSubscriber.setAppHash(0);
        statusPublisher.update(false, 0, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            Log.i("Remocon", "activityResult...RESULT_OK");
            if (requestCode == CONCERT_MASTER_CHOOSER_REQUEST_CODE) {
                init(intent);
            } else {
                // Without a master URI configured, we are in an unusable state.
                nodeMainExecutorService.shutdown();
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        shutdownRos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownRos();
        destroyService();
        // 如果city为空，代表没有定位成功，要把定位关掉
        if (TextUtils.isEmpty(city)) {
            map.destroyMap();
        }
    }


    //关闭Ros
    private void shutdownRos(){
        availableAppsCache.clear();
        nodeMainExecutorService.shutdownNodeMain(statusPublisher);
        nodeMainExecutorService.shutdownNodeMain(pairSubscriber);
        interactionsManager.shutdown();
        this.nodeMainExecutorService.forceShutdown();
        unregisterReceiver(receiver);
    }

    private void destroyService() {
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
