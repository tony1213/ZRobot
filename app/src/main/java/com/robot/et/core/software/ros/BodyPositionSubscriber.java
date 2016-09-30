package com.robot.et.core.software.ros;

import android.util.Log;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;

import geometry_msgs.PointStamped;

/**
 * Created by wudong on 16/9/29.
 */
public class BodyPositionSubscriber extends AbstractNodeMain implements MessageListener<PointStamped>{

    private Subscriber<PointStamped> subscriber;

    public double x;
    public double y;
    public double z;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("RobotET/bodyPositionSubscriber");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        subscriber = connectedNode.newSubscriber("/tracker",PointStamped._TYPE);
        subscriber.addMessageListener(this);
    }

    @Override
    public void onNewMessage(PointStamped pointStamped) {
        x = pointStamped.getPoint().getX();
        y = pointStamped.getPoint().getY();
        z = pointStamped.getPoint().getZ();
    }


    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }
}
