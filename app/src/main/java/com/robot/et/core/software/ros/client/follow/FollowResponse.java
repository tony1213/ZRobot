package com.robot.et.core.software.ros.client.follow;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/27.
 */
public interface FollowResponse extends Message {
    String _TYPE = "com.robot.et.core.software.ros.client.follow/FollowResponse";
    String _DEFINITION = "\nuint8 OK    = 0\nuint8 ERROR = 1\n\nuint8 result";
    byte OK = 0;
    byte ERROR = 1;

    byte getResult();

    void setResult(byte var1);
}
