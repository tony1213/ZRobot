package com.robot.et.core.software.ros.client;

import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;
import com.robot.et.core.software.ros.client.move.MoveRequest;
import com.robot.et.core.software.ros.client.move.MoveResponse;

import org.ros.exception.RemoteException;
import org.ros.exception.ServiceNotFoundException;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

/**
 * Created by wudong on 16/9/12.
 */
public class MoveGoalClient extends AbstractNodeMain {

    private String frame;
    private float x;
    private float y;
    private float angle;

    private  MoveGoalClient(){

    }

    private static MoveGoalClient moveGoalClient = null;

    public synchronized static MoveGoalClient getInstance(){
        if (moveGoalClient == null){
            moveGoalClient = new MoveGoalClient();
        }
        return moveGoalClient;
    }
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava_tutorial_services/moveClient");
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        ServiceClient<MoveRequest, MoveResponse> serviceClient=null;
        try {
            serviceClient = connectedNode.newServiceClient("set_goal", com.robot.et.core.software.ros.client.move.Move._TYPE);
        } catch (ServiceNotFoundException e) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "服务未初始化，请初始化移动服务");
            return;
        }
        final MoveRequest request = serviceClient.newMessage();
        request.setFrame(frame);
        request.setX(x);
        request.setY(y);
        request.setAngle(angle);
        serviceClient.call(request, new ServiceResponseListener<MoveResponse>() {
            @Override
            public void onSuccess(MoveResponse response) {
                Log.e("WorldNavigation", "response:" + response.getStatus());
                if (TextUtils.equals("SUCCESS",response.getStatus())){
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "目标到达成功");
                }else if (TextUtils.equals("FAILED",response.getStatus())){
                    SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "目标不可到达");
                }
            }

            @Override
            public void onFailure(RemoteException e) {
                SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "移动出现异常");
                Log.e("WorldNavigation", "onFailure");
//                throw new RosRuntimeException(e);
            }
        });
    }

    public void setFrame(String frame){
        this.frame = frame;
    }

    public void setPointX(float x){
        this.x = x;
    }

    public void setPointY(float y){
        this.y = y;
    }

    public void setPointAngle(float angle){
        this.angle = angle;
    }

    public void setPointGoal(float x,float y){
        this.x = x;
        this.y = y;
    }

    public void setPoseGoal(float x,float y,float angle){
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void setPoseGoalWithMap(String frame,float x,float y,float angle){
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

}
