package com.robot.et.core.software.ros.follow;

import org.ros.internal.message.Message;

/**
 * Created by Tony on 2016/8/27.
 */
public interface Follow extends Message {
    String _TYPE = "com.robot.et.core.software.ros.follow/Follow";
    String _DEFINITION = "uint8 STOPPED = 0\nuint8 FOLLOW  = 1\n\n# Following running/stopped\nuint8 state\n\n---\n\nuint8 OK    = 0\nuint8 ERROR = 1\n\nuint8 result\n";
}