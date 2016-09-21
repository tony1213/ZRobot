package com.robot.et.core.software.ros.client.follow;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/27.
 */
public interface FollowRequest extends Message {
    String _TYPE = "com.robot.et.core.software.ros.client.follow/FollowRequest";
    String _DEFINITION = "uint8 STOPPED = 0\nuint8 FOLLOW  = 1\n\n# Following running/stopped\nuint8 state\n\n";
    byte STOPPED = 0;
    byte FOLLOW = 1;

    byte getState();

    void setState(byte var1);
}
