package com.robot.et.core.software.ros.client;

import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.ros.client.visual.VisualRequest;
import com.robot.et.core.software.ros.client.visual.VisualResponse;

import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

public class VisualClient extends AbstractNodeMain {

    private short flag;
    private String name;

    public VisualClient(short flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_services/visualclient");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        ServiceClient<VisualRequest, VisualResponse> serviceClient =null;
        try {
            serviceClient = connectedNode.newServiceClient("rai_learning", com.robot.et.core.software.ros.client.visual.Visual._TYPE);
        } catch (ServiceNotFoundException e) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "服务未初始化，请初始化视觉服务");
            return;
//            throw new RosRuntimeException(e);
        }
        final VisualRequest request = serviceClient.newMessage();
        request.setRequestId(flag);
        request.setInputName(name);
        serviceClient.call(request, new ServiceResponseListener<VisualResponse>() {
            @Override
            public void onSuccess(VisualResponse response) {
                Log.e("ROS_Client", "onSuccess:Result:" + response.getStatus() + ",Name:" + response.getOutputName());
                if (flag == 1) {
                    //打开视觉
                    if (response.getStatus() == 0) {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉已启动");
                    } else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉启动失败");
                    }
                } else if (flag == 2) {
                    //视觉学习
                    if (response.getStatus()== -1){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉学习未开启");
                    }else if (response.getStatus()== 0){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "好的，记住了");
                    }else if (response.getStatus()==1){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太近了");
                    }else if (response.getStatus()==2){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太远了");
                    }else if (response.getStatus()==10){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太低了");
                    }else if (response.getStatus()==11){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太高了");
                    }else if (response.getStatus()==12){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太靠左了");
                    }else if (response.getStatus()==13){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太靠右了");
                    }else if (response.getStatus()==20){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "没看见有东西");
                    }else if (response.getStatus()==21){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "东西太小了，看不清");
                    }
                } else if (flag == 3) {
                    //视觉识别
                    if (response.getStatus()== -1){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉学习未开启");
                    }else if (response.getStatus()==0){
                        if (response.getConfidence()==0){
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "我不认识这个东西，让我学习一下吧！");
                        }else if (response.getConfidence()== 1){
                            //可能（40%以上）
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "这好像是："+response.getOutputName());
                        }else if (response.getConfidence()== 2){
                            //是（80%以上）
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "这是："+response.getOutputName());
                        }else if (response.getConfidence()== 3){
                            //一定（95%以上）
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "这是："+response.getOutputName());
                        }else if (response.getConfidence()== 10){
                            String[] temp=response.getOutputName().split("\\|");
                            Log.e("ROS_Client","content:"+temp[0]+",=="+temp[1]);
                            if (temp.length==2){
                                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "这个不是："+temp[0]+"就是："+temp[1]);
                            }else {
                                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "出现异常");
                            }
                        }
                    }else if (response.getStatus()==1){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太近了");
                    }else if (response.getStatus()==2){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "距离太远了");
                    }else if (response.getStatus()==10){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太低了");
                    }else if (response.getStatus()==11){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太高了");
                    }else if (response.getStatus()==12){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太靠左了");
                    }else if (response.getStatus()==13){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "物体太靠右了");
                    }else if (response.getStatus()==20){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "没看见有东西");
                    }else if (response.getStatus()==21){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "东西太小了，看不清");
                    }
                }else if (flag == 4){
                    //视觉识别
                    if (response.getStatus()==0){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "关闭视觉学习模块");
                    }else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "关闭视觉学习模块失败");
                    }
                }else if (flag == 5){
                    //删除所有视觉学习内容
                    if (response.getStatus()==0){
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "已删除所学内容");
                    }else {
                        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "删除学习内容失败");
                    }
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "视觉出现异常");
                Log.e("ROS_Client", "onFailure");
//                throw new RosRuntimeException(e);
            }
        });
    }
}