package com.robot.et.core.software.ros.position;

import android.util.Log;

import com.robot.et.db.RobotDB;
import com.robot.et.entity.VisionRecogniseEnvironmentInfo;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

/**
 * Created by Tony on 2016/8/26.
 */
public class PositionControler extends AbstractNodeMain implements MessageListener<geometry_msgs.PoseWithCovarianceStamped> {

    private Subscriber<geometry_msgs.PoseWithCovarianceStamped> subscriber;
    private boolean isReady = false;

    public double x;
    public double y;
    public double z;

    public String name;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("RobotET/positionControler");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        subscriber = connectedNode.newSubscriber("amcl_pose", geometry_msgs.PoseWithCovarianceStamped._TYPE);
        subscriber.addMessageListener(this);
    }

    @Override
    public void onNewMessage(geometry_msgs.PoseWithCovarianceStamped message) {
//        message.getHeader();
        x = message.getPose().getPose().getPosition().getX();
        y = message.getPose().getPose().getPosition().getY();
        z = message.getPose().getPose().getPosition().getZ();
        Log.e("ROS_Client","获取到坐标X"+x+"坐标Y"+y);
//        SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT, "获取到坐标X"+x+"坐标Y"+y);
    }

    public void setName(String name){
        this.name=name;
    }

    public double getX(){
        Log.e("ROS_Client","x="+x);
        return x;
    }

    public double getY(){
        Log.e("ROS_Client","y="+y);
        return y;
    }

    public double getZ(){
        return z;
    }
}
