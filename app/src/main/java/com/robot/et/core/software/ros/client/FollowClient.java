package com.robot.et.core.software.ros.client;

import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.ros.follow.FollowRequest;
import com.robot.et.core.software.ros.follow.FollowResponse;

import org.ros.exception.RemoteException;
import org.ros.exception.RosRuntimeException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

/**
 * Created by Tony on 2016/8/27.
 */
public class FollowClient extends AbstractNodeMain {

    private byte state;

    public FollowClient(byte state){
        this.state = state;
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_services/rmapClient");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        ServiceClient<FollowRequest, FollowResponse> serviceClient = null;
        try {
            serviceClient = connectedNode.newServiceClient("/turtlebot_follower/change_state", com.robot.et.core.software.ros.follow.Follow._TYPE);
        } catch (ServiceNotFoundException e) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "服务未初始化，请初始化跟随服务");
            return;
//            throw new RosRuntimeException(e);
        }
        final FollowRequest request = serviceClient.newMessage();
        request.setState(state);
        serviceClient.call(request, new ServiceResponseListener<FollowResponse>() {
            @Override
            public void onSuccess(FollowResponse response) {
                Log.e("ROS_Client","onSuccess：FollowResponse"+response.getResult());
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "切换跟随状态");
            }

            @Override
            public void onFailure(RemoteException e) {
                Log.e("ROS_Client","onFailure");
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "跟随出现异常");
//                throw new RosRuntimeException(e);
            }
        });
    }
}
